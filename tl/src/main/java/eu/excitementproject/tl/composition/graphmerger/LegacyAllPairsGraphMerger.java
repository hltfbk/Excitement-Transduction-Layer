package eu.excitementproject.tl.composition.graphmerger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * This graph merger performs the merge by comparing all possible node pairs. 
 * Note that in this implementation only "entailment" edges are added during the merge, while "non-entailment" edges are not added. 
 * I.e. absence of an edge in the merged graph should be interpreted as "no entailment"  

 * This is now a legacy class, it produces a valid raw graph, but the graph may not be processed properly by a GraphOptimizer
 * @author Lili Kotlerman
 *
 */public class LegacyAllPairsGraphMerger extends AbstractGraphMerger {
	public LegacyAllPairsGraphMerger(CachedLAPAccess lap, EDABasic<?> eda)
			throws GraphMergerException {
		super(lap, eda);
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(Set<FragmentGraph> fragmentGraphs,
			EntailmentGraphRaw workGraph) throws GraphMergerException {
		
		Logger mergeLogger = Logger.getLogger("eu.excitementproject.tl.composition.graphmerger.AllPairsGraphMerger"); 
		
		List<FragmentGraph> fg = new LinkedList<FragmentGraph>(fragmentGraphs);
		Collections.sort(fg, new FragmentGraph.CompleteStatementComparator());
		// Iterate over the list of fragment graphs and merge them one by one
		int i = 0;
		for (FragmentGraph fragmentGraph : fg){
			workGraph=mergeGraphs(fragmentGraph, workGraph);
			i++;
			mergeLogger.info("Merged FG #"+String.valueOf(i)+" out of "+String.valueOf(fg.size()));
		}
		
		return workGraph;
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(FragmentGraph fragmentGraph,
			EntailmentGraphRaw workGraph) throws GraphMergerException {
		
		// If the work graph is empty or null - just copy the fragment graph nodes/edges (there's nothing else to merge) and return the resulting graph
		if (workGraph==null) return new EntailmentGraphRaw(fragmentGraph, false);
		if (workGraph.isEmpty()) return new EntailmentGraphRaw(fragmentGraph, false, workGraph.hasLemmatizedLabel());
		
		 
		// else - merge new fragment graph into work graph 		
		workGraph.copyFragmentGraphNodesAndEntailingEdges(fragmentGraph);
		
		// find the node corresponding to the fragment graph's base statement in the work graph
		EntailmentUnit newBaseStatement = workGraph.getVertexWithText(fragmentGraph.getBaseStatement().getText());
		/* It might be that we already had this base statement in the raw graph, but resulting from a different completeStatement
		* e.g. The old clerk was very rude -> ... -> The clerk was rude
		*  vs. The young clerk was too rude -> ...-> The clerk was rude
		* so we want to keep track of which fragment we're merging 
		* and thus we keep the complete statement of the fragment graph, which is currently being merged */
		
		Set<EntailmentUnit> newFragmentGraphNodes;
		try {
			String fgCompleteStatement = fragmentGraph.getCompleteStatement().getText();
			newFragmentGraphNodes = new HashSet<EntailmentUnit>();
				Hashtable<Integer, Set<EntailmentUnit>> newFragmentGraph = workGraph.getFragmentGraphNodes(newBaseStatement, fgCompleteStatement);
				for (int level : newFragmentGraph.keySet()) newFragmentGraphNodes.addAll(newFragmentGraph.get(level));
		} catch (EntailmentGraphRawException e) {
			throw new GraphMergerException(e.getMessage());
		}
		
		// 2. For each of the new fragment graph nodes, calculate TE decision with all other nodes in the graph (except for the nodes in the current fragment graph and cases where an edge already exists) 
		for (EntailmentUnit newNode : newFragmentGraphNodes){
			for (EntailmentUnit node : workGraph.vertexSet()){
				if (newFragmentGraphNodes.contains(node)) continue; // don't compare with nodes from the same fragment graph
				if (workGraph.isEntailmentInAnyDirection(node, newNode)) continue; //don't compare, if already have a decision for the two nodes
				Set<EntailmentRelation> edgesToAdd = getEntailmentRelations(newNode, node);
				if (!edgesToAdd.isEmpty()){
					for (EntailmentRelation edge : edgesToAdd){
						workGraph.addEdge(edge.getSource(), edge.getTarget(), edge);
					}					
				}
			}
		}

		return workGraph;		
	}

}

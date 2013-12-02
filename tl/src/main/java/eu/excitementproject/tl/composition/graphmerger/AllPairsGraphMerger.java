package eu.excitementproject.tl.composition.graphmerger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * This graph merger performs the merge by comparing all possible node pairs. 
 * Note that in this implementation only "entailment" edges are added during the merge, while "non-entailment" edges are not added. 
 * I.e. absence of an edge in the merged graph should be interpreted as "no entailment"  

 * @author Lili Kotlerman
 *
 */public class AllPairsGraphMerger extends AbstractGraphMerger {

	public AllPairsGraphMerger(LAPAccess lap, EDABasic<?> eda)
			throws GraphMergerException {
		super(lap, eda);
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(Set<FragmentGraph> fragmentGraphs,
			EntailmentGraphRaw workGraph) throws GraphMergerException, LAPException {

		// Iterate over the list of fragment graphs and merge them one by one
		for (FragmentGraph fragmentGraph : fragmentGraphs){
			workGraph=mergeGraphs(fragmentGraph, workGraph);
		}
		return workGraph;
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(FragmentGraph fragmentGraph,
			EntailmentGraphRaw workGraph) throws GraphMergerException, LAPException {
		
		// If the work graph is empty or null - just copy the fragment graph nodes/edges (there's nothing else to merge) and return the resulting graph
		if (workGraph==null) return new EntailmentGraphRaw(fragmentGraph);
		if (workGraph.isEmpty()) return new EntailmentGraphRaw(fragmentGraph);
		
		 
		// else - merge new fragment graph into work graph 		
		workGraph.copyFragmentGraphNodesAndEdges(fragmentGraph);
		
		// find the node corresponding to the fragment graph's base statement in the work graph
		EntailmentUnit newBaseStatement = workGraph.getVertex(fragmentGraph.getBaseStatement().getText());
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
		
		// 2. For each of the new fragment graph nodes, calculate TE decision with all other nodes in the graph (except for the nodes in the current fragment graph and cases where an edge alredy exists) 
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

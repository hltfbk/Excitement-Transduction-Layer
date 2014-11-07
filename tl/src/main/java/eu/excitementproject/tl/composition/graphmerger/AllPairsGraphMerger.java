package eu.excitementproject.tl.composition.graphmerger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * This graph merger performs the merge by comparing all possible node pairs. 
 * Note that in this implementation both "entailment" and "non-entailment" edges are added during the merge. 
 * Yet, absence of an edge in the merged graph should be interpreted as "no entailment"  
 *
 * @author Lili Kotlerman
 *
 */public class AllPairsGraphMerger extends AbstractGraphMerger {
	public AllPairsGraphMerger(CachedLAPAccess lap, EDABasic<?> eda)
			throws GraphMergerException {
		super(lap, eda);
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(Set<FragmentGraph> fragmentGraphs,
			EntailmentGraphRaw workGraph) throws GraphMergerException, LAPException {
		
		Logger mergeLogger = Logger.getLogger("eu.excitementproject.tl.composition.graphmerger.AllPairsGraphMergerWithNonEntailments"); 
		
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
			EntailmentGraphRaw workGraph) throws GraphMergerException, LAPException {
		
		// If the work graph is empty or null - just copy the fragment graph nodes/edges (there's nothing else to merge) and return the resulting graph
		if (workGraph==null) workGraph = new EntailmentGraphRaw(fragmentGraph, true);
		if (workGraph.isEmpty()) workGraph = new EntailmentGraphRaw(fragmentGraph, true, workGraph.hasLemmatizedLabel());
		
		 
		// else - merge new fragment graph into work graph 		
		workGraph.copyFragmentGraphNodesAndAllEdges(fragmentGraph);

		// now for each pair of nodes, obtain and store the decision, if not yet defined
		for (EntailmentUnit src : workGraph.vertexSet()){
			for (EntailmentUnit tgt : workGraph.vertexSet()){
				if (src.equals(tgt)) continue;
				if (!workGraph.containsEdge(src, tgt)){ // only obtain new decision if no decision is defined
					EntailmentRelation edge = getRelation(src, tgt);
					workGraph.addEdge(edge.getSource(), edge.getTarget(), edge);
				}
			}
		}
		return workGraph;		
	}

}

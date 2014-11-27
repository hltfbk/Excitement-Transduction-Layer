package eu.excitementproject.tl.composition.graphmerger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * This graph merger performs the merge by only copying all fragment graphs into one raw graph 
 * (with unification of relevent {@link EntailmentUnitMention}s under one {@link EntailmentUnit} node). 
 * <p>Note that absence of an edge in the merged graph should be interpreted as "don't know", but can also be understood as "no entailment"  

 * @author Lili Kotlerman
 *
 */

public class NoEdaGraphMerger extends AbstractGraphMerger {

	/** Constructor, which calls the constructor of {@link AbstractGraphMerger} for the given LAP and EDA configurations.
	 * @param lap
	 * @param eda
	 * @throws GraphMergerException
	 */
	public NoEdaGraphMerger(CachedLAPAccess lap, EDABasic<?> eda)
			throws GraphMergerException {
		super(lap, eda);
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(Set<FragmentGraph> fragmentGraphs,
			EntailmentGraphRaw workGraph) throws GraphMergerException {
		List<FragmentGraph> fg = new LinkedList<FragmentGraph>(fragmentGraphs);
		Collections.sort(fg, new FragmentGraph.CompleteStatementComparator());
		// Iterate over the list of fragment graphs and merge them one by one
		for (FragmentGraph fragmentGraph : fg){
			workGraph=mergeGraphs(fragmentGraph, workGraph);
		}
		return workGraph;
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(FragmentGraph fragmentGraph,
			EntailmentGraphRaw workGraph) throws GraphMergerException {
		
		// If the work graph is empty or null - just copy the fragment graph nodes/edges (there's nothing else to merge) and return the resulting graph
		if (workGraph==null) return new EntailmentGraphRaw(fragmentGraph, true);
		if (workGraph.isEmpty()) return new EntailmentGraphRaw(fragmentGraph, true, workGraph.hasLemmatizedLabel());
		
		 
		// else - merge new fragment graph into work graph 		
//		workGraph.copyFragmentGraphNodesAndEntailingEdges(fragmentGraph);
		workGraph.copyFragmentGraphNodesAndAllEdges(fragmentGraph);
		
		return workGraph;		
	}

}

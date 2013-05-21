package eu.excitementproject.tl.composition.graphmerger;

import java.util.Set;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class AutomateWP2ProcedureGraphMerger extends AbstractGraphMerger {

	public AutomateWP2ProcedureGraphMerger(LAPAccess lap, EDABasic<?> eda)
			throws GraphMergerException {
		super(lap, eda);
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(Set<FragmentGraph> fragmentGraphs,
			EntailmentGraphRaw workGraph) throws GraphMergerException {

		// Iterate over the list of fragment graphs and merge them one by one
		for (FragmentGraph fragmentGraph : fragmentGraphs){
			mergeGraphs(fragmentGraph, workGraph);
		}
		return workGraph;
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(FragmentGraph fragmentGraph,
			EntailmentGraphRaw workGraph) throws GraphMergerException {
		
		// The the work graph is empty or null - just copy the fragment graph nodes/edges, there's nothing else to merge
		if(workGraph.isEmpty() || workGraph==null) workGraph = new EntailmentGraphRaw(fragmentGraph);
		else{ 
			// TODO Implement the WP2 flow			
		}
		
		return workGraph;		
	}

}

package eu.excitementproject.tl.composition.graphmerger;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class WP2ProcedureInduceClosureGraphMerger extends
		AutomateWP2ProcedureGraphMerger {

	public WP2ProcedureInduceClosureGraphMerger(CachedLAPAccess lap,
			EDABasic<?> eda) throws GraphMergerException {
		super(lap, eda);
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(FragmentGraph fragmentGraph,
			EntailmentGraphRaw workGraph) throws GraphMergerException, LAPException {		
		workGraph = super.mergeGraphs(fragmentGraph, workGraph);
		workGraph.applyTransitiveClosure(false);
		return workGraph;		
	}

}

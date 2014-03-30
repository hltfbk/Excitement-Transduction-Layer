package eu.excitementproject.tl.composition.graphmerger;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

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
	
	@Override
	protected EntailmentGraphRaw mergeBaseStatements(EntailmentGraphRaw workGraph, EntailmentUnit newBaseStatement, EntailmentUnit workGraphBaseStatement) throws GraphMergerException{
		//TODO 2nd if as else after the first if :))
		
		workGraph = super.mergeBaseStatements(workGraph, newBaseStatement, workGraphBaseStatement);
		
		if (workGraph.isEntailmentInSingleDirectionOnly(newBaseStatement, workGraphBaseStatement)){ // it can be that we have indication only in one direction, because it was induced by transitivity, but the 2nd direction was never checked, so need to check the 2nd direction 
			/*
			 * 		if (isEntailment(nodeA, nodeB)) {
			if (!isEntailment(nodeB, nodeA))return true;
		}
		if (isEntailment(nodeB, nodeA)) {
			if (!isEntailment(nodeA, nodeB)) return true;
		}		

			 */
		}
		return workGraph;
		
	}

}

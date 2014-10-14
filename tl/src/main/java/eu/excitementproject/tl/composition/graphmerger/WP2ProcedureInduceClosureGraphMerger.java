package eu.excitementproject.tl.composition.graphmerger;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
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
		workGraph.applyTransitiveClosure(); //legacy argument: changeTypeOfExistingEdges was false
		return workGraph;		
	}
	
	@Override
	protected Set<EntailmentRelation> mergeBaseStatements(EntailmentGraphRaw workGraph, EntailmentUnit newBaseStatement, EntailmentUnit workGraphBaseStatement) throws GraphMergerException{		
		//Check if there is entailment between the two base statements
		// There might be an existing entailment edge because the two base statements were present in the work graph before
		Set<EntailmentRelation> edgesToAdd = new HashSet<EntailmentRelation>();
		if (!workGraph.isEntailmentInAnyDirection(newBaseStatement, workGraphBaseStatement)){
			// If there's no existing entailment, check if there is entailment between the base statements
//			logger.info("Checking entailment between base statements");
			edgesToAdd = getEntailmentRelations(workGraphBaseStatement, newBaseStatement);
		}
		else if (workGraph.isEntailmentInSingleDirectionOnly(newBaseStatement, workGraphBaseStatement)){ // it can be that we have indication only in one direction, because it was induced by transitivity, but the 2nd direction was never checked, so need to check the 2nd direction 
			//TODO check the missing direction and add edge, if needed
			/*
			 * 		if (isEntailment(nodeA, nodeB)) {
			if (!isEntailment(nodeB, nodeA))return true;
		}
		if (isEntailment(nodeB, nodeA)) {
			if (!isEntailment(nodeA, nodeB)) return true;
		}		

			 */
		}
		return edgesToAdd;
		
	}

}

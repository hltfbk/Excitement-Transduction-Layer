package eu.excitementproject.tl.composition.graphmerger;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphEdge;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

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
		
		// If the work graph is empty or null - just copy the fragment graph nodes/edges, there's nothing else to merge
		if(workGraph.isEmpty() || workGraph==null) workGraph = new EntailmentGraphRaw(fragmentGraph);
		else{ 
			// Implement the WP2 flow
			
			// 1. Add the nodes and edges from the fragment graph into the work graph
			for (FragmentGraphEdge fragmentGraphEdge : fragmentGraph.edgeSet()){
				workGraph.addEdgeFromFrahmentGraph(fragmentGraphEdge);
			}
				// find the work graph node
			EntailmentUnit newBaseStatement = workGraph.getVertex(fragmentGraph.getBaseStatement().getText());
			
			// 2. For each base statement in the work graph perform merge with the fragment graph?
			// TODO find out how exactly :) 
			
			for (EntailmentUnit workGraphBaseStatement : workGraph.getBaseStatements()){
					// find the set of edges to add
				Set<EntailmentRelation> edgesToAdd = mergeOneBaseStatement(workGraph, workGraphBaseStatement, newBaseStatement);
					// add the edges to the work graph
				for (EntailmentRelation edge : edgesToAdd){
					workGraph.addEdge(edge.getSource(), edge.getTarget(), edge);
				}
			}
		}
		
		return workGraph;		
	}
	
	

	/**
	 * Performs one merge cycle - per one base statement from the workGraph
	 * @param workGraph
	 * @param workGraphbaseStatement
	 * @param newBaseStatement
	 * @return all the edges that should be added to merge the two graphs
	 * @throws GraphMergerException
	 */
	private Set<EntailmentRelation> mergeOneBaseStatement(EntailmentGraphRaw workGraph, EntailmentUnit workGraphBaseStatement, EntailmentUnit newBaseStatement) throws GraphMergerException {
		
		Set<EntailmentRelation> edgesToAdd = new HashSet<EntailmentRelation>(); 
		
		//Check if there is entailment between the two base statements
		// If there's no entailment between the base statements - done, nothing else to merge
		boolean isEntailment = false;
			// check one direction: workGraphBaseStatement -> newBaseStatement
		EntailmentRelation r = new EntailmentRelation(workGraphBaseStatement, newBaseStatement, this.getEda());
		if (r.getLabel().equals(DecisionLabel.Entailment)) {
			isEntailment=true;
			// add the edge to the output only if observed entailing, according to the WP2 algo
			// we don't need to store all the knowledge we have for WP2 graph merger
			edgesToAdd.add(r); 
		}

			// check the other direction: newBaseStatement -> workGraphBaseStatement
		r = new EntailmentRelation(newBaseStatement, workGraphBaseStatement, this.getEda());
		if (r.getLabel().equals(DecisionLabel.Entailment)) {
			isEntailment=true;
			edgesToAdd.add(r); 
		}
		
		if (!isEntailment) return edgesToAdd; // no entailment - we are done (return the empty edge set)
		
		// If there is entailment between the two statements

		// TODO: need to memorize the direction of the entailment and check in this direction for 1st-level nodes 
		// then propagate automatically
			
		
		
		return edgesToAdd;
		
	}
	

	
	

}

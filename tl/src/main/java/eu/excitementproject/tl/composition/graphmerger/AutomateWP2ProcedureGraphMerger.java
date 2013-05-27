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
				// find the node corresponding to the fragment graph's base statement in the work graph
			EntailmentUnit newBaseStatement = workGraph.getVertex(fragmentGraph.getBaseStatement().getText());
			
			// 2. For each base statement in the work graph perform merge with the fragment graph's base statement		
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
				 	
		//Check if there is entailment between the two base statements
		// If there's no entailment between the base statements - done, nothing else to merge
		Set<EntailmentRelation> edgesToAdd = getEntailmentRelations(workGraph, workGraphBaseStatement, newBaseStatement);
		if (edgesToAdd.isEmpty()) return edgesToAdd; // empty set = no entailment, i.e. we are done (return the empty edge set)
		
		// add the entailment relations between the 2 base statements
		for (EntailmentRelation edge : edgesToAdd){
			workGraph.addEdge(edge.getSource(), edge.getTarget(), edge);
		}
		// TODO: need to memorize the direction of the entailment and check in this direction for 1st-level nodes 
		// then propagate automatically
					
		return edgesToAdd;
		
	}
	
	private Set<EntailmentRelation> mergeFirstLevelInOneDirection(EntailmentGraphRaw workGraph, EntailmentUnit entailedBaseStatement, EntailmentUnit entailingBaseStatement){
		Set<EntailmentRelation> entailmentRelations = new HashSet<EntailmentRelation>();
		
		for (EntailmentUnit candidateEntailingtNode : workGraph.getEntailingNodes(entailingBaseStatement, 1)){
			for (EntailmentUnit candidateEntailedNode : workGraph.getEntailingNodes(entailedBaseStatement, 1 )){
				//TODO: define entailments
			}
		}
		return entailmentRelations;
	}
	

		

}

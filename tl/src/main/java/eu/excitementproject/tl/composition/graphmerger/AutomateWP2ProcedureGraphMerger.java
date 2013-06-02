package eu.excitementproject.tl.composition.graphmerger;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.eop.common.EDABasic;
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
		
		// If the work graph is empty or null - just copy the fragment graph nodes/edges (there's nothing else to merge) and return the resulting graph
		if(workGraph.isEmpty() || workGraph==null) return new EntailmentGraphRaw(fragmentGraph);
		
		 
		// else - Implement the WP2 flow
		
		// 1. Add the nodes and edges from the fragment graph into the work graph
		for (FragmentGraphEdge fragmentGraphEdge : fragmentGraph.edgeSet()){
			workGraph.addEdgeFromFragmentGraph(fragmentGraphEdge, fragmentGraph);
		}
		
			// find the node corresponding to the fragment graph's base statement in the work graph
		EntailmentUnit newBaseStatement = workGraph.getVertex(fragmentGraph.getBaseStatement().getText());
		
		// 2. For each base statement in the work graph perform merge with the fragment graph's base statement		
		for (EntailmentUnit workGraphBaseStatement : workGraph.getBaseStatements()){
			if (workGraphBaseStatement.equals(newBaseStatement)) continue; // don't check new (fragment graph's) base statement with itself
			// find the set of edges to add
			Set<EntailmentRelation> edgesToAdd = mergeOneBaseStatement(workGraph, workGraphBaseStatement, newBaseStatement);
				// add the edges to the work graph
			for (EntailmentRelation edge : edgesToAdd){
				workGraph.addEdge(edge.getSource(), edge.getTarget(), edge);
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
		Set<EntailmentRelation> edgesToAdd = getEntailmentRelations(workGraphBaseStatement, newBaseStatement);
		if (edgesToAdd.isEmpty()) return edgesToAdd; // empty set = no entailment, i.e. we are done (return the empty edge set)
		
		// add the entailment relation(s) between the 2 base statements
		for (EntailmentRelation edge : edgesToAdd){
			workGraph.addEdge(edge.getSource(), edge.getTarget(), edge);
		}
		// TODO: need to look at the direction of the entailment and check in this direction for 1st-level nodes 
		if (workGraph.isEntailment(workGraphBaseStatement, newBaseStatement)) {
			for (EntailmentRelation r: mergeFirstLevel(workGraph, workGraphBaseStatement, newBaseStatement)){
				edgesToAdd.add(r);
			}
		}
		// then propagate automatically
					
		return edgesToAdd;
		
	}
	
	/**
	 * This method is called when entailment relation A->B was detected between 2 base statements.
	 * It receives a work graph and two base statements - one from the work graph, and the other from the fragment graph that is currently being merged with the work graph
	 * The method checks for entailment in all pairs candidateEntailingtNode->candidateEntailedNode
	 * where candidateEntailingtNode-s are one-modifier (level=1) nodes, which entail A
	 * and candidateEntailedNode-s are one-modifier (level=1) nodes, which entail B. 
	 * The method returns all the entailmentRelations that were detected.  
	 * @param workGraph
	 * @param entailingBaseStatement - the base statement A, which entails the other base statement B (A->B)
	 * @param entailedBaseStatement - the base statement B, which is entailed by A
	 * @return set of EntailmentRelation-s that were detected 
	 */
	private Set<EntailmentRelation> mergeFirstLevel(EntailmentGraphRaw workGraph, EntailmentUnit entailingBaseStatement, EntailmentUnit entailedBaseStatement){
		Set<EntailmentRelation> entailmentRelations = new HashSet<EntailmentRelation>();
		
		// get the candidates (1-modidier nodes, which entail each of the base statements) 
		for (EntailmentUnit candidateEntailingtNode : workGraph.getEntailingNodes(entailingBaseStatement, 1)){
			for (EntailmentUnit candidateEntailedNode : workGraph.getEntailingNodes(entailedBaseStatement, 1 )){
				// check if there is already such a node in the graph. If yes - go to the next candidate pair
				if (workGraph.isEntailment(candidateEntailingtNode, candidateEntailedNode)) continue; 
				// check whether there is entailment. If yes - add to the set 
				EntailmentRelation r = getEntailmentRelation(candidateEntailingtNode, candidateEntailedNode);
				if (r!=null) entailmentRelations.add(r);
			}
		}
		return entailmentRelations;
	}
	

	/*private Set<EntailmentRelation> induceUpperLevels(EntailmentGraphRaw workGraph, EntailmentUnit entailingBaseStatement, EntailmentUnit entailedBaseStatement){
		Set<EntailmentRelation> entailmentRelations = new HashSet<EntailmentRelation>();
		
		 
				
		return entailmentRelations;
		
	}*/
	
	
/*	private Set<EntailmentUnit> getUpperLevelNodes(EntailmentGraphRaw workGraph, EntailmentUnit currentNode, Set<Long> fragmentGraphIds, Set<EntailmentUnit> upperLevelNodes){
		
		for (EntailmentUnit eu : workGraph.getEntailingNodes(currentNode, currentNode.getLevel()+1)){
			boolean isFromTheSameFragmentGraph = false;
			for (long id : fragmentGraphIds){
				if (eu.isFromFragmentGraph(id)) {
					isFromTheSameFragmentGraph=true;
					break;
				}
			}
			if (isFromTheSameFragmentGraph) upperLevelNodes.add(eu);
			getUpperLevelNodes(workGraph, eu, fragmentGraphIds, upperLevelNodes);
		}
			
		return upperLevelNodes;		
	}
*/		

}

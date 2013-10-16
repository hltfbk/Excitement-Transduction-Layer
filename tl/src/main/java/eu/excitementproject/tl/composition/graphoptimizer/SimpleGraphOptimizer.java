package eu.excitementproject.tl.composition.graphoptimizer;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.CycleDetector;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

public class SimpleGraphOptimizer extends AbstractGraphOptimizer{

	@Override
	public EntailmentGraphCollapsed optimizeGraph(
			EntailmentGraphRaw workGraph)
			throws CollapsedGraphGeneratorException {
		
		return optimizeGraph(workGraph, getAverageConfidenceOfEntailment(workGraph));
	}

	@Override
	public EntailmentGraphCollapsed optimizeGraph(
			EntailmentGraphRaw workGraph, Double confidenceThreshold)
			throws CollapsedGraphGeneratorException {
		
		// Step 1 - clean up the work graph
		
		//-  Only retain "entailment" edges. 
		//   (note: AutomateWP2ProcedureGraphMerger only generates "entailment" edges)		
		// - Remove all "entailment" edges with confidence < confidenceThreshold
		
		Set<EntailmentRelation> workEdgesToRemove = new HashSet<EntailmentRelation>();
		for (EntailmentRelation workEdge : workGraph.edgeSet()){
			if (!workEdge.getLabel().equals(DecisionLabel.Entailment)){
				workEdgesToRemove.add(workEdge);
			}
			else{ // if this is an "entailment" edge
				if(workEdge.getConfidence()<confidenceThreshold) {
					workEdgesToRemove.add(workEdge);
				}
			}
		}
		workGraph.removeAllEdges(workEdgesToRemove);
		
		// Step 2 - create collapsed graph from the cleaned-up work graph (copy nodes and edges)

		// - Find cycles and unite all nodes in each cycle to one equivalence class
		Set<EquivalenceClass> equivalenceClasses = findEquivalenceClassesAsCycles(workGraph);
		// - Create an empty collapsed graph and place the newly found equivalence classes as its nodes
		EntailmentGraphCollapsed collapsedGraph = new EntailmentGraphCollapsed();
		for (EquivalenceClass equivalenceClass : equivalenceClasses){
			collapsedGraph.addVertex(equivalenceClass);
		}
		
		// - Copy all the nodes which are not included in the previously found entailment classes
		//   ( make an equivalence class out of every such node and add it to the graph)
		for (EntailmentUnit workGraphNode : workGraph.vertexSet()){
			if (!containsEntailmentUnit(collapsedGraph.vertexSet(),workGraphNode)){
				collapsedGraph.addVertex(new EquivalenceClass(workGraphNode));
			}
		}
		
/*		// - Copy edges (in case of multiple edges assign max confidence)
		for (EntailmentRelation workGraphEdge : workGraph.edgeSet()){
			EquivalenceClass source = collapsedGraph.getVertex(workGraphEdge.getSource());
			if (source==null) throw new CollapsedGraphGeneratorException("Adding edges to the collapsed graph. Cannot find the equivalence class node, which includes the entailment unit "+workGraphEdge.getSource());
			EquivalenceClass target = collapsedGraph.getVertex(workGraphEdge.getTarget());
			if (target==null) throw new CollapsedGraphGeneratorException("Adding edges to the collapsed graph. Cannot find the equivalence class node, which includes the entailment unit "+workGraphEdge.getTarget());
			
			if (source.equals(target)) continue; // if source and target of the work graph edge are both mapped to the same equivalence class - don't add this edge (this will be a loop)  
			
			double maxConfidence = 0.0; 
			for (EntailmentRelationCollapsed existingEdge : collapsedGraph.getAllEdges(source, target)){
				// actually, if this set of existing edges is not empty, there should be a single edge in this set, since we do not allow multiple edges between the same pair of (source,target) for the collapsed graph
				if (existingEdge.getConfidence()>maxConfidence){
					maxConfidence = existingEdge.getConfidence(); // update max confidence
				}
			}
			// now if the candidate edge from the work graph has a higher confidence
			// remove previous edge source->target (if exists) and add a new one with updated confidence
			if (workGraphEdge.getConfidence()>maxConfidence){
				collapsedGraph.removeAllEdges(source, target);
				EntailmentRelationCollapsed newEdge = new EntailmentRelationCollapsed(source, target, workGraphEdge.getConfidence());
				collapsedGraph.addEdge(source, target, newEdge);
			}
		}
*/		

		// - Copy edges (in case of multiple edges assign min confidence)
		for (EntailmentRelation workGraphEdge : workGraph.edgeSet()){
			EquivalenceClass source = collapsedGraph.getVertex(workGraphEdge.getSource());
			if (source==null) throw new CollapsedGraphGeneratorException("Adding edges to the collapsed graph. Cannot find the equivalence class node, which includes the entailment unit "+workGraphEdge.getSource());
			EquivalenceClass target = collapsedGraph.getVertex(workGraphEdge.getTarget());
			if (target==null) throw new CollapsedGraphGeneratorException("Adding edges to the collapsed graph. Cannot find the equivalence class node, which includes the entailment unit "+workGraphEdge.getTarget());
			
			if (source.equals(target)) continue; // if source and target of the work graph edge are both mapped to the same equivalence class - don't add this edge (this will be a loop)  
			
			double minConfidence = 100.0; 
			for (EntailmentRelationCollapsed existingEdge : collapsedGraph.getAllEdges(source, target)){
				// actually, if this set of existing edges is not empty, there should be a single edge in this set, since we do not allow multiple edges between the same pair of (source,target) for the collapsed graph
				if (existingEdge.getConfidence()<minConfidence){
					minConfidence = existingEdge.getConfidence(); // update max confidence
				}
			}
			// now if the candidate edge from the work graph has a higher confidence
			// remove previous edge source->target (if exists) and add a new one with updated confidence
			if (workGraphEdge.getConfidence()<minConfidence){
				collapsedGraph.removeAllEdges(source, target);
				EntailmentRelationCollapsed newEdge = new EntailmentRelationCollapsed(source, target, workGraphEdge.getConfidence());
				collapsedGraph.addEdge(source, target, newEdge);
			}
		}
		
		// Step 3 - resolve transitivity violations
		// no need to do it - as soon as we have no cycles, we have no transitivity violations
		// since "transitivity violation" between "entailment" nodes is always a cycle
		// (a -> b and b -> c, and then instead of a->c we have c->a)
		// => if we only have "entailment" nodes, there's no need to resolve anything
		// If we had "no-entailment" nodes, we could have violations like
		// a-> and b->c, but a-not entails-c (with high confidence) - those should be resolves (not in this SimpleCollapseGraphGenerator) 
		
		
		return collapsedGraph;
	}
	
	
	public Set<EquivalenceClass> findEquivalenceClassesAsCycles(EntailmentGraphRaw workGraph){
		
		if (workGraph==null) return null;
		
		Set<EquivalenceClass> cycles = new HashSet<EquivalenceClass>();
		
		CycleDetector<EntailmentUnit,EntailmentRelation> cycleDetector= new CycleDetector<EntailmentUnit,EntailmentRelation>(workGraph);
		// find the set of all vertices which participate in at least one cycle in this graph 
		Set<EntailmentUnit> cycleNodes = cycleDetector.findCycles();
					
		// for each such vertex, find all the vertices in the corresponding cycle
		// these vertices are to form one equivalence class
		for (EntailmentUnit currentNode : cycleNodes){
			Set<EntailmentUnit> currentCycle = cycleDetector.findCyclesContainingVertex(currentNode);
			for (EntailmentUnit nodeInCurrentCycle: currentCycle){
				// if a node in the current cycle was already seen as part of another cycle, 
				// then the current and the previously found cycles of this node should be merged
				EquivalenceClass previousRelatedCycle = getEquivalenceClass(cycles, nodeInCurrentCycle);
				if (previousRelatedCycle!=null){ // if such previous cycle was found
					cycles.remove(previousRelatedCycle); // remove it from cycles
					previousRelatedCycle.add(currentCycle); // unite the previous one with the current one
					cycles.add(previousRelatedCycle); // put the updated cycle back
				}
				else{ // if no previous related cycle was found, create the new one
					cycles.add(new EquivalenceClass(currentCycle));  
				}
			}
		}		
		return cycles;
	}	

}

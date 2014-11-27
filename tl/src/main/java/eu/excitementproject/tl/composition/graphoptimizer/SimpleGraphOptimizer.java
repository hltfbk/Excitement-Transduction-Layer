package eu.excitementproject.tl.composition.graphoptimizer;

import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

import org.jgrapht.alg.CycleDetector;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * Simple implementation of {@link GraphOptimizer} interface. 
 * Removes all non-entailment edges and entailment edges with confidence < threshold, and then collapses each cycle into a single {@link EquivalenceClass} node 
 * 
 * <p> This optimizer can be used to create a collapsed version ({@link EntailmentGraphCollapsed}) of a consistent (with no transitivity violations) raw graph. 
 * @author Lili Kotlerman
 *
 */
public class SimpleGraphOptimizer extends AbstractGraphOptimizer{

	private final Logger logger = Logger.getLogger(SimpleGraphOptimizer.class);
	
	@Override
	public EntailmentGraphCollapsed optimizeGraph(
			EntailmentGraphRaw workGraph)
			throws GraphOptimizerException {
		
		return optimizeGraph(workGraph, 0.0);
	}

	@Override
	public EntailmentGraphCollapsed optimizeGraph(
			EntailmentGraphRaw graph, Double confidenceThreshold)
			throws GraphOptimizerException {
		
		EntailmentGraphRaw workGraph = new EntailmentGraphRaw(graph.vertexSet(), graph.edgeSet());
		
		// Step 1 - clean up the work graph
		
		//-  Only retain "entailment" edges. 
		//   (note: AutomateWP2ProcedureGraphMerger only generates "entailment" edges)		
		// - Remove all "entailment" edges with confidence < confidenceThreshold
		
		Set<EntailmentRelation> workEdgesToRemove = new HashSet<EntailmentRelation>();
		for (EntailmentRelation workEdge : workGraph.edgeSet()){
			if (!workEdge.getLabel().is(DecisionLabel.Entailment)){
				workEdgesToRemove.add(workEdge);
			}
			else{ // if this is an "entailment" edge
				if(workEdge.getConfidence()<confidenceThreshold) {
					workEdgesToRemove.add(workEdge);
				}
			}
		}
		workGraph.removeAllEdges(workEdgesToRemove);
		logger.info("Removed "+String.valueOf(workEdgesToRemove.size())+" low-confidence edges.");
		
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
			if (source==null) throw new GraphOptimizerException("Adding edges to the collapsed graph. Cannot find the equivalence class node, which includes the entailment unit "+workGraphEdge.getSource());
			EquivalenceClass target = collapsedGraph.getVertex(workGraphEdge.getTarget());
			if (target==null) throw new GraphOptimizerException("Adding edges to the collapsed graph. Cannot find the equivalence class node, which includes the entailment unit "+workGraphEdge.getTarget());
			
			if (source.equals(target)) continue; // if source and target of the work graph edge are both mapped to the same equivalence class - don't add this edge (this will be a loop)  
			
			double minConfidence = Double.MAX_VALUE; 
			for (EntailmentRelationCollapsed existingEdge : collapsedGraph.getAllEdges(source, target)){
				// actually, if this set of existing edges is not empty, there should be a single edge in this set, since we do not allow multiple edges between the same pair of (source,target) for the collapsed graph
				if (existingEdge.getConfidence()<minConfidence){
					minConfidence = existingEdge.getConfidence(); // update the confidence
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
		// WE CAN HAVE "a -> b" and "b -> c", but the edge "a -> c" missing, denoting "no entailment", and then it's a violation! Can resolve by adding this missing edge
		// If we have no-entailment edges, we can have explicit violations like this
		
		return collapsedGraph;
	}
	
	
	public Set<EquivalenceClass> findEquivalenceClassesAsCycles(EntailmentGraphRaw workGraph){
		
		if (workGraph==null) return null;
		
		Set<EquivalenceClass> cycles = new HashSet<EquivalenceClass>();
		
		CycleDetector<EntailmentUnit,EntailmentRelation> cycleDetector= new CycleDetector<EntailmentUnit,EntailmentRelation>(workGraph);
		// find the set of all vertices which participate in at least one cycle in this graph 
		Set<EntailmentUnit> cycleNodes = cycleDetector.findCycles();
					
		Set<EntailmentUnit> closedList = new HashSet<EntailmentUnit>();
		
		// for each such vertex, find all the vertices in the corresponding cycle
		// these vertices are to form one equivalence class
		for (EntailmentUnit currentNode : cycleNodes){
			if (closedList.contains(currentNode)) {
				logger.debug("Skipping node <<"+currentNode.getText()+">> since equivalence class with this node was already generated");
				continue; // if already generated an equivalence class with this node - no need to do it again
			}
			closedList.add(currentNode);
			EquivalenceClass currentCycle = new EquivalenceClass(cycleDetector.findCyclesContainingVertex(currentNode));
			logger.debug("Current node: "+currentNode.getText());
			logger.debug(currentCycle.getEntailmentUnits().size()+" nodes in cycle:");
			for (EntailmentUnit nodeInCurrentCycle: currentCycle.getEntailmentUnits()){
				closedList.add(nodeInCurrentCycle);
				logger.debug("-- "+nodeInCurrentCycle.getText());
				// if a node in the current cycle was already seen as part of another cycle, 
				// then the current and the previously found cycles of this node should be merged
				EquivalenceClass previousRelatedCycle = getEquivalenceClass(cycles, nodeInCurrentCycle);
				if (previousRelatedCycle!=null){ // if such previous cycle was found
					if (previousRelatedCycle.toString().equals(currentCycle.toString())) continue; // if it's not the same cycle as the c
					logger.debug("\t>> found in another cycle of "+String.valueOf(previousRelatedCycle.getEntailmentUnits().size())+" entailment units");
					logger.debug("\t>> " +previousRelatedCycle+"\n");
					cycles.remove(previousRelatedCycle); // remove it from cycles
					previousRelatedCycle.add(currentCycle.getEntailmentUnits()); // unite the previous one with the current one
					cycles.add(previousRelatedCycle); // put the updated cycle back
				}
				else{ // if no previous related cycle was found, create the new one
					cycles.add(currentCycle);  
				}				
			}
		}		
		return cycles;
	}	

}

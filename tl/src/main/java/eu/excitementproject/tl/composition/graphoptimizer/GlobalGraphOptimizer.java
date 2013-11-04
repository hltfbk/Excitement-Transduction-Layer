package eu.excitementproject.tl.composition.graphoptimizer;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.globalgraphoptimizer.api.UntypedPredicateGraphLearner;
import eu.excitementproject.eop.globalgraphoptimizer.defs.Pair;
import eu.excitementproject.eop.globalgraphoptimizer.edgelearners.EdgeLearner;
import eu.excitementproject.eop.globalgraphoptimizer.edgelearners.EfficientlyCorrectHtlLearner;
import eu.excitementproject.eop.globalgraphoptimizer.graph.AbstractOntologyGraph;
import eu.excitementproject.eop.globalgraphoptimizer.graph.AbstractRuleEdge;
import eu.excitementproject.eop.globalgraphoptimizer.graph.DirectedOneMappingOntologyGraph;
import eu.excitementproject.eop.globalgraphoptimizer.graph.DirectedOntologyGraph;
import eu.excitementproject.eop.globalgraphoptimizer.graph.RelationNode;
import eu.excitementproject.eop.globalgraphoptimizer.graph.RuleEdge;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.TEDecisionWithConfidence;

/**
 * @author Meni Adler & Lili Kotlerman
 * @since Oct 29, 2013
 *
 * This implementation of GraphOptimizer interface performs global graph optimization as described in Berant et.al (TODO find the exact reference!)
 * ATTENTION: this implementation is under development, please use SimpleGraphOptimizer for now.
 * 
 * TODO: Verify whether the algorithm involves randomization. It seems that the output may differ for the same input.
 */
public class GlobalGraphOptimizer extends AbstractGraphOptimizer {

	protected final static double DEFAULT_EDGE_COST = 0.5;
	protected final static double DEFAULT_UNKNOWN_SCORE = 0;
	
	public GlobalGraphOptimizer() {
		this(DEFAULT_EDGE_COST, DEFAULT_UNKNOWN_SCORE, new HashSet<Pair<String,String>>(), new HashSet<Pair<String,String>>());
	}
	
	public GlobalGraphOptimizer(double edgeCost, double unknownScore) {
		this(edgeCost, unknownScore, new HashSet<Pair<String,String>>(), new HashSet<Pair<String,String>>());
	}
	
	public GlobalGraphOptimizer(Set<Pair<String,String>> entailings, Set<Pair<String,String>> nonentailings)  {
		this(DEFAULT_EDGE_COST, DEFAULT_UNKNOWN_SCORE, entailings, entailings);
	}
	
	public GlobalGraphOptimizer(double edgeCost, double unknownScore, Set<Pair<String,String>> entailings, Set<Pair<String,String>> nonentailings)  {
		boolean convertProb2Score = false;
		this.edgeLearner = new EfficientlyCorrectHtlLearner(edgeCost);
		this.graphLearner = new UntypedPredicateGraphLearner(this.edgeLearner, convertProb2Score, entailings,nonentailings, unknownScore);		
	}



	@Override
	public EntailmentGraphCollapsed optimizeGraph(EntailmentGraphRaw workGraph) throws GraphOptimizerException {
		return optimizeGraph(workGraph,0.0);
	}

	@Override
	public EntailmentGraphCollapsed optimizeGraph(EntailmentGraphRaw workGraph, Double confidenceThreshold) throws GraphOptimizerException{
		DirectedOntologyGraph graph = new DirectedOneMappingOntologyGraph("work graph");

		/* Commented out: 
		 * Here we only add nodes connected by edges in the graph, and we consider all existing edges to denote entailment. 
		 * We should 1) add "orphan" nodes, not connected with any other node.
		 *           2) check whether edges hold ENTAILMENT relation or not  
		 *           3) consider adding edges NOT present in the graph as non-entailment edges with some confidence.
		 * In addition, we should consider all the edges with confidence < confidenceThreshold as non-entailment    
		 */
	/*	int i = 1;
		for (EntailmentRelation entailmentRelation : workGraph.edgeSet()) {
			EntailmentUnit source = entailmentRelation.getSource();
			EntailmentUnit target = entailmentRelation.getTarget();
			Double confidence = entailmentRelation.getConfidence();
			if (confidence == TEDecision.CONFIDENCE_NOT_AVAILABLE)
				throw new GraphOptimizerException("Unavaliable score was detected.");
			RelationNode sourceNode = new RelationNode(i++,source.getText());
			RelationNode targetNode = new RelationNode(i++,target.getText());
			try {
				graph.addEdge(new RuleEdge(sourceNode, targetNode,confidence));
			} catch (Exception e) {
				throw new GraphOptimizerException("Problem when adding edge "+source.getText()+" -> "+target.getText()+" with confidence = "+confidence+".\n"+e);
			}
		}*/

		int i = 1;
		for (EntailmentUnit source : workGraph.vertexSet()) {
			for (EntailmentUnit target : workGraph.vertexSet()){
				if (source.equals(target)) continue; // don't add loops
				Double confidence = detectConfidence(workGraph, source, target,confidenceThreshold);
				if (confidence == TEDecision.CONFIDENCE_NOT_AVAILABLE)
					throw new GraphOptimizerException("Unavaliable score was detected.");
				RelationNode sourceNode = new RelationNode(i++,source.getText());
				RelationNode targetNode = new RelationNode(i++,target.getText());
				try {
					graph.addEdge(new RuleEdge(sourceNode, targetNode,confidence));
				} catch (Exception e) {
					throw new GraphOptimizerException("Problem when adding edge "+source.getText()+" -> "+target.getText()+" with confidence = "+confidence+".\n"+e);
				}
			}
		}
		
		Set<AbstractOntologyGraph> componnetGraphs;
		try {
			componnetGraphs = graphLearner.learn(graph);
		} catch (Exception e) {
			throw new GraphOptimizerException("Problem with global optimization.\n"+e);
		}
		EntailmentGraphCollapsed ret = new EntailmentGraphCollapsed();

		/* Commented out: 
		 * Here we only add nodes connected by edges in the optimized graph, while we need all the nodes from original graph to be covered in the output. 
		 * We should 1) add "orphan" nodes, not connected with any other node.
		 *           2) collapse paraphrasing nodes into equivalence classes
		 */
		/*	for (AbstractOntologyGraph componnetGraph : componnetGraphs) {
			for (AbstractRuleEdge componentEdge : componnetGraph.getEdges()) {
		//		if (componentEdge.score() >= confidenceThreshold) { //TODO: do we need the threshold here?					
//					EquivalenceClass sourceVertex = new EquivalenceClass(new EntailmentUnit(componentEdge.from().description(),null,null,null,-1));
//					EquivalenceClass targetVertex = new EquivalenceClass(new EntailmentUnit(componentEdge.to().description(),null,null,null,-1));;
					EquivalenceClass sourceVertex = new EquivalenceClass(workGraph.getVertex(componentEdge.from().description()));
					EquivalenceClass targetVertex = new EquivalenceClass(workGraph.getVertex(componentEdge.to().description()));
					EntailmentRelationCollapsed edge = new EntailmentRelationCollapsed(sourceVertex,targetVertex,componentEdge.score());
					ret.addEdgeWithNodes(sourceVertex,targetVertex,edge);
					System.out.println(sourceVertex+" -> "+targetVertex+" "+edge.getConfidence());
		//		}
			}
		}*/
		
		EntailmentGraphRaw tmpRawGraph = new EntailmentGraphRaw();
		for (EntailmentUnit node : workGraph.vertexSet()){
			tmpRawGraph.addVertex(node);
		}
		for (AbstractOntologyGraph componnetGraph : componnetGraphs) {
			for (AbstractRuleEdge componentEdge : componnetGraph.getEdges()) {
				if (componentEdge.score() >= confidenceThreshold) { //TODO: do we need the threshold here? Should it be confidenceThreshold or just 0 (to retain only entailment edges)? 				
					EntailmentUnit source = workGraph.getVertex(componentEdge.from().description());
					EntailmentUnit target=  workGraph.getVertex(componentEdge.to().description());
					EntailmentRelation edge = new EntailmentRelation(source, target, new TEDecisionWithConfidence(componentEdge.score(), DecisionLabel.Entailment));
					tmpRawGraph.addEdge(source, target, edge);
				}
			}
		}
		
		ret = new SimpleGraphOptimizer().optimizeGraph(tmpRawGraph, 0.0); //collapse paraphrasing nodes
		return ret;
	}
	
	private Double detectConfidence(EntailmentGraphRaw workGraph, EntailmentUnit source, EntailmentUnit target, Double confidenceThreshold){
		Double confidence = -0.9; // this will be our non-entailment confidence for missing edges 
		if (workGraph.containsEdge(source, target)) {
			EntailmentRelation edge = workGraph.getEdge(source, target);
			if(edge.getTEdecision().getDecision().is(DecisionLabel.Entailment)) {
				 if (edge.getConfidence() > confidenceThreshold) confidence = edge.getConfidence(); // only if the original score is higher than the threshold, consider the edge entailing with the corresponding confidence. Otherwise treat it as if it's not present in the work graph. 
			}
			else confidence = -1.0*edge.getConfidence();			
		}
		
		return confidence;
	}

	protected UntypedPredicateGraphLearner graphLearner;
	protected EdgeLearner edgeLearner;
}

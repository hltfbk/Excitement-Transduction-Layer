package eu.excitementproject.tl.composition.graphoptimizer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

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
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
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

	private final Logger logger = Logger.getLogger(GlobalGraphOptimizer.class);
	
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
		 *           2) add edge with pos or neg score for each pair of nodes, as follows
		 *             - FG edges should become constraints (get very-very high score for entailing, low for non-entailing) 
		 *             - check whether work graph edges hold ENTAILMENT relation or not and assign pos/neg score accordingly 
		 *             - consider edges not present in the graph as non-entailment edges with some confidence.
		 *             - consider all the edges with confidence < confidenceThreshold as non-entailment    
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

	
		HashMap<String, Integer> nodeIndex = new HashMap<String, Integer>(); 
		int i = 1;
		for (EntailmentUnit node : workGraph.vertexSet()) {
			nodeIndex.put(node.getText(), i);
			RelationNode rNode = new RelationNode(i++,node.getText());
			graph.addNode(rNode);
		}
		
		for (EntailmentUnit nodeA : workGraph.vertexSet()){
			for (EntailmentUnit nodeB : workGraph.vertexSet()){
				if (nodeA.equals(nodeB)) continue; // no self-loops
				Double confidence = detectConfidence(workGraph, nodeA, nodeB, confidenceThreshold);
				if (confidence == TEDecision.CONFIDENCE_NOT_AVAILABLE)
					throw new GraphOptimizerException("Unavaliable score was detected.");
				RelationNode sourceNode = new RelationNode(nodeIndex.get(nodeA.getText()),nodeA.getText());
				RelationNode targetNode = new RelationNode(nodeIndex.get(nodeB.getText()),nodeB.getText());
				try {
					graph.addEdge(new RuleEdge(sourceNode, targetNode,confidence));
				} catch (Exception e) {
					throw new GraphOptimizerException("Problem when adding edge "+nodeA.getText()+" -> "+nodeB.getText()+" with confidence = "+confidence+".\n"+e);
				}				
			}
		}
		
/*		for (EntailmentRelation edge : workGraph.edgeSet()) {
			Double confidence = detectConfidence(workGraph, edge.getSource(), edge.getTarget(), confidenceThreshold);
			if (confidence == TEDecision.CONFIDENCE_NOT_AVAILABLE)
				throw new GraphOptimizerException("Unavaliable score was detected.");
			RelationNode sourceNode = new RelationNode(nodeIndex.get(edge.getSource().getText()),edge.getSource().getText());
			RelationNode targetNode = new RelationNode(nodeIndex.get(edge.getTarget().getText()),edge.getTarget().getText());
			try {
				graph.addEdge(new RuleEdge(sourceNode, targetNode,confidence));
			} catch (Exception e) {
				throw new GraphOptimizerException("Problem when adding edge "+edge.getSource().getText()+" -> "+edge.getTarget().getText()+" with confidence = "+confidence+".\n"+e);
			}
		}
*/		
		Set<AbstractOntologyGraph> componnetGraphs;
		try {
			componnetGraphs = graphLearner.learn(graph);
		} catch (Exception e) {
			throw new GraphOptimizerException("Problem with global optimization.\n"+ExceptionUtils.getFullStackTrace(e));
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
					logger.info(sourceVertex+" -> "+targetVertex+" "+edge.getConfidence());
		//		}
			}
		}*/
		
		EntailmentGraphRaw tmpRawGraph = new EntailmentGraphRaw();
		for (EntailmentUnit node : workGraph.vertexSet()){
			tmpRawGraph.addVertex(node);
		}
		
		for (AbstractOntologyGraph componnetGraph : componnetGraphs) {
			for (AbstractRuleEdge componentEdge : componnetGraph.getEdges()) {
				//if (componentEdge.score() >= confidenceThreshold) { //TODO: do we need the threshold here? Should it be confidenceThreshold or just 0 (to retain only entailment edges)? 				
				if (componentEdge.score() >= 0) { //keep threshold of 0 (to retain only entailment edges).  I spotted negative scores for output edges, the question is whether to retain them (they are supposed to be part of consistent transitive graph), or remove, since they seem non-entailing 				
					EntailmentUnit source = tmpRawGraph.getVertexWithText(componentEdge.from().description());
					EntailmentUnit target = tmpRawGraph.getVertexWithText(componentEdge.to().description());
					Double score = componentEdge.score();
					if (score > 1) score = 1.0;
					EntailmentRelation edge = new EntailmentRelation(source, target, new TEDecisionWithConfidence(score, DecisionLabel.Entailment));
					tmpRawGraph.addEdge(source, target, edge);
				}
			}
		}
	
		logger.debug(tmpRawGraph.toString());
		
		// TMP PATCH TO RETURN REMOVED FG EDGES
		for (EntailmentRelation edge : workGraph.edgeSet()){
			if (!edge.getLabel().is(DecisionLabel.Entailment)) continue; // only return entailing FG edges
			if (edge.getEdgeType().is(EdgeType.FRAGMENT_GRAPH)){
				EntailmentUnit src = tmpRawGraph.getVertexWithText(edge.getSource().getText());
				EntailmentUnit tgt = tmpRawGraph.getVertexWithText(edge.getTarget().getText());
				tmpRawGraph.removeAllEdges(src, tgt);
				EntailmentRelation e = new EntailmentRelation(src, tgt, new TEDecisionWithConfidence(1.0, DecisionLabel.Entailment));
				tmpRawGraph.addEdge(src, tgt, e);
			}
		}				
		tmpRawGraph.applyTransitiveClosure(); //legacy argument: changeTypeOfExistingEdges was false
		
		// now turn the output raw graph into collapsed graph
		ret = new SimpleGraphOptimizer().optimizeGraph(tmpRawGraph, -10000.0); //collapse paraphrasing nodes
		
		logger.debug("Collapsed:\n"+ret.toString());		
		return ret;
	}
	
	
	
	private Double detectConfidence(EntailmentGraphRaw workGraph, EntailmentUnit source, EntailmentUnit target, Double confidenceThreshold){
		Double confidence = null; // this will encode missing edges or edges with confidence < threshold
		
		if (workGraph.containsEdge(source, target)) {		
			// look through all the edges src->tgt and select the most confident as the representative one 
			for (EntailmentRelation edge : workGraph.getAllEdges(source, target)){
				if(edge.getTEdecision().getDecision().is(DecisionLabel.Entailment)) {
					 if (edge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) {
						 logger.debug("Adding edge "+source.getTextWithoutDoubleSpaces()+" -> "+target.getTextWithoutDoubleSpaces()+" with score: 10000.0");
						 return 10000.0; // if there is a positive FG edge, no need to check for other edges
					 }
					 // if this is not a FG edge
					 if (confidence==null) {
						 if (edge.getConfidence() >= confidenceThreshold) confidence = edge.getConfidence();
					 }
					 else if (edge.getConfidence() >= Math.abs(confidence)) {
						 confidence = edge.getConfidence(); // only if the original score is >= than the absolute value of the current confidence , consider the edge entailing with the corresponding confidence.
					 }
				}
				
				else { // if this is a non-entailment edge
					if (edge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) {
						logger.debug("Adding edge "+source.getTextWithoutDoubleSpaces()+" -> "+target.getTextWithoutDoubleSpaces()+" with score: -10000.0");
						return -10000.0; // if there is a negative FG edge, no need to check for other edges
					}
					// if this is not a FG edge			
					if (confidence==null) {
						if (edge.getConfidence() >= confidenceThreshold) confidence = -1.0 * edge.getConfidence();
					}
					else if (edge.getConfidence() >= Math.abs(confidence)) {
						 confidence = -1.0 * edge.getConfidence(); // only if the original score is >= than the absolute value of the current confidence, consider the edge non-entailing with the corresponding negative confidence value.
					}
				}				
			}
		}
		// else return default non-entailing confidence (equal to -1.0*threshold)
/*		else{
			// We should return non-entailment score for missing edges from inside FGs
			// Check: if src and tgt share a compete statement, then they belong to the same FG	 - this is for cases when no-entailment FG edges are not explicit		
			Set<String> minusSharedSet= new HashSet<String>(source.getCompleteStatementTexts());
			minusSharedSet.removeAll(target.getCompleteStatementTexts());
			if (source.getCompleteStatementTexts().size() != minusSharedSet.size()){ // i.e. if there were shared (removed) complete statements
				confidence = -10000.0;
			}
		}
*/		
		
		if (confidence==null) confidence = -1.0 * AbstractGraphOptimizer.getAverageConfidenceOfEntailment(workGraph);
		
		// Note: -1 confidence is understood as "unavailable score"
		if (confidence == TEDecision.CONFIDENCE_NOT_AVAILABLE) {
			confidence = -0.99;
		}		
		logger.debug("Adding edge "+source.getTextWithoutDoubleSpaces()+" -> "+target.getTextWithoutDoubleSpaces()+" with score: "+confidence);		
		return confidence;
	}

	protected UntypedPredicateGraphLearner graphLearner;
	protected EdgeLearner edgeLearner;
} 

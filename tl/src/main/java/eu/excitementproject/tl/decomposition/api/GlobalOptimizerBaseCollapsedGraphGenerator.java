package eu.excitementproject.tl.decomposition.api;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
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
import eu.excitementproject.eop.globalgraphoptimizer.graph.UndirectedOntologyGraph;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * @author Meni Adler
 * @since Oct 29, 2013
 *
 */
public class GlobalOptimizerBaseCollapsedGraphGenerator implements CollapsedGraphGenerator {

	protected final static double DEFAULT_EDGE_COST = 0.5;
	protected final static double DEFAULT_UNKNOWN_SCORE = 0;
	
	public GlobalOptimizerBaseCollapsedGraphGenerator() {
		this(DEFAULT_EDGE_COST, DEFAULT_UNKNOWN_SCORE, new HashSet<Pair<String,String>>(), new HashSet<Pair<String,String>>());
	}
	
	public GlobalOptimizerBaseCollapsedGraphGenerator(double edgeCost, double unknownScore) {
		this(edgeCost, unknownScore, new HashSet<Pair<String,String>>(), new HashSet<Pair<String,String>>());
	}
	
	public GlobalOptimizerBaseCollapsedGraphGenerator(Set<Pair<String,String>> entailings, Set<Pair<String,String>> nonentailings)  {
		this(DEFAULT_EDGE_COST, DEFAULT_UNKNOWN_SCORE, entailings, entailings);
	}
	
	public GlobalOptimizerBaseCollapsedGraphGenerator(double edgeCost, double unknownScore, Set<Pair<String,String>> entailings, Set<Pair<String,String>> nonentailings)  {
		boolean convertProb2Score = false;
		this.edgeLearner = new EfficientlyCorrectHtlLearner(edgeCost);
		this.graphLearner = new UntypedPredicateGraphLearner(this.edgeLearner, convertProb2Score, entailings,nonentailings, unknownScore);		
	}



	@Override
	public EntailmentGraphCollapsed generateCollapsedGraph(EntailmentGraphRaw workGraph) throws Exception {
		return generateCollapsedGraph(workGraph,0.0);
	}

	@Override
	public EntailmentGraphCollapsed generateCollapsedGraph(EntailmentGraphRaw workGraph, Double confidenceThreshold) throws Exception{
		DirectedOntologyGraph graph = new DirectedOneMappingOntologyGraph("work graph");
		int i = 1;
		for (EntailmentRelation entailmentRelation : workGraph.edgeSet()) {
			EntailmentUnit source = entailmentRelation.getSource();
			EntailmentUnit target = entailmentRelation.getTarget();
			double confidence = entailmentRelation.getConfidence();
			if (confidence == TEDecision.CONFIDENCE_NOT_AVAILABLE)
				throw new Exception("Unavaliable score was detected");
			RelationNode sourceNode = new RelationNode(i++,source.getText());
			RelationNode targetNode = new RelationNode(i++,target.getText());
			graph.addEdge(new RuleEdge(sourceNode, targetNode,confidence));
		}
		Set<AbstractOntologyGraph> componnetGraphs = graphLearner.learn(graph);
		EntailmentGraphCollapsed ret = new EntailmentGraphCollapsed();
		for (AbstractOntologyGraph componnetGraph : componnetGraphs) {
			for (AbstractRuleEdge componentEdge : componnetGraph.getEdges()) {
				if (componentEdge.score() >= confidenceThreshold) {
					EquivalenceClass sourceVertex = new EquivalenceClass(new EntailmentUnit(componentEdge.from().description(),null,null,null,-1));
					EquivalenceClass targetVertex = new EquivalenceClass(new EntailmentUnit(componentEdge.to().description(),null,null,null,-1));;
					EntailmentRelationCollapsed edge = new EntailmentRelationCollapsed(sourceVertex,targetVertex,componentEdge.score());
					ret.addEdgeWithNodes(sourceVertex,targetVertex,edge);
				}
			}
		}
		
		return ret;
	}

	protected UntypedPredicateGraphLearner graphLearner;
	protected EdgeLearner edgeLearner;
}

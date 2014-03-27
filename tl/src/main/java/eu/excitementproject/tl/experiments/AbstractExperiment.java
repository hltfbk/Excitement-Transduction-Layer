package eu.excitementproject.tl.experiments;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.demo.UseCaseOneDemo;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.EvaluatorGraphMerger;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.evaluation.graphoptimizer.EvaluatorGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * Class with methods for running experiments & evaluations
 * @author Lili Kotlerman
 *
 */
public abstract class AbstractExperiment extends UseCaseOneDemo {

	public GoldStandardEdgesLoader gsloader = null;
	public EntailmentGraphRaw m_rawGraph = null;
	public GraphOptimizer m_optimizer = null;
	
	public AbstractExperiment(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
		// Logger.getRootLogger().setLevel(Level.ERROR); 
	}
	
	public void buildRawGraph() {
		try {
			m_rawGraph = this.useOne.buildRawGraph(this.docs);
		} catch (LAPException | GraphMergerException | FragmentGraphGeneratorException | FragmentAnnotatorException | 
				ModifierAnnotatorException | IOException e) {
			e.printStackTrace();	
		}
	}

	public EntailmentGraphCollapsed collapseGraph() {
		try {
			return m_optimizer.optimizeGraph(m_rawGraph);
		} catch (GraphOptimizerException e) {
			e.printStackTrace();	
			return null;
		}
	}
	
	public EntailmentGraphCollapsed collapseGraph(Double threshold) {
		try {
			return m_optimizer.optimizeGraph(m_rawGraph, threshold);
		} catch (GraphOptimizerException e) {
			e.printStackTrace();	
			return null;
		}
	}

	/** 
	 * @param graph
	 * @param gsAnnotationsDir
	 * @return 
	 */
	private void loadGS(EntailmentGraphRaw graph, String gsAnnotationsDir){
		Set<String> nodesOfInterest = new HashSet<String>();
		for (EntailmentUnit node : graph.vertexSet()){
			nodesOfInterest.add(node.getTextWithoutDoubleSpaces()); //Use getTextWithoutDoubleSpaces() method to get node's text, since gold standard fragment graphs hold node texts without double spaces
		}
		gsloader = new GoldStandardEdgesLoader(nodesOfInterest, true); //load closure edges
		try {
			gsloader.loadAllAnnotations(gsAnnotationsDir, false);
		} catch (GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public EvaluationMeasures evaluateRawGraph(EntailmentGraphRaw graph, String gsAnnotationsDir, boolean includeFragmentGraphEdges){			
		loadGS(graph, gsAnnotationsDir);
		
		// Preliminary cleaning to run the evaluations over the same set of nodes.
		// Part of it is done by gold standard edges loader, when loading only nodes of interest. 
		// Yet, nodes of interest might contain unrelated nodes (due to using input, which has some blind-set fragments as well, or due to using a limited number of input files)  
		Set<EntailmentUnit> nodesToRemove = new HashSet<EntailmentUnit>();
		Set<String> gsNodeTexts = gsloader.getNodes();		
		for (EntailmentUnit node : graph.vertexSet()){
			if (!gsNodeTexts.contains(node.getTextWithoutDoubleSpaces())) nodesToRemove.add(node);
		}
		graph.removeAllVertices(nodesToRemove);				
		return EvaluatorGraphMerger.evaluate(gsloader.getEdges(), graph.edgeSet(), includeFragmentGraphEdges);
	}
	
	/** Excluding fragment graph edges is not available - for collapsed graph we don't keep track of the edges' origin, also logically it's not relevant for collapsed graph evaluation
	 * @param graph
	 * @param gsAnnotationsDir
	 * @return
	 */
	public EvaluationMeasures evaluateCollapsedGraph(EntailmentGraphCollapsed graph, String gsAnnotationsDir){			
		// de-collapse the graph into the corresponding raw graph
		EntailmentGraphRaw rawGraph = new EntailmentGraphRaw();
		for (EntailmentRelation e : EvaluatorGraphOptimizer.getAllEntailmentRelations(graph)){
			if (!rawGraph.containsVertex(e.getSource())) rawGraph.addVertex(e.getSource());
			if (!rawGraph.containsVertex(e.getTarget())) rawGraph.addVertex(e.getTarget());
			rawGraph.addEdge(e.getSource(), e.getTarget(), e);
		}		
		return evaluateRawGraph(rawGraph, gsAnnotationsDir, true);
	}
	
/*	public EvaluationMeasures evaluateCollapsedGraph(EntailmentGraphRaw rawGraph, EntailmentGraphCollapsed collapsedGraph, String gsAnnotationsDir, boolean includeFragmentGraphEdges){		
		loadGS(rawGraph, gsAnnotationsDir);
		//TODO Here preliminary cleaning will also be needed, unless we fix the inconsistency 		
		return EvaluatorGraphOptimizer.evaluateDecollapsedGraph(gsloader.getEdges(), collapsedGraph, includeFragmentGraphEdges);
	}*/

	/** Evaluate raw graph, where only edges with confidence > threshold are left
	 * @param confidenceThreshold
	 * @param graph
	 * @param gsAnnotationsDir
	 * @param includeFragmentGraphEdges - if true, the evaluation will consider all the edges in the raw graph; if false - fragment graph edges will be excluded from the evaluation 
	 * @return
	 */
	public EvaluationMeasures evaluateRawGraph(double confidenceThreshold, EntailmentGraphRaw graph, String gsAnnotationsDir, boolean includeFragmentGraphEdges){			
		// remove edges with confidence < threshold
		Set<EntailmentRelation> workEdgesToRemove = new HashSet<EntailmentRelation>();
		for (EntailmentRelation workEdge : graph.edgeSet()){
			if (!workEdge.getLabel().equals(DecisionLabel.Entailment)){
				workEdgesToRemove.add(workEdge);
			}
			else{ // if this is an "entailment" edge
				if(workEdge.getConfidence()<confidenceThreshold) {
					workEdgesToRemove.add(workEdge);
				}
			}
		}
		graph.removeAllEdges(workEdgesToRemove);
		// evaluate the resulting graph
		return evaluateRawGraph(graph, gsAnnotationsDir, includeFragmentGraphEdges);
	}
}

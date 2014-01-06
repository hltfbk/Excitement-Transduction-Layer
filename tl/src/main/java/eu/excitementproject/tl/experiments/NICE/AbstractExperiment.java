package eu.excitementproject.tl.experiments.NICE;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
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

public abstract class AbstractExperiment extends UseCaseOneDemo {

	GoldStandardEdgesLoader gsloader = null;
	
	public AbstractExperiment(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
	}
	
	public EntailmentGraphRaw buildRawGraph() {
		try {
			return this.useOne.buildRawGraph(this.docs);
		} catch (LAPException | GraphMergerException | FragmentGraphGeneratorException | FragmentAnnotatorException | 
				ModifierAnnotatorException | IOException e) {
			e.printStackTrace();	
			return null;
		}
	}
	
	public EntailmentGraphRaw buildRawGraph(Double thresould) {
		try {
			return this.useOne.buildRawGraph(this.docs);
		} catch (LAPException | GraphMergerException | FragmentGraphGeneratorException | FragmentAnnotatorException | 
				ModifierAnnotatorException | IOException e) {
			e.printStackTrace();	
			return null;
		}
	}

	public EntailmentGraphCollapsed buildCollapsedGraph() {
		try {
			return this.useOne.buildCollapsedGraph(this.docs);
		} catch (LAPException | EntailmentGraphRawException | FragmentGraphGeneratorException | FragmentAnnotatorException | 
				ModifierAnnotatorException | IOException | TransformerException | GraphMergerException | GraphOptimizerException e) {
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
		gsloader = new GoldStandardEdgesLoader(nodesOfInterest);
		try {
			gsloader.addAllAnnotations(gsAnnotationsDir);
		} catch (GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public EvaluationMeasures evaluateRawGraph(EntailmentGraphRaw graph, String gsAnnotationsDir, boolean includeFragmentGraphEdges){			
		loadGS(graph, gsAnnotationsDir);
		
		// Preliminary cleaning to run the evaluations over the same set of nodes.
		// Part of it is done by gold standard edges loader, when loading only nodes of interest. 
		// Yet, nodes of interest might contain unrelated nodes (due to using input, which has some blind-set fragments as well)  
		Set<EntailmentUnit> nodesToRemove = new HashSet<EntailmentUnit>();
		Set<String> gsNodeTexts = gsloader.getNodes();		
		for (EntailmentUnit node : graph.vertexSet()){
			if (!gsNodeTexts.contains(node.getTextWithoutDoubleSpaces())) nodesToRemove.add(node);
		}
		graph.removeAllVertices(nodesToRemove);				
		return EvaluatorGraphMerger.evaluate(gsloader.getEdges(), graph.edgeSet(), includeFragmentGraphEdges);
	}
	
	public EvaluationMeasures evaluateCollapsedGraph(EntailmentGraphRaw rawGraph, EntailmentGraphCollapsed collapsedGraph, String gsAnnotationsDir, boolean includeFragmentGraphEdges){		
		loadGS(rawGraph, gsAnnotationsDir);
		//TODO Here preliminary cleaning will also be needed, unless we fix the inconsistency 		
		return EvaluatorGraphOptimizer.evaluateDecollapsedGraph(gsloader.getEdges(), collapsedGraph, includeFragmentGraphEdges);
	}
	
	/** Evaluate raw graph, where only edges with confidence > threshold are left
	 * @param confidenceThreshold
	 * @param graph
	 * @param gsAnnotationsDir
	 * @param includeFragmentGraphEdges
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

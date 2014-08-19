package eu.excitementproject.tl.experiments;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.EvaluatorGraphMerger;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.evaluation.graphoptimizer.EvaluatorGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationAndAnalysisMeasures;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

/**
 * Class with methods for running experiments & evaluations
 * @author Lili Kotlerman
 *
 */
public abstract class AbstractExperiment extends UseCaseOneForExperiments {

	public GoldStandardEdgesLoader gsloader = null;
	public EntailmentGraphRaw m_rawGraph = null;
	public EntailmentGraphRaw m_rawGraph_plusClosure = null;
	public GraphOptimizer m_optimizer = null;
	
	public List<Double> confidenceThresholds;
	public Map<String,Map<Double,EvaluationAndAnalysisMeasures>> results;
	
	public static final boolean includeFragmentGraphEdges = true;
	
	public AbstractExperiment(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
		// Logger.getRootLogger().setLevel(Level.ERROR); 
		
		confidenceThresholds= new LinkedList<Double>();
		for (double confidenceThreshold=0.5; confidenceThreshold<1.01; confidenceThreshold+=0.05){
			confidenceThresholds.add(confidenceThreshold);
		}
		
		results = new HashMap<String, Map<Double,EvaluationAndAnalysisMeasures>>();
	}
	
	public String printResults(){
		String s = this.toString()+"\n";
		for (String setting : results.keySet()){
			System.out.println();
			for (Double threshold : confidenceThresholds){
				if (results.get(setting).containsKey(threshold)){
					EvaluationAndAnalysisMeasures res = results.get(setting).get(threshold);
					s += setting+"\t"+threshold.toString()+"\t"+res.getRecall().toString()+"\t"+res.getPrecision().toString()+"\t"+res.getF1().toString()+"\t"+res.getViolations().toString()+"\t"+res.getExtraFGedges().toString()+"\t"+res.getMissingFGedges().toString()+"\t"+res.getEdaCalls().toString()+"\n";
					System.out.println(s);	
				}
			}
		}
		return s;
	}
	
	@Override
	public String toString(){
		return "LAP: " +this.lap.getInstanceName()+";\tEDA: "+this.eda.getClass().getName()+
				";\tMerger: "+this.useOne.getGraphMerger().getClass().getName()+
				";\tOptimizer: "+this.m_optimizer.getClass().getName();
	}
	
	public void addResult(String setting, double threshold, EvaluationAndAnalysisMeasures res){
		Map<Double,EvaluationAndAnalysisMeasures> resultsForSetting = new HashMap<Double,EvaluationAndAnalysisMeasures>();
		if(results.containsKey(setting)) resultsForSetting = results.get(setting);
		resultsForSetting.put(threshold, res);
		results.put(setting, resultsForSetting);
	}
	
	/**
	 * @param confidenceThresholds the confidenceThresholds to set
	 */
	public void setConfidenceThresholds(List<Double> confidenceThresholds) {
		this.confidenceThresholds = confidenceThresholds;
	}



	public void buildRawGraph() {
		try {
			m_rawGraph = this.useOne.buildRawGraph(this.docs);
			m_rawGraph_plusClosure = new EntailmentGraphRaw(m_rawGraph.vertexSet(), m_rawGraph.edgeSet());
			m_rawGraph_plusClosure.applyTransitiveClosure(false);
		} catch (LAPException | GraphMergerException | FragmentGraphGeneratorException | FragmentAnnotatorException | 
				ModifierAnnotatorException | IOException e) {
			e.printStackTrace();	
		}
	}

	public void buildRawGraph(double confidenceThreshold) {
		try {
			m_rawGraph = this.useOne.buildRawGraph(this.docs, confidenceThreshold);
			m_rawGraph_plusClosure = new EntailmentGraphRaw(m_rawGraph.vertexSet(), m_rawGraph.edgeSet());
			m_rawGraph_plusClosure.applyTransitiveClosure(false);
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
	
	public EntailmentGraphCollapsed collapseGraph(Double threshold, boolean withClosure) {
		try {
			if (withClosure) return m_optimizer.optimizeGraph(m_rawGraph_plusClosure, threshold);
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
	private void loadGSAll(EntailmentGraphRaw graph, String gsAnnotationsDir){
		Set<String> nodesOfInterest = new HashSet<String>();
		for (EntailmentUnit node : graph.vertexSet()){
			nodesOfInterest.add(node.getTextWithoutDoubleSpaces()); //Use getTextWithoutDoubleSpaces() method to get node's text, since gold standard fragment graphs hold node texts without double spaces
		}
		gsloader = new GoldStandardEdgesLoader(nodesOfInterest, true); //true=load closure edges
		try {
			gsloader.loadAllAnnotations(gsAnnotationsDir, false);
		} catch (GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	/** 
	 * @param graph
	 * @param clusterAnnotationsDir
	 * @return 
	 */
	private void loadGSCluster(EntailmentGraphRaw graph, String clusterAnnotationsDir){
		Set<String> nodesOfInterest = new HashSet<String>();
		for (EntailmentUnit node : graph.vertexSet()){
			nodesOfInterest.add(node.getTextWithoutDoubleSpaces()); //Use getTextWithoutDoubleSpaces() method to get node's text, since gold standard fragment graphs hold node texts without double spaces
		}
		gsloader = new GoldStandardEdgesLoader(nodesOfInterest, true); //true=load closure edges
		try {
			gsloader.loadClusterAnnotations(clusterAnnotationsDir, false);
		} catch (GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}	
	
	public EvaluationMeasures evaluateRawGraph(EntailmentGraphRaw graph, String gsAnnotationsDir, boolean includeFragmentGraphEdges, boolean isSingleClusterGS){			
		if (isSingleClusterGS) loadGSCluster(graph, gsAnnotationsDir); 
		else loadGSAll(graph, gsAnnotationsDir);
		
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
	public EvaluationMeasures evaluateCollapsedGraph(EntailmentGraphCollapsed graph, String gsAnnotationsDir, boolean isSingleClusterGS){			
		// de-collapse the graph into the corresponding raw graph
		EntailmentGraphRaw rawGraph = new EntailmentGraphRaw();
		for (EntailmentRelation e : EvaluatorGraphOptimizer.getAllEntailmentRelations(graph)){
			if (!rawGraph.containsVertex(e.getSource())) rawGraph.addVertex(e.getSource());
			if (!rawGraph.containsVertex(e.getTarget())) rawGraph.addVertex(e.getTarget());
			rawGraph.addEdge(e.getSource(), e.getTarget(), e);
		}		
		return evaluateRawGraph(rawGraph, gsAnnotationsDir, true, isSingleClusterGS);
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
	public EvaluationMeasures evaluateRawGraph(double confidenceThreshold, EntailmentGraphRaw graph, String gsAnnotationsDir, boolean includeFragmentGraphEdges, boolean isSingleClusterGS){			
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
		return evaluateRawGraph(graph, gsAnnotationsDir, includeFragmentGraphEdges, isSingleClusterGS);
	}
	
	public int getEdaCallsNumber(){
		return this.useOne.getEdaCallsNumber();
	}
	
	/**
	 * @param evaluatedCollapsedGraph
	 * @param gsDir - directory with "FragmentGraphs" dir in it, which holds all FGs xml files 
	 * @return
	 * @throws GraphEvaluatorException
	 */
	public EvaluationAndAnalysisMeasures checkGraphConsistency(EntailmentGraphCollapsed evaluatedCollapsedGraph, File gsDir) throws GraphEvaluatorException{
		EvaluationAndAnalysisMeasures eval = new EvaluationAndAnalysisMeasures();
		boolean isConsistent = true;
		// consistency check for edges - all transitive closure edges should be explicitly present in the graph
		// if not - smth is wrong
		int violations = 0;
		int addedEdges = evaluatedCollapsedGraph.edgeSet().size();
		System.out.print("Checking edges for consistency:");
		evaluatedCollapsedGraph.applyTransitiveClosure(false);
		if (evaluatedCollapsedGraph.edgeSet().size() != addedEdges){
			for (EntailmentRelationCollapsed edge : evaluatedCollapsedGraph.edgeSet()){
				if (edge.getEdgeType().equals(EdgeType.TRANSITIVE_CLOSURE)) {
					System.err.println("\nInconsistent edge: "+ edge);
					isConsistent = false;
					violations++;
				}
			}
		}
		if(isConsistent) System.out.println("  No transitivity violations in collapsed graph's edges"); 
			
		// Now check if added edges into fragment graphs
		// First, load raw graph with FGs in it
		GoldStandardEdgesLoader gsFGloader = new GoldStandardEdgesLoader(false); //load the original data only		
		if (gsDir.isDirectory()){
			String warnings = gsFGloader.loadFGsRawGraph(gsDir.getAbsolutePath()); //load only FGs\
			if (!warnings.isEmpty()) System.out.println("Problems with cluster "+gsDir.getName()+":\n"+warnings);
		}
		EntailmentGraphRaw rfg = gsFGloader.getRawGraph();

		// now de-collapse the tested graph and check for inconsistencies
		int fgExtraEdges=0;
		int fgMissingEdges=0;
				
		Set<EntailmentRelation> decollapsedEdges = EvaluatorGraphOptimizer.getAllEntailmentRelations(evaluatedCollapsedGraph);
		for (EntailmentRelation fge : rfg.edgeSet()){
			boolean isEntailInFG = false;
			if (fge.getLabel().equals(DecisionLabel.Entailment)) isEntailInFG = true;
			boolean isEntailInRaw = false;
			for(EntailmentRelation e : decollapsedEdges){
				if (fge.isSameSourceAndTarget(e)) {
					isEntailInRaw = true;
					break;
				}
			}
			if ((isEntailInFG)&&(!isEntailInRaw)) fgMissingEdges++;
			if ((isEntailInRaw)&&(!isEntailInFG)) fgExtraEdges++;
		}

		
		
		
/*		for (EntailmentRelation e : decollapsedEdges){
						
			EntailmentUnit rs = rfg.getVertexWithText(e.getSource().getText());
			EntailmentUnit rt = rfg.getVertexWithText(e.getTarget().getText());
			for (EntailmentRelation fge : rfg.getAllEdges(rs, rt)){
				if (fge.getLabel().equals(DecisionLabel.NonEntailment)){
					isConsistent = false;
					System.err.println("Edge added into Fragment Graph: "+ e);	
					fgExtraEdges++;
					EntailmentRelationCollapsed edge = evaluatedCollapsedGraph.getEdge(evaluatedCollapsedGraph.getVertex(e.getSource().getText()), evaluatedCollapsedGraph.getVertex(e.getTarget().getText()));
					if (edge!=null) System.err.println("from collapsed edge: "+ edge);
					else System.err.println("as part of collapsed node: "+ evaluatedCollapsedGraph.getVertex(e.getSource().getText()));
				}
			}
		}
*/		eval.setViolations(violations);
		eval.setExtraFGedges(fgExtraEdges);
		eval.setMissingFGedges(fgMissingEdges);
		return eval;
	}

	/**
	 * @param evaluatedRawGraph
	 * @param gsDir - directory with "FragmentGraphs" dir in it, which holds all FGs xml files 
	 * @return
	 * @throws GraphEvaluatorException
	 */
	public EvaluationAndAnalysisMeasures checkGraphConsistency(EntailmentGraphRaw evaluatedRawGraph, File gsDir) throws GraphEvaluatorException{
		EvaluationAndAnalysisMeasures eval = new EvaluationAndAnalysisMeasures();
		boolean isConsistent = true;
		// consistency check for edges - all transitive closure edges should be explicitly present in the graph
		// if not - smth is wrong
		int violations = 0;
		int addedEdges = evaluatedRawGraph.edgeSet().size();
		System.out.print("Checking edges for consistency:");
		evaluatedRawGraph.applyTransitiveClosure(false);
		if (evaluatedRawGraph.edgeSet().size() != addedEdges){
			for (EntailmentRelation edge : evaluatedRawGraph.edgeSet()){
				if (edge.getEdgeType().equals(EdgeType.TRANSITIVE_CLOSURE)) {
					System.err.println("\nInconsistent edge: "+ edge);
					isConsistent = false;
					violations++;
				}
			}
		}
		if(isConsistent) System.out.println("  No transitivity violations in raw graph's edges"); 
			
		// Now check if added edges into fragment graphs
		// First, load raw graph with FGs in it
		GoldStandardEdgesLoader gsFGloader = new GoldStandardEdgesLoader(false); //load the original data only		
		if (gsDir.isDirectory()){
			String warnings = gsFGloader.loadFGsRawGraph(gsDir.getAbsolutePath()); //load only FGs\
			if (!warnings.isEmpty()) System.out.println("Problems with cluster "+gsDir.getName()+":\n"+warnings);
		}
		EntailmentGraphRaw rfg = gsFGloader.getRawGraph();

		// now check for inconsistencies
		int fgExtraEdges=0;
		int fgMissingEdges=0;
				
		for (EntailmentRelation fge : rfg.edgeSet()){
			boolean isEntailInFG = false;
			if (fge.getLabel().equals(DecisionLabel.Entailment)) isEntailInFG = true;
			boolean isEntailInRaw = false;
			for(EntailmentRelation e : evaluatedRawGraph.edgeSet()){
				if (fge.isSameSourceAndTarget(e)) {
					isEntailInRaw = true;
					break;
				}
			}
			if ((isEntailInFG)&&(!isEntailInRaw)) fgMissingEdges++;
			if ((isEntailInRaw)&&(!isEntailInFG)) fgExtraEdges++;
		}

		
		
		
/*		for (EntailmentRelation e : decollapsedEdges){
						
			EntailmentUnit rs = rfg.getVertexWithText(e.getSource().getText());
			EntailmentUnit rt = rfg.getVertexWithText(e.getTarget().getText());
			for (EntailmentRelation fge : rfg.getAllEdges(rs, rt)){
				if (fge.getLabel().equals(DecisionLabel.NonEntailment)){
					isConsistent = false;
					System.err.println("Edge added into Fragment Graph: "+ e);	
					fgExtraEdges++;
					EntailmentRelationCollapsed edge = evaluatedCollapsedGraph.getEdge(evaluatedCollapsedGraph.getVertex(e.getSource().getText()), evaluatedCollapsedGraph.getVertex(e.getTarget().getText()));
					if (edge!=null) System.err.println("from collapsed edge: "+ edge);
					else System.err.println("as part of collapsed node: "+ evaluatedCollapsedGraph.getVertex(e.getSource().getText()));
				}
			}
		}
*/		eval.setViolations(violations);
		eval.setExtraFGedges(fgExtraEdges);
		eval.setMissingFGedges(fgMissingEdges);
		return eval;
	}

}

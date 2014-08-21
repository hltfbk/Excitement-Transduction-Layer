package eu.excitementproject.tl.experiments;

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
	public EntailmentGraphRaw m_rfg = null;
	
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
		String s = "";
		for (String setting : results.keySet()){
			System.out.println();
			for (Double threshold : confidenceThresholds){
				if (results.get(setting).containsKey(threshold)){
					EvaluationAndAnalysisMeasures res = results.get(setting).get(threshold);
					s += setting+"\t"+threshold.toString()+"\t"+res.getRecall().toString()+"\t"+res.getPrecision().toString()+"\t"+res.getF1().toString()+"\t"+res.getOverallEdges().toString()+"\t"+res.getViolations().toString()+"\t"+res.getExtraFGedges().toString()+"\t"+res.getMissingFGedges().toString()+"\t"+res.getEdaCalls().toString()+"\n";
					System.out.println(s);	
				}
			}
		}
		return s;
	}
	
	@Override
	public String toString(){
		return "LAP: " +this.lap.getClass().getName()+";\tEDA: "+this.eda.getClass().getName()+
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
			for (EntailmentRelation e : m_rawGraph_plusClosure.edgeSet()){
				e.setEdgeType(EdgeType.UNKNOWN);
			}
			m_rfg = getFragmetGraphsInRawGraph();
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
			m_rfg = getFragmetGraphsInRawGraph();			
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
	
	public EvaluationAndAnalysisMeasures evaluateRawGraph(EntailmentGraphRaw graph, String gsAnnotationsDir, boolean includeFragmentGraphEdges, boolean isSingleClusterGS){			
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
		EvaluationAndAnalysisMeasures eval = new EvaluationAndAnalysisMeasures(EvaluatorGraphMerger.evaluate(gsloader.getEdges(), graph.edgeSet(), includeFragmentGraphEdges));
		eval.setOverallEdges(graph.edgeSet().size());
		return eval;
	}
	
	/** Excluding fragment graph edges is not available - for collapsed graph we don't keep track of the edges' origin, also logically it's not relevant for collapsed graph evaluation
	 * @param graph
	 * @param gsAnnotationsDir
	 * @return
	 */
	public EvaluationAndAnalysisMeasures evaluateCollapsedGraph(EntailmentGraphCollapsed graph, String gsAnnotationsDir, boolean isSingleClusterGS){			
		// de-collapse the graph into the corresponding raw graph
		EntailmentGraphRaw rawGraph = new EntailmentGraphRaw();
		for (EntailmentRelation e : EvaluatorGraphOptimizer.getAllEntailmentRelations(graph)){
			if (!rawGraph.containsVertex(e.getSource())) rawGraph.addVertex(e.getSource());
			if (!rawGraph.containsVertex(e.getTarget())) rawGraph.addVertex(e.getTarget());
			rawGraph.addEdge(e.getSource(), e.getTarget(), e);
		}		
		return evaluateRawGraph(rawGraph, gsAnnotationsDir, true, isSingleClusterGS);
	}
	
/*	public EvaluationAndAnalysisMeasures evaluateCollapsedGraph(EntailmentGraphRaw rawGraph, EntailmentGraphCollapsed collapsedGraph, String gsAnnotationsDir, boolean includeFragmentGraphEdges){		
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
	public EvaluationAndAnalysisMeasures evaluateRawGraph(double confidenceThreshold, EntailmentGraphRaw graph, String gsAnnotationsDir, boolean includeFragmentGraphEdges, boolean isSingleClusterGS){			
		// remove edges with confidence < threshold
		Set<EntailmentRelation> workEdgesToRemove = new HashSet<EntailmentRelation>();
		for (EntailmentRelation workEdge : graph.edgeSet()){
			if (workEdge.getEdgeType().is(EdgeType.FRAGMENT_GRAPH)) continue; // don't touch FG edges
			if (!workEdge.getLabel().is(DecisionLabel.Entailment)){
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
	public EvaluationAndAnalysisMeasures checkGraphConsistency(EntailmentGraphCollapsed graph) throws GraphEvaluatorException{
	
		EvaluationAndAnalysisMeasures eval = checkFragmentGraphViolations(EvaluatorGraphOptimizer.getDecollapsedGraph(graph));

		
		// make a copy, since testing for violations requires adding closure edges to the evaluated graph
		EntailmentGraphCollapsed evaluatedCollapsedGraph = new EntailmentGraphCollapsed(graph.vertexSet(), graph.edgeSet());
		
		
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
		eval.setViolations(violations);
		
/*		// Now check if added edges into fragment graphs
		// First, load raw graph with FGs in it, if not loaded earlier
		if (rfg==null) loadRawFragmentGraphs(gsDir);
		
		// now de-collapse the tested graph and check for inconsistencies
		int fgExtraEdges=0;
		int fgMissingEdges=0;
				
		Set<EntailmentRelation> decollapsedEdges = EvaluatorGraphOptimizer.getAllEntailmentRelations(evaluatedCollapsedGraph);
		for (EntailmentRelation fge : m_rfg.edgeSet()){
			boolean isEntailInFG = false;
			if (fge.getLabel().is(DecisionLabel.Entailment)) isEntailInFG = true;
			boolean isEntailInRaw = false;
			for(EntailmentRelation e : decollapsedEdges){
				if (fge.isSameSourceAndTarget(e)) {
					if (e.getLabel().is(DecisionLabel.Entailment)) {
						isEntailInRaw = true;
						break;
					}
				}
			}
			if ((isEntailInFG)&&(!isEntailInRaw)) fgMissingEdges++;
			if ((isEntailInRaw)&&(!isEntailInFG)) fgExtraEdges++;
		}

		
		
		
		for (EntailmentRelation e : decollapsedEdges){
						
			EntailmentUnit rs = rfg.getVertexWithText(e.getSource().getText());
			EntailmentUnit rt = rfg.getVertexWithText(e.getTarget().getText());
			for (EntailmentRelation fge : rfg.getAllEdges(rs, rt)){
				if (fge.getLabel().is(DecisionLabel.NonEntailment)){
					isConsistent = false;
					System.err.println("Edge added into Fragment Graph: "+ e);	
					fgExtraEdges++;
					EntailmentRelationCollapsed edge = evaluatedCollapsedGraph.getEdge(evaluatedCollapsedGraph.getVertex(e.getSource().getText()), evaluatedCollapsedGraph.getVertex(e.getTarget().getText()));
					if (edge!=null) System.err.println("from collapsed edge: "+ edge);
					else System.err.println("as part of collapsed node: "+ evaluatedCollapsedGraph.getVertex(e.getSource().getText()));
				}
			}
		}
		
		eval.setExtraFGedges(fgExtraEdges);
		eval.setMissingFGedges(fgMissingEdges);
*/		return eval;
	}

	/**
	 * @param evaluatedRawGraph
	 * @param gsDir - directory with "FragmentGraphs" dir in it, which holds all FGs xml files 
	 * @return
	 * @throws GraphEvaluatorException
	 */
	public EvaluationAndAnalysisMeasures checkGraphConsistency(EntailmentGraphRaw graph) throws GraphEvaluatorException{
		EvaluationAndAnalysisMeasures eval = checkFragmentGraphViolations(graph);

		// make a copy, since testing for violations requires adding closure edges to the evaluated graph
		EntailmentGraphRaw evaluatedRawGraph = new EntailmentGraphRaw(graph.vertexSet(), graph.edgeSet());

		
		// Now check for transitivity violations
		// consistency check for edges - all transitive closure edges should be explicitly present in the graph
		// if not - smth is wrong
		int violations = 0;
		System.out.print("Checking edges for consistency:");
		evaluatedRawGraph.applyTransitiveClosure(false);
		for (EntailmentRelation edge : evaluatedRawGraph.edgeSet()){
			if (evaluatedRawGraph.isConflict(edge.getSource(), edge.getTarget())) violations++; // if there is a non-entailing and an entailing edge between the same src and tct
			else{
				if (edge.getEdgeType().equals(EdgeType.TRANSITIVE_CLOSURE)) { // if this edge is a transitive closure edge, which was added in place of no edge in the evaluated graph
					System.err.println("\nInconsistent edge: "+ edge);
					violations++;
				}				
			}
		}
			
		eval.setViolations(violations);
		return eval;
	}

	
/*	public void loadRawFragmentGraphs(File gsDir) throws GraphEvaluatorException{
		GoldStandardEdgesLoader gsFGloader = new GoldStandardEdgesLoader(false); //load the original data only		
		if (gsDir.isDirectory()){
			String warnings = gsFGloader.loadFGsRawGraph(gsDir.getAbsolutePath()); //load only FGs\
			if (!warnings.isEmpty()) System.out.println("Problems with cluster "+gsDir.getName()+":\n"+warnings);
		}
		rfg = gsFGloader.getRawGraph();
	}*/

	private EvaluationAndAnalysisMeasures checkFragmentGraphViolations(
			EntailmentGraphRaw graph) {
		EvaluationAndAnalysisMeasures eval = new EvaluationAndAnalysisMeasures();
		
		// Check if added edges into / removed edges from fragment graphs
		int fgExtraEdges=0;
		int fgMissingEdges=0;
		
		boolean isEntailInFG;
		boolean isEntailInRaw;
		for (EntailmentRelation fge : m_rfg.edgeSet()){
			EntailmentUnit src = graph.getVertexWithText(fge.getSource().getText());
			EntailmentUnit tgt = graph.getVertexWithText(fge.getTarget().getText());
			if ((src==null)||(tgt==null)) continue; // some of the nodes can be removed from the evaluated graph since not found in the GS (due to pre-processing problems). Edges with such nodes will be excluded also from this analysis
		
			if (fge.getLabel().is(DecisionLabel.Entailment)) isEntailInFG = true;
			else isEntailInFG=false;
			
			if (graph.isEntailment(src, tgt)) isEntailInRaw=true;
			else isEntailInRaw = false;
			
			if ((isEntailInFG)&&(!isEntailInRaw)) fgMissingEdges++;
			if ((isEntailInRaw)&&(!isEntailInFG)) fgExtraEdges++;
		}
		
		// debug code
/*		for (EntailmentRelation fge : m_rfg.edgeSet()){
			boolean isEntailInFG = false;
			if (fge.getLabel().is(DecisionLabel.Entailment)) isEntailInFG = true;
			boolean isEntailInRaw = false;
			for(EntailmentRelation e : graph.edgeSet()){
				if (fge.isSameSourceAndTarget(e)) {
					if (!e.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)){
						if (!graph.isFragmentGraphEdge(e.getSource(), e.getTarget())){
							System.out.println("Not marked FG edge: "+e);
							try {
								System.in.read();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}														
						}	
						else if (!e.getEdgeType().equals(EdgeType.TRANSITIVE_CLOSURE)){
							System.out.println("Decision obtained, though there is a FG edge: "+e);
							try {
								System.in.read();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}																					
						}
					}
					
					
					if (e.getLabel().is(DecisionLabel.Entailment)) {
						isEntailInRaw = true;
						break;
					}
				}
			}
			if ((isEntailInFG)&&(!isEntailInRaw)) fgMissingEdges++;
			if ((isEntailInRaw)&&(!isEntailInFG)) fgExtraEdges++;
		}
*/
		eval.setExtraFGedges(fgExtraEdges);
		eval.setMissingFGedges(fgMissingEdges);

		return eval;
	}

	/**
	 * @return copy of m_rawGraph graph with all edges removed, except from FG edges which are left intact
	 */
	private EntailmentGraphRaw getFragmetGraphsInRawGraph() {
		EntailmentGraphRaw rfg = new EntailmentGraphRaw(m_rawGraph.vertexSet(), m_rawGraph.edgeSet());
		Set<EntailmentRelation> alledges = new HashSet<EntailmentRelation>(rfg.edgeSet());
		for (EntailmentRelation e : alledges){
			if (!e.getEdgeType().is(EdgeType.FRAGMENT_GRAPH)) rfg.removeEdge(e);
		}
		return rfg;
	}

}


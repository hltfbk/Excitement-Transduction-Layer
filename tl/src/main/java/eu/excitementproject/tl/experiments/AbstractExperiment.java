package eu.excitementproject.tl.experiments;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
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
	public GraphOptimizer m_optimizer = null;
	public EntailmentGraphRaw m_rfg = null;
	
	public ResultsContainer results;
	
	public static final boolean includeFragmentGraphEdges = true;
	
	public AbstractExperiment(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
		// Logger.getRootLogger().setLevel(Level.ERROR); 
				
		results = new ResultsContainer();
	}
	
	public String printResults(){
		return results.printResults();
	}
	
	public String printErrorExamples(int limit){
		return results.printErrorExamples(limit);
	}	

	public String printAvgResults(){
		return results.printAvgResults();
	}
	
	@Override
	public String toString(){
		return "LAP: " +this.lap.getClass().getName()+";\tEDA: "+this.eda.getClass().getName()+
				";\tMerger: "+this.useOne.getGraphMerger().getClass().getName()+
				";\tOptimizer: "+this.m_optimizer.getClass().getName();
	}
	
	public void addResult(String setting, double threshold, EvaluationAndAnalysisMeasures res){
		results.addResult(setting, threshold, res);
	}
	
	/**
	 * @param confidenceThresholds the confidenceThresholds to set
	 */
	public void setConfidenceThresholds(List<Double> confidenceThresholds) {
		this.results.setConfidenceThresholds(confidenceThresholds);
	}

	public EntailmentGraphRaw getPlusClosureGraph(EntailmentGraphRaw graph){
		// create a copy of the evaluated graph to remove the edges and do the evaluations without affecting the original graph
		EntailmentGraphRaw graphPlusClosure = new EntailmentGraphRaw(graph.vertexSet(), graph.edgeSet());
		graphPlusClosure.applyTransitiveClosure(); //legacy argument: changeTypeOfExistingEdges was false
		for (EntailmentRelation e : graphPlusClosure.edgeSet()){
			if (!e.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) e.setEdgeType(EdgeType.UNKNOWN);
		}
		return graphPlusClosure;
	}

	
	
	
	/** Creates raw graph for the given experiment + the corresponding graph with only FG edges in it 
	 * @return the created raw graph or null if graph creation failed for any reason
	 */
	public EntailmentGraphRaw buildRawGraph() {
		try {
			m_rawGraph = this.useOne.buildRawGraph(this.docs);
			m_rfg = getFragmetGraphsInRawGraph();
			return m_rawGraph;
		} catch (LAPException | GraphMergerException | FragmentGraphGeneratorException | FragmentAnnotatorException | 
				ModifierAnnotatorException | IOException e) {
			e.printStackTrace();	
		}
		return null;
	}

	/** Creates raw graph for the given experiment & threshold + the corresponding graph with only FG edges in it 
	 * @return the created raw graph or null if graph creation failed for any reason
	 */
	public EntailmentGraphRaw buildRawGraph(double confidenceThreshold) {
		try {
			m_rawGraph = this.useOne.buildRawGraph(this.docs, confidenceThreshold);
			m_rfg = getFragmetGraphsInRawGraph();
			return m_rawGraph;
		} catch (LAPException | GraphMergerException | FragmentGraphGeneratorException | FragmentAnnotatorException | 
				ModifierAnnotatorException | IOException e) {
			e.printStackTrace();	
		}
		return null;
	}
	
	public EntailmentGraphCollapsed collapseGraph(EntailmentGraphRaw graph) {
		try {
			return m_optimizer.optimizeGraph(graph);
		} catch (GraphOptimizerException e) {
			e.printStackTrace();	
			return null;
		}
	}
	
	public EntailmentGraphCollapsed collapseGraph(EntailmentGraphRaw graph, Double threshold) {
		try {
			return m_optimizer.optimizeGraph(graph, threshold);
		} catch (GraphOptimizerException e) {
			e.printStackTrace();	
			return null;
		}
	}

	public EntailmentGraphCollapsed collapseGraph(EntailmentGraphRaw graph, GraphOptimizer optimizer) {
		try {
			return optimizer.optimizeGraph(graph);
		} catch (GraphOptimizerException e) {
			e.printStackTrace();	
			return null;
		}
	}
	
	public EntailmentGraphCollapsed collapseGraph(EntailmentGraphRaw graph, Double threshold, GraphOptimizer optimizer) {
		try {
			return optimizer.optimizeGraph(graph, threshold);
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
		
		EntailmentGraphRaw evaluatedGraph = new EntailmentGraphRaw(graph.vertexSet(), graph.edgeSet());
		
		if (isSingleClusterGS) loadGSCluster(evaluatedGraph, gsAnnotationsDir); 
		else loadGSAll(evaluatedGraph, gsAnnotationsDir);
		
		// Preliminary cleaning to run the evaluations over the same set of nodes.
		// Part of it is done by gold standard edges loader, when loading only nodes of interest. 
		// Yet, nodes of interest might contain unrelated nodes (due to using input, which has some blind-set fragments as well, or due to using a limited number of input files)  
		Set<EntailmentUnit> nodesToRemove = new HashSet<EntailmentUnit>();
		Set<String> gsNodeTexts = gsloader.getNodes();		
		for (EntailmentUnit node : evaluatedGraph.vertexSet()){
			if (!gsNodeTexts.contains(node.getTextWithoutDoubleSpaces())) nodesToRemove.add(node);
		}
		evaluatedGraph.removeAllVertices(nodesToRemove);	
		EvaluationAndAnalysisMeasures eval = new EvaluationAndAnalysisMeasures(EvaluatorGraphMerger.evaluate(gsloader.getEdges(), evaluatedGraph.edgeSet(), includeFragmentGraphEdges));
		eval.setOverallEdges(evaluatedGraph.edgeSet().size());
		return eval;
	}
	
	public EvaluationAndAnalysisMeasures evaluateCollapsedGraph(EntailmentGraphCollapsed graph, String gsAnnotationsDir, boolean includeFragmentGraphEdges, boolean isSingleClusterGS){			
		// de-collapse the graph into the corresponding raw graph
		EntailmentGraphRaw rawGraph = EvaluatorGraphOptimizer.getDecollapsedGraph(graph);
		if (!includeFragmentGraphEdges){
			if (m_rfg!=null){
				for (EntailmentRelation fgEdge : m_rfg.edgeSet()){
					rawGraph.removeAllEdges(fgEdge.getSource(), fgEdge.getTarget());
					rawGraph.addEdge(fgEdge.getSource(), fgEdge.getTarget(), fgEdge); // add edge with type FRAGMENT_GRAPH
				}
			}
		}
		return evaluateRawGraph(rawGraph, gsAnnotationsDir, includeFragmentGraphEdges, isSingleClusterGS);
	}
	
	public EntailmentGraphRaw applyThreshold(EntailmentGraphRaw graph, double confidenceThreshold){			
		EntailmentGraphRaw graphWithThreshold = new EntailmentGraphRaw(graph.vertexSet(), graph.edgeSet());
		// remove edges with confidence < threshold
		Set<EntailmentRelation> workEdgesToRemove = new HashSet<EntailmentRelation>();
		for (EntailmentRelation workEdge : graphWithThreshold.edgeSet()){
			if (workEdge.getEdgeType().is(EdgeType.FRAGMENT_GRAPH)) continue; // don't touch FG edges

/*			// want to only retain entailment edges
			if (!workEdge.getLabel().is(DecisionLabel.Entailment)){
				workEdgesToRemove.add(workEdge);
			}
			else{ // if this is an "entailment" edge
				if(workEdge.getConfidence()<confidenceThreshold) {
					workEdgesToRemove.add(workEdge);
				}
			}
*/
			// if we want to retain all types of edges
			if(workEdge.getConfidence()<confidenceThreshold) {
				workEdgesToRemove.add(workEdge);
			}
		
		}
		graphWithThreshold.removeAllEdges(workEdgesToRemove);
		return graphWithThreshold;
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
		
		// consistency check for edges - all transitive closure edges should be explicitly present in the graph
		// if not - smth is wrong
		Set<String> violations = new HashSet<String>();
		int addedEdges = evaluatedCollapsedGraph.edgeSet().size();
		System.out.print("Checking edges for consistency:");
		evaluatedCollapsedGraph.applyTransitiveClosure(false);
		if (evaluatedCollapsedGraph.edgeSet().size() != addedEdges){
			for (EntailmentRelationCollapsed edge : evaluatedCollapsedGraph.edgeSet()){
				if (edge.getEdgeType().equals(EdgeType.TRANSITIVE_CLOSURE)) {
					System.out.println(" **** COLL CLOSURE ADDED **** "+ edge);
					System.err.println("\nInconsistent edge: "+ edge);
					violations.add(edge.getSource()+"->"+edge.getTarget());
				}
			}
		}
		eval.setViolations(violations.size());
		
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
		Set<String> violations = new HashSet<String>();
		System.out.print("Checking edges for consistency:");
		evaluatedRawGraph.applyTransitiveClosure(); //legacy argument: changeTypeOfExistingEdges was false
		for (EntailmentRelation edge : evaluatedRawGraph.edgeSet()){
			if (evaluatedRawGraph.isConflict(edge.getSource(), edge.getTarget())) {
				System.out.println("****"+violations.size()+" IS CONFLICT **** "+edge);
				//violations.add(edge.getSource()+"->"+edge.getTarget()); // if there is a non-entailing and an entailing edge between the same src and tgt
			}
			else{
				if (edge.getEdgeType().equals(EdgeType.TRANSITIVE_CLOSURE)) { // if this edge is a transitive closure edge, which was added in place of no edge in the evaluated graph
					System.out.println("****"+violations.size()+" CLOSURE ADDED ****: "+edge);
					System.err.println("\nInconsistent edge: "+ edge);
					violations.add(edge.getSource()+"->"+edge.getTarget());
				}				
			}
		}
			
		eval.setViolations(violations.size());
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

	
	public List<Double> getConfidenceThresholds(){
		return this.results.confidenceThresholds;
	}

	protected static void compareClosureAndDecollapsed(Set<EntailmentRelation> closure, Set<EntailmentRelation> decollapsed){
		Set<EntailmentRelation> gs = new HashSet<EntailmentRelation>(decollapsed);
		for (EntailmentRelation e : decollapsed){
			if (!e.getLabel().is(DecisionLabel.Entailment)){
				gs.remove(e);
			}
		}
		Set<EntailmentRelation> eval = new HashSet<EntailmentRelation>(closure); // it should play the role of the eval, since it contains FG edge type
		for (EntailmentRelation e : closure){
			if (!e.getLabel().is(DecisionLabel.Entailment)){
				eval.remove(e);
			}
		}
		
		System.out.println("COMPARE CLOSURE TO DECOLLAPSED WITH FG ::"+EvaluatorGraphMerger.evaluate(gs, eval, true));
		System.out.println("COMPARE CLOSURE TO DECOLLAPSED WITHOUT FG ::"+EvaluatorGraphMerger.evaluate(gs, eval, false));
		
	}

}


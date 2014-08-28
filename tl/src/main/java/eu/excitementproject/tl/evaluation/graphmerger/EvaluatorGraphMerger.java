package eu.excitementproject.tl.evaluation.graphmerger;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

/**
 * This class contains methods for evaluating graph merger results.
 * Currently the evaluation accepts ONLY edges with "entailment" decision label
 * @author Lili Kotlerman
*/
public class EvaluatorGraphMerger {


	/** Evaluates the merge graph procedure. 
	 * The assumption is that the nodes of the gold standard merged graph and the evaluated graph are the same,
	 * i.e. that both were built from exactly the same set of fragment graphs.
	 * Thus, evaluation reflects the quality of the merging process per se.
	 * @param goldStandardEdges - "entailment" edges from the gold standard annotation
	 * @param evaluatedGraphEdges - "entailment" edges from the evaluated graph
	 * @return evaluation measures (recall, precision, f1).
	 */
	public static EvaluationMeasures evaluate(Set<EntailmentRelation> goldStandardEdges, Set<EntailmentRelation> evaluatedGraphEdges, boolean includeFragmentGraphEdges){
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.graphmerger.EvaluatorGraphMerger");
		logger.info("Evaluating graph edges. Include fragment graph = "+includeFragmentGraphEdges);
		//TODO Transitive closure: GS fragment graphs contain closure edges. Understand whether GS merge phase has closure edges as well. If yes - need to evaluate our graphs with transitive closure. If not - need to add closure edges when generating fragment graphs.
		// e.g. GS edge "Cannot retrieve tickets at Moonport station with card --> Cannot retrieve tickets" is not found in the raw graph
		
		// TODO consider adding a verification of whether GS and work graph have the same set of nodes
		EvaluationMeasures eval = new EvaluationMeasures();
		
/*		double correctlyAddedEdges = 0.0;
		Set<String> excludedEdges = new HashSet<String>();
		for (EntailmentRelation gsEdge : goldStandardEdges){	
			boolean correct = false;
			for (EntailmentRelation workEdge : evaluatedGraphEdges){
				if (!includeFragmentGraphEdges){
					if (workEdge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) {
						excludedEdges.add(workEdge.toString()); 
						continue;	// don't check FRAGMENT_GRAPH edges (exclude from the evaluations)					
					}
				}
				if (gsEdge.isSameSourceAndTarget(workEdge)) {
				//	logger.info("\n(+)\t"+gsEdge+"\t"+workEdge);
					correctlyAddedEdges++;
					correct = true;
					continue; // if found this gs edge - go look for the next one
				}				
			}
			if (!correct) logger.info("\n(-)\t"+gsEdge);
		}
		
		
		logger.info(correctlyAddedEdges+"\t"+evaluatedGraphEdges.size()+"\t"+excludedEdges.size());
		eval.setPrecision(correctlyAddedEdges/(evaluatedGraphEdges.size()-excludedEdges.size()));
		logger.info(correctlyAddedEdges+"\t"+goldStandardEdges.size()+"\t"+excludedEdges.size());
		eval.setRecall(correctlyAddedEdges/(goldStandardEdges.size()-excludedEdges.size())); // assume that all fragment graph edges are present also in the gold standard
*/		

		Set<String> gs = new HashSet<String>(); // will hold gs edges
		Set<String> ee = new HashSet<String>(); // will hold evaluated edges
		Set<String> correct = new HashSet<String>(); // will hold common edges (correctly added)
		Set<String> fg = new HashSet<String>(); // will hold fragment graph edges to exclude, if need to exclude
		
		for (EntailmentRelation edge : goldStandardEdges){
			gs.add(getSourceAndTargetString(edge));
			correct.add(getSourceAndTargetString(edge));
		}
		logger.info("GS size: "+gs.size()+" distinct, "+goldStandardEdges.size()+" overall.");
		
		for (EntailmentRelation edge : evaluatedGraphEdges){
			if (!includeFragmentGraphEdges){
				if (edge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) {
					fg.add(getSourceAndTargetString(edge));
					continue; //add this edge to fragment graph edges list (fg), don't add to evaluated graph edges (ee)
				}
			}	
			ee.add(getSourceAndTargetString(edge));
		}
		logger.info("Evaluated edges: "+evaluatedGraphEdges.size()+" overall (including fragment graph edges)");
		if (!includeFragmentGraphEdges) logger.info("Evaluated edges: "+ee.size()+ " distinct, excluding "+fg.size()+" fragment graph edges");
		else logger.info("Evaluated edges: "+ee.size()+" distinct");
		
		// remove fg edges from the gold standard
		if (!includeFragmentGraphEdges) {
			gs.removeAll(fg);
			correct.removeAll(fg);			
			logger.info("GS size without fragment graph edges: " + gs.size());
		}

		correct.retainAll(ee); // retain only common edges
		
		logger.info(correct.size()+" correctly assigned edges out of "+ee.size() +" assigned edges");
		eval.setPrecision((0.0+correct.size())/ee.size());
		logger.info(correct.size()+" correctly found edges out of "+gs.size()+" edges in GS");
		eval.setRecall((0.0+correct.size())/gs.size()); 		
		
		for (EntailmentRelation edge : evaluatedGraphEdges){
			if (correct.contains(getSourceAndTargetString(edge))) logger.debug("(+) "+ edge);
			else if(!fg.contains(getSourceAndTargetString(edge)))  {
				logger.debug("(- FP) " + edge);
			}
		}
		for (EntailmentRelation edge : goldStandardEdges){
			if (!correct.contains(getSourceAndTargetString(edge))){
				if(!fg.contains(getSourceAndTargetString(edge))) {
					logger.debug("(- FN) " + edge);
				}
			}
		}
				
		return eval;
	}
	
	public static String getSourceAndTargetString(EntailmentRelation edge){
		return edge.getSource().getTextWithoutDoubleSpaces()+" -> "+edge.getTarget().getTextWithoutDoubleSpaces();
	}
	
	public static EvaluationMeasures evaluate(Set<EntailmentRelation> goldStandardEdges, String goldStandardAnnotationsDirectory, boolean includeFragmentGraphEdges) throws GraphEvaluatorException {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(true);
		loader.loadAllAnnotations(goldStandardAnnotationsDirectory, false);
		return evaluate(goldStandardEdges, loader.getEdges(), includeFragmentGraphEdges);
	}
}

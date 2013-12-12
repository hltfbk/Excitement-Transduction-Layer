package eu.excitementproject.tl.evaluation.graphmerger;

import java.util.Set;

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
		
		//TODO check if both GS and evaluated relations are defined over exactly the same list of nodes - if not, throw exception
		
		double correctlyAddedEdges = 0.0;
		double excludedEdges = 0.0;
		for (EntailmentRelation gsEdge : goldStandardEdges){
			for (EntailmentRelation workEdge : evaluatedGraphEdges){
				if (!includeFragmentGraphEdges){
					if (workEdge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) {
						excludedEdges++;
						continue;	// don't check FRAGMENT_GRAPH edges (exclude from the evaluations)					
					}
				}
				if (gsEdge.isSameSourceAndTarget(workEdge)) correctlyAddedEdges++;
			}
		}
		
		EvaluationMeasures eval = new EvaluationMeasures();
		eval.setPrecision(correctlyAddedEdges/(evaluatedGraphEdges.size()-excludedEdges));
		eval.setRecall(correctlyAddedEdges/(goldStandardEdges.size()-excludedEdges)); // assume that all fragment graph edges are present also in the gold standard
		
		return eval;
	}
	
	public static EvaluationMeasures evaluate(Set<EntailmentRelation> goldStandardEdges, String goldStandardAnnotationsDirectory, boolean includeFragmentGraphEdges) throws GraphEvaluatorException {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(includeFragmentGraphEdges);
		loader.addAllAnnotations(goldStandardAnnotationsDirectory);
		return evaluate(goldStandardEdges, loader.getEdges(), includeFragmentGraphEdges);
	}
}

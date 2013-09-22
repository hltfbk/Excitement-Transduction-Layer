package eu.excitementproject.tl.evaluation.graphmerger;

import java.util.Set;

import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;

/**
 * This class contains methods for evaluating graph merger results
 * @author Lili Kotlerman
*/
public class EvaluatorGraphMerger {


	/** Evaluates the merge graph procedure. 
	 * The assumption is that the nodes of the gold standard merged graph and the evaluated graph are the same,
	 * i.e. that both were built from exactly the same set of fragment graphs.
	 * Thus, evaluation reflects the quality of the merging process per se.
	 * @param goldStandardEdges
	 * @param evaluatedGraphEdges
	 * @return evaluation measures (recall, precision, f1).
	 */
	public static EvaluationMeasures evaluate(Set<EntailmentRelation> goldStandardEdges, Set<EntailmentRelation> evaluatedGraphEdges){
		
		double correctlyAddedEdges = 0.0;
		for (EntailmentRelation gsEdge : goldStandardEdges){
			for (EntailmentRelation workEdge : evaluatedGraphEdges){
				if (gsEdge.isSameSourceAndTarget(workEdge)) correctlyAddedEdges++;
			}
		}
		
		EvaluationMeasures eval = new EvaluationMeasures();
		eval.setPrecision(correctlyAddedEdges/evaluatedGraphEdges.size());
		eval.setRecall(correctlyAddedEdges/goldStandardEdges.size());
		
		return eval;
	}
}

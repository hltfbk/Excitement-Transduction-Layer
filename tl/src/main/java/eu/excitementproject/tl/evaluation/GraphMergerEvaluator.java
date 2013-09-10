package eu.excitementproject.tl.evaluation;

import java.util.Set;

import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;

public class GraphMergerEvaluator {


	/** Evaluates the merge graph procedure. 
	 * The assumption is that the nodes of the gold standard merged graph and the evaluated graph are the same,
	 * i.e. that both were built from exactly the same set of fragment graphs.
	 * Thus, evaluation reflects the quality of the merging process per se.
	 * @param goldStandardEdges
	 * @param workGraphEdges
	 * @return evaluation measures (recall, precision, f1).
	 */
	public EvaluationMeasures evaluate(Set<EntailmentRelation> goldStandardEdges, Set<EntailmentRelation> workGraphEdges){
		
		double correctlyAddedEdges = 0.0;
		for (EntailmentRelation gsEdge : goldStandardEdges){
			for (EntailmentRelation workEdge : workGraphEdges){
				if (gsEdge.isSameSourceAndTarget(workEdge)) correctlyAddedEdges++;
			}
		}
		
		EvaluationMeasures eval = new EvaluationMeasures();
		eval.setPrecision(correctlyAddedEdges/workGraphEdges.size());
		eval.setRecall(correctlyAddedEdges/goldStandardEdges.size());
		
		return eval;
	}
}

package eu.excitementproject.tl.evaluation.graphmerger;

import java.util.HashSet;
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
		
		//TODO Transitive closure: GS fragment graphs contain closure edges. Understand whether GS merge phase has closure edges as well. If yes - need to evaluate our graphs with transitive closure. If not - need to add closure edges when generating fragment graphs.
		// e.g. GS edge "Cannot retrieve tickets at Moonport station with card --> Cannot retrieve tickets" is not found in the raw graph
		
		// TODO consider adding a verification of whether GS and work graph have the same set of nodes
		
		double correctlyAddedEdges = 0.0;
		Set<String> excludedEdges = new HashSet<String>();
		for (EntailmentRelation gsEdge : goldStandardEdges){	
//			boolean correct = false;
			for (EntailmentRelation workEdge : evaluatedGraphEdges){
				if (!includeFragmentGraphEdges){
					if (workEdge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) {
						excludedEdges.add(workEdge.toString()); 
						continue;	// don't check FRAGMENT_GRAPH edges (exclude from the evaluations)					
					}
				}
				if (gsEdge.isSameSourceAndTarget(workEdge)) {
//					System.out.println("+\t"+gsEdge);
					correctlyAddedEdges++;
//					correct = true;
					continue; // if found this gs edge - go look for the next one
				}				
			}
//			if (!correct) System.out.println("-\t"+gsEdge);
		}
		
		EvaluationMeasures eval = new EvaluationMeasures();
		eval.setPrecision(correctlyAddedEdges/(evaluatedGraphEdges.size()-excludedEdges.size()));
		eval.setRecall(correctlyAddedEdges/(goldStandardEdges.size()-excludedEdges.size())); // assume that all fragment graph edges are present also in the gold standard
		
		return eval;
	}
	
	public static EvaluationMeasures evaluate(Set<EntailmentRelation> goldStandardEdges, String goldStandardAnnotationsDirectory, boolean includeFragmentGraphEdges) throws GraphEvaluatorException {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader();
		loader.addAllAnnotations(goldStandardAnnotationsDirectory);
		return evaluate(goldStandardEdges, loader.getEdges(), includeFragmentGraphEdges);
	}
}

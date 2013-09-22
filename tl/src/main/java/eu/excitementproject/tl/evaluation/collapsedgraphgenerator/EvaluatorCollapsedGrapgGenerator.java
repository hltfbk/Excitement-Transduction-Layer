package eu.excitementproject.tl.evaluation.collapsedgraphgenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.tl.evaluation.utils.TLClusteringResultsEvaluator;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * @author Lili Kotlerman
 *
 */
public class EvaluatorCollapsedGrapgGenerator {
	
	// holds the best-fit gold-standard labels for evaluated clusters. Key - real cluster's label, value - best-fit gold label
	private HashMap<String, String> bestFitLabelsMap = null;  

	
	/**
	 * Considering paraphrasing statements united together under one node ({@link EquivalenceClass}) as clusters of statements, evaluate how similar automatic "clustering" is to the gold-standard one
	 * The assumption is that the gold standard graph and the evaluated graph were built from exactly the same set of fragment graphs, i.e. over the same set of statements (entailment units)
	 * @param goldStandardNodes
	 * @param evaluatedGraphNodes
	 * @param includeAllMeasures - if true all the measures are calculated, otherwise only purity is calculated
	 * @return clustering evaluation measures (Purity, RandIndex, Recall, Precision and F1), as described in "Introduction to Information Retrieval" (Manning et al, 2008)
	 */
	public EvaluationMeasures evaluateMergeOfParaphrasingNodes(Set<EquivalenceClass> goldStandardNodes, Set<EquivalenceClass> evaluatedGraphNodes, boolean includeAllMeasures){
		EvaluationMeasures eval = new EvaluationMeasures();
		
		//  A map with the gold-standard clusters. Keys are cluster labels, values are lists containing the ids of items in the corresponding cluster
		HashMap<String, LinkedList<Integer>> evaluatedClusters = new HashMap<String, LinkedList<Integer>>();

		// Create index of statements and a map of evaluated clusters (equivalence classes)
		int cnt = 1;
		HashMap<String, Integer> entailmentUnitIndex = new HashMap<String,Integer>();  
		for (EquivalenceClass ec : evaluatedGraphNodes){
			// Initialize an entry in the map of evaluated clusters. Key is the label of the equivalence class
			evaluatedClusters.put(ec.getLabel(), new LinkedList<Integer>());
			// Add all entailment units from the equivalence class to the index and add their ids to evaluatedClusters
			for (EntailmentUnit eu : ec.getEntailmentUnits()){
				// add entailment unit to the index, if not yet there
				// TODO: do we need to check? Can an entailment unit potentially be present in more than one equivalence class? Currently assume it can...
				if (!entailmentUnitIndex.containsKey(eu.getText())) { 
					entailmentUnitIndex.put(eu.getText(), cnt);
					cnt++; 					
				}
				// add entailment unit's id to the corresponding cluster in evaluatedClusters map
				LinkedList<Integer> currCluster = evaluatedClusters.get(ec.getLabel());
				currCluster.add(entailmentUnitIndex.get(eu.getText()));
				evaluatedClusters.put(ec.getLabel(), currCluster);
			}
		}

		//  Create a map with gold-standard clusters. Same format as for evaluatedClusters
		HashMap<String, LinkedList<Integer>> goldStandardClusters = new HashMap<String, LinkedList<Integer>>();
		for (EquivalenceClass ec : goldStandardNodes){
			// Initialize an entry in the map of evaluated clusters. Key is the label of the equivalence class
			goldStandardClusters.put(ec.getLabel(), new LinkedList<Integer>());
			// Add ids of all entailment units from the equivalence class to the map and, if not yet there, to the index
			for (EntailmentUnit eu : ec.getEntailmentUnits()){
				// add entailment unit to the index, if not yet there
				// TODO: Do we need to check? Can an entailment unit be present in gold standard but not in evaluated clusters? This is not supposed to happen, but let's leave the check for now.
				if (!entailmentUnitIndex.containsKey(eu.getText())) { 
					entailmentUnitIndex.put(eu.getText(), cnt);
					cnt++; 					
				}
				// add entailment unit's id to the corresponding cluster in evaluatedClusters map
				LinkedList<Integer> currCluster = goldStandardClusters.get(ec.getLabel());
				currCluster.add(entailmentUnitIndex.get(eu.getText()));
				goldStandardClusters.put(ec.getLabel(), currCluster);
			}
		}
		
		
		TLClusteringResultsEvaluator clustEval = new TLClusteringResultsEvaluator(); 
		clustEval.calculatePurityAndBestFitGoldClusters(goldStandardClusters, evaluatedClusters);
		eval.setPurity(clustEval.getPurity());
		bestFitLabelsMap = clustEval.getBestFitLabelsMap();
		
		if (includeAllMeasures){
			// Create a set with ids of all entailment units
			HashSet<Integer> itemIds = new HashSet<Integer>(entailmentUnitIndex.values()); 

			Map<String,Double> moreMeasures = TLClusteringResultsEvaluator.calculateRecallPrecisionFmeasuresAndRandIndex(itemIds, goldStandardClusters, evaluatedClusters);
			eval.setPrecision(moreMeasures.get("P"));
			eval.setRecall(moreMeasures.get("R"));
			eval.setRandIndex(moreMeasures.get("randIndex"));
		}
		
		return eval;
	}

	public EvaluationMeasures evaluateEdges(Set<EquivalenceClass> goldStandardNodes, Set<EquivalenceClass> evaluatedGraphNodes, Set<EntailmentRelationCollapsed> goldStandardEdges, Set<EntailmentRelationCollapsed> evaluatedGraphEdges, boolean byPurity){
		
		if(byPurity && (bestFitLabelsMap == null)) evaluateMergeOfParaphrasingNodes(goldStandardNodes, evaluatedGraphNodes, false); // only perform purity calculations

		// follow the same procedure as in EvaluatorGraphMerger class
		double correctlyAddedEdges = 0.0;
		for (EntailmentRelationCollapsed gsEdge : goldStandardEdges){
			for (EntailmentRelationCollapsed evaluatedEdge : evaluatedGraphEdges){
				if (isSameSourceAndTarget(gsEdge, evaluatedEdge, byPurity)) correctlyAddedEdges++;
			}
		}
		
		EvaluationMeasures eval = new EvaluationMeasures();
		eval.setPrecision(correctlyAddedEdges/evaluatedGraphEdges.size());
		eval.setRecall(correctlyAddedEdges/goldStandardEdges.size());
		
		return eval;
	}
	
	private boolean isSameSourceAndTarget(EntailmentRelationCollapsed gsEdge, EntailmentRelationCollapsed evaluatedEdge, boolean byPurity){
		if (byPurity){
			String evaluatedBestFitSource = bestFitLabelsMap.get(evaluatedEdge.getSource().getLabel());
			String evaluatedBestFitTarget = bestFitLabelsMap.get(evaluatedEdge.getTarget().getLabel());
			if ((gsEdge.getSource().getLabel().equals(evaluatedBestFitSource)) && (gsEdge.getTarget().getLabel().equals(evaluatedBestFitTarget))) return true;		 
			return false;			
		}
		 
		// if not by purity, then use equivalence classes' labels to define whether the source and target are the same 
		if ((gsEdge.getSource().getLabel().equals(evaluatedEdge.getSource().getLabel())) && (gsEdge.getTarget().getLabel().equals(evaluatedEdge.getTarget().getLabel()))) return true;		 
		return false;
	}
	
}

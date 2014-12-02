package eu.excitementproject.tl.evaluation.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.eop.common.utilities.ClusteringResultsEvaluator;

/**
 * This class extends ClusteringResultsEvaluator, which contains methods for calculating clustering evaluation measures (Purity, RandIndex, Recall, Precision and F-measures), as described in 
 * "Introduction to Information Retrieval" (Manning et al, 2008)
 * The class adopts the methods for TL needs
 * 
 * @author Lili Kotlerman
 * 
 */
public class TLClusteringResultsEvaluator extends ClusteringResultsEvaluator{
	
	private Double purity;
	
	// holds the best-fit gold-standard labels for evaluated clusters. Key - real cluster's label, value - best-fit gold label
	private HashMap<String, String> bestFitLabelsMap;  
	
	
	public TLClusteringResultsEvaluator() {
		purity = null;
		bestFitLabelsMap = null;
	}


	public Double getPurity() {
		return purity;
	}


	public HashMap<String, String> getBestFitLabelsMap() {
		return bestFitLabelsMap;
	}



	/**
	 * @param goldStandardClusters - a hash table with the gold-standard clusters. Keys are cluster labels, values are lists containing the ids of items in the corresponding cluster
	 * @param evaluatedClusters - a hash table with the clusters to be evaluated (the same format as above)
	 * @return Putiry value
	 */
	public void calculatePurityAndBestFitGoldClusters(Map<String, ? extends List<Integer>> goldStandardClusters, Map<String, ? extends List<Integer>> evaluatedClusters){
		purity=0.0;
		bestFitLabelsMap = new HashMap<String, String>();
		double denominator=0.0;
		for (String clustName : evaluatedClusters.keySet()){
			denominator += evaluatedClusters.get(clustName).size(); //the number of reasons in the cluster
			double maxNominator = 0.0;
			String bestFitLabel = "NO_MATCH";
			for (String gsCluster : goldStandardClusters.keySet()){
				LinkedList<Integer> tmpGScluster = new LinkedList<Integer>();
				for (Integer x: goldStandardClusters.get(gsCluster)){
					tmpGScluster.add(x);
				}
				//System.out.print(clustName+" "+gsCluster+" : "+tmpGScluster.size()+ " ");
				tmpGScluster.retainAll(evaluatedClusters.get(clustName));
				//logger.info(tmpGScluster.size());
				if (!tmpGScluster.isEmpty()){
					double candidateNominator = tmpGScluster.size();
					if (maxNominator<candidateNominator) {
						maxNominator=candidateNominator;
						bestFitLabel = gsCluster; // candidate to be the best gold-standard cluster to fit the evaluated cluster
					}					
				}
			}
			purity += maxNominator;
			bestFitLabelsMap.put(clustName, bestFitLabel);
		}		
		purity /= denominator;
	}		
}
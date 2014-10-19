package eu.excitementproject.clustering.legacy;

import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;

@Deprecated
@SuppressWarnings("unused")
public abstract class CopyOfRankedResultsLoader implements ResultsLoader{

	private Hashtable<String,LinkedList<String>> rankedClustersToEvaluate;
	private Hashtable<String,LinkedList<Integer>> rankedClustersToEvaluateById;
	
	private final boolean b_LIMIT_CLUSTERS_PER_REASON = true;
	private final int CLUSTERS_LIMIT = 1;
	private final boolean retain_topK_clusters = true;
	private int topKclusters = 20;
	
	public Hashtable<String,LinkedList<String>> getClustersToEvaluate(){
		return rankedClustersToEvaluate;
	}
	
	public Hashtable<String,LinkedList<Integer>> getClustersToEvaluateById(){
		return rankedClustersToEvaluateById;
	}

	public Hashtable<Integer,LinkedList<String>> defineSelectedClusters(Hashtable<String,Hashtable<Integer,Double>> clusterReasonScores){
		System.out.println(clusterReasonScores);
		Hashtable<Integer,Hashtable<String,Double>> reasonClusterScores = new Hashtable<Integer, Hashtable<String,Double>>();
		for (String clustName : clusterReasonScores.keySet()){
			for (Integer rId : clusterReasonScores.get(clustName).keySet()){
				if (!reasonClusterScores.containsKey(rId)){
					reasonClusterScores.put(rId, new Hashtable<String, Double>());
				}
				double score = clusterReasonScores.get(clustName).get(rId);
				reasonClusterScores.get(rId).put(clustName,score);
			}
		}
		
		Hashtable<Integer,LinkedList<String>> selectedClustersPerReason = new Hashtable<Integer, LinkedList<String>>();
		for (Integer rId : reasonClusterScores.keySet()){
			Hashtable<String,Double> scores = reasonClusterScores.get(rId);
			
			LinkedList<Double> scoresList = new LinkedList<Double>();
			for (Double score : scores.values()){
				if(!scoresList.contains(score)){
					scoresList.add(score);
				}
			}
			Collections.sort(scoresList,Collections.reverseOrder());	
			LinkedList<String> winnerClusters = new LinkedList<String>();
			for (Double score : scoresList){
				if (winnerClusters.size()>=CLUSTERS_LIMIT) {
					// don't allow more than clustersLimit clusters to win, 
					// except if the limit was exceeded because several winners had the same score
					break;
				}
				for(String clustName : scores.keySet()){ 
					// add all the clusters with current score to winners
					if(scores.get(clustName).equals(score)){
						winnerClusters.add(clustName);
					}
				}
			}
			selectedClustersPerReason.put(rId, winnerClusters);
		}
		return selectedClustersPerReason;
	}
	
	
	public void prepareRankedClusters(
			Hashtable<String,Hashtable<Integer,Double>> clusterReasonScores,
			Hashtable<Integer, String> reasonsById){
				
		Hashtable<Integer,LinkedList<String>> selectedClustersPerReason = new Hashtable<Integer, LinkedList<String>>();
		if (b_LIMIT_CLUSTERS_PER_REASON){
			selectedClustersPerReason=defineSelectedClusters(clusterReasonScores);
		}
		
		rankedClustersToEvaluate=new Hashtable<String, LinkedList<String>>();
		rankedClustersToEvaluateById=new Hashtable<String, LinkedList<Integer>>();
	
		for (String clustName : clusterReasonScores.keySet()){
			Hashtable<Integer,Double> scores = clusterReasonScores.get(clustName);
			
			LinkedList<Double> scoresList = new LinkedList<Double>();
			for (Double score : scores.values()){
				if(!scoresList.contains(score)){
					scoresList.add(score);
				}
			}
			
			Collections.sort(scoresList,Collections.reverseOrder());
			
			LinkedList<Integer> idCluster = new LinkedList<Integer>();
			LinkedList<String> textCluster = new LinkedList<String>(); 
			for (Double score : scoresList){
				for(Integer rId: scores.keySet()){
					if (b_LIMIT_CLUSTERS_PER_REASON){
						// add only reasons for which current cluster is among the N (=clustersLimit) best-scoring clusters
						if (!selectedClustersPerReason.get(rId).contains(clustName)){
							continue; // proceed to the next reason
						}
					}
					if(scores.get(rId).equals(score)){
						idCluster.add(rId);
						textCluster.add(reasonsById.get(rId));
					}
				}
			}
			if (textCluster.size()>0){ // don't add clusters with no reasons in them
				// cluster can be empty if "limitClustersPerReason" is set to "true"
				rankedClustersToEvaluate.put(clustName, textCluster);
				rankedClustersToEvaluateById.put(clustName, idCluster);
			}
		}
		
	}
	
	public Hashtable<String,Hashtable<Integer,Double>> retainTopKClusters(Hashtable<String,Hashtable<Integer,Double>> clusterReasonScores){
		// retain top-K largest clusters
		
		LinkedList<Integer> sizesList = new LinkedList<Integer>();
		for (String clustName: clusterReasonScores.keySet()){
			sizesList.add(clusterReasonScores.get(clustName).size());
		}
		if (sizesList.size()<=topKclusters) return clusterReasonScores;
		
		Collections.sort(sizesList,Collections.reverseOrder());
		LinkedList<Integer> winnersSizeList = new LinkedList<Integer>();
		for (int i=0; i<topKclusters; i++){
			winnersSizeList.add(sizesList.get(i));
		}
		
		Hashtable<String,Hashtable<Integer,Double>> topKclusterReasonScores = new Hashtable<String, Hashtable<Integer,Double>>();
		for (String clustName : clusterReasonScores.keySet()){
			int currClusterSize = clusterReasonScores.get(clustName).size();
			if (winnersSizeList.contains(currClusterSize)){
				topKclusterReasonScores.put(clustName, clusterReasonScores.get(clustName));
			}
		}
		return topKclusterReasonScores;
	}
}

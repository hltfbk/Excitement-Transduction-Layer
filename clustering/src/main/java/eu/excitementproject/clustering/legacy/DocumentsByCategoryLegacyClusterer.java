package eu.excitementproject.clustering.legacy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.data.api.TextCollection;

@Deprecated
public class DocumentsByCategoryLegacyClusterer implements DocumentClusterer {

	
	private Map<String, List<String>> wordClusters= new HashMap<String, List<String>>();

	@Override
	public Map<String, List<Integer>> clusterDocuments(
			TextCollection textCollection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNumberOfDocumentClusters(int K) {
		// TODO Auto-generated method stub

	}


	public DocumentsByCategoryLegacyClusterer() {
	}

	// ===============================================
	
	private final boolean b_USE_TERM_COUNTS = true;
	

	private Map<Integer,Integer> reasonTermNumbers = new Hashtable<Integer, Integer>();
	private final boolean b_NORMALIZE_SCORES = true;
	
	
	
	private Map<String,Map<Integer,Double>> clusterReasonScores = new Hashtable<String, Map<Integer,Double>>();
	private final boolean filterDuplicateReasonsFromClusters = true;
	
	
	private void countReasonTermNumbers(Map<String, Map<Integer,Integer>> reasonsByTerm){
		for (String aTerm : reasonsByTerm.keySet()){
			for(Integer rId: reasonsByTerm.get(aTerm).keySet()){
				int count = reasonsByTerm.get(aTerm).get(rId);
				if(reasonTermNumbers.containsKey(rId)){
					count+=reasonTermNumbers.get(rId);
				}
				reasonTermNumbers.put(rId, count);
			}
		}
	}
	
	public void loadWordResults(String resultsFilename,Map<String,Integer> termFrequencies,
			Map<String, Map<Integer,Integer>> reasonsByTerm, 
			Map<Integer, String> reasonsById) throws IOException, FileNotFoundException{
		// load word clusters

		//prepare reasonTermNumbers
		countReasonTermNumbers(reasonsByTerm);
		// load word clusters
		for(String category : wordClusters.keySet()) {
			List<String> currCluster = wordClusters.get(category);
	    	LinkedList<Integer> intCluster = new LinkedList<Integer>();
	    	LinkedList<String> reasonsCluster = new LinkedList<String>();
	    	String clustName="";		
			clusterReasonScores.put(clustName,new Hashtable<Integer, Double>());
	    	Object[] wordsRepresentingCluster = currCluster.toArray();
	    	for(int i=0; i<wordsRepresentingCluster.length; i++){
	    		String aWord = wordsRepresentingCluster[i].toString();
	    		clustName+=aWord+", ";
	    		double wordWeight = 1.0;
	    		if (b_USE_TERM_COUNTS) {
	    			if (termFrequencies.containsKey(aWord)){
	    				wordWeight=termFrequencies.get(aWord);
	    			}
	    		}
	    		if(reasonsByTerm.containsKey(aWord)){
	    			for (Integer rId : reasonsByTerm.get(aWord).keySet()){
	    				double wordCountInReason = reasonsByTerm.get(aWord).get(rId);
	    				if (reasonsCluster.contains(reasonsById.get(rId))){
	    					Double oldScore=clusterReasonScores.get(clustName).get(rId);
	    					clusterReasonScores.get(clustName).put(rId,oldScore+wordCountInReason*wordWeight); //increase freq by 1
	    					if (filterDuplicateReasonsFromClusters) continue;
	    				}
	    				intCluster.add(rId);
	    				reasonsCluster.add(reasonsById.get(rId));
	    				//put frequency*wordWeight as score - meanwhile saw this reason with this cluster one time
	    				clusterReasonScores.get(clustName).put(rId, wordCountInReason*wordWeight);
	    			}
	    			
	    		}

	    
		    	if(b_NORMALIZE_SCORES){
			    	// normalize scores within the cluster - divide by |clusterTerms|*|reasonTerms| to get cosine score (weights =1)
		    		double clusterTermsNumber = wordsRepresentingCluster.length;
		    	//	System.out.println(clustName);
		    		if(!clusterReasonScores.get(clustName).isEmpty()){
		    			for(Integer rId : clusterReasonScores.get(clustName).keySet()){
		    				double score = clusterReasonScores.get(clustName).get(rId);
		    				score /= (clusterTermsNumber*reasonTermNumbers.get(rId));
		    			//	score /= reasonTermNumbers.get(rId);
		    				clusterReasonScores.get(clustName).put(rId, score);
		    			}
		    		}
		    	}
		    }
		}
		prepareRankedClusters(clusterReasonScores,reasonsById);
	}	
	
	//=========================================================================
	private Map<String,LinkedList<String>> rankedClustersToEvaluate;
	private Map<String,LinkedList<Integer>> rankedClustersToEvaluateById;
	
	private final boolean b_LIMIT_CLUSTERS_PER_REASON = true;
	private final int CLUSTERS_LIMIT = 1;
	private int minClusterSize=1;

	public Map<Integer,LinkedList<String>> defineSelectedClusters(Map<String,Map<Integer,Double>> clusterReasonScores){
		System.out.println(clusterReasonScores);
		
		Map<Integer, Map<String,Double>> reasonClusterScores = new Hashtable<Integer, Map<String,Double>>();
		
		for (String clustName : clusterReasonScores.keySet()){
			for (Integer rId : clusterReasonScores.get(clustName).keySet()){
				if (!reasonClusterScores.containsKey(rId)){
					reasonClusterScores.put(rId, new Hashtable<String, Double>());
				}
				double score = clusterReasonScores.get(clustName).get(rId);
				reasonClusterScores.get(rId).put(clustName,score);
			}
		}
		
		Map<Integer,LinkedList<String>> selectedClustersPerReason = new Hashtable<Integer, LinkedList<String>>();
		for (Integer rId : reasonClusterScores.keySet()){
			Map<String,Double> scores = reasonClusterScores.get(rId);
			
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
			Map<String,Map<Integer,Double>> clusterReasonScores,
			Map<Integer, String> reasonsById){	

		prepareRankedClustersIteration(clusterReasonScores,reasonsById);

/*		prepareRankedClustersIteration(clusterReasonScores,reasonsById);
		System.out.println("Prepared ranked clusters: "+rankedClustersToEvaluate.size());
		// retain top K largest clusters
		if (retain_topK_clusters){
			Map<String,Map<Integer,Double>> topKclusterReasonScores = retainTopKRankedClusters(clusterReasonScores);
			System.out.println("Retained clusters: "+topKclusterReasonScores.size());
			prepareRankedClustersIteration(topKclusterReasonScores, reasonsById);
		}*/
		
		// retain top K largest clusters
	/*	if (retain_topK_clusters){
			System.out.println("Initial clusters: "+clusterReasonScores.size());
			clusterReasonScores = retainTopKClusters(clusterReasonScores,100);
			System.out.println("Retained clusters: "+clusterReasonScores.size());
		}*/		
	//	retainTopKRankedClusters(clusterReasonScores, 40);
	}
	
	public void prepareRankedClustersIteration(
			Map<String,Map<Integer,Double>> clusterReasonScores,
			Map<Integer, String> reasonsById){
		
		
		
		Map<Integer,LinkedList<String>> selectedClustersPerReason = new Hashtable<Integer, LinkedList<String>>();
		if (b_LIMIT_CLUSTERS_PER_REASON){
			selectedClustersPerReason=defineSelectedClusters(clusterReasonScores);
		}
		
		rankedClustersToEvaluate=new Hashtable<String, LinkedList<String>>();
		rankedClustersToEvaluateById=new Hashtable<String, LinkedList<Integer>>();
	
		for (String clustName : clusterReasonScores.keySet()){
			Map<Integer,Double> scores = clusterReasonScores.get(clustName);
			
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
			if (textCluster.size()>=minClusterSize){ // don't add clusters with less than minClusterSize reasons in them
				// cluster can be empty if "limitClustersPerReason" is set to "true"
				rankedClustersToEvaluate.put(clustName, textCluster);
				rankedClustersToEvaluateById.put(clustName, idCluster);
			}
		}
		
	}
	
	public Map<String,Map<Integer,Double>> retainTopKClusters(Map<String,Map<Integer,Double>> clusterReasonScores, int K){
		// retain top-K largest clusters
		
		LinkedList<Integer> sizesList = new LinkedList<Integer>();
		for (String clustName: clusterReasonScores.keySet()){
			sizesList.add(clusterReasonScores.get(clustName).size());
		}
		if (sizesList.size()<=K) return clusterReasonScores;
		
		Collections.sort(sizesList,Collections.reverseOrder());
		LinkedList<Integer> winnersSizeList = new LinkedList<Integer>();
		for (int i=0; i<K; i++){
			winnersSizeList.add(sizesList.get(i));
		}
		
		Map<String,Map<Integer,Double>> topKclusterReasonScores = new Hashtable<String, Map<Integer,Double>>();
		for (String clustName : clusterReasonScores.keySet()){
			int currClusterSize = clusterReasonScores.get(clustName).size();
			if (winnersSizeList.contains(currClusterSize)){
				topKclusterReasonScores.put(clustName, clusterReasonScores.get(clustName));
			}
		}
		return topKclusterReasonScores;
	}
	
	public Map<String,Map<Integer,Double>> retainTopKRankedClusters(Map<String,Map<Integer,Double>> clusterReasonScores,int K){
		// retain top-K largest clusters
		
		LinkedList<Integer> sizesList = new LinkedList<Integer>();
		for (String clustName: rankedClustersToEvaluate.keySet()){
			sizesList.add(rankedClustersToEvaluate.get(clustName).size());
		}
		if (sizesList.size()<=K) return clusterReasonScores;
		
		Collections.sort(sizesList,Collections.reverseOrder());
		LinkedList<Integer> winnersSizeList = new LinkedList<Integer>();
		for (int i=0; i<K; i++){
			winnersSizeList.add(sizesList.get(i));
		}
		
		System.out.println(winnersSizeList.size()+": "+winnersSizeList);
		Map<String,Map<Integer,Double>> topKclusterReasonScores = new Hashtable<String, Map<Integer,Double>>();
		for (String clustName : rankedClustersToEvaluate.keySet()){
			int currClusterSize = rankedClustersToEvaluate.get(clustName).size();
			if (winnersSizeList.contains(currClusterSize)){
				if (!topKclusterReasonScores.containsKey(clustName)){
					topKclusterReasonScores.put(clustName, clusterReasonScores.get(clustName));
				}
			}
		}
		return topKclusterReasonScores;
	}
	
		
}

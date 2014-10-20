package eu.excitementproject.clustering.legacy;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;



@Deprecated
public class ClusteringRankedResultsLoader<V> extends RankedResultsLoader {

	/**
	 * @param args
	 */

	private final boolean b_USE_TERM_COUNTS = true;
	

	private Map<Integer,Integer> reasonTermNumbers = new Hashtable<Integer, Integer>();
	private final boolean b_NORMALIZE_SCORES = true;
	
	private LinkedList<LinkedList<String>> wordClusters= new LinkedList<LinkedList<String>>();

	
	private Map<String,Map<Integer,Double>> clusterReasonScores = new Hashtable<String, Map<Integer,Double>>();
	private final boolean filterDuplicateReasonsFromClusters = true;
	
	public enum NodeType{
		WORD,
		SENTENCE
	}
	
	private Set<Set<V>> clustering;
	private NodeType nType;
	

	
	public ClusteringRankedResultsLoader(Set<Set<V>> aClustering, NodeType nodeType) {
		clustering = aClustering;
		nType=nodeType;
	}
	
	@Override
	public void loadResults(String resultsFilename,Map<String,Integer> termFrequencies,
			Map<String, Map<Integer,Integer>> reasonsByTerm, 
			Map<Integer, String> reasonsById) throws IOException, FileNotFoundException{
		if (nType.equals(NodeType.WORD)){
			loadWordResults(resultsFilename, termFrequencies, reasonsByTerm, reasonsById);
		}
		if (nType.equals(NodeType.SENTENCE)){
			loadSentenceResults(resultsFilename, termFrequencies, reasonsByTerm, reasonsById);
		}
	}

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
		while(clustering.iterator().hasNext()) {
			Set<V> currCluster= clustering.iterator().next();
	    	LinkedList<Integer> intCluster = new LinkedList<Integer>();
	    	LinkedList<String> reasonsCluster = new LinkedList<String>();
	    	LinkedList<String> wordslist = new LinkedList<String>();
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
	    		wordslist.add(aWord);
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

		    	wordClusters.add(wordslist);
	    
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
	
	public void loadSentenceResults(String resultsFilename,Map<String,Integer> termFrequencies,
			Map<String, Map<Integer,Integer>> reasonsByTerm, 
			Map<Integer, String> reasonsById) throws IOException, FileNotFoundException{
		// load word clusters

			Integer clustId=0;
			while(clustering.iterator().hasNext()) {
				Set<V> currCluster = clustering.iterator().next();
				clustId++;
		    	LinkedList<Integer> intCluster = new LinkedList<Integer>();
		    	LinkedList<String> reasonsCluster = new LinkedList<String>();
			    String clustName=clustId.toString();			    	
			    Object[] reasons = currCluster.toArray();
		    	for(int i=0; i<reasons.length; i++){
		    		Integer rId = Integer.valueOf(reasons[i].toString());
		    		if (reasonsCluster.contains(reasonsById.get(rId))){
    					Double oldScore=clusterReasonScores.get(clustName).get(rId);
    					clusterReasonScores.get(clustName).put(rId,oldScore+1);
    					if (filterDuplicateReasonsFromClusters) continue;
    				}
    				intCluster.add(rId);
    				reasonsCluster.add(reasonsById.get(rId));
    				//put frequency as score - meanwhile saw this reason with this cluster one time
    				if(!clusterReasonScores.containsKey(clustName)){
    					clusterReasonScores.put(clustName,new Hashtable<Integer, Double>());
    				}
    				clusterReasonScores.get(clustName).put(rId, 1.0);
    			}
				prepareRankedClusters(clusterReasonScores,reasonsById);
		    }
	}
}
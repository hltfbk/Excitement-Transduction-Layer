package eu.excitementproject.clustering.legacy;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;




@Deprecated
public class ClusteringResultsLoader<V> implements ResultsLoader{

	/**
	 * @param args
	 */

	private LinkedList<LinkedList<String>> wordClusters= new LinkedList<LinkedList<String>>();
	private Hashtable<String,LinkedList<Integer>> clustersToEvaluateById= new Hashtable<String, LinkedList<Integer>>();
	private Hashtable<String,LinkedList<String>> clustersToEvaluate= new Hashtable<String, LinkedList<String>>();

	
	private Hashtable<String,Hashtable<Integer,Double>> clusterReasonScores = new Hashtable<String, Hashtable<Integer,Double>>();
	private final boolean filterDuplicateReasonsFromClusters = true;
	
	public enum NodeType{
		WORD,
		SENTENCE
	}
	
	private Set<Set<V>> clustering;
	private NodeType nType;
	

	
	public ClusteringResultsLoader(Set<Set<V>> aClustering, NodeType nodeType) {
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


	public void loadWordResults(String resultsFilename,Map<String,Integer> termFrequencies,
			Map<String, Map<Integer,Integer>> reasonsByTerm, 
			Map<Integer, String> reasonsById) throws IOException, FileNotFoundException{
		// load word clusters

			while(clustering.iterator().hasNext()) {
				Set<V> currCluster = clustering.iterator().next();
		    	LinkedList<Integer> intCluster = new LinkedList<Integer>();
		    	LinkedList<String> reasonsCluster = new LinkedList<String>();
		    	LinkedList<String> wordslist = new LinkedList<String>();
			    String clustName="";			    	
			    Object[] words = currCluster.toArray();
		    	for(int i=0; i<words.length; i++){
		    		String aWord = words[i].toString();
		    		clustName+=aWord+", ";
		    		wordslist.add(aWord);
		    		if(reasonsByTerm.containsKey(aWord)){
		    			for (Integer rId : reasonsByTerm.get(aWord).keySet()){
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
			    			
		    		}
		    	}
		    	wordClusters.add(wordslist);
		    	clustersToEvaluateById.put(clustName,intCluster);
		    	clustersToEvaluate.put(clustName,reasonsCluster);
		    }
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
		    	clustersToEvaluateById.put(clustName,intCluster);
		    	clustersToEvaluate.put(clustName,reasonsCluster);
		    }
	}

	@Override
	public Hashtable<String, LinkedList<Integer>> getClustersToEvaluateById() {
		return clustersToEvaluateById;
	}

	@Override
	public Hashtable<String, LinkedList<String>> getClustersToEvaluate() {
		return clustersToEvaluate;
	}
}

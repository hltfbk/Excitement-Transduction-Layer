package eu.excitementproject.clustering.eval;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MeanAveragePrecisionCalulator {
	
	public static double getClusterAP(List<Integer> gsDocumentsInCluster, List<Integer> documentsInCluster){
		
		double ret_num=0.0;
		double rel_num=0.0;
		double ap=0.0;
        for (Integer docId : documentsInCluster){
            ret_num++;
            if (gsDocumentsInCluster.contains(docId)){
                rel_num++;
                ap+=(rel_num/ret_num);        	            	
            }
        }
        ap /= gsDocumentsInCluster.size();
	    return ap;
	}
	
	/**
	 * @param goldStandardClusters - a hash table with the gold-standard clusters. Keys are cluster labels, values are LinkedLists containing the ids of items in the corresponding cluster
	 * @param evaluatedClusters - a hash table with the clusters to be evaluated (the same format as above)
	 * @return MAP value, where individual AP values are calculated for each cluster compared to most similar GS cluster (detected in the same way as for purity calculations)
	 */
	public static double calculateMAP(Map<String, ? extends List<Integer>> goldStandardClusters, Map<String, ? extends List<Integer>> evaluatedClusters){
		double map=0.0;
		for (String clustName : evaluatedClusters.keySet()){
			int maxCommonDocuments = 0;
			String bestFitCluster = "";
			for (String gsCluster : goldStandardClusters.keySet()){
				LinkedList<Integer> tmpGScluster = new LinkedList<Integer>();
				for (Integer x: goldStandardClusters.get(gsCluster)){
					tmpGScluster.add(x);
				}
				tmpGScluster.retainAll(evaluatedClusters.get(clustName));
				int commonDocumentsWithCandidate = tmpGScluster.size();
				if (maxCommonDocuments<commonDocumentsWithCandidate) {
					maxCommonDocuments=commonDocumentsWithCandidate;
					bestFitCluster = gsCluster;
				}
			}
			if (goldStandardClusters.containsKey(bestFitCluster)) {
				map += getClusterAP(goldStandardClusters.get(bestFitCluster), evaluatedClusters.get(clustName));
			}
		}		
		map /= evaluatedClusters.size();
		return map;
	}	

}

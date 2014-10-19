package eu.excitementproject.clustering.clustering.impl.util.rep;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.eop.common.datastructures.PairMap;


/**
 * @author Lili Kotlerman
 *
 */
public class StringsPatrition {	
	
	private final Map<String, List<String>> clusters;
	
	
	public StringsPatrition(Map<String, List<String>> clusters,
			PairMap<String, Double> semanticRelatednessMap) {
		super();
		this.clusters = clusters;
	}

	/**
	 * @return the clusters
	 */
	public Map<String, List<String>> getClusters() {
		return clusters;
	}	

	public Map<String, List<String>> getSortedClusters(PairMap<String, Double> semanticRelatednessMap){
		Map<String, List<String>> sortedRes = new HashMap<String, List<String>>();
		for (String label : clusters.keySet()){
			sortedRes.put(label, sortBySemanticRelatednessToCluster(clusters.get(label), semanticRelatednessMap));
		}		
		return sortedRes;
	}
	
	private List<String> sortBySemanticRelatednessToCluster(List<String> cluster, PairMap<String, Double> semanticRelatednessMap){		
		List<ClusterMember<String>> tmpCluster = new LinkedList<ClusterMember<String>>();
		for (String term : cluster){
			double score = SimilarityAndRelatednessCalculator.getSemanticRelatednessToCluster(term, new HashSet<String>(cluster), semanticRelatednessMap);
			tmpCluster.add(new ClusterMember<String>(term, score));
		}
		
		Collections.sort(tmpCluster, new ClusterMember.ReverseOrderByScoreComparator<String>());		
		List<String> sortedCluster = new LinkedList<String>();
		for (ClusterMember<String> term : tmpCluster){
			sortedCluster.add(term.member);
		}
		return sortedCluster;
	}
	
	
	public static List<String> getSortedByScoreCluster(List<ClusterMember<String>> cluster){		
		Collections.sort(cluster, new ClusterMember.ReverseOrderByScoreComparator<String>());		
		List<String> sortedCluster = new LinkedList<String>();
		for (ClusterMember<String> term : cluster){
			sortedCluster.add(term.member);
		}
		return sortedCluster;
	}	
	
}

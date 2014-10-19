package eu.excitementproject.clustering.clustering.impl.completeLink;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import java.util.Map;
import java.util.Set;

import com.aliasi.cluster.CompleteLinkClusterer;

import com.aliasi.util.Distance;

import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.impl.completeLink.util.SemanticDistance;
import eu.excitementproject.clustering.clustering.impl.util.rep.StringsPatrition;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 * 
 * Wrapper for LingPype Complete Link tool
 * Since the tool does not provide scores/confidence values of term assignments to clusters, cluster members are sorted by semantic relatedness to the cluster 
 */
public class TermsCompleteLinkClusterer implements TermClusterer {

	int m_topKclusters = 10;
		
	@Override
	public Map<String, List<String>> clusterTerms(
			TextCollection textCollection) {

		// cluster all the terms in the collection (original + expansions) using semantic relatedness to measure distance
		Distance<String> distance = new SemanticDistance(textCollection.getTermSemanticRelatednessMap());
		Set<String> collectionTerms = new HashSet<String>(textCollection.getDocIdsByOriginalTerm().keySet());
		collectionTerms.addAll(textCollection.getDocIdsByExpansionTerm().keySet());
		System.out.println("Start clustering of "+String.valueOf(collectionTerms.size())+" terms.");
		
		StringsPatrition res = new StringsPatrition(termClusterer(distance, collectionTerms, m_topKclusters), textCollection.getTermSemanticRelatednessMap());
		return res.getSortedClusters(textCollection.getTermSemanticRelatednessMap());		
	}

	private Map<String,List<String>> termClusterer(Distance<String> distance, Set<String> terms, int topKClusters){
		CompleteLinkClusterer<String> clusterer = new CompleteLinkClusterer<String>(1, distance);
		Map<String,List<String>> clusteringResults = new HashMap<String, List<String>>();
		System.out.println("Done. Retrieving the clusters.");		
		int clusterId = 1;
		for(Set<String> cluster : clusterer.hierarchicalCluster(terms).partitionK(topKClusters)){
			//Set<String> termsInCuster = new HashSet<String>(cluster);
//			clusteringResults.put(String.valueOf(clusterId), termsInCuster);
			clusteringResults.put(String.valueOf(clusterId), new LinkedList<String>(cluster));
			clusterId++;
			System.out.print(clusterId);
			System.out.print(":");
			System.out.println(cluster.size());
			System.out.println(cluster);
		}
		return clusteringResults;
	}

	@Override
	public void setNumberOfTermClusters(int K) {
		m_topKclusters = K;
	}
}

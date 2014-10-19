package eu.excitementproject.clustering.clustering.impl.util.rep;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;


public class VectorsPartition {	
	
	private final Map<String, List<Integer>> clusters;
	private final Map<Integer, VectorRepresentation> documents;
	
	
	public VectorsPartition(Map<String, List<Integer>> clusters,
			Set<VectorRepresentation> setOfDocuments) {
		super();
		this.clusters = clusters;
		documents = new HashMap<Integer, VectorRepresentation>();
		for (VectorRepresentation doc : setOfDocuments){
			documents.put(doc.getId(), doc);
		}
	}

	/**
	 * @return the clusters
	 */
	public Map<String, List<Integer>> getClusters() {
		return clusters;
	}	
	
	/**
	 * @return the documents
	 */
	public Set<VectorRepresentation> getAllDocuments() {
		return new HashSet<VectorRepresentation>(documents.values());
	}

	public Map<String, List<Integer>> getSortedClusters(){
		Map<String, List<Integer>> sortedRes = new HashMap<String, List<Integer>>();
		for (String label : clusters.keySet()){
			sortedRes.put(label, sortBySimilarityToCentroid(clusters.get(label)));
		}		
		return sortedRes;
	}
	
	public List<Integer> sortBySimilarityToCentroid(List<Integer> cluster){
		List<VectorRepresentation> docsInCluster = new LinkedList<VectorRepresentation>();
		for (Integer docId : cluster){
			docsInCluster.add(documents.get(docId));
		}
		VectorRepresentation centroid = SimilarityAndRelatednessCalculator.getCentroid(docsInCluster);
		
		List<ClusterMember<Integer>> tmpCluster = new LinkedList<ClusterMember<Integer>>();
		for (Integer docId : cluster){
			double score = SimilarityAndRelatednessCalculator.getCosineSimilarityToCentroid(documents.get(docId), centroid);
			tmpCluster.add(new ClusterMember<Integer>(docId, score));
		}
		
		Collections.sort(tmpCluster, new ClusterMember.ReverseOrderByScoreComparator<Integer>());		
		List<Integer> sortedCluster = new LinkedList<Integer>();
		for (ClusterMember<Integer> doc : tmpCluster){
			sortedCluster.add(doc.member);
		}
		return sortedCluster;
	}
	
	
	public List<Integer> sortClusters(List<ClusterMember<Integer>> cluster){
		Collections.sort(cluster, new ClusterMember.ReverseOrderByScoreComparator<Integer>());		
		List<Integer> sortedCluster = new LinkedList<Integer>();
		for (ClusterMember<Integer> doc : cluster){
			sortedCluster.add(doc.member);
		}
		return sortedCluster;
	}	
	
}

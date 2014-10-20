package eu.excitementproject.clustering.clustering.impl.completeLink;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.cluster.CompleteLinkClusterer;

import com.aliasi.util.Distance;

import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.impl.completeLink.util.CosineDistance;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.clustering.impl.util.rep.VectorsPartition;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 * 
 * Wrapper for LingPype Complete Link tool
 * Since the tool does not provide scores/confidence values of document assignments to clusters, cluster members are sorted by similarity to centroid vector 
 *
 */
public abstract class AbstractDocumentCompleteLinkClusterer implements DocumentClusterer {

	int m_topKclusters = 10;
	boolean m_useExpandedCollection;
	WeightType m_weightType;
		
	public AbstractDocumentCompleteLinkClusterer(boolean useExpandedCollection, WeightType weightType) {
		m_useExpandedCollection = useExpandedCollection;
		m_weightType = weightType;
		
	}

	@Override
	public Map<String, List<Integer>> clusterDocuments(
			TextCollection textCollection) {		
		// represent documents as term vectors and cluster the vectors using cosine distance
		Set<VectorRepresentation> documents = representDocuments(textCollection);
		Distance<VectorRepresentation> distance = new CosineDistance();
		
		VectorsPartition res = new VectorsPartition(vectorClusterer(distance, documents, m_topKclusters), documents);
		return res.getSortedClusters();
	}
	
	public static Map<String, List<Integer>> clusterDocuments(Set<VectorRepresentation> documentVectors, int topKclusters) {
		// cluster the given vectors using cosine distance
		Distance<VectorRepresentation> distance = new CosineDistance();

		VectorsPartition res = new VectorsPartition(vectorClusterer(distance, documentVectors, topKclusters), documentVectors);
		return res.getSortedClusters();
	}	

	protected abstract Set<VectorRepresentation> representDocuments(TextCollection textCollection);
	
	private static Map<String,List<Integer>> vectorClusterer(Distance<VectorRepresentation> distance, Set<VectorRepresentation> documents, int topKClusters){
		CompleteLinkClusterer<VectorRepresentation> clusterer = new CompleteLinkClusterer<VectorRepresentation>(1, distance);
	
		Map<String,List<Integer>> clusteringResults = new HashMap<String, List<Integer>>();
		int clusterId = 1;
		for(Set<VectorRepresentation> cluster : clusterer.hierarchicalCluster(documents).partitionK(topKClusters)){
//			System.out.println(clusterId+": ");
			List<Integer> docIds = new LinkedList<Integer>();
			for (VectorRepresentation docVector : cluster){
				docIds.add(docVector.getId());
//				System.out.println("\t"+docVector.getId());
			}
			clusteringResults.put(String.valueOf(clusterId), docIds);
			clusterId++;
		}
		return clusteringResults;
	}

	@Override
	public void setNumberOfDocumentClusters(int K) {
		m_topKclusters = K;
	}
}

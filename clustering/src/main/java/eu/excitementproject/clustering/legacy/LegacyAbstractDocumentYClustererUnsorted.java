package eu.excitementproject.clustering.legacy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 * 
 * Implementation of the Y-clustering algorithm by Hui Ye and Steve Young, "A clustering approach to semantic decoding." INTERSPEECH. 2006. *
 * 
 * If a negative number is passed to setNumberOfDocumentClusters(), no top-K cut-off will be performed in the final iteration.  
 * Final templates ('centroid' documents) are used as cluster labels 
 */
public abstract class LegacyAbstractDocumentYClustererUnsorted extends LegacyAbstractYClustererUnsorted<VectorRepresentation> implements DocumentClusterer {

	public LegacyAbstractDocumentYClustererUnsorted(boolean useExpandedCollection,
			WeightType weightType, Double similarityThreshold) {
		super(useExpandedCollection, weightType, similarityThreshold);
		// TODO Auto-generated constructor stub
	}		

	@Override
	public Map<String, List<Integer>> clusterDocuments(
			TextCollection textCollection) {
		// represent documents as vectors and cluster the vectors using cosine distance
		return vectorClusterer(representDocuments(textCollection), m_topKclusters);
	}
	
	protected abstract Set<VectorRepresentation> representDocuments(TextCollection textCollection);
	
	private Map<String,List<Integer>> vectorClusterer(Set<VectorRepresentation> documents, int topKClusters){
		// get Y-clustering results
		Map<VectorRepresentation,List<VectorRepresentation>> res = clusterElements(documents, topKClusters);
		
		// translate results to the output format
		Map<String,List<Integer>> clusteringResults = new HashMap<String, List<Integer>>();
		for (VectorRepresentation template : res.keySet()){
			String clusterLabel = template.toString();
			List<Integer> docsInCluster = new LinkedList<Integer>();
			for (VectorRepresentation document : res.get(template)){
				docsInCluster.add(document.getId());
			}
			clusteringResults.put(clusterLabel, docsInCluster);
		}
			
		return clusteringResults;
	}

	@Override
	public void setNumberOfDocumentClusters(int K) {
		m_topKclusters = K;
	}
		
	@Override
	protected double getSimilarity(VectorRepresentation documentA, VectorRepresentation documentB){
		return SimilarityAndRelatednessCalculator.cosineSimilarity(documentA, documentB);
	}
	
	@Override
	protected VectorRepresentation getOutOfClassKey() {
		Map<String,Double> miscVector = new HashMap<String,Double>();
		miscVector.put(NON_CLUSTER_LABEL, 0.0);
		return new VectorRepresentation(-1, miscVector);
	}
	

}

package eu.excitementproject.clustering.clustering.impl.chinesewhispers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.Clusterer;
import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * Extends {@link AbstractChineseWhispersClusterer} for document clustering
 * 
 * @author Lili Kotlerman
 * 
 */
public abstract class AbstractDocumentChineseWhispersClusterer extends AbstractChineseWhispersClusterer<VectorRepresentation> implements DocumentClusterer {

		
	/**
	 * @param useExpandedCollection
	 * @param textCollection
	 * @param weightType
	 * @param configurationFilename
	 * @param similarityThreshold
	 * @throws ClusteringException
	 */
	public AbstractDocumentChineseWhispersClusterer(boolean useExpandedCollection,
			TextCollection textCollection, WeightType weightType,
			String configurationFilename, Double similarityThreshold)
			throws ClusteringException {
		super(useExpandedCollection, textCollection, weightType, configurationFilename,
				similarityThreshold);
		m_inputFilenamePrefix += "doc_";
	}

	@Override
	public Map<String, List<Integer>> clusterDocuments(
			TextCollection textCollection) throws ClusteringException {
		// represent documents as vectors and cluster the vectors using cosine distance
		return vectorClusterer(representDocuments(textCollection));
	}
	
	/** 
	 * @param documents - set of the input documents in {@link VectorRepresentation} format  
	 * @return Mapping of cluster names (String) to the corresponding sorted lists of document ids
	 * @throws ClusteringException
	 */
	private Map<String,List<Integer>> vectorClusterer(Set<VectorRepresentation> documents) throws ClusteringException{
		Set<Integer> clusteredDocIds = new HashSet<Integer>(); 
		
		// get CW-clustering results
		Map<String,List<String>> res = clusterElements(documents);
		
		// translate results to the output format
		Map<String,List<Integer>> clusteringResults = new HashMap<String, List<Integer>>();
		for (String clusterLabel : res.keySet()){
			List<Integer> docsInCluster = new LinkedList<Integer>();
			for (String documentId : res.get(clusterLabel)){
				docsInCluster.add(Integer.valueOf(documentId));
				clusteredDocIds.add(Integer.valueOf(documentId));
			}
			clusteringResults.put(clusterLabel, docsInCluster);
		}
		
		// add any unclustered documents to a non-clust cluster
		List<Integer> docsInCluster = new LinkedList<Integer>();
		for (VectorRepresentation doc : documents){
			Integer id = doc.getId();
			if (!clusteredDocIds.contains(id)) docsInCluster.add(id);
		}
		clusteringResults.put(Clusterer.NON_CLUSTER_LABEL, docsInCluster);
		
		return clusteringResults;
	}	
	
	/**
	 * Get the set of documents in a text collection in {@link VectorRepresentation} format  
	 * @param textCollection - a {@link TextCollection}, the documents in which are to be represented as vectors
	 * @return set of vector representations
	 */
	protected abstract Set<VectorRepresentation> representDocuments(TextCollection textCollection);
	
	
	@Override
	protected double getSimilarity(VectorRepresentation documentA, VectorRepresentation documentB){
		return SimilarityAndRelatednessCalculator.cosineSimilarity(documentA, documentB);
	}
	
	@Override
	protected String getElementString(VectorRepresentation element) {
		return element.getId().toString();
	}
	

}

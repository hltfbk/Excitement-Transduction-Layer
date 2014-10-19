package eu.excitementproject.clustering.legacy;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;



import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.tc.DocumentByCategoryClusterer;
import eu.excitementproject.clustering.clustering.impl.util.DocumentsToVectorsConverter;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

@Deprecated
public class DocumentByTopKCategoriesClustererLegacy extends DocumentByCategoryClusterer{

	int m_topKclusters = 10;
	
	@Override
	public void setNumberOfDocumentClusters(int K) {
		m_topKclusters = K;		
	}

	
	public DocumentByTopKCategoriesClustererLegacy(TextCollection textCollection,
			Map<String, List<String>> termClustersAsCategories,
			WeightType docWeightType,
			WeightType catWeightType) {
		super(textCollection, termClustersAsCategories, docWeightType,
				catWeightType);
	}
	
	@Override
	public Map<String, List<Integer>> clusterDocuments(TextCollection textCollection) throws ClusteringException {
		Set<VectorRepresentation> documents = DocumentsToVectorsConverter.convertDocumentsToDenseTermVectors(textCollection, m_useExpandedCollection, m_docWeightType);
		Set<VectorRepresentation> categories = convertCategoriesToVectors(textCollection);
		for (VectorRepresentation document : documents){
			assignDocumentToBestCategory(document, categories);
		}
		System.out.println("Done with first assignment");
		// retain only topK biggest clusters
		if (documentsByCategory.size() > m_topKclusters){
			retainTopKBiggestDocumentClusters();
		}
		
		//TODO reassign documents from deleted clusters!
		
		return getDocumentClusters();
	}


	private List<Integer> retainTopKBiggestDocumentClusters(){
		List<Integer> documentsToReassign = new LinkedList<Integer>();
		
		LinkedList<Integer> clusterSizes = new LinkedList<Integer>();
		Map<Integer,List<String>> clusterSizeIndex = new HashMap<Integer,List<String>>(); 
		for (String categoryLablel: documentsByCategory.keySet()){
			int clusterSize = documentsByCategory.get(categoryLablel).size();
			clusterSizes.add(clusterSize);
			List<String> labels = new LinkedList<String>();
			if (clusterSizeIndex.containsKey(clusterSize)) labels = clusterSizeIndex.get(clusterSize);
			labels.add(categoryLablel);
			clusterSizeIndex.put(clusterSize, labels);
		}
		Comparator<Integer> comparator = Collections.reverseOrder();
		Collections.sort(clusterSizes,comparator);
		
		int thresholdSizeForDelete = clusterSizes.get(m_topKclusters); // this is the size of the first candidate for delete, which is the K-th element of the sorted list of cluster sizes
		if (clusterSizes.get(m_topKclusters-1) > thresholdSizeForDelete){ // if the previous cluster is bigger - then delete all the clusters of size <= threshold size
			documentsToReassign = deleteDocumentClustersBySizeThreshold(thresholdSizeForDelete, clusterSizeIndex);
		}
		else { // if the previous cluster is of the same size as threshold, delete all the clusters smaller than threshold, and find the best candidates for delete among the clusters of the threshold size
			documentsToReassign = deleteDocumentClustersBySizeThreshold(thresholdSizeForDelete-1, clusterSizeIndex);
			while (documentsByCategory.size() > m_topKclusters){
				documentsToReassign.addAll(deleteWorstCluster(clusterSizeIndex.get(thresholdSizeForDelete)));
			}
		}
		return documentsToReassign;
	}

	private List<Integer> deleteDocumentClustersBySizeThreshold(int threshold, Map<Integer,List<String>> clusterSizeIndex){
		List<Integer> documentsToReassign = new LinkedList<Integer>();
		for (Integer size : clusterSizeIndex.keySet()){
			if (size <= threshold){
				for (String clusterLabel : clusterSizeIndex.get(size)){
					documentsToReassign.addAll(deleteDocumentCluster(clusterLabel));
				}
			}
		}
		return documentsToReassign;
	}
	
	private List<Integer> deleteDocumentCluster(String clusterLabel){
		List<Integer> documentsToReassign = new LinkedList<Integer>();
		if (documentsByCategory.containsKey(clusterLabel)){
			documentsToReassign.addAll(documentsByCategory.get(clusterLabel).keySet());
			documentsByCategory.remove(clusterLabel);
		}	
	//	System.out.println("Cluster "+clusterLabel+" deleted. Need to reassign "+documentsToReassign.size()+" documents");
		return documentsToReassign;		
	}
	
	private List<Integer> deleteWorstCluster(List<String> clusterLabels){
		String clusterToDelete = "";
		Double worstScore = 1.0; //scores are cosine values
		for (String clusterLabel : clusterLabels){
			Double score = getClusterConfidenceScore(clusterLabel);
			if (score == null) continue; // if such cluster does not exist any more
			if (score > 0){
				if (score < worstScore){
					clusterToDelete = clusterLabel;
					worstScore = score;
				}
			}
			else return deleteDocumentCluster(clusterLabel); // if found a cluster with zero score - delete it, no need to continue the search
		}		
		return deleteDocumentCluster(clusterToDelete);			
	}

	/** Returns average of cluster assignment scores or null if the cluster does not exist
	 * @param clusterLabel
	 * @return
	 */
	private Double getClusterConfidenceScore(String clusterLabel){
		Double score = null;
		if (documentsByCategory.containsKey(clusterLabel)){
			if (documentsByCategory.get(clusterLabel).isEmpty()) return 0.0;
			score = 0.0;
			for (int docId : documentsByCategory.get(clusterLabel).keySet()){
				score += documentsByCategory.get(clusterLabel).get(docId);
			}
			score /= documentsByCategory.get(clusterLabel).size();
		}
		return score;
	}

}

package eu.excitementproject.clustering.clustering.impl.tc;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;



import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.DocumentsToVectorsConverter;
import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

public class DocumentByTopKCategoriesClustererWThreshold extends DocumentByCategoryClusterer{

	int m_topKclusters = 10;
	double m_threshold = 0.7;
	
	@Override
	public void setNumberOfDocumentClusters(int K) {
		m_topKclusters = K;		
	}

	
	public DocumentByTopKCategoriesClustererWThreshold(TextCollection textCollection, double threshold, 
			Map<String, List<String>> termClustersAsCategories, WeightType docWeightType,
			WeightType catWeightType) {

		super(textCollection, termClustersAsCategories, docWeightType,
				catWeightType);
		m_threshold = threshold;
	}
	
	@Override
	public Map<String, List<Integer>> clusterDocuments(TextCollection textCollection) throws ClusteringException {
		Set<VectorRepresentation> documents = DocumentsToVectorsConverter.convertDocumentsToDenseTermVectors(textCollection, m_useExpandedCollection, m_docWeightType);
		Set<VectorRepresentation> categories = convertCategoriesToVectors(textCollection);
		for (VectorRepresentation document : documents){
			assignDocumentToRelevantCategories(document, categories);
			// assignDocumentToBestCategory(document, categories);
		}
		System.out.println("Done with first assignment");
		
		// retain only topK biggest categories after the first assignment, 
		// and use DocumentByCategoryClusterer to reassign documents to the selected top-K categories  
		if (documentsByCategory.size() > m_topKclusters){
			DocumentByCategoryClusterer step2Clusterer = new DocumentByCategoryClusterer
					(textCollection, getTopKBiggestCategories(), m_docWeightType, m_catWeightType);
			return step2Clusterer.clusterDocuments(textCollection);
		}				
		else return getDocumentClusters();
	}


	private Map<String, List<String>> getTopKBiggestCategories(){	
		
		LinkedList<Integer> clusterSizes = new LinkedList<Integer>();
		for (String categoryLablel: documentsByCategory.keySet()){
			clusterSizes.add(documentsByCategory.get(categoryLablel).size());
		}
		Comparator<Integer> comparator = Collections.reverseOrder();
		Collections.sort(clusterSizes,comparator);
		
		int thresholdSizeForDelete = clusterSizes.get(m_topKclusters); // this is the size of the first candidate for delete, which is the K-th element of the sorted list of cluster sizes
		List<String> categoriesToRetain = new LinkedList<String>();
		for (String categoryLablel: documentsByCategory.keySet()){
			if (documentsByCategory.get(categoryLablel).size() >= thresholdSizeForDelete){
				categoriesToRetain.add(categoryLablel);				
			}
		}
		
		Map<String, List<String>> topKTermClustersAsCategories = new HashMap<String, List<String>>();
		for (Integer catId : m_termClustersAsCategories.keySet()){
			String label = getCategoryLabel(m_termClustersAsCategories.get(catId));
			if (categoriesToRetain.contains(label)){
				topKTermClustersAsCategories.put(label, m_termClustersAsCategories.get(catId));
			}
		}
		
		System.out.println("Categories to retain after first assignment: ["+String.valueOf(categoriesToRetain.size())+"] "+categoriesToRetain);
		return topKTermClustersAsCategories;
	}
	
	
	/** Assigns each document to all categories for which similarity(doc, cat) > m_threshold
	 * @param document
	 * @param categories
	 */
	private void assignDocumentToRelevantCategories(VectorRepresentation document, Set<VectorRepresentation> categories){
		boolean assigned = false;
		for (VectorRepresentation category : categories){
			double score = SimilarityAndRelatednessCalculator.cosineSimilarity(category.getVector(), document.getVector());
			if (score > m_threshold) {  
				String label = getCategoryLabel(m_termClustersAsCategories.get(category.getId()));
				addDocumentToCategory(label, document.getId(), score);
				assigned=true;
			}			
		}
		if (!assigned) assignDocumentToBestCategory(document, categories);
	}	

}

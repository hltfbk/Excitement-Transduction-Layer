package eu.excitementproject.clustering.clustering.impl.tc;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

public class DocumentByTopKCategoriesTwoStepsClusterer extends DocumentByCategoryClusterer{

	int m_topKclusters = 10;
	double m_threshold = 0.7;
	
	
	@Override
	public void setNumberOfDocumentClusters(int K) {
		m_topKclusters = K;		
	}

	
	public DocumentByTopKCategoriesTwoStepsClusterer(TextCollection textCollection, double threshold, 
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
			assignDocumentToBestCategory(document, categories);
		}
		System.out.println("Done with first assignment");
		
		// retain only topK biggest categories after the first assignment, 
		// and use DocumentByCategoryClusterer to reassign documents to the selected top-K categories  
		if (documentsByCategory.size() > m_topKclusters){
				
			Map<String, List<String>> topKCategories = getTopKBiggestCategories();
			Set<Integer> unclassifiedDocIds = new HashSet<Integer>();
			for (String catLabel : topKCategories.keySet()){
				if (documentsByCategory.containsKey(catLabel)){
					unclassifiedDocIds.addAll(documentsByCategory.get(catLabel).keySet());
					documentsByCategory.remove(catLabel);
				}
			}
			
			Set<Integer> categoriesToRetain = new HashSet<Integer>();
			for (Integer catId: m_termClustersAsCategories.keySet()){
				String label = getCategoryLabel(m_termClustersAsCategories.get(catId));
				if (topKCategories.containsKey(label)) {
					categoriesToRetain.add(catId);
				}
			}
			
			Set<VectorRepresentation> newCategories = new HashSet<VectorRepresentation>();
			for (VectorRepresentation category : categories){
				if (categoriesToRetain.contains(category.getId())) newCategories.add(category);
			}
			
			System.out.println("Number of categories for 2nd round:"+String.valueOf(newCategories.size()));
			for (VectorRepresentation document : documents){
				if (unclassifiedDocIds.contains(document.getId())) assignDocumentToBestCategory(document, newCategories);
			}

			
			//return step2Clusterer.clusterDocuments(textCollection);
		}	
		
		
		
		
		
		return getDocumentClusters();
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
			if (documentsByCategory.get(categoryLablel).size() > thresholdSizeForDelete){
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
	
	

}

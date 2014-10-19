package eu.excitementproject.clustering.legacy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.DocumentsToVectorsConverter;
import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/** The clusterer assigns documents to term clusters, treating them as categories in unsupervised TC setting.
 *  The number of document clusters returned will be the same as the number of provided term clusters (categories) 
 * @author Lili
 *
 */
public class LegacyDocumentByCategoryClustererUnsorted implements DocumentClusterer{

	protected Map<Integer, List<String>> m_termClustersAsCategories= new HashMap<Integer, List<String>>();
	protected Map<String, Map<Integer,Double>> documentsByCategory = new HashMap<String, Map<Integer,Double>>();
	protected final boolean m_useExpandedCollection = false; // always false, cause we want to assign back the original, non-expanded texts 
	protected WeightType m_docWeightType;
	protected WeightType m_catWeightType;
	
	@Override
	public Map<String, List<Integer>> clusterDocuments(TextCollection textCollection) throws ClusteringException {
		Set<VectorRepresentation> documents = DocumentsToVectorsConverter.convertDocumentsToDenseTermVectors(textCollection, m_useExpandedCollection, m_docWeightType);
		Set<VectorRepresentation> categories = convertCategoriesToVectors(textCollection);
		for (VectorRepresentation document : documents){
		//	System.out.println(textCollection.getDocTextsByDocId().get(document.getId()));
			assignDocumentToBestCategory(document, categories);
		}
		
		return getDocumentClusters();
	}

	@Deprecated
	public void setNumberOfDocumentClusters(int K) {				
	}

	protected String getCategoryLabel(List<String> categoryTerms){
		return categoryTerms.toString();
	}
	
	public LegacyDocumentByCategoryClustererUnsorted(Map<String, List<String>> termClustersAsCategories, WeightType docWeightType, WeightType catWeightType) {
		int catId=1;
		for (String catName : termClustersAsCategories.keySet()){
			// change category names to integer ids
			m_termClustersAsCategories.put(catId, termClustersAsCategories.get(catName));
			// initialize to-be document clusters, one per category
			documentsByCategory.put(getCategoryLabel(termClustersAsCategories.get(catName)), new HashMap<Integer,Double>());
			catId++;
		}
		m_docWeightType = docWeightType;
		m_catWeightType = catWeightType;
		
		// remove non-class category, if present 
		if (m_termClustersAsCategories.containsKey(NON_CLUSTER_LABEL)) m_termClustersAsCategories.remove(NON_CLUSTER_LABEL);

	}
	
	public Set<VectorRepresentation> convertCategoriesToVectors(TextCollection textCollection) throws ClusteringException{
		Set<VectorRepresentation> categories = new HashSet<VectorRepresentation>();
		for (Integer catId : m_termClustersAsCategories.keySet()){			
			Map<String,Double> vector = new HashMap<String, Double>();
			for (String term : m_termClustersAsCategories.get(catId)){
				Double idf = 0.0; 
				if (m_useExpandedCollection){ // if expanded documents/categories should be used (never true in current version, but just for future use)
					idf = textCollection.getInvertedDocumentFrequencyAfterExpansion(term);
				}
				else{
					idf = textCollection.getInvertedDocumentFrequencyBeforeExpansion(term);
				}
				
				if (idf!=null) { // if the term occurred in the collection (original or expanded accordingly)
					if (m_catWeightType.equals(WeightType.BINARY)) vector.put(term, 1.0);
					else if (m_catWeightType.equals(WeightType.DF)) vector.put(term, 1.0/idf);
					else throw new ClusteringException("Invalid value "+m_catWeightType+" of WeightType is given. Only BINARY and DOCUMENT_FREQUENCY weight types are allowed for weighting category terms in DocumentByCategoryClusterer.");
				}
			}				
			categories.add(new VectorRepresentation(catId, vector));
		}
		return categories;
	}
	
	protected void assignDocumentToBestCategory(VectorRepresentation document, Set<VectorRepresentation> categories){
		String winningCategorylabel=NON_CLUSTER_LABEL; // if none of the categories will have a score >0, the doc will be assigned to the NON_CLUSTER_LABEL category 
		double winnerScore = 0.0;
//		Map<String,Double> cat = null;
		for (VectorRepresentation category : categories){
			double score = SimilarityAndRelatednessCalculator.cosineSimilarity(category.getVector(), document.getVector());
//			double score = SimilarityAndRelatednessCalculator.semiNormalizedCosineSimilarity(document.getVector(),category.getVector()); // does not normalize by category-vector size
	//		if (score>0) System.out.println("\t"+score+"\t"+category);
			if (score > winnerScore) { // note that if more than one categories result in the same score, the document will be assigned only to one of them 
				// can use ">=" instead of ">" to ensure every document is assigned to some category, even if it has 0 score with all if them (which should not happen normally)
				winnerScore = score;
				winningCategorylabel = getCategoryLabel(m_termClustersAsCategories.get(category.getId()));
	//			cat = category.getVector();
			}
		}

		addDocumentToCategory(winningCategorylabel, document.getId(), winnerScore);
	//	System.out.println(">>>>assigned to " +winningCategorylabel.toUpperCase()+" ("+winnerScore+") :: "+cat);
	}

	private void addDocumentToCategory(String categoryLabel, Integer documentId, Double score){
		// add only if adding to one of the valid categories (documentClusters keys)
		if (!documentsByCategory.containsKey(NON_CLUSTER_LABEL)) documentsByCategory.put(NON_CLUSTER_LABEL, new HashMap<Integer,Double>());
		if (documentsByCategory.containsKey(categoryLabel)){
			Map<Integer,Double> documentsInCategory = documentsByCategory.get(categoryLabel);
			documentsInCategory.put(documentId, score);		
			documentsByCategory.put(categoryLabel, documentsInCategory);
		}
	}
	
	protected Map<String, List<Integer>> getDocumentClusters(){
		Map<String, List<Integer>> clusters = new HashMap<String, List<Integer>>();
		for (String clusterLabel : documentsByCategory.keySet()){
			if (documentsByCategory.get(clusterLabel).isEmpty()) continue; // do not add empty clusters
			clusters.put(clusterLabel, new LinkedList<Integer>(documentsByCategory.get(clusterLabel).keySet()));
			//TODO: return documents ordered by score desc 
		}
		return clusters;
	}
	
	
	
}

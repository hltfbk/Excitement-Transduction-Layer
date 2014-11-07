package eu.excitementproject.clustering.data.api;

import java.util.List;
import java.util.Map;

import eu.excitementproject.eop.common.datastructures.PairMap;

/**
 * @author Lili Kotlerman
 *
 */
public interface TextCollection {

	/**
	 * @return A map of document ids (Integer) corresponding to each given text (String). <p> 
	 * The assumption is that there may be more than one document with identical text.
	 */
	public Map<String, ? extends List<Integer>> getDocIdsByText();

	/**
	 * @return A mapping of document id (Integer) to the corresponding document's text (String).
	 */
	public Map<Integer, String> getDocTextsByDocId();

	/**
	 * @return A mapping with a list of gold-standard cluster names (String) corresponding to each document, represented by its id (Integer)
	 */
	public Map<Integer, ? extends List<String>> getGoldStandardClustersPerDocId();

	/**
	 * @return A mapping with a list of document ids (Integer) corresponding to each cluster name (String) in the gold-standard
	 */
	public Map<String, ? extends List<Integer>> getDocIdsPerGoldStandardCluster();

	/**
	 * @return A mapping which for each term (String) contains a map of document id (Integer) and the frequency (Integer) of the given term in the given document. <p>
	 * The terms considered are only the terms occurring in the original document texts (without expansion)  
	 */
	public Map<String, Map<Integer, Double>> getDocIdsByOriginalTerm();

	/**
	 * @return A mapping which for each expansion term (String) contains a map of document id (Integer) and the confidence (Double) of the given expansion term for the given document. <p>
	 * (The documents are expanded with additional terms - expansion terms).
	 */
	public Map<String, Map<Integer, Double>> getDocIdsByExpansionTerm();
	
	public Map<Integer, Map<String, Double>> getOriginalTermsByDocId();
	
	public Map<Integer, Map<String, Double>> getExpansionTermsByDocId();
		
	public PairMap<String, Double> getTermSemanticRelatednessMap();
	
	public Double getOverallTermFrequencyBeforeExpansion(String term);
	
	public Double getOverallTermFrequencyAfterExpansion(String term);
	
	public Double getTermFrequencyInDocumentAfterExpansion(String term, Integer docId);

	public Double getTermFrequencyInDocumentBeforeExpansion(String term, Integer docId);

	public Map<String, Double> getAllDocumentTermsAfterExpansion(Integer docId);

	public Double getInvertedDocumentFrequencyBeforeExpansion(String term);
	
	public Double getInvertedDocumentFrequencyAfterExpansion(String term);
		
	public void loadNewCollection();

	public void expandCollection(LexicalExpander LE);
	
	public String getDatasetName();

	public String getInputFilename();

	public void setDomainVocabulary(String filename);
	
	/**
	 * @param word - a word (or ngram)
	 * @return true if the word is a stopword, false otherwise. If no stopwords are defined, return false
	 */
	public boolean isStopword(String word);

}
package eu.excitementproject.clustering.clustering.impl.lda;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.datastructures.Pair;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.lda.demo.ContextTester;
import eu.excitementproject.lda.demo.DocumentContextTester;

/**
 * @author Lili Kotlerman
 * Only applicable to expanded collection. Only classifies using local model
 *
 */
public abstract class AbstractTermByRelatednessLdaClusterer implements TermClusterer {

	protected int m_topKclusters = 10;
	int m_numOfTopics;
	protected DocumentContextTester m_ldaTester;
	String m_conf;
	
	public void init(boolean useExpandedCollection, TextCollection textCollection, String conf, int numOfTopics, WeightType weightType) throws ClusteringException{
		m_conf = conf;
		m_numOfTopics = numOfTopics;
		try {
			ConfigurationFile cf = new ConfigurationFile(conf);
			ConfigurationParams cp = cf.getModuleConfiguration("context-tester");
			String textCollectionFile = cp.getString("extraction-documents-file");
			// prepare text collection as input to LDA 
			System.out.println("Preparing input for LDA...");
			prepareMalletInput(textCollection, textCollectionFile, weightType);		
		} catch (Exception e) {
			throw new ClusteringException(m_conf+"Cannot initialize LDA document clusterer.\n"+e);
		}		
	}
	
	
	public AbstractTermByRelatednessLdaClusterer(boolean useExpandedCollection, TextCollection textCollection, String conf, int numOfTopics, WeightType weightType) throws ClusteringException{
		init(useExpandedCollection, textCollection, conf, numOfTopics, weightType);
		try {
			ConfigurationFile cf = new ConfigurationFile(conf);
			// train a local LDA model over the given textCollection
			System.out.println("Training LDA model...");
			ContextTester.trainLda(cf, m_numOfTopics);
			System.out.println("Done. Saving top words per topic...");
			ContextTester.printTopWords(cf, m_numOfTopics);
		} catch (Exception e) {
			throw new ClusteringException(m_conf+"Cannot create LDA document clusterer.\n"+e);
		}		
	}


	@Override
	public Map<String, List<String>> clusterTerms (
			TextCollection textCollection) throws ClusteringException {
		
		// if no external model file is given
		try {		
			// cluster the collection using the resulting model
			m_ldaTester = new DocumentContextTester(m_conf);			
			return clusterPseudoDocuments(textCollection);
		} catch (Exception e) {
			throw new ClusteringException(e.getMessage());
		}
	}
	
	@Override
	public void setNumberOfTermClusters(int K) {
		m_topKclusters = K;		
	}

	public abstract Map<String, List<String>> clusterPseudoDocuments(TextCollection textCollection) throws Exception;
	
	/** Convert presudoDocString from lda to topic distribution  
	 * @param term
	 * @param pseudoDocString - in the following format
	 * docLength \t topicId::topicProb \t topicId::topicProb ...  
	 * 63	47::0.5493150949478149	74::0.31643834710121155
	 * @return
	 */
	public static Map<String, Double> getTopicDistributionOfTerm(String pseudoDocString){
		Map<String, Double> topicDistribution = new HashMap<String, Double>();
		for (String s : pseudoDocString.split("\t")){
			if (s.contains("::")){
				String topicId = s.split("::")[0];
				Double topicProb = Double.valueOf(s.split("::")[1]);
				topicDistribution.put(topicId, topicProb);
			}			
		}
		return topicDistribution;
	}
	
	public void prepareMalletInput(TextCollection textCollection, String filename, WeightType weightType) throws Exception{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename))); 
		for (String origTerm : textCollection.getDocIdsByOriginalTerm().keySet()){
			writer.write(prepareMalletPseudoDocumentForTerm(textCollection, origTerm, weightType));
		}
		writer.close();
	}

	public String prepareMalletPseudoDocumentForTerm(TextCollection textCollection, String term, WeightType weightType){
		String docString = "["+term+"]\tD";
		
		Set<String> terms = new HashSet<String>();
		
		if (textCollection.getTermSemanticRelatednessMap().getPairContaining(term)!=null){
			for (Pair<String> termPair : textCollection.getTermSemanticRelatednessMap().getPairContaining(term)){
				terms.addAll(termPair.toSet());
			}			
		}
		else return docString+="\t["+term+"]\n"; // no info on related terms => the term represents itself

		for (String t : terms){
			Double weight = SimilarityAndRelatednessCalculator.semanticRelatedness(t, term, textCollection.getTermSemanticRelatednessMap()); 
			for (int i=0; i<=weight; i++) docString+="\t["+t+"]";
		}
		return docString+"\n";
	}
	
	/**
	 * @param topicDistribution - a full vector representation produced by LDA (vector entries are topics, entry scores are topic prob values for the given document)
	 * @return vector where a single feature is present - the topic with the best prob score from the input vector
	 */
	public static Map<String,Double> getBestTopicForTerm(Map<String, Double> topicDistribution){
		Map<String,Double> res = new HashMap<String, Double>();
		double bestProb = 0.0;
		String bestTopic = null;
		for (String topic : topicDistribution.keySet()){
			if (topicDistribution.get(topic) > bestProb){
				bestProb = topicDistribution.get(topic);
				bestTopic = topic;		
			}
		}
		if (bestTopic!=null) res.put(bestTopic, bestProb);
		return res;
	}	
}

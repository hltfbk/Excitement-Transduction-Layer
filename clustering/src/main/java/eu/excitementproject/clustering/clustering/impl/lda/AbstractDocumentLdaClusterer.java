package eu.excitementproject.clustering.clustering.impl.lda;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.lda.demo.ContextTester;
import eu.excitementproject.lda.demo.DocumentContextTester;

/**
 * @author Lili Kotlerman
 *
 */
public abstract class AbstractDocumentLdaClusterer implements DocumentClusterer {

	boolean m_useExpandedCollection;
	protected int m_topKclusters = 10;
	int m_numOfTopics;
	String m_externalModelFilename = null;
	protected DocumentContextTester m_ldaTester;
	String m_conf;
	
	public void init(boolean useExpandedCollection, TextCollection textCollection, String conf, int numOfTopics, WeightType weightType) throws ClusteringException{
		m_useExpandedCollection = useExpandedCollection;
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
	
	
	public AbstractDocumentLdaClusterer(boolean useExpandedCollection, TextCollection textCollection, String conf, int numOfTopics, WeightType weightType) throws ClusteringException{
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

	public AbstractDocumentLdaClusterer(boolean useExpandedCollection, TextCollection textCollection, String conf, int numOfTopics, WeightType weightType, String externalSerializedLdaModelFilename) throws ClusteringException {	
		m_externalModelFilename = externalSerializedLdaModelFilename;
		init(useExpandedCollection, textCollection, conf, numOfTopics, weightType);
	}

	@Override
	public Map<String, List<Integer>> clusterDocuments (
			TextCollection textCollection) throws ClusteringException {
		
		// if no external model file is given
		if (m_externalModelFilename==null) {
			try {		
				// cluster the collection using the resulting model
				m_ldaTester = new DocumentContextTester(m_conf);			
				return clusterDocumentsByLocalModel(textCollection);
			} catch (Exception e) {
				throw new ClusteringException(e.getMessage());
			}
		}
		// if need to use an external model file
		else {
			try {
				m_ldaTester = new DocumentContextTester(m_conf);	
				return clusterDocumentsByExternallModel(textCollection,  m_externalModelFilename);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ClusteringException("Cannot cluster with external model "+ m_externalModelFilename+"\n"+e);
			}
		}
	}
	

	public abstract Map<String, List<Integer>> clusterDocumentsByExternallModel(
			TextCollection textCollection, String m_externalModelFilename2) throws Exception;


	@Override
	public void setNumberOfDocumentClusters(int K) {
		m_topKclusters = K;		
	}

	public abstract Map<String, List<Integer>> clusterDocumentsByLocalModel(TextCollection textCollection) throws Exception;
	
	/** Convert docString from lda to VectorRepresentation  
	 * @param docId
	 * @param docString - in the following format
	 * docLength \t topicId::topicProb \t topicId::topicProb ...  
	 * 63	47::0.5493150949478149	74::0.31643834710121155
	 * @return
	 */
	public static VectorRepresentation parseDoc2TopicsString(String docId, String docString){
		Map<String, Double> vector = new HashMap<String, Double>();
		for (String s : docString.split("\t")){
			if (s.contains("::")){
				String topicId = s.split("::")[0];
				Double topicProb = Double.valueOf(s.split("::")[1]);
				vector.put(topicId, topicProb);
			}			
		}
		return new VectorRepresentation(Integer.valueOf(docId), vector);
	}
	
	public void prepareMalletInput(TextCollection textCollection, String filename, WeightType weightType) throws Exception{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename))); 
		for (Integer docId : textCollection.getDocTextsByDocId().keySet()){
			writer.write(prepareMalletInputForDocument(textCollection, docId, weightType));
		}
		writer.close();
	}

	public String prepareMalletInputForDocument(TextCollection textCollection, Integer docId, WeightType weightType){
		String docString = "["+String.valueOf(docId)+"]\tD";
		Set<String> terms = new HashSet<String>(textCollection.getOriginalTermsByDocId().get(docId).keySet());
		if (m_useExpandedCollection){
			if (textCollection.getExpansionTermsByDocId().containsKey(docId)) {
				terms.addAll(textCollection.getExpansionTermsByDocId().get(docId).keySet());
			}
		}	
		for (String term : terms){
			Double weight = WeightCalculator.getTermWeightInDocument(textCollection, m_useExpandedCollection, docId, term, weightType);
			for (int i=0; i<=weight; i++) docString+="\t["+term+"]";
		}
		return docString+"\n";
	}
	
	/**
	 * @param vector - a full vector representation produced by LDA (vector entries are topics, entry scores are topic prob values for the given document)
	 * @return vector where a single feature is present - the topic with the best prob score from the input vector
	 */
	public static VectorRepresentation getBestTopicVectorRepresentation(VectorRepresentation vector){
		Map<String,Double> res = new HashMap<String, Double>();
		double bestProb = 0.0;
		String bestTopic = null;
		for (String topic : vector.getVector().keySet()){
			if (vector.getVector().get(topic) > bestProb){
				bestProb = vector.getVector().get(topic);
				bestTopic = topic;		
			}
		}
		if (bestTopic!=null) res.put(bestTopic, bestProb);
		return new VectorRepresentation(vector.getId(), res);
	}	
}

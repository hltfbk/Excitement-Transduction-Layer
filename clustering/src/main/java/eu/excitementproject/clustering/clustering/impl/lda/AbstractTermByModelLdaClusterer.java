package eu.excitementproject.clustering.clustering.impl.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.Clusterer;
import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;

/**
 * @author Lili Kotlerman
 * Classifies using term-topic probabilities of an external model. Applicable to extended and non-extended collections, but clusters only original terms
 *
 */
public abstract class AbstractTermByModelLdaClusterer implements TermClusterer {

	String m_conf;
	File m_topicProbFile;
	
		
	public AbstractTermByModelLdaClusterer(TextCollection textCollection, String conf) throws ClusteringException{
		m_conf = conf;
		try {
			ConfigurationFile cf = new ConfigurationFile(conf);
			ConfigurationParams cp = cf.getModuleConfiguration("lda-term-clustering");
			m_topicProbFile = new File(cp.getString("term-prob-file"));
			// prepare text collection as input to LDA 
			System.out.println("Preparing input for LDA...");
		} catch (Exception e) {
			throw new ClusteringException(m_conf+"Cannot initialize LDA document clusterer.\n"+e);
		}		
	}

	public AbstractTermByModelLdaClusterer(TextCollection textCollection, String conf, File modelFile) throws ClusteringException{
		m_conf = conf;
		try {
			m_topicProbFile = modelFile;
			// prepare text collection as input to LDA 
			System.out.println("Preparing input for term LDA clusterer...");
		} catch (Exception e) {
			throw new ClusteringException(m_conf+"Cannot initialize LDA document clusterer.\n"+e);
		}		
	}

	

	@Override
	public Map<String, List<String>> clusterTerms (
			TextCollection textCollection) throws ClusteringException{
		return getTermClusters(textCollection.getDocIdsByOriginalTerm().keySet());
	}
	
	public abstract Map<String, List<String>> getTermClusters(Set<String> termsToCluster);
	
	@Override @Deprecated
	public void setNumberOfTermClusters(int K) {
				
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
	
	/**
	 * Read the file with term-topic probabilities and extract topic distributions of each of the terms of interest
	 * @throws IOException 
	 */
	public Map<String,Map<String,Double>> getTopicsForTerms(Set<String> termsToCluster) throws IOException{
		Map<String,Map<String,Double>> res = new HashMap<String,Map<String,Double>>();
		BufferedReader r = new BufferedReader(new FileReader(m_topicProbFile));
		String line = r.readLine(); // title line
		line = r.readLine();
		while(line!=null){
			String[] s = line.split("\t");
			String term = s[0].replace("[","").replace("]","");
			String topic = s[1];
			Double prob = Double.valueOf(s[2]);
			if (termsToCluster.contains(term)){
				Map<String,Double> termTopics = new HashMap<String,Double>();
				if (res.containsKey(term)) termTopics = res.get(term);
				termTopics.put(topic, prob);
				res.put(term, termTopics);
			}
			line = r.readLine();
		}
		r.close();
		
		// add non-class cluster with terms not found in the model
		for (String term : termsToCluster){
			if (!res.containsKey(term)) {
				Map<String,Double> termTopics = new HashMap<String,Double>();
				termTopics.put(Clusterer.NON_CLUSTER_LABEL, 1.0);
				res.put(term, termTopics);
			}
		}
		return res;		
	}	
	
}

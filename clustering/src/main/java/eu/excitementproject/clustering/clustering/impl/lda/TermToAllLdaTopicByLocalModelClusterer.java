package eu.excitementproject.clustering.clustering.impl.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.Clusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.data.api.TextCollection;

public class TermToAllLdaTopicByLocalModelClusterer extends
						TermToAllLdaTopicsByModelClusterer  {

	public TermToAllLdaTopicByLocalModelClusterer(TextCollection textCollection, String conf, File localModelFile) throws ClusteringException{
		super(textCollection, conf, localModelFile);
	}

	@Override
	public Map<String,Map<String,Double>> getTopicsForTerms(Set<String> termsToCluster) throws IOException{
		Map<String,Map<String,Double>> res = new HashMap<String,Map<String,Double>>();
		BufferedReader r = new BufferedReader(new FileReader(m_topicProbFile));
		String topic = null;
		String line = r.readLine(); // title line
		line = r.readLine();
		while(line!=null){
			String[] s = line.split("\t");
			if (s.length != 2) {
				if (!line.contains("-----")) topic = line.replace("\n", "");
				line = r.readLine();				
				continue;
			}
			String term = s[0].replace("[","").replace("]","");
			Double prob = Double.valueOf(s[1]);
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

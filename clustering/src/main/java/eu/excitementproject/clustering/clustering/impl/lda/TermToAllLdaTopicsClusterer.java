package eu.excitementproject.clustering.clustering.impl.lda;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.rep.ClusterMember;
import eu.excitementproject.clustering.clustering.impl.util.rep.StringsPatrition;
import eu.excitementproject.clustering.data.api.TextCollection;

public class TermToAllLdaTopicsClusterer extends
		AbstractTermLdaClusterer {

	public TermToAllLdaTopicsClusterer(TextCollection textCollection, String conf)
			throws ClusteringException {
		super(textCollection, conf);
		// TODO Auto-generated constructor stub
	}

	public TermToAllLdaTopicsClusterer(TextCollection textCollection,
			String conf, File localModelFile) throws ClusteringException {
		super(textCollection, conf, localModelFile);
	}

	@Override
	public Map<String, List<String>> getTermClusters(Set<String> termsToCluster) {
		Map<String, List<String>> res = new HashMap<String, List<String>>();
		Map<String, List<ClusterMember<String>>> resWithScores = new HashMap<String, List<ClusterMember<String>>>();
		
		try {
			Map<String, Map<String,Double>> topicDistributions = getTopicsForTerms(termsToCluster);
			for (String term : topicDistributions.keySet()){
				Map<String,Double> topicsOfTerm = topicDistributions.get(term);
				for (String topic : topicsOfTerm.keySet()){ 
					List<ClusterMember<String>> membersOfTopic = new LinkedList<ClusterMember<String>>();
					if (resWithScores.containsKey(topic)) membersOfTopic = resWithScores.get(topic);
					membersOfTopic.add(new ClusterMember<String>(term,topicsOfTerm.get(topic)));
					resWithScores.put(topic, membersOfTopic);
				}
			}
		} catch (IOException e) {			
			e.printStackTrace();
			return res;
		}
		
		for (String topicId : resWithScores.keySet()){
			List<String> sorted = StringsPatrition.getSortedByScoreCluster(resWithScores.get(topicId));
			res.put(sorted.toString(), sorted);
		}
		return res;

	}

}

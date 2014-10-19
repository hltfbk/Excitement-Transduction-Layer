package eu.excitementproject.clustering.legacy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.lda.AbstractTermByRelatednessLdaClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

public class LegacyTermToBestLdaTopicClustererUnsorted extends AbstractTermByRelatednessLdaClusterer {

	public LegacyTermToBestLdaTopicClustererUnsorted(boolean useExpandedCollection,
			TextCollection textCollection, String conf, int numOfTopics,
			WeightType weightType) throws ClusteringException {
		super(useExpandedCollection, textCollection, conf, numOfTopics, weightType);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, List<String>> clusterPseudoDocuments(TextCollection textCollection) throws Exception {			
		
		// assumes the number of topics in the model is K
		// i.e. the number of resulting clusters will be the same as the number of topics in the model
		
		Map<String,List<String>> res = new HashMap<String, List<String>>();
		Map<String,String> pseudoDocs = m_ldaTester.getDocumentTopicProbabilities(); 
		for (String term : pseudoDocs.keySet()){			
			Map<String, Double> v = getBestTopicForTerm(getTopicDistributionOfTerm(pseudoDocs.get(term)));
			for (String topicId : v.keySet()){
				// the distribution contains only one entry - the best topic for the current pseudo-document with its prob score
				List<String> termsInTopic = new LinkedList<String>();
				if (res.containsKey(topicId)) termsInTopic = res.get(topicId);				
				termsInTopic.add(term); // add current term to the list of terms assigned to the current topic
				res.put(topicId, termsInTopic);
			}
		}	
		if (m_topKclusters!=res.size()) System.err.println("Cannot produce the requested K="+m_topKclusters+" term clusters, since the number of topics in the model is "+res.size()+" or some of the topics in the model did not 'win' for any term. Created "+res.size()+" term clusters.");
		return res;
	}	

}

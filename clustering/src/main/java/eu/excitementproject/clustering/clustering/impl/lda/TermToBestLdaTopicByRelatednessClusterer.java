package eu.excitementproject.clustering.clustering.impl.lda;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.clustering.impl.util.rep.ClusterMember;
import eu.excitementproject.clustering.clustering.impl.util.rep.StringsPatrition;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 *  Creates term clusters by creating a pseudo-document per term, holding the term and all its semantically related terms
 *  Local LDA model is created for the pseudo-documents, and each term (pseudo-document) is assigned to the topic with best prob for this pseudo-document 
 *    
 *  Cluster members are sorted (desc) by the corresponding topic probability 
 *
 * @author Lili Kotlerman
 *
 */
public class TermToBestLdaTopicByRelatednessClusterer extends AbstractTermByRelatednessLdaClusterer {

	public TermToBestLdaTopicByRelatednessClusterer(boolean useExpandedCollection,
			TextCollection textCollection, String conf, int numOfTopics,
			WeightType weightType) throws ClusteringException {
		super(useExpandedCollection, textCollection, conf, numOfTopics, weightType);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, List<String>> clusterPseudoDocuments(TextCollection textCollection) throws Exception {			
		
		// assumes the number of topics in the model is K
		// i.e. the number of resulting clusters will be the same as the number of topics in the model

		Map<String,List<ClusterMember<String>>> resWithScores = new HashMap<String, List<ClusterMember<String>>>();
		Map<String,String> pseudoDocs = m_ldaTester.getDocumentTopicProbabilities(); 
		for (String term : pseudoDocs.keySet()){			
			Map<String, Double> v = getBestTopicForTerm(getTopicDistributionOfTerm(pseudoDocs.get(term)));
			for (String topicId : v.keySet()){
				Double termScoreForTopic =v.get(topicId);
				List<ClusterMember<String>> termsInTopic = new LinkedList<ClusterMember<String>>();
				if (resWithScores.containsKey(topicId)) termsInTopic = resWithScores.get(topicId);				
				termsInTopic.add(new ClusterMember<String>(term, termScoreForTopic)); // add current term to the list of terms assigned to the current topic
				resWithScores.put(topicId, termsInTopic);
			}
		}	
		if (m_topKclusters!=resWithScores.size()) System.err.println("Cannot produce the requested K="+m_topKclusters+" term clusters, since the number of topics in the model is "+resWithScores.size()+" or some of the topics in the model did not 'win' for any term. Created "+resWithScores.size()+" term clusters.");

		Map<String,List<String>> res = new HashMap<String, List<String>>();
		for (String topicId : resWithScores.keySet()){
			List<String> sorted = StringsPatrition.getSortedByScoreCluster(resWithScores.get(topicId));
			res.put(sorted.toString(), sorted);
		}
		return res;


		
	}	

}

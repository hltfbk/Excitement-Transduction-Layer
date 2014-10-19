package eu.excitementproject.clustering.clustering.impl.lda;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.clustering.impl.util.rep.ClusterMember;
import eu.excitementproject.clustering.clustering.impl.util.rep.StringsPatrition;
import eu.excitementproject.clustering.data.api.TextCollection;

/** Assign each document directly to the LDA topic with the highest probability for this doc.
 * 
 *  Assumption: the number of topics in the LDA model is K.
	The number of resulting clusters will be the same as the number of topics in the model,
	or less if some of the topics did not 'win' (get highest probability) for any of the documents
 * 
 *  Cluster members are sorted (desc) by the corresponding topic probability 
 *
 * @author Lili Kotlerman
 *
 */
public class DocumentToBestLdaTopicClusterer extends AbstractDocumentLdaClusterer {
	
	public DocumentToBestLdaTopicClusterer(boolean useExpandedCollection, TextCollection textCollection, String conf, int numOfTopics, WeightType weightType) throws ClusteringException {
		super(useExpandedCollection, textCollection, conf, numOfTopics, weightType);
	}
	
	

	

	public Map<String, List<Integer>> clusterDocumentsByLocalModel(TextCollection textCollection) throws Exception {			
		
		// assumes the number of topics in the model is K
		// i.e. the number of resulting clusters will be the same as the number of topics in the model
		Map<String,String> docs = m_ldaTester.getDocumentTopicProbabilities(); 
		
		Map<String,List<ClusterMember<String>>> resWithScores = new HashMap<String,List<ClusterMember<String>>>();
		for (String docId : docs.keySet()){			
			VectorRepresentation v = getBestTopicVectorRepresentation(parseDoc2TopicsString(docId, docs.get(docId)));
			for (String topicId : v.getVector().keySet()){
				// the vector contains only one entry - the winning topic (with the highest score)
				Double docSoreForTopic = v.getVector().get(topicId);
				List<ClusterMember<String>> docsInTopic = new LinkedList<ClusterMember<String>>();
				if (resWithScores.containsKey(topicId)) docsInTopic = resWithScores.get(topicId);				
				docsInTopic.add(new ClusterMember<String>(v.getId().toString(), docSoreForTopic)); // add current doc id to the list of documents assigned to the current topic
				resWithScores.put(topicId, docsInTopic);
			}
		}	
		if (m_topKclusters!=resWithScores.size()) System.err.println("Cannot produce the requested K="+m_topKclusters+" text clusters, since the number of topics in the model is "+resWithScores.size()+" or some of the topics in the model did not 'win' for any document. Created "+resWithScores.size()+" text clusters.");

		Map<String,List<Integer>> res = new HashMap<String, List<Integer>>();
		for (String topicId : resWithScores.keySet()){
			List<String> sortedCluster = StringsPatrition.getSortedByScoreCluster(resWithScores.get(topicId));
			List<Integer> sortedIds = new LinkedList<Integer>();
			for (String docId : sortedCluster){
				sortedIds.add(Integer.valueOf(docId));
			}
			res.put(topicId, sortedIds);
		}
		return res;
	}
	
	public DocumentToBestLdaTopicClusterer(boolean useExpandedCollection,
			TextCollection textCollection, String conf, int numOfTopics,
			WeightType weightType, String externalSerializedLdaModelFilename)
			throws ClusteringException {
		super(useExpandedCollection, textCollection, conf, numOfTopics, weightType,
				externalSerializedLdaModelFilename);
		// TODO Auto-generated constructor stub
	}



	public Map<String, List<Integer>> clusterDocumentsByExternallModel(TextCollection textCollection, String modelFilename) throws Exception {			
		
		// assumes the number of topics in the model is K
		// i.e. the number of resulting clusters will be the same as the number of topics in the model
		Map<String,String> docs = m_ldaTester.getDocumentTopicProbabilities(modelFilename);
		
		// this is copy-paste from local model 
		Map<String,List<ClusterMember<String>>> resWithScores = new HashMap<String,List<ClusterMember<String>>>();
		for (String docId : docs.keySet()){			
			VectorRepresentation v = getBestTopicVectorRepresentation(parseDoc2TopicsString(docId, docs.get(docId)));
			for (String topicId : v.getVector().keySet()){
				// the vector contains only one entry - the winning topic (with the highest score)
				Double docSoreForTopic = v.getVector().get(topicId);
				List<ClusterMember<String>> docsInTopic = new LinkedList<ClusterMember<String>>();
				if (resWithScores.containsKey(topicId)) docsInTopic = resWithScores.get(topicId);				
				docsInTopic.add(new ClusterMember<String>(v.getId().toString(), docSoreForTopic)); // add current doc id to the list of documents assigned to the current topic
				resWithScores.put(topicId, docsInTopic);
			}
		}	
		if (m_topKclusters!=resWithScores.size()) System.err.println("Cannot produce the requested K="+m_topKclusters+" text clusters, since the number of topics in the model is "+resWithScores.size()+" or some of the topics in the model did not 'win' for any document. Created "+resWithScores.size()+" text clusters.");

		Map<String,List<Integer>> res = new HashMap<String, List<Integer>>();
		for (String topicId : resWithScores.keySet()){
			List<String> sortedCluster = StringsPatrition.getSortedByScoreCluster(resWithScores.get(topicId));
			List<Integer> sortedIds = new LinkedList<Integer>();
			for (String docId : sortedCluster){
				sortedIds.add(Integer.valueOf(docId));
			}
			res.put(topicId, sortedIds);
		}
		return res;
	}	
	
	
}

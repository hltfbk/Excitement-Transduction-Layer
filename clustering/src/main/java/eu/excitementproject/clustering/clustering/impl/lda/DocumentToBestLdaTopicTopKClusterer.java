package eu.excitementproject.clustering.clustering.impl.lda;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.clustering.impl.util.rep.ClusterMember;
import eu.excitementproject.clustering.clustering.impl.util.rep.StringsPatrition;
import eu.excitementproject.clustering.data.api.TextCollection;

/** Assign each document directly to the LDA topic with the highest probability for this doc.
 * 	If the resulting clustering contains more than K clusters, reassign the documents to top-K biggest topics from the first assignment 
 * 
 *  Assumption: the number of topics in the LDA model is >=  K.
 *	The number of resulting clusters will be: 
 *	K - normally
 *	K+1 - if some of the docs cannot be assigned to any of the top-K topics, it is assigned to the topic labeled "misc-unknown-cluster", which is the topic K+1
 *	less than K - if some of the top-K topics did not 'win' for any of the documents while reassigning
 * 
 *  Cluster members are sorted (desc) by the corresponding topic probability 
 *
 * @author Lili Kotlerman
 *
 */

 //TODO currently external model is loaded twice - need to fix for efficiency

public class DocumentToBestLdaTopicTopKClusterer extends DocumentToBestLdaTopicClusterer {
	
	public DocumentToBestLdaTopicTopKClusterer(boolean useExpandedCollection, TextCollection textCollection, String conf, int numOfTopics, WeightType weightType) throws ClusteringException {
		super(useExpandedCollection, textCollection, conf, numOfTopics, weightType);
	}
	
		
	public DocumentToBestLdaTopicTopKClusterer(boolean useExpandedCollection,
			TextCollection textCollection, String conf, int numOfTopics,
			WeightType weightType, String externalSerializedLdaModelFilename)
			throws ClusteringException {
		super(useExpandedCollection, textCollection, conf, numOfTopics, weightType,
				externalSerializedLdaModelFilename);
		// TODO Auto-generated constructor stub
	}



	public Map<String, List<Integer>> clusterDocumentsByExternallModel(TextCollection textCollection, String modelFilename) throws Exception {			
		
		// assume the number of topics in the model is >=  K, but need to return K clusters
		
		// assign each doc to its best topic according to the model
		Map<String,List<Integer>> firstAssignmentResults = super.clusterDocumentsByExternallModel(textCollection, modelFilename);

		// if resulting clustering has more than K clusters - reassign to top-K biggest topics
		if (firstAssignmentResults.size() > m_topKclusters){
			Map<String,String> docs = m_ldaTester.getDocumentTopicProbabilities(modelFilename);
			System.out.println(docs.size());

			Map<String,List<ClusterMember<String>>> resWithScores = new HashMap<String,List<ClusterMember<String>>>();
			for (String docId : docs.keySet()){			
				VectorRepresentation v = getBestTopicOutOfGivenVectorRepresentation(parseDoc2TopicsString(docId, docs.get(docId)), getTopKBiggestTopics(firstAssignmentResults));
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
		else return firstAssignmentResults;		
	}	

	private Set<String> getTopKBiggestTopics(Map<String, List<Integer>> firstAssignmentResults){		
		LinkedList<Integer> clusterSizes = new LinkedList<Integer>();
		for (String topicId: firstAssignmentResults.keySet()){
			clusterSizes.add(firstAssignmentResults.get(topicId).size());
		}
		Comparator<Integer> comparator = Collections.reverseOrder();
		Collections.sort(clusterSizes,comparator);
		
		int thresholdSizeForDelete = clusterSizes.get(m_topKclusters); // this is the size of the first candidate for delete, which is the K-th element of the sorted list of cluster sizes
		
		Set<String> topicsToRetain = new HashSet<String>();
		for (String topicId: firstAssignmentResults.keySet()){
			if (firstAssignmentResults.get(topicId).size() > thresholdSizeForDelete){
				topicsToRetain.add(topicId);				
			}
		}	
		return topicsToRetain;
	}

	/**
	 * @param vector - a full vector representation produced by LDA (vector entries are topics, entry scores are topic prob values for the given document)
	 * @return vector where a single feature is present - the topic with the best prob score from the input vector, out of the given set of topics
	 */
	public static VectorRepresentation getBestTopicOutOfGivenVectorRepresentation(VectorRepresentation vector, Set<String> topicIds){
		
		Map<String,Double> res = new HashMap<String, Double>();
		double bestProb = 0.0;
		String bestTopic = null;
		for (String topicId : vector.getVector().keySet()){
			if (!topicIds.contains(topicId)) continue; // skip topics that are not in the given set of topics
			if (vector.getVector().get(topicId) > bestProb){
				bestProb = vector.getVector().get(topicId);
				bestTopic = topicId;		
			}
		}
		if (bestTopic!=null) res.put(bestTopic, bestProb);
		else res.put(NON_CLUSTER_LABEL, 0.01); // assign the doc to the artificial topic with topic-id "misc-unknown-topic" with some low prob
		return new VectorRepresentation(vector.getId(), res);
	}	
	
}

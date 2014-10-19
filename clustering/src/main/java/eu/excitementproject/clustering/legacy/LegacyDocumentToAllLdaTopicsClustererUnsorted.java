package eu.excitementproject.clustering.legacy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.lda.AbstractDocumentLdaClusterer;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/** Assign each document directly to the LDA topic with the highest probability for this doc.
 * 
 *  Assumption: the number of topics in the LDA model is K.
	The number of resulting clusters will be the same as the number of topics in the model,
	or less if some of the topics did not 'win' (get highest probability) for any of the documents
 * @author Lili Kotlerman
 *
 */
public class LegacyDocumentToAllLdaTopicsClustererUnsorted extends AbstractDocumentLdaClusterer {
	
	public LegacyDocumentToAllLdaTopicsClustererUnsorted(boolean useExpandedCollection, TextCollection textCollection, String conf, int numOfTopics, WeightType weightType) throws ClusteringException {
		super(useExpandedCollection, textCollection, conf, numOfTopics, weightType);
	}
	
	

	

	public Map<String, List<Integer>> clusterDocumentsByLocalModel(TextCollection textCollection) throws Exception {			
		
		// assumes the number of topics in the model is K
		// i.e. the number of resulting clusters will be the same as the number of topics in the model
	
		Map<String,List<Integer>> res = new HashMap<String, List<Integer>>();
		Map<String,String> docs = m_ldaTester.getDocumentTopicProbabilities(); 
		for (String docId : docs.keySet()){			
			VectorRepresentation v = parseDoc2TopicsString(docId, docs.get(docId));
			for (String topicId : v.getVector().keySet()){
				// the vector contains only one entry - the best topic for the current document with its prob score
				List<Integer> docsInTopic = new LinkedList<Integer>();
				if (res.containsKey(topicId)) docsInTopic = res.get(topicId);				
				docsInTopic.add(v.getId()); // add current doc id to the list of documents assigned to the current topic
				res.put(topicId, docsInTopic);
			}
		}	
		if (m_topKclusters!=res.size()) System.err.println("Cannot produce the requested K="+m_topKclusters+" text clusters, since the number of topics in the model is "+res.size()+" or some of the topics in the model did not 'win' for any document. Created "+res.size()+" text clusters.");
		return res;
	}
	
	public LegacyDocumentToAllLdaTopicsClustererUnsorted(boolean useExpandedCollection,
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
		
		Map<String,List<Integer>> res = new HashMap<String, List<Integer>>();
		Map<String,String> docs = m_ldaTester.getDocumentTopicProbabilities(modelFilename);
		System.out.println(docs.size());
	
		// this is copy-paste from local model 
		for (String docId : docs.keySet()){			
			VectorRepresentation v = parseDoc2TopicsString(docId, docs.get(docId));
			for (String topicId : v.getVector().keySet()){
				// the vector contains only one entry - the best topic for the current document with its prob score
				List<Integer> docsInTopic = new LinkedList<Integer>();
				if (res.containsKey(topicId)) docsInTopic = res.get(topicId);				
				docsInTopic.add(v.getId()); // add current doc id to the list of documents assigned to the current topic
				res.put(topicId, docsInTopic);
			}
		}	
		if (m_topKclusters!=res.size()) System.err.println("Cannot produce the requested K="+m_topKclusters+" text clusters, since the number of topics in the model is "+res.size()+". Created "+res.size()+" text clusters.");
		return res;
	}	
	
	
}

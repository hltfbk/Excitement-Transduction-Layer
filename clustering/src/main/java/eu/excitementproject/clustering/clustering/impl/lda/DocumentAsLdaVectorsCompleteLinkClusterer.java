package eu.excitementproject.clustering.clustering.impl.lda;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.completeLink.AbstractDocumentCompleteLinkClusterer;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/** Represent documents in LDA space and cluster them with CompleteLink. Sorting of cluster members is defined by Complete Link.
 * @author Lili Kotlerman
 *
 */
public class DocumentAsLdaVectorsCompleteLinkClusterer extends AbstractDocumentLdaClusterer {
	
	public DocumentAsLdaVectorsCompleteLinkClusterer(boolean useExpandedCollection, TextCollection textCollection, String conf, int numOfTopics, WeightType weightType) throws ClusteringException {
		super(useExpandedCollection, textCollection, conf, numOfTopics, weightType);
	}

	

	public DocumentAsLdaVectorsCompleteLinkClusterer(
			boolean useExpandedCollection, TextCollection textCollection,
			String conf, int numOfTopics, WeightType weightType,
			String externalSerializedLdaModelFilename)
			throws ClusteringException {
		super(useExpandedCollection, textCollection, conf, numOfTopics, weightType,
				externalSerializedLdaModelFilename);
		// TODO Auto-generated constructor stub
	}



	public Map<String, List<Integer>> clusterDocumentsByLocalModel(TextCollection textCollection) throws Exception {			
		Set<VectorRepresentation> documentVectors = new HashSet<VectorRepresentation>();
		Map<String,String> docs = m_ldaTester.getDocumentTopicProbabilities(); 
		for (String docId : docs.keySet()){
			documentVectors.add(parseDoc2TopicsString(docId, docs.get(docId)));
		}						
		return AbstractDocumentCompleteLinkClusterer.clusterDocuments(documentVectors, m_topKclusters);
	}

	@Override
	public Map<String, List<Integer>> clusterDocumentsByExternallModel(
			TextCollection textCollection, String m_externalModelFilename2)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}

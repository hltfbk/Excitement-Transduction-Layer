package eu.excitementproject.clustering.clustering.impl.chinesewhispers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.DocumentsToVectorsConverter;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * Implements document clustering with Chinese Whispers for documents represented as bag-of-cluster (BOC) vectors
 * 
 * @author Lili Kotlerman
 *
 */
public class DocumentsChineseWhispersClustererBOC extends AbstractDocumentChineseWhispersClusterer {

	/**
	 * Term clusters to be used for document representation.
	 * <p>Mapping from cluster name (String) to list of terms in the cluster.
	 * List is used to allow sorting terms. 
	 */
	Map<String,List<String>> m_termsByConcept; // [concept name] [term, term, ...]

	/**
	 * @param concepts
	 * @param useExpandedCollection
	 * @param textCollection
	 * @param weightType
	 * @param configurationFilename
	 * @param similarityThreshold
	 * @throws ClusteringException
	 */
	public DocumentsChineseWhispersClustererBOC(Map<String,List<String>> concepts, boolean useExpandedCollection,
			TextCollection textCollection, WeightType weightType,
			String configurationFilename, Double similarityThreshold)
			throws ClusteringException {
		super(useExpandedCollection, textCollection, weightType, configurationFilename,
				similarityThreshold);
		m_termsByConcept = concepts;
		
	}


	

	@Override
	protected Set<VectorRepresentation> representDocuments(TextCollection textCollection) {	
		return DocumentsToVectorsConverter.convertDocumentsToSparseConceptVectors(textCollection, m_useExpandedCollection, m_termsByConcept, m_weightType);
	}


	@Override @Deprecated  
	public void setNumberOfDocumentClusters(int K) {
	}


}

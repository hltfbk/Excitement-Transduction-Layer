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
 * @author Lili Kotlerman
 *
 */
public class DocumentsChineseWhispersClustererBOC extends AbstractDocumentChineseWhispersClusterer {

	Map<String,List<String>> m_termsByConcept; // [concept name] [term, term, ...]

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

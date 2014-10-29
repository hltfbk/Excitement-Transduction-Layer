package eu.excitementproject.clustering.clustering.impl.completeLink;

import java.util.Set;

import eu.excitementproject.clustering.clustering.impl.util.DocumentsToVectorsConverter;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 *
 */
public class DocumentsCompleteLinkClustererBOW extends AbstractDocumentCompleteLinkClusterer {

	Integer  m_topKfeatures = null; 
	public void setTopKFeatures(int K){
		m_topKfeatures = K;
	}
	
	public DocumentsCompleteLinkClustererBOW(boolean useExpandedCollection, WeightType weightType) {
		super(useExpandedCollection, weightType);
	}

	@Override
	protected Set<VectorRepresentation> representDocuments(TextCollection textCollection) {
		if (m_topKfeatures!=null) return DocumentsToVectorsConverter.convertDocumentsToDenseTermVectors(textCollection, m_useExpandedCollection, m_weightType, m_topKfeatures);	
		return DocumentsToVectorsConverter.convertDocumentsToDenseTermVectors(textCollection, m_useExpandedCollection, m_weightType);	
	}
	
}

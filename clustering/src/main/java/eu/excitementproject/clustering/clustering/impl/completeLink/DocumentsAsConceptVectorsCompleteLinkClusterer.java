package eu.excitementproject.clustering.clustering.impl.completeLink;

import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.impl.util.DocumentsToVectorsConverter;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 *
 */
public class DocumentsAsConceptVectorsCompleteLinkClusterer extends AbstractDocumentCompleteLinkClusterer {

	Map<String,List<String>> m_termsByConcept; // [concept name] [term, term, ...]
	
	Integer  m_topKfeatures = null; 
	public void setTopKFeatures(int K){
		m_topKfeatures = K;
	}

	
	public DocumentsAsConceptVectorsCompleteLinkClusterer(boolean useExpandedCollection, Map<String,List<String>> concepts, WeightType weightType) {
		super(useExpandedCollection, weightType);
		m_termsByConcept=concepts;
	}

	@Override
	protected Set<VectorRepresentation> representDocuments(TextCollection textCollection) {	
		if (m_topKfeatures!=null) return DocumentsToVectorsConverter.convertDocumentsToSparseConceptVectors(textCollection, m_useExpandedCollection, m_termsByConcept, m_weightType, m_topKfeatures);
		return DocumentsToVectorsConverter.convertDocumentsToSparseConceptVectors(textCollection, m_useExpandedCollection, m_termsByConcept, m_weightType);
	}

}

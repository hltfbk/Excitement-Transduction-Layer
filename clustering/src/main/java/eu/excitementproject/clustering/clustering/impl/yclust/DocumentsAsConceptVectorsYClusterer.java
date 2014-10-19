package eu.excitementproject.clustering.clustering.impl.yclust;

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
public class DocumentsAsConceptVectorsYClusterer extends AbstractDocumentYClusterer {

	Map<String,List<String>> m_termsByConcept; // [concept name] [term, term, ...]
	
	Integer  m_topKfeatures = null; 
	public void setTopKFeatures(int K){
		m_topKfeatures = K;
	}

	public DocumentsAsConceptVectorsYClusterer(boolean useExpandedCollection, Map<String,List<String>> concepts, WeightType weightType, Double similarityThreshold) {
		super(useExpandedCollection, weightType, similarityThreshold);
		m_termsByConcept=concepts;
	}

	@Override
	protected Set<VectorRepresentation> representDocuments(TextCollection textCollection) {	
		if (m_topKfeatures!=null) return DocumentsToVectorsConverter.convertDocumentsToSparseConceptVectors(textCollection, m_useExpandedCollection, m_termsByConcept, m_weightType, m_topKfeatures);
		return DocumentsToVectorsConverter.convertDocumentsToSparseConceptVectors(textCollection, m_useExpandedCollection, m_termsByConcept, m_weightType);
	}

}

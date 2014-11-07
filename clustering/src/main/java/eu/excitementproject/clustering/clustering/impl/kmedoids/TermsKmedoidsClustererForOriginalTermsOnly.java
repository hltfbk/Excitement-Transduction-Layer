package eu.excitementproject.clustering.clustering.impl.kmedoids;

import java.util.Set;
import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 * 
 * TermsKMedoidsClusterer, which only takes into account the original terms in a collection
 */
public class TermsKmedoidsClustererForOriginalTermsOnly extends TermsKMedoidsClusterer implements TermClusterer {
	
	public TermsKmedoidsClustererForOriginalTermsOnly(boolean useExpandedCollection,
			WeightType weightType, Double similarityThreshold, boolean multi) {
		super(useExpandedCollection, weightType, similarityThreshold, multi);
	}

	@Override
	protected Set<String> getTerms(TextCollection textCollection){
		return textCollection.getDocIdsByOriginalTerm().keySet();
	}

}

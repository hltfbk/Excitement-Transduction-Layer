package eu.excitementproject.clustering.clustering.impl.kmedoids;

import java.util.Set;
import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 * 
 * Implementation of the Y-clustering algorithm by Hui Ye and Steve Young, "A clustering approach to semantic decoding." INTERSPEECH. 2006. *
 * 
 * If a negative number is passed to setNumberOfTermClusters(), no top-K cut-off will be performed in the final iteration.  
 * Final templates ('centroid' terms) are used as cluster labels. 
 */
public class OriginalTermsYClusterer extends TermsYClusterer implements TermClusterer {
	
	public OriginalTermsYClusterer(boolean useExpandedCollection,
			WeightType weightType, Double similarityThreshold, boolean multi) {
		super(useExpandedCollection, weightType, similarityThreshold, multi);
	}

	@Override
	protected Set<String> getTerms(TextCollection textCollection){
		return textCollection.getDocIdsByOriginalTerm().keySet();
	}

}

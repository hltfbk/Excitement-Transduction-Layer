package eu.excitementproject.clustering.clustering.impl.chinesewhispers;


import java.util.Set;

import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 * 
 */
public class OriginalTermsCWClusterer extends TermsCWClusterer implements TermClusterer {

	public OriginalTermsCWClusterer(boolean useExpandedCollection,
			TextCollection textCollection, WeightType weightType,
			String configurationFilename, Double similarityThreshold)
			throws ClusteringException {
		super(useExpandedCollection, textCollection, weightType, configurationFilename,
				similarityThreshold);
	}

	@Override
	protected Set<String> getTerms(TextCollection textCollection){
		return textCollection.getDocIdsByOriginalTerm().keySet();
	}

}

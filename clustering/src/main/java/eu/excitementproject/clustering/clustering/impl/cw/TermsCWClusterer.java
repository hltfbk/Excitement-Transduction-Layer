package eu.excitementproject.clustering.clustering.impl.cw;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.Clusterer;
import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.datastructures.PairMap;

/**
 * @author Lili Kotlerman
 * 
 */
public class TermsCWClusterer extends AbstractChineseWhispersClusterer<String> implements TermClusterer {

	public TermsCWClusterer(boolean useExpandedCollection,
			TextCollection textCollection, WeightType weightType,
			String configurationFilename, Double similarityThreshold)
			throws ClusteringException {
		super(useExpandedCollection, textCollection, weightType, configurationFilename,
				similarityThreshold);
		m_inputFilenamePrefix += "term_";

	}

	PairMap<String,Double> m_semanticRelatednessMap;
		
	@Override
	public Map<String, List<String>> clusterTerms(
			TextCollection textCollection) throws ClusteringException {
		
		m_semanticRelatednessMap = textCollection.getTermSemanticRelatednessMap();

		Set<String> collectionTerms = getTerms(textCollection);
		Map<String, List<String>> clusteringResults = clusterElements(collectionTerms);

		// add any unclustered terms to a non-clust cluster
		Set<String> clusteredTerms = new HashSet<String>();
		for (List<String> cluster : clusteringResults.values()){
			clusteredTerms.addAll(cluster);
		}
		List<String> termsInCluster = new LinkedList<String>();
		for (String term : collectionTerms){
			if (!clusteredTerms.contains(term)) termsInCluster.add(term);
		}
		clusteringResults.put(Clusterer.NON_CLUSTER_LABEL, termsInCluster);
		
		return clusteringResults;
	}


	protected Set<String> getTerms(TextCollection textCollection){
		Set<String> collectionTerms = new HashSet<String>(textCollection.getDocIdsByOriginalTerm().keySet());		
		if (m_useExpandedCollection) collectionTerms.addAll(textCollection.getDocIdsByExpansionTerm().keySet());
		return collectionTerms;
	}

	@Override
	protected double getSimilarity(String termA, String termB) {
		 return SimilarityAndRelatednessCalculator.semanticRelatedness(termA, termB, m_semanticRelatednessMap);
/*		 double similarity = SimilarityAndRelatednessCalculator.semanticRelatedness(termA, termB, m_semanticRelatednessMap);
		 if (similarity < 1) return 1.0;
		 if (similarity > 1) return 2.0;
		 return 1.0;
*/	}



	@Override @Deprecated
	public void setNumberOfTermClusters(int K) {		
	}



	@Override
	protected String getElementString(String element) {
		return element;
	}

}

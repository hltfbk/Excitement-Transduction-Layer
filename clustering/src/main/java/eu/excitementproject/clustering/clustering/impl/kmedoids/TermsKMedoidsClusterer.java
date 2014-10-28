package eu.excitementproject.clustering.clustering.impl.kmedoids;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.clustering.impl.util.rep.ClusterMember;
import eu.excitementproject.clustering.clustering.impl.util.rep.StringsPatrition;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.datastructures.PairMap;

/**
 * @author Lili Kotlerman
 * 
 * Implementation of the K-medoids clustering algorithm as suggested by by Hui Ye and Steve Young, "A clustering approach to semantic decoding." INTERSPEECH. 2006. *
 * 
 * If a negative number is passed to setNumberOfTermClusters(), no top-K cut-off will be performed in the final iteration.  
 * Final templates ('centroid' terms) are used as cluster labels. 
 */
public class TermsKMedoidsClusterer extends AbstractKmedoidsClusterer<String> implements TermClusterer {

	private final boolean m_multi; // if true, multi-class clustering of terms will be returned
	
	//TODO: init templates will all the original terms, rather than random selection
	
public TermsKMedoidsClusterer(boolean useExpandedCollection,
			WeightType weightType, Double similarityThreshold, boolean multi) {
		super(useExpandedCollection, weightType, similarityThreshold);
		m_multi = multi;
	}

	PairMap<String,Double> m_semanticRelatednessMap;
		
	@Override
	public Map<String, List<String>> clusterTerms(
			TextCollection textCollection) {
		
		m_semanticRelatednessMap = textCollection.getTermSemanticRelatednessMap();
		
		Set<String> collectionTerms = getTerms(textCollection);
		return termClusterer(collectionTerms, m_topKclusters, m_multi);	
	}
	
	protected Set<String> getTerms(TextCollection textCollection){
		Set<String> collectionTerms = new HashSet<String>(textCollection.getDocIdsByOriginalTerm().keySet());		
		if (m_useExpandedCollection) collectionTerms.addAll(textCollection.getDocIdsByExpansionTerm().keySet());
		return collectionTerms;
	}

	private Map<String,List<String>> termClusterer(Set<String> terms, int topKClusters, boolean multi){

		Map<String,Map<String,Double>> res = clusterElements(terms, topKClusters, multi);
		Map<String,List<String>> clusteringResults = new HashMap<String, List<String>>();
		for(String label : res.keySet()){
			Set<String> clusterTerms = new HashSet<String>(res.get(label).keySet());
			List<ClusterMember<String>> termsInClusterWithScore = new LinkedList<ClusterMember<String>>();
			for (String term : clusterTerms){
				Double score = res.get(label).get(term);
				termsInClusterWithScore.add(new ClusterMember<String>(term,score));
			}
			
			clusteringResults.put(label, StringsPatrition.getSortedByScoreCluster(termsInClusterWithScore));

/*			System.out.print(label);
			System.out.print(":");
			System.out.println(cluster.size());
			//System.out.println(cluster);			
*/		}
		return clusteringResults;
	}

	@Override
	public void setNumberOfTermClusters(int K) {
		m_topKclusters = K;
	}

	@Override
	protected double getSimilarity(String termA, String termB) {
		return SimilarityAndRelatednessCalculator.semanticRelatedness(termA, termB, m_semanticRelatednessMap);
	}

	@Override
	protected String getOutOfClassKey() {		
		return NON_CLUSTER_LABEL;
	}
}

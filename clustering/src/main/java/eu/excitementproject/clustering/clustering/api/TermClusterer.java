package eu.excitementproject.clustering.clustering.api;

import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * Interface for term clustering
 *
 * @author Lili Kotlerman
 */
public interface TermClusterer extends Clusterer{
	
	/**
	 * @param textCollection - the {@link TextCollection}, terms in which are to be clustered
	 * @return A mapping with a list of terms (String) corresponding to each cluster name (String). List - since the terms can be sorted by confidence of assignment
	 * @throws ClusteringException
	 */
	public Map<String, List<String>> clusterTerms(TextCollection textCollection) throws ClusteringException;

	/**
	 * Set the desired number K of output term clusters  
	 * @param K 
	 */
	public void setNumberOfTermClusters(int K);
}

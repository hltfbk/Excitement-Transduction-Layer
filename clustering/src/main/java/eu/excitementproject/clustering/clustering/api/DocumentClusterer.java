package eu.excitementproject.clustering.clustering.api;

import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * Interface for document clustering
 * 
 * @author Lili Kotlerman
 */
public interface DocumentClusterer extends Clusterer{
	
	/**
	 * @param textCollection - the {@link TextCollection}, documents in which are to be clustered
	 * @return A mapping with a list of document ids (Integer) corresponding to each cluster name (String). List - since the documents can be sorted by confidence of assignment
	 * @throws ClusteringException
	 */
	public Map<String, List<Integer>> clusterDocuments(TextCollection textCollection) throws ClusteringException;

	/**
	 * Set the desired number K of output document clusters  
	 * @param K 
	 */
	public void setNumberOfDocumentClusters(int K);
	
}

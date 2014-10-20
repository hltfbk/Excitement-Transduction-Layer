package eu.excitementproject.clustering.clustering.api;

import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 *
 */
public interface DocumentClusterer extends Clusterer{
	
	/**
	 * @return A mapping with a list of document ids (Integer) corresponding to each cluster name (String). List - since the documents can be sorted by confidence of assignment 
	 * @throws ClusteringException 
	 */
	public Map<String, List<Integer>> clusterDocuments(TextCollection textCollection) throws ClusteringException;

	public void setNumberOfDocumentClusters(int K);
	
}

package eu.excitementproject.tl.structures.collapsedgraph;

import java.util.Map;

/**
 * This class contains a few statistics computed for an EntailmentGraphCollapsed that are relevant
 * for computing category confidence scores for use case 2.
 * 
 * @author Kathrin Eichler
 *
 */

public class GraphStatistics {
	
	Map<String,Integer> numberOfMentionsPerCategory;
	int totalNumberOfMentions;
	int totalNumberOfNodes;
	
	/**
	 * @return a map containing the number of mentions in the graph associated to each category
	 */
	public Map<String, Integer> getNumberOfMentionsPerCategory() {
		return numberOfMentionsPerCategory;
	}
	/**
	 * @param numberOfMentionsPerCategory a map containing the number of mentions in the graph associated to each category
	 */
	public void setNumberOfMentionsPerCategory(
			Map<String, Integer> numberOfMentionsPerCategory) {
		this.numberOfMentionsPerCategory = numberOfMentionsPerCategory;
	}
	/**
	 * @return the total number of mentions contained in the graph
	 */
	public int getTotalNumberOfMentions() {
		return totalNumberOfMentions;
	}
	/**
	 * @param totalNumberOfNodes the total number of mentions contained in the graph
	 */
	public void setTotalNumberOfMentions(int totalNumberOfNodes) {
		this.totalNumberOfMentions = totalNumberOfNodes;
	}
	/**
	 * @return the total number of nodes (equivalence classes) in the graph
	 */
	public int getTotalNumberOfNodes() {
		return totalNumberOfNodes;
	}
	/**
	 * @param totalNumberOfNodes the total number of nodes (equivalence classes) in the graph
	 */
	public void setTotalNumberOfNodes(int totalNumberOfNodes) {
		this.totalNumberOfNodes = totalNumberOfNodes;
	}
}

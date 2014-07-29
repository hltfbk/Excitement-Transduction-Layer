package eu.excitementproject.tl.structures.collapsedgraph;

import java.util.Map;

public class GraphStatistics {
	
	Map<String,Integer> numberOfMentionsPerCategory;
	int totalNumberOfMentions;
	int totalNumberOfNodes;
	
	public Map<String, Integer> getNumberOfMentionsPerCategory() {
		return numberOfMentionsPerCategory;
	}
	public void setNumberOfMentionsPerCategory(
			Map<String, Integer> numberOfMentionsPerCategory) {
		this.numberOfMentionsPerCategory = numberOfMentionsPerCategory;
	}
	public int getTotalNumberOfMentions() {
		return totalNumberOfMentions;
	}
	public void setTotalNumberOfMentions(int totalNumberOfNodes) {
		this.totalNumberOfMentions = totalNumberOfNodes;
	}
	public int getTotalNumberOfNodes() {
		return totalNumberOfNodes;
	}
	public void setTotalNumberOfNodes(int totalNumberOfNodes) {
		this.totalNumberOfNodes = totalNumberOfNodes;
	}
	
	

}

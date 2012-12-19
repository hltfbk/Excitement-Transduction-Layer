package composition;

import java.util.List;
import java.util.Map;

import composition.entities.EntailmentRelation;

import decomposition.entities.EntailmentUnit;

/**
 * This class represents the data structure that holds a raw entailment graph. 
 * In the raw entailment graph, a node corresponds to an entailment unit, 
 * an edge corresponds to an entailment decision. 
 * 
 * OPEN ISSUES: 
 * - How do we deal with "known" entailment relations (fragment --> subfragment)?
 * - Do we need a separate graph for each scenario?
 * 
 *  TO BE IMPLEMENTED BY ???
 *  
 * @author Kathrin
 */

public class EntailmentGraphRaw {

	private int ID;
	private List<EntailmentUnit> nodes;
	private List<EntailmentRelation> edges;
	private Map<Integer, Integer> processedPairs;
	
	public EntailmentGraphRaw() {
		//construct empty graph
	}
	
	public EntailmentGraphRaw(String graphXML) {
		//construct graph from XML
	}
	
	public int getID() {
		return ID;
	}
	
	public void setID(int iD) {
		ID = iD;
	}
	
	public List<EntailmentUnit> getNodes() {
		return nodes;
	}
	
	public void setNodes(List<EntailmentUnit> nodes) {
		this.nodes = nodes;
	}
	
	public List<EntailmentRelation> getEdges() {
		return edges;
	}
	
	public void setEdges(List<EntailmentRelation> edges) {
		this.edges = edges;
	}
	
	public Map<Integer, Integer> getProcessedPairs() {
		return processedPairs;
	}
	
	public void setProcessedPairs(Map<Integer, Integer> processedPairs) {
		this.processedPairs = processedPairs;
	}
	
	//METHODS
	
	public int addNode(EntailmentUnit entailmentUnit) {
		int nodeId = 0;
		return nodeId;
	}
	
	
	public int addEdge(EntailmentRelation entailmentRelation) {
		int edgeId = 0;
		return edgeId;
	}
	
	public boolean process(EntailmentUnit entailmentUnit) {
		boolean success = false;
		//compare incoming entailmentUnit to all existing nodes; create nodes and edges accordingly
		return success;
	}
	
	public boolean process(EntailmentGraphRaw newGraph) {
		boolean success = false;
		return success;
	}
	
	public EntailmentUnit getMatchingNode(EntailmentUnit entailmentUnit) {
		//integrate entailment unit into existing node
		EntailmentUnit eu = null;
		return eu;
	}
	
	public int getMatchingNodeId(EntailmentUnit entailmentUnit) {
		return 0;
	}

	public Map<String,Double> getConfidenceScoresForEdge(int from, int to) {
		return null;
	}
	
	
	public List<EntailmentRelation> getIncomingEntailmentRelations(int nodeId) {
		return null;
	}

	public List<EntailmentRelation> getOutgoingEntailmentRelations(int nodeId) {
		return null;
	}

	public String toXML() {
		return "";
	}
	
}

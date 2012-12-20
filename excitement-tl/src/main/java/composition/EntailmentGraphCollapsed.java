package composition;

import java.util.List;

import java.util.Map;
import java.util.SortedMap;

import composition.entities.EquivalenceClass;
import decomposition.entities.EntailmentUnit;

/**
 * This class represents the data structure that holds a collapsed entailment graph. 
 * In the collapsed entailment graph, a node corresponds to an equivalence class, 
 * an edge corresponds to a positive entailment relation between two nodes. 
 * 
 *  * OPEN ISSUES: 
 * - How do we deal with "known" entailment relations (fragment --> subfragment)?
 * - Do we need a separate graph for each scenario?

 *  TO BE IMPLEMENTED BY ???
 *  
 * @author Kathrin
 */

public class EntailmentGraphCollapsed {

	private List<EquivalenceClass> nodes;
	private Map<Integer, Integer> edges;
	private double confidenceThreshold;
	private String rawGraphId;
	
	public List<EquivalenceClass> getNodes() {
		return nodes;
	}
	
	public void setNodes(List<EquivalenceClass> nodes) {
		this.nodes = nodes;
	}
	
	public Map<Integer, Integer> getEdges() {
		return edges;
	}
	
	public void setEdges(Map<Integer, Integer> edges) {
		this.edges = edges;
	}
	
	public double getConfidenceThreshold() {
		return confidenceThreshold;
	}
	
	public void setConfidenceThreshold(double confidenceThreshold) {
		this.confidenceThreshold = confidenceThreshold;
	}
	
	public String getRawGraphId() {
		return rawGraphId;
	}
	
	public void setRawGraphId(String rawGraphId) {
		this.rawGraphId = rawGraphId;
	}
	
	//METHODS 
	
	public int getNumberOfTextualInputs() {
		return 0;
	}
	
	public int getNumberOfEntailmentUnits() {
		return 0;
	}
	
	public int getNumberOfEquivalenceClasses() {
		return this.nodes.size();
	}
	
	public List<EntailmentUnit> getEquivalentEntailmentUnits(EntailmentUnit entailmentUnit) {
		return null;
	}
	
	public List<Integer> getEntailingEquivalenceClasses(EntailmentUnit entailmentUnit) {
		return null;
	}

	public List<Integer> getEntailedEquivalenceClasses(EntailmentUnit entailmentUnit) {
		return null;
	}
	
	public SortedMap<EquivalenceClass, Integer> getTopNodes(int mapSize) {
		//sort equivalence classes by number of associated interactions
		//return the top X entries, where X = mapSize
		return null;
	}
	
	public EntailmentGraphRaw getSubgraphFor(EntailmentUnit entailmentUnit) {
		return null;
	}
	
	public List<String> getAssociatedTextualInputs(EntailmentUnit entailmentUnit) {
		//return IDs of textual inputs containing the entailmentUnit or an equivalent statement
		return null;
	}
	
	public String toXML() {
		return "";
	}
}

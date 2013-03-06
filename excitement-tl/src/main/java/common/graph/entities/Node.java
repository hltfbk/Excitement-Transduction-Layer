package common.graph.entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Node {
	
	private int value;
	private HashMap<EntailmentRelation,Set<Node>> related;
	
	public Set<Node> getRelatedNodes(){
		Set<Node> nodes = new HashSet<Node>();
		for (EntailmentRelation r: related.keySet()) {
			nodes.addAll(related.get(r));
		}
		return nodes;
	}
	
	public Set<Node> getRelatedNodes(EntailmentRelation r){
		if (related.containsKey(r))
			return related.get(r);
		return null;
	}
	
	public void addRelation(EntailmentRelation r, Node n){
		Set<Node> nodes;
		if (related.containsKey(r)) {
			nodes = related.get(r);
		} else {
			nodes = new HashSet<Node>();
		}
		nodes.add(n);
		related.put(r,nodes);
	}
}

package graph;

import java.util.Set;

public class Graph<Node> {
	public Set<Node> baseNodes;
	
	public Set<Node> getBaseNodes() {
		return baseNodes;
	}
	
	// returns all nodes at a certain level -- i.e. that have "level" number of modifiers 
	public Set<Node> getNodes(int level) {
		if (level == 0)
			return baseNodes;
		
		// start from the baseNodes (i.e. no modifiers) and go up through 
		// the node's parents (maintained in the Node object) up to level "level" 
		return null;
	}
}

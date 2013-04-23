package eu.excitementproject.tl.structures.fragmentgraph;

import org.junit.Test;

public class FragmentGraphTest {

	@Test
	public void test() {
		FragmentGraph g = new FragmentGraph(FragmentGraphEdge.class);
		
		// make nodes
		EntailmentUnitMention v1 = new EntailmentUnitMention("base statement");
		EntailmentUnitMention v2 = new EntailmentUnitMention("base statement + M1",1);
		EntailmentUnitMention v3 = new EntailmentUnitMention("base statement + M2",1);
		EntailmentUnitMention v4 = new EntailmentUnitMention("base statement + M1 + M2",2);
		
		// add nodes to graph
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);
		
		// add an edge with explicit edge object
		g.addEdge(v1, v2, new FragmentGraphEdge(v1, v2, 3.0));
		
		// add an edge using the default method
		g.addEdge(v1, v3);
		g.addEdge(v2, v4);
		g.addEdge(v3, v4);
		
		System.out.println("Graph: \\" + g.toString());
	}
}

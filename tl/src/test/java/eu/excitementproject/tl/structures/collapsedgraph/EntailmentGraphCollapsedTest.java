package eu.excitementproject.tl.structures.collapsedgraph;

import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;

public class EntailmentGraphCollapsedTest {

	@Test
	public void test() {
		try {
			System.out.println("Test loading collapsed graph from file\n");
			EntailmentGraphCollapsed graph = new EntailmentGraphCollapsed(new File("./src/test/outputs/collapsed_graph.xml"));
			graph.toString();
			System.out.println("Graph based on "+ graph.getNumberOfFragmentGraphs()+ "fragment graphs");
		} catch (EntailmentGraphCollapsedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

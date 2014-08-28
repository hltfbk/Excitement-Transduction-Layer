package eu.excitementproject.tl.structures.collapsedgraph;

import static org.junit.Assert.fail;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;

public class EntailmentGraphCollapsedTest {

	private static final Logger logger = Logger.getLogger(EntailmentGraphCollapsedTest.class);

	@Test
	public void test() {
		try {
			logger.info("Test loading collapsed graph from file\n");
			EntailmentGraphCollapsed graph = new EntailmentGraphCollapsed(new File("./src/test/outputs/collapsed_graph.xml"));
			graph.toString();
			logger.info("Graph based on "+ graph.getNumberOfFragmentGraphs()+ "fragment graphs");
		} catch (EntailmentGraphCollapsedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}

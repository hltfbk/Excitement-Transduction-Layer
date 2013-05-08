package eu.excitementproject.tl.structures.rawgraph;

import org.junit.Test;


public class EntailmentGraphRawTest {
	
	@Test
	public void test() {
		EntailmentGraphRaw rawGraph = EntailmentGraphRaw.getSampleOuput();
		System.out.println(rawGraph.toString());
	}

}



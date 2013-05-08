package eu.excitementproject.tl.structures.rawgraph;

import org.junit.Test;


public class EntailmentGraphRawTest {
	
	@Test
	public void test() {
		EntailmentGraphRaw rawGraph = EntailmentGraphRaw.getSampleOuput(false);
		System.out.println("********************************\n Non-random graph:\n"+rawGraph.toString());

		
		rawGraph = EntailmentGraphRaw.getSampleOuput(true);
		System.out.println("********************************\n Random graph:\n"+rawGraph.toString());
		
	}

}



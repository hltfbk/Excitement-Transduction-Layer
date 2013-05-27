package eu.excitementproject.tl.decomposition.fragmentgraphgenerator;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import junit.framework.Assert;

import org.apache.uima.jcas.JCas;
import org.junit.Test;

import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;

public class FragmentGraphGeneratorTest {

	/**
	 *  The test class tests FragmentGraphGenerator 
	 */
	@Test
	public void test() {
		
		try {
			// first, call CASUtils to generate InputCAS examples. 		
			CASUtils.generateExamples(); // the examples are stored in /target. 
			
			// generate fragment graphs from each of the inputCAS examples.  
			JCas aJCas = CASUtils.createNewInputCas(); 
			File f1 = new File("./target/CASInput_example_1.xmi"); 
			File f2 = new File("./target/CASInput_example_2.xmi");
			File f3 = new File("./target/CASInput_example_3.xmi"); 
			File f4 = new File("./target/CASInput_example_4.xmi"); 

			// initiate the FragGraphGenerator... 
			FragmentGraphGeneratorFromCAS fragGen = new FragmentGraphGeneratorFromCAS(); 

			// Read in inputCASes for the examples, and generate the FragmentGraphs 
			CASUtils.deserializeFromXmi(aJCas, f1); 
			Set<FragmentGraph> fgs_example1 = fragGen.generateFragmentGraphs(aJCas); 
			CASUtils.deserializeFromXmi(aJCas, f2); 
			Set<FragmentGraph> fgs_example2 = fragGen.generateFragmentGraphs(aJCas); 
			CASUtils.deserializeFromXmi(aJCas, f3); 
			Set<FragmentGraph> fgs_example3 = fragGen.generateFragmentGraphs(aJCas); 
			CASUtils.deserializeFromXmi(aJCas, f4); 
			Set<FragmentGraph> fgs_example4 = fragGen.generateFragmentGraphs(aJCas); 
			
			// To Vivi (& also to Lili?): Now investigate the generated Fragment Graphs. 
			// They are actually the "Demo" Fragment Graphs of KAthrin's examples. 
			
			// TODO (for Vivi) 
			// replace and enrich the following asserts with more logical ones (check the number of graphs, number of nodes per graphs, etc?) 
			
			// Test for example #1: Food was really bad 
			Assert.assertNotNull(fgs_example1); 
			Assert.assertTrue(fgs_example1.size() > 0);
			System.out.println("\n________________\nFragment graphs for example 1: ");
			for(FragmentGraph f: fgs_example1) {
				System.out.println(f.toString());
			}
			
			// Test for example #2: I didn't like the food 
			Assert.assertNotNull(fgs_example2); 
			Assert.assertTrue(fgs_example2.size() > 0);
			System.out.println("\n________________\nFragment graphs for example 2: ");
			for(FragmentGraph f: fgs_example2) {
				System.out.println(f.toString());
			}

			// Test for example #3: a little more leg room would have been perfect
			Assert.assertNotNull(fgs_example3); 
			Assert.assertTrue(fgs_example3.size() > 0); 
			System.out.println("\n________________\nFragment graphs for example 3: ");
			for(FragmentGraph f: fgs_example3) {
				System.out.println(f.toString());
			}
			
			// Test for example #4: Disappointed with the amount of legroom compared with other trains 
			Assert.assertNotNull(fgs_example4); 
			Assert.assertTrue(fgs_example4.size() > 0); 
			System.out.println("\n________________\nFragment graphs for example 4: ");
			for(FragmentGraph f: fgs_example4) {
				System.out.println(f.toString());
			}
			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}

}

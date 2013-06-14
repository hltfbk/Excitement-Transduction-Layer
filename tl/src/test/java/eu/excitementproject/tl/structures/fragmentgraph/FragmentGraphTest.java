package eu.excitementproject.tl.structures.fragmentgraph;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.uima.jcas.JCas;
//import org.junit.Ignore;
import org.junit.Test;

import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.laputils.CASUtils;

public class FragmentGraphTest {

	@Test
	public void test(){
		String text = "The hard old seats were very uncomfortable";
		Set<String> modifiers = new HashSet<String>();
		modifiers.add("hard");
		modifiers.add("old");
		modifiers.add("very");
		FragmentGraph g = new FragmentGraph(text,modifiers);
				
		System.out.println("Graph: \n" + g.toString());
		
		System.out.println("Base statement: \n" + g.getBaseStatement().getText());
		System.out.println("Top statement: \n" + g.getCompleteStatement().getText());
		
		System.out.println("Interaction Id: " + g.getInteractionId());
		
		for(EntailmentUnitMention eum: g.vertexSet()) {
			System.out.println("text: " + eum.getText() + " (level " + eum.getLevel() + ")" );
			for(SimpleModifier sm: eum.getModifiers()) {
				System.out.println("\t" + sm.getText() + " (" + sm.getStart() + " -- " + sm.getEnd() + ")");
			}
		}
		
		try{
			
		
		// generate fragment graphs from each of the inputCAS examples.  
		JCas aJCas = CASUtils.createNewInputCas(); 
		File f = new File("./src/test/resources/WP2_public_data_CAS_XMI/alma_speech/Speech3.1.004.txt.xmi"); 

		// initiate the FragGraphGenerator... 
		FragmentGraphGeneratorFromCAS fragGen = new FragmentGraphGeneratorFromCAS(); 

		// Read in inputCASes for the examples, and generate the FragmentGraphs 
		CASUtils.deserializeFromXmi(aJCas, f); 
		Set<FragmentGraph> fgs_example1 = fragGen.generateFragmentGraphs(aJCas);  
		
		// To Vivi (& also to Lili?): Now investigate the generated Fragment Graphs. 
		// They are actually the "Demo" Fragment Graphs of KAthrin's examples. 
		
		// TODO (for Vivi) 
		// replace and enrich the following asserts with more logical ones (check the number of graphs, number of nodes per graphs, etc?) 
		
		// Test for example #1: Food was really bad 
		Assert.assertNotNull(fgs_example1); 
		Assert.assertTrue(fgs_example1.size() > 0);
		System.out.println("\n________________\nFragment graphs for example 1: ");
		for(FragmentGraph fg: fgs_example1) {
			System.out.println(fg.toString());
		}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}

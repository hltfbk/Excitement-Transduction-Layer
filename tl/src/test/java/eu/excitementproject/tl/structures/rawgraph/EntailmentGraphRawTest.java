package eu.excitementproject.tl.structures.rawgraph;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.junit.Test;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;

public class EntailmentGraphRawTest {
	
	@Test
	public void test() {
		
		System.out.println("\n\n=================================================================================================================================\n");
		System.out.println("TESTING SAMPLE OUTPUT \n");
		System.out.println("=================================================================================================================================\n");
		
		EntailmentGraphRaw rawGraph = EntailmentGraphRaw.getSampleOuput(false);
		System.out.println("********************************\n Non-random graph:\n"+rawGraph.toString());

		
		rawGraph = EntailmentGraphRaw.getSampleOuput(true);
		System.out.println("********************************\n Random graph:\n"+rawGraph.toString());
		
		System.out.println("\n\n=================================================================================================================================\n");
		System.out.println("TESTING public EntailmentGraphRaw(FragmentGraph fg) \n");
		System.out.println("=================================================================================================================================\n");


		String text = "The hard old seats were very uncomfortable";
		Set<String> modifiers = new HashSet<String>();
		modifiers.add("hard");
		modifiers.add("old");
		modifiers.add("very");
		FragmentGraph g = new FragmentGraph(text,modifiers);
		
		rawGraph = new EntailmentGraphRaw(g);
		System.out.println("********************************\n Copy of a Fragment graph:\n"+rawGraph.toString());
		
		System.out.println("********************************\n Restore fragment graph from the raw graph:\n"+rawGraph.toString());
		Set<EntailmentUnit> bs = rawGraph.getBaseStatements();
		for (EntailmentUnit eu : bs){
			System.out.println(eu.getText());	
			for (String completeString : eu.completeStatementTexts){
				System.out.println(completeString);	
				try {
					Hashtable<Integer, Set<EntailmentUnit>> fgNodes = rawGraph.getFragmentGraphNodes(eu,completeString);
					for (int level : fgNodes.keySet()){
						System.out.println("\tlevel "+level);
						for (EntailmentUnit node : fgNodes.get(level)){
							System.out.println("\t\t"+node.toString());
						}
					}
				} catch (EntailmentGraphRawException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}							
			}
		}
		
	}

}



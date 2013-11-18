package eu.excitementproject.tl.structures.rawgraph;

import java.io.File;
import java.util.Hashtable;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.junit.Test;

import static org.junit.Assert.fail;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;

public class EntailmentGraphRawTest {
	

	@Test
	public void test() {
		
		System.out.println("\n\n=================================================================================================================================\n");
		System.out.println("TESTING SAMPLE OUTPUT \n");
		System.out.println("=================================================================================================================================\n");
		
		EntailmentGraphRaw rawGraph = EntailmentGraphRaw.getSampleOuput(false);
		System.out.println("********************************\n Non-random graph:\n"+rawGraph.toString());
		try {
			XMLFileWriter.write(rawGraph.toXML(), "./src/test/outputs/sampleRawGraph.xml");			
		} catch (EntailmentGraphRawException | TransformerException e) {
			fail(e.getMessage());
		}

		
		rawGraph = EntailmentGraphRaw.getSampleOuput(true);
		System.out.println("********************************\n Random graph:\n"+rawGraph.toString());
		
		System.out.println("\n\n=================================================================================================================================\n");
		System.out.println("TESTING public EntailmentGraphRaw(FragmentGraph fg) \n");
		System.out.println("=================================================================================================================================\n");


		FragmentGraph g = FragmentGraph.getSampleGraph();
		
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
					fail(e.getMessage());
				}							
			}
		}

		System.out.println("\n\n=================================================================================================================================\n");
		System.out.println("TESTING XML SAVE/LOAD \n");
		System.out.println("=================================================================================================================================\n");
		rawGraph = EntailmentGraphRaw.getSampleOuput(false);
		System.out.println("********************************\n Sample graph:\n"+rawGraph.toString());
		
		try {
			String xmlFile = "./src/test/outputs/sampleRawGraph.xml";
			EntailmentGraphRaw loadedGraph = new EntailmentGraphRaw(new File(xmlFile));
			loadedGraph.toString();
			for (EntailmentUnit eu : loadedGraph.vertexSet()){
				System.out.println("\n"+eu.getText());
				System.out.println(eu.getCompleteStatementTexts().size());
				for (String text: eu.getCompleteStatementTexts()){
					System.out.println("  : "+text);					
				}
			}
		} catch (EntailmentGraphRawException e) {
			fail(e.getMessage());
		}
		
	}

}



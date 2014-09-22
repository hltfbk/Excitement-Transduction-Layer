package eu.excitementproject.tl.structures.rawgraph;

import java.io.File;
import java.util.Hashtable;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.fail;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;

public class EntailmentGraphRawTest {
	
	private final Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void test() {
		
		logger.info("\n\n=================================================================================================================================\n");
		logger.info("TESTING SAMPLE OUTPUT \n");
		logger.info("=================================================================================================================================\n");
		
		EntailmentGraphRaw rawGraph = EntailmentGraphRaw.getSampleOuput(false);
		logger.info("********************************\n Non-random graph:\n"+rawGraph.toString());
		try {
			XMLFileWriter.write(rawGraph.toXML(), "./src/test/outputs/sampleRawGraph.xml");			
		} catch (EntailmentGraphRawException | TransformerException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		
		rawGraph = EntailmentGraphRaw.getSampleOuput(true);
		logger.info("********************************\n Random graph:\n"+rawGraph.toString());
		
		logger.info("\n\n=================================================================================================================================\n");
		logger.info("TESTING public EntailmentGraphRaw(FragmentGraph fg) \n");
		logger.info("=================================================================================================================================\n");


		FragmentGraph g = FragmentGraph.getSampleGraph();
		
		rawGraph = new EntailmentGraphRaw(g, false);
		logger.info("********************************\n Copy of a Fragment graph:\n"+rawGraph.toString());
		
		logger.info("********************************\n Restore fragment graph from the raw graph:\n"+rawGraph.toString());
		Set<EntailmentUnit> bs = rawGraph.getBaseStatements();
		for (EntailmentUnit eu : bs){
			logger.info(eu.getText());	
			for (String completeString : eu.completeStatementTexts){
				logger.info(completeString);	
				try {
					Hashtable<Integer, Set<EntailmentUnit>> fgNodes = rawGraph.getFragmentGraphNodes(eu,completeString);
					for (int level : fgNodes.keySet()){
						logger.info("\tlevel "+level);
						for (EntailmentUnit node : fgNodes.get(level)){
							logger.info("\t\t"+node.toString());
						}
					}
				} catch (EntailmentGraphRawException e) {
					e.printStackTrace();
					fail(e.getMessage());
				}							
			}
		}

		logger.info("\n\n=================================================================================================================================\n");
		logger.info("TESTING XML SAVE/LOAD \n");
		logger.info("=================================================================================================================================\n");
		rawGraph = EntailmentGraphRaw.getSampleOuput(false);
		logger.info("********************************\n Sample graph:\n"+rawGraph.toString());
		
		try {
			String xmlFile = "./src/test/outputs/sampleRawGraph.xml";
			EntailmentGraphRaw loadedGraph = new EntailmentGraphRaw(new File(xmlFile));
			loadedGraph.toString();
			for (EntailmentUnit eu : loadedGraph.vertexSet()){
				logger.info("\n"+eu.getText());
				logger.info(eu.getCompleteStatementTexts().size());
				for (String text: eu.getCompleteStatementTexts()){
					logger.info("  : "+text);					
				}
			}
		} catch (EntailmentGraphRawException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

}



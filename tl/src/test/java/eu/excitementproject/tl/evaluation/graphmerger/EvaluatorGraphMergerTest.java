package eu.excitementproject.tl.evaluation.graphmerger;

import static org.junit.Assert.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.excitementproject.tl.demo.DemoUseCase1NICEEnglish;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;

public class EvaluatorGraphMergerTest {

	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.evaluation.graphmerger.test"); 
		
		boolean includeFragmentGraphEdges = true;
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(true);
		String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/_annotationExample.xml";
		try {
			loader.addAnnotationsFromFile(annotationFilename, false);
			testlogger.info("Loaded "+loader.getEdges().size()+" gold standard edges.");
/*			int i=1;		
			for (EntailmentRelation edge : loader.getEdges){
				System.out.println(i+": "+edge);
				i++;
			}
*/			
			
			testlogger.info(EvaluatorGraphMerger.evaluate(loader.getEdges(), loader.getEdges(), includeFragmentGraphEdges));
		} catch (GraphEvaluatorException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

}

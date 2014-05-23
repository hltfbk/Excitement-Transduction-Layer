package eu.excitementproject.tl.evaluation.categoryannotator;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

public class EvaluatorCategoryAnnotatorTest {
	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.evaluation.categoryannotator.test"); 
		
		try {					
			testlogger.info("Reading dataset for evaluation.");
			String inputFilename = "./src/test/resources/WP2_public_data_XML/german_dummy_data_for_evaluator_test.xml"; //dataset to be evaluated
			String outputDirname = "./src/test/outputs/"; //output directory (for generated entailment graph)
			String configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml"; //config file for EDA
					
			testlogger.info("Computing result for dataset.");
			EvaluatorCategoryAnnotator eca = new EvaluatorCategoryAnnotator();
			double result = eca.runEvaluationOnTrainTestDataset(inputFilename, outputDirname, configFilename);
			testlogger.info("result: " + result);
			Assert.assertEquals(result, 0.5); 
			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}
}

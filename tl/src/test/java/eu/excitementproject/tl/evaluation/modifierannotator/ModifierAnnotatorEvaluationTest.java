package eu.excitementproject.tl.evaluation.modifierannotator;

import java.io.IOException;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;

public class ModifierAnnotatorEvaluationTest {
	
	@Test
	public void test() {
	
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator: test");
		logger.setLevel(Level.INFO);
		
		try {
			EvaluationMeasures eval = ModifierAnnotatorEvaluator.evaluateModifiers(
					"src/test/resources/WP2_public_data_CAS_XMI/nice_speech", 
//					"src/test/resources/WP2_public_data_CAS_XMI/alma_speech", 
					"eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator", 
					"EN");
			
			logger.info(eval.toString());
			
		} catch (LAPException e) {
			logger.error("Error creating the LAP object");
			e.printStackTrace();
		} catch (ModifierAnnotatorException e) {
			logger.error("Error creating modifier annotator object");
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("I/O error");
			e.printStackTrace();
		}
		
	}
}

package eu.excitementproject.tl.evaluation.modifierannotator;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
//import org.junit.Ignore;
import org.junit.Test;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;

public class ModifierAnnotatorEvaluationTest {
	
	@Ignore
	@Test
	public void test() {
	
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator: test");
		logger.setLevel(Level.INFO);
		
		try {
			EvaluationMeasures eval = ModifierAnnotatorEvaluator.evaluateModifiers(
//					"src/test/resources/WP2_public_data_CAS_XMI/nice_email_3", 
//					"src/test/resources/WP2_public_data_CAS_XMI/ALMA_social_media", 
//					"target/WP2_public_data_CAS_XMI/ALMA_social_media",
					
					"src/test/resources/WP2_public_data_CAS_XMI/NICE_open/all/",

//					"eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator",
//					"eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotatorNoNeg",
//					"eu.excitementproject.tl.decomposition.modifierannotator.AdvAdjAsModifierAnnotatorNoNeg",
					"eu.excitementproject.tl.decomposition.modifierannotator.AdvAdjPPAsModifierAnnotatorNoNeg",
//					"eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator",
//					"eu.excitementproject.tl.decomposition.fragmentannotator.KeywordBasedFixedLengthFragmentAnnotator",
					"eu.excitementproject.tl.decomposition.fragmentannotator.KeywordBasedFragmentAnnotator",

//					"IT");
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

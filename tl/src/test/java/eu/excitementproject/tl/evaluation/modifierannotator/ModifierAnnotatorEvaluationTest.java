package eu.excitementproject.tl.evaluation.modifierannotator;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
//import org.junit.Ignore;
import org.junit.Test;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.KeywordBasedFixedLengthFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAdjAsModifierAnnotator;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAdjPPAsModifierAnnotator;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.laputils.DependencyLevelLapEN;
import eu.excitementproject.tl.laputils.LAPUtils;

@SuppressWarnings("unused")
public class ModifierAnnotatorEvaluationTest {
	
//	@Ignore
	@Test
	public void test() {
	
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator: test");
		logger.setLevel(Level.INFO);
		
		try {
			
			String lang = "IT";
//			String lang = "EN";			
			
			LAPAccess lap = LAPUtils.initializeLAP(lang);
			
//			FragmentAnnotator fragAnn = new SentenceAsFragmentAnnotator(lap);
			FragmentAnnotator fragAnn = new KeywordBasedFixedLengthFragmentAnnotator(lap, 6);
			
			EvaluationMeasures eval = ModifierAnnotatorEvaluator.evaluateModifiers(
			
					"src/main/resources/exci/alma/xmi_perFragmentGraph",
//					"src/main/resources/exci/nice/xmi_perFragmentGraph/all",

					new AdvAsModifierAnnotator(lap, fragAnn, false),
//					new AdvAdjAsModifierAnnotator(lap, fragAnn, false),
//					new AdvAdjPPAsModifierAnnotator(lap, fragAnn, false),
//					new AdvAdjPPAsModifierAnnotator(lap, fragAnn, true),
					
					fragAnn,
					lap,
					
					true   // true means it will use the gold standard fragment annotations
					);
			
			logger.info(eval.toString());
			
		} catch (LAPException e) {
			logger.error("Error creating the LAP object");
			e.printStackTrace();
		} catch (ModifierAnnotatorException e) {
			logger.error("Error creating modifier annotator object");
			e.printStackTrace();
		} catch (FragmentAnnotatorException e) {
			logger.error("Error creating fragment annotator object");
			e.printStackTrace();
		}catch (IOException e) {
			logger.error("I/O error");
			e.printStackTrace();
		} 
		
	}
}

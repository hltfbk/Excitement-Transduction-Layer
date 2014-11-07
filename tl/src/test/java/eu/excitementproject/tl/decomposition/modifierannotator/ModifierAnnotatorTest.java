package eu.excitementproject.tl.decomposition.modifierannotator;

import static org.junit.Assert.fail;

import java.io.File;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Ignore;
import org.junit.Test;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.KeywordBasedFixedLengthFragmentAnnotator;
import eu.excitementproject.tl.laputils.AnnotationUtils;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.DependencyLevelLapEN;
import eu.excitementproject.tl.laputils.DependencyLevelLapIT;
import eu.excitementproject.tl.structures.Interaction;

/**
 * This small unit test tests two simple baseline implementations. 
 * 
 * <P>
 * <LI> KeywordsAsFragmentAnnoator 
 * <LI> AdvAsModifierAnnotator
 * 
 * <P>
 * 
 * @author vivi@fbk 
 *
 */
@SuppressWarnings("unused")
public class ModifierAnnotatorTest {

	@Ignore 
			// added by Gil. Slight parser change in EOP LAP 1.1.3 makes a strange error. 
	        // Vivi, please take a look and re-initiated the test for us. 
	        // the error comes with 
	        //    dependency: (durch,Sprechblasen() => NK
	        // 
	        // (the above causes exception of "Unclosed group near index xx". )
	        // I guess this might have caused by LAP tokenization error...  
	        // Maybe exception treatment on "parenthesis" should changed... 
	        // For now we can't fix LAP side yet, so, make something to patch such cases... 
	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.decomposition.modifierannotator.test"); 
		
		try {
//			LAPAccess lap = new DependencyLevelLapIT(); 			
			LAPAccess lap = new DependencyLevelLapEN(); 			

			FragmentAnnotator frAnnot = new KeywordBasedFixedLengthFragmentAnnotator(lap); 

//			ModifierAnnotator modAnnot = new AdvAsModifierAnnotator(lap);
//			ModifierAnnotator modAnnot = new AdvAdjAsModifierAnnotator(lap, true);
			ModifierAnnotator modAnnot = new AdvAdjPPAsModifierAnnotator(lap, true); 


//			File xmiIn = new File("target/WP2_public_data_CAS_XMI/ALMA_social_media/0320.txt.xmi");
//			File xmiIn = new File("target/WP2_public_data_CAS_XMI/ALMA_social_media/0202.txt.xmi");
			File xmiIn = new File("src/test/resources/WP2_public_data_CAS_XMI/NICE_reAnnotated/perInteraction/dev/EMAIL0110/11.txt.xmi");
					
			JCas goldJCas = CASUtils.createNewInputCas();
			CASUtils.deserializeFromXmi(goldJCas, xmiIn);
			lap.addAnnotationOn(goldJCas);

			// creating a new cas without the annotations from the xmi, only with keywords if there are any
			String interactionText = goldJCas.getDocumentText(); 
			String interactionLang = goldJCas.getDocumentLanguage(); 
			Interaction in = new Interaction(interactionText, interactionLang);  
			JCas aJCas = in.createAndFillInputCAS();

			AnnotationUtils.transferAnnotations(goldJCas, aJCas, KeywordAnnotation.class);
			
			testlogger.info("CAS created, now annotating fragments based on keywords");
//				frAnnot.annotateFragments(aJCas);
			((KeywordBasedFixedLengthFragmentAnnotator) frAnnot).annotateFragments(aJCas, 4);
						
			//check the annotated data
			AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertNotNull(frgIndex);
				
			testlogger.info("\t " + frgIndex.size() + " fragments annotated (after filtering based on subsumption)");
				
			for(Annotation a: frgIndex) {
//					testlogger.info(a.toString());
				testlogger.info("DETERMINED FRAGMENT: (" + a.getBegin() + " -- " + a.getEnd() + ")");
				testlogger.info("DETERMINED FRAGMENT: " + a.getCoveredText());
			}
						
			// all right, continue to add modifier. 
			testlogger.info("Calling " + modAnnot.getClass().getName() + " on the same CAS."); 
			modAnnot.annotateModifiers(aJCas);
			
		} catch (Exception e) {
			fail(e.getMessage()); 
		}
	}
}

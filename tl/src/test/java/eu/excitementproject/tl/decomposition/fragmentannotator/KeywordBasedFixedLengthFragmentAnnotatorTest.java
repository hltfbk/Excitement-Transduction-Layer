package eu.excitementproject.tl.decomposition.fragmentannotator;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;

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
import eu.excitementproject.eop.lap.dkpro.MaltParserDE;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.AnnotationUtils;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.laputils.LemmaLevelLapIT;
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
 * @author Vivi Nastase
 *
 */
public class KeywordBasedFixedLengthFragmentAnnotatorTest {


//	@Ignore
	@Test
	public void test_xmi() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator.test"); 
		
		try {
			LAPAccess lap = new LemmaLevelLapIT(); 			
			FragmentAnnotator frAnnot = new KeywordBasedFixedLengthFragmentAnnotator(lap); 


			File xmiIn = new File("src/test/resources/ALMA/XMIs/0007.txt.xmi");
			
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
			testlogger.info("Calling AdvAsModifierAnnotator on the same CAS."); 
			ModifierAnnotator modAnnot = new AdvAsModifierAnnotator(lap); 
			modAnnot.annotateModifiers(aJCas);
			
		} catch (Exception e) {
//			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Ignore
	@Test
	public void test_de() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator.test"); 
		
		try {
			LAPAccess lap = new MaltParserDE(); 			
			FragmentAnnotator frAnnot = new KeywordBasedFixedLengthFragmentAnnotator(lap); 

			File f = new File("./src/test/resources/WP2_public_data_XML/keywordAnnotations.xml"); 		

			List<Interaction> iList = InteractionReader.readInteractionXML(f); 
			testlogger.info("The test file `" + f.getPath() + "'has " + iList.size() + " interactions in it."); 

			for (int i = 0; i < iList.size(); i++) {
				Interaction one = iList.get(i); 
				testlogger.info("\tinteraction (id: " + one.getInteractionId() +") text is:" + one.getInteractionString());

				// check CAS does holds category metadata 
				JCas aJCas = one.createAndFillInputCAS(); 

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
				testlogger.info("Calling AdvAsModifierAnnotator on the same CAS."); 
				ModifierAnnotator modAnnot = new AdvAsModifierAnnotator(lap); 
				modAnnot.annotateModifiers(aJCas);
			}
			
		} catch (Exception e) {
			fail(e.getMessage()); 
		}
	}
}

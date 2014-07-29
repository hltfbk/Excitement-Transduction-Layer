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
import org.junit.Test;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.dkpro.MaltParserDE;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.InteractionReader;
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
public class KeywordBasedFragmentAnnotatorTest {

//	@Ignore 
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
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator.test"); 
		
		try {
			LAPAccess lap = new MaltParserDE(); 			
			FragmentAnnotator frAnnot = new KeywordBasedFragmentAnnotator(lap); 


			File f = new File("./src/test/resources/WP2_public_data_XML/keywordAnnotations.xml"); 		

			List<Interaction> iList = InteractionReader.readInteractionXML(f); 
			testlogger.info("The test file `" + f.getPath() + "'has " + iList.size() + " interactions in it."); 

			for (int i = 0; i < iList.size(); i++) {
				Interaction one = iList.get(i); 
				testlogger.info("\tinteraction (id: " + one.getInteractionId() +") text is:" + one.getInteractionString());

				// check CAS does holds category metadata 
				JCas aJCas = one.createAndFillInputCAS(); 

				testlogger.info("CAS created, now annotating fragments based on keywords");
				frAnnot.annotateFragments(aJCas);
						
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

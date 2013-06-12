package eu.excitementproject.tl.decomposition.fragmentannotator;

import static org.junit.Assert.*;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator; 
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;

/**
 * This small unit test tests two simple baseline implementations. 
 * 
 * <P>
 * <LI> SentenceAsFragmentAnnoator 
 * <LI> AdvAsModifierAnnotator
 * 
 * <P>
 * 
 * @author Gil 
 *
 */
public class SentenceAsFragmentAnnotatorTest {

	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator.test"); 
		
		try {
			LAPAccess lap = new TreeTaggerEN(); 			
			FragmentAnnotator frAnnot = new SentenceAsFragmentAnnotator(lap); 

			// all right, generate a CAS 
			// set three sentences, and check we have two
			// fragments. 
			testlogger.info("Calling SentenceAsFragmentAnnotator on three sentences."); 
			JCas a = CASUtils.createNewInputCas(); 
			a.setDocumentLanguage("EN"); 
			a.setDocumentText("I really enjoyed riding latest ICE. However, I was really disappointed by the delay. I missed my connection."); 
			frAnnot.annotateFragments(a); 
			
			// check the annotated data
			AnnotationIndex<Annotation> frgIndex = a.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 3); 
			
			// all right, continue to add modifier. 
			testlogger.info("Calling AdvAsModifierAnnotator on the same CAS."); 
			ModifierAnnotator modAnnot = new AdvAsModifierAnnotator(lap); 
			modAnnot.annotateModifiers(a); 
			
			// check the annotated data 
			AnnotationIndex<Annotation> modIndex = a.getAnnotationIndex(ModifierAnnotation.type);
			Assert.assertEquals(modIndex.size(), 3); // really, However, really 

			// Cool. Now check on a clean, new CAS, with two adverbs 
			a.reset(); 
			a.setDocumentLanguage("EN"); 
			a.setDocumentText("The trip was really nice. I literally fall in love with the region."); 
			modAnnot.annotateModifiers(a); 
			
			// check the annotated data 
			modIndex = a.getAnnotationIndex(ModifierAnnotation.type);
			Assert.assertEquals(modIndex.size(), 2); 

			testlogger.info("No problem observed on the test cases"); 
			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}
}

package eu.excitementproject.tl.decomposition.fragmentannotator;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.cachedlap.CachedLAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.laputils.LemmaLevelLapEN;

/**
 * This small unit test tests TokenAsFragmentAnnotator 
 * 
 * @author Aleksandra Gabryszak
 *
 */
public class TokenAsFragmentAnnotatorTest {

	@Test
	public void test() {
		Logger testlogger = Logger.getLogger(this.getClass().getName()); 
		LAPAccess lap;
		FragmentAnnotator fragAnnotator;
		AnnotationIndex<Annotation> frgIndex;
		
		try {
			/** test on German text **/
			lap = new CachedLAPAccess(new LemmaLevelLapDE());
			
			// annotate all tokens
			fragAnnotator = new TokenAsFragmentAnnotator(lap);
			testlogger.info("Annotating all tokens except punctuation in German sentence... "); 
			JCas cas = CASUtils.createNewInputCas();
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Peter will heute ihren älteren Bruder besuchen.");
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 7);
			
			/** test on English text **/
			lap = new CachedLAPAccess(new LemmaLevelLapEN());
			fragAnnotator = new TokenAsFragmentAnnotator(lap);
			
			// annotate all tokens
			cas.reset();
			testlogger.info("Annotating all tokens except punctuation in English sentence... "); 
			cas.setDocumentLanguage("EN"); 
			cas.setDocumentText("Peter really wants to visit his older brother.");
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 8);
			
		} catch (LAPException | FragmentAnnotatorException e) {
			e.printStackTrace();
		}
	}

}

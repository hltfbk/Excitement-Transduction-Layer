package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.Arrays;
import java.util.List;

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
import eu.excitementproject.tl.laputils.WordDecompositionType;

public class TokenAsFragmentAnnotatorForGermanTest {
	
	@Test
	public void test() {
		Logger testlogger = Logger.getLogger(this.getClass().getName()); 
		LAPAccess lap;
		FragmentAnnotator fragAnnotator;
		List<String> tokenPOSFilter;
		AnnotationIndex<Annotation> frgIndex;
		
		try {
			/** test on German text **/
			lap = new CachedLAPAccess(new LemmaLevelLapDE());
			JCas cas = CASUtils.createNewInputCas();
			
			// annotate all tokens, but no compound parts
			testlogger.info("Annotating all tokens except punctuation in German sentence... ");
			cas.reset();
			cas = CASUtils.createNewInputCas();
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Das Programm brachte eine Fehlermeldung .");
			fragAnnotator = new TokenAsFragmentAnnotatorForGerman(lap, WordDecompositionType.NONE);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 5);
			
			// annotate all tokens + compound parts
			testlogger.info("Annotating all tokens except punctuation + compound parts in German sentence... ");
			cas.reset();
			cas = CASUtils.createNewInputCas();
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Das Programm brachte eine Fehlermeldung .");
			fragAnnotator = new TokenAsFragmentAnnotatorForGerman(lap, WordDecompositionType.NO_RESTRICTION);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 7);
			
			
			// annotate all tokens + compound parts of words with hyphen
			testlogger.info("Annotating all tokens except punctuation + compound parts in German sentence... ");
			cas.reset();
			cas = CASUtils.createNewInputCas();
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Fehlermeldung in der XML-Datei");
			fragAnnotator = new TokenAsFragmentAnnotatorForGerman(lap, WordDecompositionType.ONLY_HYPHEN);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 6);
			
			//annotate only nouns, but no compound parts
			testlogger.info("Annotating only nouns in German sentence... "); 
			cas.reset();
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Das Programm brachte eine Fehlermeldung .");
			tokenPOSFilter = Arrays.asList(new String []{"NN"}); 
			fragAnnotator = new TokenAsFragmentAnnotatorForGerman(lap, tokenPOSFilter, WordDecompositionType.NONE);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 2);
			
			//annotate only nouns + compound parts
			testlogger.info("Annotating only nouns + compund parts in German sentence... "); 
			cas.reset();
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Das Programm brachte eine Fehlermeldung .");
			tokenPOSFilter = Arrays.asList(new String []{"NN", "NE"}); 
			fragAnnotator = new TokenAsFragmentAnnotatorForGerman(lap, tokenPOSFilter, WordDecompositionType.NO_RESTRICTION);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 4);
			
		} catch (LAPException | FragmentAnnotatorException e) {
			e.printStackTrace();
		}
	}

}

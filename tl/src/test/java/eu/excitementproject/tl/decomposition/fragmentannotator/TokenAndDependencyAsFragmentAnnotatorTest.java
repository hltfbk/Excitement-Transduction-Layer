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
import eu.excitementproject.eop.lap.dkpro.MaltParserDE;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * This small unit test tests TokenAndDependencyAsFragmentAnnoator
 * 
 * @author Aleksandra
 *
 */

public class TokenAndDependencyAsFragmentAnnotatorTest {

	@Test
	public void test() {
		Logger testlogger = Logger.getLogger(this.getClass().getName()); 
		LAPAccess lap;
		FragmentAnnotator fragAnnotator;
		List<String> tokenPOSFilter;
		List<String> dependencyFilter;
		List<String> governorPOSFilter; 
		List<String> dependentPOSFilter;
		AnnotationIndex<Annotation> frgIndex;
		
		try {
			/** test on German text **/
			lap = new CachedLAPAccess(new MaltParserDE());
			fragAnnotator = new TokenAndDependencyAsFragmentAnnotator(lap);
			
			// annotate all tokens
			testlogger.info("Annotating all tokens except punctuation and all dependencies in German sentence... "); 
			JCas cas = CASUtils.createNewInputCas();
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Peter will heute ihren älteren Bruder besuchen.");
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 13);
			
			//annotate only nouns and complete verbs as tokens and only depencencies with direct objects on a clean CAS
			testlogger.info("Annotating only nouns and complete verbs in German sentence... "); 
			cas.reset();
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Peter will heute ihren älteren Bruder besuchen");
			tokenPOSFilter = Arrays.asList(new String []{"NN", "NE", "VVFIN", "VVIMPF", "VVINF", "VVIZU", "VVPP"}); 
			dependencyFilter = Arrays.asList(new String [] {"OA"});
			governorPOSFilter = null; 
			dependentPOSFilter = null;
			fragAnnotator = new TokenAndDependencyAsFragmentAnnotator(lap, tokenPOSFilter, dependencyFilter, governorPOSFilter, dependentPOSFilter);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 4);

		} catch (LAPException | FragmentAnnotatorException e) {
			e.printStackTrace();
		}
	}

}

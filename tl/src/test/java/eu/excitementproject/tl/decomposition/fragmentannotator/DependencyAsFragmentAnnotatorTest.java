package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

/**
 * This small unit test tests DependencyAsFragmentAnnotator
 * 
 * @author Aleksandra
 *
 */
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

public class DependencyAsFragmentAnnotatorTest {
	
	@Test
	public void test() {
		Logger testlogger = Logger.getLogger(this.getClass().getName()); 
		LAPAccess lap;
		FragmentAnnotator fragAnnotator;
		List<String> dependencyFilter;
		List<String> governorPOSFilter; 
		List<String> dependentPOSFilter;
		AnnotationIndex<Annotation> frgIndex;
		
		try {
			/** test on german text **/
			lap = new CachedLAPAccess(new MaltParserDE());
			
			// annotate all dependencies 
			fragAnnotator = new DependencyAsFragmentAnnotator(lap);
			testlogger.info("Annotating all dependencies in a sentence... "); 
			JCas cas = CASUtils.createNewInputCas();
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Meine Frau besucht heute ihren älteren Bruder.");
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 6);
			
			//annotate only direct objects on a new, clean CAS
			cas.reset();
			testlogger.info("Annotating only dependencies with direct objects in German sentence... "); 
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Meine Frau besucht heute ihren älteren Bruder.");
			dependencyFilter = Arrays.asList(new String [] {"OA"});
			governorPOSFilter = null; 
			dependentPOSFilter = null;
			fragAnnotator = new DependencyAsFragmentAnnotator(lap, dependencyFilter, governorPOSFilter, dependentPOSFilter);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 1);
			
			//annotate only dependencies, in which governor is a noun
			cas.reset();
			testlogger.info("Annotating only dependencies, where governor ia a noun and dependent is an adjective..."); 
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Meine Frau besucht heute ihren älteren Bruder.");
			dependencyFilter = null;
			governorPOSFilter = Arrays.asList(new String []{"NN", "NE"});
			dependentPOSFilter = Arrays.asList(new String []{"ADJA", "ADJD"});
			fragAnnotator = new DependencyAsFragmentAnnotator(lap, dependencyFilter, governorPOSFilter, dependentPOSFilter);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 1);
			
		} catch (LAPException | FragmentAnnotatorException e) {
			e.printStackTrace();
		}
	}

}

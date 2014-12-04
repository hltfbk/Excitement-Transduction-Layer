package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.ArrayList;
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
import eu.excitementproject.tl.laputils.POSTag_DE;
import eu.excitementproject.tl.laputils.WordDecompositionType;

public class DependencyAsFragmentAnnotatorForGermanTest {
	
	@Test
	public void test() {
		Logger testlogger = Logger.getLogger(this.getClass().getName()); 
		LAPAccess lap;
		FragmentAnnotator fragAnnotator;
		List<String> dependencyFilter;
		List<POSTag_DE> governorPOSFilter;
		List<String> governorWordFilter;
		List<POSTag_DE> dependentPOSFilter;
		List<String> dependentWordFilter;
		WordDecompositionType wordDecompositionType;
		AnnotationIndex<Annotation> frgIndex;
		
		try {
			/** test on german text **/
			lap = new CachedLAPAccess(new MaltParserDE());
			JCas cas = CASUtils.createNewInputCas();
			
			// annotate all dependencies 
			fragAnnotator = new DependencyAsFragmentAnnotatorForGerman(lap, WordDecompositionType.NONE);
			testlogger.info("Annotating all dependencies in a sentence... "); 
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
			fragAnnotator = new DependencyAsFragmentAnnotatorForGerman(lap, dependencyFilter, WordDecompositionType.NONE);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 1);
			
			//annotate only dependencies, in which governor is a noun
			cas.reset();
			testlogger.info("Annotating only dependencies, where governor ia a noun and dependent is an adjective..."); 
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Meine Frau besucht heute ihren älteren Bruder.");
			governorPOSFilter = Arrays.asList(new POSTag_DE []{POSTag_DE.NN, POSTag_DE.NE});
			dependentPOSFilter = Arrays.asList(new POSTag_DE []{POSTag_DE.ADJA, POSTag_DE.ADJD});
			fragAnnotator = new DependencyAsFragmentAnnotatorForGerman(lap, governorPOSFilter, dependentPOSFilter, WordDecompositionType.NONE);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 1);
			
			//annotate only dependencies, in which governor is a noun and dependent is adjectiv or lemma "keine"
			cas.reset();
			testlogger.info("Annotating only dependencies, where governor ia a noun and dependent is an adjective or lemma of dependent is 'keine'"); 
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Der gute Peter hatte keinen Blumen gekauft.");
			governorPOSFilter = Arrays.asList(new POSTag_DE []{POSTag_DE.NN, POSTag_DE.NE});
			dependentPOSFilter = Arrays.asList(new POSTag_DE []{POSTag_DE.ADJA, POSTag_DE.ADJD});
			dependentWordFilter = Arrays.asList(new String []{"keine"});
			fragAnnotator = new DependencyAsFragmentAnnotatorForGerman(lap, governorPOSFilter, 
					new ArrayList<String>(), dependentPOSFilter, dependentWordFilter, WordDecompositionType.NONE);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 2);
			
			//annotate all dependencies + splits
			cas.reset();
			testlogger.info("Annotating only dependencies, where governor ia a noun and dependent is an adjective or lemma of dependent is 'keine'"); 
			cas.setDocumentLanguage("DE"); 
			cas.setDocumentText("Fehlermeldung des Softwareherstellers");
			governorPOSFilter = Arrays.asList(new POSTag_DE []{POSTag_DE.NN, POSTag_DE.NE});
			dependentPOSFilter = Arrays.asList(new POSTag_DE []{POSTag_DE.NN, POSTag_DE.NE});
			fragAnnotator = new DependencyAsFragmentAnnotatorForGerman(lap, governorPOSFilter, dependentPOSFilter, WordDecompositionType.NO_RESTRICTION);
			fragAnnotator.annotateFragments(cas);
			frgIndex = cas.getAnnotationIndex(DeterminedFragment.type);
			Assert.assertEquals(frgIndex.size(), 5);
			
		} catch (LAPException | FragmentAnnotatorException e) {
			e.printStackTrace();
		}
	}
	
}

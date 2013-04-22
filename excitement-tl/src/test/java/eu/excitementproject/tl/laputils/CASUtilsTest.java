package eu.excitementproject.tl.laputils;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import eu.excitement.type.tl.AssumedFragment;
import eu.excitement.type.tl.FragmentPart;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;

public class CASUtilsTest {

	@Test
	public void test() {

		try {
		// generate CAS from CASutils 
		// (testing createNewInputCas()) 
		JCas aJCas = CASUtils.createNewInputCas(); 
		assertNotNull(aJCas); 

		// set some text 
		String docText = "It was a really slow train."; 
		aJCas.setDocumentLanguage("EN");
		aJCas.setDocumentText(docText); 
		
		// make sure we can use any LAP, like TreeTaggerEN 
		// (checking that the CAS can hold/access generic LAP annotation types) 
		LAPAccess l = null; 
		l = new TreeTaggerEN(); 
		l.addAnnotationOn(aJCas); 				
		//  Dump the result... (if you want)  
		//CASUtils.dumpCAS(aJCas);  
		
		String fragText = "It was a slow train."; 
		// check that we can add TL types, too... 
		{
			AssumedFragment a = new AssumedFragment(aJCas);  
			a.setBegin(0); 
			a.setEnd(26); 			
			a.setText(fragText); 
			
			FSArray v = new FSArray(aJCas, 10);  // an Array of 10 elements ... 
			a.setFragParts(v); 
			
			// non continous region example ... 
			FragmentPart p1 = new FragmentPart(aJCas); 
			FragmentPart p2 = new FragmentPart(aJCas); 
			p1.setBegin(0); 
			p1.setEnd(7); 
			p2.setBegin(16); 
			p2.setEnd(26); 
			a.setFragParts(0, p1); 
			a.setFragParts(1, p2);
			a.addToIndexes(); 
		}
		
		// check back that there are actually in... 
		AnnotationIndex<Annotation> tokenIndex = aJCas.getAnnotationIndex(Token.type);
		Iterator<Annotation> tokenIter = tokenIndex.iterator(); 
		
		// check number of tokens correct ... 
		int numToken = 0; 
		while(tokenIter.hasNext())
		{
			tokenIter.next(); 
			numToken++; 
		}
		assertEquals(numToken,7); 
		
		// check fragment annotation, 
		AnnotationIndex<Annotation> fragIndex = aJCas.getAnnotationIndex(AssumedFragment.type);
		Iterator<Annotation> fragIter = fragIndex.iterator(); 		
		
		AssumedFragment af = (AssumedFragment) fragIter.next(); 
		
		assertEquals(fragText, af.getText()); 
		assertEquals(0, af.getFragParts(0).getBegin()); 
		assertEquals(7, af.getFragParts(0).getEnd());
		assertEquals(16, af.getFragParts(1).getBegin()); 
		assertEquals(26, af.getFragParts(1).getEnd());
		
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}

}

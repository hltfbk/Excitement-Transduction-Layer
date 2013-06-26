package eu.excitementproject.tl.laputils;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger; 
import org.apache.log4j.BasicConfigurator; 

import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitement.type.tl.AssumedFragment;
import eu.excitement.type.tl.FragmentPart;
import eu.excitement.type.tl.Metadata;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
public class CASUtilsTest {

	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO);  // for UIMA (hiding < INFO) 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.laputils"); 
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
		
		// testing for CASUtils.annotateOneAssumedFragment 
		try {
			JCas aJCas = CASUtils.createNewInputCas();
			//                0123456789012345678901234567890123456
			String docText = "abcd this is, actually, fragment. zzz"; 
			aJCas.setDocumentLanguage("EN");
			aJCas.setDocumentText(docText); 
			CASUtils.Region[] r = new CASUtils.Region[2]; 
			r[0] = new CASUtils.Region(5,12);
			r[1] = new CASUtils.Region(24,33);
			CASUtils.annotateOneAssumedFragment(aJCas, r); 
			//CASUtils.dumpCAS(aJCas); 
			
			// Okay. now aJCas has one assumed fragment annotated. check this 
			AnnotationIndex<Annotation> fragIndex = aJCas.getAnnotationIndex(AssumedFragment.type);
			Iterator<Annotation> fragIter = fragIndex.iterator(); 		
			
			AssumedFragment af = (AssumedFragment) fragIter.next(); 
			testlogger.info(af.getText()); 
			testlogger.info(af.getFragParts(0).getBegin()); 
			testlogger.info(af.getFragParts(1).getEnd()); 
		
			// 
			// testing for serialization / deserialization 
			File xmiOut = new File("./target/testout.xmi"); 
			CASUtils.serializeToXmi(aJCas, xmiOut); 
			JCas jcas2 = CASUtils.createNewInputCas(); 
			CASUtils.deserializeFromXmi(jcas2, xmiOut); 
			Assert.assertEquals(jcas2.getDocumentText(),aJCas.getDocumentText()); 
			//CASUtils.dumpCAS(jcas2); 
			CASUtils.dumpAnnotationsInCAS(aJCas, AssumedFragment.type); 

		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
		
		// testing for generate examples 
		try {
			CASUtils.generateExamples(); 
			// load one of the example? 
			File xmiIn = new File("./target/CASInput_example_4.xmi"); 
			JCas anotherJCas = CASUtils.createNewInputCas(); 
			CASUtils.deserializeFromXmi(anotherJCas, xmiIn); 
			//CASUtils.dumpCAS(anotherJCas);
			//System.out.println(anotherJCas.getDocumentText()); 
			//testlogger.info(anotherJCas.getDocumentText()); 
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
		
		// testing for metadata annotation 
		try {
//			- interactionId 
//			- channel 
//			- provider 
//			- date (string as YYYY-MM-DD)  
//			- businessScenario 
//			- author
			JCas aJCas = CASUtils.createNewInputCas(); 
			aJCas.setDocumentLanguage("EN"); 
			aJCas.setDocumentText("test document."); 
			CASUtils.Region[] r = new CASUtils.Region[1]; 
			r[0] = new CASUtils.Region(0,12);
			CASUtils.annotateOneAssumedFragment(aJCas, r); 
			
			String interactionId = "Heidelberg.1"; 
			String channel = "e-mail"; 
			String provider = "HEICL"; 
			String date = "2013-06-12"; 
			String businessScenario = null; 
			String author = null; 
			String category = null; 
		
			CASUtils.addTLMetaData(aJCas, interactionId, channel, provider, date, businessScenario, author, category); 
			File f = new File("./target/metatest.xmi"); 
			CASUtils.serializeToXmi(aJCas, f);
			
			// reread and run getMetadata 
			aJCas.reset(); 
			CASUtils.deserializeFromXmi(aJCas, f); 
			Metadata m = CASUtils.getTLMetaData(aJCas); 
			
			Assert.assertEquals(m.getInteractionId(), interactionId); 
			Assert.assertEquals(m.getChannel(), channel); 
			Assert.assertEquals(m.getProvider(), provider); 
			Assert.assertEquals(date, m.getDate());  
			Assert.assertEquals(businessScenario, m.getBusinessScenario());
			Assert.assertEquals(author,  m.getAuthor()); 
			
			// only metadata will be printed 
			CASUtils.dumpAnnotationsInCAS(aJCas, Metadata.type); 
			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
		
		
	}
}

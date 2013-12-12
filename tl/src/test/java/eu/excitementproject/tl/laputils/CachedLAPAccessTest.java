package eu.excitementproject.tl.laputils;

import static org.junit.Assert.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.PlatformCASProber;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;

@SuppressWarnings("unused")
public class CachedLAPAccessTest {

	@Test
	public void test() {

		// Set Log4J 
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO);  // for UIMA (hiding < INFO) 
		
		// prepare one underlying LAP, say, TreeTagger 
		LAPAccess underlyingLAP = null; 
		CachedLAPAccess cachedLAP = null; 
		
		// here's our text and hypothesis. 
		String text = "This is a pipe."; 
		String hypo = "Holy, this is not a pipe!"; 
		
		try {
			underlyingLAP = new LemmaLevelLapEN(); // tree tagger 
			//underlyingLAP = new MaltParserEN(); 
			cachedLAP = new CachedLAPAccess(underlyingLAP); // and cached LAP that works with this LAP
		}
		catch(Exception e)
		{
			fail(e.getMessage()); 
		}

		
		//
		// here's the usage. 
		//
		
		// First, "previously", how you used LAP without cache. 
		JCas originalCAS = null; 
		try {
			originalCAS = underlyingLAP.generateSingleTHPairCAS(text, hypo); 
			// as you see in this call, it is the LAP that generates 
			// one CAS and gives you back. 
			// making a new CAS takes some time (10 - 20 ms?) 
			// So, if we can skip 
			// by providing already existing CAS, it would be better.
			// next example shows how you can do this. 
		}
		catch(Exception e)
		{
			fail(e.getMessage()); 
		}
		
		// here's the "new usage" 
		// 
		// First, you need one CAS that will be used again and again for 
		// the cachedLAP. We reuse it all the time. 		
		JCas workJCas = null; 
		try 
		{
			workJCas = CASUtils.createNewInputCas(); 	
		}
		catch(Exception e)
		{
			fail(e.getMessage()); 
		}
		
		// Okay. we have one CAS. 
		// now, we ask nicely to the cachedLAP to use this CAS 
		// to annotate text and hypothesis. 
		// and the LAP do not make a new CAS. 
		try {
			// note that you don't need to .reset() the CAS. The first thing that 
			// this cachedLAP does is "reset()" that CAS to make it a empty, clean CAS. 
			cachedLAP.annotateSingleTHPairCAS(text, hypo, workJCas); 
		}
		catch(Exception e)
		{
			fail(e.getMessage()); 
		}
		
		//
		// usage example ENDS 
		//
		
		
		//
		// Some test codes 
		
		JCas cachedCAS = null; 
		// caching test 
		try {
			cachedCAS = cachedLAP.generateSingleTHPairCAS(text, hypo);
			cachedCAS = cachedLAP.generateSingleTHPairCAS(hypo, text);
			cachedCAS = cachedLAP.generateSingleTHPairCAS(hypo, text);
			originalCAS = underlyingLAP.generateSingleTHPairCAS(hypo, text); 
			PlatformCASProber.probeCas(originalCAS,  System.out);
			PlatformCASProber.probeCas(cachedCAS,  System.out);
			//PlatformCASProber.probeCasAndPrintContent(originalCAS, System.out); 
			//PlatformCASProber.probeCasAndPrintContent(cachedCAS, System.out); 			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}	
		
		//  meaningless, but well. can't resist.   
		try {
			JCas a = CASUtils.createNewInputCas(); 

			for(int i=0; i < 3000; i++)
			{
				//underlyingLAP.generateSingleTHPairCAS(hypo, text); // with TREETAGGER EN 22.5 seconds 
				//cachedLAP.generateSingleTHPairCAS(hypo, text); // TREETAGGER EN 18.118
				cachedLAP.annotateSingleTHPairCAS(hypo, text, a); // TREETAGGER EN 4.687 
			}
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
		
	}

}

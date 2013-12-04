package eu.excitementproject.tl.laputils;

import static org.junit.Assert.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.PlatformCASProber;

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
		
		try {
			underlyingLAP = new LemmaLevelLapEN(); 
			cachedLAP = new CachedLAPAccess(underlyingLAP); 
		}
		catch(Exception e)
		{
			fail(e.getMessage()); 
		}
		
		// now we have original LAP "underlyingLAP" and 
		// cached (wrappered) one 
		JCas originalCAS = null; 
		JCas cachedCAS = null; 
		String text = "This is a pipe."; 
		String hypo = "Holy, this is not a pipe!"; 
		try {
			originalCAS = underlyingLAP.generateSingleTHPairCAS(text, hypo); 
			cachedCAS = cachedLAP.generateSingleTHPairCAS(text, hypo); 
		}
		catch(Exception e)
		{
			fail(e.getMessage()); 
		}
		// Prover
		try {
			PlatformCASProber.probeCas(originalCAS, System.out);
			PlatformCASProber.probeCas(cachedCAS, System.out);
			// even dump? 
			//PlatformCASProber.probeCasAndPrintContent(originalCAS, System.out); 
			//PlatformCASProber.probeCasAndPrintContent(cachedCAS, System.out); 			
		}
		catch(Exception e)
		{
			fail(e.getMessage()); 
		}	
		
		// caching test 
		try {
			cachedCAS = cachedLAP.generateSingleTHPairCAS(text, hypo);
			cachedCAS = cachedLAP.generateSingleTHPairCAS(hypo, text);
			cachedCAS = cachedLAP.generateSingleTHPairCAS(hypo, text);
			originalCAS = underlyingLAP.generateSingleTHPairCAS(hypo, text); 
			//PlatformCASProber.probeCasAndPrintContent(originalCAS, System.out); 
			//PlatformCASProber.probeCasAndPrintContent(cachedCAS, System.out); 			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}	
		
		
		//  meaningless, but well. can't resist.   
		try {
			for(int i=0; i < 1000; i++)
			{
				JCas a = CASUtils.createNewInputCas(); 
				//underlyingLAP.generateSingleTHPairCAS(hypo, text); // 91.02
				cachedLAP.annotateSingleTHPairCAS(hypo, text, a); // 67.564 
				//cachedLAP.generateSingleTHPairCAS(hypo, text); // 
			}
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}
	}

}

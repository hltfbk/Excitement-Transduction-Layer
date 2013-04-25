package  eu.excitementproject.tl.laputils;

import java.io.File;
import java.io.InputStream;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.PlatformCASProber;

/**
 * The class holds various small utility static methods that might be 
 * useful in handling CASes: Like getting a new CAS, generate a new 
 * CAS by copying the provided CAS, etc. 
 * 
 * @author Gil 
 */

// TODO: set typepath convention for UIMAFit based CAS generation. 
// TODO: Check UIMAFit CASUtil, and wrap some needed things.  

public final class CASUtils {
	/**
	 * <P>
	 * This method generates a new JCas and returns it. 
	 * The resulting CAS is empty, and holds nothing. No text, no language id, etc. The caller has to set them up. 
	 * <P>
	 * Note: Generally, making a new CAS is not really a good thing to do. It is a heavy and big object. If you can work on CAS sequentially, you should do that. This means that, using one CAS, if the work is done, reset the CAS (calling .reset()), and set a new document on the CAS, etc. The following is from UIMA JavaDoc.
	 * <P><I>
	 * Important: CAS creation is expensive, so if at all possible an application should reuse CASes. When a JCas instance is no longer being used, call its JCas.reset() method, which will remove all prior analysis information, and then reuse that same JCas instance for another call to process(JCas). </I>
	 * @return JCas that can express/hold all CAS types that is known to EOP & TL Layer 
	 */
	static public JCas createNewInputCas() throws LAPException
	{
		JCas a = null; 
		AnalysisEngine typeAE = null; 
		try {
			InputStream s = CASUtils.class.getResourceAsStream("/desc/TLDummyAE.xml"); // This AE does nothing, but holding all types.
			XMLInputSource in = new XMLInputSource(s, null); 
			ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);		
			typeAE = UIMAFramework.produceAnalysisEngine(specifier); 
		} 
		catch (InvalidXMLException e)
		{
			throw new LAPException("AE descriptor is not a valid XML", e);			
		}
		catch (ResourceInitializationException e)
		{
			throw new LAPException("Unable to initialize the AE", e); 
		}		

		try {
			a = typeAE.newJCas(); 
		}
		catch (ResourceInitializationException e)
		{
			throw new LAPException("Unable to create new JCas", e); 
		}
		
		return a; 
	}
	
	/**
	 *  This method dumps the content of CAS to Console (or log, if available) 
	 *  
	 */
	static public void dumpCAS(JCas aJCas)
	{
		PlatformCASProber.printAnnotations(aJCas.getCas(), System.out); 
		// TODO, dump to log, instead of console. 
	}

	/**
	 * This static method serializes the given JCAS into an XMI file . 
	 * 
	 * @param JCas aJCas: the JCas to be serialized 
	 * @param File f: file path, where XMI file will be written 
	 */
	static public void serializeCAS(JCas aJCas, File f)
	{
		// TODO write body
	}
	
	/**
	 * This static method loads a serialized XMI file and fill up the JCAS. 
	 * Note that this method will first clear (by calling .reset()) the given CAS, and will fill it with given File, assuming the File is an XMI-zed CAS. If not, it will raise an exception. 
	 * 
	 * @param aJCas
	 * @param f
	 */
	static public void loadXmi(JCas aJCas, File f)
	{
		// TODO write body 
		
	}
	
}

package  eu.excitementproject.tl.laputils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLSerializer;
import org.xml.sax.SAXException;

import eu.excitement.type.tl.AssumedFragment;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.FragmentPart;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.PlatformCASProber;

/**
 * The class holds various small utility static methods that might be 
 * useful in handling CASes: Like getting a new CAS, generate a new 
 * CAS by copying the provided CAS, etc. 
 * 
 * @author Gil 
 */


// Things to be considered as future improvements 
// TODO: [#C] set typepath convention for UIMAFit based CAS generation. 
// TODO: [#C] Check UIMAFit CASUtil, and wrap some needed things.  

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
	}

	/**
	 * This static method serializes the given JCAS into an XMI file. 
	 * 
	 * @param JCas aJCas: the JCas to be serialized 
	 * @param File f: file path, where XMI file will be written 
	 */
	static public void serializeToXmi(JCas aJCas, File f) throws LAPException 
	{
		// Serializing formula.
		try {
			FileOutputStream out; 
			out = new FileOutputStream(f);
			XmiCasSerializer ser = new XmiCasSerializer(aJCas.getTypeSystem());
			XMLSerializer xmlSer = new XMLSerializer(out, false);
			ser.serialize(aJCas.getCas(), xmlSer.getContentHandler());
			out.close();
		}
		catch (FileNotFoundException e) 
		{
			throw new LAPException("Unable to open the file for the serialization", e); 
		}
		catch(IOException e)
		{
			throw new LAPException("IOException while closing the serialization file ", e);
		}
		catch(SAXException e)
		{
			throw new LAPException("Failed in generating XML for the serialization", e);
		}
	}
	
	/**
	 * This static method loads a serialized XMI file and fill up the JCAS. 
	 * Note that this method will first clear (by calling .reset()) the given CAS, and will fill it with given File, assuming the File is an XMI-zed CAS. If not, it will raise an exception. 
	 * 
	 * @param aJCas
	 * @param f
	 */
	static public void deserializeFromXmi(JCas aJCas, File f) throws LAPException
	{
		aJCas.reset(); 
		
		try {
			FileInputStream inputStream = new FileInputStream(f);
			XmiCasDeserializer.deserialize(inputStream, aJCas.getCas());
			inputStream.close();
		}
		catch(FileNotFoundException e)
		{
			throw new LAPException("Unable to open file for deserialization", e); 
		}
		catch(IOException e)
		{
			throw new LAPException("IOException happenes while accessing XMI file",e); 
		}
		catch(SAXException e)
		{
			throw new LAPException("XML parsing failed while reading XMI file", e); 
		}
		
	}
	
	/**
	 * This static method gets one JCAS, and an array of (begin, end) tuple (in forms of CASUtils.Region) 
	 * and annotate those regions with "AssumedFragment"
	 * The method is provided to easily annotate and generate some CAS without really concerning about CAS internals. 
	 * 
	 * <P> 
	 * Note that this method annotates only "one" fragment, that may have multiple (maybe non continuous) sub areas.
	 * This means that Region[] r is treated as "FragmentPart". See Fragment Annotation Type definition (TLFragment.xml) for more detail. 
	 * 
	 * @param aJCas
	 * @param r
	 */
	static public void annotateOneAssumedFragment(JCas aJCas, Region[] r ) throws LAPException
	{
		// we will blindly follow the given region and add annotation. 
		
		int leftmost = r[0].getBegin(); 
		int rightmost = r[(r.length -1)].getEnd(); 

		AssumedFragment af = new AssumedFragment(aJCas); 
		af.setBegin(leftmost);
		af.setEnd(rightmost); 
		FSArray v = new FSArray(aJCas, r.length); 
		af.setFragParts(v); 

		String fragText=""; 
		// Generate fragment parts 
		for (int i=0; i < r.length; i++)
		{		
			FragmentPart p = new FragmentPart(aJCas); 
			p.setBegin(r[i].getBegin()); 
			p.setEnd(r[i].getEnd());
			af.setFragParts(i, p); 
			fragText = fragText + p.getCoveredText() + " ";  
		}
		// get covered texts from those parts 
		af.setText(fragText); 
		af.addToIndexes(); 
		
		Logger l = Logger.getLogger("eu.excitementproject.tl.laputils"); 
		l.info("Generated an AssummedFragment annotation. Fragment text is: " + fragText); 
		
	}
	
	/**
	 * This static method gets one JCAS, and an array of (begin, end) tuple (in forms of CASUtils.Region) 
	 * and annotate those regions with "DeterminedFragment"
	 * The method is provided to easily annotate and generate some CAS without really concerning about CAS internals. 
	 * 
	 * <P> 
	 * Note that this method annotates only "one" fragment, that may have multiple (maybe non continuous) sub areas.
	 * This means that Region[] r is treated as "FragmentPart". See Fragment Annotation Type definition (TLFragment.xml) for more detail. 
	 * 
	 * @param aJCas
	 * @param r
	 */
	static public void annotateOneDeterminedFragment(JCas aJCas, Region[] r ) throws LAPException
	{
		// we will blindly follow the given region and add annotation. 
		
		int leftmost = r[0].getBegin(); 
		int rightmost = r[(r.length -1)].getEnd(); 

		DeterminedFragment df = new DeterminedFragment(aJCas); 
		df.setBegin(leftmost);
		df.setEnd(rightmost); 
		FSArray v = new FSArray(aJCas, r.length); 
		df.setFragParts(v); 

		String fragText=""; 
		// Generate fragment parts 
		for (int i=0; i < r.length; i++)
		{		
			FragmentPart p = new FragmentPart(aJCas); 
			p.setBegin(r[i].getBegin()); 
			p.setEnd(r[i].getEnd());
			df.setFragParts(i, p); 
			fragText = fragText + p.getCoveredText() + " ";  
		}
		// get covered texts from those parts 
		df.setText(fragText); 
		df.addToIndexes(); 
		
		Logger l = Logger.getLogger("eu.excitementproject.tl.laputils"); 
		l.info("Generated a DeterminedFragment annotation. Fragment text is: " + fragText); 
		
	}

	static public void annotateOneModifier(JCas aJCas, Region[] r, ModifierAnnotation dependOn) throws LAPException 
	{
		// TODO write the code 
	}
	
	static public void annotateOneModifier(JCas aJCas, Region[] r) throws LAPException
	{
		annotateOneModifier(aJCas, r, null); 
	}
	
	
	
	/**
	 * An inner class that simply holds "begin" and "end" as int. 
	 * The class is used as an argument on some of the methods. 
	 */
	public static class Region 
	{
		public Region(int begin, int end)
		{
			this.begin = begin; 
			this.end = end; 
		}
		public int getBegin()
		{
			return begin; 
		}
		public int getEnd()
		{
			return end; 
		}
		
		private final int begin; 
		private final int end; 
	}
}

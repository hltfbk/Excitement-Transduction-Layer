package eu.excitementproject.tl.laputils;

import java.io.File;
import java.util.List;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.tl.structures.Interaction;

/**
 * This class provides simple reader for WP2 defined Interaction XML file.
 * 
 * @author Gil
 *
 */
public final class InteractionReader {

	/**
	 * This static utility method gets one File, and read in the interactions written in the file as a list of {@link Interaction} object. 
	 *  
	 * @param f XML file (WP2-defined XML file) for Interaction. 
	 * @return List<Interaction> - A list of interaction objects.  
	 */
	public static List<Interaction> readInteractionXML(File f) throws Exception
	{
		// TODO: fill in 
		return null; 
	}
	
	/**
	 * 
	 * 
	 * @param interactionText
	 * @param graphsInXML
	 * @return
	 * @throws Exception
	 */
	public static JCas readWP2FragGraphDump(File interactionText, File graphsInXML) throws Exception
	{
		// TODO: fill in 
		return null; 
	}
	
}

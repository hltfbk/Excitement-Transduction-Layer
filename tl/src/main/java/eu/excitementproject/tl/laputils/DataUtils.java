package eu.excitementproject.tl.laputils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.FileUtils;

import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.structures.Interaction;

/**
 * collection of static methods for loading various types of data (full XMIs, XMIs without (fragment/modifier annotations), XMLs)
 * 
 * @author vivi@fbk
 *
 */
public class DataUtils {

	
	/**
	 * Load the given number of XMI files from the given input directory 
	 * 
	 * @param dataDir -- directory with input XMI files (with annotations)
	 * @param fileNumberLimit
	 * 
	 * @return a list of CAS objects corresponding to the input data
	 */
	public static List<JCas> loadData(String dataDir, int fileNumberLimit) {
		
		System.out.println("Loading data from " + dataDir);
		
		List<JCas> docs = new ArrayList<JCas>();
		File dir = new File(dataDir);

		JCas aJCas;

		try {
			int i =0;
			for (File f : FileUtils.getFiles(dir,true)) {
				System.out.println("processing file " + f.getName());
				i++; 
				if (i>fileNumberLimit) break;

				aJCas = CASUtils.createNewInputCas(); 
				CASUtils.deserializeFromXmi(aJCas, f); 
				docs.add(aJCas);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docs;
	}
	
	
	/**
	 * Create a CAS with only the text and keyword annotations (if any exist)
	 * 
	 * @param dataDir -- directory with input XMI files
	 * @param fileNumberLimit
	 * 
	 * @return the list of CAS object corresponding to the input data, 
	 *  containing only the interaction text and possibly keyword annotations (no fragments, no modifiers)
	 */
	
	public static List<JCas> loadDataNoAnnot(String dataDir, int fileNumberLimit) {
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.laputils.DataUtils:loadDataNoAnnot");
		logger.setLevel(Level.INFO);
		
		List<JCas> docs = new ArrayList<JCas>();
		File dir = new File(dataDir);

		try {
			int i =0;
			for (File f : FileUtils.getFiles(dir, true)) {
				logger.info("Processing file " + f.getName());
				i++; 
				if (i>fileNumberLimit) break;
				
				JCas goldJCas = CASUtils.createNewInputCas(); 
				CASUtils.deserializeFromXmi(goldJCas, f); 
				
				String interactionText = goldJCas.getDocumentText(); 
				String interactionLang = goldJCas.getDocumentLanguage(); 
				Interaction in = new Interaction(interactionText, interactionLang);  
				JCas aJCas = in.createAndFillInputCAS();

				AnnotationUtils.transferAnnotations(goldJCas, aJCas, KeywordAnnotation.class);
				
				docs.add(aJCas);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return docs;
	}
	
	

	/**
	 * Load XML-style data (used mostly for OMQ data)
	 * 
	 * @param dataDir -- directory with input XML files
	 *  
	 * @return a set of Interactions corresponding to the input files
	 */
	public static Set<Interaction> loadXMLData(String dataDir) {

		Set<Interaction> docs = new HashSet<Interaction>();
		File dir = new File(dataDir);
		
		for (File f: FileUtils.getFiles(dir,true) ) {
			try {
				docs.addAll(InteractionReader.readInteractionXML(f));
			} catch (DataReaderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return docs;
	}

	
	/**
	 * Load XML-style data
	 * 
	 * @param files -- an array of input XML files
	 * 
	 * @return a set of Interactions corresponding to the input files
	 */
	public static Set<Interaction> loadXMLData(String[] files) {

		Set<Interaction> docs = new HashSet<Interaction>();	
		File f;
		
		for (String name: files) {

			f = new File(name); 
			try {
				docs.addAll(InteractionReader.readInteractionXML(f));
			} catch (DataReaderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		return docs;
	}

}

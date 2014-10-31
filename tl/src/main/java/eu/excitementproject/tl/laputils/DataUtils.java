package eu.excitementproject.tl.laputils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.structures.Interaction;

public class DataUtils {

	
	public static List<JCas> loadData(String dataDir, int fileNumberLimit) {
		
		System.out.println("Loading data from " + dataDir);
		
		List<JCas> docs = new ArrayList<JCas>();
		File dir = new File(dataDir);
	//	int fileNumberLimit = 4; //commented by Lili 30.06 - now exposed in the constructor

		//File f;
		JCas aJCas;

		try {
			int i =0;
			for (File f : dir.listFiles()) {
				System.out.println("processing file " + f.getName());
				i++; 
				if (i>fileNumberLimit) break;
				//		f = new File(name); 
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
	 * @param dataDir
	 * @param fileNumberLimit
	 * @return
	 */
	
	public static List<JCas> loadDataNoAnnot(String dataDir, int fileNumberLimit) {
		
		List<JCas> docs = new ArrayList<JCas>();
		File dir = new File(dataDir);
	//	int fileNumberLimit = 4; //commented by Lili 30.06 - now exposed in the constructor

		//File f;

		try {
			int i =0;
			for (File f : dir.listFiles()) {
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
	
	
	
	public static Set<Interaction> loadXMLData(String dataDir) {

		Set<Interaction> docs = new HashSet<Interaction>();
		File dir = new File(dataDir);
		
		for (File f: dir.listFiles() ) {
			try {
				docs.addAll(InteractionReader.readInteractionXML(f));
			} catch (DataReaderException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return docs;
	}

	
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

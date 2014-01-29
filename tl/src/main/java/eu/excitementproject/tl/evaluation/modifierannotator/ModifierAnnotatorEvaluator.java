package eu.excitementproject.tl.evaluation.modifierannotator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.modifierannotator.AbstractModifierAnnotator;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.evaluation.utils.FragmentAndModifierMatchCounter;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.Interaction;

public class ModifierAnnotatorEvaluator {

	public static EvaluationMeasures evaluateModifiers(String xmiDir, String modifierAnnotator, String language) 
			throws LAPException, ModifierAnnotatorException, IOException{
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator");
		logger.setLevel(Level.INFO);
		
		logger.info("Starting processing : \n\tdir: " + xmiDir + "\n\tmodifier annotator: " + modifierAnnotator + "\n\tlanguage: " + language);
		
		LAPAccess lap = initializeLAP(language);
		AbstractModifierAnnotator modAnnot = initializeAnnotator(modifierAnnotator, lap);
		List<Integer> counts = new ArrayList<Integer>(Arrays.asList(0,0,0,0)); // TP, FP, TN, FN
		
		for(File xmiIn: FileUtils.listFiles(new File(xmiDir), new String[]{"xmi"}, false)) {
			
			logger.info("Processing " + xmiIn.getName());
			// 1st. load the gold annotations from the xmi
			JCas goldJCas = CASUtils.createNewInputCas();
			CASUtils.deserializeFromXmi(goldJCas, xmiIn);
			// It has Mod & Frag annots, but doesn't have tokens at all!
			// we need to add them. 
			
			// check if the CAS covers text (e.g. nice_email_@ 108849.txt.xmi)
			if ( goldJCas != null && 
					goldJCas.getDocumentText() != null && 
					! goldJCas.getDocumentText().isEmpty() && 
					goldJCas.getDocumentText().length() > 0) {
				lap.addAnnotationOn(goldJCas);
			
				// 2nd. prepare a system output, by making a interaction ...
				String interactionText = goldJCas.getDocumentText();
				String interactionLang = goldJCas.getDocumentLanguage();
				Interaction in = new Interaction(interactionText, interactionLang);  
				JCas sysJCas = in.createAndFillInputCAS();
				modAnnot.annotateModifiers(sysJCas); 
			
				counts = addScores(counts, FragmentAndModifierMatchCounter.countModifierCounts(sysJCas, goldJCas));
			}
		}
		
		logger.info("Final counts: " + counts.toString());
		
		return new EvaluationMeasures(counts);
	}
	
	
	private static List<Integer> addScores(List<Integer> counts, List<Integer> modCounts) {
				
		return new ArrayList<Integer>(Arrays.asList(counts.get(0) + modCounts.get(0),
													counts.get(1) + modCounts.get(1),
													counts.get(2) + modCounts.get(2),
													counts.get(3) + modCounts.get(3)));
	}


	private static AbstractModifierAnnotator initializeAnnotator(
			String modifierAnnotator, LAPAccess lap) {
		AbstractModifierAnnotator modAnnot = null;
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator:initializeModifierAnnotator");
		logger.setLevel(Level.INFO);
		
		try {
			Class<?> modAnnotClass = Class.forName(modifierAnnotator);
			Constructor<?> modAnnotClassConstructor = modAnnotClass.getConstructor(LAPAccess.class);
			modAnnot = (AbstractModifierAnnotator) modAnnotClassConstructor.newInstance(lap);
			
			logger.info("Modifier instantiated from class: " + modifierAnnotator);
			
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Error initializing modifier annotator : " + e.getClass());
			e.printStackTrace();
		}	
		
		return modAnnot;
	}

	
	
	protected static LAPAccess initializeLAP(String language){
		
		String lapClassName = "eu.excitementproject.tl.laputils.LemmaLevelLap" + language.toUpperCase();
		LAPAccess lap = null;
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator:initializeLAP");
		logger.setLevel(Level.INFO);
		
		try {
			Class<?> lapClass = Class.forName(lapClassName);
			Constructor<?> lapClassConstructor = lapClass.getConstructor();
			lap = (LAPAccess) lapClassConstructor.newInstance();
			
			logger.info("LAP initialized from class : " + lapClassName);
			
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Error initializing LAP : " + e.getClass());
			e.printStackTrace();
		}
		
		return lap;
	}
}

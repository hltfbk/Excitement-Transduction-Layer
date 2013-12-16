package eu.excitementproject.tl.evaluation.fragmentannotator;

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
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.AbstractFragmentAnnotator;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.evaluation.utils.FragmentAndModifierMatchCounter;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.Interaction;

public class FragmentAnnotatorEvaluator {

	public static EvaluationMeasures evaluateFragments(String xmiDir, String fragmentAnnotator, String language) 
			throws LAPException, FragmentAnnotatorException, IOException{
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.FragmentAnnotatorEvaluator");
		logger.setLevel(Level.INFO);
		
		logger.info("Starting processing : \n\tdir: " + xmiDir + "\n\tmodifier annotator: " + fragmentAnnotator + "\n\tlanguage: " + language);
		
		LAPAccess lap = initializeLAP(language);
		AbstractFragmentAnnotator fragAnnot = initializeAnnotator(fragmentAnnotator, lap);
		List<Integer> counts = new ArrayList<Integer>(Arrays.asList(0,0,0,0)); // TP, FP, TN, FN
		
		for(File xmiIn: FileUtils.listFiles(new File(xmiDir), new String[]{"xmi"}, false)) {
			
			logger.info("Processing " + xmiIn.getName());
			// 1st. load the gold annotations from the xmi
			JCas goldJCas = CASUtils.createNewInputCas();
			CASUtils.deserializeFromXmi(goldJCas, xmiIn);
			// It has Mod & Frag annots, but doesn't have tokens at all!
			// we need to add them. 
			lap.addAnnotationOn(goldJCas);
			
			// 2nd. prepare a system output, by making a interaction ...
			String interactionText = goldJCas.getDocumentText();
			String interactionLang = goldJCas.getDocumentLanguage();
			
		// how to add the keywords here? we have no xmis for data that has keywords ... 
//			String interactionKeywords = ... ;
			
			Interaction in = new Interaction(interactionText, interactionLang);  
			JCas sysJCas = in.createAndFillInputCAS();
			fragAnnot.annotateFragments(sysJCas); 
			
			counts = addScores(counts, FragmentAndModifierMatchCounter.countFragmentCounts(goldJCas, sysJCas));
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


	private static AbstractFragmentAnnotator initializeAnnotator(
			String fragmentAnnotator, LAPAccess lap) {
		AbstractFragmentAnnotator fragAnnot = null;
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator:initializeModifierAnnotator");
		logger.setLevel(Level.INFO);
		
		try {
			Class<?> fragAnnotClass = Class.forName(fragmentAnnotator);
			Constructor<?> fragAnnotClassConstructor = fragAnnotClass.getConstructor(LAPAccess.class);
			fragAnnot = (AbstractFragmentAnnotator) fragAnnotClassConstructor.newInstance(lap);
			
			logger.info("Modifier instantiated from class: " + fragmentAnnotator);
			
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Error initializing modifier annotator : " + e.getClass());
			e.printStackTrace();
		}	
		
		return fragAnnot;
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

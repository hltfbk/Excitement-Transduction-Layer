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

import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.AbstractFragmentAnnotator;
import eu.excitementproject.tl.decomposition.modifierannotator.AbstractModifierAnnotator;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasuresMacro;
import eu.excitementproject.tl.evaluation.utils.FragmentAndModifierMatchCounter;
import eu.excitementproject.tl.laputils.AnnotationUtils;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.Interaction;

public class ModifierAnnotatorEvaluator {

	public static EvaluationMeasures evaluateModifiers(String xmiDir, String modifierAnnotator, String fragmentAnnotator, String language) 
			throws LAPException, ModifierAnnotatorException, FragmentAnnotatorException, IOException{
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator");
		logger.setLevel(Level.INFO);
		
		logger.info("Starting processing : \n\tdir: " + xmiDir + "\n\tmodifier annotator: " + modifierAnnotator +  "\n\tfragment annotator: " + fragmentAnnotator  + "\n\tlanguage: " + language);
		
		LAPAccess lap = initializeLAP(language);
		FragmentAnnotator fragAnn = initializeFragmentAnnotator(fragmentAnnotator, lap);
		AbstractModifierAnnotator modAnn = initializeModifierAnnotator(modifierAnnotator, fragAnn, lap);
		
		return processXMIs(lap, fragAnn, modAnn, xmiDir);
	}
		
	
	public static EvaluationMeasures evaluateModifiers(String xmiDir, AbstractModifierAnnotator modAnn, FragmentAnnotator fragAnn, LAPAccess lap) 
			throws LAPException, ModifierAnnotatorException, FragmentAnnotatorException, IOException {
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator");
		logger.setLevel(Level.INFO);
		
		logger.info("Starting processing : \n\tdir: " + xmiDir + "\n\tmodifier annotator: " + modAnn.getClass() +  "\n\tfragment annotator: " + fragAnn.getClass()  + "\n\tLAP: " + lap.getClass());
				
		return processXMIs(lap, fragAnn, modAnn, xmiDir);		
	}
	
	
	public static EvaluationMeasures processXMIs(LAPAccess lap, FragmentAnnotator fragAnn, AbstractModifierAnnotator modAnn, String xmiDir) 
			throws LAPException, ModifierAnnotatorException, FragmentAnnotatorException, IOException{
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator:processXMIs");
		logger.setLevel(Level.INFO);
		
		List<Integer> counts = new ArrayList<Integer>(Arrays.asList(0,0,0,0)); // TP, FP, TN, FN
		EvaluationMeasuresMacro emm = new EvaluationMeasuresMacro();
		
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
				
				AnnotationUtils.transferAnnotations(goldJCas, sysJCas, KeywordAnnotation.class);
								
				modAnn.annotateModifiers(sysJCas);
			
				List<Integer> modCounts =  FragmentAndModifierMatchCounter.countModifierCounts(sysJCas, goldJCas);
				counts = addScores(counts,modCounts);
				emm.addScores(new EvaluationMeasures(modCounts));
			}
		}
		
		logger.info("Final counts: " + counts.toString());
		logger.info("\nMacro-scores: Recall=" + emm.getRecall() + ";   Precision=" + emm.getPrecision() + ";   Fscore=" + emm.getFscore() + "\n");
		
		return new EvaluationMeasures(counts);
	}
	
	
	private static List<Integer> addScores(List<Integer> counts, List<Integer> modCounts) {
				
		return new ArrayList<Integer>(Arrays.asList(counts.get(0) + modCounts.get(0),
													counts.get(1) + modCounts.get(1),
													counts.get(2) + modCounts.get(2),
													counts.get(3) + modCounts.get(3)));
	}


	public static AbstractModifierAnnotator initializeModifierAnnotator(
			String modifierAnnotator, FragmentAnnotator fragAnn, LAPAccess lap) {
		AbstractModifierAnnotator modAnnot = null;
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator:initializeModifierAnnotator");
		logger.setLevel(Level.INFO);
		
		try {
			Class<?> modAnnotClass = Class.forName(modifierAnnotator);
			Constructor<?> modAnnotClassConstructor = modAnnotClass.getConstructor(LAPAccess.class, FragmentAnnotator.class);
			modAnnot = (AbstractModifierAnnotator) modAnnotClassConstructor.newInstance(lap, fragAnn);
			
			logger.info("Modifier instantiated from class: " + modifierAnnotator);
			
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Error initializing modifier annotator : " + e.getClass());
			e.printStackTrace();
		}	
		
		return modAnnot;
	}

	
	public static AbstractFragmentAnnotator initializeFragmentAnnotator(
			String fragmentAnnotator, LAPAccess lap) {
		AbstractFragmentAnnotator fragAnnot = null;
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator:initializeFragmentAnnotator");
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
	
	public static LAPAccess initializeLAP(String language){
		
		String lapClassName = "eu.excitementproject.tl.laputils.LemmaLevelLap" + language.toUpperCase();
//		String lapClassName = "eu.excitementproject.tl.laputils.DependencyLevelLap" + language.toUpperCase();

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

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
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.uimafit.util.JCasUtil;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitement.type.tl.ModifierAnnotation;
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
import eu.excitementproject.tl.laputils.LAPUtils;
import eu.excitementproject.tl.structures.Interaction;

/**
 * Collection of static methods for evaluating the modifier annotations
 * 
 * @author Vivi Nastase and Tae-Gil Noh
 *
 */
public class ModifierAnnotatorEvaluator {

	/**
	 * Evaluates the modifier annotations (token by token)
	 * 
	 * @param xmiDir -- directory with gold standard XMIs
	 * @param modifierAnnotator -- modifier annotator
	 * @param fragmentAnnotator -- fragment annotator (if specified it will annotate first fragments and the modifiers inside these fragments)
	 * @param language
	 * @param useGSfragAnnot -- whether to use the gold-standard fragment annotations, or not (then it makes fragments using the fragment annotator)
	 * @return an EvaluationMeasures object that contains the counts (TP, FP, TN, FN)
	 * 
	 * @throws LAPException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentAnnotatorException
	 * @throws IOException
	 */
	public static EvaluationMeasures evaluateModifiers(String xmiDir, String modifierAnnotator, String fragmentAnnotator, String language, boolean useGSfragAnnot) 
			throws LAPException, ModifierAnnotatorException, FragmentAnnotatorException, IOException{
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator");
		logger.setLevel(Level.INFO);
		
		logger.info("Starting processing : \n\tdir: " + xmiDir + "\n\tmodifier annotator: " + modifierAnnotator +  "\n\tfragment annotator: " + fragmentAnnotator  + "\n\tlanguage: " + language);
		
		LAPAccess lap = LAPUtils.initializeLAP(language);
		
		FragmentAnnotator fragAnn = initializeFragmentAnnotator(fragmentAnnotator, lap);
		
		AbstractModifierAnnotator modAnn = initializeModifierAnnotator(modifierAnnotator, fragAnn, lap);
		
		return processXMIs(lap, fragAnn, modAnn, xmiDir, useGSfragAnnot);
	}
		
	/**
	 * Evaluates the modifier annotations (token by token)
	 * 
	 * @param xmiDir -- directory with gold-standard annotations
	 * @param modAnn -- modifier annotator (object)
	 * @param fragAnn -- fragment annotator (object)
	 * @param lap -- the Linguistic Analysis Pipeline (for both the fragment and modifier annotators)
	 * @param useGSfragAnnot -- whether to use the gold-standard annotations or not (then it makes fragments using the given fragment annotator)
	 * @return an EvaluationMeasures object that contains the counts (TP, FP, TN, FN)
	 * 
	 * @throws LAPException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentAnnotatorException
	 * @throws IOException
	 */
	public static EvaluationMeasures evaluateModifiers(String xmiDir, AbstractModifierAnnotator modAnn, FragmentAnnotator fragAnn, LAPAccess lap, boolean useGSfragAnnot) 
			throws LAPException, ModifierAnnotatorException, FragmentAnnotatorException, IOException {
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator");
		logger.setLevel(Level.INFO);
		
		logger.info("Starting processing : \n\tdir: " + xmiDir + "\n\tmodifier annotator: " + modAnn.getClass() +  "\n\tfragment annotator: " + fragAnn.getClass()  + "\n\tLAP: " + lap.getClass());
				
		return processXMIs(lap, fragAnn, modAnn, xmiDir, useGSfragAnnot);		
	}
	
	/**
	 * Processes the XMIs, and evaluates the annotations (token by token)
	 * 
	 * @param lap -- LAP for the annotators
	 * @param fragAnn -- fragment annotator
	 * @param modAnn -- modifier annotator
	 * @param xmiDir -- directory with input XMIs
	 * @param useGSfragAnnot -- whether to use the gold-standard fragment annotations or not (then it makes fragments using the given fragment annotator)
	 * @return an EvaluationMeasures object that contains the counts (TP, FP, TN, FN)
	 * 
	 * @throws LAPException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentAnnotatorException
	 * @throws IOException
	 */
	public static EvaluationMeasures processXMIs(LAPAccess lap, FragmentAnnotator fragAnn, AbstractModifierAnnotator modAnn, String xmiDir, boolean useGSfragAnnot) 
			throws LAPException, ModifierAnnotatorException, FragmentAnnotatorException, IOException{
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.modifierannotator.ModifierAnnotatorEvaluator:processXMIs");
		logger.setLevel(Level.INFO);
		
		List<Integer> counts = new ArrayList<Integer>(Arrays.asList(0,0,0,0)); // TP, FP, TN, FN
		EvaluationMeasuresMacro emm = new EvaluationMeasuresMacro();

		int modAnnotationsCount = 0;
		int modCountGS = 0;
		int modTokenCountGS = 0;
		int dependsOnCount = 0;
		
		for(File xmiIn: FileUtils.listFiles(new File(xmiDir), new String[]{"xmi"}, true)) {
			
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
								
				if (useGSfragAnnot) {
					AnnotationUtils.transferAnnotations(goldJCas, sysJCas, DeterminedFragment.class);
					modAnn.setFragmentAnnotator(null);
				} else {
					AnnotationUtils.transferAnnotations(goldJCas, sysJCas, KeywordAnnotation.class);	
				}
								
				modAnnotationsCount += modAnn.annotateModifiers(sysJCas);
							
				List<Integer> modCounts =  FragmentAndModifierMatchCounter.countModifierCounts(sysJCas, goldJCas);
				counts = addScores(counts,modCounts);
				emm.addScores(new EvaluationMeasures(modCounts));
				
				modCountGS += AnnotationUtils.countAnnotations(goldJCas, ModifierAnnotation.class);
				modTokenCountGS += AnnotationUtils.countAnnotationTokens(goldJCas, ModifierAnnotation.class);
				
				dependsOnCount += countDependents(sysJCas);
			}
		}
		
		logger.info("Final counts: " + counts.toString() + " / " + FragmentAndModifierMatchCounter.getClassDetails(counts));
		logger.info("\nMacro-scores: Recall=" + emm.getRecall() + ";   Precision=" + emm.getPrecision() + ";   Fscore=" + emm.getFscore() + "\n" + "Number of instances: " + modAnnotationsCount);
		logger.info("Gold standard information: " + modCountGS + " instances / " + modTokenCountGS + " tokens");
		logger.info("Number of dependent modifiers: " + dependsOnCount);
		
		return new EvaluationMeasures(counts);
	}
	
	
	private static int countDependents(JCas aJCas) {
		int count = 0;
		
		DocumentAnnotation da = JCasUtil.selectSingle(aJCas, DocumentAnnotation.class);		
		List<ModifierAnnotation> mas = JCasUtil.selectCovered(aJCas, ModifierAnnotation.class, da);
		
		if (mas != null) {
			for(ModifierAnnotation ma : mas) {
				if (ma.getDependsOn() != null) {
					count++;
				}
			}
		}
		
		return count;
	}

	/**
	 * Adds counts from the currently processed modifiers to the overall counts
	 * 
	 * @param counts
	 * @param modCounts
	 * @return
	 */
	private static List<Integer> addScores(List<Integer> counts, List<Integer> modCounts) {
				
		return new ArrayList<Integer>(Arrays.asList(counts.get(0) + modCounts.get(0),
													counts.get(1) + modCounts.get(1),
													counts.get(2) + modCounts.get(2),
													counts.get(3) + modCounts.get(3)));
	}


	/**
	 * It initializes the modifier annotator from the given class, the given fragment annotator and LAP
	 * 
	 * @param modifierAnnotator
	 * @param fragAnn
	 * @param lap
	 * @return a ModifierAnnotator object
	 */
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

	
	/**
	 * Initializes the fragment annotator from the given class and LAP
	 * 
	 * @param fragmentAnnotator
	 * @param lap
	 * @return a FragmentAnnotator object
	 */
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
	
}

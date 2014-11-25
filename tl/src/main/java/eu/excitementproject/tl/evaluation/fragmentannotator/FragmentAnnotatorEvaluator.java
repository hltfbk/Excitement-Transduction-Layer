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
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitement.type.tl.FragmentPart;
import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitement.type.tl.ModifierPart;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.AbstractFragmentAnnotator;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasuresMacro;
import eu.excitementproject.tl.evaluation.utils.FragmentAndModifierMatchCounter;
import eu.excitementproject.tl.laputils.AnnotationUtils;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.LAPUtils;
import eu.excitementproject.tl.structures.Interaction;

/**
 * Collection of static methods for evaluating the fragment annotations
 * 
 * @author vivi@fbk and gil@heidelberg
 *
 */
@SuppressWarnings("unused")
public class FragmentAnnotatorEvaluator {

	/**
	 * Evaluates the fragment annotations (token by token)
	 * 
	 * @param xmiDir -- directory with gold-standard XMIs
	 * @param fragmentAnnotator -- fragment annotator 
	 * @param language
	 * @return an EvaluationMeasures object that contains the counts (TP, FP, TN, FN)
	 * 
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws IOException
	 */
	public static EvaluationMeasures evaluateFragments(String xmiDir, String fragmentAnnotator, String language) 
			throws LAPException, FragmentAnnotatorException, IOException{
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.fragmentannotator.FragmentAnnotatorEvaluator");
		logger.setLevel(Level.INFO);
		
		logger.info("Starting processing : \n\tdir: " + xmiDir + "\n\tfragment annotator: " + fragmentAnnotator + "\n\tlanguage: " + language);
		
		LAPAccess lap = LAPUtils.initializeLAP(language);
		AbstractFragmentAnnotator fragAnnot = initializeAnnotator(fragmentAnnotator, lap);
		List<Integer> counts = new ArrayList<Integer>(Arrays.asList(0,0,0,0)); // TP, FP, TN, FN
		EvaluationMeasuresMacro emm = new EvaluationMeasuresMacro();
		
		for(File xmiIn: FileUtils.listFiles(new File(xmiDir), new String[]{"xmi"}, true)) {
			
			logger.info("Processing " + xmiIn.getName());
			// 1st. load the gold annotations from the xmi
			JCas goldJCas = CASUtils.createNewInputCas();
			CASUtils.deserializeFromXmi(goldJCas, xmiIn);
			// It has Mod & Frag annots, but doesn't have tokens at all!
			// we need to add them. 
			
			if (goldJCas != null &&
					goldJCas.getDocumentText() != null &&
					! goldJCas.getDocumentText().isEmpty() &&
					goldJCas.getDocumentText().matches(".*\\w+.*")) {
		
				lap.addAnnotationOn(goldJCas);
			
/*				// 2nd. prepare a system output, by making a interaction ...
				String interactionText = goldJCas.getDocumentText();
				String interactionLang = goldJCas.getDocumentLanguage();
			
				// how to add the keywords here? we have no xmis for data that has keywords ... 
				//			String interactionKeywords = ... ;
			
				Interaction in = new Interaction(interactionText, interactionLang);				  
				JCas sysJCas = in.createAndFillInputCAS();
*/
				String interactionText = goldJCas.getDocumentText(); 
				String interactionLang = goldJCas.getDocumentLanguage(); 
				Interaction in = new Interaction(interactionText, interactionLang);  
				JCas sysJCas = in.createAndFillInputCAS();

				AnnotationUtils.transferAnnotations(goldJCas, sysJCas, KeywordAnnotation.class);
								
				fragAnnot.annotateFragments(sysJCas); 
				
				AnnotationUtils.printAnnotations(sysJCas, POS.class);
			
				List<Integer> fragCounts =  FragmentAndModifierMatchCounter.countFragmentCounts(sysJCas, goldJCas);
//				List<Integer> fragCounts =  FragmentAndModifierMatchCounter.countFragmentCountsRelativeToBaseStatements(sysJCas, goldJCas);

				counts = addScores(counts,fragCounts);
				emm.addScores(new EvaluationMeasures(fragCounts));
			}
		}
		
		logger.info("Final counts: " + counts.toString() + " / " + FragmentAndModifierMatchCounter.getClassDetails(counts));
		logger.info("\nMacro-scores: Recall=" + emm.getRecall() + ";   Precision=" + emm.getPrecision() + ";   Fscore=" + emm.getFscore() + "\n" + "Number of instances: " + emm.getNrOfInstances());
		
		return new EvaluationMeasures(counts);
	}
	
	
	/**
	 * Add counts for the current fragment/interaction to the existing counts
	 * 
	 * @param counts
	 * @param fragCounts
	 * @return
	 */
	private static List<Integer> addScores(List<Integer> counts, List<Integer> fragCounts) {
				
		return new ArrayList<Integer>(Arrays.asList(counts.get(0) + fragCounts.get(0),
													counts.get(1) + fragCounts.get(1),
													counts.get(2) + fragCounts.get(2),
													counts.get(3) + fragCounts.get(3)));
	}


	/**
	 * Initializes the fragment annotator using the given class and LAP
	 * 
	 * @param fragmentAnnotator 
	 * @param lap -- the Linguistic Analysis Pipeline for the given fragment annotator
	 * @return
	 */
	private static AbstractFragmentAnnotator initializeAnnotator(
			String fragmentAnnotator, LAPAccess lap) {
		AbstractFragmentAnnotator fragAnnot = null;
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.fragmentannotator.FragmentAnnotatorEvaluator:initializeFragmentAnnotator");
		logger.setLevel(Level.INFO);
		
		try {
			Class<?> fragAnnotClass = Class.forName(fragmentAnnotator);
			Constructor<?> fragAnnotClassConstructor = fragAnnotClass.getConstructor(LAPAccess.class);
			fragAnnot = (AbstractFragmentAnnotator) fragAnnotClassConstructor.newInstance(lap);
			
			logger.info("Fragment annotator instantiated from class: " + fragmentAnnotator);
			
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Error initializing modifier annotator : " + e.getClass());
			e.printStackTrace();
		}	
		
		return fragAnnot;
	}

}

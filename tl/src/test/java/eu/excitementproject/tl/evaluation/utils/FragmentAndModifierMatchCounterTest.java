package eu.excitementproject.tl.evaluation.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.LemmaLevelLapEN;
import eu.excitementproject.tl.structures.Interaction;

public class FragmentAndModifierMatchCounterTest {

	@Ignore
	@Test
	public void test() {
		
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO);  // set this to DEBUG for see DEBUG logs and why counts are given as they are. 

		// Test for Modifier Match Count. 
		// (count Modifiers for TruePos, FalsePos, TrueNeg, FalseNeg) 
		LAPAccess lap = null; 
		JCas goldJCas = null; 
		try {
			lap = new LemmaLevelLapEN(); 
			
			// First. load gold data 
			goldJCas = CASUtils.createNewInputCas(); 
			File xmiIn = new File("./src/test/resources/WP2_public_data_CAS_XMI/NICE_open/all/100771.txt.xmi"); 
			CASUtils.deserializeFromXmi(goldJCas, xmiIn); 
			// It has Mod & Frag annots, but doesn't have tokens at all! 
			// we need to add them. 
			lap.addAnnotationOn(goldJCas); 

			// 2nd. prepare a system output, by making a interaction ...
			String interactionText = goldJCas.getDocumentText(); 
			String interactionLang = goldJCas.getDocumentLanguage(); 
			Interaction in = new Interaction(interactionText, interactionLang);  
			JCas sysJCas = in.createAndFillInputCAS();
			// and call a Modifier annotator 
			ModifierAnnotator modAnnot = new AdvAsModifierAnnotator(lap); 
			modAnnot.annotateModifiers(sysJCas); 

			// now run the counter. 
			List<Integer> fourInts = FragmentAndModifierMatchCounter.countModifierCounts(sysJCas, goldJCas); 
			assertNotNull(fourInts); 
		}
		catch (Exception e)
		{
			e.printStackTrace(); 
			fail(e.getMessage()); 
		}
		
		// Test for Fragment Match Count  
		// (count Fragment for TruePos, FalsePos, TrueNeg, FalseNeg) 
		
		// we will reuse the same gold JCas
		try 
		{
			// let's prepare a system fragment output, by making an interaction ...
			String interactionText = goldJCas.getDocumentText(); 
			String interactionLang = goldJCas.getDocumentLanguage(); 
			Interaction in = new Interaction(interactionText, interactionLang);  
			JCas sysJCas = in.createAndFillInputCAS();
			// ... and call a Fragment annotator 
			FragmentAnnotator frAnnot = new SentenceAsFragmentAnnotator(lap); 
			frAnnot.annotateFragments(sysJCas); 
			
			// count it. 
			List<Integer> fourInts = FragmentAndModifierMatchCounter.countFragmentCounts(sysJCas, goldJCas); 
			assertNotNull(fourInts);			
		}
		catch (Exception e)
		{
			e.printStackTrace(); 
			fail(e.getMessage()); 	
		}
	}
}

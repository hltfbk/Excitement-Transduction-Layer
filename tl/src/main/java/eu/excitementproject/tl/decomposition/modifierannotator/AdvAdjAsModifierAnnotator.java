package eu.excitementproject.tl.decomposition.modifierannotator;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

/**
 * This class implements a simple "modifier annotator" implementation solely based on 
 * POS tags. It will simply annotate any continuous tokens that are ADJ and/or ADV as modifiers. 
 * 
 * <P> 
 * This simple implementation does not care about dependOn, or non-continuous regions 
 * 
 * @author Gil
 * 
 */
public class AdvAdjAsModifierAnnotator extends AbstractModifierAnnotator {

	public AdvAdjAsModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException
	{
		super(lap); 
	}
	
	@Override
	public void annotateModifiers(JCas text) throws ModifierAnnotatorException {
		// TODO fill in the code 
		
		// first check POS tokens 
		// if not found, run the given lap and check again. 
		// still no  POS tokens--> throw exception 
		
		// iterate over tokens, mark any continuous ADJ and ADV tokens as modifiers.  

		// DONE 
	}

}

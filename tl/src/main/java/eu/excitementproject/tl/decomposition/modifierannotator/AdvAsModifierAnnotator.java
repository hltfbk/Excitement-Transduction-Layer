package eu.excitementproject.tl.decomposition.modifierannotator;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

/**
 * This class implements a simple "modifier annotator" implementation solely based on 
 * POS tags. It will simply annotate any continuous tokens that are ADV as modifiers. 
 * 
 * <P> 
 * Note that, to really do the Modifier annotation, we will need dependency parsing + some more knowledge.  
 * Finding so called "Modifier", is not that easy task: it is actually picking out non-essential components in terms of predicate structure. (or something like that) 
 * Anyway, this simple implementation does not care about dependOn, or non-continuous regions. 
 * 
 * @author Gil
 * 
 */
public class AdvAsModifierAnnotator extends AbstractModifierAnnotator {

	public AdvAsModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException
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

package eu.excitementproject.tl.decomposition.modifierannotator;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
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
 * @author Gil, vivi@fbk
 * 
 */
public class AdvAsModifierAnnotator extends POSbasedModifierAnnotator {
	
	/**
	 * Constructor with LAP, default negation check, adds as classes adverbs (ADV)
	 * 
	 * @param lap -- the Linguistic Annotation Pipeline for annotation
	 * @throws ModifierAnnotatorException
	 */	
	@SuppressWarnings("unchecked")
	public AdvAsModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException
	{
		super(lap); 
		addPOSClasses(ADV.class);
	}

	/**
	 * Constructor with LAP and negation check, adds as classes adverbs (ADV)
	 * 
	 * @param lap -- the Linguistic Annotation Pipeline for annotation
	 * @param checkNegation -- check/not whether the modifier candidate is in the scope of a negation
	 * @throws ModifierAnnotatorException
	 */
	@SuppressWarnings("unchecked")
	public AdvAsModifierAnnotator(LAPAccess lap, boolean checkNegation) throws ModifierAnnotatorException
	{
		super(lap, checkNegation);
		addPOSClasses(ADV.class);
	}

	/**
	 * Constructor with LAP and fragment annotator, default negation check, adds as classes adverbs (ADV)
	 * 
	 * @param lap -- the Linguistic Annotation Pipeline for annotation
	 * @param fragAnn -- fragment annotator
	 * @throws ModifierAnnotatorException
	 */
	@SuppressWarnings("unchecked")
	public AdvAsModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn) throws ModifierAnnotatorException
	{
		super(lap, fragAnn);
		addPOSClasses(ADV.class);
	}

	/**
	 * Constructor with LAP, fragment annotator and negation check, adds as classes adverbs (ADV) and adjectives (ADJ)
	 * 
	 * @param lap -- the Linguistic Annotation Pipeline for annotation
	 * @param fragAnn -- fragment annotator
	 * @param checkNegation -- check/not whether the modifier candidate is in the scope of a negation
	 * @throws ModifierAnnotatorException
	 */		
	@SuppressWarnings("unchecked")
	public AdvAsModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn, boolean checkNegation) throws ModifierAnnotatorException
	{
		super(lap, fragAnn, checkNegation);
		addPOSClasses(ADV.class);
	}
	
}

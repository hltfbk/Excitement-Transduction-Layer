package eu.excitementproject.tl.decomposition.modifierannotator;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
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
 * @author Gil
 * 
 */
public class AdvAdjAsModifierAnnotator extends POSbasedModifierAnnotator {
			
	@SuppressWarnings("unchecked")
	public AdvAdjAsModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException
	{
		super(lap);
		addPOSClasses(ADV.class, ADJ.class);
	}
	
	@SuppressWarnings("unchecked")
	public AdvAdjAsModifierAnnotator(LAPAccess lap, boolean checkNegation) throws ModifierAnnotatorException
	{
		super(lap, checkNegation);
		addPOSClasses(ADV.class, ADJ.class);
	}
	
	@SuppressWarnings("unchecked")
	public AdvAdjAsModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn) throws ModifierAnnotatorException
	{
		super(lap, fragAnn);
		addPOSClasses(ADV.class, ADJ.class);
	}

	@SuppressWarnings("unchecked")
	public AdvAdjAsModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn, boolean checkNegation) throws ModifierAnnotatorException
	{
		super(lap, fragAnn, checkNegation);
		addPOSClasses(ADV.class, ADJ.class);
	}
	
}

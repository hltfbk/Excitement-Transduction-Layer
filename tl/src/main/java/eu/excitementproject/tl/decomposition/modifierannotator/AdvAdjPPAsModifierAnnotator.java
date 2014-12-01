package eu.excitementproject.tl.decomposition.modifierannotator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.AnnotationUtils;
import eu.excitementproject.tl.laputils.CASUtils.Region;

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
public class AdvAdjPPAsModifierAnnotator extends POSbasedModifierAnnotator {
	
	/**
	 * Constructor with LAP, default negation check, adds as classes adverbs (ADV), adjectives (ADJ) and prepositional phrases (based on the preposition PP)
	 * 
	 * @param lap -- the Linguistic Annotation Pipeline for annotation
	 * @throws ModifierAnnotatorException
	 */
	@SuppressWarnings("unchecked")
	public AdvAdjPPAsModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException
	{
		super(lap);
		addPOSClasses(ADV.class, ADJ.class, PP.class);
	}
	
	/**
	 * Constructor with LAP and negation check, adds as classes adverbs (ADV), adjectives (ADJ) and prepositional phrases (based on the preposition PP)
	 * 
	 * @param lap -- the Linguistic Annotation Pipeline for annotation
	 * @param checkNegation -- check/not whether the modifier candidate is in the scope of a negation
	 * @throws ModifierAnnotatorException
	 */
	@SuppressWarnings("unchecked")
	public AdvAdjPPAsModifierAnnotator(LAPAccess lap, boolean checkNegation) throws ModifierAnnotatorException
	{
		super(lap, checkNegation);
		addPOSClasses(ADV.class, ADJ.class, PP.class);
	}
	
	/**
	 * Constructor with LAP and fragment annotator, default negation check, adds as classes adverbs (ADV), adjectives (ADJ) and prepositional phrases (based on the preposition PP)
	 * 
	 * @param lap -- the Linguistic Annotation Pipeline for annotation
	 * @param fragAnn -- fragment annotator
	 * @throws ModifierAnnotatorException
	 */
	@SuppressWarnings("unchecked")
	public AdvAdjPPAsModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn) throws ModifierAnnotatorException
	{
		super(lap, fragAnn);
		addPOSClasses(ADV.class, ADJ.class, PP.class);
	}
	
	
	/**
	 * Constructor with LAP, fragment annotator and negation check, adds as classes adverbs (ADV), adjectives (ADJ) and prepositional phrases (based on the preposition PP)
	 * 
	 * @param lap -- the Linguistic Annotation Pipeline for annotation
	 * @param fragAnn -- fragment annotator
	 * @param checkNegation -- check/not whether the modifier candidate is in the scope of a negation
	 * @throws ModifierAnnotatorException
	 */	
	@SuppressWarnings("unchecked")
	public AdvAdjPPAsModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn, boolean checkNegation) throws ModifierAnnotatorException
	{
		super(lap, fragAnn, checkNegation);
		addPOSClasses(ADV.class, ADJ.class, PP.class);
	}
	
	
	/**
	 * Implementation of the method for adding modifier annotations 
	 * 
	 * @param fragItr -- iterator over the fragment annotations in the given CAS object
	 * @param aJCas -- the CAS object
	 */
	@Override
	protected int addModifierAnnotations(Iterator<Annotation> fragItr, JCas aJCas) throws ModifierAnnotatorException {
			
		modLogger.info("Annotating TLmodifier annotations of class on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 
		Integer negationPosition = -1;
		
		int num_mods = 0;
		
		while(fragItr.hasNext()) {
			FragmentAnnotation frag = (FragmentAnnotation) fragItr.next();
					
			if (wantedClasses != null) {
				
				if (checkNegation) {
					negationPosition = AnnotationUtils.checkNegation(frag);
				}
				
				for(Class<? extends POS> cls: wantedClasses) {
					
					modLogger.info("\tchecking for " + cls.getSimpleName());
								
					if (cls.equals(PP.class))
						num_mods += addPPModifiers(aJCas, frag, negationPosition);
					else
						num_mods += addModifiers(aJCas, frag, negationPosition, cls);
				}
			}
			modLogger.info("Checking for modifier dependencies ... ");
			addDependencies(aJCas, frag);
		}			
		return num_mods;
	}		
	
	
	
	/**
	 * Annotates PP phrases as modifiers, if dependency information is available
	 * 
 	 * @param aJCas a CAS object
	 * @param frag a fragment annotation in the given CAS object
	 * @param negationPos the position of the negation in the fragment (-1 if there is none)
	 * @throws ModifierAnnotatorException 
	 */
	public int addPPModifiers(JCas aJCas, FragmentAnnotation frag, int negationPos) throws ModifierAnnotatorException {
		Logger modLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.modifierannotator:addPPModifiers");
		int all_mods = 0;
		
		modLogger.info("Adding PP modifiers for fragment: *" + frag.getCoveredText() + "*");
		
		Collection<Dependency> dependencies = JCasUtil.select(aJCas, Dependency.class);
		if (dependencies != null && !dependencies.isEmpty()) {
			
//			List<? extends Annotation> listMods = JCasUtil.selectCovered(aJCas, PP.class, frag);
			List<? extends Annotation> listMods = AnnotationUtils.selectByPOS(aJCas, frag, PP.class);			
			
			int num_mods = 0;
		
			if (listMods != null && ! listMods.isEmpty()) {
				modLogger.info( listMods.size() + " PPs found!");

				for (Annotation a: listMods) {

					modLogger.info("Checking phrase for PP *" + a.getCoveredText() + "* (" + a.getBegin() + "," + a.getEnd() + ")");
					
					if (negationPos < 0 || ! AnnotationUtils.inNegationScope(a.getBegin(), frag, negationPos)) {

						num_mods += annotatePPModifier(aJCas, frag, a);
					} else {
						modLogger.info("Potential modifier *" + a.getCoveredText() + "* is (or is in scope of) a negation: " + a.getCoveredText());
					}
				}
			}
			modLogger.info("Annotated " + num_mods + " PP modifiers for fragment *" + frag.getCoveredText() + "*");
			all_mods += num_mods;
			num_mods = 0;
		}
		return all_mods;
	}
	
	
	/**
	 * Annotator for one prepositional phrase
	 * 
	 * @param aJCas -- CAS object
	 * @param frag -- fragment annotation that contains the prepositional phrase
	 * @param a -- annotation object corresponding to the preposition
	 * 
	 * @return -- the number of annotated PP modifiers
	 */
	private int annotatePPModifier(JCas aJCas, FragmentAnnotation frag, Annotation a) {
				
		Set<Region> pp = AnnotationUtils.getPhraseRegion(aJCas, frag, a);
		
		if (pp != null && pp.size() > 0) {
			AnnotationUtils.annotatePhraseModifier(aJCas, pp);
			return 1;
		} 
		return 0;
	}
	
}

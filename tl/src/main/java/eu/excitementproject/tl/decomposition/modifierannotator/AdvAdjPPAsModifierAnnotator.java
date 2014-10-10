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
	
	
	@SuppressWarnings("unchecked")
	public AdvAdjPPAsModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException
	{
		super(lap);
		addPOSClasses(ADV.class, ADJ.class, PP.class);
	}

	@SuppressWarnings("unchecked")
	public AdvAdjPPAsModifierAnnotator(LAPAccess lap, boolean checkNegation) throws ModifierAnnotatorException
	{
		super(lap, checkNegation);
		addPOSClasses(ADV.class, ADJ.class, PP.class);
	}
	
	@SuppressWarnings("unchecked")
	public AdvAdjPPAsModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn) throws ModifierAnnotatorException
	{
		super(lap, fragAnn);
		addPOSClasses(ADV.class, ADJ.class, PP.class);
	}
	
	@SuppressWarnings("unchecked")
	public AdvAdjPPAsModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn, boolean checkNegation) throws ModifierAnnotatorException
	{
		super(lap, fragAnn, checkNegation);
		addPOSClasses(ADV.class, ADJ.class, PP.class);
	}
	
	
	@Override
	protected void addModifierAnnotations(Iterator<Annotation> fragItr, JCas aJCas, boolean checkNegation, Set<Class<? extends POS>> POSclasses) throws ModifierAnnotatorException {
			
		modLogger.info("Annotating TLmodifier annotations on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 
		Integer negationPosition = -1;
		
		while(fragItr.hasNext()) {
			FragmentAnnotation frag = (FragmentAnnotation) fragItr.next();
					
			if (POSclasses != null) {
				for(Class<? extends POS> cls: POSclasses) {
					
					if (checkNegation) {
						negationPosition = AnnotationUtils.checkNegation(frag);
					}
			
					if (cls.equals(PP.class))
						addPPModifiers(aJCas, frag, negationPosition);
					else
						AnnotationUtils.addModifiers(aJCas, frag, negationPosition, cls);
				}
			}
		}			
	}		
	
	
	
	/**
	 * Annotates PP phrases as modifiers, if dependency information is available
	 * 
 	 * @param aJCas a CAS object
	 * @param frag a fragment annotation in the given CAS object
	 * @param negationPos the position of the negation in the fragment (-1 if there is none)
	 * @throws ModifierAnnotatorException 
	 */
	public void addPPModifiers(JCas aJCas, FragmentAnnotation frag, int negationPos) throws ModifierAnnotatorException {
		Logger modLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.modifierannotator:addPPModifiers");

		Collection<Dependency> dependencies = JCasUtil.select(aJCas, Dependency.class);
		if (dependencies != null && !dependencies.isEmpty()) {
			
			List<? extends Annotation> listMods = JCasUtil.selectCovered(aJCas, PP.class, frag);
			int num_mods = 0;
		
			if (listMods != null && ! listMods.isEmpty()) {
				modLogger.info("PPs found!");

				for (Annotation a: listMods) {

					modLogger.info("Adding phrase for PP " + a.getCoveredText());
					if (negationPos < 0 || ! AnnotationUtils.inNegationScope(a.getBegin(), frag, negationPos)) {

						annotatePPModifier(aJCas, a);
						num_mods++;
					} else {
						modLogger.info("Potential modifier is or is in scope of a negation: " + a.getCoveredText());
					}
				}
			}
			modLogger.info("Annotated " + num_mods + " PP modifiers for fragment " + frag.getCoveredText());
			num_mods = 0;
		}
	}
	
	
	private void annotatePPModifier(JCas aJCas, Annotation a) {
		
		System.out.println("Adding one PP modifier for " + a.getCoveredText());
		
		Set<Region> pp = AnnotationUtils.getPhraseRegion(aJCas, a);
		
		if (pp != null && pp.size() > 0) {
			AnnotationUtils.annotatePhraseModifier(aJCas, pp);
		}
	}
	
}

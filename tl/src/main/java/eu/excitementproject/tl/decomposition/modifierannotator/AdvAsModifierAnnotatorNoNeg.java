package eu.excitementproject.tl.decomposition.modifierannotator;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;

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
public class AdvAsModifierAnnotatorNoNeg extends AbstractModifierAnnotator {

	private int negationPos = -1;

	private String negationPattern = "(no|non|not|n't|kein|keine|keinen|nein|nessun|nessuna|nessuno)";
	
	private Logger modLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.modifierannotator"); 
	
	public AdvAsModifierAnnotatorNoNeg(LAPAccess lap) throws ModifierAnnotatorException
	{
		super(lap); 
	}
	
	public AdvAsModifierAnnotatorNoNeg(LAPAccess lap, FragmentAnnotator fragAnn) throws ModifierAnnotatorException
	{
		super(lap, fragAnn); 
	}
	
	@Override
	public void annotateModifiers(JCas aJCas) throws ModifierAnnotatorException, FragmentAnnotatorException {
		
		// check if it already has modifier annotations. If it is so, 
		// we don't process this CAS and pass. 
		AnnotationIndex<Annotation> modIndex = aJCas.getAnnotationIndex(ModifierAnnotation.type);
		if (modIndex.size() > 0)
		{
			modLogger.info("The CAS already has " + modIndex.size() + " modifier annotations. Won't process this CAS."); 
			return; 
		}


		
		// check POS annotation is there or not 
		AnnotationIndex<Annotation> posIndex = aJCas.getAnnotationIndex(POS.type);
		Iterator<Annotation> posItr = posIndex.iterator(); 		

		if (!posItr.hasNext())
		{
			// It seems that there are no POS annotations in the CAS. 
			// Run LAP on it. 
			modLogger.info("No POS annotations found: calling the given LAP."); 
			try 
			{
				this.getLap().addAnnotationOn(aJCas); 
			}
			catch (LAPException e)
			{
				throw new ModifierAnnotatorException("Unable to run LAP on the input CAS: LAP raised an exception",e); 
			}
			// all right. LAP annotated. Try once again 
			posIndex = aJCas.getAnnotationIndex(POS.type);
			posItr = posIndex.iterator(); 		
			
			// throw exception, if still no POS annotations in it 
			if (!posItr.hasNext())
			{
				throw new ModifierAnnotatorException("Calling on LAPAccess " + this.getLap().getClass().getName() + " didn't added POS annotation. Cannot proceed."); 
			}
		}
		
		// check Fragment annotations (only annotate modifiers inside fragments)
		AnnotationIndex<Annotation> fragIndex = aJCas.getAnnotationIndex(FragmentAnnotation.type);
		Iterator<Annotation> fragItr = fragIndex.iterator();
		
		if (!fragItr.hasNext()) {
			modLogger.info("No Fragment annotations found");
			if (this.fragAnn != null) {
				try {
					this.fragAnn.annotateFragments(aJCas);
				} catch (FragmentAnnotatorException e) {
					throw new FragmentAnnotatorException("Unable to annotate fragments on the input CAS", e);
				}
				
				// all right. Fragments added. Try again
				fragIndex = aJCas.getAnnotationIndex(FragmentAnnotation.type);
				fragItr = fragIndex.iterator();
				
				if (!fragItr.hasNext()) {
					throw new ModifierAnnotatorException("The CAS had no fragment annotations, and the fragment annotator " + this.fragAnn.getClass().getName() + " could not create any");					
				}
								
			} else {
				throw new ModifierAnnotatorException("The CAS had no fragment annotations, and the ModifierAnnotator instance had no fragment annotator to create some.");
			}
		}
		
		modLogger.info("Annotating TLmodifier annotations on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 
		int num_mods = 0; 

		while(fragItr.hasNext()) {
			FragmentAnnotation frag = (FragmentAnnotation) fragItr.next();
		
//			AnnotationIndex<Annotation> advIndex = aJCas.getAnnotationIndex(ADV.type);
//			Iterator<Annotation> advItr = advIndex.iterator();
	
			checkNegation(frag);
			
			List<ADV> listAdv = JCasUtil.selectCovered(aJCas, ADV.class, frag);
			
			if (listAdv != null && ! listAdv.isEmpty()) {
				for (ADV adv: listAdv) {

					int begin = adv.getBegin(); 
					int end = adv.getEnd(); 

					if (! isNegation(adv.getCoveredText()) &&
						! inNegationScope(begin, frag)) {
						
						CASUtils.Region[] r = new CASUtils.Region[1]; 
						r[0] = new CASUtils.Region(begin,  end); 
			
						modLogger.info("Annotating the following as a modifier: " + adv.getCoveredText());
						
						try {
							CASUtils.annotateOneModifier(aJCas, r); 
						} catch (LAPException e) {
							throw new ModifierAnnotatorException("CASUtils reported exception while annotating Modifier, on sentence (" + begin + ","+ end, e );
						}
						num_mods++;
					} else {
						modLogger.info("Potential modifier is or is in scope of a negation: " + adv.getCoveredText());
					}
				}
			}
			modLogger.info("Annotated " + num_mods + " for fragment " + frag.getCoveredText());
			num_mods = 0;
		}			
	}

	/**
	 * Check if the modifier annotation is a negation
	 * 
	 * @param m - a modifier annotation
	 * @return
	 */
	private boolean isNegation(String mod) {
		return (mod.toLowerCase().matches(negationPattern));
	}

	/**
	 * Checks if a modifier for a given fragment is fine with respect to the negation (if there is one)
	 * i.e. -- neither the negation nor anything in its scope (here we consider everything coming after it as the scope)
	 *         is removed 
	 * 
	 * (example: "not enough seating" -- neither "not", nor "enough" can be removed)	
	 * 
	 * @param m -- a set of modifier annotations from the document CAS
	 * @return -- true if the modifier given is OK with respect to the negation
	 */
	private boolean inNegationScope(int m_begin, FragmentAnnotation f) {

		// we could check here only relative to the fragment parts ...
		if (negationPos > 0 && (m_begin - f.getBegin() >= negationPos)) {
			return true;
		}
		
		return false;
	}

	/** 
	 * Checks if the current fragment contains negations, and sets the negationPosition attribute
	 */
	private void checkNegation(FragmentAnnotation fragment) {
		
		negationPos = -1;
		String text = fragment.getCoveredText();
		
		Pattern p = Pattern.compile("\\b" + negationPattern + "\\b",Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);

		if (m.find()) {			
			negationPos = text.indexOf(m.group(1));
		}		
	}
	
}

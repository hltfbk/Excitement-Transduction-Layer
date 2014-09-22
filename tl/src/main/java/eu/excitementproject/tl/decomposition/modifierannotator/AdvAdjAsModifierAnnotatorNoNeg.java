package eu.excitementproject.tl.decomposition.modifierannotator;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADJ;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.AnnotationUtils;

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
public class AdvAdjAsModifierAnnotatorNoNeg extends AbstractModifierAnnotator {

	private int negationPos = -1;
	
	private Logger modLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.modifierannotator"); 
	
	public AdvAdjAsModifierAnnotatorNoNeg(LAPAccess lap) throws ModifierAnnotatorException
	{
		super(lap); 
	}
	
	public AdvAdjAsModifierAnnotatorNoNeg(LAPAccess lap, FragmentAnnotator fragAnn) throws ModifierAnnotatorException
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

		while(fragItr.hasNext()) {
			FragmentAnnotation frag = (FragmentAnnotation) fragItr.next();
		
			negationPos = AnnotationUtils.checkNegation(frag);
			
			AnnotationUtils.addModifiers(aJCas, frag, negationPos, ADV.class);
			AnnotationUtils.addModifiers(aJCas, frag, negationPos, ADJ.class);
		}			
	}
	
}

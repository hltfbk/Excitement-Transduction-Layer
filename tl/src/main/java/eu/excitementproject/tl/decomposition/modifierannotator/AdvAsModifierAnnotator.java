package eu.excitementproject.tl.decomposition.modifierannotator;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ADV;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
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
public class AdvAsModifierAnnotator extends AbstractModifierAnnotator {

	public AdvAsModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException
	{
		super(lap); 
	}
	
	@Override
	public void annotateModifiers(JCas aJCas) throws ModifierAnnotatorException {
		
		Logger fragLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.modifierannotator"); 

		// check POS annotation is there or not 
		AnnotationIndex<Annotation> posIndex = aJCas.getAnnotationIndex(POS.type);
		Iterator<Annotation> posItr = posIndex.iterator(); 		

		if (!posItr.hasNext())
		{
			// It seems that there are no POS annotations in the CAS. 
			// Run LAP on it. 
			fragLogger.info("No POS annotations found: calling the given LAP."); 
			try 
			{
				this.getLap().addAnnotationOn(aJCas); 
			}
			catch (LAPException e)
			{
				throw new ModifierAnnotatorException("Unable to run LAP on the inputCAS: LAP raised an exception",e); 
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

		fragLogger.info("Annotating TLmodifier annotations on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 
		int num_mods = 0; 

		AnnotationIndex<Annotation> advIndex = aJCas.getAnnotationIndex(ADV.type);
		Iterator<Annotation> advItr = advIndex.iterator();
		
		while(advItr.hasNext())
		{
			// if it is an Adverb (RB), annotate it as one modifier. 
			ADV adv = (ADV) advItr.next(); 
			
			int begin = adv.getBegin(); 
			int end = adv.getEnd(); 
			CASUtils.Region[] r = new CASUtils.Region[1]; 
			r[0] = new CASUtils.Region(begin,  end); 
			
			fragLogger.debug("Annotating the following as a modifier: " + adv.getCoveredText()); 
			try {
				CASUtils.annotateOneModifier(aJCas, r); 
			}
			catch (LAPException e)
			{
				throw new ModifierAnnotatorException("CASUtils reported exception while annotating Modifier, on sentence (" + begin + ","+ end, e );
			}
			num_mods++; 
		}
		fragLogger.info("Annotated " + num_mods + " TL layer modifier annotations."); 
	}

}

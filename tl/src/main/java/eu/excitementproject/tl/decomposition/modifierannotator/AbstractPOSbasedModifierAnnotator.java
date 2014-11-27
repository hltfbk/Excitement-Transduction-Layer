package eu.excitementproject.tl.decomposition.modifierannotator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.AnnotationUtils;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * This class implements a simple "modifier annotator" implementation solely based on 
 * POS tags. It will simply annotate any continuous tokens that have the specified POS as modifiers. 
 * 
 * <P> 
 * Note that, to really do the Modifier annotation, we will need dependency parsing + some more knowledge.  
 * Finding so called "Modifier", is not that easy task: it is actually picking out non-essential components in terms of predicate structure. (or something like that) 
 * Anyway, this simple implementation does not care about dependOn, or non-continuous regions. 
 * 
 * @author Gil
 * 
 */
public abstract class AbstractPOSbasedModifierAnnotator extends AbstractModifierAnnotator {
	
	protected Logger modLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.modifierannotator"); 

	// the set of POS classes that should be marked as modifiers
	protected Set<Class<? extends POS>> wantedClasses;
	
	// if true, accept a candidate as modifier if it is not in the scope of a negation
	protected boolean checkNegation = false;
	
	/**
	 * Constructor with LAP
	 * 
	 * @param lap
	 * @throws ModifierAnnotatorException
	 */
	public AbstractPOSbasedModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException
	{
		super(lap); 
	}
	
	/**
	 * Constructor with LAP and fragment annotator
	 * 
	 * @param lap
	 * @param fragAnn
	 * @throws ModifierAnnotatorException
	 */
	public AbstractPOSbasedModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn) throws ModifierAnnotatorException
	{
		super(lap, fragAnn); 
	}
	
	/**
	 * The class to be implemented by the variations we want (use ADV as modifiers, or use ADJ, or PPs, or any combination
	 */
	protected abstract void addPOSClasses();
	
	/**
	 * Annotate as modifiers the tokens whose POS fits the specified classes
	 * 
	 * @param aJCas
	 */
	@Override
	public int annotateModifiers(JCas aJCas) throws ModifierAnnotatorException {
		
		// check if it already has modifier annotations. If it is so, 
		// we don't process this CAS and pass. 
		AnnotationIndex<Annotation> modIndex = aJCas.getAnnotationIndex(ModifierAnnotation.type);
		if (modIndex.size() > 0)
		{
			modLogger.info("The CAS already has " + modIndex.size() + " modifier annotations. Won't process this CAS."); 
			return modIndex.size(); 
		}
		
		// check POS annotation is there or not 
		AnnotationIndex<Annotation> posIndex = aJCas.getAnnotationIndex(POS.type);
		Iterator<Annotation> posItr = posIndex.iterator(); 		
		
		modLogger.info("the CAS has " + posIndex.size() + " POS annotations");
		
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
				throw new ModifierAnnotatorException("Calling on LAPAccess " + this.getLap().getClass().getName() + " didn't add POS annotations. Cannot proceed."); 
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
//					throw new FragmentAnnotatorException("Unable to annotate fragments on the input CAS", e);
					modLogger.info("No fragment annotations were added on the input CAS");
				}
				
				// all right. Fragments added. Try again
				fragIndex = aJCas.getAnnotationIndex(FragmentAnnotation.type);
				fragItr = fragIndex.iterator();
				
				if (!fragItr.hasNext()) {
//					throw new ModifierAnnotatorException("The CAS had no fragment annotations, and the fragment annotator " + this.fragAnn.getClass().getName() + " could not create any");
					modLogger.info("The CAS had no fragment annotations, and the fragment annotator " + this.fragAnn.getClass().getName() + " could not create any");
				}
								
			} else {
//				throw new ModifierAnnotatorException("The CAS had no fragment annotations, and the ModifierAnnotator instance had no fragment annotator to create some.");
				modLogger.info("The CAS had no fragment annotations, and the ModifierAnnotator instance had no fragment annotator to create some.");				
			}
		}
		
		return addModifierAnnotations(fragItr, aJCas);
	}
	
	
	/**
	 * Iterates through all the annotated fragments to add modifier annotators
	 * 
	 * @param fragItr -- iterator over the fragment annotations in the given CAS object
	 * @param aJCas -- CAS object with annotations
	 * @return
	 * @throws ModifierAnnotatorException
	 */
	protected int addModifierAnnotations(Iterator<Annotation> fragItr, JCas aJCas) throws ModifierAnnotatorException {
		
		modLogger.info("Annotating TLmodifier annotations on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 
		Integer negationPosition = -1;
		
		int modAnnotCount = 0;
		
		while(fragItr.hasNext()) {
			FragmentAnnotation frag = (FragmentAnnotation) fragItr.next();
					
			if (wantedClasses != null) {
				
				if (checkNegation) {
					negationPosition = AnnotationUtils.checkNegation(frag);
				}
				
				for(Class<? extends POS> cls: wantedClasses) {
					
					modAnnotCount += addModifiers(aJCas, frag, negationPosition, cls);
				}
			}
		}			
		return modAnnotCount;
	}
	
	/**
	 * Adds modifier annotations to a CAS object for a given fragment. These modifiers must have the given POS. 
	 * 
	 * @param aJCas a CAS object
	 * @param frag a fragment annotation in the given CAS object
	 * @param negationPos the position of the negation in the fragment (-1 if there is none)
	 * @param modClass the POS class of the modifiers to be annotated
	 * @throws ModifierAnnotatorException 
	 */
	protected static int addModifiers(JCas aJCas, FragmentAnnotation frag, int negationPos, Class<? extends Annotation> modClass) throws ModifierAnnotatorException {

		Logger modLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.modifierannotator:addModifiers");
		
//		List<? extends Annotation> listMods = JCasUtil.selectCovered(aJCas, modClass, frag);
// Work around, because for English we have double POS annotations
		
		List<? extends Annotation> listMods = AnnotationUtils.selectByPOS(aJCas, frag, modClass);
		int num_mods = 0;
		
		if (listMods != null && ! listMods.isEmpty()) {
			
			modLogger.info("Found " + listMods.size() + " potential modifiers. Negation position: " + negationPos);
			
			for (Annotation a: listMods) {

				if (isModifier(aJCas, a,frag)) {
					
					int begin = a.getBegin(); 
					int end = a.getEnd(); 

					if ( negationPos < 0 ||
							( ! AnnotationUtils.isNegation(a.getCoveredText()) &&
									! AnnotationUtils.inNegationScope(begin, frag, negationPos)) 
							) {
					
						CASUtils.Region[] r = new CASUtils.Region[1]; 
						r[0] = new CASUtils.Region(begin,  end); 
	
						modLogger.info("Annotating the following as a modifier: " + a.getCoveredText() + " (" + modClass.getSimpleName() + ")");
						
						try {
							CASUtils.annotateOneModifier(aJCas, r); 
						} catch (LAPException e) {
							throw new ModifierAnnotatorException("CASUtils reported exception while annotating Modifier, on sentence (" + begin + ","+ end, e );
						}
						num_mods++;
					} else {
						modLogger.info("Potential modifier is (or is in the scope of) a negation: " + a.getCoveredText());
					}
				}
			}
		}
		modLogger.info("Annotated " + num_mods + " for fragment " + frag.getCoveredText());
		return num_mods;
	}

	/**
	 * Check if this a "proper" modifier -- i.e. not in predicative position, 
	 * Simple check: if it is not preceeded by a verb (e.g. "is pretty" ...) then it's OK. make this better
	 * 
	 * @param a
	 * @param frag
	 * @return
	 */
	private static boolean isModifier(JCas aJCas, Annotation a, FragmentAnnotation frag) {
		
		List<Token> anns = JCasUtil.selectPreceding(aJCas, Token.class, a, 1);
		if (anns != null && anns.size() > 0) {
			
			System.out.println("Checking POS: " + anns.get(0).getPos().getClass());
			
			return (! anns.get(0).getPos().equals(V.class));
		}
		
		return true;
	}			
	
}

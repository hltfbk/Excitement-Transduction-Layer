package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.AnnotationUtils;
import eu.excitementproject.tl.laputils.CASUtils.Region;

/**
 * This class implements the keyword-based {@link FragmentAnnotator}. 
 * For each annotated keyword, we extract a fragment based on its dependency relations
 * 
 * Based on the {@link SentenceAsFragmentAnnotator}
 * 
 * @author vivi@fbk
 *
 */
public class KeywordBasedFragmentAnnotator extends AbstractFragmentAnnotator {
	
	Logger logger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator.KeywordBasedFragmentAnnotator");

	/**
	 * Constructor with LAP
	 * 
	 * @param l -- Linguistic Analysis Pipeline for annotation -- must include a dependency parser 
	 * @throws FragmentAnnotatorException
	 */
	public KeywordBasedFragmentAnnotator(LAPAccess l) throws FragmentAnnotatorException
	{
		super(l); 
	}
	
	
	/**
	 * Adds fragment annotations based on keywords and their surrounding dependency relations
	 * 
	 * @param aJCas
	 */
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {

		Logger fragLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator"); 
		int num_frag = 0; 
		
		// first of all, check if determined fragmentation is there or not. 
		// If it is there, we don't run and pass 
		// check the annotated data
		AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
		if (frgIndex.size() > 0)
		{
			fragLogger.info("The CAS already has " + frgIndex.size() + " determined fragment annotation. Won't process this CAS."); 
			return; 
		}
		
		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		if (tokens == null || tokens.size() == 0) {
			try {
				fragLogger.info("Annotating CAS object with LAP " + this.getLap().getClass());
				this.getLap().addAnnotationOn(aJCas);
			} catch (LAPException e) {
				throw new FragmentAnnotatorException("CASUtils reported exception while trying to add annotations on CAS " + aJCas.getDocumentText(), e );														
			}
		}
		
		// check for keyword annotations
		Collection<KeywordAnnotation> keywords = JCasUtil.select(aJCas, KeywordAnnotation.class);
//		Collection<KeywordAnnotation> keywords = (Collection<KeywordAnnotation>) AnnotationUtils.collectAnnotations(aJCas, KeywordAnnotation.class);

		
		if (keywords != null && keywords.size() > 0) {
		
			fragLogger.info("The text has " + keywords.size() + " keywords: " + getCoveredText(keywords));
			
			Collection<Dependency> dependencies = JCasUtil.select(aJCas, Dependency.class);
					
			// if there are dependencies, make fragments
			if (dependencies != null && dependencies.size() > 0) {

				fragLogger.info("Annotating determined fragments on CAS using keywords. CAS Text is: \"" + aJCas.getDocumentText() + "\"."); 

				// depending on how the keyword annotation was done, there may be more than one keyword in a fragment;
				// so the fragments must be filtered
				Set<Set<Region>> detFrags = new HashSet<Set<Region>>();
				
				for(KeywordAnnotation k: keywords) {

					Set<Region> frags = makeOneFragment(aJCas, k, 0);
					if (frags != null && frags.size() >0 ) {
						detFrags.add(frags);
						num_frag++;
					}
				}
				
				// filter fragments to avoid duplicates (subsumed fragments)
				detFrags = filterFragments(detFrags);
				
				// insert annotations
				
//				insert determined fragments as fragment parts if necessary!
				annotateDeterminedFragments(aJCas, detFrags);
				
			} else {
				fragLogger.info("No dependency annotations found (and could not create them either)");
			}
		
		} else {
			fragLogger.info("No keyword annotations found");
		}
		
		fragLogger.info("Annotated " + num_frag + " determined fragments"); 
	}


	/**
	 * Gathers and returns the text covering a collection of (possibly non-contiguous) annotations
	 * 
	 * @param annotations
	 * @return text corresponding to the given annotations
	 */
	private String getCoveredText(Collection<?> annotations) {
		String s = "";
		for(Object a: annotations) {
			s += ((Annotation) a).getCoveredText() + " ";
		}
		return s;
	}

	// check for overlapping fragments and keep the largest only
	// if there is partial overlap, should we merge the fragments?
	// I thought yes, but then we can have conjuctions that partly overlap and should not be merged!
	/**
	 * Filter out fragments that are subsumed by others. 
	 * It can happen if there are multiple keyword annotations
	 * 
	 * @param detFrags -- the fragments built on the given keywords
	 * @return a filtered set of fragments
	 */
	private Set<Set<Region>> filterFragments(Set<Set<Region>> detFrags) {

		Set<Set<Region>> newFrags = new HashSet<Set<Region>>();
		boolean change = false;
		
		for (Set<Region> frag : detFrags) {
			change = false;
			Set<Set<Region>> _newFrags = new HashSet<Set<Region>>(newFrags);
			for (Set<Region> f: _newFrags) {

				// if one Region includes the other
				
				logger.info("Comparing fragments: \n\t\tfrag: " + getCoveredSpan(frag) + "\n\t\tf: " + getCoveredSpan(f));
						
				if (frag.containsAll(f)) {
					newFrags.remove(f);
					newFrags.add(frag);
					change = true;
					logger.info("\tCHANGED f with frag");
				} else {
					if (f.containsAll(frag)) {
						newFrags.remove(frag);
						newFrags.add(f);
						change = true;
						logger.info("\tCHANGED frag with f");
					}
				
				}
			}
			if (! change) 
				newFrags.add(frag);
		}

		if (change) 
			return filterFragments(newFrags);
		
		return newFrags;
	}


	/**
	 * Gathers and returns information about a set of Regions that make up a fragment
	 * 
	 * @param frag
	 * @return
	 */
	private String getCoveredSpan(Set<Region> frag) {
		String str = "";
		for(Region s: frag) {
			str += s.toString() + " ";
		}
		return str;
	}

	/**
	 * insert DeterminedFragment annotations in the CAS
	 * 
	 * @param aJCas
	 * @param detFrags
	 * @throws FragmentAnnotatorException
	 */
	private void annotateDeterminedFragments(JCas aJCas, Set<Set<Region>> detFrags) throws FragmentAnnotatorException{
		for(Set<Region> frag: detFrags) {
			AnnotationUtils.annotateDeterminedFragment(aJCas, frag);
		}
	}


	/**
	 * Makes one fragment centered on a given keyword
	 * 
	 * @param aJCas
	 * @param k
	 * @param step -- because there are sometime circular dependencies, this parameter is used to control the number of calls 
	 * @return
	 */
	private Set<Region> makeOneFragment(JCas aJCas, Annotation k, int step) {

		Set<Region> frags = null;
		Sentence coveringSentence = (Sentence) JCasUtil.selectCovering(aJCas, Sentence.class, k.getBegin(), k.getEnd()).get(0);
		
		if (step > 2) 
			return frags;
		
		if (isNorV(aJCas, k) && AnnotationUtils.isGovernorInDeps(aJCas, k)) {
//		if (isNorV(aJCas, k) || (! isGovernorInDeps(aJCas, k))) {  // if it is noun or verb, or if the word does not have a governor, then expand it instead of trying to go up)
			frags = AnnotationUtils.getRegions(k.getCoveredText(), k.getBegin(), k.getEnd(), aJCas, coveringSentence, "any");
		} else {
			if (! AnnotationUtils.isGovernorInDeps(aJCas, k)) {
				frags = AnnotationUtils.getRegions(k.getCoveredText(), k.getBegin(), k.getEnd(), aJCas, coveringSentence, "any");
			}
		}

		// start generating the fragment from one level up
		// this happens if the keyword is only Dependent but never Governor in dependency relations
		// or if it is not a noun or verb; 
		// or if it is a noun or verb with no dependencies of its own, to avoid "unigram" fragments

		if (frags == null || frags.size() == 0) {
			Token t = AnnotationUtils.getGovernor(k, aJCas);

			if (t != null && 
					(! t.getCoveredText().contentEquals(k.getCoveredText()))
				) {
				logger.info("building fragment with head token " + t.getCoveredText());
				frags = makeOneFragment(aJCas, t, step+1);
			}
		}
		
		return frags;
	}
	

	/**
	 * Checks if a given word (passed as Annotation) in a text is a noun or verb
	 * (used to avoid centering fragments on adjectives/adverbs)
	 * 
	 * @param aJCas
	 * @param k
	 * @return
	 */
	private boolean isNorV(JCas aJCas, Annotation k) {

		Collection<POS> pos = JCasUtil.selectCovering(aJCas, POS.class, k.getBegin(), k.getEnd());
		
		for(POS p: pos) {
			logger.info("POS for " + k.getCoveredText() + " : " + p.getPosValue());
//			if (p.getPosValue().matches("^(N|V).*"))
			if (p.getPosValue().matches("^V.*"))
				return true;
		}
		return false;
	}

}

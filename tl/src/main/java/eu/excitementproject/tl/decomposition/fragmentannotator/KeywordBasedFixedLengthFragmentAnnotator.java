package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CASUtils.Region;

/**
 * This class implements the keyword-based "fragment annotator". 
 * For each annotated keyword, we extract a fragment based on its dependency relations
 * 
 * Based on the SentenceAsFragmentAnnotator
 * 
 * @author vivi@fbk
 *
 */
public class KeywordBasedFixedLengthFragmentAnnotator extends AbstractFragmentAnnotator {
	
	Logger logger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator.KeywordBasedFixedLengthFragmentAnnotator");

	int windowSize = 3;
	
	public KeywordBasedFixedLengthFragmentAnnotator(LAPAccess l) throws FragmentAnnotatorException
	{
		super(l); 
	}
	
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {

		int num_frag = 0; 
		
		// first of all, check if determined fragmentation is there or not. 
		// If it is there, we don't run and pass 
		// check the annotated data
		AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
		if (frgIndex.size() > 0)
		{
			logger.info("The CAS already has " + frgIndex.size() + " determined fragment annotation. Won't process this CAS."); 
			return; 
		}

		Collection<Token> tokens = JCasUtil.select(aJCas, Token.class);
		if (tokens == null || tokens.size() == 0) {
			try {
				this.getLap().addAnnotationOn(aJCas);
			} catch (LAPException e) {
				throw new FragmentAnnotatorException("CASUtils reported exception while trying to add annotations on CAS " + aJCas.getDocumentText(), e );														
			}
		}
		
		tokens = JCasUtil.select(aJCas, Token.class);
		
		if (tokens != null && tokens.size() > 0) {

			// check for keyword annotations
			Collection<KeywordAnnotation> keywords = JCasUtil.select(aJCas, KeywordAnnotation.class);
				
			if (keywords != null && keywords.size() > 0) {
		
				logger.info("The text has " + keywords.size() + " keywords: " + getCoveredText(keywords));
				logger.info("Annotating determined fragments on CAS using keywords. CAS Text is: \"" + aJCas.getDocumentText() + "\"."); 
							
				// depending on how the keyword annotation was done, there may be more than one keyword in a fragment;
				// so the fragments must be filtered
				Set<Region> detFrags = new HashSet<Region>();
				
				for(KeywordAnnotation k: keywords) {

					Region frag = makeOneFragment(aJCas, k);
					if (frag != null) {
						detFrags.add(frag);
						num_frag++;
					}
				}
				
				// 	filter fragments to avoid duplicates (subsumed fragments)
				detFrags = filterFragments(detFrags);
				
				//  insert annotations			
				//	insert determined fragments as fragment parts if necessary!
				annotateDeterminedFragments(aJCas, detFrags);
			} else {
				logger.info("No keyword annotations found");
			}
		} else {
			logger.info("No token annotations found");
		}
		
		logger.info("Annotated " + num_frag + " determined fragments"); 
	}


	/** 
	 * To generate fragments centered on given keywords with adjustable window size
	 * 
	 * @param aJCas
	 * @param winSize
	 * @throws FragmentAnnotatorException
	 */
	public void annotateFragments(JCas aJCas, int winSize) throws FragmentAnnotatorException {
		windowSize = winSize;
		annotateFragments(aJCas);
	}
	
	
	
	public static String getCoveredText(Collection<?> annotations) {
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
	private Set<Region> filterFragments(Set<Region> detFrags) {

		Set<Region> newFrags = new HashSet<Region>();
		boolean change = false;
		
		for (Region frag : detFrags) {
			change = false;
			Set<Region> _newFrags = new HashSet<Region>(newFrags);
			for (Region f: _newFrags) {
				
				// if the fragments intersect
				if (((frag.getBegin() - f.getBegin()) * (f.getBegin() - frag.getEnd()) > 0) 
						||
						((frag.getBegin() - f.getEnd()) * (f.getEnd() - frag.getEnd()) > 0)) {
					newFrags.remove(f);
					newFrags.add(new Region(Math.min(frag.getBegin(), f.getBegin()), Math.max(frag.getEnd(), f.getEnd())));
					change = true;
//					logger.info("\tCombining fragments: \n\t\t(" + f.getBegin() + "," + f.getEnd() + ")\n\t\t(" + frag.getBegin() + "," + frag.getEnd() + ")" );
				}
			}
			if (! change) {
//				logger.info("\tADDED frag");
				newFrags.add(frag);
			}
		}

		if (change) {
			return filterFragments(newFrags);
		}
		
		return newFrags;
	}


	/**
	 * insert DeterminedFragment annotations in the CAS
	 * 
	 * @param aJCas
	 * @param detFrags
	 * @throws FragmentAnnotatorException
	 */
	private void annotateDeterminedFragments(JCas aJCas, Set<Region> detFrags) throws FragmentAnnotatorException{

//		detFrags = RegionUtils.compressRegions(detFrags);

		for(Region frag: detFrags) {
			if (frag != null) {
				// compress the spans (by concatenating adjacent portions) before generating the fragment annotation
				logger.info("Adding fragment with Region: " + frag.getBegin() + " - " + frag.getEnd());
				CASUtils.Region[] r = new CASUtils.Region[1];
				r[0] =  new CASUtils.Region(frag.getBegin(), frag.getEnd());

				try {
					CASUtils.annotateOneDeterminedFragment(aJCas, r);
				} catch (LAPException e) {
					throw new FragmentAnnotatorException("CASUtils reported exception while adding Fragment on keyword " + getCoveredText(aJCas,r) , e );									
				}
			}
		}
	}

	
	/**
	 * return the text covered by an array of regions
	 * 
	 * @param aJCas
	 * @param r
	 * @return
	 */
	private String getCoveredText(JCas aJCas, Region[] r) {
		String s = "";
		
		for(int i = 0; i < r.length; i++) {
			s += aJCas.getDocumentText().substring(r[i].getBegin(), r[i].getEnd()) + " ";
		}
		
		return s;
	}

	/**
	 * Makes one fragment centered on a given keyword
	 * 
	 * @param aJCas
	 * @param k
	 * @return
	 */
	private Region makeOneFragment(JCas aJCas, Annotation k) {

		logger.info("Forming fragment for keyword: " + k.getCoveredText() + " (" + k.getBegin() + "," + k.getEnd() + ")");
				
		List<Token> tokens = JCasUtil.selectCovered(aJCas, Token.class, k);
//		List<Token> tokens = JCasUtil.selectCovering(aJCas, Token.class, k.getBegin(), k.getEnd()); // this gave errors for interaction 0320.txt, focus "TUO 444"
			
		if (tokens == null || tokens.isEmpty()) {
			logger.error("Null (or empty) tokens list!");
		} else {

			Sentence coveringSentence = (Sentence) JCasUtil.selectCovering(aJCas, Sentence.class, k.getBegin(), k.getEnd());

			int fragStart = addPrecedingTokens(aJCas, tokens.get(0), windowSize, coveringSentence);
			int fragEnd = addFollowingTokens(aJCas, tokens.get(tokens.size()-1), windowSize, coveringSentence);

			logger.info("Fragment: " + aJCas.getDocumentText().substring(fragStart, fragEnd));
			
			return new Region(fragStart, fragEnd);
		}
		return null;
	}

	
	/**
	 * Add following tokens to the given keyword within the given keyword and the keyword's sentence
	 * Do not jump sentence boundaries
	 * 
	 * @param aJCas
	 * @param k
	 * @return
	 */
	private int addFollowingTokens(JCas aJCas, Annotation k, int count, Annotation sentence) {

		int end = k.getEnd();
		Annotation last = k;
		// count only "proper" tokens, and compensate for skipped ones by adding more tokens
		int skipped = 0;

		
		for (Annotation a: JCasUtil.selectFollowing(aJCas, Token.class, k, count)) {
			if (isCoveredBy(a, sentence)) {
				if (a.getEnd() > end) {
					end = a.getEnd();
					last = a;
				}	
				if (! a.getCoveredText().matches(".*\\w+.*")) {
					skipped++;
				}
			}
		}
		
		if (skipped > 0) {
			return addFollowingTokens(aJCas, last, skipped, sentence);
		}
		
		return end;
	}


	/**
	 * Add preceding tokens to the given keyword within the given window and the keyword's sentence
	 * 
	 * @param aJCas
	 * @param k
	 * @param coveringSentence 
	 * @return
	 */
	private int addPrecedingTokens(JCas aJCas, Annotation k, int count, Sentence sentence) {

		int start = k.getBegin();
		Annotation first = k;
		int skipped = 0;
		
		for (Annotation a: JCasUtil.selectPreceding(aJCas, Token.class, k, count)) {
			if (isCoveredBy(k, sentence)) {
				if (a.getBegin() < start) {
					start = a.getBegin();
					first = a;
				}
				if (! a.getCoveredText().matches(".*\\w+.*")) {
					skipped++;
				}
			}
		}
		
		if (skipped > 0) {
			return addPrecedingTokens(aJCas, first, skipped, sentence);
		}
		
		return start;
	}
	

	
	/**
	 * Check if a given annotation is within the given sentence
	 *  
	 * @param a an annotation
	 * @param sentence a sentence annotation
	 * @return true if the annotation is within the scope of the sentence
	 */
	private boolean isCoveredBy(Annotation a, Annotation sentence) {
		return ((sentence.getBegin() <= a.getBegin()) 
				&& (a.getEnd() <= sentence.getEnd()));
	}
	
}

package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
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
public class KeywordBasedFragmentAnnotator extends AbstractFragmentAnnotator {
	
	Logger logger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator.KeywordBasedFragmentAnnotator");

	public KeywordBasedFragmentAnnotator(LAPAccess l) throws FragmentAnnotatorException
	{
		super(l); 
	}
	
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
		
		// check for keyword annotations
		Collection<KeywordAnnotation> keywords = JCasUtil.select(aJCas, KeywordAnnotation.class);
				
		if (keywords != null && keywords.size() > 0) {
		
			fragLogger.info("The text has " + keywords.size() + " keywords: " + getCoveredText(keywords));
			
			Collection<Dependency> dependencies = JCasUtil.select(aJCas, Dependency.class);
			
			// if there are no dependency annotations, call the LAP to make some
			if (dependencies == null || dependencies.size() == 0) {
				try {
					this.getLap().addAnnotationOn(aJCas);
				} catch (LAPException e) {
					throw new FragmentAnnotatorException("CASUtils reported exception while trying to add annotations on CAS " + aJCas.getDocumentText(), e );														
				}
			}
			
			// if there are dependencies now, then make fragments
			if (dependencies != null && dependencies.size() > 0) {

				fragLogger.info("Annotating determined fragments on CAS using keywords. CAS Text is: \"" + aJCas.getDocumentText() + "\"."); 

				// depending on how the keyword annotation was done, there may be more than one keyword in a fragment;
				// so the fragments must be filtered
				Set<Set<Region>> detFrags = new HashSet<Set<Region>>();
				
				for(KeywordAnnotation k: keywords) {

					Set<Region> frags = makeOneFragment(aJCas, k);
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
				// if they overlap
/*				Set<Region> union = new HashSet<Region>(f);
				union.addAll(frag);
				if (frag.size() + f.size() > union.size()) {
						newFrags.remove(f);
						newFrags.add(union);
						change = true;
				}
*/
				// if one Region includes the other
				
//				logger.info("Comparing fragments: \n\t\tfrag: " + getCoveredSpan(frag) + "\n\t\tf: " + getCoveredSpan(f));
				
				if (frag.containsAll(f)) {
					newFrags.remove(f);
					newFrags.add(frag);
					change = true;
//					logger.info("\tCHANGED f with frag");
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

		for(Set<Region> frags: detFrags) {
			if (frags != null && frags.size() > 0) {
				// compress the spans (by concatenating adjacent portions) before generating the fragment annotation
				frags = compressSpans(frags);
				logger.info("Adding fragment with Region: " + getCoveredSpan(frags));
				CASUtils.Region[] r = new CASUtils.Region[frags.size()];
				int i = 0;
				for(Region s: frags) {
					r[i++] = new CASUtils.Region(s.getBegin(), s.getEnd());
				}
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
	private Set<Region> makeOneFragment(JCas aJCas, Annotation k) {

		Set<Region> frags = null;
		
		if (isNorV(aJCas, k) && isGovernorInDeps(aJCas, k)) { 
			frags = getFragment(k.getCoveredText(), k.getBegin(), k.getEnd(), aJCas);
		}

		// start generating the fragment from one level up
		// this happens if the keyword is only Dependent but never Governor in dependency relations
		// or if it is not a noun or verb; 
		// or if it is a noun or verb with no dependencies of its own, to avoid "unigram" fragments

		if (frags == null || frags.size() == 0) {
			Token t = getGovernor(k, aJCas);

			if (t != null && 
					(! t.getCoveredText().matches(k.getCoveredText()))
				) {
				logger.info("building fragment with head token " + t.getCoveredText());
				frags = makeOneFragment(aJCas, t);
			}
		}
		
		return frags;
	}
	
	
	/**
	 * Check if a given word (passed as an Annotation parameter) appears as governor in some dependency relation
	 * (used to avoid having unigram fragments)
	 *  
	 * @param aJCas
	 * @param k
	 * @return true if it is governor at least once, false otherwise
	 */
	private boolean isGovernorInDeps(JCas aJCas, Annotation k) {
		
		List<Dependency> deps = JCasUtil.selectCovering(aJCas, Dependency.class, k.getBegin(), k.getEnd());
		
		logger.info("Checking if " + k.getCoveredText() + " is governor in some dependencies");
		
		if (deps != null && deps.size() > 0) {
			for (Dependency d: deps) {
				if (d.getGovernor().getCoveredText().matches(k.getCoveredText())) 
					return true;
			}
		}
		
		return false;
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

	/**
	 * get the governor (as in a dependency relation) for the given word
	 * 
	 * @param k -- a keyword annotation
	 * @return -- the Token corresponding to the governor found, or null
	 */
	private Token getGovernor(Annotation k, JCas aJCas) {
				
		List<Dependency> deps = JCasUtil.selectCovering(aJCas, Dependency.class, k.getBegin(), k.getEnd());
				
		if (deps != null && deps.size() > 0) {
			for(Dependency d: deps) {
//				if (d.getDependent().getCoveredText().matches(k.getCoveredText())) {
				if (d.getDependent().getCoveredText().contains(k.getCoveredText())) {  // contains instead of matches for words separated by "-" (which the tokenizer will not split, even if sometimes it should
					return d.getGovernor();
				}
			}
		}
		
		return null;
	}

	
	/**
	 * create a text fragment by taking all dependents (recursively) of a given word
	 * 
	 * @param word -- the starting word for forming the fragment
	 * @param begin -- beginning position of the word
	 * @param end -- end position of the word
	 * @param aJCas -- CAS object
	 * @return a set of spans corresponding to the fragment. Maybe we could compress this by merging adjacent fragments
	 */
	private Set<Region> getFragment(String word, int begin, int end,
			JCas aJCas) {
		
//		logger.info("\tbuildling fragment around " + word + " (" + begin + "," + end + ")");
		
//		List<Dependency> deps = JCasUtil.selectCovering(aJCas, Dependency.class, begin, end);
		List<Dependency> deps = getRelevantDependencies(aJCas, begin, end);
		
		// create the spans sorted by their location in the text, so they can be properly concatenated
		SortedSet<Region> spans = new TreeSet<Region>(new Comparator<Region>() {
			public int compare(Region a, Region b) {
				if (a.getBegin() < b.getBegin()) { return -1; }
				if (a.getBegin() > b.getBegin()) { return 1; }
				return 0;
			}
		});
	
		spans.add(new Region(begin,end));
		
		if (deps != null && deps.size() > 0) {
			for(Dependency d: deps) {
				
				logger.info("\t\tdependency: (" + d.getGovernor().getCoveredText() + "," + d.getDependent().getCoveredText() + ") => " + d.getDependencyType());
				
				if (d.getGovernor().getCoveredText().matches(word)) {
					Token t = d.getDependent();
					Set<Region> newSpans = getFragment(t.getCoveredText(), t.getBegin(), t.getEnd(), aJCas);
					if (newSpans != null && newSpans.size() > 0) {
						spans.addAll(newSpans);
					}
				}
			}
		}
				
	
// compress the spans only after overlap/redundancy check!		
/*		if (spans.size() > 1) {
			return compressSpans(spans);
		}
*/		
		return spans;
	}


	// necessary because the begin/end of each dependency is the entire text, so we get all the dependencies in the text using selectCovering(...) while selectCovered(...) is too restrictive
	private List<Dependency> getRelevantDependencies(JCas aJCas, int begin,	int end) {
		
		List<Dependency> deps = JCasUtil.selectCovering(aJCas, Dependency.class, begin, end);
		List<Dependency> relevantDeps = new ArrayList<Dependency>();
		
		for (Dependency d: deps) {
			if ( ( ! d.getGovernor().getCoveredText().equals(d.getDependent().getCoveredText())) // this test is because of a strange dependency noted while using OMQ with MaltParser (im,im) => MNR
					&&
				 ( d.getDependent().getCoveredText().matches("\\w.*")) // avoid punctuation (at least starts with letter)
					&&
				 ( (d.getGovernor().getBegin() == begin && d.getGovernor().getEnd() == end)
				     || 
				   (d.getDependent().getBegin() == begin && d.getDependent().getEnd() == end))
				) {
				relevantDeps.add(d);
			}
		}
		
		return relevantDeps;
	}

	/**
	 * Compresses the previously generated spans by aggregating adjacent ones
	 * We know it contains more than one Region
	 * 
	 * @param spans
	 * @return a compressed version of the given spans
	 */
	private Set<Region> compressSpans(Set<Region> spanset) {
		
		Region[] spans = spanset.toArray(new Region[spanset.size()]);

		SortedSet<Region> compressedSpans = new TreeSet<Region>(new Comparator<Region>() {
			public int compare(Region a, Region b) {
				if (a.getBegin() < b.getBegin()) { return -1; }
				if (a.getBegin() > b.getBegin()) { return 1; }
				return 0;
			}
		});
				
		if (spans != null && spans.length > 0) {  
		
			int begin = spans[0].getBegin();	
			int end = spans[0].getEnd();
		
			for(int i=1; i < spans.length; i++) {
						
				if ( (0 <= (spans[i].getBegin() - end)) && ((spans[i].getBegin() - end) <= 3)) {
					end = spans[i].getEnd();
				} else {
					compressedSpans.add(new Region(begin, end));
					begin = spans[i].getBegin();
					end = spans[i].getEnd();
				}			
			}
		
			compressedSpans.add(new Region(begin, end));
		}
		
		return compressedSpans;
	}

}

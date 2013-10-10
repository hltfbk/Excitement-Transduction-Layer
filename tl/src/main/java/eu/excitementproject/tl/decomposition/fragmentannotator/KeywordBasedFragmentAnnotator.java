package eu.excitementproject.tl.decomposition.fragmentannotator;

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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;

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

	public KeywordBasedFragmentAnnotator(LAPAccess l) throws FragmentAnnotatorException
	{
		super(l); 
	}
	
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {

		Logger fragLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator"); 
		int num_frag = 0; 
		
		// first of all, check determined fragmentation is there or not. 
		// If it is there, we don't run and pass 
		// check the annotated data
		AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
		if (frgIndex.size() > 0)
		{
			fragLogger.info("The CAS already has " + frgIndex.size() + " determined fragment annotation. Won't process this CAS."); 
			return; 
		}
		
		// check sentence annotation is there or not
		Set<KeywordAnnotation> keywords = (Set<KeywordAnnotation>) JCasUtil.select(aJCas, KeywordAnnotation.class);
		Set<Dependency> dependencies = (Set<Dependency>) JCasUtil.select(aJCas, Dependency.class);
		
		if (keywords != null && keywords.size() > 0) {
			
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

				for(KeywordAnnotation k: keywords) {
					fragLogger.info("\tadding fragment on keyword " + k.getCoveredText());

					Set<Span> frags = getFragment(k.getCoveredText(), k.getBegin(), k.getEnd(), aJCas);
					
					// this happens if the keyword is only Dependent but never Governor in dependency relations
					// then go one level up instead
					if (frags == null || frags.size() == 0) {
						Token t = getGovernor(k, aJCas);
						if (t != null) {
							frags = getFragment(t.getCoveredText(), t.getBegin(), t.getEnd(), aJCas);
							if (frags != null && frags.size() > 0) {
								CASUtils.Region[] r = new CASUtils.Region[frags.size()];
								int i = 0;
								for(Span s: frags) {
									r[i] = new CASUtils.Region(s.getBegin(), s.getEnd());
								}
								try {
									CASUtils.annotateOneDeterminedFragment(aJCas, r);
									num_frag++; 
								} catch (LAPException e) {
									throw new FragmentAnnotatorException("CASUtils reported exception while annotating Fragment on keyword " + k.getCoveredText(), e );									
								}
							}
						}
					}
				}
			} else {
				fragLogger.info("No dependency annotations found (and could not create them either)");
			}
			
		} else {
			fragLogger.info("No keyword annotations found");
		}
		
		fragLogger.info("Annotated " + num_frag + " determined fragments"); 
	}

	
	/**
	 * get the governor (as in a dependency relation) for the given word
	 * 
	 * @param k -- a keyword annotation
	 * @return -- the Token corresponding to the governor found, or null
	 */
	private Token getGovernor(KeywordAnnotation k, JCas aJCas) {
		
		List<Dependency> deps = JCasUtil.selectCovering(aJCas, Dependency.class, k.getBegin(), k.getEnd());
		
		if (deps != null && deps.size() > 0) {
			for(Dependency d: deps) {
				if (d.getDependent().getCoveredText().matches(k.getCoveredText())) {
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
	private Set<Span> getFragment(String word, int begin, int end,
			JCas aJCas) {
		
		List<Dependency> deps = JCasUtil.selectCovering(aJCas, Dependency.class, begin, end);
		SortedSet<Span> spans = new TreeSet<Span>(new Comparator<Span>() {
			public int compare(Span a, Span b) {
				if (a.getBegin() < b.getBegin()) { return -1; }
				if (a.getBegin() > b.getBegin()) { return 1; }
				return 0;
			}
		});
		
		if (deps != null && deps.size() > 0) {
			for(Dependency d: deps) {
				if (d.getGovernor().getCoveredText().matches(word)) {
					Token t = d.getDependent();
					spans.add(new Span(t.getBegin(), t.getEnd()));
					spans.addAll(getFragment(t.getCoveredText(), t.getBegin(), t.getEnd(), aJCas));
				}
			}
		}
		
		if (spans.size() > 1) {
			return compressSpans((Span[]) spans.toArray());
		}
		
		return spans;
	}


	/**
	 * Compresses the previously generated spans by aggregating adjacent ones
	 * We know it contains more than one span
	 * 
	 * @param spans
	 * @return a compressed version of the given spans
	 */
	private Set<Span> compressSpans(Span[] spans) {
		
		Set<Span> compressedSpans = new HashSet<Span>();
		
		int begin = spans[0].getBegin();
		int end = spans[0].getEnd();
		
		for(int i=1; i < spans.length; i++) {
			if (spans[i].getBegin() == end+2) {
				end = spans[i].getEnd();
			} else {
				compressedSpans.add(new Span(begin, end));
				begin = spans[i].getBegin();
				end = spans[i].getEnd();
			}
		}
		
		compressedSpans.add(new Span(begin, end));
		
		return compressedSpans;
	}

}

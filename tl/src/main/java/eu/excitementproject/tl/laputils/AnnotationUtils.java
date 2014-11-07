package  eu.excitementproject.tl.laputils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import com.aliasi.util.Collections;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitement.type.tl.FragmentPart;
import eu.excitement.type.tl.KeywordAnnotation;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils.Region;

/**
 * The class holds various small utility static methods that might be 
 * useful in handling CASes: Like getting a new CAS, serialize, deserialize, 
 * adding some annotations of TL inputCAS, etc. 
 * 
 * @author vivi@fbk 
 */


@SuppressWarnings("unused")
public final class AnnotationUtils {
	
	
	/**
	 * Add a set of annotations of a given class to a new CAS object (used for transfering keyword annotation, in particular, 
	 * from a gold-standard CAS read from an xmi file, to a new one (that only contains the interaction text) for testing purposes
	 *   
	 * @param goldJCas a CAS object containing annotations 
	 * @param aJCas a new CAS object to which we will transfer some of the annotations from goldJCas
	 * @param AnnotationClass the class of annotations to be transfered (e.g. KeywordAnnotation)
	 */
	public static void transferAnnotations(JCas goldJCas, JCas aJCas, Class<? extends Annotation> AnnotationClass) {

		Logger logger = Logger.getLogger("eu.excitementproject.tl.laputils.AnnotationUtils.transferAnnotations");
		
		Collection<? extends Annotation> annotations = JCasUtil.select(goldJCas, AnnotationClass);
		String interactionText = aJCas.getDocumentText();
		
		if (annotations != null && annotations.size() > 0) {
				
			logger.info("There are " + annotations.size() + " annotations of class " + AnnotationClass.getName());
			logger.info("Adding these annotations to new CAS with SOFA: \"" + interactionText + "\"."); 
			
			for (Annotation a: annotations) {

				// check if the annotation is legit -- for modifiers it happens that it has 0 span
				if (a.getEnd() - a.getBegin() > 0) {
				
					if (AnnotationClass.equals(KeywordAnnotation.class)) {
									
						CASUtils.Region r = makeRegion(a, interactionText); 
						CASUtils.annotateOneKeyword(aJCas, r);
						
					} else if (AnnotationClass.equals(DeterminedFragment.class)) {
						
//						Collection<FragmentPart> fps = JCasUtil.selectCovered(goldJCas, FragmentPart.class, a);
						FSArray fps = ((DeterminedFragment) a).getFragParts();
						
						CASUtils.Region[] rs;
						
						if (fps.size() > 0) {
							rs = new CASUtils.Region[fps.size()];

							int i = 0;
							for (FragmentPart fp: JCasUtil.select(fps, FragmentPart.class)) {
								rs[i++] = makeRegion(fp, interactionText);
								logger.info("Added region: " + rs[i-1].getBegin() + " -- " + rs[i-1].getEnd() + " of type " + fp.getClass());
							}
						} else {
							rs = new CASUtils.Region[1];
							rs[0] = makeRegion(a, interactionText);
						}
						
						
						try {
							CASUtils.annotateOneDeterminedFragment(aJCas, rs);
						} catch (LAPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				
					logger.info("Added annotation: " + a.getCoveredText() );
					
				} else {
					logger.info("Skipping erroneous annotation: " + a.getBegin() + " - " + a.getEnd());
				}
			}
		}
	}		
	
	
	/**
	 * insert one DeterminedFragment annotation in the CAS
	 * 
	 * @param aJCas
	 * @param detFrags
	 * @throws FragmentAnnotatorException
	 */
	public static void annotateDeterminedFragment(JCas aJCas, Set<Region> frag) throws FragmentAnnotatorException{

		if (frag != null && frag.size() > 0) {
			// compress the spans (by concatenating adjacent portions) before generating the fragment annotation
			frag = RegionUtils.compressRegions(frag);

			CASUtils.Region[] r = new CASUtils.Region[frag.size()];
			int i = 0;
			for(Region s: frag) {
				r[i++] = new CASUtils.Region(s.getBegin(), s.getEnd());
			}
			try {
				CASUtils.annotateOneDeterminedFragment(aJCas, r);
			} catch (LAPException e) {
				throw new FragmentAnnotatorException("CASUtils reported exception while adding Fragment on text " + getCoveredText(aJCas, r) , e );									
			}
		}
	}

	
	/**
	 * insert one (complex) Modifier annotation in the CAS
	 * 
	 * @param aJCas
	 * @param detFrags
	 * @throws ModifierAnnotatorException
	 */
	public static void annotatePhraseModifier(JCas aJCas, Set<Region> frag) {

		if (frag != null && frag.size() > 0) {
			// compress the spans (by concatenating adjacent portions) before generating the fragment annotation
			
			System.out.println("Annotating phrase modifier, but first compress the regions");
			
			frag = RegionUtils.compressRegions(frag);

			CASUtils.Region[] r = new CASUtils.Region[frag.size()];
			int i = 0;
			for(Region s: frag) {
				r[i++] = new CASUtils.Region(s.getBegin(), s.getEnd());
			}
			
			System.out.println("Now calling CASUtils to actually add the modifier");

			try {
				CASUtils.annotateOneModifier(aJCas, r);
			} catch (LAPException e) {
				e.printStackTrace();
				System.out.println("CASUtils reported exception while adding Fragment on text " + getCoveredText(aJCas, r));									
			}
		}
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
	public static void addModifiers(JCas aJCas, FragmentAnnotation frag, int negationPos, Class<? extends Annotation> modClass) throws ModifierAnnotatorException {

		Logger modLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.modifierannotator:addModifiers");
		
		List<? extends Annotation> listMods = JCasUtil.selectCovered(aJCas, modClass, frag);
		int num_mods = 0;
		
		if (listMods != null && ! listMods.isEmpty()) {
			for (Annotation a: listMods) {

				int begin = a.getBegin(); 
				int end = a.getEnd(); 

//				if (! isNegation(a.getCoveredText()) &&
//					! inNegationScope(begin, frag, negationPos)) {
				if ( negationPos < 0 ||
						( ! isNegation(a.getCoveredText()) &&
						  ! inNegationScope(begin, frag, negationPos)) 
					) {
					
					CASUtils.Region[] r = new CASUtils.Region[1]; 
					r[0] = new CASUtils.Region(begin,  end); 
	
					modLogger.info("Annotating the following as a modifier: " + a.getCoveredText());
				
					try {
						CASUtils.annotateOneModifier(aJCas, r); 
					} catch (LAPException e) {
						throw new ModifierAnnotatorException("CASUtils reported exception while annotating Modifier, on sentence (" + begin + ","+ end, e );
					}
					num_mods++;
				} else {
					modLogger.info("Potential modifier is or is in scope of a negation: " + a.getCoveredText());
				}
			}
		}
		modLogger.info("Annotated " + num_mods + " for fragment " + frag.getCoveredText());
		num_mods = 0;
	}			

	
	/**
	 * Checks if there are dependency annotations in a CAS object
	 * 
	 * @param aJCas
	 * @return
	 */
	public static boolean hasDependencyAnnotations(JCas aJCas){
		
		Collection<Dependency> dependencies = JCasUtil.select(aJCas, Dependency.class);
		
		// if there are no dependency annotations, call the LAP to make some
		if (dependencies == null || dependencies.size() == 0) {
			return false;
		}
		return true;
	}
	

	/**
	 * Generate a set of regions that cover a phrase that starts from the given Annotation (word)
	 * 
	 * @param aJCas the CAS object
	 * @param a the given annotation from which the phrase is being generated 
	 * @return
	 */
	public static Set<Region> getPhraseRegion(JCas aJCas, Annotation a){

		Set<Region> frags = null;
			
		if (isGovernorInDeps(aJCas, a)) {
			frags = getFragment(a.getCoveredText(), a.getBegin(), a.getEnd(), aJCas);
		}
			
		return frags;
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
	public static Set<Region> getFragment(String word, int begin, int end, JCas aJCas) {
	
		Logger logger = Logger.getLogger("eu.excitementproject.tl.laputils.AnnotationUtils.getFragment");

		logger.info("\tbuildling fragment around " + word + " (" + begin + "," + end + ")");
	
		//	List<Dependency> deps = JCasUtil.selectCovering(aJCas, Dependency.class, begin, end);
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
			
				//	if (d.getGovernor().getCoveredText().matches(word)) {
				if (d.getGovernor().getCoveredText().equals(word)) {
					Token t = d.getDependent();
					Set<Region> newSpans = getFragment(t.getCoveredText(), t.getBegin(), t.getEnd(), aJCas);
					if (newSpans != null && newSpans.size() > 0) {
						spans.addAll(newSpans);
					}
				}
			}
		}
			
		//compress the spans only after overlap/redundancy check!		
		/*		if (spans.size() > 1) {
			return compressSpans(spans);
		}
		 */		
		return spans;
	}

	
	// necessary because the begin/end of each dependency is the entire text, so we get all the dependencies 
	// in the text using selectCovering(...) while selectCovered(...) is too restrictive
	/**
	 * Return the dependencies in which the word corresponding to the given begin-end interval is the governor
	 * 
	 * @param aJCas the CAS object
	 * @param begin the start position of the governor
	 * @param end the end position of the governor
	 * @return a list of dependencies for which the governor is indeed the governor
	 */
	public static List<Dependency> getRelevantDependencies(JCas aJCas, int begin, int end) {
		
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
	 * Check if a given word (passed as an Annotation parameter) appears as governor in some dependency relation
	 * (used to avoid having unigram fragments)
	 *  
	 * @param aJCas
	 * @param k
	 * @return true if it is governor at least once, false otherwise
	 */
	public static boolean isGovernorInDeps(JCas aJCas, Annotation k) {
		
		List<Dependency> deps = JCasUtil.selectCovering(aJCas, Dependency.class, k.getBegin(), k.getEnd());
		
		if (deps != null && deps.size() > 0) {
			System.out.println("Dependencies found!");
			for (Dependency d: deps) {
				System.out.println("Dep: " + d.getGovernor().getCoveredText() + " -> " + d.getDependent().getCoveredText());
//				if (d.getGovernor().getCoveredText().matches(k.getCoveredText()))
				if (d.getGovernor().getCoveredText().contentEquals(k.getCoveredText()))
					return true;
			}
		}
	
		return false;
	}
	
	
	/**
	 * get the governor (as in a dependency relation) for the given word
	 * 
	 * @param k -- a keyword annotation
	 * @return -- the Token corresponding to the governor found, or null
	 */
	public static Token getGovernor(Annotation k, JCas aJCas) {
				
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
	 * return the text covered by an array of regions
	 * 
	 * @param aJCas
	 * @param r
	 * @return
	 */
	public static String getCoveredText(JCas aJCas, Region[] r) {
		String s = "";
		
		for(int i = 0; i < r.length; i++) {
			s += aJCas.getDocumentText().substring(r[i].getBegin(), r[i].getEnd()) + " ";
		}
		
		return s;
	}
	
	/**
	 * Check if the modifier annotation is a negation
	 * 
	 * @param m - a modifier annotation
	 * @return
	 */
	public static boolean isNegation(String mod) {
		
		String negationPattern = "(no|non|not|n't|kein|keine|keinen|nein|nessun|nessuna|nessuno)";		
		
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
	public static boolean inNegationScope(int m_begin, FragmentAnnotation f, int negationPos) {

		// we could check here only relative to the fragment parts ...
		if (negationPos > 0 && (m_begin - f.getBegin() >= negationPos)) {
			return true;
		}
		
		return false;
	}
	
	



	/** 
	 * Checks if the current fragment contains negations, and sets the negationPosition attribute
	 */
	public static int checkNegation(FragmentAnnotation fragment) {
		
		String negationPattern = "(no|non|not|n't|kein|keine|keinen|nein|nessun|nessuna|nessuno)";

		int negationPos = -1;
		String text = fragment.getCoveredText();
		
		Pattern p = Pattern.compile("\\b" + negationPattern + "\\b",Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);

		if (m.find()) {			
			negationPos = text.indexOf(m.group(1));
		}
		
		return negationPos;
	}

	
	
	public static CASUtils.Region makeRegion(Annotation a, String text) {
		
		String a_text = a.getCoveredText();
		int frag_start = text.indexOf(a_text); 
		int frag_end = frag_start + a_text.length(); 
		
		return new CASUtils.Region(frag_start, frag_end);
	}
	

	/**
	 * Collects the annotations and filters out the empty ones (it happens with the keywords sometimes)
	 * 
	 * @param aJCas
	 * @param class1
	 * @return
	 */
/*	public static Collection<? extends Annotation> collectAnnotations(JCas aJCas,
			Class<? extends Annotation> type) {
		
		Collection<? extends Annotation> anns = JCasUtil.select(aJCas, type);
		anns.removeAll(anns);
		
		for(Annotation a: JCasUtil.select(aJCas, type)) {
			if (a.getEnd() - a.getBegin() > 0) {
				anns.add(type.cast(a));
			}
		}
		
		return anns;
	}
*/


	
}

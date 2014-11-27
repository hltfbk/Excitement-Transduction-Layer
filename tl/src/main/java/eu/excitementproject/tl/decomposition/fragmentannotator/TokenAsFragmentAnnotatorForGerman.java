package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.CARD;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * This class implements a simple "fragment annotator": token + compound part 
 * Namely, each token except the punctuation is considered as a possible (continuous) fragment. Excluded are also one character words except numbers. 
 * If a filter for parts of speech is passed to the constructor, then only tokens are annotated, which match the filter.
 * If a token is a compound word, then the compound parts can be also annotated.
 * 
 * @author Aleksandra (November 2014)
 *
 */
public class TokenAsFragmentAnnotatorForGerman extends TokenAsFragmentAnnotator {

	private final List<String> tokenPOSFilter;
	private final boolean useOnlyHyphenDecomposition;
	private GermanWordSplitter splitter;
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param decompoundWords -- set to true if words are to decompound
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess lap, boolean decompoundWords) 
			throws FragmentAnnotatorException {
		super(lap);
		tokenPOSFilter = null;
		this.useOnlyHyphenDecomposition = false;
		if(decompoundWords){
			try {
				splitter = new GermanWordSplitter();
				splitter.setStrictMode(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param decompoundWords -- set to true if words are to decompound
	 * @param useOnlyHyphenDecomposition -- set to true if only compound words with hyphen should be decompounded
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess lap, boolean decompoundWords, boolean useOnlyHyphenDecomposition) 
			throws FragmentAnnotatorException {
		super(lap);
		tokenPOSFilter = null;
		this.useOnlyHyphenDecomposition = useOnlyHyphenDecomposition;
		if(decompoundWords){
			try {
				splitter = new GermanWordSplitter();
				splitter.setStrictMode(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param tokenPOSFilter - types of parts of speeches of tokens, which which should be annotated 
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess lap, List<String> tokenPOSFilter, boolean decompoundWords) 
			throws FragmentAnnotatorException {
		super(lap); 
		this.tokenPOSFilter = tokenPOSFilter;
		this.useOnlyHyphenDecomposition = false;
		if(decompoundWords){
			try {
				splitter = new GermanWordSplitter();
				splitter.setStrictMode(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param tokenPOSFilter - types of parts of speeches of tokens, which which should be annotated 
	 * @param decompoundWords
	 * @param useOnlyHyphnenDecomposition
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess lap, List<String> tokenPOSFilter, boolean decompoundWords, 
			boolean useOnlyHyphnenDecomposition) throws FragmentAnnotatorException {
		super(lap); 
		this.tokenPOSFilter = tokenPOSFilter;
		this.useOnlyHyphenDecomposition = useOnlyHyphnenDecomposition;
		if(decompoundWords){
			try {
				splitter = new GermanWordSplitter();
				splitter.setStrictMode(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {
		
		Logger fragLogger = Logger.getLogger(this.getClass().getName()); 

		// first of all, check determined fragmentation is there or not. 
		// If it is there, we don't run and pass 
		// check the annotated data
		AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
		
		if (frgIndex.size() > 0)
		{
			fragLogger.info("The CAS already has " + frgIndex.size() + " determined fragment annotation. Won't process this CAS."); 
			return; 
		}
		
		//add lap annotation
		addLAPTokenAnnotation(aJCas);

		//add determined fragment annotation
		fragLogger.info("Annotating determined fragments on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 
		int num_frag = 0; 
		AnnotationIndex<Annotation> tokenIndex = aJCas.getAnnotationIndex(Token.type);
		Iterator<Annotation> tokenItr = tokenIndex.iterator();
		
		while(tokenItr.hasNext()) {
			//annotate each token except punctuation as one fragment, if it matches the filter 
			Token tk = (Token) tokenItr.next(); 
			try {
					if(isAllowed(tk, tokenPOSFilter)){
						if(tk.getCoveredText().length()==1 && !(tk.getPos() instanceof CARD)){
							continue;
						}
						CASUtils.Region[] r = new CASUtils.Region[1];
						r[0] = new CASUtils.Region(tk.getBegin(),  tk.getEnd()); 
						fragLogger.info("Annotating the following as a fragment: " + tk.getCoveredText());
						CASUtils.annotateOneDeterminedFragment(aJCas, r);
						num_frag++;
						if(splitter != null){
							String tokenText = tk.getCoveredText();
							Collection<String> compoundParts = decompoundWord(tokenText, splitter, useOnlyHyphenDecomposition);
							if(compoundParts.size() > 1){
								for(String compoundPart : compoundParts){
									if(compoundPart.length() == 1){
										Character ch = compoundPart.charAt(0);
										if(!Character.isDigit(ch)){
											continue;
										}
									}
									if(compoundPart.length()>1){
										if(!compoundPart.equals(tk.getCoveredText())) {
											int index = tokenText.indexOf(compoundPart);
											int compoundPartBegin = tk.getBegin() + index;
											int compoundPartEnd = compoundPartBegin + compoundPart.length();
											index = compoundPartEnd + 1;
											r[0] = new CASUtils.Region(compoundPartBegin,  compoundPartEnd);
											System.out.println("Annotating the following as a fragment: " + tokenText + " " + aJCas.getDocumentText().substring(compoundPartBegin, compoundPartEnd));
											fragLogger.info("Annotating the following as a fragment: " + aJCas.getDocumentText().substring(compoundPartBegin, compoundPartEnd));
											CASUtils.annotateOneDeterminedFragment(aJCas, r);
											num_frag++;
										}
									} 
								}
							}
						}
					}
//				}
			} 
			
			catch (LAPException e)
			{
				throw new FragmentAnnotatorException("CASUtils reported exception while annotating Fragment, on token (" + tk.getBegin() + ","+ tk.getEnd(), e );
			}
			 
		}
		fragLogger.info("Annotated " + num_frag + " determined fragments"); 
	}
	
	/**
	 * 
	 * @param word
	 * @param splitter
	 * @return
	 */
	private Set<String> decompoundWord(String word, GermanWordSplitter splitter, boolean useOnlyHyphenDecomposition){
		Set<String> splits = new HashSet<String>();
		if(splitter != null){
			String [] hyphenSplits = word.split("[-]");//to deal with compounds "XML-Daten", where the strict method of GermanWordSplitter fails
				for(String hyphenSplit : hyphenSplits) { 
					splits.add(hyphenSplit);
					if(!useOnlyHyphenDecomposition) {
						splits.addAll(splitter.splitWord(hyphenSplit));
					}
				}			
		}
		return splits ;
	}
	
}


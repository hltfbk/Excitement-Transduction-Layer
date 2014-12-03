package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.io.IOException;
import java.util.Arrays;
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
import eu.excitementproject.tl.laputils.POSTag_DE;
import eu.excitementproject.tl.laputils.WordDecompositionType;

/**
 * This class implements a simple "fragment annotator": token + compound part 
 * Namely, each token  is considered as a possible (continuous) fragment. 
 * Excluded are also punctuation tokens, one character words except numbers. 
 * If filters are passed via constructor, then only tokens, which match the filter, are annotated
 * If a token is a compound word, then the compound parts can be also annotated.
 * 
 * @author Aleksandra (November 2014)
 *
 */
public class TokenAsFragmentAnnotatorForGerman extends TokenAsFragmentAnnotator {

	private final List<POSTag_DE> tokenPOSFilter;
	private final WordDecompositionType decompositionType;
	private GermanWordSplitter splitter;
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param decompositionType - WordDecompositionType.NONE or WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess lap, WordDecompositionType decompositionType) 
			throws FragmentAnnotatorException {
		super(lap);
		tokenPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.decompositionType = decompositionType;
		if(!decompositionType.equals(WordDecompositionType.NONE)){
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
	 * @param tokenPOSFilter - types of part of speech (POS), which are allowed to be a POS the token
	 * @param decompositionType - WordDecompositionType.NONE or WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess lap, List<POSTag_DE> tokenPOSFilter, WordDecompositionType decompositionType)
			throws FragmentAnnotatorException {
		super(lap); 
		this.tokenPOSFilter = tokenPOSFilter;
		this.decompositionType = decompositionType;
		if(!decompositionType.equals(WordDecompositionType.NONE)){
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
					String tokenText = tk.getCoveredText();
					if(tokenText.length()==1 && !isDigit(tokenText)){
						continue;
					}
					CASUtils.Region[] r = new CASUtils.Region[1];
					r[0] = new CASUtils.Region(tk.getBegin(),  tk.getEnd()); 
					fragLogger.info("Annotating the following as a fragment: " + tk.getCoveredText());
					CASUtils.annotateOneDeterminedFragment(aJCas, r);
					num_frag++;
					if(splitter != null){
						Collection<String> compoundParts = decompoundWord(tokenText, decompositionType);
						if(compoundParts.size() > 1){
							for(String compoundPart : compoundParts){
								if(compoundPart.length() == 1 && !isDigit(compoundPart)){
										continue;
								}
//								if(compoundPart.length()>1){
									if(!compoundPart.equals(tokenText)) {
										int index = tokenText.indexOf(compoundPart);
										int compoundPartBegin = tk.getBegin() + index;
										int compoundPartEnd = compoundPartBegin + compoundPart.length();
										index = compoundPartEnd + 1;
										r[0] = new CASUtils.Region(compoundPartBegin,  compoundPartEnd);
//										System.out.println("Annotating the following as a fragment: " + tokenText + " " + aJCas.getDocumentText().substring(compoundPartBegin, compoundPartEnd));
										fragLogger.info("Annotating the following as a fragment: " + aJCas.getDocumentText().substring(compoundPartBegin, compoundPartEnd));
										CASUtils.annotateOneDeterminedFragment(aJCas, r);
										num_frag++;
									}
//								} 
							}
						}
					}
				}
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
	private Set<String> decompoundWord(String word, WordDecompositionType decompositionType){
		Set<String> splits = new HashSet<String>();
		if(decompositionType !=  null) {
			if(!decompositionType.equals(WordDecompositionType.NONE)){
				if(splitter != null){
					String [] hyphenSplits = word.split("[-]");//to deal with compounds "XML-Daten", where the strict method of GermanWordSplitter fails
					for(String hyphenSplit : hyphenSplits) { 
						splits.add(hyphenSplit);
						if(decompositionType.equals(WordDecompositionType.NO_RESTRICTION)) {
							splits.addAll(splitter.splitWord(hyphenSplit));
						}
					}
				}
			}
		}
		return splits ;
	}
	
	/**
	 * check if the token type is allowed
	 * return true
	 * @param token -- Token
	 * @param posFilter - List <POSTag_DE>
	 * @return
	 */
	protected boolean isAllowed(Token token, List<POSTag_DE> posFilter){
		POSTag_DE posTagDE = POSTag_DE.mapToPOStag_DE(token.getPos().getPosValue());
			if(posTagDE != POSTag_DE.COMMA 
				&& posTagDE != POSTag_DE.SENTENCE_ENDING_PUNCTUATION 
				&& posTagDE!= POSTag_DE.OTHERS
				&& posFilter.contains(posTagDE))
				return true;
		return false;
	}
	
	/**
	 * check if a String is a digit
	 * @param word
	 * @return
	 */
	private boolean isDigit(String word){
		if(word.length() == 1){
			Character ch = word.charAt(0);
			return Character.isDigit(ch);
		}
		return false;
	}
}


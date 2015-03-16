package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.POSTag_DE;
import eu.excitementproject.tl.laputils.WordDecompositionType;

/**
 * This class implements a token-based {@link FragmentAnnotator} for German: token + compound part 
 * Each token is considered as a possible (continuous) fragment. 
 * Excluded are punctuation tokens, one character words except numbers. 
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
	private JCas tmpCAS; //stores temporarily compound part as JCas to compute the lemma of the compound part, used only if word form not stored as key in the variable tmpLemmaMap
	private Map<String, String> tmpLemmaMap = new HashMap<String, String>(); //maps temporarily word form onto lemma, used to get the lemma without building of JCas 
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param decompositionType - WordDecompositionType.NONE or WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @throws FragmentAnnotatorException
	 * @throws LAPException 
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess lap, WordDecompositionType decompositionType) 
			throws FragmentAnnotatorException, LAPException {
		super(lap);
		tokenPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.decompositionType = decompositionType;
		this.setDecompounderDE(decompositionType);
		if(splitter != null){
			tmpCAS = CASUtils.createNewInputCas();
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param tokenPOSFilter - types of part of speech (POS), which are allowed to be a POS the token
	 * @param decompositionType - WordDecompositionType.NONE or WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @throws FragmentAnnotatorException
	 * @throws LAPException 
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess lap, List<POSTag_DE> tokenPOSFilter, WordDecompositionType decompositionType)
			throws FragmentAnnotatorException, LAPException {
		super(lap); 
		this.tokenPOSFilter = tokenPOSFilter;
		this.decompositionType = decompositionType;
		this.setDecompounderDE(decompositionType);
		if(splitter != null){
			tmpCAS = CASUtils.createNewInputCas();
		}
	}
	
	/**
	 * Annotate each token except punctuation as one fragment, if it matches the filter.
	 * Component parts of a compound are annotated separately. 
	 */
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
		Set<Lemma> lemmasToAdd = new HashSet<Lemma>();
		while(tokenItr.hasNext()) {
			//annotate each token except punctuation as one fragment, if it matches the filter 
			Token tk = (Token) tokenItr.next(); 
			try {
				if(isAllowed(tk, tokenPOSFilter)){
					String tokenText = tk.getCoveredText();
					if(tokenText.length()==1 && !isDigit(tokenText)){
						continue;
					}
					tmpLemmaMap.put(tokenText, tk.getLemma().getValue());
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
										int index = tokenText.toLowerCase().indexOf(compoundPart.toLowerCase());
										int compoundPartBegin = tk.getBegin() + index;
										int compoundPartEnd = compoundPartBegin + compoundPart.length();
										index = compoundPartEnd + 1;
										r[0] = new CASUtils.Region(compoundPartBegin,  compoundPartEnd);
										Lemma compoundLemma = createLemma(aJCas, compoundPart, compoundPartBegin, compoundPartEnd);
										lemmasToAdd.add(compoundLemma);
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
		//add lemma of compound parts to the indexes
		for(Lemma lemma : lemmasToAdd){
			aJCas.addFsToIndexes(lemma);
		}
	}
	
	/**
	 * Creates Lemma for a given word form, which can be found in the inputCas at the specified begin and end index.
	 *  
	 * @param inputCas
	 * @param wordForm
	 * @param begin
	 * @param end
	 * @return
	 */
	private Lemma createLemma(JCas inputCas, String wordForm, int begin, int end) {
		Lemma lemma = new Lemma(inputCas, begin, end);
		try{
			tmpCAS.reset();
			tmpCAS.setDocumentText(wordForm);
			tmpCAS.setDocumentLanguage("DE");
			if(tmpLemmaMap.containsKey(wordForm)){
				lemma.setValue(tmpLemmaMap.get(wordForm));
			} else {
				this.getLap().addAnnotationOn(tmpCAS);
				Token tmpToken  = (Token) tmpCAS.getAnnotationIndex(Token.type).iterator().next();
				lemma.setValue(tmpToken.getLemma().getValue());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return lemma;
	}
	
	/**
	 * 
	 * @param word
	 * @param decompositionType
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
	 * check if the token type is allowed to be annotated
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
	
	/**
	 * 
	 * @param wordDecompositionType
	 */
	private void setDecompounderDE(WordDecompositionType wordDecompositionType){
		if(!wordDecompositionType.equals(WordDecompositionType.NONE)){
			try {
				splitter = new GermanWordSplitter();
				splitter.setStrictMode(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


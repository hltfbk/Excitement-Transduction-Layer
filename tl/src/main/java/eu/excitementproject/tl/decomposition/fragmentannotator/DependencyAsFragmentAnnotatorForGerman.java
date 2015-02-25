package eu.excitementproject.tl.decomposition.fragmentannotator;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.POSTag_DE;
import eu.excitementproject.tl.laputils.WordDecompositionType;

/**
 * This class extends the {@link DependencyAsFragmentAnnotator} to deal with German-language text. 
 * 
 * Each dependency (except when dependent is punctuation token or dependent and governor are identical words) 
 * is considered as a possible fragment. If filters are passed via constructor, then then only dependencies 
 * are annotated, which match the filter.
 * 
 * @author Aleksandra
 *
 */

public class DependencyAsFragmentAnnotatorForGerman extends DependencyAsFragmentAnnotator {
	
	private final List<POSTag_DE> governorPOSFilter;
	private final List<POSTag_DE> dependentPOSFilter;
	
	private final List<String> dependencyTypeFilter;
	private final boolean restrictDependencyType;
	
	private final List<String> governorWordFilter;
	private final List<String> dependentWordFilter;
	private final boolean restrictWordList;
	
	private final WordDecompositionType wordDecompositionType;
	private GermanWordSplitter splitter;
	
	private JCas tmpCAS; //stores temporarily compound part as JCas to compute the lemma of the compound part, used only if word form not stored as key in the variable tmpLemmaMap
	private Map<String, String> tmpLemmaMap = new HashMap<String, String>(); //maps temporarily word form onto lemma, used to get the lemma without building of JCas
	
	/**
	 * 
	 * @param lap - Linguistic analysis pipeline for processing the text
	 * @param wordDecompositionType - WordDecompositionType.NONE, WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * 
	 * @throws FragmentAnnotatorException
	 * @throws LAPException 
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, WordDecompositionType wordDecompositionType) throws FragmentAnnotatorException, LAPException
	{
		super(lap);
		this.restrictDependencyType = false; this.dependencyTypeFilter = null;
		this.governorPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.dependentPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.restrictWordList = false; this.governorWordFilter = null; this.dependentWordFilter = null;
		this.wordDecompositionType = wordDecompositionType;
		this.setDecompounderDE(wordDecompositionType);
		if(splitter != null){
			tmpCAS = CASUtils.createNewInputCas();
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @param wordDecompositionType - WordDecompositionType.NONE, WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @throws FragmentAnnotatorException
	 * @throws LAPException 
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, List<String> dependencyTypeFilter, 
			WordDecompositionType wordDecompositionType) throws FragmentAnnotatorException, LAPException
	{
		super(lap); 
		this.restrictDependencyType = true;
		this.dependencyTypeFilter = dependencyTypeFilter;
		this.governorPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.dependentPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.restrictWordList = false; this.governorWordFilter = null; this.dependentWordFilter = null;
		this.wordDecompositionType = wordDecompositionType;
		this.setDecompounderDE(wordDecompositionType);
		if(splitter != null){
			tmpCAS = CASUtils.createNewInputCas();
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @param governorPOSFilter    - types of part of speech (POS), which are allowed to be a POS of governor token
	 * @param dependentPOSFilter  - types of part of speech (POS), which are allowed to be a POS of dependent token
	 * @param wordDecompositionType - WordDecompositionType.NONE, WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @throws FragmentAnnotatorException
	 * @throws LAPException 
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, List<POSTag_DE> governorPOSFilter, 
			List<POSTag_DE> dependentPOSFilter, WordDecompositionType wordDecompositionType) throws FragmentAnnotatorException, LAPException
	{
		super(lap); 
		this.restrictDependencyType = false; this.dependencyTypeFilter = null;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
		this.restrictWordList = true; 
		this.governorWordFilter = new ArrayList<String>(); 
		this.dependentWordFilter = new ArrayList<String>();
		this.wordDecompositionType = wordDecompositionType;
		this.setDecompounderDE(wordDecompositionType);
		if(splitter != null){
			tmpCAS = CASUtils.createNewInputCas();
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param governorPOSFilter    - types of part of speech (POS), which are allowed to be a POS of governor token
	 * @param governorWordFilter   - words or lemmas, that are allowed to be a governor token
	 * @param dependentPOSFilter  - types of part of speech (POS), which are allowed to be a POS of dependent token
	 * @param dependentWordFilter  - words or lemmas, that are allowed to be a dependent token
	 * @param wordDecompositionType - WordDecompositionType.NONE, WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @throws FragmentAnnotatorException
	 * @throws LAPException 
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, 
			List<POSTag_DE> governorPOSFilter, List<String> governorWordFilter,
			List<POSTag_DE> dependentPOSFilter, List<String> dependentWordFilter, 
			WordDecompositionType wordDecompositionType) throws FragmentAnnotatorException, LAPException
	{
		super(lap); 
		this.restrictDependencyType = false; this.dependencyTypeFilter = null;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
		this.restrictWordList = true; 
		this.governorWordFilter = governorWordFilter; 
		this.dependentWordFilter = dependentWordFilter;
		this.wordDecompositionType = wordDecompositionType;
		this.setDecompounderDE(wordDecompositionType);
		if(splitter != null){
			tmpCAS = CASUtils.createNewInputCas();
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @param governorPOSFilter    - types of part of speech (POS), which are allowed to be a POS of governor token
	 * @param dependentPOSFilter  - types of part of speech (POS), which are allowed to be a POS of dependent token
	 * @param wordDecompositionType - WordDecompositionType.NONE, WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @throws FragmentAnnotatorException
	 * @throws LAPException 
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, List<String> dependencyTypeFilter, List<POSTag_DE> governorPOSFilter, 
			List<POSTag_DE> dependentPOSFilter, WordDecompositionType wordDecompositionType) throws FragmentAnnotatorException, LAPException
	{
		super(lap); 
		this.restrictDependencyType = true;
		this.dependencyTypeFilter = dependencyTypeFilter;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
		this.restrictWordList = true; 
		this.governorWordFilter = new ArrayList<String>(); 
		this.dependentWordFilter = new ArrayList<String>();
		this.wordDecompositionType = wordDecompositionType;
		this.setDecompounderDE(wordDecompositionType);
		if(splitter != null){
			tmpCAS = CASUtils.createNewInputCas();
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @param governorPOSFilter    - types of part of speech (POS), which are allowed to be a POS of governor token
	 * @param governorWordFilter   - words or lemmas, that are allowed to be a governor token
	 * @param dependentPOSFilter  - types of part of speech (POS), which are allowed to be a POS of dependent token
	 * @param dependentWordFilter  - words or lemmas, that are allowed to be a dependent token
	 * @param wordDecompositionType - WordDecompositionType.NONE, WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @throws FragmentAnnotatorException
	 * @throws LAPException 
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, List<String> dependencyTypeFilter, 
			List<POSTag_DE> governorPOSFilter, List<String> governorWordFilter,
			List<POSTag_DE> dependentPOSFilter, List<String> dependentWordFilter,
			WordDecompositionType wordDecompositionType) throws FragmentAnnotatorException, LAPException
	{
		super(lap); 
		this.restrictDependencyType = true;
		this.dependencyTypeFilter = dependencyTypeFilter;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
		this.restrictWordList = true; 
		this.governorWordFilter = governorWordFilter; 
		this.dependentWordFilter = dependentWordFilter;
		this.wordDecompositionType = wordDecompositionType;
		this.setDecompounderDE(wordDecompositionType);
		if(splitter != null){
			tmpCAS = CASUtils.createNewInputCas();
		}
	}
	
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {

		Logger fragLogger = Logger.getLogger(this.getClass().getName()); 
		
		// first of all, check if determined fragmentation is there or not. 
		// If it is there, we don't run and pass 
		AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
		if (frgIndex.size() > 0)
		{
			fragLogger.info("The CAS already has " + frgIndex.size() + " determined fragment annotation. Won't process this CAS."); 
			return; 
		}
		
		//ok, there are no determined fragments, so annotate some fragments
		int num_dependency_frag = 0;
		
		//add dependency annotation
		addLAPAnnotation(aJCas);
		
		//annotate each dependency as a fragment if it matches the filters
		AnnotationIndex<Annotation> dependencyIndex = aJCas.getAnnotationIndex(Dependency.type);
		if (dependencyIndex.size() > 0) {
			Collection<Dependency> deps = JCasUtil.select(aJCas, Dependency.class);
			Set<String> addedLemmasIndexSet = new HashSet<String>();
			for(Dependency d : deps){
				if(isAllowed(d, restrictDependencyType, governorPOSFilter, dependentPOSFilter)){
					Token governor = d.getGovernor();
					Token dependent = d.getDependent();			
					num_dependency_frag += annotateDependency(aJCas, governor, dependent, wordDecompositionType, addedLemmasIndexSet);
				}
			}
			fragLogger.info("Annotated " + num_dependency_frag + " dependency fragments");
		}
	}
	
	/**
	 * Add dependency annotation on JCas
	 * 
	 * @param aJCas
	 * @throws FragmentAnnotatorException
	 */
	private void addLAPAnnotation(JCas aJCas) throws FragmentAnnotatorException{
		AnnotationIndex<Annotation> dependencyIndex = aJCas.getAnnotationIndex(Dependency.type);
			
		// if there are no dependency annotations, call the LAP to make some
		if (dependencyIndex.size() == 0){
			try {
				this.getLap().addAnnotationOn(aJCas);
			} 
			catch (LAPException e) {
				throw new FragmentAnnotatorException("CASUtils reported exception while trying to add annotations on CAS " + aJCas.getDocumentText(), e );														
			}
			
			// check dependencies again and throw exception, if still no dependencies
			dependencyIndex = aJCas.getAnnotationIndex(Dependency.type);
			if (dependencyIndex.size() == 0){
				throw new FragmentAnnotatorException("Calling on LAPAccess " + this.getLap().getClass().getName() + " didn't added dependency annotation. Cannot proceed."); 
			}
		}
	}

	/**
	 * Check if dependency is allowed to be annotated
	 * 
	 * @param dependency
	 * @return
	 */
	private boolean isAllowed(Dependency dependency, boolean restrictDependencyType, List<POSTag_DE> governorPosFilter, List<POSTag_DE> dependentFilter){
		if(isAllowed(dependency.getDependencyType())) {
			Token governor = dependency.getGovernor();
			Token dependent = dependency.getDependent();
			if (!governor.getCoveredText().equalsIgnoreCase(dependent.getCoveredText()) 
				&& isAllowed(governor, governorPosFilter, governorWordFilter)
				&& isAllowed(dependent, dependentFilter, dependentWordFilter)) {
				return true;
			}
				
		}
		return false;
	}
	
	/**
	 * Check if dependency type is allowed
	 * 
	 * @param dependencyType
	 * @return
	 */
	private boolean isAllowed(String dependencyType){
		if(!restrictDependencyType || dependencyTypeFilter.contains(dependencyType)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if the token type is allowed
	 * 
	 * @param token -- Token
	 * @param posFilter - List <POSTag_DE>
	 * @return
	 */
	private boolean isAllowed(Token token, List<POSTag_DE> posFilter, List<String> wordFilter){
		POSTag_DE posTagDE = POSTag_DE.mapToPOStag_DE(token.getPos().getPosValue());
			if(posTagDE != POSTag_DE.COMMA 
				&& posTagDE != POSTag_DE.SENTENCE_ENDING_PUNCTUATION 
				&& posTagDE!= POSTag_DE.OTHERS
				&& (posFilter.contains(posTagDE) 
						|| (restrictWordList && wordFilter!= null && wordFilter.contains(token.getCoveredText())) 
						|| (restrictWordList && wordFilter!= null && containsLemma(token.getLemma().getValue(), wordFilter)))){
				return true;
			}
		return false;
	}
	
	/**
	 * Annotates dependency with given input parameters
	 * 
	 * @param aJCas
	 * @param governor
	 * @param dependent
	 * @param wordDecompositionType - WordDecompositionType.NONE, WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
	 * @return
	 */
	private int annotateDependency(JCas aJCas, Token governor, Token dependent,  
			WordDecompositionType wordDecompositionType, Set<String> addedLemmasIndexSet){
		int frag_num = 0;
		
		String governorText = governor.getCoveredText();
		String dependentText = dependent.getCoveredText();
		if(!governorText.equalsIgnoreCase(dependentText)) {
			if(wordDecompositionType == WordDecompositionType.NONE){
				annotateDependency(aJCas, governorText, governor.getBegin(), governor.getEnd(), 
						dependentText, dependent.getBegin(), dependent.getEnd());
				frag_num++;
			}
			else {
				if(splitter != null){
					//annotate governor and dependent
					annotateDependency(aJCas, governorText, governor.getBegin(), governor.getEnd(), 
							dependentText, dependent.getBegin(), dependent.getEnd());
					
					Set<Lemma> lemmasToAdd = new HashSet<Lemma>();
					
					//annotate governor + splits of dependent
					Collection<String> compoundParts = decompoundWord(dependentText, wordDecompositionType);
					for(String compoundPart : compoundParts){
						if(!compoundPart.equalsIgnoreCase(dependentText) && !compoundPart.equalsIgnoreCase(governorText)) {
							if(compoundPart.length() == 1 && !isDigit(compoundPart)){
									continue;
							}
							int index = dependentText.indexOf(compoundPart);
							int compoundPartBegin = dependent.getBegin() + index;
							int compoundPartEnd = compoundPartBegin + compoundPart.length();
							index = compoundPartEnd + 1;
							annotateDependency(aJCas, governorText, governor.getBegin(), governor.getEnd(), 
									compoundPart, compoundPartBegin, compoundPartEnd);
							frag_num++;
							if(!addedLemmasIndexSet.contains(compoundPartBegin + " # " + compoundPartEnd)){
								addedLemmasIndexSet.add(compoundPartBegin + " # " + compoundPartEnd);
								Lemma compoundPartLemma = createLemma(aJCas, compoundPart, compoundPartBegin, compoundPartEnd);
								lemmasToAdd.add(compoundPartLemma);
							}
						}
					}
					
					//annotate dependent + splits of governor
					compoundParts = decompoundWord(governorText, wordDecompositionType);
					for(String compoundPart : compoundParts){
						if(!compoundPart.equalsIgnoreCase(governorText) && !compoundPart.equalsIgnoreCase(dependentText)) {
							if(compoundPart.length() == 1 && !isDigit(compoundPart)){
									continue;
							}
							int index = governorText.indexOf(compoundPart);
							int compoundPartBegin = governor.getBegin() + index;
							int compoundPartEnd = compoundPartBegin + compoundPart.length();
							index = compoundPartEnd + 1;
							annotateDependency(aJCas, compoundPart, compoundPartBegin, compoundPartEnd, 
									dependentText, dependent.getBegin(), dependent.getEnd());
							frag_num++;
							if(!addedLemmasIndexSet.contains(compoundPartBegin + " # " + compoundPartEnd)){
								addedLemmasIndexSet.add(compoundPartBegin + " # " + compoundPartEnd);
								Lemma compoundPartLemma = createLemma(aJCas, compoundPart, compoundPartBegin, compoundPartEnd);
								lemmasToAdd.add(compoundPartLemma);
							}
						}
					}
					
					//add lemma of compound parts to the indexes
					for(Lemma lemma : lemmasToAdd){
						aJCas.addFsToIndexes(lemma);
					}
					
				}//if splitter != null
				
			}//wordDecompositionType != WordDecompositionType.NONE
			
		}
		
		return frag_num;
	}
	

	/**
	 * Annotates dependency with given input parameters
	 * 
	 * @param aJCas
	 * @param governorText
	 * @param governorBegin
	 * @param governorEnd
	 * @param dependentText
	 * @param dependentBegin
	 * @param dependentEnd
	 * @return
	 */
	private boolean annotateDependency(JCas aJCas, String governorText, int governorBegin, int governorEnd, String dependentText, int dependentBegin, int dependentEnd){
		Logger fragLogger = Logger.getLogger(this.getClass().getName()); 
		boolean isAnnotated = false;
		CASUtils.Region[] r = new CASUtils.Region[2];
		if(governorBegin < dependentBegin){
			r[0] = new CASUtils.Region(governorBegin,  governorEnd); 
			r[1] = new CASUtils.Region(dependentBegin, dependentEnd);
			fragLogger.info("Annotating the following text as fragment: " +   governorText +  " " + dependentText);
		}
		else {
			r[0] = new CASUtils.Region(dependentBegin, dependentEnd);
			r[1] = new CASUtils.Region(governorBegin,  governorEnd); 
			fragLogger.info("Annotating the following text as fragment: " +   governorText +  " " + dependentText);
		}
			
		try {
			CASUtils.annotateOneDeterminedFragment(aJCas, r);
			isAnnotated = true;
		} catch (LAPException e) {
			e.printStackTrace();
		}
		return isAnnotated;
	}
	
	
	/**
	 * Check if the input word filter contain the input lemma. 
	 * To deal with lemma type: lemma1|lemma2, which is sometimes returned by the TreeTagger, 
	 * the lemma is splitted around matches of the regular expression \\|
	 * Return true if word filter contain at least one of the splits.
	 * 
	 * @param wordFilter
	 * @param lemma
	 * @return
	 */
	private boolean containsLemma(String lemma, List<String> wordFilter) {
		for(String tempLemma : lemma.split("\\|")){
			if(wordFilter.contains(tempLemma)){
				return true;
			}
		}
		return false;
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
	 * @param decompositionType - WordDecompositionType.NONE, WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
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
	 * Check if a String is a digit
	 * 
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
	 * @param wordDecompositionType - WordDecompositionType.NONE, WordDecompositionType.NO_RESTRICTION, WordDecompositionType.ONLY_HYPHEN
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
package eu.excitementproject.tl.decomposition.fragmentannotator;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.POSTag_DE;

/**
 * This class implements the dependency-based fragment annotator. 
 * Each dependency (except when dependent is punctuation token) is considered as a possible (continuous) fragment.
 * If filters for a type of dependencies, part of speech of governor and part of speech of dependent are passed to the constructor 
 * then only dependencies are annotated, which match the filter.
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
	
	/**
	 * @param lap
	 * @throws FragmentAnnotatorException
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap) throws FragmentAnnotatorException
	{
		super(lap);
		this.restrictDependencyType = false; this.dependencyTypeFilter = null;
		this.governorPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.dependentPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.restrictWordList = false; this.governorWordFilter = null; this.dependentWordFilter = null;
	}
	
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @throws FragmentAnnotatorException
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, List<String> dependencyTypeFilter) throws FragmentAnnotatorException
	{
		super(lap); 
		this.restrictDependencyType = true;
		this.dependencyTypeFilter = dependencyTypeFilter;
		this.governorPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.dependentPOSFilter = Arrays.asList(POSTag_DE.class.getEnumConstants());
		this.restrictWordList = false; this.governorWordFilter = null; this.dependentWordFilter = null;
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param governorPOSFilter -  types of part of speech of governor tokens, which should be annotated
	 * @param dependentPOSFilter - types of part of speech of dependent tokens, which should be annotated
	 * @throws FragmentAnnotatorException
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, List<POSTag_DE> governorPOSFilter, 
			List<POSTag_DE> dependentPOSFilter) throws FragmentAnnotatorException
	{
		super(lap); 
		this.restrictDependencyType = false; this.dependencyTypeFilter = null;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
		this.restrictWordList = true; 
		this.governorWordFilter = new ArrayList<String>(); 
		this.dependentWordFilter = new ArrayList<String>();
	}
	
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, 
			List<POSTag_DE> governorPOSFilter, List<String> governorWordFilter,
			List<POSTag_DE> dependentPOSFilter, List<String> dependentWordFilter) throws FragmentAnnotatorException
	{
		super(lap); 
		this.restrictDependencyType = false; this.dependencyTypeFilter = null;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
		this.restrictWordList = true; 
		this.governorWordFilter = governorWordFilter; 
		this.dependentWordFilter = dependentWordFilter;
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @param governorPOSFilter -  types of part of speech of governor tokens, which should be annotated
	 * @param dependentPOSFilter - types of part of speech of dependent tokens, which should be annotated
	 * @throws FragmentAnnotatorException
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, List<String> dependencyTypeFilter, List<POSTag_DE> governorPOSFilter, 
			List<POSTag_DE> dependentPOSFilter) throws FragmentAnnotatorException
	{
		super(lap); 
		this.restrictDependencyType = true;
		this.dependencyTypeFilter = dependencyTypeFilter;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
		this.restrictWordList = true; 
		this.governorWordFilter = new ArrayList<String>(); 
		this.dependentWordFilter = new ArrayList<String>();
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @param governorPOSFilter -  types of part of speech of governor tokens, which should be annotated
	 * @param dependentPOSFilter - types of part of speech of dependent tokens, which should be annotated
	 * @throws FragmentAnnotatorException
	 */
	public DependencyAsFragmentAnnotatorForGerman(LAPAccess lap, List<String> dependencyTypeFilter, 
			List<POSTag_DE> governorPOSFilter, List<String> governorWordFilter,
			List<POSTag_DE> dependentPOSFilter, List<String> dependentWordFilter) throws FragmentAnnotatorException
	{
		super(lap); 
		this.restrictDependencyType = true;
		this.dependencyTypeFilter = dependencyTypeFilter;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
		this.restrictWordList = true; 
		this.governorWordFilter = governorWordFilter; 
		this.dependentWordFilter = dependentWordFilter;
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
			for(Dependency d : deps){
				if(isAllowed(d, restrictDependencyType, governorPOSFilter, dependentPOSFilter)){
					Token governor = d.getGovernor();
					Token dependent = d.getDependent();
					CASUtils.Region[] r = new CASUtils.Region[2];
					if(governor.getBegin() < dependent.getBegin()){
						r[0] = new CASUtils.Region(governor.getBegin(),  governor.getEnd()); 
						r[1] = new CASUtils.Region(dependent.getBegin(),  dependent.getEnd());
						fragLogger.info("Annotating the following text as fragment: " +  governor.getCoveredText() + " " + dependent.getCoveredText());
					}
					else {
						r[0] = new CASUtils.Region(dependent.getBegin(),  dependent.getEnd());
						r[1] = new CASUtils.Region(governor.getBegin(),  governor.getEnd()); 
						fragLogger.info("Annotating the following text as fragment: " +  dependent.getCoveredText() + " " + governor.getCoveredText());
					}
						
					try {
						CASUtils.annotateOneDeterminedFragment(aJCas, r);
						num_dependency_frag++;
					} catch (LAPException e) {
						e.printStackTrace();
					}
				}
			}
			fragLogger.info("Annotated " + num_dependency_frag + " dependency fragments");
		}
	}
	
	/**
	 * add dependency annotation on JCas
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
	 * check if dependency is allowed to be annotated
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
	 * check if dependency type is allowed
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
	 * check if the token type is allowed
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
}
package eu.excitementproject.tl.decomposition.fragmentannotator;


import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * This class implements the dependency-based fragment annotator. 
 * Each dependency (except when dependent is punctuation token or dependent and governor are identical words) 
 * is considered as a possible fragment.
 * 
 * @author Aleksandra
 *
 */

public class DependencyAsFragmentAnnotator extends AbstractFragmentAnnotator {
	
	private final List<String> dependencyTypeFilter;
	private final boolean restrictDependencyType;

	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @throws FragmentAnnotatorException
	 */
	public DependencyAsFragmentAnnotator(LAPAccess lap) throws FragmentAnnotatorException
	{
		super(lap);
		this.restrictDependencyType = false; this.dependencyTypeFilter = null;
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @throws FragmentAnnotatorException
	 */
	public DependencyAsFragmentAnnotator(LAPAccess lap, List<String> dependencyTypeFilter) throws FragmentAnnotatorException
	{
		super(lap); 
		this.restrictDependencyType = true;
		this.dependencyTypeFilter = dependencyTypeFilter;
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
				if(isAllowed(d)){
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
	 * @param dependency
	 * @return
	 */
	private boolean isAllowed(Dependency dependency){
		if(isAllowed(dependency.getDependencyType())){
			Token governor = dependency.getGovernor();
			Token dependent = dependency.getDependent();
			if (!governor.getCoveredText().equalsIgnoreCase(dependent.getCoveredText()) 
				&& isALlowed(dependency.getDependent())
				&& isALlowed(dependency.getDependent())){
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
	 * check if the token type is allowed to be annotated as fragment
	 * return true only if token is no punctuation and no other symbol like ()[]-|>< etc;
	 * @param token -- Token
	 * @param posFilter - List <POSTag_DE>
	 * @return
	 */
	private boolean isALlowed(Token token) {
		if(!(token.getPos() instanceof PUNC)){
			return true;
		}
		return false;
	}
}

package eu.excitementproject.tl.decomposition.fragmentannotator;


import java.util.Collection;
import java.util.Iterator;
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

/**
 * This class implements the dependency-based fragment annotator. 
 * Each dependency (except when dependent is punctuation token) is considered as a possible (continuous) fragment.
 * If filters for a type of dependencies, part of speech of governor and part of speech of dependent are passed to the constructor 
 * then only dependencies are annotated, which match the filter.
 * 
 * @author Aleksandra
 *
 */

public class DependencyAsFragmentAnnotator extends AbstractFragmentAnnotator {
	
	private final List<String> dependencyTypeFilter;
	private final List<String> governorPOSFilter;
	private final List<String> dependentPOSFilter;

	public DependencyAsFragmentAnnotator(LAPAccess lap) throws FragmentAnnotatorException
	{
		super(lap);
		dependencyTypeFilter = null;
		governorPOSFilter = null;
		dependentPOSFilter = null;
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @param governorPOSFilter -  types of part of speech of governor tokens, which should be annotated
	 * @param dependentPOSFilter - types of part of speech of dependent tokens, which should be annotated
	 * @throws FragmentAnnotatorException
	 */
	public DependencyAsFragmentAnnotator(LAPAccess lap, List<String> dependencyTypeFilter, List<String> governorPOSFilter, 
			List<String> dependentPOSFilter) throws FragmentAnnotatorException
	{
		super(lap); 
		this.dependencyTypeFilter = dependencyTypeFilter;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
	}
	
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {

		Logger fragLogger = Logger.getLogger(this.getClass().getName()); 
		
		// first of all, check if determined fragmentation is there or not. 
		// If it is there, we don't run and pass 
		// check the annotated data
		AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
		if (frgIndex.size() > 0)
		{
			fragLogger.info("The CAS already has " + frgIndex.size() + " determined fragment annotation. Won't process this CAS."); 
			return; 
		}
		
		int num_dependency_frag = 0;
		
		// check if dependency is there or not 
		AnnotationIndex<Annotation> dependencyIndex = aJCas.getAnnotationIndex(Dependency.type);
		Iterator<Annotation> dependencyItr = dependencyIndex.iterator(); 		
		
		// if there are no dependency annotations, call the LAP to make some
		if (!dependencyItr.hasNext()) {
			try 
			{
				this.getLap().addAnnotationOn(aJCas);
			} 
			catch (LAPException e) 
			{
				throw new FragmentAnnotatorException("CASUtils reported exception while trying to add annotations on CAS " + aJCas.getDocumentText(), e );														
			}
			
			// all right. LAP annotated. Try once again 
			dependencyIndex = aJCas.getAnnotationIndex(Dependency.type);
			
			// throw exception, if still no dependencies
			if (dependencyIndex.size() == 0)
			{
				throw new FragmentAnnotatorException("Calling on LAPAccess " + this.getLap().getClass().getName() + " didn't added dependency annotation. Cannot proceed."); 
			}
		}
		
		//annotate each dependency as a fragment if it matches the filters
		if (dependencyIndex.size() > 0) {
			Collection<Dependency> deps = JCasUtil.select(aJCas, Dependency.class);
			for(Dependency d : deps){
				if(!d.getDependent().getPos().getType().getShortName().equals("PUNC")){
					if(dependencyTypeFilter == null || dependencyTypeFilter.isEmpty() || dependencyTypeFilter.contains(d.getDependencyType())){
						Token governor = d.getGovernor();
						Token dependent = d.getDependent();
						int governorBegin = 0;
						int governorEnd = 0;
						int dependentBegin = 0;
						int dependentEnd = 0;
						
						if(!governor.getCoveredText().equalsIgnoreCase(dependent.getCoveredText())){ //if governor and dependent has the same text, dont't annotate because is is probably a parser error
							if((governorPOSFilter == null || governorPOSFilter.isEmpty() || governorPOSFilter.contains(governor.getPos().getPosValue()))
								&& (dependentPOSFilter == null || dependentPOSFilter.isEmpty() || dependentPOSFilter.contains(dependent.getPos().getPosValue()))){
								governorBegin = governor.getBegin();
								governorEnd = governor.getEnd();
								dependentBegin = dependent.getBegin();
								dependentEnd = dependent.getEnd();
								CASUtils.Region[] r = new CASUtils.Region[2];
								if(governorBegin < dependentBegin){
									r[0] = new CASUtils.Region(governorBegin,  governorEnd); 
									r[1] = new CASUtils.Region(dependentBegin,  dependentEnd);
								}
								else{
									r[0] = new CASUtils.Region(dependentBegin,  dependentEnd);
									r[1] = new CASUtils.Region(governorBegin,  governorEnd); 
								}
									
								try {
									if(governorBegin < dependentBegin)
										fragLogger.info("Annotating the following text as fragment: " +  governor.getCoveredText() + " " + dependent.getCoveredText());
									else
										fragLogger.info("Annotating the following text as fragment: " +  dependent.getCoveredText() + " " + governor.getCoveredText());
									CASUtils.annotateOneDeterminedFragment(aJCas, r);
									num_dependency_frag++;
								} catch (LAPException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
			fragLogger.info("Annotated " + num_dependency_frag + " dependency fragments");
		}
	}
}
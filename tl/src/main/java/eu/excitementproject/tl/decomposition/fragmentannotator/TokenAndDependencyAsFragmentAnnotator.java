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
 * This class combines TokenAsFragmentAnnotator and DependencyAsFragmentAnnotator.
 * Each token and dependency (except when POS of a token is punctuation) are considered as possible (continuous) fragments.
 * If filters for a part of speech of tokens, for type of dependencies, for part of speech of governor and part of speech of dependent
 * are passed to the constructor, then only tokens and dependencies are annotated, which match the filter.
 * 
 * @author Aleksandra
 *
 */

public class TokenAndDependencyAsFragmentAnnotator extends AbstractFragmentAnnotator {

	private List<String> tokenPOSFilter;
	private List<String> dependencyTypeFilter;
	private List<String> governorPOSFilter;
	private List<String> dependentPOSFilter;
	
	public TokenAndDependencyAsFragmentAnnotator(LAPAccess lap)
			throws FragmentAnnotatorException {
		super(lap);
	}
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param tokenPOSFilter - types of parts of speech of tokens, which which should be annotated 
	 * @param dependencyTypeFilter - types of dependencies, which should be annotated
	 * @param governorPOSFilter -  types of parts of speech of governor tokens, which should be annotated
	 * @param dependentPOSFilter - types of parts of speech of dependent tokens, which should be annotated
	 * @throws FragmentAnnotatorException
	 */
	public TokenAndDependencyAsFragmentAnnotator(LAPAccess lap, List<String> tokenPOSFilter, List<String> dependencyTypeFilter, List<String> governorPOSFilter, 
			List<String> dependentPOSFilter) throws FragmentAnnotatorException
	{
		super(lap);
		this.tokenPOSFilter = tokenPOSFilter;
		this.dependencyTypeFilter = dependencyTypeFilter;
		this.governorPOSFilter = governorPOSFilter;
		this.dependentPOSFilter = dependentPOSFilter;
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
		
		int num_frag = 0; 
		int num_token_frag = 0;
		int num_dependency_frag = 0;
		
		/** check if token and dependency annotation is there or not **/ 
		AnnotationIndex<Annotation> tokenIndex = aJCas.getAnnotationIndex(Token.type);
		Iterator<Annotation> tokenItr = tokenIndex.iterator(); 		
		
		AnnotationIndex<Annotation> dependencyIndex = aJCas.getAnnotationIndex(Dependency.type);
		Iterator<Annotation> dependencyItr = dependencyIndex.iterator(); 		
		
		if (!tokenItr.hasNext() || !dependencyItr.hasNext())
		{
			// It seems that there are no tokens or no dependencies in the CAS. 
			// Run LAP on it. 
			fragLogger.info("No token or no dependency annotation found: calling the given LAP"); 
			try 
			{
				this.getLap().addAnnotationOn(aJCas); 
			}
			catch (LAPException e)
			{
				throw new FragmentAnnotatorException("Unable to run LAP on the inputCAS: LAP raised an exception",e); 
			}
			
			// all right. LAP annotated. Try once again 
			tokenIndex = aJCas.getAnnotationIndex(Token.type);
			tokenItr = tokenIndex.iterator(); 				
			dependencyIndex = aJCas.getAnnotationIndex(Dependency.type);
			dependencyItr = dependencyIndex.iterator(); 	
			
			// throw exception, if still no token 
			if (!tokenItr.hasNext() || !dependencyItr.hasNext())
			{
				throw new FragmentAnnotatorException("Calling on LAPAccess " + this.getLap().getClass().getName() + " didn't add Token or/and Dependency annotation. Cannot proceed."); 
			}

		}

		fragLogger.info("Annotating determined fragments on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 
		
		while(tokenItr.hasNext())
		{
			//annotate each token except punctuation as one fragment, if it matches the filter 
			Token tk = (Token) tokenItr.next(); 
			int begin = tk.getBegin(); 
			int	end = tk.getEnd(); 
			CASUtils.Region[] r = new CASUtils.Region[1];
			r[0] = new CASUtils.Region(begin,  end); 
			
			try {
				if(tk.getPos()!= null){
					if(!tk.getPos().getType().getShortName().equals("PUNC")){
						if (tokenPOSFilter == null || tokenPOSFilter.isEmpty() || tokenPOSFilter.contains(tk.getPos().getPosValue())){
							fragLogger.info("Annotating the following as a fragment: " + tk.getCoveredText());
							CASUtils.annotateOneDeterminedFragment(aJCas, r);
							num_token_frag++;
						}
					}
				}
			}
			
			catch (LAPException e)
			{
				throw new FragmentAnnotatorException("CASUtils reported exception while annotating Fragment, on token (" + begin + ","+ end, e );
			}
			 
		}
		fragLogger.info("Annotated " + num_token_frag + " token fragments"); 
		
		
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
			num_frag = num_token_frag + num_dependency_frag;
			fragLogger.info("Annotated " + num_frag + " determined fragments");
		}
	}	
}

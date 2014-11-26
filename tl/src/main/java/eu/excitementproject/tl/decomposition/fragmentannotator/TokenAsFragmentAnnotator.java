package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.Collection;
import java.util.Iterator;
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
 * This class implements the simplest baseline "fragment annotator". 
 * Namely, each token except the punctuation is considered as a possible (continuous) fragment.
 * If a filter for parts of speech is passed to the constructor, then only tokens are annotated, which match the filter.
 * 
 * @author Aleksandra
 *
 */
public class TokenAsFragmentAnnotator extends AbstractFragmentAnnotator {

	private final List<String> tokenPOSFilter;
	
	public TokenAsFragmentAnnotator(LAPAccess l) throws FragmentAnnotatorException
	{
		super(l); 
		tokenPOSFilter = null;
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param tokenPOSFilter - types of parts of speeches of tokens, which which should be annotated 
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotator(LAPAccess l, List<String> tokenPOSFilter) throws FragmentAnnotatorException
	{
		super(l); 
		this.tokenPOSFilter = tokenPOSFilter;
	}
	
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {
		
		Logger fragLogger = Logger.getLogger(this.getClass().getName()); 

		// first of all, check determined fragmentation is there or not. 
		// If it is there, we don't run and pass 
		AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
		
		if (frgIndex.size() > 0) {
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
					CASUtils.Region[] r = new CASUtils.Region[1];
					r[0] = new CASUtils.Region(tk.getBegin(),  tk.getEnd()); 
					fragLogger.info("Annotating the following as a fragment: " + tk.getCoveredText());
					CASUtils.annotateOneDeterminedFragment(aJCas, r);
					num_frag++;
				}
			} 
			
			catch (LAPException e) {
				throw new FragmentAnnotatorException("CASUtils reported exception while annotating Fragment, on token (" + tk.getBegin() + ","+ tk.getEnd(), e );
			}
		}
		fragLogger.info("Annotated " + num_frag + " determined fragments"); 
	}
	
	/**
	 * 
	 * @param aJCas
	 * @throws FragmentAnnotatorException
	 */
	private void addLAPTokenAnnotation(JCas aJCas) throws FragmentAnnotatorException{
		AnnotationIndex<Annotation> tokenIndex = aJCas.getAnnotationIndex(Token.type);
		if (tokenIndex.size() == 0)
		{
			// It seems that there are no tokens in the CAS. Run LAP on it. 
			try {
				this.getLap().addAnnotationOn(aJCas); 
			}
			catch (LAPException e)
			{
				throw new FragmentAnnotatorException("Unable to run LAP on the inputCAS: LAP raised an exception", e); 
			}

			// check token annotation and throw exception, if still no tokens found
			tokenIndex = aJCas.getAnnotationIndex(Token.type);		
			if (tokenIndex.size() == 0){
				throw new FragmentAnnotatorException("Calling on LAPAccess " + this.getLap().getClass().getName() + 
						" didn't added token annotation. Cannot proceed."); 
			}
		}
	}
	
	/**
	 * check if the token type is allowed
	 * @param token
	 * @param posFilter
	 * @return
	 */
	private boolean isAllowed(Token token, List<String> posFilter){
		if(!(token.getPos() instanceof PUNC)) {
			if(posFilter == null 
					|| posFilter.isEmpty()
					|| posFilter.contains(token.getPos().getPosValue())) {
				return true;
			}
		}
		return false;
	}
}

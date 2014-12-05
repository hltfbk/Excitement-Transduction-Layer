package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PUNC;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * This class implements the a simple token-based {@link FragmentAnnotator}. 
 * 
 * Namely, each token except the punctuation is considered as a possible (continuous) fragment.
 * 
 * @author Aleksandra
 *
 */
public class TokenAsFragmentAnnotator extends AbstractFragmentAnnotator {
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotator(LAPAccess l) throws FragmentAnnotatorException
	{
		super(l); 
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
			if(isALlowed(tk)) {
				try {
					CASUtils.Region[] r = new CASUtils.Region[1];
					r[0] = new CASUtils.Region(tk.getBegin(),  tk.getEnd()); 
					fragLogger.info("Annotating the following as a fragment: " + tk.getCoveredText());
					CASUtils.annotateOneDeterminedFragment(aJCas, r);
					num_frag++;
				} 
				
				catch (LAPException e) {
					throw new FragmentAnnotatorException("CASUtils reported exception while annotating Fragment, on token (" + tk.getBegin() + ","+ tk.getEnd(), e );
				}
			}
		}
		fragLogger.info("Annotated " + num_frag + " determined fragments"); 
	}
	

	/**
	 * 
	 * @param aJCas
	 * @throws FragmentAnnotatorException
	 */
	protected void addLAPTokenAnnotation(JCas aJCas) throws FragmentAnnotatorException{
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
	 * check if the token type is allowed to be annotated as fragment
	 * return true only if token is no punctuation and no other symbol like ()[]-|>< etc;
	 * 
	 * @param token -- Token
	 * @return
	 */
	protected boolean isALlowed(Token token) {
		if(!(token.getPos() instanceof PUNC)){
			return true;
		}
		return false;
	}
	
}

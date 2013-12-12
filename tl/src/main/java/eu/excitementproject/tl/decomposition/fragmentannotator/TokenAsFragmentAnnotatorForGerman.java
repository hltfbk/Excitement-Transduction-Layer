package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * This class implements the simplest baseline "fragment annotator". 
 * Namely, each token is considered as a (continuous) fragment. 
 * 
 * @author Aleksandra (based on Gil's class SentenceAsFragmentAnnotator)
 *
 */
public class TokenAsFragmentAnnotatorForGerman extends AbstractFragmentAnnotator {

	public TokenAsFragmentAnnotatorForGerman(LAPAccess l) throws FragmentAnnotatorException
	{
		super(l); 
	}
	
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {
		
		Logger fragLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator"); 

		// first of all, check determined fragmentation is there or not. 
		// If it is there, we don't run and pass 
		// check the annotated data
		AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
		
		if (frgIndex.size() > 0)
		{
			fragLogger.info("The CAS already has " + frgIndex.size() + " determined fragment annotation. Won't process this CAS."); 
			return; 
		}
		
		// check token annotation is there or not 
		AnnotationIndex<Annotation> tokenIndex = aJCas.getAnnotationIndex(Token.type);
		Iterator<Annotation> tokenItr = tokenIndex.iterator(); 		
		
		if (!tokenItr.hasNext())
		{
			// It seems that there are no tokens in the CAS. 
			// Run LAP on it. 
			fragLogger.info("No token annotation found: calling the given LAP"); 
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
			
			// throw exception, if still no token 
			if (!tokenItr.hasNext())
			{
				throw new FragmentAnnotatorException("Calling on LAPAccess " + this.getLap().getClass().getName() + " didn't added Token annotation. Cannot proceed."); 
			}

		}

		fragLogger.info("Annotating determined fragments on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 

		//list of posValues, which the annotated token should have
		//the following list works only for German!!
		List<String> wantedPosValues =  Arrays.asList(
				new String []{"ADJA", "ADJD", "NN", "NE", "VVFIN", "VVIMPF", "VVINF", "VVIZU", "VVPP"});
		
		int num_frag = 0; 
		
		while(tokenItr.hasNext())
		{
			// simply annotate each token as one fragment, 
			Token tk = (Token) tokenItr.next(); 
			int begin = tk.getBegin(); 
			int	end = tk.getEnd(); 
			CASUtils.Region[] r = new CASUtils.Region[1];
			r[0] = new CASUtils.Region(begin,  end); 
			
			try {
				//annotate only nouns, adjectives and main verbs
				//now this works only for German!!
				if (wantedPosValues.contains(tk.getPos().getPosValue())){
					fragLogger.info("Annotating the following as a fragment: " + tk.getCoveredText());
					CASUtils.annotateOneDeterminedFragment(aJCas, r);
					num_frag++;
				}
			}
			
			catch (LAPException e)
			{
				throw new FragmentAnnotatorException("CASUtils reported exception while annotating Fragment, on token (" + begin + ","+ end, e );
			}
			 
		}
		fragLogger.info("Annotated " + num_frag + " determined fragments"); 
	}
}

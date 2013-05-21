package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * This class implements the simplest baseline "fragment annotator". 
 * Namely, each sentence is considered as a (continuous) fragment. 
 * 
 * @author Gil
 *
 */
public class SentenceAsFragmentAnnotator extends AbstractFragmentAnnotator {

	SentenceAsFragmentAnnotator(LAPAccess l) throws FragmentAnnotatorException
	{
		super(l); 
	}
	
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {

		Logger fragLogger = Logger.getLogger("eu.excitementproject.tl.decomposition.fragmentannotator"); 

		// check sentence annotation is there or not 
		AnnotationIndex<Annotation> sentIndex = aJCas.getAnnotationIndex(Sentence.type);
		Iterator<Annotation> sentItr = sentIndex.iterator(); 		
		
		if (!sentItr.hasNext())
		{
			// It seems that there are no sentences in the CAS. 
			// Run LAP on it. 
			fragLogger.info("No sentence annotation found: calling the given LAP"); 
			try 
			{
				this.getLap().addAnnotationOn(aJCas); 
			}
			catch (LAPException e)
			{
				throw new FragmentAnnotatorException("Unable to run LAP on the inputCAS: LAP raised an exception",e); 
			}
			// all right. LAP annotated. Try once again 
			sentIndex = aJCas.getAnnotationIndex(Sentence.type);
			sentItr = sentIndex.iterator(); 		
			
			// throw exception, if still no sentence 
			if (!sentItr.hasNext())
			{
				throw new FragmentAnnotatorException("Calling on LAPAccess " + this.getLap().getClass().getName() + " didn't added Sentence annotation. Cannot proceed."); 
			}

		}

		fragLogger.info("Annotating determined fragments on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 
		int num_frag = 0; 
		while(sentItr.hasNext())
		{
			// simply annotate each sentence as one, continuous fragment. 
			Sentence st = (Sentence) sentItr.next(); 
			int begin = st.getBegin(); 
			int end = st.getEnd(); 
			CASUtils.Region[] r = new CASUtils.Region[1]; 
			r[0] = new CASUtils.Region(begin,  end); 
			
			fragLogger.debug("Annotating the following as a fragment: " + st.getCoveredText()); 
			try {
				CASUtils.annotateOneDeterminedFragment(aJCas, r); 
			}
			catch (LAPException e)
			{
				throw new FragmentAnnotatorException("CASUtils reported exception while annotating Fragment, on sentence (" + begin + ","+ end, e );
			}
			num_frag++; 
		}
		fragLogger.info("Annotated " + num_frag + " determined fragments"); 
	}

}

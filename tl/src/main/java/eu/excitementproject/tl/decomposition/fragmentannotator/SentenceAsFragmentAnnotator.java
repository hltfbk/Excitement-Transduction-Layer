package eu.excitementproject.tl.decomposition.fragmentannotator;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;

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
	public void annotateFragments(JCas text) throws FragmentAnnotatorException {

		// TODO: fill in the code 
		// check sentence annotation is there or not 
		
		// if not, call LAP to annotate it 
		
		// (after LAP call is done check sentence annotation is there, if not raise exception) 
		
		// for each sentence, mark them as a simple fragment. 
		
		// Done. 
	}

}

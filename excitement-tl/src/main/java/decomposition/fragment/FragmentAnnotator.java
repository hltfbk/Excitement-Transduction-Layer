package decomposition.fragments;

import java.util.List;

import org.apache.uima.jcas.JCas;

import decomposition.entities.EntailmentUnit;
import decomposition.entities.TextualInput;

/**
 * This interface needs to be implemented for each scenario to annotate fragments on a textual input CAS. 
 * 
 * @author Kathrin
 *
 */
public interface FragmentAnnotator {
	
	/**
	 * This method adds fragment annotation to the textual input CAS (e.g., based on linguistic annotation) 
	*/
	public JCas addFragmentAnnotation(JCas in);

}

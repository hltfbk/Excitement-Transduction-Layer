package decomposition;

import org.apache.uima.jcas.JCas;

import decomposition.entities.TextualInput;

/**
 * This class implements the scenario-independent methods for processing a textual input. 
 * This includes transforming the input into a CAS representation and adding linguistic 
 * annotation by calling an LAP.
 * 
 * TO BE IMPLEMENTED BY DFKI
 * 
 * @author Kathrin
 *
 */


public class TextualInputProcessor {
				
	//add linguistic annotation --> LAP-specific, but general
	public JCas addLinguisticAnnotation(TextualInput in) {
		//create Textual Input CAS
		//add linguistic annotation
		//OPEN QUESTION: How to specify LAP? config file? input parameter?
		return null;
	}

}

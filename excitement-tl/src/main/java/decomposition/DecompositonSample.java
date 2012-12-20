package decomposition;

import java.util.List;


import org.apache.uima.jcas.JCas;

import decomposition.entities.EntailmentUnit;
import decomposition.entities.TextualInput;

/**
 * This class shows the usage of the decomposition code. It provides a simple example showing 
 * how to use the decomposition code to create entailment units from a textual input.
 * 
 * TO BE IMPLEMENTED BY DFKI
 * 
 * @author Kathrin
 *
 */
public class DecompositonSample {

	public static void main(String[] args) {
		
		//create textual input
		TextualInput in = new TextualInput();
		in.setText("This is a test.");
		in.setLanguageCode("en");
		
		//process textual input: transform it into CAS and annotate it using LAP
		TextualInputProcessor tip = new TextualInputProcessor();
		JCas annotatedCAS = tip.addLinguisticAnnotation(in);
		
		//add fragment annotation
		EntailmentUnitCreator euc = new EntailmentUnitCreatorSample();
		JCas fragmentCAS = euc.addFragmentAnnotation(annotatedCAS);	
		
	}
}

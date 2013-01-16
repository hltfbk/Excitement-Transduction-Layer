package decomposition;

import java.util.List;


import org.apache.uima.jcas.JCas;

import decomposition.entities.EntailmentUnit;
import decomposition.entities.TextualInput;

/**
 * This class shows the usage of the decomposition code. It provides a simple example showing 
 * how to use the decomposition code to create entailment units from a textual input.
 * 
 * TO BE IMPLEMENTED BY ???
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
		JCas cas = tip.createTextualInputCAS(in);
		JCas annotatedCAS = tip.addLinguisticAnnotation(cas);
		
		//add fragment annotation
		FragmentAnnotator fa = new FragmentAnnotatorOMQ();
		JCas fragmentCAS = fa.addFragmentAnnotation(annotatedCAS);	
		
		//create entailment units (subfragments and fragments) and store subfragment-fragment relation information
		EntailmentUnitCreator euc = new EntailmentUnitCreatorGerman();
		List<EntailmentUnit> entailmentUnits = euc.generateEntailmentUnits(fragmentCAS);
		
	}
}

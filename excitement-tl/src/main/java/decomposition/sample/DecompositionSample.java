package decomposition.sample;

import java.util.List;


import org.apache.uima.jcas.JCas;

import decomposition.TextualInputProcessor;
import decomposition.entities.EntailmentUnit;
import decomposition.entities.TextualInput;
import decomposition.fragments.FragmentAnnotator;
import decomposition.fragments.FragmentAnnotatorOMQ;
import decomposition.modifiers.ModifierExtractor;
import decomposition.modifiers.ModifierExtractorGerman;

/**
 * This class shows the usage of the decomposition code. It provides a simple example showing 
 * how to use the decomposition code to create entailment units from a textual input.
 * 
 * TO BE IMPLEMENTED BY ???
 * 
 * @author Kathrin
 *
 */
public class DecompositionSample {

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
		ModifierExtractor euc = new ModifierExtractorGerman();
		euc.createInputToBuildGraph(fragmentCAS);
		
	}
}

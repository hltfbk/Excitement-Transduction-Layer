package decomposition.modifiers;

import java.util.List;

import org.apache.uima.jcas.JCas;

import decomposition.entities.EntailmentUnit;
import decomposition.entities.TextualInput;

/**
 * This interface needs to be implemented for each scenario / language to create entailment units from an annotated CAS. 
 * Entailment units refer to fragments and subfragments. 
 * Creating entailment units involves the following steps: 
 * - reading a CAS (textual input annotated with linguistic analysis and fragments)
 * - using the linguistic annotation to create subfragments from the fragments
 * - creating entailment units (also storing the fragment-subfragment relation)
 * 
 * @author Kathrin
 *
 */

public interface ModifierExtractor {
	
	//read annotated fragments on TextualInputCAS and extract subfragments; store both fragments and subfragments and their relations
	public void createInputToBuildGraph(JCas in);
	
}

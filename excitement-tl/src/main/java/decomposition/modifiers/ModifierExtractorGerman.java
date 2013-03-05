package decomposition.modifiers;

import java.util.List;

import org.apache.uima.jcas.JCas;

import decomposition.entities.EntailmentUnit;
import decomposition.entities.TextualInput;

/**
 * This class implements the entailment unit creation for German. 
 * 
 * TO BE IMPLEMENTED BY DFKI/OMQ
 * 
 * @author Kathrin
 *
 */

public class ModifierExtractorGerman implements ModifierExtractor {
	
	//read annotated fragments on TextualInputCAS and extract subfragments; store both fragments and subfragments and their relations
	public void createInputToBuildGraph(JCas in) {
		//for example: remove all modifiers
		//create a recursive method List<EntailmentUnit> removeModifiers(EntailmentUnit eu) {foreach eu: removeModifiers; if not change: break loop}
	}
	
}

package decomposition.modifiers;

import java.util.List;

import org.apache.uima.jcas.JCas;

import decomposition.entities.EntailmentUnit;
import decomposition.entities.TextualInput;

/**
 * This class implements the entailment unit creation for Italian. 
 * 
 * TO BE IMPLEMENTED BY FBK/ALMA
 * 
 * @author Kathrin
 *
 */

public class ModifierExtractorItalian implements ModifierExtractor {
	
	//read annotated fragments on TextualInputCAS and extract subfragments; store both fragments and subfragments and their relations
	public void createInputToBuildGraph(JCas in) {
	}
	
}

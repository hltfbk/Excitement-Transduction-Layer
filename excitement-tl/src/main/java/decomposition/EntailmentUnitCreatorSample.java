package decomposition;

import java.util.List;
import org.apache.uima.jcas.JCas;

import decomposition.entities.EntailmentUnit;
import decomposition.entities.TextualInput;

/**
 * This class shows a sample of the entailment unit creation.
 * 
 * TO BE IMPLEMENTED BY DFKI
 * 
 * @author Kathrin
 *
 */

public class EntailmentUnitCreatorSample implements EntailmentUnitCreator {
	
	//read annotated fragments on TextualInputCAS and extract subfragments; store both fragments and subfragments and their relations
	public List<EntailmentUnit> generateEntailmentUnits(JCas in) {
		//for example: remove all modifiers
		//create a recursive method List<EntailmentUnit> removeModifiers(EntailmentUnit eu) {foreach eu: removeModifiers; if not change: break loop}
		return null;
	}
	
}

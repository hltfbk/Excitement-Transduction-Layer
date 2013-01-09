package composition;

import java.util.List;

import org.apache.uima.jcas.JCas;

import decomposition.EntailmentUnitCreator;
import decomposition.EntailmentUnitCreatorSample;
import decomposition.entities.EntailmentUnit;


/**
 * This class shows the usage of the composition code. It provides a simple example showing 
 * how to use the composition code to build an entailment graph from a set of entailment units.
 * 
 * TO BE IMPLEMENTED BY DFKI
 * 
 * @author Kathrin
 *
 */
public class CompositionSample {
	
	public static void main(String[] args) {
		
		//create empty graph
		EntailmentGraphRaw egr = new EntailmentGraphRaw();
					
		//read some list of entailment units
		List<EntailmentUnit> eunits = null;	
		
		//generate entailment pairs
		
		//process pairs and extend the graph
	}

}

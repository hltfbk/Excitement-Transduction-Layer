package decomposition;

import java.util.List;

import org.apache.uima.jcas.JCas;

import decomposition.entities.*;

/** 
 * This class is a sample implementation of the EntailmentUnitCreator interface. Using a simple example, 
 * it illustrates how to create entailment units from an input CAS (using linguistic annotation). 
 * This serves as a starting point for implementing the scenario-specific EntailmentUnitCreator
 * implementations.
 * 
 * TO BE IMPLEMENTED BY DFKI
 * 
 * @author Kathrin
 *
 */
public class FragmentAnnotatorSample implements FragmentAnnotator {
	
	public JCas addFragmentAnnotation(JCas in) {
		// TODO Auto-generated method stub
		
		//for example: one fragment per sentence
		return null;
	}

}


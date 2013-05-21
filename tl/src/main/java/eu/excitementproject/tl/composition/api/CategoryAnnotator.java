package eu.excitementproject.tl.composition.api;

import java.util.Set;


import org.apache.uima.jcas.JCas;

import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.structures.search.NodeMatch;

/**
 * ** OMQ usecase composition-flow "CategoryAnnotator" 
This module actually annotates category annotation of usecase-2, on
the input CAS.
- input: one input CAS (=JCas=), one set of Match (=Set<Match>=), 
- output: no new data, but the input CAS is now annotated with
  category annotation.
- failure: any failure will be reported as an exception. We will
  define an exception for this module type.
- interface: We can express the above contract with an interface with
  one method. We are expecting to have one implementation for
  usecase-2 for the prototype, but this can be expanded in the future. 
- dependency: no external dependency expected. 

 * @author Kathrin Eichler
 *
 */

public interface CategoryAnnotator {

	/**
	 * @param cas - input CAS ({@link JCas})
	 * @param matches - set of matching nodes ({@link MatchingNode})
	 * @throws CategoryAnnotatorException if category annotation fails
	 */
	public void addCategoryAnnotation(JCas cas, Set<NodeMatch> matches) 
			throws CategoryAnnotatorException;

}

/**
 * 
 */
package eu.excitementproject.tl.decomposition.api;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;


/**
 
 * ** Fragment Graph Builder Module
- input: =JCas= with text, metadata, fragment annotation, and modifier
  annotation. If any of the needed data is missing, it will raise an
  exception.
- output: a set of Fragment Graphs. (=Set<FragmentGraph>=)
- failues: if the implementation can't generate the graphs for some
  reason, it will raise an exception. (We will define an exception for
  this module type).
- interface:
We can express the above mentioned contract with an interface with
one method. For the prototype, we are expecting to share one
implementation, but this can be expanded in the future.
- dependency:
We do not foresee any external EOP component dependency for this
module.
* @author Lili
 */
public interface FragmentGraphGenerator {
	
	/**
	 * @param text - =JCas= with text, metadata, fragment annotation, and modifier
  annotation
	 * @return a set of Fragment Graphs ({@link FragmentGraph}).
	 * @throws FragmentGraphGeneratorException if any of the needed data is missing in the input CAS or if the implementation can't generate the graphs for some
  reason
	 */
	public Set<FragmentGraph>
		generateFragmentGraphs(JCas text) throws FragmentGraphGeneratorException; 

}

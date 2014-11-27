/**
 * 
 */
package eu.excitementproject.tl.decomposition.api;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;


/**
 
 * ** Fragment Graph Generator Module Interface
 * 
<p> <b>Input:</b> {@link JCas} with text, metadata, fragment annotation, and modifier
  annotation. If any of the needed data is missing, a {@link FragmentGraphGeneratorException} will be raised .
<p><b>Output:</b> A set of {@link FragmentGraph}s.
<p><b>Failures:</b> If the implementation cannot generate the graphs for some
  reason, it will raise a {@link FragmentGraphGeneratorException}.
<p><b>Dependency:</b>We do not foresee any external EOP component dependency for this
module.

* @author Lili Kotlerman
 */
public interface FragmentGraphGenerator {
	
	/**
	 * @param text - {@link JCas} with text, metadata, fragment annotation, and modifier
  annotation
	 * @return a set of Fragment Graphs ({@link FragmentGraph}).
	 * @throws FragmentGraphGeneratorException if any of the needed data is missing in the input CAS or if the implementation can't generate the graphs for some
  reason
	 */
	public Set<FragmentGraph>
		generateFragmentGraphs(JCas text) throws FragmentGraphGeneratorException; 

}

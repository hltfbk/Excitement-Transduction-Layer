package eu.excitementproject.tl.composition.api;

//import java.util.List;
import java.util.Set;

import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.search.NodeMatch;

/**
 * OMQ usecase composition-flow "NodeMatcher"
The module will match the given fragment graph to the entailment graph. 
It will return a set of node matches (matching nodes in the entailment graph
associated to match confidence scores) as the result. 
  
 * @author Kathrin Eichler
 *
 */
public interface NodeMatcher {

	/**
	 * @param fragmentGraph - fragment graph ({@link FragmentGraph})
	 * @return set of node matches ({@link NodeMatch})
	 * @throws NodeMatcherException if node matching fails
	 */
	public Set<NodeMatch> findMatchingNodesInGraph(FragmentGraph fragmentGraph) 
			throws NodeMatcherException;

}

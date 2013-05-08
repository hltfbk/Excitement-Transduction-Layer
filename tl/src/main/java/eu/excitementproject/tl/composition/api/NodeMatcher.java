package eu.excitementproject.tl.composition.api;

import java.util.List;
import java.util.Set;

import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;

/**
 * ** OMQ usecase composition-flow "NodeMatcher"
The module will match the given fragment graph to the entailment
graph. It will return a set of "related nodes" of the entailment graph
as the result. The module will do a fast matching (e.g. search-engine
like matching) to produce results in near-real-time.
- input: A list of fragment graph (=List<FragmentGraph>=), one
  entailment graph (=EntailmentGraphRaw=)   
- output: a set of matching nodes of the entailment graph, with
  some information. (=Set<MatchingNode>=)   
- failure: if the data is missing or some unexpected error occurs, it
  will raise an exception. (We will define an exception for this
  module type). 
  
 * @author Kathrin Eichler
 *
 */
public interface NodeMatcher {

	/**
	 * @param fragmentGraphs - set of fragment graphs ({@link FragmentGraph})
	 * @param entailmentGraph - one entailment graph (raw) ({@link EntailmentGraphRaw})
	 * @return set of node matches ({@link NodeMatch})
	 * @throws NodeMatcherException if node matching fails
	 */
	public Set<NodeMatch> findMatchingNodesInGraph(Set<FragmentGraph> fragmentGraphs, EntailmentGraphRaw entailmentGraph) 
			throws NodeMatcherException;


}

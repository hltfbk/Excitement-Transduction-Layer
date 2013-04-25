package eu.excitementproject.tl.composition.api;

import java.util.Set;

import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;


/**
 * ** Merge Graph Module
- input-case1: a set of FragmentGraphs (=Set<FragmentGraph>=), and one work
  graph (=EntailmentGraphRaw=).
- input-case2: one FragmentGraph (=FragmentGraph=), and one work
  graph (=EntailmentGraphRaw=).
- (not for prototype) input-case3: two work graphs (=EntailmentGraphRaw=,
  =EntailmentGraphRaw=).
- output (case1,2): no additional output. When successfully called,
  the work graph (EntailmentGraphRaw) will be enriched by the given
  fragment, or by the given set of fragments.
- (not for prototype) output-case3: one new work graph will be
  returned. This graph may/maynot be one of the given graphs.
- failues: if the implementation can't merge the graphs for some
  reason, it will raise an exception. (We will define an exception
  for this module type).
- interface:
We can express the above mentioned contract with an interface with
a few methods. For the prototype, we are expecting to share one
implementation, but this can be expanded in the future.
- dependency:
An implementation of this implmenetation need to call LAP and EDA.
The needed LAP & EDA related configurations should be passed via the
Constructor (Thus, they are not defined in the interface). Also, any
additional configurable parameters of this module implementation
should be clearly exposed in the Constructor.

(Q: I've assumed that we need both input-case 1 & 2. Is this right?)
(Q: I've assumed that input case 3 is not needed for prototype)

 *  @author Lili

 */
public interface GraphMerger {
	
	/** input-case1: a set of FragmentGraphs ({@link FragmentGraph}), and one work
  graph ({@link EntailmentGraphRaw}).
	 * @param fragmentGraphs
	 * @param workGraph
	 * @return the work graph (EntailmentGraphRaw) enriched by the given set of fragments
	 * @throws GraphMergerException if the implementation can't merge the graphs for some
  reason
	 */
	public EntailmentGraphRaw mergeGraphs(
		Set<FragmentGraph> fragmentGraphs,
		EntailmentGraphRaw workGraph) throws GraphMergerException;

	/** input-case2: a single FragmentGraph ({@link FragmentGraph}), and a work
	  graph ({@link EntailmentGraphRaw}).
		 * @param fragmentGraphs
		 * @param workGraph
		 * @return the work graph (EntailmentGraphRaw) enriched by the given fragment
		 * @throws GraphMergerException if the implementation can't merge the graphs for some
	  reason
		 */
	public EntailmentGraphRaw mergeGraphs(
			FragmentGraph fragmentGraph,
			EntailmentGraphRaw workGraph) throws GraphMergerException;
	
}

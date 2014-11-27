package eu.excitementproject.tl.composition.api;

import java.util.Set;

import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;


/**
 Merge Graph Module Interface 
  
<p> <b> Input-case1:</b> a set of {@link FragmentGraph}s, and one work
  graph {@link EntailmentGraphRaw}.
<p> <b>Input-case2:</b> one {@link FragmentGraph}, and one work
  graph {@link EntailmentGraphRaw}.

<p> <b>Output (case1,2):</b> No additional output. When successfully called,
  the work graph will be enriched by merging it with the given
  fragment graph (set of fragment graphs).

<p> <b>Failures:</b> if the implementation can't merge the graphs for some
  reason, it will raise a {@link GraphMergerException}.

<p> <b>Dependency:</b> An implementation of this interface may need to call LAP and EDA.
The needed LAP & EDA related configurations should be passed via the
Constructor (thus, they are not defined in the interface). Also, any
additional configurable parameters of this module implementation
should be clearly exposed in the Constructor.

 @author Lili Kotlerman

 */
public interface GraphMerger {
	
	/** Input-case0: a set of FragmentGraphs ({@link FragmentGraph})
		 * @param fragmentGraphs
		 * @return a work graph ({@link EntailmentGraphRaw}) is created and enriched by the given set of fragments
		 * @throws GraphMergerException if the implementation can't merge the graphs for some
	  reason
		 */
		public EntailmentGraphRaw mergeGraphs(
			Set<FragmentGraph> fragmentGraphs) throws GraphMergerException;

	
	/** Input-case1: a set of FragmentGraphs ({@link FragmentGraph}), and one work
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
	
	/**
	 * Set confidence threshold to be applied while merging.
	 * To disable thresholding, set the threshold to null.
	 * @param entailmentConfidenceThreshold the entailmentConfidenceThreshold to set
	 */
	public void setEntailmentConfidenceThreshold(Double entailmentConfidenceThreshold);


	/**
	 * @return the number of EDA calls made to perform merging. If this information is not available, return null    
	 */
	public Integer getEdaCallsNumber();
}

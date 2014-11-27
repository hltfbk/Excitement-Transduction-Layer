package eu.excitementproject.tl.composition.api;

import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;


/**
 Graph Optimizer Module Interface 
 
<p> <b>Input:</b> One raw entailment graph {@link EntailmentGraphRaw}.
<p>	<b>Output:</b> One collapsed entailment graph {@link EntailmentGraphCollapsed}.
<p>	<b>Failures:</b>  If the implementation cannot produce the collapsed graph for some
  reason, it will raise a {@link GraphOptimizerException}.
<p>	<b>Dependency:</b> We do not foresee any external EOP component dependency for this
module. Still, like other modules, if it needs any arguments or
configurable values, they should be exposed in the implementation
constructor.

 * @author Lili Kotlerman

 */
public interface GraphOptimizer {

	/**
	 * @param workGraph - the input raw entailment graph ({@link EntailmentGraphRaw})
	 * @return collapsed entailment graph ({@link EntailmentGraphCollapsed})
	 * @throws GraphOptimizerException if the implementation cannot produce the collapsed graph for some reason
	 */
	public EntailmentGraphCollapsed optimizeGraph(EntailmentGraphRaw workGraph) 
			throws GraphOptimizerException;

	/**
	 * @param workGraph - the input raw entailment graph ({@link EntailmentGraphRaw})
	 * @param confidenceThreshold - threshold value on entailment confidence. All (positive) entailment edges in the raw graph with confidence lower than this threshold will be considered non-entailing.
	 * @return collapsed entailment graph ({@link EntailmentGraphCollapsed})
	 * @throws GraphOptimizerException if the implementation cannot produce the collapsed graph for some reason
	 */
	public EntailmentGraphCollapsed optimizeGraph(EntailmentGraphRaw workGraph, Double confidenceThreshold) 
			throws GraphOptimizerException;
}

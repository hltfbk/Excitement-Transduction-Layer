package eu.excitementproject.tl.composition.api;

import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;


/**
 *** Convert Graph Module
- input: one entailment graph (=EntailmentGraphRaw=).
- output: one collapsed entailment graph
  (=EntailmentGraphCollapsed=).
- failues: if the implementation can't convert the graph for some
  reason, it will raise an exception. (We will define an exception
  for this module type).
- interface:
We can express the above mentioned contract with an interface with
a few methods. For the prototype, we are expecting to share one
implementation, but this can be expanded in the future.
- dependency:
We do not foresee any external EOP component dependency for this
module. But this is not definite. The first prototype will shed some
light for us. Like other modules, if it needs any arguments or
configurable values, they will be exposed in the implementation
constructor.

** OMQ usecase composition-flow "Matcher"
(Gil: I will update this part after finishing use-case 2 documentation)

** OMQ usecase composition-flow "Annotator"
(Gil: I will update this part after finishing use-case 2 documentation)

 * @author Lili

 */
public interface CollapsedGraphGenerator {

	/**
	 * @param workGraph - one entailment graph ({@link EntailmentGraphRaw})
	 * @return one collapsed entailment graph ({@link EntailmentGraphCollapsed})
	 * @throws CollapsedGraphGeneratorException if the implementation can't convert the graph for some
  reason
	 */
	public EntailmentGraphCollapsed generateCollapsedGraph(EntailmentGraphRaw workGraph) 
			throws CollapsedGraphGeneratorException;

	public EntailmentGraphCollapsed generateCollapsedGraph(EntailmentGraphRaw workGraph, Double confidenceThreshold) 
			throws CollapsedGraphGeneratorException;
}

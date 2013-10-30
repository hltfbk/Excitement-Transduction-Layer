package eu.excitementproject.tl.decomposition.api;

import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public interface CollapsedGraphGenerator {
	public EntailmentGraphCollapsed generateCollapsedGraph(EntailmentGraphRaw workGraph) throws Exception;
	public EntailmentGraphCollapsed generateCollapsedGraph(EntailmentGraphRaw workGraph, Double confidenceThreshold) throws Exception;
}

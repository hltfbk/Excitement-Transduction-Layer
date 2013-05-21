package eu.excitementproject.tl.structures.rawgraph.utils;

public enum EdgeType {

	GeneratedByEDA,
	CopiedFromFragmentGraph,
	InducedByTransitivity,
	Unknown, //ToDo: do we need Unknown type? Are there other types?
	;
	
	public boolean is(EdgeType edgeType){
		if (this.equals(edgeType)) return true;
		return false;
	}
	
}

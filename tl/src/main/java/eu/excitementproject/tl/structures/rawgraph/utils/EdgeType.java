package eu.excitementproject.tl.structures.rawgraph.utils;

public enum EdgeType {

	EDA,
	FRAGMENT_GRAPH,
	INDUCED,
	UNKNOWN, //TODO: do we need Unknown type? Are there other types?
	;
	
	public boolean is(EdgeType edgeType){
		if (this.equals(edgeType)) return true;
		return false;
	}
	
/*	public String toString(){
		if (this.is(EdgeType.EDA)) return "EDA edge";
		if (this.is(EdgeType.FRAGMENT_GRAPH)) return "FRAGMENT_GRAPH edge";
		if (this.is(EdgeType.INDUCED)) return "INDUCED edge";
		return "UNKNOWN edge type";		
	}
*/	
}

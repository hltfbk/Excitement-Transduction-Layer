package eu.excitementproject.tl.structures.rawgraph.utils;

/**
 * Enum to store the types of edges in TL raw graphs (EntailmentGraphRaw)
 * @author Lili Kotlerman
 *
 */
public enum EdgeType {

	EDA,
	FRAGMENT_GRAPH,
	INDUCED,
	MANUAL_ANNOTATION,
	UNKNOWN, 
	;
	

	public boolean is(EdgeType edgeType){
		if (this.equals(edgeType)) return true;
		return false;
	}
	
	public static EdgeType convert(String str){
		if (str.equals("EDA")) return EDA;
		if (str.equals("FRAGMENT_GRAPH")) return FRAGMENT_GRAPH;
		if (str.equals("INDUCED")) return INDUCED;
		if (str.equals("MANUAL_ANNOTATION")) return MANUAL_ANNOTATION;
		return UNKNOWN;
	}
	
}

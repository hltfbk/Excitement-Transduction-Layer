package eu.excitementproject.tl.structures.rawgraph.utils;

/**
 * Enum to store the types of edges in TL raw graphs (EntailmentGraphRaw)
 * @author Lili Kotlerman
 *
 */
public enum EdgeType { // note that the types are not mutually exclusive, choice of edge type depends on the type of the graph, and the context in which it is used

	EDA, //e.g. EDA can be called for direct edges, as well as for edges that could have been induced by transitive closure
	FRAGMENT_GRAPH,
	INDUCED, 
	MANUAL_ANNOTATION,
	
	TRANSITIVE_CLOSURE,
	DIRECT,
	
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
		if (str.equals("DIRECT")) return DIRECT;
		if (str.equals("TRANSITIVE_CLOSURE")) return TRANSITIVE_CLOSURE;
		
		return UNKNOWN;
	}
	
}

package  eu.excitementproject.tl.structures.collapsedgraph;

import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;



/**
 * 
 * @author vivi@fbk & Lili Kotlerman
 * 
 * The edge is obtained by collapsing multiple edges (aka decisions from different EDAs)
 * into one edge
 *
 * This class extends DefaultEdge:
 * http://jgrapht.org/javadoc/org/jgrapht/graph/DefaultEdge.html
 * 
 * @param <V> -- the vertex class for the EntailmentGraphCollapsed (EquivalenceClass)
 */
public class EntailmentRelationCollapsed extends DefaultEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	EquivalenceClass source;
	EquivalenceClass target;
	
	EdgeType edgeType;
	
	double confidence;
	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/
	
	/**
	 * 
	 * @param source -- source vertex
	 * @param target -- target vertex
	 * @param c -- confidence of the entailment relation, obtained by combining the edges of the work graph (EntailmentGraphRaw)
	 *  (corresponding to decisions from various EDAs) 
	 */
	public EntailmentRelationCollapsed(EquivalenceClass source, EquivalenceClass target, double c) {
		this(source, target, c, EdgeType.DIRECT); //by default assign "direct" edge type
	}
	

	/**
	 * @return the edgeType
	 */
	public EdgeType getEdgeType() {
		return edgeType;
	}

	public EntailmentRelationCollapsed(EquivalenceClass source, EquivalenceClass target, double c, EdgeType edgeType) {
		this.source = source;
		this.target = target;
		this.confidence = c;
		this.edgeType = edgeType;
	}
	/******************************************************************************************
	 * SETTERS/GERRETS
	 * ****************************************************************************************/

	/**
	 * @return the source
	 */
	@Override
	public EquivalenceClass getSource() {
		return source;
	}

	/**
	 * @return the target
	 */
	@Override
	public EquivalenceClass getTarget() {
		return target;
	}
	
	/**
	 * @return the confidence
	 */
	public double getConfidence(){
		return confidence;
	}
	
	/******************************************************************************************
	 * PRINT
	 * ****************************************************************************************/

	@Override 
	public String toString(){
		return this.getSource().getLabel().trim().replaceAll(" +", " ")+" --> "+this.getTarget().getLabel().trim().replaceAll(" +", " ") +" ("+this.getConfidence()+") ";
	}	
	
	/** Returns a string with the edge in DOT format for outputting the graph
	 * @return the generated string
	 */
	public String toDOT(){
		String s = this.getSource().toDOT()+ " -> "+this.getTarget().toDOT();
		s+= " [label="+this.getConfidence()+"]";
		s+= " [color=blue]";
		return s+"\n";
	}	
	
	/******************************************************************************************
	 * METHODS FOR INTERNAL TESTING PURPOSES
	 * ****************************************************************************************/
	/** Returns a string with the edge in DOT format for outputting the graph
	 * @return the generated string
	 */
	public String toDOT(Map<String,String> nodeTextById){
		String s = this.getSource().toDOT(nodeTextById)+ " -> "+this.getTarget().toDOT(nodeTextById);
		s+= " [label="+this.getConfidence()+"]";
		s+= " [color=blue]";
		return s+"\n";
	}	
}

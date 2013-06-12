package  eu.excitementproject.tl.structures.collapsedgraph;

import org.jgrapht.graph.DefaultEdge;



/**
 * 
 * @author vivi@fbk
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
	
	double confidence;
	
	/**
	 * 
	 * @param source -- source vertex
	 * @param target -- target vertex
	 * @param c -- confidence of the entailment relation, obtained by combining the edges of the work graph (EntailmentGraphRaw)
	 *  (corresponding to decisions from various EDAs) 
	 */
	public EntailmentRelationCollapsed(EquivalenceClass source, EquivalenceClass target, double c) {
		this.source = source;
		this.target = target;
		this.confidence = c;
	}

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
	
	public double getConfidence(){
		return confidence;
	}
	
	
	
}

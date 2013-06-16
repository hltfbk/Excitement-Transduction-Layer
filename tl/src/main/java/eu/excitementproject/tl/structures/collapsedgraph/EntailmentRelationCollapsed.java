package  eu.excitementproject.tl.structures.collapsedgraph;

import org.jgrapht.graph.DefaultEdge;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;



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
	
	@Override 
	public String toString(){
		return this.getSource().getLabel().trim().replaceAll(" +", " ")+" --> "+this.getTarget().getLabel().trim().replaceAll(" +", " ") +" ("+this.getConfidence()+") ";
	}	
	
	public String toDOT(){
		String s = this.getSource().toDOT()+ " -> "+this.getTarget().toDOT();
		s+= " [label="+this.getConfidence()+"]";
		s+= " [color=blue]";
		return s+"\n";
	}	
	
}

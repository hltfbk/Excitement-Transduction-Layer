package eu.excitementproject.tl.structures.fragmentgraph;

import org.jgrapht.graph.DefaultEdge;



/**
 * 
 * @author vivi@fbk
 * 
 * The edge class for the FragmentGraph
 * For now edges are directed, with default weight.
 * 
 * This class extends {@link DefaultEdge}, documented here:
 * http://jgrapht.org/javadoc/org/jgrapht/graph/DefaultEdge.html
 */
public class FragmentGraphEdge extends DefaultEdge{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2082959681128417819L;
	
	EntailmentUnitMention source;
	EntailmentUnitMention target;
	
	double weight = 1;
	
	/**
	 * Default constructor
	 * @param v1 -- source vertex
	 * @param v2 -- target vertex
	 * @param w -- edge weight
	 */
	FragmentGraphEdge(EntailmentUnitMention v1, EntailmentUnitMention v2, double w) {
		source = v1;
		target = v2;
		weight = w;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getWeight(){
		return weight;
	}
	
}

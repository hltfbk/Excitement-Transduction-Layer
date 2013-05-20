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
	 * 
	 * @return -- edge's weight
	 */
	public double getWeight(){
		return weight;
	}

	/* =========== The two getters are added by Lili on May, 20. */
	
	@Override
	public EntailmentUnitMention getSource() {
		return source;
	}

	@Override
	public EntailmentUnitMention getTarget() {
		return target;
	}

	
	
}

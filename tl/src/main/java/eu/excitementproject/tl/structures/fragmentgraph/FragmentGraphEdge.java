package eu.excitementproject.tl.structures.fragmentgraph;

import org.jgrapht.graph.DefaultEdge;



/**
 * 
 * The edge class for the FragmentGraph
 * For now edges are directed, with default weight.
 * 
 * This class extends {@link DefaultEdge}, documented here:
 * http://jgrapht.org/javadoc/org/jgrapht/graph/DefaultEdge.html
 * 
 * @author Vivi Nastase
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

	/* =========== The two getters are added by Lili on May, 20.*/ 
	
	@Override
	public EntailmentUnitMention getSource() {
		return source;
	}

	@Override
	public EntailmentUnitMention getTarget() {
		return target;
	}

	public FragmentGraphEdge(EntailmentUnitMention source,
			EntailmentUnitMention target) {
		super();
		this.source = source;
		this.target = target;
	}
	
	
	// stolen from the EntailmentRelation
	/******************************************************************************************
	 * PRINT
	 * ****************************************************************************************/
	@Override 
	public String toString(){
		return this.getSource().getText()+" --> "+this.getTarget().getText() +" (Default entailment) ";
	}
	
	/** Returns a string with the edge in DOT format for outputting the graph
	 * @return the generated string
	 */	public String toDOT(){
		String s = "\""+this.getSource().getTextWithoutDoubleSpaces()+"\" -> \""+this.getTarget().getTextWithoutDoubleSpaces()+"\"";
		s+= " [label=1]";
		String color = "red";
		color = "green";		
		s+= " [color="+color+"]";
		return s+"\n";
	}
	
}

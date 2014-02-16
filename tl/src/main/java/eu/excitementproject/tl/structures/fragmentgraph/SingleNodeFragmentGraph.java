package eu.excitementproject.tl.structures.fragmentgraph;


import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;



/**
 * @author Lili Kotlerman
 *
 */
public class SingleNodeFragmentGraph extends FragmentGraph {

	private static final long serialVersionUID = -2323354508908941295L;

	public SingleNodeFragmentGraph(String text) {
		this(new ClassBasedEdgeFactory<EntailmentUnitMention, FragmentGraphEdge>(FragmentGraphEdge.class));
		baseStatement = new EntailmentUnitMention(text, 0 , text);
		topStatement = new EntailmentUnitMention(text, 0 , text);
		this.addNode(baseStatement);
	}
	
	public SingleNodeFragmentGraph(
			EdgeFactory<EntailmentUnitMention, FragmentGraphEdge> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

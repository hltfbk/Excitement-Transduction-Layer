package eu.excitementproject.tl.structures.fragmentgraph;

import java.util.HashSet;

import org.apache.uima.jcas.JCas;
import org.jgrapht.graph.ClassBasedEdgeFactory;

import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitement.type.tl.ModifierAnnotation;

/**
 * Class for the "lite" version of the fragment graphs, with only top (complete) and bottom (base) nodes (statements)
 * 
 * @author vivi@fbk
 *
 */
public class FragmentGraphLite extends FragmentGraph {


	private static final long serialVersionUID = -518117209342716390L;

	/**
	 * Basic constructor from edge class
	 * 
	 * @param edgeClass
	 */
	public FragmentGraphLite(Class<? extends FragmentGraphEdge> edgeClass) {
		super(edgeClass);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor from edge factory
	 * 
	 * @param classBasedEdgeFactory
	 */
	public FragmentGraphLite(
			ClassBasedEdgeFactory<EntailmentUnitMention, FragmentGraphEdge> classBasedEdgeFactory) {
		super(classBasedEdgeFactory);
	}
	
	/**
	 * Build a fragment graph from a (determined) fragment in a CAS object corresponding to a document,
	 * based on the modifier annotations in the fragment.
	 * 
	 * This will build a graph where:
	 * -- each node is the fragment text, minus a subset of modifiers
	 * (NOTE: the modifier combinations kept should be valid! i.e., we cannot have a modifier A
	 *  that depends on another modifier B, but not B)
	 * -- there is an edge between two nodes A and B (direction: A->B), where the set of modifiers in node B M_B = M_A \cup {M_i} 
	 * 
	 * @param aJCas -- CAS object containing annotations for a document
	 * @param f -- the fragment annotation from which to produce a {@link} FragmentGraph 
	 */
	public FragmentGraphLite(JCas aJCas, FragmentAnnotation frag) {
		this(new ClassBasedEdgeFactory<EntailmentUnitMention, FragmentGraphEdge>(FragmentGraphEdge.class));
		
		document = aJCas;
		fragment = frag;
		baseStatement = new EntailmentUnitMention(aJCas, frag, new HashSet<ModifierAnnotation>());
		topStatement = new EntailmentUnitMention(aJCas, frag, FragmentGraph.getFragmentModifiers(aJCas, frag));
		buildGraph();
	}
	
	/**
	 * builds a graph consisting of only top and bottom nodes
	 */
	private void buildGraph() {
		this.addNode(baseStatement);
		this.addNode(topStatement);
		if (! baseStatement.equals(topStatement)) {
			this.addEdge(topStatement,baseStatement);
		}
	}
}

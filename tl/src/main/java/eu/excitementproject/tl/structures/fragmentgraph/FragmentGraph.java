package eu.excitementproject.tl.structures.fragmentgraph;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

/**
 * 
 * @author vivi@fbk
 *
 *	Graph structure for a text fragment.
 *	We assume a text fragment is composed of a base statement (BS) plus a number of modifiers (M).
 *  A node of this graph will correspond to BS + M_1 ... M_k
 *  We assume a textual entailment (TE) relation between every two statements (S_i, S_j) that differ only
 *  by one modifier: S_i = S_j + M_x => S_i -TE-> S_j
 *  
 *  This class extends the DefaultDirectedWeightedGraph class, because the graph is directed
 *  and we might decide to have the edges weighted. Currently they are not.
 *  
 *  JavaDoc for DefaultDirectedWeightedGraph class for information about inherited methods:
 *  http://jgrapht.org/javadoc/org/jgrapht/graph/DefaultDirectedWeightedGraph.html
 *
 * @param <V> Vertex class
 * @param <E> Edge class
 */
public class FragmentGraph extends DefaultDirectedWeightedGraph<EntailmentUnitMention,FragmentGraphEdge> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4631493969220124299L;
	
	/*
	 * apart from the graph's structure, we might benefit from keeping track 
	 * of the base statements (what WP2 calls "base predicates", but that confused us
	 * so I renamed them to what we called them in our meeting)
	 */
	EntailmentUnitMention baseStatement;

	/**
	 * a CAS object that holds contextual (and structural) information for the text fragment
	 * 
	 * Contextual information covers the document where the fragment comes from, position of
	 * the fragment in the document, etc.
	 * 
	 * Structural information covers tokenization, POS, NEs, parse tree if available, etc.
	 */
	JCas fragmentCAS;
	
	/**
	 * Default constructor
	 * 
	 * @param arg0 -- edge factory for the graph
	 */
	public FragmentGraph(EdgeFactory<EntailmentUnitMention,FragmentGraphEdge> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Default constructor 
	 * 
	 * @param edgeClass -- class of the graph's edges (FragmentGraphEdge)
	 */
	public FragmentGraph(Class<? extends FragmentGraphEdge> edgeClass) {
		this(new ClassBasedEdgeFactory<EntailmentUnitMention, FragmentGraphEdge>(edgeClass));
		// TODO Auto-generated constructor stub
	}
	
/*	
	public void addNode(V v) {
		this.addNode(v);
	}
*/	
	/**
	 * 
	 * @return the base statements of the fragment graph (useful for merging methods) -- for compatibility upwards (with WorkGraph)
	 */
	public EntailmentUnitMention getBaseStatement(){
		return baseStatement;
	}

	/**
	 * This might be a useful method for the graph merging part, if we follow the
	 * implementation ideas from WP2
	 * 
	 * @param level -- the number of modifiers on top of the base statement
	 * @return -- the set of nodes with "level" number of modifiers 
	 */
/*	public Set<V> getNodes(int level) {
		Set<V> nodes = new HashSet<V>();
		nodes.add(baseStatement);
		return getNodes(level, nodes);
	}
	
	public Set<V> getNodes(int level, Set<V> nodes){
		if (level == 0)
			return nodes;
		
		Set<V> newNodes = new HashSet<V>();
		for(V n: nodes) {
			for(E e: this.edgesOf(n)) {
				newNodes.add(this.getEdgeTarget(e));
			}
		}
		return getNodes(level-1,newNodes);
	}
*/

	/**
	 * 
	 * @param level -- the number of modifiers desired
	 * @return -- the nodes that have "level" number of modifiers (are at distance "level" from the root, aka the base statement)
	 */
	public Set<EntailmentUnitMention> getNodes(int level) {
		Set<EntailmentUnitMention> nodes = new HashSet<EntailmentUnitMention>();
		
		for(EntailmentUnitMention v: this.vertexSet()) {
			if (((EntailmentUnitMention) v).getLevel() == level) {
				nodes.add(v);
			}
		}
		return nodes;
	}
	
	
	@Override
	public String toString() {
		String str = "";
		for(EntailmentUnitMention v : this.vertexSet()) {
			str += "vertex: " + v.toString() + "\n";
			for(EntailmentUnitMention x: this.vertexSet()) {
				if (this.containsEdge(v, x))
				str += "\tedge to: " + x.toString() + "\n";
			}
		}
		return str;
	}	

}

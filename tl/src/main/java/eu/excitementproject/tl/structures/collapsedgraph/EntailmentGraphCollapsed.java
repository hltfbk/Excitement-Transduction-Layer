package eu.excitementproject.tl.structures.collapsedgraph;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * 
 * @author vivi@fbk
 *
 * The structure of the collapsed graph (cleaned up edges, clustered nodes in equivalence classes)
 * 
 * This graph is built from the work graph, by collapsing multiple edges between the same
 * pair of vertices into one edge, and grouping entailment units into equivalence classes.
 * Unlike the work graph, this is no longer a multigraph, but a simple directed graph. 
 * 
 * It extends DefaultDirectedWeightedGraph, for inherited methods see the JavaDoc:
 * http://jgrapht.org/javadoc/org/jgrapht/graph/DefaultDirectedWeightedGraph.html
 * 
 */

public class EntailmentGraphCollapsed extends DefaultDirectedWeightedGraph<EquivalenceClass,EntailmentRelationCollapsed>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5957243707939421299L;
		
	/**
	 * Default graph constructor
	 * @param arg0 -- edge factory
	 */
	public EntailmentGraphCollapsed(EdgeFactory<EquivalenceClass, EntailmentRelationCollapsed> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Default constructor
	 * 
	 * @param arg0 -- edge class
	 */
	public EntailmentGraphCollapsed(Class<? extends EntailmentRelationCollapsed> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Converts an input work graph to a format that would be useful to the end users
	 * This might mean changing the nodes from complex annotated objects to sets of strings
	 * and compressing multiple edges into one
	 * @param <WV>
	 * @param <WE>
	 * 
	 * @param wg
	 */
	public void convertGraph(EntailmentGraphRaw wg) {
		// iterate over wg's vertices and build the corresponding EntailmentGraphCollapsed nodes,
		// and over all edges starting from the current vertex, and either choose
		// not to include them in the final graph, or compress multiple edges 
		// connecting the same two vertices into one
		
		try {
		
			Map<EntailmentUnit,EquivalenceClass> nodeMap = new HashMap<EntailmentUnit,EquivalenceClass>();
			for(EntailmentUnit wv: wg.vertexSet()) {
				EquivalenceClass v = new EquivalenceClass(wv);
				nodeMap.put(wv, v);
				this.addVertex(v);
				for (EntailmentRelation we: wg.outgoingEdgesOf(wv)) {
					EntailmentUnit _wv = wg.getEdgeTarget(we);
					EquivalenceClass _v;
					if (! nodeMap.containsKey(_wv)) {
						_v = new EquivalenceClass(_wv);
						nodeMap.put(_wv,_v);
					} else {
						_v = nodeMap.get(_wv);
					}
					this.addEdge(v,_v);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}

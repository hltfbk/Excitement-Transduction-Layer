package eu.excitementproject.tl.structures.rawgraph;

import java.io.File;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

import eu.excitementproject.eop.common.EDABasic;
import fragmentGraph.FragmentGraph;


/**
 * 
 * @author vivi@fbk
 * 
 * The graph structure for the work graph. We call it EntailmentGraphRaw.
 * This graph grows by adding to it FragmentGraph-s by "merging"
 * The merging is done through an interface. 
 * The nodes are entailment units, and the edges (entailment relation) are generated
 * based on decisions from the EDAs. As such there can be several edges between the same
 * two nodes, each corresponding to one EDA query.  
 * 
 *  This graph extends DirectedMultigraph, to allow for multiple directed edges between
 *  the same two nodes. The JavaDoc for the {@link DirectedMultigraph} for information about
 *  inherited methods is here:
 *  http://jgrapht.org/javadoc/org/jgrapht/graph/DirectedMultigraph.html
 */
public class EntailmentGraphRaw extends
		DirectedMultigraph<EntailmentUnit,EntailmentRelation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3274655854206417667L;
	/*
	 * To build the work graph we need to know the configuration,
	 * and in particular the EDA and LAP to use (and possibly other stuff)
	 */

	/**
	 * The EDA used to produce TE decisions for building new edges
	 */
	EDABasic<?> eda;
	
		
	/**
	 * 
	 * @param arg0 -- the class for the edges (in our case this would be FragmentGraphEdge.class)
	 */
	public EntailmentGraphRaw(Class<? extends EntailmentRelation> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * @param arg0 -- edge factory
	 */
	public EntailmentGraphRaw(EdgeFactory<EntailmentUnit,EntailmentRelation> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/*
	 * a constructor for initializing a graph from a (xml) file
	 */
	/**
	 * 
	 * @param file -- a file (possibly xml) from which to load a previously produced graph
	 */
	public EntailmentGraphRaw(File file){
		super(EntailmentRelation.class);
//		loadGraphFromFile(file);
	}
	
	/**
	 * Initialize a work graph from a fragment graph
	 * @param fg -- a fragment graph object
	 */
	public EntailmentGraphRaw(FragmentGraph fg) {
		// to do
		super(EntailmentRelation.class);
	}
	
	/**
	 * Get the base statements of a work graph for the merging procedure (according to
	 * the process used in WP2, where pairs of base statements are compared first, and
	 * if there is entailment, pair up the corresponding extension (+ 1 modifier each)
	 * 
	 * @return -- the base statements (the roots) of the graph
	 */
	public Set<EntailmentUnit> getBaseStatements(){
//		return baseStatements;
		return null;
	}
}

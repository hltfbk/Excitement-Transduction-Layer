package eu.excitementproject.tl.structures.rawgraph;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphEdge;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;
import eu.excitementproject.tl.structures.rawgraph.utils.TEDecisionByScore;


/**
 * 
 * @author vivi@fbk & LiliKotlerman
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
	
	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/

	/**
	 * 
	 * @param arg0 -- the class for the edges (in our case this would be FragmentGraphEdge.class)
	 */
	public EntailmentGraphRaw(Class<? extends EntailmentRelation> arg0) {		
		super(arg0);
	}
	
	/**
	 * 
	 * @param arg0 -- edge factory
	 */
	public EntailmentGraphRaw(EdgeFactory<EntailmentUnit,EntailmentRelation> arg0) {
		super(arg0);		
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
		super(EntailmentRelation.class);
		for (FragmentGraphEdge fragmentGraphEdge : fg.edgeSet()){
			this.addEdgeFromFrahmentGraph(fragmentGraphEdge);
		}
	}
	
	
	/**
	 * Initialize an empty work graph
	 */
	public EntailmentGraphRaw(){
		super(EntailmentRelation.class);
	}
	

	/******************************************************************************************
	 * SETTERS/GERRETS
	 * ****************************************************************************************/

	
	/**
	 * Get the base statements of a work graph for the merging procedure (according to
	 * the process used in WP2, where pairs of base statements are compared first, and
	 * if there is entailment, pair up the corresponding extension (+ 1 modifier each)
	 * 
	 * @return -- the base statements (the roots) of the graph
	 */
	public Set<EntailmentUnit> getBaseStatements(){
		Set<EntailmentUnit> baseStatements = new HashSet<EntailmentUnit>();
		for (EntailmentUnit node : this.vertexSet()){
			if (node.isBaseStatement()) baseStatements.add(node);
		}
		return baseStatements;
	}
	
	

	/******************************************************************************************
	 * OTHER METHODS TO WORK WITH THE GRAPH
	 * ****************************************************************************************/

	/** Returns the set of nodes, which entail the given node
	 * @param node whose entailing nodes are returned
	 * @return Set<EntailmentUnit> with all the entailing nodes of the given node
	 */
	public Set<EntailmentUnit> getEntailingNodes(EntailmentUnit node){
		Set<EntailmentUnit> entailingNodes = new HashSet<EntailmentUnit>();
		for (EntailmentRelation edge : this.incomingEdgesOf(node)){
			entailingNodes.add(edge.getSource());
		}
		return entailingNodes;
	}
	
	/** Returns the set of nodes, which entail the given node and have the specified level (number of modifiers)
	 * @param node whose entailing nodes are returned
	 * @return Set<EntailmentUnit> with the entailing nodes of the given node
	 */
	public Set<EntailmentUnit> getEntailingNodes(EntailmentUnit node, int level){
		Set<EntailmentUnit> entailingNodes = new HashSet<EntailmentUnit>();
		for (EntailmentRelation edge : this.incomingEdgesOf(node)){
			EntailmentUnit entailingNode = edge.getSource();
			if (entailingNode.getLevel()==level) entailingNodes.add(entailingNode);
		}
		return entailingNodes;
	}
	
	/** Returns the set of nodes, entailed by the given node
	 * @param node whose entailed nodes are returned
	 * @return Set<EntailmentUnit> with all the entailed nodes of the given node
	 */
	public Set<EntailmentUnit> getEntailedNodes(EntailmentUnit node){
		Set<EntailmentUnit> entailedNodes = new HashSet<EntailmentUnit>();
		for (EntailmentRelation edge : this.outgoingEdgesOf(node)){
			entailedNodes.add(edge.getTarget());
		}
		return entailedNodes;
	}
	
	/**
	 * Create an edge from sourceVertex to targetVertex using the specified eda 
	 * @param sourceVertex
	 * @param targetVertex
	 * @param eda
	 */
	public void addEdgeFromEDA(EntailmentUnit sourceVertex, EntailmentUnit targetVertex, EDABasic<?> eda){
		EntailmentRelation edge = new EntailmentRelation(sourceVertex, targetVertex, eda);
		this.addEdge(sourceVertex, targetVertex, edge);
	}
	
	/**
	 * Copy an edge from a FragmentGraph - if vertices do not exist - add them. If they do - increment the frequency counter
	 * @param fragmentGraphEdge -- the edge to copy into the graph
	 * TODO: how to deal with the original edge weight? Currently copied as is (=1 for everyone).
	 */
	public void addEdgeFromFrahmentGraph(FragmentGraphEdge fragmentGraphEdge){
		EntailmentUnit sourceVertex = getVertex(fragmentGraphEdge.getSource().getText());
		EntailmentUnit targetVertex = getVertex(fragmentGraphEdge.getTarget().getText());

		// if vertices do not exist - add them, otherwise - update their frequency
		if(sourceVertex==null){
			sourceVertex = new EntailmentUnit(fragmentGraphEdge.getSource());
			this.addVertex(sourceVertex);
		}
		else sourceVertex.incrementFrequency();
		
		if(targetVertex==null){
			targetVertex = new EntailmentUnit(fragmentGraphEdge.getTarget());
			this.addVertex(targetVertex);
		}
		else targetVertex.incrementFrequency();
		
		// now create and add the edge
		EntailmentRelation edge = new EntailmentRelation(sourceVertex, targetVertex, new TEDecisionByScore(fragmentGraphEdge.getWeight()), EdgeType.CopiedFromFragmentGraph);
		this.addEdge(sourceVertex, targetVertex, edge);
	}
	
	/**
	 * Create an edge induced by transitivity. Confidence is to be given as parameter.
	 * @param sourceVertex
	 * @param targetVertex
	 * @param confidence
	 */
	public void addEdgeByTransitivity(EntailmentUnit sourceVertex, EntailmentUnit targetVertex, Double confidence){
		EntailmentRelation edge = new EntailmentRelation(sourceVertex, targetVertex, new TEDecisionByScore(confidence), EdgeType.InducedByTransitivity);
		this.addEdge(sourceVertex, targetVertex, edge);
	}
	
	/**
	 * Return the vertex (EntailmentUnit) with the corresponding text, if it is found in the graph. 
	 * Otherwise return null.
	 * @param text the text of the EntailmentUnit to be found
	 * @return
	 */
	public EntailmentUnit getVertex(String text){
		for (EntailmentUnit eu : this.vertexSet()){
			if (eu.getText().equals(text)) return eu;
		}
		return null;
	}
	
	/**
	 * @return true if the graph has no vertices (i.e. the graph is empty) 
	 */
	public boolean isEmpty(){
		if(this.vertexSet().isEmpty()) return true;
		return false;
	}

	
	/******************************************************************************************
	 * PRINT GRAPH
	 * ****************************************************************************************/
	
	@Override
	public String toString(){
		String s = "";
		s+="\nNODES:";
		for (EntailmentUnit v: this.vertexSet()){
			s+="\n\t"+v.toString();
		}
		
		s+="\n\nBASE STATEMENT NODES:";
		for (EntailmentUnit v: this.getBaseStatements()){
			s+="\n\t"+v.toString();
		}

		s+="\n\nENTAILMENTS";
		for (EntailmentRelation e: this.edgeSet()){
			if ((e.getLabel().is(DecisionLabel.Entailment)) || (e.getLabel().is(DecisionLabel.Paraphrase))) {
				s+="\n\t"+e.toString();
			}
		}

		s+="\n\nALL EDGES:";
		for (EntailmentRelation e: this.edgeSet()){
			s+="\n\t"+e.toString();
		}
		
		return s;
	}
	
	/******************************************************************************************
	 * DUMMY FUNCTIONALITY
	 * ****************************************************************************************/
	
	/**
	 * Get a sample EntailmentGraphRaw
	 * @param randomEdges - True for random edges, False for 'correct' edges
	 * Nodes: (bs - base statement)
	 * 		A "Food was really bad." (modifier: really)
		bs	B "Food was bad."
		bs	C "I didn't like the food."
			D "a little more leg room would have been perfect" (modifier: "a little")
		bs	E "more leg room would have been perfect"
			F "Disappointed with the amount of legroom compared with other trains" (modifiers: "the amount of", "compared with other trains")
			G "Disappointed with legroom compared with other trains" (modifier: "compared with other trains")
			H "Disappointed with the amount of legroom" (modifier: "the amount of")
		bs	I "Disappointed with legroom"
			
	 */
		
	public static EntailmentGraphRaw getSampleOuput(boolean randomEdges){
		
		// create the to-be graph nodes
		EntailmentUnit A = new EntailmentUnit("Food was really bad.",1);
		EntailmentUnit B = new EntailmentUnit("Food was bad."); B.setBaseStatement(true);
		EntailmentUnit C = new EntailmentUnit("I didn't like the food."); C.setBaseStatement(true);
		EntailmentUnit D = new EntailmentUnit("a little more leg room would have been perfect",1);
		EntailmentUnit E = new EntailmentUnit("more leg room would have been perfect"); E.setBaseStatement(true);
		EntailmentUnit F = new EntailmentUnit("Disappointed with the amount of legroom compared with other trains",2);
		EntailmentUnit G = new EntailmentUnit("Disappointed with legroom compared with other trains",1);
		EntailmentUnit H = new EntailmentUnit("Disappointed with the amount of legroom",1);
		EntailmentUnit I = new EntailmentUnit("Disappointed with legroom"); I.setBaseStatement(true);

		// create an empty graph
		EntailmentGraphRaw sampleRawGraph = new EntailmentGraphRaw();
		
		// add nodes
		sampleRawGraph.addVertex(A); sampleRawGraph.addVertex(B); sampleRawGraph.addVertex(C);
		sampleRawGraph.addVertex(D); sampleRawGraph.addVertex(E); sampleRawGraph.addVertex(F);
		sampleRawGraph.addVertex(G); sampleRawGraph.addVertex(H); sampleRawGraph.addVertex(I);
		

		if (randomEdges){ // add random edges
			EDABasic<?> eda = new RandomEDA();
				
			// add edges - calculate TEDecision in both directions between all pairs of nodes (don't calculate for a node with itself) 
			for (EntailmentUnit v1 : sampleRawGraph.vertexSet()){
				for (EntailmentUnit v2 : sampleRawGraph.vertexSet()){
					if (!v1.equals(v2)) { //don't calculate for a node with itself  
						sampleRawGraph.addEdgeFromEDA(v1, v2, eda);
						sampleRawGraph.addEdgeFromEDA(v2, v1, eda);
					}
				}
			}
		}
		else{ // add 'correct' edges
			
	/*		Edges(Entailment relations in raw graph):
				A --> B (from fragment)
				B <--> C
				D --> E (from fragment)
				F --> G, H (from fragment)
				G --> I (from fragment)
				H --> I (from fragment)
				E <--> I			
	*/

			// add fragment graph edges
			sampleRawGraph.addEdge(A, B, new EntailmentRelation(A, B, new TEDecisionByScore(1.0)));
			sampleRawGraph.addEdge(B, A, new EntailmentRelation(B, A, new TEDecisionByScore(0.0)));
						
			sampleRawGraph.addEdge(D, E, new EntailmentRelation(D, E, new TEDecisionByScore(1.0)));
			sampleRawGraph.addEdge(E, D, new EntailmentRelation(E, D, new TEDecisionByScore(0.0)));
			
			sampleRawGraph.addEdge(F, G, new EntailmentRelation(F, G, new TEDecisionByScore(1.0)));
			sampleRawGraph.addEdge(G, F, new EntailmentRelation(G, F, new TEDecisionByScore(0.0)));
			sampleRawGraph.addEdge(F, H, new EntailmentRelation(F, H, new TEDecisionByScore(1.0)));
			sampleRawGraph.addEdge(H, F, new EntailmentRelation(H, F, new TEDecisionByScore(0.0)));
	
			sampleRawGraph.addEdge(G, I, new EntailmentRelation(G, I, new TEDecisionByScore(1.0)));
			sampleRawGraph.addEdge(I, G, new EntailmentRelation(I, G, new TEDecisionByScore(0.0)));

			sampleRawGraph.addEdge(H, I, new EntailmentRelation(H, I, new TEDecisionByScore(1.0)));
			sampleRawGraph.addEdge(I, H, new EntailmentRelation(I, H, new TEDecisionByScore(0.0)));

			
			// add edges "obtained" from eda (EdgeType.GeneratedByEDA)

			sampleRawGraph.addEdge(B, C, new EntailmentRelation(B, C, new TEDecisionByScore(0.72), EdgeType.GeneratedByEDA));
			sampleRawGraph.addEdge(C, B, new EntailmentRelation(C, B, new TEDecisionByScore(0.77), EdgeType.GeneratedByEDA));

			sampleRawGraph.addEdge(B, E, new EntailmentRelation(B, E, new TEDecisionByScore(0.05), EdgeType.GeneratedByEDA));
			sampleRawGraph.addEdge(E, B, new EntailmentRelation(E, B, new TEDecisionByScore(0.08), EdgeType.GeneratedByEDA));
			sampleRawGraph.addEdge(C, E, new EntailmentRelation(C, E, new TEDecisionByScore(0.07), EdgeType.GeneratedByEDA));
			sampleRawGraph.addEdge(E, C, new EntailmentRelation(E, C, new TEDecisionByScore(0.11), EdgeType.GeneratedByEDA));
			
			sampleRawGraph.addEdge(E, I, new EntailmentRelation(E, I, new TEDecisionByScore(0.82), EdgeType.GeneratedByEDA));
			sampleRawGraph.addEdge(I, E, new EntailmentRelation(I, E, new TEDecisionByScore(0.74), EdgeType.GeneratedByEDA));

			sampleRawGraph.addEdge(B, I, new EntailmentRelation(B, I, new TEDecisionByScore(0.06), EdgeType.GeneratedByEDA));
			sampleRawGraph.addEdge(I, B, new EntailmentRelation(I, B, new TEDecisionByScore(0.03), EdgeType.GeneratedByEDA));
			sampleRawGraph.addEdge(C, I, new EntailmentRelation(C, I, new TEDecisionByScore(0.09), EdgeType.GeneratedByEDA));
			sampleRawGraph.addEdge(I, C, new EntailmentRelation(I, C, new TEDecisionByScore(0.04), EdgeType.GeneratedByEDA));
								
		}

		return sampleRawGraph;
	}
	
}

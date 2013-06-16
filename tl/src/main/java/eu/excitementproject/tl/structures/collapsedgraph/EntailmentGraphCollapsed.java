package eu.excitementproject.tl.structures.collapsedgraph;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;

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
		
	Set<String> textualInputs = null; //the textual inputs (complete statements), on which the entailment graph was built.
	int numberOfEntailmentUnits; //the number of entailment units contained in the graph. This number is not necessarily the same as the number of nodes in the graph, since each equivalence class node corresponds to one or more entailment unit(s).

	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/

	/**
	 * Default constructor
	 * 
	 * @param arg0 -- edge class
	 *//*
	public EntailmentGraphCollapsed(Class<? extends EntailmentRelationCollapsed> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}
*/
	
	
	/**
	 * Initialize an empty collapsed graph
	 */
	public EntailmentGraphCollapsed(){
		super(EntailmentRelationCollapsed.class);
		numberOfEntailmentUnits = 0;
		textualInputs = new HashSet<String>();
	}

	
	
	/******************************************************************************************
	 * SETTERS/GERRETS
	 * ****************************************************************************************/

	/**
	 * @return the numberOfTextualInputs
	 */
	public int getNumberOfTextualInputs() {
		return this.textualInputs.size();
	}

	/**
	 * @return the numberOfEntailmentUnits
	 */
	public int getNumberOfEntailmentUnits() {
		return numberOfEntailmentUnits;
	}	

	/**
	 * @return the number of equivalence classes (number of nodes in the graph)
	 */
	public int getNumberOfEquivalenceClasses() {
		return this.vertexSet().size();
	}	
	
	
	/******************************************************************************************
	 * OTHER METHODS TO WORK WITH THE GRAPH
	 * ****************************************************************************************/
	
		
	/**This method returns equivalent entailment units for a given input entailment unit text, 
	 * i.e. entailment units, which are in the same equivalence class
	 * @param entailmentUnitText - the canonical text of the entailment unit whose paraphrases are to be found
	 * @return the set of entailment units. If there is no such entailment unit as given in the method’s parameter, the method will return null.
	 */
	public Set<EntailmentUnit> getEquivalentEntailmentUnits(String entailmentUnitText){
		EquivalenceClass vertex = getVertex(entailmentUnitText);
		if (vertex!=null) return vertex.getEntailmentUnits();
		return null;
	}
	
	/**This method returns equivalent entailment units for a given input entailment unit, 
	 * i.e. entailment units, which are in the same equivalence class
	 * @param entailmentUnit - the entailment unit whose paraphrases are to be found
	 * @return the set of entailment units. If there is no such entailment unit as given in the method’s parameter, the method will return null.
	 */	public Set<EntailmentUnit> getEquivalentEntailmentUnits(EntailmentUnit entailmentUnit){		
		return getEquivalentEntailmentUnits(entailmentUnit.getText());
	}
	
	/** Return equivalence class, which includes the input text
	 * @param text
	 * @return the node which has the input text as its label or as the canonical text as any of its entailment units
	 * If such node could not be found - returns null
	 */
	public EquivalenceClass getVertex (String text){
		for (EquivalenceClass vertex : this.vertexSet()){
			if (vertex.getLabel().equals(text)) return vertex;
			for (EntailmentUnit eu : vertex.getEntailmentUnits()){
				if (eu.getText().equals(text)) return vertex;
			}
		}
		return null;
	}
	

	/** Return equivalence class, which includes the input entailment unit
	 * @param eu - the entailment unit
	 * @return the node which includes the input entailment unit
	 * If such node could not be found - returns null
	 */
	public EquivalenceClass getVertex (EntailmentUnit eu){
		for (EquivalenceClass vertex : this.vertexSet()){
			if (vertex.containsEntailmentUnit(eu)) return vertex;
		}
		return null;
	}	

	/** Returns the set of nodes, which entail the given node
	 * @param node whose entailing nodes are returned
	 * @return Set<EquivalenceClass> with all the entailing nodes of the given node
	 */
	public Set<EquivalenceClass> getEntailingNodes(EquivalenceClass node){
		if (!this.containsVertex(node)) return null;
		
		Set<EquivalenceClass> entailingNodes = new HashSet<EquivalenceClass>();
		for (EntailmentRelationCollapsed edge : this.incomingEdgesOf(node)){
			entailingNodes.add(edge.getSource());
		}
		return entailingNodes;
	}
		
	/** Returns the set of nodes, entailed by the given node
	 * @param node whose entailed nodes are returned
	 * @return Set<EquivalenceClass> with all the entailed nodes of the given node
	 */
	public Set<EquivalenceClass> getEntailedNodes(EquivalenceClass node){
		if (!this.containsVertex(node)) return null;

		Set<EquivalenceClass> entailedNodes = new HashSet<EquivalenceClass>();
		for (EntailmentRelationCollapsed edge : this.outgoingEdgesOf(node)){
			entailedNodes.add(edge.getTarget());
		}
		return entailedNodes;
	}
	
	
	/** This method returns equivalence classes containing entailment units entailing the input entailment unit,
	 *  i.e. equivalence classes, for which there is an edge going from this equivalence class 
	 *  to the equivalence class of the input entailment unit. 
	 * @param entailmentUnitText - -- the canonical text of the entailment unit whose entailing equivalence classes are to be found
	 * @return the set of equivalence class nodes. If there are no equivalence classes answering this search, an empty set will be returned. 
	 * If there is no such entailment unit as given in the method’s parameter, the method will return null.
	 */
	public Set<EquivalenceClass> getEntailingEquivalenceClasses(String entailmentUnitText){
		EquivalenceClass vertex = getVertex(entailmentUnitText);
		if (vertex!=null) return this.getEntailingNodes(vertex);
		return null;
	}
	
	/** This method returns equivalence classes containing entailment units entailing the input entailment unit,
	 *  i.e. equivalence classes, for which there is an edge going from this equivalence class 
	 *  to the equivalence class of the input entailment unit. 
	 * @param entailmentUnitText - -- the entailment unit whose entailing equivalence classes are to be found
	 * @return the set of equivalence class nodes. If there are no equivalence classes answering this search, an empty set will be returned. 
	 * If there is no such entailment unit as given in the method’s parameter, the method will return null.
	 */
	public Set<EquivalenceClass> getEntailingEquivalenceClasses(EntailmentUnit entailmentUnit){
		return getEntailingEquivalenceClasses(entailmentUnit.getText());
	}


	/** This method returns equivalence classes containing entailment units entailed by the input entailment unit,
	 * i.e. equivalence classes for which there is an edge going to this equivalence class
	 * from the equivalence class of the input entailment unit.    
	 * @param entailmentUnitText - the canonical text of the entailment unit whose entailed equivalence classes are to be found
	 * @return the set of equivalence class nodes. If there are no equivalence classes answering this search, an empty set will be returned. 
	 * If there is no such entailment unit as given in the method’s parameter, the method will return null.
	 */
	public Set<EquivalenceClass> getEntailedEquivalenceClasses(String entailmentUnitText){
		EquivalenceClass vertex = getVertex(entailmentUnitText);
		if (vertex!=null) return this.getEntailingNodes(vertex);
		return null;		
	}
	
	/** This method returns equivalence classes containing entailment units entailed by the input entailment unit,
	 * i.e. equivalence classes for which there is an edge going to this equivalence class
	 * from the equivalence class of the input entailment unit.    
	 * @param entailmentUnitText - the entailment unit whose entailed equivalence classes are to be found
	 * @return the set of equivalence class nodes. If there are no equivalence classes answering this search, an empty set will be returned. 
	 * If there is no such entailment unit as given in the method’s parameter, the method will return null.
	 */	public Set<EquivalenceClass> getEntailedEquivalenceClasses(EntailmentUnit entailmentUnit){
		return getEntailedEquivalenceClasses(entailmentUnit.getText());
	}
	 
	 
	 /** the method and returns a subgraph with all nodes containing the input entailment unit, 
	  * as well as all nodes directly connected to one of these nodes,
	  * i.e., all equivalent, entailed or entailing entailment units.
	 * @param entailmentUnitText - canonical text of the entailment unit whose subgraph should be returned
	 * @return the required subgraph. 
	 * If there are no nodes connected to the given node, empty graph will be returned (graph with no nodes and no edges).
	 * If there is no such entailment unit as given in the method’s parameter, the method will return null.
	 */
	public EntailmentGraphCollapsed getSubgraphFor(String entailmentUnitText){
		 // find the vertex with the given entailmentUnitText
		 EquivalenceClass vertex = getVertex(entailmentUnitText);
		 if (vertex == null) return null;
		 
		 EntailmentGraphCollapsed subgraph = new EntailmentGraphCollapsed();
		 // copy to subgraph all the edges (with their nodes), which touch the vertex we found
		 for (EntailmentRelationCollapsed edge : this.edgesOf(vertex)){
			 subgraph.addEdgeWithNodes(edge.getSource(), edge.getTarget(), edge);
		 }
		 return subgraph;
	 }
	 
	 /** the method and returns a subgraph with all nodes containing the input entailment unit, 
	  * as well as all nodes directly connected to one of these nodes,
	  * i.e., all equivalent, entailed or entailing entailment units.
	 * @param entailmentUnitText - the entailment unit whose subgraph should be returned
	 * @return the required subgraph. 
	 * If there are no nodes connected to the given node, empty graph will be returned (graph with no nodes and no edges).
	 * If there is no such entailment unit as given in the method’s parameter, the method will return null.
	 */public EntailmentGraphCollapsed getSubgraphFor(EntailmentUnit  entailmentUnit){
		 return getSubgraphFor(entailmentUnit.getText());
	 }

	 /** Adds the given edge (and if needed - its nodes) to the graph. 
	  * If source or target node are not present in the graph - they will be added.
	  * If an edge source -> target is already present in the graph, the edge will not be added (collapsed graph is not a multi-graph).
	 * @param source
	 * @param target
	 * @param edge
	 */
	public void addEdgeWithNodes(EquivalenceClass source, EquivalenceClass target, EntailmentRelationCollapsed edge){
		 if(!this.containsVertex(source)) this.addVertex(source);
		 if(!this.containsVertex(target)) this.addVertex(target);

		 if (!this.containsEdge(source,target)){ // if already contains an edge - don't add, since this is not a multi-graph!
			 this.addEdge(source, target, edge);
		 }
		 	 
	 }

	
	/** Returns top-X nodes sorted by number of interactions
	 * @param X
	 * @return
	 */
	public List<EquivalenceClass> sortNodesByNumberOfInteractions(int X){
		if (X > this.vertexSet().size()) X = this.vertexSet().size(); // cannot return more nodes than we have in the graph
				
		List<EquivalenceClass> sortedNodes = new LinkedList<EquivalenceClass>();
		sortedNodes.addAll(this.vertexSet());
		Collections.sort(sortedNodes, new DescendingNumberOfInteractionsComparator());
		sortedNodes.subList(X, sortedNodes.size()).clear(); //remove all the elements with index starting at X (incusive)
		return sortedNodes;
	}
	
	public Set<String> getRelevantInteractionIDs(String entailmentUnitText){
		return getRelevantInteractionIDs(this.getVertex(entailmentUnitText));		
	}
	
	public Set<String> getRelevantInteractionIDs(EntailmentUnit entailmentUnit){
		return getRelevantInteractionIDs(this.getVertex(entailmentUnit));
	}
	
	public Set<String> getRelevantInteractionIDs(EquivalenceClass node){
		if (!this.containsVertex(node)) return null;
		return node.getInteractionIds();
	}
	
	
/*	*//**
	 * Converts an input work graph to a format that would be useful to the end users
	 * This might mean changing the nodes from complex annotated objects to sets of strings
	 * and compressing multiple edges into one
	 * @param <WV>
	 * @param <WE>
	 * 
	 * @param wg
	 *//*
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
	}*/	

	/******************************************************************************************
	 * PRINT GRAPH
	 * ****************************************************************************************/
	
	@Override
	public String toString(){
		String s = "The graph is built based on "+ textualInputs.size()+" textual inputs (complete statements) and contains "+numberOfEntailmentUnits+" entailment units";
		s+="\nNODES:";
		for (EquivalenceClass v: this.vertexSet()){
			s+="\n"+v.toString();
		}
		
		s+="\n\nEDGES:";
		for (EntailmentRelationCollapsed e: this.edgeSet()){
			s+="\n\t"+e.toString();
		}		
		return s;
	}
	
	
	public class DescendingNumberOfInteractionsComparator implements Comparator<EquivalenceClass> {
	    @Override
	    public int compare(EquivalenceClass nodeA, EquivalenceClass nodeB) {
	        return -1*Integer.compare(nodeA.getInteractionIds().size(),nodeB.getInteractionIds().size());
	    }
	}
	
	public boolean addVertex(EquivalenceClass v){
		boolean added = super.addVertex(v);
		if (added){
			for (EntailmentUnit eu : v.getEntailmentUnits()){
				numberOfEntailmentUnits++;
				textualInputs.addAll(eu.getCompleteStatementTexts());
			}						
		}
		return added;
	}
}

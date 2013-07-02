package eu.excitementproject.tl.structures.collapsedgraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * 
 * @author vivi@fbk & Lili Kotlerman
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
	 * Initialize an empty collapsed graph
	 */
	public EntailmentGraphCollapsed(){
		super(EntailmentRelationCollapsed.class);
		numberOfEntailmentUnits = 0;
		textualInputs = new HashSet<String>();
	}

	
	/**
	 * 
	 * @param xmlFile -- a file (possibly xml) from which to load a previously produced graph
	 */
	public EntailmentGraphCollapsed(File xmlFile) throws EntailmentGraphCollapsedException{
		super(EntailmentRelationCollapsed.class);
		numberOfEntailmentUnits = 0;
		textualInputs = new HashSet<String>();
    	try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
    
			doc.getDocumentElement().normalize();	     
			doc.getDocumentElement().getNodeName();
			
			NodeList equivalenceClassList = doc.getElementsByTagName("equivalenceClassNode");
			// create and add nodes
			for (int nodeNumber = 0; nodeNumber < equivalenceClassList.getLength(); nodeNumber++) {  
				Node eqClassNode = equivalenceClassList.item(nodeNumber);     
				eqClassNode.getNodeName();  
				if (eqClassNode.getNodeType() == Node.ELEMENT_NODE) { 
					// read the label of the node
					Element eqClassElement = (Element) eqClassNode;
					String label = eqClassElement.getAttribute("label");
				
					// create the set of entailment units of the node
					NodeList entailmentUnitList = eqClassNode.getChildNodes();
					Set<EntailmentUnit> s_eu = new HashSet<EntailmentUnit>();
					
					for (int temp = 0; temp < entailmentUnitList.getLength(); temp++) {    
						Node eu = entailmentUnitList.item(temp);     
						if (!eu.getNodeName().equals("entailmentUnit")) continue;   

						Element euElement = (Element) eu;
						String text = euElement.getAttribute("text");
						Integer level = Integer.valueOf(euElement.getAttribute("level"));

						Set<String> completeStatementTexts = new HashSet<String>();
						Set<EntailmentUnitMention> mentions = new HashSet<EntailmentUnitMention>();
						Set<String> interactionIds = new HashSet<String>();
						
						NodeList childNodes = eu.getChildNodes();
				       	for (int i = 0; i < childNodes.getLength(); i++) {    
				       		Node child = childNodes.item(i);
				       		if (child.getNodeName().equals("completeStatement")){
					       		Element csElement = (Element) child;
					       		String cstext = csElement.getAttribute("text");
				       			completeStatementTexts.add(cstext);
				       		}
				       		
				       		if (child.getNodeName().equals("interactionId")){
				       			Element idElement = (Element) child;
				       			String id = idElement.getAttribute("id");
				       			interactionIds.add(id);
				       		}
				       		
				       		if (child.getNodeName().equals("entailmentUnitMention")){
					       		Element eumElement = (Element) child;
					       		int eumLevel = Integer.valueOf(eumElement.getAttribute("level"));
					       		EntailmentUnitMention m = new EntailmentUnitMention(eumElement.getAttribute("text"), eumLevel);
					       		m.setCategoryId(eumElement.getAttribute("categoryId"));
					       		mentions.add(m);	       			
				       		}
						}		
				       	EntailmentUnit newEntailmentUnit = new EntailmentUnit(text, completeStatementTexts, mentions, interactionIds, level);
					    s_eu.add(newEntailmentUnit);
					} // done creating s_eu
					// create and add a new node to the graph
					this.addVertex(new EquivalenceClass(label, s_eu));					
				}							
			}
			
			// create and add edges
			NodeList edgeList = doc.getElementsByTagName("entailmentRelationCollapsedEdge");
			for (int temp = 0; temp < edgeList.getLength(); temp++) {    
				Node er = edgeList.item(temp);     
				er.getNodeName();     
				if (er.getNodeType() == Node.ELEMENT_NODE) {  
					
					Element erElement = (Element) er;
					String source = erElement.getAttribute("source");
					String target = erElement.getAttribute("target");
					double confidence = Double.valueOf(erElement.getAttribute("confidence"));
					
					EquivalenceClass sourceVertex = this.getVertex(source);
					EquivalenceClass targetVertex = this.getVertex(target);
					EntailmentRelationCollapsed e = new EntailmentRelationCollapsed(sourceVertex, targetVertex, confidence);
					this.addEdge(sourceVertex, targetVertex, e);
				}
			}
		} catch (DOMException | ParserConfigurationException | SAXException | IOException e) {
			throw new EntailmentGraphCollapsedException("Could not load collapsed graph from " + xmlFile.getAbsolutePath()+"\n"+e.getMessage());
		}    	
	}	
	
	/******************************************************************************************
	 * METHODS REQUIRED BY INDUSTRIAL SCENARIOS
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
		if (vertex!=null) return this.getEntailedNodes(vertex);
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
	 
	 
	 /** The method returns a subgraph with all nodes containing the input entailment unit, 
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
	 
	 /** The method returns a subgraph with all nodes containing the input entailment unit, 
	  * as well as all nodes directly connected to one of these nodes,
	  * i.e., all equivalent, entailed or entailing entailment units.
	 * @param entailmentUnitText - the entailment unit whose subgraph should be returned
	 * @return the required subgraph. 
	 * If there are no nodes connected to the given node, empty graph will be returned (graph with no nodes and no edges).
	 * If there is no such entailment unit as given in the method’s parameter, the method will return null.
	 */public EntailmentGraphCollapsed getSubgraphFor(EntailmentUnit  entailmentUnit){
		 return getSubgraphFor(entailmentUnit.getText());
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
	
	/** The  method  returns the ids of interactions that contain entailment units equivalent to the input  
	 * entailment unit based on the entailment graph.
	 * If the input entailment unit is not found under any of the nodes in the graph, the method will return null.
	 * @param entailmentUnitText - the canonical text of the input entailment unit
	 * @return set of interaction ids
	 */
	public Set<String> getRelevantInteractionIDs(String entailmentUnitText){
		return getRelevantInteractionIDs(this.getVertex(entailmentUnitText));		
	}
	
	/** The  method  returns the ids of interactions that contain entailment units equivalent to the input  
	 * entailment unit based on the entailment graph.
	 * If the input entailment unit is not found under any of the nodes in the graph, the method will return null.
	 * @param entailmentUnit - the input entailment unit
	 * @return set of interaction ids
	 */
	public Set<String> getRelevantInteractionIDs(EntailmentUnit entailmentUnit){
		return getRelevantInteractionIDs(this.getVertex(entailmentUnit));
	}
	
	/** The  method  returns the ids of interactions that contain entailment units covered by the input equivalence class
	 * If such equivalence class node is not found in the graph, the method will return null.
	 * @param node - the input equivalence class node 
	 * @return set of interaction ids
	 */
	public Set<String> getRelevantInteractionIDs(EquivalenceClass node){
		if (!this.containsVertex(node)) return null;
		return node.getInteractionIds();
	}
		

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
			
	/** Generates a single string, which contains the graph in DOT format for visualization
	 * @return the generated string
	 */
	public String toDOT(){
		String s = "digraph collapsedGraph {\n";
		for (EntailmentRelationCollapsed edge : this.edgeSet()){
			s+=edge.toDOT();
		}
		s+="}";	
		return s;
	}
	
	/** Saves the graph in DOT format to the given file. If such file already exists, it will be overwritten.
	 * @param filename - the name of the file to save the graph
	 * @throws EntailmentGraphRawException if the method did not manage to save the graph (e.g. if the folder specified in the filename does not exist)
	 */
	public void toDOT(String filename) throws IOException{
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write(this.toDOT());
			out.close();
	}	
	
	public void toXML(String filename) throws EntailmentGraphCollapsedException{
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("collapsedGraph");
			doc.appendChild(rootElement);
 
			// add nodes
			for (EquivalenceClass ec : this.vertexSet()){
				// staff elements
				Element equivalenceClassNode = doc.createElement("equivalenceClassNode");
				rootElement.appendChild(equivalenceClassNode);
 
				// set label attribute to eu element
				equivalenceClassNode.setAttribute("label",ec.getLabel());
 
				/*	protected Set<String> completeStatementTexts;					*/
				for (EntailmentUnit eu : ec.getEntailmentUnits()){
					// EntailmentUnit elements
					Element entailmentUnit = doc.createElement("entailmentUnit");
					// set text attribute to eu element
					entailmentUnit.setAttribute("text",eu.getText());
					// set level attribute to eu element
					entailmentUnit.setAttribute("level",String.valueOf(eu.getLevel()));

					/*	protected Set<String> completeStatementTexts;					*/
					for (String csText : eu.getCompleteStatementTexts()){
						// completeStatementText elements
						Element completeStatementText = doc.createElement("completeStatement");
						completeStatementText.setAttribute("text",csText);
						entailmentUnit.appendChild(completeStatementText);						
					}

					/*	protected Set<EntailmentUnitMention> mentions = null;										*/
					for (EntailmentUnitMention eum : eu.getMentions()){
						// eu mentions elements
						Element eumention = doc.createElement("entailmentUnitMention");
						eumention.setAttribute("text",eum.getText());
						eumention.setAttribute("categoryId",eum.getCategoryId());
						eumention.setAttribute("level",String.valueOf(eum.getLevel()));						
						entailmentUnit.appendChild(eumention);						
					}

					/*	protected Set<String> interactionIds = null;										*/
					for (String interactionId : eu.getInteractionIds()){
						// completeStatementText elements
						Element interaction = doc.createElement("interactionId");
						interaction.setAttribute("id",interactionId);
						entailmentUnit.appendChild(interaction);						
					}					
					equivalenceClassNode.appendChild(entailmentUnit);						
				}
			}
 
			// add edges
			for (EntailmentRelationCollapsed r  : this.edgeSet()){
				// staff elements
				Element entailmentrelationEdge = doc.createElement("entailmentRelationCollapsedEdge");
				rootElement.appendChild(entailmentrelationEdge);
 
				// set source attribute to eu element
				entailmentrelationEdge.setAttribute("source",r.getSource().getLabel());
				// set target attribute to eu element
				entailmentrelationEdge.setAttribute("target",r.getTarget().getLabel());
				// set confidence attribute to eu element
				entailmentrelationEdge.setAttribute("confidence",String.valueOf(r.getConfidence()));
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filename));
 
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
 
			transformer.transform(source, result);
		} catch (DOMException | ParserConfigurationException | TransformerFactoryConfigurationError |
				TransformerException e) {
			// TODO Auto-generated catch block
			throw new EntailmentGraphCollapsedException(e.getMessage());
		}		 
  }
	

	/******************************************************************************************
	 * OTHER AUXILIARY METHODS
	 * ****************************************************************************************/
	
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

	/**
	 * Comparator to sort equivalence classes in descending order by their number of interactions
	 */
	public class DescendingNumberOfInteractionsComparator implements Comparator<EquivalenceClass> {
	    @Override
	    public int compare(EquivalenceClass nodeA, EquivalenceClass nodeB) {
	        return -1*Integer.compare(nodeA.getInteractionIds().size(),nodeB.getInteractionIds().size());
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.jgrapht.graph.AbstractBaseGraph#addVertex(java.lang.Object)
	 * Overrides the addVertex method of AbstractBaseGraph. The method adds the given vertex and updates the  
	 * numberOfEntailmentUnits and textualInputs attributes. 
	 */
	@Override
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

	/******************************************************************************************
	 * LEGACY
	 * ****************************************************************************************/

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


}

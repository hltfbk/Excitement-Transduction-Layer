package eu.excitementproject.tl.structures.rawgraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DirectedMultigraph;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphEdge;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.rawgraph.utils.TEDecisionByScore;
import eu.excitementproject.tl.structures.rawgraph.utils.TEDecisionWithConfidence;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;


/**
 * 
 * @author vivi@fbk & Lili Kotlerman
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
	Logger logger = Logger.getLogger("eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw");

	
	private static final long serialVersionUID = -3274655854206417667L;
	/*
	 * To build the work graph we need to know the configuration,
	 * and in particular the EDA and LAP to use (and possibly other stuff)
	 */
	
	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/

	/*
	 * a constructor for initializing a graph from a (xml) file
	 */
	/**
	 * 
	 * @param xmlFile -- an xml file from which to load a previously produced graph
	 */
	public EntailmentGraphRaw(File xmlFile) throws EntailmentGraphRawException{
		super(EntailmentRelation.class);
    	try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
    
			doc.getDocumentElement().normalize();	     
			doc.getDocumentElement().getNodeName();
			NodeList entailmentUnitList = doc.getElementsByTagName("entailmentUnitNode");
			
			// create and add nodes
			for (int temp = 0; temp < entailmentUnitList.getLength(); temp++) {    
				Node eu = entailmentUnitList.item(temp);     
				eu.getNodeName();   

				Element euElement = (Element) eu;
				String text = euElement.getAttribute("text");
				Integer level = Integer.valueOf(euElement.getAttribute("level"));

				Set<String> completeStatementTexts = new HashSet<String>();
				Set<EntailmentUnitMention> mentions = new HashSet<EntailmentUnitMention>();
				
				NodeList childNodes = eu.getChildNodes();
		       	for (int i = 0; i < childNodes.getLength(); i++) {    
		       		Node child = childNodes.item(i);
		       		if (child.getNodeName().equals("completeStatement")){
			       		Element csElement = (Element) child;
			       		String cstext = csElement.getAttribute("text");
		       			completeStatementTexts.add(cstext);
		       		}
		       				       		
		       		if (child.getNodeName().equals("entailmentUnitMention")){
			       		Element eumElement = (Element) child;
			       		int eumLevel = Integer.valueOf(eumElement.getAttribute("level"));
			       		EntailmentUnitMention m = new EntailmentUnitMention(eumElement.getAttribute("text"), eumLevel, eumElement.getAttribute("interactionId"));
			       		m.setCategoryId(eumElement.getAttribute("categoryId"));
			       		mentions.add(m);	       			
		       		}
				}

							       
			    this.addVertex(new EntailmentUnit(text, completeStatementTexts, mentions, level));
			}
			
			
			// create and add edges
			NodeList entailmentRelationList = doc.getElementsByTagName("entailmentRelationEdge");
			for (int temp = 0; temp < entailmentRelationList.getLength(); temp++) {    
				Node er = entailmentRelationList.item(temp);     
				er.getNodeName();     
				if (er.getNodeType() == Node.ELEMENT_NODE) {  
					
					Element erElement = (Element) er;
					String source = erElement.getAttribute("source");
					String target = erElement.getAttribute("target");
					EdgeType edgeType = EdgeType.convert(erElement.getAttribute("type"));
					TEDecision edge = new TEDecisionWithConfidence(Double.valueOf(erElement.getAttribute("confidence")), DecisionLabel.getLabelFor(erElement.getAttribute("decisionLabel")));
					
					EntailmentUnit sourceVertex = this.getVertexWithText(source);
					EntailmentUnit targetVertex = this.getVertexWithText(target);
					EntailmentRelation e = new EntailmentRelation(sourceVertex, targetVertex, edge, edgeType);
					this.addEdge(sourceVertex, targetVertex, e);
				}
			}
		} catch (DOMException | ParserConfigurationException | SAXException
				| IOException | EDAException e) {
			throw new EntailmentGraphRawException("Could not load collapsed graph from " + xmlFile.getAbsolutePath()+"\n"+e.getMessage());
		}

    	
	}
	
	/**
	 * Initialize a work graph from a fragment graph
	 * @param fg -- a fragment graph object
	 */
	public EntailmentGraphRaw(FragmentGraph fg) {
		super(EntailmentRelation.class);
		copyFragmentGraphNodesAndEdges(fg);
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
	 * OTHER AUXILIARY METHODS
	 * ****************************************************************************************/

	public boolean isEntailment(EntailmentUnit entailingNode, EntailmentUnit entailedNode){
		if (getEdge(entailingNode, entailedNode)!=null) return true;
		return false;
	}

	public boolean isEntailmentInAnyDirection(EntailmentUnit nodeA, EntailmentUnit nodeB){
		if (nodeA.equals(nodeB)) return true; // if both nodes are the same
		if (isEntailment(nodeA, nodeB)||(isEntailment(nodeB, nodeA))) return true;
		return false;
	}

	/** Copies all nodes and edges from the input fragment graph to the raw entailment graph
	 * @param fg - the inout fragment graph
	 */
	public void copyFragmentGraphNodesAndEdges(FragmentGraph fg){
		// copy nodes (add if new, update mentions if exist) - need to do this separately from edges, since there might be "orphan" nodes (this should only happen when the fragment graph has a single node, i.e. base statement = complete statement)
		for(EntailmentUnitMention fragmentGraphNode : fg.vertexSet()){
			this.addEntailmentUnitMention(fragmentGraphNode, fg.getCompleteStatement().getText());
			}
		// add edges
		for (FragmentGraphEdge fragmentGraphEdge : fg.edgeSet()){
			this.addEdgeFromFragmentGraph(fragmentGraphEdge, fg);
		}
	}
	
	/** The method gets an EntailmentUnitMention and either adds a new EntailmentUnit node or, if a relevant EntailmentUnit already exists in the graph, updates the list of its mentions  
	 * @param mention - the EntailmentUnitMention to be added to the graph
	 * @param completeStatementText - the text of the mention's complete statement
	 */
	public void addEntailmentUnitMention(EntailmentUnitMention mention, String completeStatementText){
		EntailmentUnit node = this.getVertexWithText(mention.getText());
		if (node==null) {
			EntailmentUnit newNode = new EntailmentUnit(mention, completeStatementText);
			this.addVertex(newNode);
		}
		else{
			node.addMention(mention, completeStatementText);
		}
	}
	
	/** Given a base statement node and a complete statement text, the method finds and returns all the nodes from the corresponding fragment graph
	 * (fragment graph can be defined by its complete statement, while the same base statement can be part of different fragment graphs)  
	 * @param baseStatementNode
	 * @param completeStatementText
	 * @return Hashtable where keys (int) are levels and values (Set<EntailmentUnit>) are sets of entailment unit nodes found at the corresponding level.
	 * If the input base statement was not produced from the input complete statement, empty Hashtable will be returned  
	 * @throws EntailmentGraphRawException
	 */
	public Hashtable<Integer, Set<EntailmentUnit>> getFragmentGraphNodes(EntailmentUnit baseStatementNode, String completeStatementText) throws EntailmentGraphRawException {
		Hashtable<Integer, Set<EntailmentUnit>> nodesByLevel = new Hashtable<Integer, Set<EntailmentUnit>>(); 
		
// /*		
		System.out.println("----");
		System.out.println(baseStatementNode);
		System.out.println(completeStatementText);
		System.out.println(baseStatementNode.completeStatementTexts);
// */
		if (!baseStatementNode.completeStatementTexts.contains(completeStatementText)) throw new EntailmentGraphRawException("Base statement node \""+baseStatementNode.getText()+"\" does not correspond to the complete statement \""+ completeStatementText+"\"\n");
		
		EntailmentUnit completeStatementNode = getVertexWithText(completeStatementText);
		if (completeStatementNode==null) throw new EntailmentGraphRawException("The raw graph does not contain a node \""+completeStatementText+"\"\n");
		
		Set<EntailmentUnit> nodes = getAllNodes(completeStatementNode, baseStatementNode, new HashSet<EntailmentUnit>());
		for(EntailmentUnit node: nodes){
			if (node.isPartOfFragmentGraph(completeStatementText)){
				Integer level = node.getLevel();
				if (!nodesByLevel.containsKey(level)) nodesByLevel.put(level, new HashSet<EntailmentUnit>());
				if (!nodesByLevel.get(level).contains(node)) nodesByLevel.get(level).add(node);
			}
		}		
		return nodesByLevel;
	}
	
	
	/** Returns the set of all nodes, which form the possible paths from sourceNode to targetNode (including the sourceNode and the targetNode).
	 * If targetNode is the same as sourceNode, the method will return a set with a single EntailmentUnit. 
	 * The method recursively updates the set nodesToReturn, which it obtains as its parameter.
	 * @param sourceNode
	 * @param targetNode
	 * @param nodesToReturn
	 * @return
	 */
	public Set<EntailmentUnit> getAllNodes(EntailmentUnit sourceNode, EntailmentUnit targetNode, Set<EntailmentUnit> nodesToReturn){
		if (nodesToReturn==null) nodesToReturn = new HashSet<EntailmentUnit>();
		
		nodesToReturn.add(sourceNode);
		if (targetNode.equals(sourceNode)) return nodesToReturn;
		
		Set<EntailmentUnit> newNodes = this.getEntailedNodes(sourceNode);
		for (EntailmentUnit newNode : newNodes){
			if (!nodesToReturn.contains(newNode)) {
				nodesToReturn=getAllNodes(newNode,targetNode,nodesToReturn);
			}
		}
		return nodesToReturn;
	}
	
	
	/** Returns the set of nodes, which entail the given node
	 * @param node whose entailing nodes are returned
	 * @return Set<EntailmentUnit> with all the entailing nodes of the given node
	 */
	public Set<EntailmentUnit> getEntailingNodes(EntailmentUnit node){
		if (!this.containsVertex(node)) return null;

		Set<EntailmentUnit> entailingNodes = new HashSet<EntailmentUnit>();
		for (EntailmentRelation edge : this.incomingEdgesOf(node)){
			if(edge.getTEdecision().getDecision().is(DecisionLabel.Entailment)){ // only add the node to the list if the edge is an "entailment" edge
				entailingNodes.add(edge.getSource());				
			}
		}
		return entailingNodes;
	}
	
	/** Returns the set of nodes, which entail the given node and have the specified level (number of modifiers)
	 * @param node whose entailing nodes are returned
	 * @param level - number of modifiers
	 * @return Set<EntailmentUnit> with the entailing nodes of the given node
	 */
	public Set<EntailmentUnit> getEntailingNodes(EntailmentUnit node, int level){
		if (!this.containsVertex(node)) return null;

		Set<EntailmentUnit> entailingNodes = new HashSet<EntailmentUnit>();
		for (EntailmentRelation edge : this.incomingEdgesOf(node)){
			if(edge.getTEdecision().getDecision().is(DecisionLabel.Entailment)){ // only add the node to the list if the edge is an "entailment" edge
				EntailmentUnit entailingNode = edge.getSource();
				if (entailingNode.getLevel()==level) entailingNodes.add(entailingNode);				
			}
		}
		return entailingNodes;
	}
	
	/** Returns the set of nodes, entailed by the given node
	 * @param node whose entailed nodes are returned
	 * @return Set<EntailmentUnit> with all the entailed nodes of the given node
	 */
	public Set<EntailmentUnit> getEntailedNodes(EntailmentUnit node){
		if (!this.containsVertex(node)) return null;

		Set<EntailmentUnit> entailedNodes = new HashSet<EntailmentUnit>();
		for (EntailmentRelation edge : this.outgoingEdgesOf(node)){
			if(edge.getTEdecision().getDecision().is(DecisionLabel.Entailment)){
				entailedNodes.add(edge.getTarget());	// only add the node to the list if the edge is an "entailment" edge			
			}
		}
		return entailedNodes;
	}
	
	/** Returns the set of nodes, which are entailed by the given node and have the specified level (number of modifiers)
	 * @param node whose entailed nodes are returned
	 * @param level - number of modifiers
	 * @return Set<EntailmentUnit> with all the entailed nodes of the given node
	 */
	public Set<EntailmentUnit> getEntailedNodes(EntailmentUnit node, int level){
		if (!this.containsVertex(node)) return null;

		Set<EntailmentUnit> entailedNodes = new HashSet<EntailmentUnit>();
		for (EntailmentRelation edge : this.outgoingEdgesOf(node)){
			if(edge.getTEdecision().getDecision().is(DecisionLabel.Entailment)){
				// only add the node to the list if the edge is an "entailment" edge
				EntailmentUnit entailedNode = edge.getTarget();
				if (entailedNode.getLevel()==level) entailedNodes.add(entailedNode);				
			}

		}
		return entailedNodes;
	}
	

	/** Get entailed nodes what belong to the same fragment graph and have the specified nummber of modifiers (level)
	 * @param node
	 * @return
	 */
	public Set<EntailmentUnit> getEntailedNodesFromSameFragmentGraph(EntailmentUnit node, int level){
		if (!this.containsVertex(node)) return null;

		Set<EntailmentUnit> entailedNodes = new HashSet<EntailmentUnit>();
		for (EntailmentRelation edge : this.outgoingEdgesOf(node)){
			EntailmentUnit entailedNode = edge.getTarget();
			if (entailedNode.getLevel()==level){ // first check the level condition
				for (String completeStatement : entailedNode.getCompleteStatementTexts()){ // now check if have a common completeStatement (came from the same gragment graph)
					if (node.getCompleteStatementTexts().contains(completeStatement)){
						entailedNodes.add(entailedNode); // if yes - add
						break; // and go to the next edge
					}
				}
				
			}
		}
		return entailedNodes;
	}	

	/**
	 * (vivi@fbk: added the lap parameter)
	 * 
	 * Create an edge from sourceVertex to targetVertex using the specified eda 
	 * @param sourceVertex
	 * @param targetVertex
	 * @param eda
	 * @return the edge, which was added to the graph
	 * @throws LAPException 
	 */
	public EntailmentRelation addEdgeFromEDA(EntailmentUnit sourceVertex, EntailmentUnit targetVertex, EDABasic<?> eda, CachedLAPAccess lap) throws EntailmentGraphRawException{
		EntailmentRelation edge = new EntailmentRelation(sourceVertex, targetVertex, eda, lap);
		this.addEdge(sourceVertex, targetVertex, edge);
		return edge;
	}
	
	/**
	 * Copy an edge from a FragmentGraph. 
	 * Although the method is called from copyFragmentGraphNodesAndEdges(), where nodes are added before adding edges, for generality if vertices do not exist - add them, and if a vertex exists - add the corresponding new entailment unit mention.
	 * Since the mentions, their complete statements and interaction ids are sets, there will be no duplicate mentions etc. added 
	 * @param fragmentGraphEdge -- the edge to copy into the graph
	 * @return the edge, which was added to the graph
	 * TODO: how to deal with the original edge weight? Currently copied as is (=1 for everyone).
	 */
	public EntailmentRelation addEdgeFromFragmentGraph(FragmentGraphEdge fragmentGraphEdge, FragmentGraph fg){
		// take care of the source and target vertices 
		this.addEntailmentUnitMention(fragmentGraphEdge.getSource(), fg.getCompleteStatement().getText());
		this.addEntailmentUnitMention(fragmentGraphEdge.getTarget(), fg.getCompleteStatement().getText());

		// now create and add the edge
		EntailmentRelation edge = new EntailmentRelation(this.getVertexWithText(fragmentGraphEdge.getSource().getText()), this.getVertexWithText(fragmentGraphEdge.getTarget().getText()), new TEDecisionByScore(fragmentGraphEdge.getWeight()), EdgeType.FRAGMENT_GRAPH);
		this.addEdge(this.getVertexWithText(fragmentGraphEdge.getSource().getText()), this.getVertexWithText(fragmentGraphEdge.getTarget().getText()), edge);
		return edge;
	}
	
	
	
	/**
	 * Create an edge induced from prior knowledge. Confidence is to be given as parameter.
	 * The vertices are assumed to be present in the graph
	 * @param sourceVertex
	 * @param targetVertex
	 * @param confidence
	 * @return the edge, which was added to the graph
	 */
	public EntailmentRelation addEdgeByInduction(EntailmentUnit sourceVertex, EntailmentUnit targetVertex, DecisionLabel entailmentDesicion, Double confidence){
		EntailmentRelation edge = new EntailmentRelation(sourceVertex, targetVertex, new TEDecisionWithConfidence(confidence,entailmentDesicion), EdgeType.INDUCED);
		this.addEdge(sourceVertex, targetVertex, edge);
		return edge;
	}
	
	/**
	 * Return the vertex (EntailmentUnit) with the corresponding text, if it is found in the graph. 
	 * Otherwise return null.
	 * @param text the text of the EntailmentUnit to be found
	 * @return
	 */
/*	public EntailmentUnit getVertex(String text){
		for (EntailmentUnit eu : this.vertexSet()){
			if (eu.getText().equals(text)) return eu;
		}
		return null;
	}*/
	
	
	/**
	 * Return the vertex (EntailmentUnit), which has the given text at one of its mentions, if it is found in the graph. 
	 * Otherwise return null.
	 * The case of the text is ignored (to unify texts regardless to their case)
	 * @param text the text of the EntailmentUnit to be found
	 * @return
	 */
	public EntailmentUnit getVertexWithText(String text){
		for (EntailmentUnit eu : this.vertexSet()){
			if (eu.isTextIncludedOrRelevant(text)) return eu;
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
		
		s+="\n\nEDGES:";
		for (EntailmentRelation e: this.edgeSet()){
			s+="\n\t"+e.toString();
		}
		
		return s;
	}
	
	/** Generates a single string, which contains the graph in DOT format for visualization
	 * @return the generated string
	 */
	public String toDOT(){
		String s = "digraph rawGraph {\n";
		for (EntailmentUnit node : this.vertexSet()){
			s+="\""+node.getText()+"\";";
		}
		for (EntailmentRelation edge : this.edgeSet()){
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
	
	
	public DOMSource toXML() throws EntailmentGraphRawException{
				try {
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
					// root elements
					Document doc = docBuilder.newDocument();
					Element rootElement = doc.createElement("rawGraph");
					doc.appendChild(rootElement);
 
					// add nodes
					for (EntailmentUnit eu : this.vertexSet()){
						// EntailmentUnit elements
						Element entailmentUnitNode = doc.createElement("entailmentUnitNode");
						rootElement.appendChild(entailmentUnitNode);
 
						// set text attribute to eu element
						entailmentUnitNode.setAttribute("text",eu.getText());
						// set level attribute to eu element
						entailmentUnitNode.setAttribute("level",String.valueOf(eu.getLevel()));
 
						/*	protected Set<String> completeStatementTexts;					*/
						for (String csText : eu.getCompleteStatementTexts()){
							// completeStatementText elements
							Element completeStatementText = doc.createElement("completeStatement");
							completeStatementText.setAttribute("text",csText);
							entailmentUnitNode.appendChild(completeStatementText);						
						}

						/*	protected Set<EntailmentUnitMention> mentions = null;										*/
						for (EntailmentUnitMention eum : eu.getMentions()){
							// eu mentions elements
							Element eumention = doc.createElement("entailmentUnitMention");
							eumention.setAttribute("text",eum.getText());
							eumention.setAttribute("categoryId",eum.getCategoryId());
							eumention.setAttribute("interactionId",eum.getInteractionId());
							eumention.setAttribute("level",String.valueOf(eum.getLevel()));						
							entailmentUnitNode.appendChild(eumention);						
						}

					/*		protected Set<String> interactionIds = null;										
						for (String interactionId : eu.getInteractionIds()){
							// completeStatementText elements
							Element interaction = doc.createElement("interactionId");
							interaction.setAttribute("id",interactionId);
							entailmentUnitNode.appendChild(interaction);						
						}				*/	
					}
 
					// add edges
					for (EntailmentRelation r  : this.edgeSet()){
						// staff elements
						Element entailmentrelationEdge = doc.createElement("entailmentRelationEdge");
						rootElement.appendChild(entailmentrelationEdge);
 
						// set source attribute to eu element
						entailmentrelationEdge.setAttribute("source",r.getSource().getText());
						// set target attribute to eu element
						entailmentrelationEdge.setAttribute("target",r.getTarget().getText());
						// set confidence attribute to eu element
						entailmentrelationEdge.setAttribute("confidence",String.valueOf(r.getConfidence()));
						// set edgeType attribute to eu element
						entailmentrelationEdge.setAttribute("type",r.getEdgeType().toString());
						// set label attribute to eu element
						entailmentrelationEdge.setAttribute("decisionLabel",r.getLabel().toString());
						// set eda attribute to eu element
						if (r.getEda()!=null )entailmentrelationEdge.setAttribute("eda",r.getEda().toString());
						// set lap attribute to eu element
						if (r.getLap() != null) entailmentrelationEdge.setAttribute("lap",r.getLap().getComponentName());
					}
					
					return new DOMSource(doc);
				
				} catch (DOMException | ParserConfigurationException e) {
					throw new EntailmentGraphRawException(e.getMessage());
					// TODO Auto-generated catch block
				}		 
		  }
	
	public void toXML(String filename) throws EntailmentGraphRawException{
		
		try {
			XMLFileWriter.write(this.toXML(), filename);
		} catch (TransformerException e) {
			throw new EntailmentGraphRawException("Cannot save the graph to the xml file "+filename+".\n"+e);
		}
		
	}
	
	public String toStringDetailed(){
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
	 * TRANSITIVE CLOSURE
	 * ****************************************************************************************/
	   /**
     * Computes floor(log_2(n)) + 1
     */
    private int computeBinaryLog(int n)
    {
        assert n >= 0;

        int result = 0;
        while (n > 0) {
            n >>= 1;
            ++result;
        }

        return result;
    }
    
    /** 
     * @param source
     * @param target
     * @return the highest confidence of source -> target
     */
    private Double getBestDirectConfidence(EntailmentUnit source, EntailmentUnit target){
    	Double confidence = 0.0;
    	for (EntailmentRelation edge : this.getAllEdges(source, target)){
    		if (edge.getConfidence() > confidence) confidence = edge.getConfidence();
    	}
    	return confidence;
    }
    
    /**
	 *  Adds transitive closure edges to the graph. Only consider "entailment" edges!!! (currently we don't propagate non-entailment relation)
	 *  Based on org.jgrapht.alg.TransitiveClosure
	 *  
	 * @param changeTypeOfExistingEdges - if true, existing transitive closure edges will change their type to "TRANSITIVE_CLOSURE" 
	 */
	public void applyTransitiveClosure(boolean changeTypeOfExistingEdges){    
		Map<EntailmentUnit,Double> newEdgeTargets = new HashMap<EntailmentUnit,Double>();

        // At every iteration of the outer loop, we add a path of length 1
        // between nodes that originally had a path of length 2. In the worst
        // case, we need to make floor(log |V|) + 1 iterations. We stop earlier
        // if there is no change to the output graph.

        int bound = computeBinaryLog(this.vertexSet().size());
        boolean done = false;
        for (int i = 0; !done && (i < bound); ++i) {
            done = true;
            for (EntailmentUnit v1 : this.vertexSet()) {
                newEdgeTargets.clear();

                for (EntailmentUnit v2 : this.getEntailedNodes(v1)) {
                	Double confidence = getBestDirectConfidence(v1,v2);
                	
                    for (EntailmentUnit v3 : this.getEntailedNodes(v2)) {

                    	if (newEdgeTargets.containsKey(v3)){
                    		continue; // since we're in a multu-graph, maybe we already added v3 to newEdgeTargets 
                    	}
                    	
                    	Double secondStepConfidence = getBestDirectConfidence(v2, v3);
                        // Assign min confidence of the 2 edges as the confidence of the transitive edge
                        if (secondStepConfidence < confidence) confidence=secondStepConfidence;

                        if (v1.equals(v3)) {
                            // Don't add self loops.
                            continue;
                        }

                        EntailmentRelation e = this.getEdge(v1, v3);
                        if (e != null) {
                            // There is already an edge from v1 ---> v3
                        	if (!changeTypeOfExistingEdges)	continue; 
                        	
                        	if (e.getEdgeType().is(EdgeType.TRANSITIVE_CLOSURE)) {
                        		continue; // if it's a closure edge already - skip
                        	}
 
                        	// if it's not a closure edge, add it as an edge with EdgeType="TRANSITIVE_CLOSURE"
                        	confidence = e.getConfidence(); // if we had this edge before, we want to keep its confidence, we only change its type 
                        }
                        
                        newEdgeTargets.put(v3,confidence);
                        done = false;
                    }
                }

                for (EntailmentUnit v3 : newEdgeTargets.keySet()) {
                    EntailmentRelation e = this.getEdge(v1, v3);
                	if (e!=null){
                    	this.removeAllEdges(v1, v3);                    	
                		logger.info("Removed \"direct\" edge(s): "+ e.toString()+" to add a corresponding transitive closure edge");
                    }
                	double confidence = newEdgeTargets.get(v3);
                	EntailmentRelation closureEdge = new EntailmentRelation(v1, v3, new TEDecisionWithConfidence(confidence, DecisionLabel.Entailment), EdgeType.TRANSITIVE_CLOSURE);
                	this.addEdge(v1, v3, closureEdge);
                	logger.info("Added transitive closure edge: "+closureEdge.toString());
                }
            }
        }
	}	
	
    /**
	 *  Removes all transitive closure edges from the graph.
	 *  Expected use - for internal testing purposes only
	 */
/*	public void removeTransitiveClosure(){    
		Set<EntailmentRelation> edgesToRemove = new HashSet<EntailmentRelation>();

        // At every iteration of the outer loop, we find if there is a path of length 1
        // between nodes that also have a path of length 2. In the worst
        // case, we need to make floor(log |V|) + 1 iterations. We stop earlier
        // if there is no change to the output graph.

        int bound = computeBinaryLog(this.vertexSet().size());
        boolean done = false;
        for (int i = 0; !done && (i < bound); ++i) {
            done = true;
            for (EntailmentUnit v1 : this.vertexSet()) {
            	edgesToRemove.clear();

                for (EntailmentUnit v2 : this.getEntailedNodes(v1)) {
                    for (EntailmentUnit v3 : this.getEntailedNodes(v2)) {
                        Set<EntailmentRelation> e = this.getAllEdges(v1, v3);
                        if (!e.isEmpty()) {
                        	edgesToRemove.addAll(e);
                        	logger.info("Remove transitive closure edges: "+ e.toString());
                            done = false;
                        }
                    }
                }

                this.removeAllEdges(edgesToRemove);
            }
        }
	}*/
	
	public void removeTransitiveClosure(){   
		Set<EntailmentRelation> edgesToRemove = new HashSet<EntailmentRelation>();
		for(EntailmentRelation e : this.edgeSet()){
			if (e.getEdgeType().is(EdgeType.TRANSITIVE_CLOSURE)) edgesToRemove.add(e); 
		}
		removeAllEdges(edgesToRemove);
	}
	
	/******************************************************************************************
	 * METHODS FOR INTERNAL TESTING PURPOSES
	 * ****************************************************************************************/
	
	/** Create an edge from sourceVertex to targetVertex using the random EDA 
	 * No LAP is specified, which is not the case is real settings when EDA is always paired with its required LAP 
	 * @param sourceVertex
	 * @param targetVertex
	 * @param eda
	 * @return the edge, which was added to the graph
	 * @throws LAPException 
	 */
	public EntailmentRelation addEdgeWithRandomDecision(EntailmentUnit sourceVertex, EntailmentUnit targetVertex) {
		EntailmentRelation edge = EntailmentRelation.generateRandomEntailmentRelation(sourceVertex, targetVertex);
		this.addEdge(sourceVertex, targetVertex, edge);
		return edge;
	}
	
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
		
	public static EntailmentGraphRaw getSampleOuput(boolean randomEdges) {
		
		// create the to-be graph nodes
		EntailmentUnit A = new EntailmentUnit("Food was really bad.",1,"Food was really bad.","interaction1");
		EntailmentUnit B = new EntailmentUnit("Food was bad.",0,"Food was really bad.","interaction1");
		EntailmentUnit C = new EntailmentUnit("I didn't like the food.",0,"I didn't like the food.","interaction2");
		EntailmentUnit D = new EntailmentUnit("a little more leg room would have been perfect",1,"a little more leg room would have been perfect","interaction3");
		EntailmentUnit E = new EntailmentUnit("more leg room would have been perfect",0,"a little more leg room would have been perfect","interaction3"); 
		EntailmentUnit F = new EntailmentUnit("Disappointed with the amount of legroom compared with other trains",2,"Disappointed with the amount of legroom compared with other trains","interaction4");
		EntailmentUnit G = new EntailmentUnit("Disappointed with legroom compared with other trains",1,"Disappointed with the amount of legroom compared with other trains","interaction4");
		EntailmentUnit H = new EntailmentUnit("Disappointed with the amount of legroom",1,"Disappointed with the amount of legroom compared with other trains","interaction4");
		EntailmentUnit I = new EntailmentUnit("Disappointed with legroom",0,"Disappointed with the amount of legroom compared with other trains","interaction4");

		// create an empty graph
		EntailmentGraphRaw sampleRawGraph = new EntailmentGraphRaw();
		
		// add nodes
		sampleRawGraph.addVertex(A); sampleRawGraph.addVertex(B); sampleRawGraph.addVertex(C);
		sampleRawGraph.addVertex(D); sampleRawGraph.addVertex(E); sampleRawGraph.addVertex(F);
		sampleRawGraph.addVertex(G); sampleRawGraph.addVertex(H); sampleRawGraph.addVertex(I);
		

		if (randomEdges){ // add random edges			
			// add edges - calculate TEDecision in both directions between all pairs of nodes (don't calculate for a node with itself) 
			for (EntailmentUnit v1 : sampleRawGraph.vertexSet()){
				for (EntailmentUnit v2 : sampleRawGraph.vertexSet()){
					if (!v1.equals(v2)) { //don't calculate for a node with itself  
						sampleRawGraph.addEdgeWithRandomDecision(v1, v2);
						sampleRawGraph.addEdgeWithRandomDecision(v2, v1);
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

			sampleRawGraph.addEdge(B, C, new EntailmentRelation(B, C, new TEDecisionByScore(0.72), EdgeType.EDA));
			sampleRawGraph.addEdge(C, B, new EntailmentRelation(C, B, new TEDecisionByScore(0.77), EdgeType.EDA));

			sampleRawGraph.addEdge(B, E, new EntailmentRelation(B, E, new TEDecisionByScore(0.05), EdgeType.EDA));
			sampleRawGraph.addEdge(E, B, new EntailmentRelation(E, B, new TEDecisionByScore(0.08), EdgeType.EDA));
			sampleRawGraph.addEdge(C, E, new EntailmentRelation(C, E, new TEDecisionByScore(0.07), EdgeType.EDA));
			sampleRawGraph.addEdge(E, C, new EntailmentRelation(E, C, new TEDecisionByScore(0.11), EdgeType.EDA));
			
			sampleRawGraph.addEdge(E, I, new EntailmentRelation(E, I, new TEDecisionByScore(0.82), EdgeType.EDA));
			sampleRawGraph.addEdge(I, E, new EntailmentRelation(I, E, new TEDecisionByScore(0.74), EdgeType.EDA));

			sampleRawGraph.addEdge(B, I, new EntailmentRelation(B, I, new TEDecisionByScore(0.06), EdgeType.EDA));
			sampleRawGraph.addEdge(I, B, new EntailmentRelation(I, B, new TEDecisionByScore(0.03), EdgeType.EDA));
			sampleRawGraph.addEdge(C, I, new EntailmentRelation(C, I, new TEDecisionByScore(0.09), EdgeType.EDA));
			sampleRawGraph.addEdge(I, C, new EntailmentRelation(I, C, new TEDecisionByScore(0.04), EdgeType.EDA));
								
		}

		return sampleRawGraph;
	}
	
	public static EntailmentGraphRaw getSampleOuputWithCategories(boolean randomEdges){
		
		// create the to-be graph nodes
		EntailmentUnit A = new EntailmentUnit("Food was really bad.",1,"Food was really bad.", "1");
		EntailmentUnit B = new EntailmentUnit("Food was bad.",0,"Food was really bad.", "1");
		EntailmentUnit C = new EntailmentUnit("I didn't like the food.",0,"I didn't like the food.", "2");
		EntailmentUnit D = new EntailmentUnit("a little more leg room would have been perfect",1,"a little more leg room would have been perfect", "3");
		EntailmentUnit E = new EntailmentUnit("more leg room would have been perfect",0,"a little more leg room would have been perfect", "3"); 
		EntailmentUnit F = new EntailmentUnit("Disappointed with the amount of legroom compared with other trains",2,"Disappointed with the amount of legroom compared with other trains", "3");
		EntailmentUnit G = new EntailmentUnit("Disappointed with legroom compared with other trains",1,"Disappointed with the amount of legroom compared with other trains", "3");
		EntailmentUnit H = new EntailmentUnit("Disappointed with the amount of legroom",1,"Disappointed with the amount of legroom compared with other trains", "4");
		EntailmentUnit I = new EntailmentUnit("Disappointed with legroom",0,"Disappointed with the amount of legroom compared with other trains", "3");

		// create an empty graph
		EntailmentGraphRaw sampleRawGraph = new EntailmentGraphRaw();
		
		// add nodes
		sampleRawGraph.addVertex(A); sampleRawGraph.addVertex(B); sampleRawGraph.addVertex(C);
		sampleRawGraph.addVertex(D); sampleRawGraph.addVertex(E); sampleRawGraph.addVertex(F);
		sampleRawGraph.addVertex(G); sampleRawGraph.addVertex(H); sampleRawGraph.addVertex(I);
		

		if (randomEdges){ // add random edges
			// add edges - calculate TEDecision in both directions between all pairs of nodes (don't calculate for a node with itself) 
			for (EntailmentUnit v1 : sampleRawGraph.vertexSet()){
				for (EntailmentUnit v2 : sampleRawGraph.vertexSet()){
					if (!v1.equals(v2)) { //don't calculate for a node with itself  
						sampleRawGraph.addEdgeWithRandomDecision(v1, v2);
						sampleRawGraph.addEdgeWithRandomDecision(v2, v1);
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

			sampleRawGraph.addEdge(B, C, new EntailmentRelation(B, C, new TEDecisionByScore(0.72), EdgeType.EDA));
			sampleRawGraph.addEdge(C, B, new EntailmentRelation(C, B, new TEDecisionByScore(0.77), EdgeType.EDA));

			sampleRawGraph.addEdge(B, E, new EntailmentRelation(B, E, new TEDecisionByScore(0.05), EdgeType.EDA));
			sampleRawGraph.addEdge(E, B, new EntailmentRelation(E, B, new TEDecisionByScore(0.08), EdgeType.EDA));
			sampleRawGraph.addEdge(C, E, new EntailmentRelation(C, E, new TEDecisionByScore(0.07), EdgeType.EDA));
			sampleRawGraph.addEdge(E, C, new EntailmentRelation(E, C, new TEDecisionByScore(0.11), EdgeType.EDA));
			
			sampleRawGraph.addEdge(E, I, new EntailmentRelation(E, I, new TEDecisionByScore(0.82), EdgeType.EDA));
			sampleRawGraph.addEdge(I, E, new EntailmentRelation(I, E, new TEDecisionByScore(0.74), EdgeType.EDA));

			sampleRawGraph.addEdge(B, I, new EntailmentRelation(B, I, new TEDecisionByScore(0.06), EdgeType.EDA));
			sampleRawGraph.addEdge(I, B, new EntailmentRelation(I, B, new TEDecisionByScore(0.03), EdgeType.EDA));
			sampleRawGraph.addEdge(C, I, new EntailmentRelation(C, I, new TEDecisionByScore(0.09), EdgeType.EDA));
			sampleRawGraph.addEdge(I, C, new EntailmentRelation(I, C, new TEDecisionByScore(0.04), EdgeType.EDA));
								
		}

		return sampleRawGraph;
	}

	/******************************************************************************************
	 * LEGACY
	 * ****************************************************************************************/
	/*	*//**
	 * 
	 * @param arg0 -- the class for the edges (in our case this would be FragmentGraphEdge.class)
	 *//*
	public EntailmentGraphRaw(Class<? extends EntailmentRelation> arg0) {		
		super(arg0);
	}
	
	*//**
	 * 
	 * @param arg0 -- edge factory
	 *//*
	public EntailmentGraphRaw(EdgeFactory<EntailmentUnit,EntailmentRelation> arg0) {
		super(arg0);		
	}
*/
	
}

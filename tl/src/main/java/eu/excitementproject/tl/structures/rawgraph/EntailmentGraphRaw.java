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
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.edautils.RandomEDA;
import eu.excitementproject.tl.edautils.TEDecisionByScore;
import eu.excitementproject.tl.edautils.TEDecisionWithConfidence;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphEdge;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;


/**
 * 
 * 
 * The graph structure for the work graph - EntailmentGraphRaw.
 * <p> The graph grows by adding to it FragmentGraph-s by "merging", which is done through {@link GraphMerger} interface. 
 * <p>The nodes are entailment units ({@link EntailmentUnit}), and the edges ({@link EntailmentRelation}) 
 * are generated based on decisions from the EDAs. 
 * As such there can be several edges between the same two nodes, each corresponding to one EDA query.  
 * 
 *  This graph extends {@link DirectedMultigraph}, to allow for multiple directed edges between
 *  the same two nodes. The JavaDoc for the {@link DirectedMultigraph} for information about
 *  inherited methods can be found here:
 *  http://jgrapht.org/javadoc/org/jgrapht/graph/DirectedMultigraph.html

 * @author Lili Kotlerman & Vivi Nastase & Aleksandra Gabryszak & Kathrin Eichler
 */
public class EntailmentGraphRaw extends
		DirectedMultigraph<EntailmentUnit,EntailmentRelation> {

	Logger logger = Logger.getLogger("eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw");
	private static final long serialVersionUID = -3274655854206417667L;
	
	private boolean addLemmatizedLabel;

	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/

	/**
	 * Initialize an empty work graph. 
	 * @param addLemmatizedLabel add lemmatized text of each entailment unit text as lemmatizedText
	 */
	public EntailmentGraphRaw(boolean addLemmatizedLabel){
		this();
		this.addLemmatizedLabel = addLemmatizedLabel;
	}
	
	/**
	 * Create an entailment graph with the given nodes and edges
	 * @param nodes
	 * @param edges
	 */
	public EntailmentGraphRaw(Set<EntailmentUnit> nodes, Set<EntailmentRelation> edges){
		this();
		for (EntailmentUnit node : nodes){
			this.addVertex(node);  
		}
		for (EntailmentRelation e : edges){
			this.addEdge(e.getSource(), e.getTarget(), e);
		}
	}	
	
	/**
	 * Initialize a graph from an xml file
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
				String lemmaLabel = euElement.getAttribute("lemmaLabel");
				if(!lemmaLabel.isEmpty()){
					this.addLemmatizedLabel = true;
				}
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

				if(this.addLemmatizedLabel){			       
					this.addVertex(new EntailmentUnit(text, lemmaLabel, completeStatementTexts, mentions, level));
				}
				else{
					this.addVertex(new EntailmentUnit(text, completeStatementTexts, mentions, level));
				}
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
	 * @param fg -- a fragment graph
	 * @param includeNonEntailingEdges -- whether to include negative (non-entailing) edges from the given fragment graph. If set to false, only positive edges will be added. 
	 */
	public EntailmentGraphRaw(FragmentGraph fg, boolean includeNonEntailingEdges) {
		super(EntailmentRelation.class);
		if (includeNonEntailingEdges) copyFragmentGraphNodesAndAllEdges(fg);
		else copyFragmentGraphNodesAndEntailingEdges(fg);
	}
	
	/**
	 * Initialize a work graph from a fragment graph
	 * @param fg -- a fragment graph
	 * @param includeNonEntailingEdges -- whether to include negative (non-entailing) edges from the given fragment graph. If set to false, only positive edges will be added. 
	 * @param addLemmatizedLabel
	 */
	public EntailmentGraphRaw(FragmentGraph fg, boolean includeNonEntailingEdges, boolean addLemmatizedLabel) {
		super(EntailmentRelation.class);
		this.addLemmatizedLabel = addLemmatizedLabel;
		if (includeNonEntailingEdges) copyFragmentGraphNodesAndAllEdges(fg);
		else copyFragmentGraphNodesAndEntailingEdges(fg);
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
	
	/**
	 * @return
	 */
	public boolean hasLemmatizedLabel(){
		return this.addLemmatizedLabel;
	}
	
	

	/******************************************************************************************
	 * OTHER AUXILIARY METHODS
	 * ****************************************************************************************/

	/**
	 * Return true if an edge entailingNode->entailedNode is present in the graph and originates from a fragment graph (as specified by its {@link EdgeType}).
	 * Otherwise return false.
	 * @param entailingNode
	 * @param entailedNode
	 * @return true/false
	 */
	public boolean isFragmentGraphEdge(EntailmentUnit entailingNode, EntailmentUnit entailedNode){
		Set<EntailmentRelation> se = getAllEdges(entailingNode, entailedNode);
		if (se==null) return false;
		for (EntailmentRelation e : se){
			if (e.getEdgeType().is(EdgeType.FRAGMENT_GRAPH)) return true;
		}
		return false;
	}

	/**
	 * Detect if there is a conflict for entailingNode->entailedNode
	 * @param entailingNode
	 * @param entailedNode
	 * @return true if there is at least one entailing edge from entailingNode to entailedNode, and at least one non-entailing edge from entailingNode to entailedNode
	 */
	public boolean isConflict(EntailmentUnit entailingNode, EntailmentUnit entailedNode){
		if (entailingNode.equals(entailedNode)) return false; // if both nodes are the same, no conflict, they are always mutually entailing
		Set<EntailmentRelation> se = getAllEdges(entailingNode, entailedNode);
		if (se==null) return false;	// if no edges - no conflict
		boolean isPositive = false;
		boolean isNegative = false;
		for (EntailmentRelation e : se){
			if (e.getLabel().is(DecisionLabel.Entailment)) isPositive=true;
			if (e.getLabel().is(DecisionLabel.NonEntailment)) isNegative = true;
		}
		if (isPositive && isNegative) return true; 
		return false;
	}
	
	
	/**
	 * Detect if there is entailment for for entailingNode->entailedNode
	 * @param entailingNode
	 * @param entailedNode
	 * @return true if there is at least one entailing edge from entailingNode to entailedNode
	 */
	public boolean isEntailment(EntailmentUnit entailingNode, EntailmentUnit entailedNode){
		if (entailingNode.equals(entailedNode)) return true; // if both nodes are the same
		Set<EntailmentRelation> se = getAllEdges(entailingNode, entailedNode);
		if (se==null) return false;		
		for (EntailmentRelation e : se){
			if (e.getLabel().is(DecisionLabel.Entailment)) return true;
		}
		return false;
	}
	
    /**
     * Detect if there is entailment without conflicts for entailingNode->entailedNode
     * @param entailingNode
     * @param entailedNode
     * @return true if there are only entailing edge(s) from entailingNode to entailedNode, and no non-entailing ones
     */
    public boolean isEntailmentOnly(EntailmentUnit entailingNode, EntailmentUnit entailedNode) {
		boolean isEntailment = isEntailment(entailingNode, entailedNode);
		boolean isConflict = isConflict(entailingNode, entailedNode);
		if (isEntailment){
			if (!isConflict) return true; // if isEntailment, but no conflict - then only entailment edges are there
		}
		return false;
	}


	/**
	 * Detect if there is a positive entailment decision between nodeA and nodeB in any direction. 
	 * @param nodeA
	 * @param nodeB
	 * @return true if there is entailment for either nodeA->nodeB, nodeB->nodeA or both. Does not take conflicts into consideration.
	 */
	public boolean isEntailmentInAnyDirection(EntailmentUnit nodeA, EntailmentUnit nodeB){
		if (nodeA.equals(nodeB)) return true; // if both nodes are the same
		if (isEntailment(nodeA, nodeB)||(isEntailment(nodeB, nodeA))) return true;
		return false;
	}

	/**
	 * Detect if there is a positive entailment decision between nodeA and nodeB in a single direction only. 
	 * @param nodeA
	 * @param nodeB
	 * @return true if there is entailment for either (1) nodeA->nodeB, but not nodeB->nodeA or (2) nodeB->nodeA, but not nodeA->nodeB.
	 *  Does not take conflicts into consideration.
	 */
	public boolean isEntailmentInSingleDirectionOnly(EntailmentUnit nodeA, EntailmentUnit nodeB){
		if (nodeA.equals(nodeB)) return false; // if both nodes are the same - this is bidirectional, not single direction
		if (isEntailment(nodeA, nodeB)) {
			if (!isEntailment(nodeB, nodeA))return true;
		}
		if (isEntailment(nodeB, nodeA)) {
			if (!isEntailment(nodeA, nodeB)) return true;
		}		
		return false;
	}

	/** Copies all nodes and entailing edges (including transitive closure) from the input fragment graph to the raw entailment graph
	 * @param fg - the input fragment graph
	 */
	public void copyFragmentGraphNodesAndEntailingEdges(FragmentGraph fg){
		// first add transitive closure to the FG
		fg.applyTransitiveClosure();
		
		// copy nodes (add if new, update mentions and complete statements if exist) - need to do this separately from edges, since there might be "orphan" nodes (this should only happen when the fragment graph has a single node, i.e. base statement = complete statement)
		for(EntailmentUnitMention fragmentGraphNode : fg.vertexSet()){
			this.addEntailmentUnitMention(fragmentGraphNode, fg.getCompleteStatement().getText());
			}
		// add edges
		for (FragmentGraphEdge fragmentGraphEdge : fg.edgeSet()){
			this.addEdgeFromFragmentGraph(fragmentGraphEdge, fg);
		}
	}
	
	/** Copies all nodes and all edges (entailing, inclu transitive closure, and non-entailing) from the input fragment graph to the raw entailment graph
	 * @param fg - the input fragment graph
	 */
	public void copyFragmentGraphNodesAndAllEdges(FragmentGraph fg){
		// first add transitive closure to the FG
		fg.applyTransitiveClosure();
		
		// copy nodes (add if new, update mentions and complete statements if exist) - need to do this separately from edges, since there might be "orphan" nodes (this should only happen when the fragment graph has a single node, i.e. base statement = complete statement)
		for(EntailmentUnitMention fragmentGraphNode : fg.vertexSet()){
			this.addEntailmentUnitMention(fragmentGraphNode, fg.getCompleteStatement().getText());
			}

		for (FragmentGraphEdge fragmentGraphEdge : fg.edgeSet()){
			this.addEdgeFromFragmentGraph(fragmentGraphEdge, fg);
		}
		
		// add all non-entailment edges from FG explicitly into the graph 
		for (EntailmentUnitMention src : fg.vertexSet()){
			for(EntailmentUnitMention tgt : fg.vertexSet()){
				if (src.equals(tgt)) continue;
				if (!fg.containsEdge(src, tgt)){ // if a non-entailment is to be added
					EntailmentUnit srcUnit = this.getVertexWithText(src.getText());
					EntailmentUnit tgtUnit = this.getVertexWithText(tgt.getText());
					EntailmentRelation e = new EntailmentRelation(srcUnit, tgtUnit, new TEDecisionWithConfidence(1.0, DecisionLabel.NonEntailment));
					e.edgeType = EdgeType.FRAGMENT_GRAPH;
					if (this.containsEdge(srcUnit,tgtUnit)){
						Set<EntailmentRelation> edgesToRemove = new HashSet<EntailmentRelation>(this.getAllEdges(srcUnit, tgtUnit));
						this.removeAllEdges(edgesToRemove);
					}
					this.addEdge(srcUnit, tgtUnit, e);
				}
			}
		}
		
	}

	/** Copies all nodes and all edges from the input raw graph to the raw entailment graph
	 * @param egr - the input raw graph
	 */
	public void copyRawGraphNodesAndAllEdges(EntailmentGraphRaw egr){
		
		// first add transitive closure to the EGR
		//egr.applyTransitiveClosure(); //TODO: check if needed! if transitive closure is applied is decided in GraphMerger
	
		// copy nodes (add if new, update mentions and complete statements if exist) - need to do this separately from edges, 
		//since there might be "orphan" nodes (this should only happen when the fragment graph has a single node, 
		//i.e. base statement = complete statement)
		for (EntailmentUnit rawGraphNode : egr.vertexSet()) {
			for(EntailmentUnitMention mention : rawGraphNode.getMentions()){
				this.addEntailmentUnitMention(mention, mention.getTextWithoutDoubleSpaces());
			}
		}
		
		// copy edges
		for (EntailmentRelation rawGraphEdge : egr.edgeSet()){
			String sourceText = rawGraphEdge.getSource().getTextWithoutDoubleSpaces();
			String targetText = rawGraphEdge.getTarget().getTextWithoutDoubleSpaces();
			EntailmentUnit sourceVertex = this.getVertexWithText(sourceText);
			EntailmentUnit targetVertex = this.getVertexWithText(targetText);
			
			//TODO: information about EDA and LAP is lost. Consider better implementation to reconstruct the EDA and LAP information
			EntailmentRelation rel = new EntailmentRelation(sourceVertex, targetVertex, 
					rawGraphEdge.getTEdecision(), rawGraphEdge.getEdgeType());
			if(!this.isEntailment(sourceVertex, targetVertex)){
				this.addEdge(sourceVertex, targetVertex, rel);
			}
		}
	}

	
	/** The method gets an EntailmentUnitMention and either adds a new EntailmentUnit node or, if a relevant EntailmentUnit already exists in the graph, updates the list of its mentions  
	 * @param mention - the EntailmentUnitMention to be added to the graph
	 * @param completeStatementText - the text of the mention's complete statement
	 */
	public void addEntailmentUnitMention(EntailmentUnitMention mention, String completeStatementText){
		EntailmentUnit node = this.getVertexWithText(mention.getText());
		if (node==null) {
			EntailmentUnit newNode = new EntailmentUnit(mention, completeStatementText, this.addLemmatizedLabel);
			this.addVertex(newNode);
		}
		else{
			//Commented out: Tmp patch to have same text on separate nodes
			//EntailmentUnit newNode = new EntailmentUnit(mention, completeStatementText);
			//this.addVertex(newNode);			
			
			node.addMention(mention, completeStatementText);  
		}
	}
	
	/** Given a base statement node and a complete statement text, the method finds and returns all the nodes from the corresponding fragment graph
	 * (fragment graph can be defined by its complete statement, while the same base statement can be part of different fragment graphs)  
	 * @param baseStatementNode
	 * @param completeStatementText
	 * @return Hashtable where keys (int) are levels and values (Set<{@link EntailmentUnit}>) are sets of entailment unit nodes found at the corresponding level.
	 * If the input base statement was not produced from the input complete statement, empty Hashtable will be returned  
	 * @throws EntailmentGraphRawException
	 */
	public Hashtable<Integer, Set<EntailmentUnit>> getFragmentGraphNodes(EntailmentUnit baseStatementNode, String completeStatementText) throws EntailmentGraphRawException {
		Hashtable<Integer, Set<EntailmentUnit>> nodesByLevel = new Hashtable<Integer, Set<EntailmentUnit>>(); 
		
 /*		
		logger.info("----");
		logger.info(baseStatementNode);
		logger.info(completeStatementText);
		logger.info(baseStatementNode.completeStatementTexts);
 */
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
	 * @return updated nodesToReturn set
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
	 * @return Set<{@link EntailmentUnit}> with all the entailing nodes of the given node
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
	 * @param node - node whose entailing nodes are returned
	 * @param level - number of modifiers in the returned nodes
	 * @return Set<{@link EntailmentUnit}> with the entailing nodes of the given node
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
	 * @return Set<{@link EntailmentUnit}> with all the entailed nodes of the given node
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
	 * @return Set<{@link EntailmentUnit}> with all the entailed nodes of the given node
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
	

	/** Get nodes entailed by a given node, which belong to the same fragment graph with the given node and have the specified number of modifiers (level)
	 * @param node whose entailed nodes are returned
	 * @param level - number of modifiers
	 * @return Set<{@link EntailmentUnit}>
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
	 * Create an edge from sourceVertex to targetVertex using the specified eda and lap 
	 * @param sourceVertex
	 * @param targetVertex
	 * @param eda
	 * @param lap
	 * @return the edge, which was added to the graph
	 * @throws EntailmentGraphRawException 
	 */
	public EntailmentRelation addEdgeFromEDA(EntailmentUnit sourceVertex, EntailmentUnit targetVertex, EDABasic<?> eda, CachedLAPAccess lap) throws EntailmentGraphRawException{
		EntailmentRelation edge = new EntailmentRelation(sourceVertex, targetVertex, eda, lap);
		this.addEdge(sourceVertex, targetVertex, edge);
		return edge;
	}
	
	/**
	 * Copy an edge from a FragmentGraph. 
	 * <p>If vertices do not exist - add them, and if a vertex exists - add the corresponding new entailment unit mention.
	 * Since the mentions, their complete statements and interaction ids are sets, there will be no duplicate mentions etc. added to the graph nodes
	 * <p>Currently the original weight of the edges is not changed when copying.
	 * @param fragmentGraphEdge -- the edge to copy into the graph
	 * @param fg -- the fragment graph, from which the edge is copied
	 * @return the edge, which was added to the graph
	 */
	public EntailmentRelation addEdgeFromFragmentGraph(FragmentGraphEdge fragmentGraphEdge, FragmentGraph fg){
		// take care of the source and target vertices 
		this.addEntailmentUnitMention(fragmentGraphEdge.getSource(), fg.getCompleteStatement().getText());
		this.addEntailmentUnitMention(fragmentGraphEdge.getTarget(), fg.getCompleteStatement().getText());

		// now create and add the edge
		EntailmentRelation edge = new EntailmentRelation(this.getVertexWithText(fragmentGraphEdge.getSource().getText()), this.getVertexWithText(fragmentGraphEdge.getTarget().getText()), new TEDecisionByScore(fragmentGraphEdge.getWeight()), EdgeType.FRAGMENT_GRAPH);
		this.addEdge(this.getVertexWithText(fragmentGraphEdge.getSource().getText()), this.getVertexWithText(fragmentGraphEdge.getTarget().getText()), edge);
//		logger.info("Added FG edge "+edge.toString());
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
	 * Return the vertex ({@link EntailmentUnit}), which has the given text at one of its mentions, if it is found in the graph. 
	 * Otherwise return null.
	 * The case of the text and extra spaces are ignored to unify texts regardless to their case/spacing differences.
	 * @param text the text of the EntailmentUnit to be found
	 * @return the vertex, if found.
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
			s+="\""+node.toDOT()+"\";";
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
	public void toDOT(String filename) throws EntailmentGraphRawException{
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(filename));
				out.write(this.toDOT());
				out.close();
			} catch (IOException e) {
				throw new EntailmentGraphRawException("Could not save the file in DOT format to "+filename+"\n"+e.getMessage());

			}
	}	
	
	
	/**
	 * Translate the graph to xml format
	 * @return contents of the graph in {@link DOMSource} format
	 * @throws EntailmentGraphRawException
	 */
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
						// set lemmaLabel attribute to eu element
						if(eu.getLemmatizedText() != null){
							entailmentUnitNode.setAttribute("lemmaLabel",eu.getLemmatizedText());
						}
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
	
	/** Save the graph to an xml file
	 * @param filename -- the name of the file to be saved
	 * @throws EntailmentGraphRawException
	 */
	public void toXML(String filename) throws EntailmentGraphRawException{
		
		try {
			XMLFileWriter.write(this.toXML(), filename);
		} catch (TransformerException e) {
			throw new EntailmentGraphRawException("Cannot save the graph to the xml file "+filename+".\n"+e);
		}
		
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
     * Find the best confidence of source -> target within the graph
     * @param source
     * @param target
     * @return the highest confidence of source -> target
     */
    private Double getBestDirectConfidence(EntailmentUnit source, EntailmentUnit target){
    	Double confidence = 0.0;
    	for (EntailmentRelation edge : this.getAllEdges(source, target)){
    		if (edge.getLabel().is(DecisionLabel.NonEntailment)) continue; // don't consider the confidence of non-entailing edges
    		if (edge.getConfidence() > confidence) confidence = edge.getConfidence();
    	}
    	return confidence;
    }
    
    /**
	 *  Add transitive closure edges to the graph. 
	 *  <p> Only considers "entailment" edges, i.e. can add conflicts to the graph if non-entailment decisions are present in it
	 *  <p> Based on the algorithm from {@link org.jgrapht.alg.TransitiveClosure}
	 */
	public void applyTransitiveClosure(){    
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

                    	if (this.isEntailment(v1, v3)){
                    		// don't add duplicate entailing edges
                        		continue; 
                    	}
                            	
                        newEdgeTargets.put(v3,confidence);
                        
/*                        //debugging part
                        String dbgTarget = "The food offering is in need of a improvement";
                        String dbgSource = "The food is in need of a serious improvement";
                        if (v1.getTextWithoutDoubleSpaces().equals(dbgSource)){
                        	if (v3.getTextWithoutDoubleSpaces().equals(dbgTarget)){
                        		logger.info(v1);
                        		logger.info(v1.getTextWithoutDoubleSpaces());
                        		logger.info(v2);
                        		logger.info(v2.getTextWithoutDoubleSpaces());
                        		logger.info(v3);
                        		logger.info(v3.getTextWithoutDoubleSpaces());
                        		try {
									System.in.read();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
                        	}
                        }
*/                        
                        done = false;
                    }
                }

                for (EntailmentUnit v3 : newEdgeTargets.keySet()) {
                	double confidence = newEdgeTargets.get(v3);
                	EntailmentRelation closureEdge = new EntailmentRelation(v1, v3, new TEDecisionWithConfidence(confidence, DecisionLabel.Entailment), EdgeType.TRANSITIVE_CLOSURE);
                	this.addEdge(v1, v3, closureEdge);
                	logger.info("Added transitive closure edge: "+closureEdge.toString());
                }
            }
        }
	}	
		
	/**
	 * Returns the transitive reduction of the graph. Does not change the graph itself.
	 * <p><b>Not tested yet!</b>
	 * @return transitive reduction of the graph
	 */
	public EntailmentGraphRaw getTransitiveReduction(){   
		EntailmentGraphRaw reduction = new EntailmentGraphRaw(this.vertexSet(), this.edgeSet());
		for (EntailmentUnit x : reduction.vertexSet()){
			for (EntailmentUnit y : reduction.vertexSet()){
				if (x.equals(y)) continue;
				for (EntailmentUnit z : reduction.vertexSet()){
					if (x.equals(z)) continue;
					if (y.equals(z)) continue;
					if ((isEntailment(x, y))&&(isEntailment(y, z))) reduction.removeAllEdges(x, z);
				}
			}		
		}
		return reduction;
	}

	/******************************************************************************************
	 * METHODS FOR INTERNAL TESTING PURPOSES
	 * ****************************************************************************************/
	
	/**
	 * @return detailed representation of the graph as {@link String}
	 */
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

	/**
	 * Return the vertex ({@link EntailmentUnit}) with text equal to the given text, if it is found in the graph. 
	 * Otherwise return null.
	 * The case/spacing of the text is not ignored
	 * @param text - the text of the EntailmentUnit to be found
	 * @return
	 */
	public EntailmentUnit getVertexWithExactText(String text){
		for (EntailmentUnit eu : this.vertexSet()){
			if (eu.getText().equals(text)) return eu;
		}
		return null;
	}

	/** Create an edge from sourceVertex to targetVertex with a random decision using {@link RandomEDA}. 
	 * @param sourceVertex
	 * @param targetVertex
	 * @return the edge, which was added to the graph
	 */
	public EntailmentRelation addEdgeWithRandomDecision(EntailmentUnit sourceVertex, EntailmentUnit targetVertex) {
		EntailmentRelation edge = EntailmentRelation.generateRandomEntailmentRelation(sourceVertex, targetVertex);
		this.addEdge(sourceVertex, targetVertex, edge);
		return edge;
	}
	
	/**
	 * Get a sample EntailmentGraphRaw
	 * <p>
	 * Nodes: (bs - base statement):
	 	<li> A - "Food was really bad." (modifier: really)
		<li> B (bs) - "Food was bad."
		<li> C (bs) - "I didn't like the food."
		<li> D - "a little more leg room would have been perfect" (modifier: "a little")
		<li> E (bs) - "more leg room would have been perfect"
		<li> F - "Disappointed with the amount of legroom compared with other trains" (modifiers: "the amount of", "compared with other trains")
		<li> G - "Disappointed with legroom compared with other trains" (modifier: "compared with other trains")
		<li> H - "Disappointed with the amount of legroom" (modifier: "the amount of")
		<li> I (bs) - "Disappointed with legroom"

	 * @param randomEdges -true for random edges, false for 'correct' edges
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
	
	/** The same as {@link EntailmentGraphRaw#getSampleOuput} with additional information
	 * on categories for use-case-2.
	 * @param randomEdges
	 * @return
	 */
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
}

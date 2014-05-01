package eu.excitementproject.tl.evaluation.graphmerger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.rawgraph.utils.TEDecisionWithConfidence;

/**
 * This is the class responsible for loading gold standard edges from manually annotated data
 * @author Lili Kotlerman
 *
 */
public class GoldStandardEdgesLoader {
	
	private boolean excludeSelfLoops = true; // will be true, if false not explicitly required  

	public void setExcludeSelfLoops(boolean excludeSelfLoops) {
		this.excludeSelfLoops = excludeSelfLoops;
	}

	private static final String DIRECT_EDGE_TYPE_STRING = "direct";
	private static final String CLOSURE_EDGE_TYPE_STRING = "clousure";

	private boolean loadClosure;
	

	protected Map<String,EntailmentRelation> edges;
	protected Map<String,String> nodeTextById;
	/**
	 * @return the nodeTextById
	 */
	public Map<String, String> getNodeTextById() {
		return nodeTextById;
	}

	Set<String> nodesOfInterest;
	
	Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader");

	/**
	 * @param withClosure - defines which of the merged xml files will be loaded - with or without transitive closure edges
	 */
	private void setMergedFileSuffix(boolean withClosure){
		loadClosure=withClosure;
	}
	
	public boolean isValidMergedFile(String filename){
		if (!filename.endsWith(".xml")) return false;
		
		if (loadClosure){
			if (filename.contains("PlusClosure.xml")) return true;
			return false;
		}
		else {
			if (filename.contains("PlusClosure.xml")) return false;
			return true;			
		}
	}
	
	/** Loads all GS edges
	 */
	public GoldStandardEdgesLoader(boolean withClosure) {
		this(null, withClosure);
	}
	
	/** Loads GS edges connecting nodes of interest
	 * @param nodesOfInterest - Used to only load edges between the nodes in this set. Other edges will be ignored. 
	 */
	public GoldStandardEdgesLoader(Set<String> nodesOfInterest, boolean withClosure) {
		setMergedFileSuffix(withClosure);
		edges = new HashMap<String,EntailmentRelation>();
		nodeTextById = new HashMap<String,String>(); //[id] [text]
		this.nodesOfInterest = nodesOfInterest;
	}

	/**
	 * @return the edges (including transitive)
	 */
	public Set<EntailmentRelation> getEdges() {
		return new HashSet<EntailmentRelation>(edges.values());
	}

	/**
	 * @param annotationsFolder
	 * @param loadFragmentGraphs - set =true to verify FGs are consistent with the merged xml (or in case merge xml does not include all the FGs) 
	 * @throws GraphEvaluatorException
	 */
	public void loadAllAnnotations(String annotationsFolder, boolean loadFragmentGraphs) throws GraphEvaluatorException{
		File mainAnnotationsDir = new File(annotationsFolder);
		if (mainAnnotationsDir.isDirectory()){
			logger.debug("Loading GS annotations from folder "+annotationsFolder);			
			for (String object : mainAnnotationsDir.list()){
				// get sub-directories				
				File clusterAnnotationDir = new File(mainAnnotationsDir+"/"+object);
				if (clusterAnnotationDir.isDirectory()){
					loadClusterAnnotations(clusterAnnotationDir.getAbsolutePath(), loadFragmentGraphs);
					
/*					if (loadFragmentGraphs){
						// go to the corresponding "FragmentGraphs" folder and load all the fragment graphs 
						// important: the annotation of merge-step edges does not list nodes, which are not connected to other fragment graphs
						File clusterAnnotationFragmentGraphsDir = new File (clusterAnnotationDir+"/"+"FragmentGraphs");
						if (clusterAnnotationFragmentGraphsDir.isDirectory()){
							logger.debug("Loading fragment graph annotations for cluster "+clusterAnnotationDir);
							int fgid=1;
							for (File annotationFile : clusterAnnotationFragmentGraphsDir.listFiles()){
								if (annotationFile.getName().endsWith(".xml")){
									logger.debug("Fragment graph # "+fgid);
									try {
										ClusterStatistics.processCluster(annotationFile);
									} catch (ParserConfigurationException | SAXException | IOException e) {							
										e.printStackTrace();
									}
									addAnnotationsFromFile(annotationFile.getPath());
									fgid++;
								}
							}							
						}
						else System.err.println("The directory " + clusterAnnotationDir +"does not contain the \"FragmentGraphs\" sub-directory with fragment graph annotations.");						
					}

					// now load merge-graph annotations	
					// each sub-directory should contain a folder called "FinalMergedGraph" with a single xml file with annotations (or two files - *.xml or *PlusClosure.xml)
					File clusterAnnotationMergedGraphDir = new File (clusterAnnotationDir+"/"+"FinalMergedGraph");
					if (clusterAnnotationMergedGraphDir.isDirectory()){
						for (File annotationFile : clusterAnnotationMergedGraphDir.listFiles()){
							if (annotationFile.getName().endsWith(MERGED_XML_SUFFIX)){
								logger.debug(">>>>Loading merge annotations from file "+annotationFile);
								addAnnotationsFromFile(annotationFile.getPath());
								try {
									ClusterStatistics.processCluster(annotationFile);
								} catch (ParserConfigurationException | SAXException | IOException e) {							
									e.printStackTrace();
								}
							}
						}	
					}*/
				}
			}
		}
		else throw new GraphEvaluatorException("Invalid directory with gold-standard annotations in given: " + annotationsFolder);
	}
		

	/**
	 * @param annotationsFolder
	 * @param loadFragmentGraphs - set =true to verify FGs are consistent with the merged xml (or in case merge xml does not include all the FGs)
	 * @throws GraphEvaluatorException
	 */
	public void loadClusterAnnotations(String annotationsFolder, boolean loadFragmentGraphs) throws GraphEvaluatorException{
		File clusterAnnotationDir = new File(annotationsFolder);
		if (clusterAnnotationDir.isDirectory()){
			
			if (loadFragmentGraphs){
				// go to the corresponding "FragmentGraphs" folder and load all the fragment graphs 
				// important: the annotation of merge-step edges does not list nodes, which are not connected to other fragment graphs
				File clusterAnnotationFragmentGraphsDir = new File (clusterAnnotationDir+"/"+"FragmentGraphs");
				if (clusterAnnotationFragmentGraphsDir.isDirectory()){
					logger.info("Loading fragment graph annotations for cluster "+clusterAnnotationDir);
					int fgid=1;
					for (File annotationFile : clusterAnnotationFragmentGraphsDir.listFiles()){
						if (annotationFile.getName().endsWith(".xml")){
							logger.debug("Fragment graph # "+fgid);
									try {
										ClusterStatistics.processCluster(annotationFile);
									} catch (ParserConfigurationException | SAXException | IOException e) {							
										e.printStackTrace();
									}
									addAnnotationsFromFile(annotationFile.getPath(), true);
							fgid++;
						}
					}							
				}
				else System.err.println("The directory " + clusterAnnotationDir +"does not contain the \"FragmentGraphs\" sub-directory with fragment graph annotations.");				
			}
			
			// now load merge-graph annotations	
			// clusterAnnotationDir should contain a folder called "FinalMergedGraph" with a single xml file with annotations
			File clusterAnnotationMergedGraphDir = new File (clusterAnnotationDir+"/"+"FinalMergedGraph");
			if (clusterAnnotationMergedGraphDir.isDirectory()){
				for (File annotationFile : clusterAnnotationMergedGraphDir.listFiles()){
					if (isValidMergedFile(annotationFile.getName())){
					logger.info("Loading merge annotations from file "+annotationFile);
					addAnnotationsFromFile(annotationFile.getPath(), false);
							try {
								ClusterStatistics.processCluster(annotationFile);
							} catch (ParserConfigurationException | SAXException | IOException e) {							
								e.printStackTrace();
							}
					}
				}
			}									
		}
		else throw new GraphEvaluatorException("Invalid directory with gold-standard annotations in given: " + annotationsFolder);
	}
	
	
	
	public void addAnnotationsFromFile(String xmlAnnotationFilename, boolean isFragmentGraphAnnotation) throws GraphEvaluatorException{		
		// read all the nodes from xml annotation file and add them to the index 
	   		try {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(new File(xmlAnnotationFilename));
   
					doc.getDocumentElement().normalize();	     
					doc.getDocumentElement().getNodeName();
					NodeList nodes = doc.getElementsByTagName("node");
					
					// add nodes to the dictionary nodeTextById
					for (int temp = 0; temp < nodes.getLength(); temp++) {    
						Node xmlNode = nodes.item(temp);     

						Element nodeElement = (Element) xmlNode;
						String id = nodeElement.getAttribute("id");
						NodeList xmlChildNodes = xmlNode.getChildNodes();
				       	for (int i = 0; i < xmlChildNodes.getLength(); i++) {    
				       		Node child = xmlChildNodes.item(i);
				       		if (child.getNodeName().equals("original_text")){
							   	String text = child.getTextContent();
				       			if (nodesOfInterest!=null){
				       				if (!nodesOfInterest.contains(text)) continue; // don't add nodes which are not of interest, if nodesOfInterest is not null
				       			}
							   	nodeTextById.put(id, text);				       			
				       			//if (id.endsWith("_0")) System.out.println(text);
				       			logger.debug("\t"+id+"\t"+text);
				       		}
				       	}
					}   										
					
					// load all the edges
					NodeList entailmentRelationList = doc.getElementsByTagName("edge");
					for (int temp = 0; temp < entailmentRelationList.getLength(); temp++) {    
						Node er = entailmentRelationList.item(temp);     
						er.getNodeName();     
						Element erElement = (Element) er;
						String src = erElement.getAttribute("source");
						if (!nodeTextById.containsKey(src)) {
							// if nodesOfInterest==null, then we have a buggy behavior of the annotation file
							// if nodesOfInterest!=null, then this is likely to be because src is not one of the nodesOfInterest, so no error message needed
							if (nodesOfInterest==null) logger.error("Annotation file "+xmlAnnotationFilename+" contains an edge with source node "+ src+ ", which is not presented in the nodes list");
							continue;
						}
						String tgt = erElement.getAttribute("target");
						if (!nodeTextById.containsKey(tgt)) {
							if (nodesOfInterest==null) logger.error("Annotation file "+xmlAnnotationFilename+" contains an edge with target node "+ tgt+ ", which is not presented in the nodes list");
							continue;
						}
						
						EdgeType type = EdgeType.MANUAL_ANNOTATION;						
						if (isFragmentGraphAnnotation) type = EdgeType.FRAGMENT_GRAPH;
						else{
							NodeList features = erElement.getChildNodes();
							for (int i =0; i<features.getLength(); i++){
								if (features.item(i).getNodeName().equals("entailment")){
									Element erEntailment = (Element) features.item(i);
									if(erEntailment.getAttribute("type").equals(DIRECT_EDGE_TYPE_STRING)) type = EdgeType.DIRECT;
									else if(erEntailment.getAttribute("type").equals(CLOSURE_EDGE_TYPE_STRING)) type = EdgeType.TRANSITIVE_CLOSURE;
								}								
							}							
						}

						EntailmentUnit sourceUnit = getGoldStandardNode(nodeTextById.get(src)); 
						if (excludeSelfLoops){ // GS contains edges between nodes with the same text, when the nodes originate from different fragments. In out graphs we have those at one node, so need to exclude "loop" annotations from the GS for our evaluations
							if (sourceUnit.isTextIncludedOrRelevant(nodeTextById.get(tgt))) continue; 
						}
						EntailmentUnit targetUnit = getGoldStandardNode(nodeTextById.get(tgt));
						EntailmentRelation edge = getGoldStandardEdge(sourceUnit, targetUnit, type);
						edges.put(edge.toString(),edge); // for some reason "equals" method of EntailmentRelation does not recognize the edges returned by getGoldStandardEdge(sourceUnit, targetUnit) for same source and target texts as equal, to overcome this we use map instead of set, with edge's toString() as keys, since toString() outputs will be equal in our case						
					}
				} catch (ParserConfigurationException | SAXException | IOException e) {
					throw new GraphEvaluatorException("Problem loading annotations from file "+ xmlAnnotationFilename+ ".\n" + e.getMessage());
				}		
	}	


   //	Methods for internal testing purposes
	
	/** Generates a single string, which contains the gold standard edges in DOT format for visualization
	 * @return the generated string
	 */
	public String toDOT(){
		String s = "digraph gsGraph {\n";
		for (EntailmentRelation edge : edges.values()){
			s+=edge.toDOT();
		}
		s+="}";	
		return s;
	}
			
	public EntailmentGraphRaw getRawGraph(){
		EntailmentGraphRaw g = new EntailmentGraphRaw();
		for (String v : nodeTextById.values()){
			g.addVertex(getGoldStandardNode(v)); // the EUs should be the same as created when adding edges to the "edges" attribute of the class 
		}
		for (EntailmentRelation e : edges.values()){
			g.addEdge(e.getSource(), e.getTarget(), e);
		}
		return g;
	}
	
	public EntailmentGraphRaw getFragmentGraph(Set<String> idsOfInterest){
		EntailmentGraphRaw g = new EntailmentGraphRaw();
		for (String id : nodeTextById.keySet()){
			if (!idsOfInterest.contains(id)) continue;
			g.addVertex(getGoldStandardNode(nodeTextById.get(id))); // the EUs should be the same as created when adding edges to the "edges" attribute of the class
		//	System.out.println("Added node "+nodeTextById.get(id));
		}
		for (EntailmentRelation e : edges.values()){
			try {
				g.addEdge(e.getSource(), e.getTarget(), e);
			} catch (IllegalArgumentException e1) {
				// System.out.println("Edge not from current FG: "+ e);
			}
		}
		g.applyTransitiveClosure(false);
		return g;
	}

	public EntailmentGraphCollapsed getCollapsedGraph() throws GraphOptimizerException {
		SimpleGraphOptimizer opt = new SimpleGraphOptimizer();
		EntailmentGraphCollapsed g = opt.optimizeGraph(getRawGraph(),0.0);
		g.applyTransitiveClosure(false);
		return g;
	}
	
	public static EntailmentUnit getGoldStandardNode(String text){
		 EntailmentUnit eu = new EntailmentUnit(text, -1, "", "unknown"); // "-1" level means "unknown", put "" as complete statement text, since only the text of the node is compared when comparing edges
		 return eu;
		 
	//	 return new EntailmentUnit(eu.getTextWithoutDoubleSpaces(), -1, "", "unknown");
		//TODO:	do this carefully so that nodeTextById also contains texts without double spaces, as well as nodesOfInterest 
	}
	
	protected EntailmentRelation getGoldStandardEdge(EntailmentUnit sourceUnit, EntailmentUnit targetUnit){
		return new EntailmentRelation(sourceUnit, targetUnit, new TEDecisionWithConfidence(1.0, DecisionLabel.Entailment), EdgeType.MANUAL_ANNOTATION);
	}
	
	protected EntailmentRelation getGoldStandardEdge(EntailmentUnit sourceUnit, EntailmentUnit targetUnit, EdgeType type){
		return new EntailmentRelation(sourceUnit, targetUnit, new TEDecisionWithConfidence(1.0, DecisionLabel.Entailment), type);
	}

	public Set<String> getNodes(){
		return new HashSet<String>(nodeTextById.values());
	}
	
	/**
	 * Loads a graph with all FGs inside it (entailing and non-entailing edges explicitly given), while other edges (merge edges) are not added
	 * @param annotationsFolder
	 * @throws GraphEvaluatorException
	 */
	public void loadFGrawGraph(String annotationsFolder) throws GraphEvaluatorException{
		File clusterAnnotationDir = new File(annotationsFolder);
		if (clusterAnnotationDir.isDirectory()){
			
		// go to the corresponding "FragmentGraphs" folder and load all the fragment graphs 
		// important: the annotation of merge-step edges does not list nodes, which are not connected to other fragment graphs
		File clusterAnnotationFragmentGraphsDir = new File (clusterAnnotationDir+"/"+"FragmentGraphs");
		if (clusterAnnotationFragmentGraphsDir.isDirectory()){
			logger.info("Loading fragment graph annotations for cluster "+clusterAnnotationDir);
			int fgid=1;
			for (File annotationFile : clusterAnnotationFragmentGraphsDir.listFiles()){
				if (annotationFile.getName().endsWith(".xml")){
					logger.debug("Fragment graph # "+fgid);
							try {
								ClusterStatistics.processCluster(annotationFile);
							} catch (ParserConfigurationException | SAXException | IOException e) {							
								e.printStackTrace();
							}
							addFGFromFile(annotationFile.getPath());
					fgid++;
				}
			}							
		}
		else System.err.println("The directory " + clusterAnnotationDir +"does not contain the \"FragmentGraphs\" sub-directory with fragment graph annotations.");				
		}
	}
	
	public void addFGFromFile(String xmlAnnotationFilename) throws GraphEvaluatorException{		
		// read all the nodes from xml annotation file and add them to the index 
	   		try {
	   			
	   			Set<String> idsInThisFG = new HashSet<String>();
	   			
				EdgeType type =  EdgeType.FRAGMENT_GRAPH;

				
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(new File(xmlAnnotationFilename));
   
					doc.getDocumentElement().normalize();	     
					doc.getDocumentElement().getNodeName();
					NodeList nodes = doc.getElementsByTagName("node");
					
					// add nodes to the dictionary nodeTextById
					for (int temp = 0; temp < nodes.getLength(); temp++) {    
						Node xmlNode = nodes.item(temp);     

						Element nodeElement = (Element) xmlNode;
						String id = nodeElement.getAttribute("id");
						NodeList xmlChildNodes = xmlNode.getChildNodes();
				       	for (int i = 0; i < xmlChildNodes.getLength(); i++) {    
				       		Node child = xmlChildNodes.item(i);
				       		if (child.getNodeName().equals("original_text")){
							   	String text = child.getTextContent();
							   	nodeTextById.put(id, text);	
							   	idsInThisFG.add(id);
				       			logger.debug("\t"+id+"\t"+text);
				       		}
				       	}
					}   										
					
					// load all the "YES" edges
					NodeList entailmentRelationList = doc.getElementsByTagName("edge");
					for (int temp = 0; temp < entailmentRelationList.getLength(); temp++) {    
						Node er = entailmentRelationList.item(temp);     
						er.getNodeName();     
						Element erElement = (Element) er;
						String src = erElement.getAttribute("source");
						if (!idsInThisFG.contains(src)) {
							// if nodesOfInterest==null, then we have a buggy behavior of the annotation file
							// if nodesOfInterest!=null, then this is likely to be because src is not one of the nodesOfInterest, so no error message needed
							continue;
						}
						String tgt = erElement.getAttribute("target");
						if (!idsInThisFG.contains(tgt)) {
							continue;
						}
						
						EntailmentUnit sourceUnit = getGoldStandardNode(nodeTextById.get(src)); 
						EntailmentUnit targetUnit = getGoldStandardNode(nodeTextById.get(tgt));
						EntailmentRelation edge = getGoldStandardEdge(sourceUnit, targetUnit, type);
						edges.put(edge.toString(),edge); // for some reason "equals" method of EntailmentRelation does not recognize the edges returned by getGoldStandardEdge(sourceUnit, targetUnit) for same source and target texts as equal, to overcome this we use map instead of set, with edge's toString() as keys, since toString() outputs will be equal in our case						
					}
					
			//		System.out.println(">>>>>Loading FG's positive edges");
					EntailmentGraphRaw rfg = getFragmentGraph(idsInThisFG);
					
					// now add all "NO" edges - we're inside a FG, so for each pair of nodes, if it's not "yes" then it's "no"
					for (String sid : idsInThisFG){
						for (String tid : idsInThisFG){
							if (sid.equals(tid)) continue;
							EntailmentUnit sourceUnit = rfg.getVertexWithText(nodeTextById.get(sid)); 
							EntailmentUnit targetUnit = rfg.getVertexWithText(nodeTextById.get(tid));
							boolean foundYesEdges = false;
							for (EntailmentRelation e : rfg.getAllEdges(sourceUnit, targetUnit)){
								if (e.getLabel().equals(DecisionLabel.Entailment)) {
									foundYesEdges = true;
									break;
								}
							}
							if (!foundYesEdges){ // if there's no "yes" edge between current src and tgt
								EntailmentRelation noEdge = new EntailmentRelation(sourceUnit, targetUnit, new TEDecisionWithConfidence(1.0, DecisionLabel.NonEntailment), type);
								edges.put(noEdge.toString(),noEdge); // for some reason "equals" method of EntailmentRelation does not recognize the edges returned by getGoldStandardEdge(sourceUnit, targetUnit) for same source and target texts as equal, to overcome this we use map instead of set, with edge's toString() as keys, since toString() outputs will be equal in our case
								System.out.println("NO_EDGE_ADDED: "+noEdge);
							}
						}
					}
					
				} catch (ParserConfigurationException | SAXException | IOException e) {
					throw new GraphEvaluatorException("Problem loading annotations from file "+ xmlAnnotationFilename+ ".\n" + e.getMessage());
				}		
	}	
	
}

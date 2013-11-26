package eu.excitementproject.tl.evaluation.graphmerger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
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
	
	Set<EntailmentRelation> edges;
	Map<String,String> nodeTextById;	
	
	public GoldStandardEdgesLoader() {
		edges = new HashSet<EntailmentRelation>();
		nodeTextById = new HashMap<String,String>(); //[id] [text]	
	}
	

	/**
	 * @return the edges
	 */
	public Set<EntailmentRelation> getEdges() {
		return edges;
	}

	public void addAnnotations(String xmlAnnotationFilename) throws GraphEvaluatorException{		
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
				       			nodeTextById.put(id, text);
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
						if (!nodeTextById.containsKey(src)) throw new GraphEvaluatorException("Annotation file "+xmlAnnotationFilename+" contains and edge with source node "+ src+ ", which is not presented in the nodes list");
						String tgt = erElement.getAttribute("target");
						if (!nodeTextById.containsKey(tgt)) throw new GraphEvaluatorException("Annotation file "+xmlAnnotationFilename+" contains and edge with target node "+ tgt+ ", which is not presented in the nodes list");
						EntailmentUnit sourceUnit = new EntailmentUnit(nodeTextById.get(src), -1, "", "unknown"); // "-1" level means "unknown", put "" as complete statement text, since only the text of the node is compared when comparing edges
						EntailmentUnit targetUnit = new EntailmentUnit(nodeTextById.get(tgt), -1, "", "unknown"); 
						edges.add(new EntailmentRelation(sourceUnit, targetUnit, new TEDecisionWithConfidence(1.0, DecisionLabel.Entailment), EdgeType.MANUAL_ANNOTATION));
					}
				} catch (ParserConfigurationException | SAXException | IOException e) {
					throw new GraphEvaluatorException("Problem loading annotations from file "+ xmlAnnotationFilename+ ".\n" + e.getMessage());
				}		
	}	

	/** Generates a single string, which contains the gold standard edges in DOT format for visualization
	 * @return the generated string
	 */
	public String toDOT(){
		String s = "digraph gsGraph {\n";
		int i=1;
		for (EntailmentRelation edge : edges){
			s+=edge.toDOT();
			System.out.println(i+": "+edge);
			i++;
			if(i==1000) break;
		}
		s+="}";	
		return s;
	}
	
	public static void main(String[] args) {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader();
//		String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/_annotationExample.xml";
		String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/email0020.xml";
		try {
			loader.addAnnotations(annotationFilename);
			BufferedWriter out = new BufferedWriter(new FileWriter(annotationFilename+".dot"));
			out.write(loader.toDOT());
			out.close();			
		} catch (GraphEvaluatorException | IOException e) {
}

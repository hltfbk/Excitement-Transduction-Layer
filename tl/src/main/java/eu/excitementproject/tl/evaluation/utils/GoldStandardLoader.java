package eu.excitementproject.tl.evaluation.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sleepycat.je.rep.impl.RepGroupDB.NodeBinding;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.rawgraph.utils.TEDecisionWithConfidence;

/**
 * This is the class responsible for loading gold standard data
 * @author Lili Kotlerman
 *
 */
public class GoldStandardLoader {
	
	public static Set<EntailmentRelation> loadGoldStandardEdges(Set<EntailmentRelation> previouslyLoadedEdges, String xmlAnnotationFilename){
		Set<EntailmentRelation> edges = new HashSet<EntailmentRelation>();
		if (previouslyLoadedEdges != null)  edges.addAll(previouslyLoadedEdges);
		
		// read all the nodes from xml annotation file and create their index 
		Map<String,String> nodeTextById = new HashMap<String,String>(); //[id] [text]
	   			try {
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(new File(xmlAnnotationFilename));
   
					doc.getDocumentElement().normalize();	     
					doc.getDocumentElement().getNodeName();
					NodeList nodes = doc.getElementsByTagName("node");
					
					// add nodes to the dictionary nodeTextById
					for (int temp = 0; temp < nodes.getLength(); temp++) {    
						Node node = nodes.item(temp);     
						//node.getNodeName();   

						Element nodeElement = (Element) node;
						String text = nodeElement.getAttribute("original_text");
						String id = nodeElement.getAttribute("id");
					   	nodeTextById.put(id, text);
					}   	
					
					
					
					// load all the edges
					NodeList entailmentRelationList = doc.getElementsByTagName("edge");
					for (int temp = 0; temp < entailmentRelationList.getLength(); temp++) {    
						Node er = entailmentRelationList.item(temp);     
						er.getNodeName();     
						Element erElement = (Element) er;
						String source = erElement.getAttribute("source");
						String target = erElement.getAttribute("target");
					}
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		
		return edges;
	}

}

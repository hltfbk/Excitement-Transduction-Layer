package eu.excitementproject.tl.laputils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node; 
import org.xml.sax.SAXException;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.tl.structures.Interaction;

/**
 * 
 * This class provides a simple reader for WP2 defined Interaction XML file.
 * Also, the class provides a not-so-simple reader of WP2 fragment graph annotation dump. 
 * 
 * @author Gil
 *
 */
public final class InteractionReader {

	/**
	 * This static utility method gets one File, and read in the interactions written in the file as a list of {@link Interaction} object. 
	 *  
	 * @param f XML file (WP2-defined XML file) for Interaction. 
	 * @return List<Interaction> - A list of interaction objects.  
	 */
	public static List<Interaction> readInteractionXML(File xmlFile) throws Exception
	{
		// NOTE ON the WP2 interaction data format 
//		// * dataset  
//		- provider
//		- channel
//		- language
//
//		** interactions [?]
//
//		*** interaction [?+] 
//		**** metadata
//		- businessScenario (String: some string) 
//		- dataSource (string: company name?) 
//		- id (string?: generally not set, all 1) 
//		- date (string? : also not properly set) 
//		- category (empty for NICE) 
//		**** text
//		(just plain text) 
		
		// Open XML file 
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			dom = db.parse(xmlFile);

		}catch(ParserConfigurationException pce) {
			throw new Exception("unable to generate the XML parser", pce);
		}catch(SAXException se) {
			throw new Exception("unable to parse the XML input file", se);
		}catch(IOException ioe) {
			throw new Exception("unable to access the input file", ioe);
		}	
		
		// open the document <dataset> (top) 
		Element dataset = dom.getDocumentElement(); 
		
		// first, read in the metadata attached to the dataset. 
		Element providerE = (Element) dataset.getElementsByTagName("provider").item(0); 
		Element channelE = (Element) dataset.getElementsByTagName("channel").item(0);
		Element languageE = (Element) dataset.getElementsByTagName("language").item(0);

		// common metadata for all interactions of this file. 
		String provider = providerE.getFirstChild().getNodeValue(); 
		String channel = channelE.getFirstChild().getNodeValue(); 
		String lang = languageE.getFirstChild().getNodeValue(); 
		
		Element interactions = (Element) dataset.getElementsByTagName("interactions").item(0); 
		NodeList interactionNodes = interactions.getElementsByTagName("interaction");

		// the stage is ready with interactionNodes. 
		// loop them over, generate one Interaction per <Interaction> 
		ArrayList<Interaction> interactionList= new ArrayList<Interaction>(); 
		for(int i=0; i < interactionNodes.getLength(); i++)
		{
			// get interaction String
			Element oneInteraction = (Element) interactionNodes.item(i); 
			String interactionText = oneInteraction.getElementsByTagName("text").item(0).getFirstChild().getNodeValue(); 
			
			// get metadata (for now, only cateogry, if exist) 
			
			// TODO: check we will use metadata like the followings: 
			// businessScenario (e.g. train, coffeehouse, etc) , 
			// dataSource (company name, prolly confidential), 
			// id (meaningless in our setup? - all 1 in the data) 
			// date (meaningless in our setup? - all 0001-01-01 in the data) 
		
			Element meta = (Element) oneInteraction.getElementsByTagName("metadata").item(0); 
			Node c = meta.getElementsByTagName("category").item(0).getFirstChild(); 
			String category = null; 
			if (c != null)
			{
				category = c.getNodeValue(); 
			}
			
			Interaction interaction = new Interaction(interactionText, lang, category, channel, provider); 
			interactionList.add(interaction); 			
		}
		
		return interactionList; 
	}
	
	/**
	 * 
	 * 
	 * @param interactionText
	 * @param graphsInXML
	 * @return
	 * @throws Exception
	 */
	public static JCas readWP2FragGraphDump(File interactionText, File graphsInXML) throws Exception
	{
		// TODO: fill in 
		return null; 
	}
	
}

package eu.excitementproject.tl.laputils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.DataIntegrityFail;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.structures.Interaction;

/**
 * 
 * This class provides a simple reader for OMQ categories.
 * 
 * @author Kathrin
 *
 */
public final class CategoryReader {

	/**
	 * This static utility method gets one File, and read in the interactions written in the file as a list of {@link Interaction} object. 
	 *  
	 *  <P> Note that if there is any "not-well-escaped sequence (as in ALMA social)", the XML parser will fail. Make sure the XML has no bad escapes. 
	 *  
	 * @param f XML file (WP2-defined XML file) for Interaction. 
	 * @return List<Interaction> - A list of interaction objects.  
	 */
	public static List<Interaction> readCategoryXML(File xmlFile) throws DataReaderException
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
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.laputils.InteractionReader");
		logger.info("Processing file " + xmlFile);
		
		try {
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			dom = db.parse(xmlFile);

		}catch(ParserConfigurationException pce) {
			throw new DataReaderException("unable to generate the XML parser", pce);
		}catch(SAXException se) {
			throw new DataReaderException("unable to parse the XML input file", se);
		}catch(IOException ioe) {
			throw new DataReaderException("unable to access the input file", ioe);
		}	
		
		System.out.println("Processing file ...");
		
		// open the document <dataset> (top) 
		Element dataset = dom.getDocumentElement(); 
		
		// first, read in the metadata attached to the dataset. 
		Element providerE = (Element) dataset.getElementsByTagName("provider").item(0); 
		Element channelE = (Element) dataset.getElementsByTagName("channel").item(0);
		Element languageE = (Element) dataset.getElementsByTagName("language").item(0);

		// common metadata for all interactions of this file. 
		/*
		String provider = providerE.getFirstChild().getNodeValue(); 
		String channel = channelE.getFirstChild().getNodeValue(); 
		String lang = languageE.getFirstChild().getNodeValue(); 
		*/
		String provider = "OMQ";
		String channel = "email";
		String lang = "DE"; //TODO: ask Alex to insert the data! 

		
		Element categories = (Element) dataset.getElementsByTagName("categories").item(0); 
		NodeList categoryGroupNodes = categories.getElementsByTagName("categoryGroup");

		// the stage is ready with interactionNodes. 
		// loop them over, generate one Interaction per <Interaction> 
		ArrayList<Interaction> interactionList= new ArrayList<Interaction>(); 
		for(int i=0; i < categoryGroupNodes.getLength(); i++)
		{
			// get category group element
			Element oneCategoryGroup = (Element) categoryGroupNodes.item(i); 
			
			NodeList categoryNodes = oneCategoryGroup.getElementsByTagName("category");
			String categoryText = "";
			String categoryId = "";
			
			for (int j=0; j < categoryNodes.getLength(); j++) {
				Node categoryNode = categoryNodes.item(j);
				categoryText = categoryNode.getTextContent();
				NamedNodeMap attributes = categoryNode.getAttributes();
				int numAttrs = attributes.getLength();
				for (int k = 0; k < numAttrs; k++) {
					Attr attr = (Attr) attributes.item(k);
					if (attr.getNodeName().equals("id")) categoryId = attr.getNodeValue();
				}				
				Interaction category = new Interaction(categoryText, categoryText, lang, "c_"+categoryId, categoryId, channel, provider, null); 
				interactionList.add(category); 			
			}						
		}
		
		return interactionList; 
	}	
}
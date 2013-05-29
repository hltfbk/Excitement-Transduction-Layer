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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node; 
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.DataIntegrityFail;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
//import eu.excitementproject.tl.laputils.CASUtils.Region;
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
	public static List<Interaction> readInteractionXML(File xmlFile) throws DataReaderException
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
			throw new DataReaderException("unable to generate the XML parser", pce);
		}catch(SAXException se) {
			throw new DataReaderException("unable to parse the XML input file", se);
		}catch(IOException ioe) {
			throw new DataReaderException("unable to access the input file", ioe);
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
	 * This reader reads WP2 "human annotated fragment graph annotation dump" data, and fills in the given CAS with the "interaction" with "fragment" annotation. Also adds "modifiers" annotations, if any.  
	 *  - interaction as SOFA text of CAS.   
	 *  - fragment as fragment annotation. (Note that it is determinedFragment --- due to this is human decision) 
	 *  - modifier annotations if any. 
	 *  
	 *  <P> The resulting CAS is ready to be fed to the FragmentGraph generator.  
	 *  
	 *  <P> <B> Warning: WP2 graph dump does not have language ID. So this method does not and cannot fill languageID of the CAS. Thus the caller need to set the language ID of the CAS, by calling aJCas.setDocumentLanguage()
	 *  
	 * @param interactionRawText A raw text file that holds the interaction string. 
	 * @param graphsInXML The graphs dump, as defined in WP2 public data (the XML holds one <F_entailment_graph>) 
	 * @param JCas a JCas object: This JCAS will be filled in with interaction text, fragment annotation and modifier annotation. Note that if the interaction text is "different" from the already existing SOFA text (CAS document text), the JCAS object will be reset. Otherwise, the reader method will only "add" annotations.  
	 * @return
	 * @throws DataReaderException Generic errors (file not found, XML malformed, etc) will be thrown with this one. 
	 * @throws DataIntegrityFail If something is logically wrong in the files --- this exception is thrown: for example, fragemnt original text is given, but not found in interaction text, etc. 
	 */
	public static void readWP2FragGraphDump(File interactionText, File graphsInXML, JCas aJCas) throws DataReaderException, DataIntegrityFail
	{
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.laputils"); 
		
		// read the interaction file whole as a string. 
		String interactionString = ""; 
		
		try {
			List<String> s = Files.readAllLines(interactionText.toPath(), Charset.defaultCharset()); 
			for(int i=0; i < s.size(); i++)
			{
				interactionString = interactionString + s.get(i); 
			}
		}
		catch(IOException e)
		{
			throw new DataReaderException("unable to read interaction raw file",e); 
		}
		testlogger.debug("Content of the interaction raw file:");
		testlogger.debug(interactionString); 			
		
		// read XML, fetch:
		// first node. 
		//     - original text 
		//     - modifier 
		
		// Open XML file 
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			//parse using builder to get DOM representation of the XML file
			dom = db.parse(graphsInXML);

		}catch(ParserConfigurationException pce) {
			throw new DataReaderException("unable to generate the XML parser", pce);
		}catch(SAXException se) {
			throw new DataReaderException("unable to parse the XML input file", se);
		}catch(IOException ioe) {
			throw new DataReaderException("unable to access the input file", ioe);
		}	

		// open the document <F_entailment_graph> (the top) 
		Element dataset = dom.getDocumentElement(); 
		
		// we only need the first node, which has the fragment and all modifiers
		Element topnode = (Element) dataset.getElementsByTagName("node").item(0); 
		
		// fetch "original text", and "modifiers" of the top node, 
		Node original_text_node = topnode.getElementsByTagName("original_text").item(0); 
		Node modifiers_node = topnode.getElementsByTagName("modifiers").item(0); 
		
		// sanity check 
		if (original_text_node == null) 
		{
			throw new DataIntegrityFail("XML file does not have original_text in top node"); 
		}
		if (modifiers_node == null) 
		{
			throw new DataIntegrityFail("XML file does not have modifier element in top node; ill formed: at least <modifiers/> should be there."); 
		}

		String original_text = original_text_node.getFirstChild().getNodeValue(); 
		String modifiers = null; 
		if (modifiers_node.getFirstChild() != null)
		{
			modifiers = modifiers_node.getFirstChild().getNodeValue(); 
		}
		
		testlogger.debug("Content of the fragment <original_text>:");
		testlogger.debug(original_text); 			

		testlogger.debug("Content of the <modifiers>:");
		testlogger.debug(modifiers); 			
		
		
		// Match original text, and set it as fragment 
		// Match modifier, set it as modifiers 
		if (!interactionString.contains(original_text))
		{
			// The interaction raw text does not have this fragment 
			// original text. Something is a missing (e.g. wrong XML, or wrong raw interaction) 

			testlogger.info("Integrity fail: throwing an exception. The interaction raw text does not contain fragment text.");
			testlogger.info("Content of the fragment <original_text>:");
			testlogger.info(original_text); 			
			testlogger.info("Content of the interaction raw file:");
			testlogger.info(interactionString); 			
			throw new DataIntegrityFail("The interaction raw text does not contain fragment original text -- can be caused by non-continous fragment. Currently the reader does not non-continuous fragments yet."); 
			
			// TODO [#A] Code for non-continuous fragment matching :-( Non-trivial. 
			
		}
		// it contains, so the following will always be meaningful. 
		int frag_start = interactionString.indexOf(original_text); 
		int frag_end = frag_start + original_text.length(); 

		// Okay, let's annotate the fragment 
		
		// prepare CAS
		// Note that we can't set language ID, cause the file format doesn't have one. 
		// We reset CAS only if the interaction text is differ from existing CAS SOFA text. 
		String SOFAText = aJCas.getDocumentText();
		
		if (SOFAText == null)
		{
			testlogger.debug("Cleaning CAS and set interaction text."); 
			aJCas.reset(); 
			aJCas.setDocumentText(interactionString); 
		}
		else if (!SOFAText.equals(interactionString))  
		{
			testlogger.debug("Cleaning CAS and set interaction text."); 
			aJCas.reset(); 
			aJCas.setDocumentText(interactionString); 
		}
		else
		{
			testlogger.debug("Same interaction text already in the CAS. Only adding annotations."); 			
		}

		// annotate Fragment 
		CASUtils.Region[] r = new CASUtils.Region[1]; 
		r[0] = new CASUtils.Region(frag_start, frag_end);

		// annotate the modifier 			
		testlogger.debug("Annotating fragment:"); 
		testlogger.debug("Text: " + original_text + ", begin: " + frag_start + ", end: " + frag_end ); 			

		try {
			CASUtils.annotateOneDeterminedFragment(aJCas,  r); 
		}
		catch (LAPException e)
		{
			throw new DataReaderException("Failed to annotate CAS - CASUtils reported exception while trying to annotate DeterminedFragment", e); 
		}
		
		// prepare modifiers. First divide the modifier string, since it can
		// have multiple modifiers init. 
		// e.g. 		
        // <modifiers>ladiespoz=2;at Stars Bridgepoz=4 5 6</modifiers>

		if (modifiers != null)
		{
			String[] modpoz_data = modifiers.split(";"); 
			for (String modpoz : modpoz_data)
			{
				String[] m = modpoz.split("poz="); 
				String mod = m[0]; 
			
				if (!original_text.contains(mod))
				{
					// original text does not have modifier string. Something wrong. 
					testlogger.info("Integrity fail: throwing an exception. The fragment text does not contain modifier text.");
					testlogger.info("Content of the fragment <original_text>:");
					testlogger.info(original_text); 			
					testlogger.info("The problematic modifier");
					testlogger.info(mod); 			

					throw new DataIntegrityFail("Something is wrong. The fragment text does not contain modifier text."); 			
				}
				// match and convert the location to "Interaction" level index. 
				// TODO : note that if the fragment holds two same modifiers, this can lead to wrong match. 
				// one solution would be using poz value... but that has its own problem. So for now, going this way. 
			
				int temp_begin = original_text.indexOf(mod); 
				int temp_end = temp_begin + mod.length(); 
				int mod_begin = temp_begin + frag_start; 
				int mod_end = temp_end + frag_start; 

				// annotate the modifier 			
				testlogger.debug("Annotating modifier:"); 
				testlogger.debug("Text: " + mod + ", begin: " + mod_begin + ", end: " + mod_end ); 			

				r = new CASUtils.Region[1]; 
				r[0] = new CASUtils.Region(mod_begin, mod_end);
				try { 
					CASUtils.annotateOneModifier(aJCas, r); 
				}
				catch (LAPException e)
				{
					throw new DataReaderException("CASUtil report failure while trying to annotate modifier annotations", e); 
				}
			}	// end for each modpoz 
		} // end if modifier != null 
		else
		{
			testlogger.debug("This fragment node has no modifiers."); 
		}
	}
	
}

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
	 *  <P> Note that if there is any "not-well-escaped sequence (as in ALMA social)", the XML parser will fail. Make sure the XML has no bad escapes. 
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
			
			// TODO (low priority) check we will use metadata like the followings, or not.  
			// businessScenario (e.g. train, coffeehouse, etc) , 
			// dataSource (company name, prolly confidential), 
			// date (meaningless in our setup? - all 0001-01-01 in the data) 
			
			// get the keywords
			String keywords = getKeywords(oneInteraction);
			
			Element meta = (Element) oneInteraction.getElementsByTagName("metadata").item(0); 
			Node c = meta.getElementsByTagName("category").item(0).getFirstChild(); 
			String category = null;
			String interactionId = null; 
			if (c != null)
			{
				category = c.getNodeValue(); 
			}
			
			Node idval = meta.getElementsByTagName("id").item(0).getFirstChild(); 
			if (idval != null)
			{
				interactionId = idval.getNodeValue(); 
			}
			
			Interaction interaction = new Interaction(interactionText, lang, interactionId, category, channel, provider, keywords); 
			interactionList.add(interaction); 			
		}
		
		return interactionList; 
	}
	
	private static String getKeywords(Element oneInteraction) {

		String keywords = null;
		
		NodeList elements = oneInteraction.getElementsByTagName("keyword");
				
		if (elements != null && elements.getLength() > 0) {
			keywords = oneInteraction.getElementsByTagName("keyword").item(0).getFirstChild().getNodeValue();
		}
		
		return keywords;
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
	 *  <P> Known problem:
	 *  Non-continuous fragment must be mapped from raw texts. (no mapping info given in the file). This is not trivial problem. 
	 *  It works okay in current implementation, but some times it fails. For example: 
	 *  <P><I> Full text: "XXCompany something not relevant, ... XXCompany is here but not really good".</I></P>
	 *  <P><I> Fragment written as: "XXCompany is not really good".
	 *  <P> Current implementation will map XXCompany to the first occurrence. No easy solution to really resolve this; something like full aligner would be needed. but for now, this would be an overkill. Leaving as it is. Non-continuous modifiers also have the same problem. (But much less, since there are only 10 non-cont-mods in the data)
	 *  
	 * @param interactionRawText A raw text file that holds the interaction string. 
	 * @param graphsInXML The graphs dump, as defined in WP2 public data (the XML holds one <F_entailment_graph>) 
	 * @param JCas a JCas object: This JCAS will be filled in with interaction text, fragment annotation and modifier annotation. Note that if the interaction text is "different" from the already existing SOFA text (CAS document text), the JCAS object will be reset. Otherwise, the reader method will only "add" annotations.  
	 * @param languageID Note that WP2 dump data does not have language ID, while CAS must have the language ID. thus, you have to supply a language ID. (e.g. "EN", "DE","IT" ...) 
	 * @return
	 * @throws DataReaderException Generic errors (file not found, XML malformed, etc) will be thrown with this one. 
	 * @throws DataIntegrityFail If something is logically wrong in the files --- this exception is thrown: for example, fragment original text is given, but not found in interaction text, etc. 
	 */
	public static void readWP2FragGraphDump(File interactionText, File graphsInXML, JCas aJCas, String languageID) throws DataReaderException, DataIntegrityFail
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
		
		// store the file name as ID - this will be annotated within tl.Metadata 
		// (with setting document text) 
		String interactionId = interactionText.getName(); 
		
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

		if ((modifiers != null) && (modifiers.trim().length() > 0 ))  // sometimes there are "empty space" modifiers, and we are checking this with trim() 
		{
			testlogger.debug("Content of the <modifiers>:");
			testlogger.debug(modifiers); 			
		}
		else
		{
			testlogger.debug("No or empty <modifiers>"); 						
		}
		
		
		// Match original text, and set it as fragment 
		int frag_start; 
		int frag_end; 
		CASUtils.Region[] r = null; 
		
		// If this is not a simple "one-region" fragment ... 
		if (!interactionString.contains(original_text))
		{
			// The interaction raw text does not have this fragment as it is. 
			// It can be a non continuous fragment. Let's try to align it. 
			r = alignBtoA(interactionString, original_text); // this method will return a list  
			
			if (r == null) // means, no such align was possible 
			{
				// if this mapping was not possible. 
				testlogger.info("Integrity fail: throwing an exception. The interaction raw text does not contain fragment text.");
				testlogger.info("Content of the fragment <original_text>:");
				testlogger.info(original_text); 			
				testlogger.info("Content of the interaction raw file:");
				testlogger.info(interactionString); 			
				throw new DataIntegrityFail("The interaction raw text does not contain fragment original text -- this can be caused by wrong tokenization (different tokenization on raw text and original text, etc), or changes in marking original_text."); 
			}
			else
			{
				// put frag_start, frag_end, from first r[0] and last r[size-1]. 
				frag_start = r[0].getBegin();  
				frag_end = r[r.length-1].getEnd(); 
				// r is already ready. 
			}
		}
		else // simple one region fragment 
		{
			// it contains, so the following will always be meaningful. 
			frag_start = interactionString.indexOf(original_text); 
			frag_end = frag_start + original_text.length(); 
			
			r = new CASUtils.Region[1]; 
			r[0] = new CASUtils.Region(frag_start, frag_end);
		}
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
			aJCas.setDocumentLanguage(languageID); 
			// annotate the metadata (among fraggraph dumpdata, only interactionID is available from interaction file name) 
			CASUtils.addTLMetaData(aJCas, interactionId, null, null, null, null, null, null); 

		}
		else if (!SOFAText.equals(interactionString))  
		{
			testlogger.debug("Cleaning CAS and set interaction text."); 
			aJCas.reset(); 
			aJCas.setDocumentText(interactionString); 
			aJCas.setDocumentLanguage(languageID); 
			// annotate the metadata (among fraggraph dumpdata, only interactionID is available from interaction file name) 
			CASUtils.addTLMetaData(aJCas, interactionId, null, null, null, null, null, null); 
		}
		else
		{
			testlogger.debug("The same interaction text already in the CAS. Only adding annotations."); 			
		}


		// now annotate via CASUtils.annotateOneDeterminedFragment 
		testlogger.debug("Annotating fragment:"); 
		testlogger.debug("Text: " + original_text + ", begin: " + frag_start + ", end: " + frag_end ); 			
		if (r.length > 1)
		{
			testlogger.debug("(This has a non-continuous fragment)"); 
			String s = "[ "; 
			for(CASUtils.Region x: r)
			{
				int b = x.getBegin(); 
				int e = x.getEnd(); 
				s = s + "(" + b + " - " + e + "), "; 
			}
			s += " ]"; 
			testlogger.debug(s); 
		}
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

		if ((modifiers != null) && (modifiers.trim().length() > 0 ))  // sometimes there are "empty space" modifiers, and we are checking this with trim() 
		{						
			String[] modpoz_data = modifiers.split(";"); 
			for (String modpoz : modpoz_data)
			{
				String[] m = modpoz.split("poz="); 
				String mod = m[0]; 
			
				// modifier match on fragment. 
				// Note that we need to convert the location to "Interaction" level index. 
				// TODO : (or not?) (Known problem) note that if the fragment holds two same modifiers, this can lead to wrong match. 
				// one solution would be using poz value... but that has its own problem. So for now, going this way. 
				
				int temp_begin=0;  // begin location on fragment
				int temp_end=0;    // end location on frag 
				int mod_begin=0;   // begin on document text 
				int mod_end=0;     // end on document text 

				CASUtils.Region[] mod_r = null; 

				if (!original_text.contains(mod))
				{
					// modifier text does not exist as it is. Try to check if it exist as 
					// "non-continuous way". 
					// It can be a non continuous fragment. Let's try to align it. 
					CASUtils.Region[] local_r = alignBtoA(original_text, mod); // this method will return a list  
					
					if (local_r == null) // this null array means that mapping wasn't possible. 
					{
						// Something wrong. 
						testlogger.info("Integrity fail: throwing an exception. The fragment text does not contain modifier text.");
						testlogger.info("Content of the fragment <original_text>:");
						testlogger.info(original_text); 			
						testlogger.info("The problematic modifier");
						testlogger.info(mod); 			

						throw new DataIntegrityFail("Something is wrong. The fragment text does not contain modifier text. "); 
					}
					else
					{
						// Okay. Let's do the "multi-part modifier match" 
						
						// for each region in the modifier, add "frag_strat" (local_r notes index on fragment text)
						// to retrieve "document index", we need to do this. 
						mod_r = new CASUtils.Region[local_r.length];
						for(int i=0; i < mod_r.length; i++)
						{
							mod_r[i] = new CASUtils.Region(local_r[i].getBegin() + frag_start, local_r[i].getEnd() + frag_start); 
						}
						// now mod_r is ready 
						// set mod_begin & mod_end 
						mod_begin = mod_r[0].getBegin(); 
						mod_end = mod_r[mod_r.length -1].getEnd(); 						
					}
				}
				else
				{
					// simple match case 
								
					temp_begin = original_text.indexOf(mod); 
					temp_end = temp_begin + mod.length(); 
					mod_begin = temp_begin + frag_start; 
					mod_end = temp_end + frag_start; 

					mod_r = new CASUtils.Region[1]; 
					mod_r[0] = new CASUtils.Region(mod_begin, mod_end);

				}
				
				// annotate the modifier 			
				testlogger.debug("Annotating modifier:"); 
				testlogger.debug("Text: " + mod + ", begin: " + mod_begin + ", end: " + mod_end ); 	
				
				if (mod_r.length > 1)
				{
					testlogger.debug("(This is a non-continuous modifier *2)"); 
					String s = "[ "; 
					for(CASUtils.Region x: mod_r)
					{
						int b = x.getBegin(); 
						int e = x.getEnd(); 
						s = s + "(" + b + " - " + e + "), "; 
					}
					s += " ]"; 
					testlogger.debug(s); 
				}

				try { 
					CASUtils.annotateOneModifier(aJCas, mod_r); 
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

	/**
	 * A utility method. Gets two string A and B, maps tokens (WS separated from string) of B to A; report mapping of each B tokens as the locations of A.  
	 * Mainly used for non-continuous fragment mapping 
	 * 
	 * Return null, if B cannot be fully mapped to A. 
	 * Return non-null, List<CASUtils.Region>, if one such mapping is found. 
	 * 
	 * Simple, Left first matching via WS-separation of two string. 
	 * @param A the longer string. (
	 * @param B the shorter string, which will be mapped to A. 
	 * @return
	 */
	public static CASUtils.Region[] alignBtoA(String a, String b) {
		
		//Logger testlogger = Logger.getLogger("eu.excitementproject.tl.laputils"); 
		//testlogger.debug("aligner input a: " + a); 
		//testlogger.debug("aligner input b: " + b); 
		
		// sanity check 
		if (a == null || b== null)
			return null;

		// split 
		String[] a_tokens = a.split("\\s+"); 
		String[] b_tokens = b.split("\\s+"); 
		
		// another sanity check 
		if (a_tokens.length == 0 || b_tokens.length == 0)
			return null; 
		
		// stores region of each token of A, on A string 
		CASUtils.Region[] a_tokens_regions = new CASUtils.Region[a_tokens.length]; 
		
		// result will be stored here 
		CASUtils.Region[] result = new CASUtils.Region[b_tokens.length];  

		// init a_tokens_regions (per each token, same index i)  
		int last_end_position = 0; 
		for(int i=0; i < a_tokens.length; i++)
		{
			int begin = a.indexOf(a_tokens[i], last_end_position); 
			int end = begin + a_tokens[i].length(); 
			a_tokens_regions[i] = new CASUtils.Region(begin, end); 
			last_end_position = end; 
		}

		
		// now iterate over a tokens, and match b. 
		int point_on_b= 0; 
		boolean all_matched = false; 
		for(int i=0; i < a_tokens.length; i++)
		{
			// slide over a tokens, while b pointer stays there 
			// unless there is a match. (b pointer only proceed if there is a match) 
			
			String token_a = a_tokens[i]; 
			String token_b = b_tokens[point_on_b]; 
			
			// some preprocessing to make sure ',' or '.' doesn't hinder match.  
			token_a = token_a.replaceAll("\\W", ""); 
			token_b = token_b.replaceAll("\\W", ""); 
			
			//if ( a_tokens[i].equals(b_tokens[point_on_b]) )
			if (token_a.equals(token_b))
			{
				// match found. push region of a_tokens[i] to result 
				result[point_on_b] = a_tokens_regions[i]; 
				// update b pointer 
				point_on_b ++; 
				if (point_on_b >= b_tokens.length)
				{
					all_matched = true; 
					break; // ran out of b
				}
			}
			// no match? nothing to do. 
		}
		
		
		// the method will return null, if such mapping was not possible				
		if (!all_matched)
			return null;  
		else
		{
			// sanity check 
			assert(result[b_tokens.length -1] != null); 
			return result; 
		}
	}	
}

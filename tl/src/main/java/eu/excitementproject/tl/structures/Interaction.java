package eu.excitementproject.tl.structures;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * This class defines a simple data structure that reflects WP2 data on
 * "one interaction". Note that this class is to be used just as a simple data holder without much functionality.  
 * Also note that internally in WP6, the standard notion of interaction representation is the InputCAS, as defined in the dataflow document.   
 * 
 * <P> This class is provided mainly to be used as one of "top level" argument, from WP7. See TopLevel API document for more detail. 
 * @author Gil
 *
 */

/**
 * @author Gil (modified by Kathrin to include relevantText and to allow several categories)
 *
 */
public class Interaction implements Comparable<Interaction> {
	
	static Logger logger = Logger.getLogger(Interaction.class.getName());

	/**
	 * Constructor for the data type. This constructor is "full" one. 
	 * 
	 * @param interactionString Whole interaction as one string. 
	 * @param langID language ID, following ISO standard (EN, DE, IT, etc) 
	 * @param channel channel of the interaction, free string, and depends on the application 
	 * @param provider
	 * @param category
	 * @param keywords -- an array of keywords for the interaction 
	 */

	
	public Interaction(String interactionString, String langID, String interactionId, String category, String channel, String provider, String keywords)
	{
		this.lang = langID; 
		this.interactionId = interactionId; 
		this.channel = channel; 
		this.provider= provider; 
		this.interactionString = interactionString; 	
		this.relevantTexts = null;
		this.categoryString = category;
		if (category == null) {
			this.categories = null;
		} else {
			this.categories = category.split(",");
		}
		if (keywords == null) {
			this.keywords = null;
		} else {
			this.keywords = keywords.split(",");
		}
	}


	/**
	 * Constructor for the data type. This constructor is "full" one (including relevantText). 
	 * 
	 * @param interactionString Whole interaction as one string. 
	 * @param relevantText relevant text within the full string
	 * @param langID language ID, following ISO standard (EN, DE, IT, etc) 
	 * @param channel channel of the interaction, free string, and depends on the application 
	 * @param provider
	 * @param category
	 * @param keywords -- an array of keywords for the interaction 
	 */
	public Interaction(String interactionString, List<RelevantText> relevantTexts, String langID, String interactionId, String category, String channel, String provider, String keywords) 
	{
		this.lang = langID; 
		this.interactionId = interactionId; 
		this.channel = channel; 
		this.provider= provider; 
		this.interactionString = interactionString; 	
		this.relevantTexts = relevantTexts;
		this.categoryString = category;
		if (category == null) {
			this.categories = null;
		} else {
			this.categories = category.split(",");
		}
		if (keywords == null) {
			this.keywords = null;
		} else {
			this.keywords = keywords.split(",");
		}
	}

	/**
	 * Constructor for the data type. This constructor is "full" one. 
	 * 
	 * @param interactionString Whole interaction as one string. 
	 * @param langID language ID, following ISO standard (EN, DE, IT, etc) 
	 * @param channel channel of the interaction, free string, and depends on the application 
	 * @param provider
	 * @param category 
	 */
	
	public Interaction(String interactionString, String langID, String interactionId, String category, String channel, String provider)
	{
		this(interactionString, langID, interactionId, category, channel, provider, null);
	}
	
	/**
	 * Minimal Constructor for Usecase 1. The constructor will only fill interaction and Language ID. channel, provider and category will be set as null.  If you need to set those metadata, use the full constructor.
	 * 
	 * @param interactionString Whole interaction as one string
	 * @param langID language ID, following ISO standard (EN, DE, IT, etc) 
	 */
	public Interaction(String interactionString, String langID)
	{
		this(interactionString, langID, null, null, null, null, null); 
	}
	
	/**
	 * Minimal Constructor for Usecase 1 with keywords. The constructor will only fill interaction and Language ID and keywords. channel, provider and category will be set as null.  If you need to set those metadata, use the full constructor.
	 * 
	 * @param interactionString Whole interaction as one string
	 * @param langID language ID, following ISO standard (EN, DE, IT, etc) 
	 */
	public Interaction(String interactionString, String langID, String keywords)
	{
		this(interactionString, langID, null, null, null, null, keywords); 
	}
	
	
	/**
	 * Minimal Constructor for Usecase 2. The constructor will only fill interaction, language ID and cateogry. Channel and category will be null. If you need to set those metadata, use full constructor. 
	 * 
	 * @param interactionString Whole interaction as one string
	 * @param langID language ID, following ISO standard (EN, DE, IT, etc) 
	 * @param category channel of the interaction, free string, and depends on the application
	 */
	public Interaction(String interactionString, String langID, String interactionId, String category)
	{
		this(interactionString, langID, interactionId, category, null, null, null); 
	}
	
	
	
	/**
	 * Minimal Constructor for Usecase 2 with keywords. The constructor will only fill interaction, language ID and cateogry. Channel and category will be null. If you need to set those metadata, use full constructor. 
	 * 
	 * @param interactionString Whole interaction as one string
	 * @param langID language ID, following ISO standard (EN, DE, IT, etc) 
	 * @param category channel of the interaction, free string, and depends on the application
	 */
	public Interaction(String interactionString, String langID, String interactionId, String category, String keywords)
	{
		this(interactionString, langID, interactionId, category, null, null, keywords); 
	}
	
	/**
	 * This method first generates a new CAS and set language ID and CAS text by the information of this Interaction. 
	 * <P> 
	 * Note that creating a new JCAS is a costly function; (compared to reusing existing ones) so you should use fillInputCAS(), if you are running this to work sequentially.  
	 * 
	 * @return JCas a newly created JCAS that holds this Interaction. No annotations in it. No linguistic annotations in it. 
	 */
	public JCas createAndFillInputCAS() throws LAPException
	{
		JCas aJCas = CASUtils.createNewInputCas(); 
		this.fillInputCAS(aJCas);
		return aJCas; 
	}
	

	/**
	 * This method returns a list of CASes, one for each relevant text in the interaction, filled with the information of this Interaction. 
	 * 
	 * @return List<JCas> a list of JCASes for this Interaction.  
	 */
	public List<JCas> createAndFillInputCASes(boolean relevantTextProvided) throws LAPException
	{		
		List<JCas> cases = new ArrayList<JCas>();
		if (relevantTextProvided) {
			logger.info("Number of relevant texts: " + relevantTexts.size());
			for (int i=0; i<this.relevantTexts.size(); i++) { //dealing with multiple relevant texts in the same interaction
				RelevantText relevantText = relevantTexts.get(i);
				String[] categories = relevantText.getGoldCategory().split(",");
				if (categories.length > 2) {
				}
				logger.info("Number of categories assigned to relevant text: " + categories.length);
				for (int j=0; j<categories.length; j++) { //dealing with multiple categories assigned to the same relevant text
					JCas aJCas = CASUtils.createNewInputCas(); 
					aJCas.setDocumentLanguage(this.lang); 
					aJCas.setDocumentText(relevantText.getText());
					CASUtils.addTLMetaData(aJCas, this.interactionId, this.channel, this.provider, null, null, null, categories[j]);
					CASUtils.addTLKeywords(aJCas, this.keywords);
					cases.add(aJCas);
				}
			}
		} else {
			JCas aJCas = CASUtils.createNewInputCas(); 
			this.fillInputCAS(aJCas);
			cases.add(aJCas);
		}
		return cases;
	}
	
	
	/**
	 * This methods gets one JCAS, cleans it up, and fill it with this Interaction. set language ID and CAS text by the information of this Interaction.
	 * 
	 * @param aJCas
	 */
	public void fillInputCAS(JCas aJCas)
	{
		aJCas.reset(); 
		aJCas.setDocumentLanguage(this.lang); 
		aJCas.setDocumentText(this.interactionString); 
		CASUtils.addTLMetaData(aJCas, this.interactionId, this.channel, this.provider, null, null, null, this.categoryString);
		CASUtils.addTLKeywords(aJCas, this.keywords);
		
		// TODO : do we need to add category metadata information 
		// as category annotation? Check this with Kathrin 
	}
		
	/**
	 * This methods gets one JCAS, cleans it up, and fill it with this Interaction. set language ID and CAS text by the information of this Interaction.
	 * 
	 * @param aJCas
	 * @param relevantText, if true: set document text to relevant text only
	 */
	/*
	public void fillInputCAS(JCas aJCas, boolean relevantTextProvided)
	{
		aJCas.reset(); 
		aJCas.setDocumentLanguage(this.lang); 
		if (relevantTextProvided) {
			aJCas.setDocumentText(this.relevantText); 
		} else {
			aJCas.setDocumentText(this.interactionString); 
		}
		CASUtils.addTLMetaData(aJCas, this.interactionId, this.channel, this.provider, null, null, null, this.categoryString);
		CASUtils.addTLKeywords(aJCas, this.keywords);
	}*/

	/**
	 * public getter for interaction string 
	 * @return the interaction string. 
	 * 
	 */
	public final String getInteractionString()
	{
		return interactionString; 
	}
	/**
	 * public getter for relevant text 
	 * @return the relevant text. 
	 * 
	 */
	public final List<RelevantText> getRelevantTexts()
	{
		return relevantTexts; 
	}
	/**
	 * public getter for language ID 
	 * @return language ID 
	 */
	public final String getLang()
	{
		return lang; 
	}
	
	/**
	 * public getter for the channel
	 * @return channel 
	 */
	public final String getChannel()
	{
		return channel; 
	}
	
	/** public getter for the provider 
	 * @return provider
	 */
	public final String getProvider()
	{
		return provider; 
	}
	
	/** public getter for the category value (String)
	 * @return category 
	 */
	public final String getCategoryString()
	{
		return categoryString; 
	}
	
	/** public getter for the category value (Array)
	 * @return category 
	 */
	public final String[] getCategories()
	{
		return categories; 
	}

	public final String getInteractionId()
	{
		return interactionId; 
	}
	
	/**
	 *  Interaction as String: Obligatory value, main data of this data type. cannot be null. 
	 */
	private final String interactionString; 
	
	/**
	 *  Relevant texts as RelevantText-s: Optional value, can be null. 
	 */
	private final List<RelevantText> relevantTexts; 
	
	/**
	 * language ID: Obligatory metadata. cannot be null.  
	 */
	private final String lang; 
	
	
	/**
	 * channel, provider, and category is additional metadata 
	 * that are optional. (can be null) 
	 */
	private final String interactionId; 
	private final String channel; 
	private final String provider; 
	private final String categoryString;
	private final String[] categories;
	private final String[] keywords;

	@Override
	public int compareTo(Interaction o) {
		return this.interactionId.compareTo(o.interactionId);
	}
}

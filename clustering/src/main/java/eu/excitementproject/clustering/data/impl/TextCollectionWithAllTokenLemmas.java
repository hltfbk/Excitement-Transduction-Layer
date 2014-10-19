package eu.excitementproject.clustering.data.impl;

import java.net.MalformedURLException;

import java.util.LinkedList;
import java.util.List;

import eu.excitementproject.eop.common.representation.partofspeech.PartOfSpeech;
import eu.excitementproject.eop.common.representation.partofspeech.UnsupportedPosTagStringException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFileDuplicateKeyException;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;
import eu.excitementproject.eop.lap.biu.postagger.PosTaggedToken;
import eu.excitementproject.eop.lap.biu.postagger.PosTaggerException;

/**
 * @author Lili Kotlerman
 *
 * Disables filtering by POS and extraction of bigrams from the extended collection 
 * Thus for each text all tokens (lemmatized unigrams) are extracted 
 */
public class TextCollectionWithAllTokenLemmas extends TextCollectionWithBigramsAndFilteringByPos  {
	
	/******************************************************************************************
	 * CONSTRUCTORS & INITIALIZATION
	 * @throws ConfigurationException 
	 * @throws ConfigurationFileDuplicateKeyException 
	 * ****************************************************************************************/
 
	public TextCollectionWithAllTokenLemmas(String configurationFileName) throws LemmatizerException, MalformedURLException, ConfigurationFileDuplicateKeyException, ConfigurationException{
		super(configurationFileName);
	}
	
	
	public TextCollectionWithAllTokenLemmas(String configurationFileName, String dataFilename) throws LemmatizerException, MalformedURLException, ConfigurationFileDuplicateKeyException, ConfigurationException{
		super(configurationFileName, dataFilename);
	}
	
	
	/******************************************************************************************
	 * METHODS FOR TERM FILTERING (STOP WORDS, FUNCTION WORDS, TOO FREQUENT/INFREQUENT TERMS)
	 * ****************************************************************************************/
	
	@Override
	protected boolean isValidPOS(char pos){
		return true;
	}
		
	/******************************************************************************************
	 * METHODS FOR TEXT PROCESSING
	 * ****************************************************************************************/
	
	/** Currently process the text as if it is a single sentence (no splitting performed)
	 * 	Extract unigrams only
	 * @param text
	 * @return
	 */
	@Override
	public List<String> extractDocumentTerms(String text) throws LemmatizerException, UnsupportedPosTagStringException, PosTaggerException{
		List<String> terms = new LinkedList<String>();
		System.out.println(m_posTagger);
		m_posTagger.setTokenizedSentence(normalizeText(text)); 
		m_posTagger.process();		
		
		for (PosTaggedToken token: m_posTagger.getPosTaggedTokens()){
			PartOfSpeech pos = token.getPartOfSpeech(); 
			char shortPOS = pos.toString().toLowerCase().charAt(0);
			String tokenLemma= getLemma(token.getToken(), pos);
			if (isValidTerm(tokenLemma, shortPOS)){
				terms.add(tokenLemma+m_posSeparator+String.valueOf(shortPOS));
			}
		}	
		if (terms.isEmpty()) {
			terms.add("non-topic-words-only");
			System.out.println("The document: <<"+text+">> has no content words.");
		}
		return terms;
	}	
}

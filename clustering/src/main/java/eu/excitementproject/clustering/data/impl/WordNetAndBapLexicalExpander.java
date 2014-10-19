package eu.excitementproject.clustering.data.impl;

import java.util.HashMap;

import eu.excitementproject.clustering.data.api.AbstractLexicalExpander;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.representation.partofspeech.PartOfSpeech;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;

/**
 * @author Lili Kotlerman
 *
 */
public class WordNetAndBapLexicalExpander extends AbstractLexicalExpander {

	private BapLexicalExpander bap;
	private WordNetLexicalExpander wn; 

	
	public WordNetAndBapLexicalExpander(String configurationFileName, TextCollection collection) throws ConfigurationException {
		super(collection);
		init(configurationFileName, collection);
	}


	private void init(String configurationFileName, TextCollection collection) throws ConfigurationException {		
		bap = new BapLexicalExpander(configurationFileName, collection);
		wn = new WordNetLexicalExpander(configurationFileName, collection);
	}
	

	private HashMap<String, Double> joinExpansions(HashMap<String, Double> bapExpansions, HashMap<String, Double> wnExpansions){
		HashMap<String,Double> expansions = bapExpansions;
		System.out.println(expansions);
		for (String expan : wnExpansions.keySet()){
			double score = wnExpansions.get(expan);
			if (expansions.containsKey(expan)) {
				score += expansions.get(expan);
			}
			expansions.put(expan, score);
		}
		return expansions;		
	}
	
	@Override
	public HashMap<String, Double> getUnfilteredExpansions(String lemma, PartOfSpeech pos)
			throws Exception {
		return joinExpansions(bap.getExpansions(lemma, pos), wn.getExpansions(lemma, pos));
	}

	@Override
	public HashMap<String, Double> getUnfilteredExpansions(String lemma) throws Exception {
		return joinExpansions(bap.getExpansions(lemma), wn.getExpansions(lemma));
	}
}

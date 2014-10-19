package eu.excitementproject.clustering.data.api;

import java.util.HashMap;

import eu.excitementproject.eop.common.representation.partofspeech.PartOfSpeech;

/**
 * @author Lili Kotlerman
 *
 */
public interface LexicalExpander {
	
	public HashMap<String,Double> getExpansions(String lemma, PartOfSpeech pos) throws Exception;

	public HashMap<String,Double> getExpansions(String lemma) throws Exception;
		
	// add code to check whether original terms are connected in resources, even if not returned within top-k expansions
}

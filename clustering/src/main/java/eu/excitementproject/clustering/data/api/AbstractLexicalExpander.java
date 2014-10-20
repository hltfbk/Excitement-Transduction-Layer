package eu.excitementproject.clustering.data.api;

import java.util.HashMap;

import eu.excitementproject.eop.common.representation.partofspeech.PartOfSpeech;

public abstract class AbstractLexicalExpander implements LexicalExpander {
	
	TextCollection m_underlyingTextCollection;

	
	public AbstractLexicalExpander(TextCollection m_underlyingTextCollection) {
		this.m_underlyingTextCollection = m_underlyingTextCollection;
	}
	
	public abstract HashMap<String, Double> getUnfilteredExpansions(String lemma, PartOfSpeech pos) throws Exception;
	public abstract HashMap<String, Double> getUnfilteredExpansions(String lemma) throws Exception;
	
	public HashMap<String, Double> filterStopwords (HashMap<String, Double> unfilteredExpansions){
		HashMap<String, Double> filteredExpansions = new HashMap<String, Double>();
		for (String expansion : unfilteredExpansions.keySet()){
			if (!m_underlyingTextCollection.isStopword(expansion)) filteredExpansions.put(expansion, unfilteredExpansions.get(expansion));
		}
		return filteredExpansions;
	}

	@Override
	public HashMap<String, Double> getExpansions(String lemma, PartOfSpeech pos)
			throws Exception{
		return filterStopwords(getUnfilteredExpansions(lemma, pos));
	}

	@Override
	public HashMap<String, Double> getExpansions(String lemma) throws Exception {
		return filterStopwords(getUnfilteredExpansions(lemma));
	}

}

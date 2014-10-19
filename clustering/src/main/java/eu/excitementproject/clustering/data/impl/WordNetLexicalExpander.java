package eu.excitementproject.clustering.data.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.clustering.data.api.AbstractLexicalExpander;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalResourceException;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalRule;
import eu.excitementproject.eop.common.representation.partofspeech.BySimplerCanonicalPartOfSpeech;
import eu.excitementproject.eop.common.representation.partofspeech.PartOfSpeech;
import eu.excitementproject.eop.common.representation.partofspeech.SimplerCanonicalPosTag;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.eop.core.component.lexicalknowledge.wordnet.WordnetLexicalResource;
import eu.excitementproject.eop.core.component.lexicalknowledge.wordnet.WordnetRuleInfo;
import eu.excitementproject.eop.core.utilities.dictionary.wordnet.WordNetRelation;

/**
 * @author Lili Kotlerman
 * Expands using heuristic similar to that of 
 */
public class WordNetLexicalExpander extends AbstractLexicalExpander {

	private WordnetLexicalResource wn; 

	public final static double WN_CONFIDENCE = 0.03;

	private final boolean m_REPLACE_DIGITS = true;
	private final String m_replaceDigitsWith = "@"; 
	
	public WordNetLexicalExpander(String configurationFileName, TextCollection collection) throws ConfigurationException {
		super(collection);
		init(new ConfigurationFile(configurationFileName));
	}


	private void init(ConfigurationFile conf) throws ConfigurationException {		
/*		if(conf.isModuleExist("LexicalExpander")){
			ConfigurationParams params = conf.getModuleConfiguration("LexicalExpander");
		}
*/		
		if(conf.isModuleExist("WN")){
			ConfigurationParams params = conf.getModuleConfiguration("WN");
			try {
				wn = new WordnetLexicalResource(params);
				// TODO need to set WN relations?
			} catch (LexicalResourceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String replaceDigits(String s){
		if (m_REPLACE_DIGITS){
			String s1=s.replace("0", m_replaceDigitsWith).replace("1", m_replaceDigitsWith).replace("2", m_replaceDigitsWith).replace("3", m_replaceDigitsWith).replace("4", m_replaceDigitsWith).replace("5", m_replaceDigitsWith).replace("6", m_replaceDigitsWith).replace("7", m_replaceDigitsWith).replace("8", m_replaceDigitsWith).replace("9", m_replaceDigitsWith);
			s1 = s1.replace("_", " "); // now WN returns stuff with underscore instead of space
			return s1;
		}
		else return s;		
	}
	

	private HashMap<String,Double> getWordNetExpansions(String lemma, PartOfSpeech pos) throws LexicalResourceException{
		HashMap<String,Double> expansions = new HashMap<String, Double>();

		//	Get derivations and synonyms of the given term	
		Set<WordNetRelation> rel = new HashSet<WordNetRelation>();
		rel.add(WordNetRelation.SYNONYM);
		rel.add(WordNetRelation.DERIVATIONALLY_RELATED);
		rel.add(WordNetRelation.HYPONYM);
		rel.add(WordNetRelation.HYPERNYM);		
		rel.add(WordNetRelation.MEMBER_HOLONYM);
		rel.add(WordNetRelation.MEMBER_MERONYM);
				
		// get synonyms and derivation of the given term
		wn.setDefaultRelationSet(rel);
		try {
			for (LexicalRule<? extends WordnetRuleInfo> termSynDer : wn.getRulesForRight(lemma, pos)){			
				expansions.put(replaceDigits(termSynDer.getLLemma().toLowerCase()), WN_CONFIDENCE);	
				expansions.put(replaceDigits(termSynDer.getLLemma().toLowerCase().replace(" ", "")), WN_CONFIDENCE);
				expansions.put(replaceDigits(termSynDer.getLLemma().toLowerCase().replace(" ", "-")), WN_CONFIDENCE);
			}
		} catch (Exception e) {
			System.out.println("Cannot expand term with WN. Skipping the term <"+lemma+">:"+pos.toString());
			e.printStackTrace();
		}	

		return expansions;
	}
	
	@Override
	public HashMap<String, Double> getUnfilteredExpansions(String lemma, PartOfSpeech pos)
			throws Exception {
		HashMap<String,Double> expansions = new HashMap<String, Double>();		
		expansions.putAll(getWordNetExpansions(lemma, pos));
		if (lemma.contains(" ")){
			// add two variations - with "-" and as signle term, for compatibility with ukwac expansions, since there are no multiword terms there
			// e.g. "credit card" = "creditcard" and "credit-card"			
			String[] addLemmas = {lemma.replace(" ", "-"), lemma.replace(" ", "")};
			for (String additionalLemma : addLemmas){
				HashMap<String,Double> additionalExpansions = getWordNetExpansions(additionalLemma, pos);
				if (!additionalExpansions.isEmpty()){
					expansions.putAll(additionalExpansions);
					expansions.put(additionalLemma, WN_CONFIDENCE);
				}				
			}
		}
		System.out.println("[wn]: "+lemma+" : "+expansions.size()+" : "+expansions.keySet());
		return expansions;
	}

	@Override
	public HashMap<String, Double> getUnfilteredExpansions(String lemma) throws Exception {
		HashMap<String, Double> expansions = new HashMap<String, Double>();

		PartOfSpeech n = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.NOUN);
		PartOfSpeech v = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.VERB);
		PartOfSpeech a = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.ADJECTIVE);

		expansions.putAll(getUnfilteredExpansions(lemma,n));
		expansions.putAll(getUnfilteredExpansions(lemma,v));
		expansions.putAll(getUnfilteredExpansions(lemma,a));
		
		//if (lemma.contains(" ")) System.out.println(lemma+" >> "+expansions);
		return expansions;
	}
}

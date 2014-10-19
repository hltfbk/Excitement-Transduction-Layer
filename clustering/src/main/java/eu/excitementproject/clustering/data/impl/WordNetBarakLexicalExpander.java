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
public class WordNetBarakLexicalExpander extends AbstractLexicalExpander {

	private WordnetLexicalResource wn; 


	private final boolean m_REPLACE_DIGITS = true;
	private final String m_replaceDigitsWith = "@"; 
	
	public WordNetBarakLexicalExpander(String configurationFileName, TextCollection collection) throws ConfigurationException {
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
			return s1;
		}
		else return s;		
	}
	

	private HashMap<String,Double> getWordNetExpansions(String lemma, PartOfSpeech pos) throws LexicalResourceException{
		double WN_SYN_DER_CONFIDENCE = 1.0;
		double WN_HYPO_HYPER_CONFIDENCE = 1.0;

		HashMap<String,Double> expansions = new HashMap<String, Double>();

		//	Get derivations and synonyms of the given term	
		Set<WordNetRelation> relSynDer = new HashSet<WordNetRelation>();
		relSynDer.add(WordNetRelation.SYNONYM);
		relSynDer.add(WordNetRelation.DERIVATIONALLY_RELATED);
		
		//	Get derivations and synonyms of the given term	
		Set<WordNetRelation> relHyperHypo = new HashSet<WordNetRelation>();
	//	relHyperHypo.add(WordNetRelation.HYPERNYM);
		relHyperHypo.add(WordNetRelation.HYPONYM);
	/*	relHyperHypo.add(WordNetRelation.MEMBER_HOLONYM);
		relHyperHypo.add(WordNetRelation.MEMBER_MERONYM);*/
		
		
		//relations.add(WordNetRelation.DERIVED);
		
		// get synonyms and derivation of the given term
		wn.setDefaultRelationSet(relSynDer);
		for (LexicalRule<? extends WordnetRuleInfo> termSynDer : wn.getRulesForRight(lemma, pos)){			
			expansions.put(replaceDigits(termSynDer.getLLemma().toLowerCase()), WN_SYN_DER_CONFIDENCE);			
			// now add hypernyms and hyponyms of the current synonym/derivation
			wn.setDefaultRelationSet(relHyperHypo);
			for (LexicalRule<? extends WordnetRuleInfo> termHyp : wn.getRulesForRight(termSynDer.getLLemma(), termSynDer.getLPos())){				
				expansions.put(replaceDigits(termHyp.getLLemma().toLowerCase()), WN_HYPO_HYPER_CONFIDENCE*WN_SYN_DER_CONFIDENCE);
				// now add synonyms and derivations of current hyper-hyponym
				wn.setDefaultRelationSet(relSynDer);
				for (LexicalRule<? extends WordnetRuleInfo> term2ndSynDer : wn.getRulesForRight(termHyp.getLLemma(), termHyp.getLPos())){					
					expansions.put(replaceDigits(term2ndSynDer.getLLemma().toLowerCase()), WN_HYPO_HYPER_CONFIDENCE*WN_SYN_DER_CONFIDENCE);		
				}							
			}			
		}
		// now add hypernyms and hyponyms of the given term + their synonyms and derivations
		wn.setDefaultRelationSet(relHyperHypo);
		for (LexicalRule<? extends WordnetRuleInfo> termHyperHypo : wn.getRulesForRight(lemma, pos)){			
			expansions.put(replaceDigits(termHyperHypo.getLLemma().toLowerCase()), WN_HYPO_HYPER_CONFIDENCE);			
			// now add synonyms and derivations of current hyper-hyponym
			wn.setDefaultRelationSet(relSynDer);
			for (LexicalRule<? extends WordnetRuleInfo> term2ndSynDer : wn.getRulesForRight(termHyperHypo.getLLemma(), termHyperHypo.getLPos())){				
				expansions.put(replaceDigits(term2ndSynDer.getLLemma().toLowerCase()), WN_HYPO_HYPER_CONFIDENCE*WN_SYN_DER_CONFIDENCE);		
			}							
		}	

		return expansions;
	}
	
	@Override
	public HashMap<String, Double> getUnfilteredExpansions(String lemma, PartOfSpeech pos)
			throws Exception {
		HashMap<String,Double> expansions = new HashMap<String, Double>();		
		expansions.putAll(getWordNetExpansions(lemma, pos));
		System.out.println(lemma+" : "+expansions.size()+" : "+expansions.keySet());
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
		
		if (lemma.contains(" ")) System.out.println(lemma+" >> "+expansions);
		return expansions;
	}
}

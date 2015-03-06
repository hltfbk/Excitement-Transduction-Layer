package eu.excitementproject.tl.experiments.OMQ;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalResourceException;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalRule;
import eu.excitementproject.eop.core.component.lexicalknowledge.derivbase.DerivBaseResource;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetRelation;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetWrapper;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * SEDAUtils provides methods needed to build a graph with SEDA Entailment Algorithmus used in SEDAGraphBuilder
 * (for now it works only for German)
 * 
 * @author Aleksandra ()
 *
 */

public class SEDAUtils {

	static Logger logger = Logger.getLogger(SEDAUtils.class); 
	
	/**
	 * Get related text for a single or a two token text given the lexical resource
	 * DerivBaseResource, GermaNet, GermanWordSplitter or negation maper.
	 * 
	 * @param text
	 * @param lemmatizedText
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param mapNegation
	 * @return
	 * @throws LexicalResourceException
	 */
	public static Set<String> getRelatedText(String text, String lemmatizedText, DerivBaseResource derivBaseResource, 
			GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations, GermanWordSplitter splitter, boolean mapNegation) 
					throws LexicalResourceException{
		
		Set<String> permutations = new HashSet<String>();
		List<String> textTokens = Arrays.asList(text.split("\\s+")); //add original text tokens  
		List<String> textLemmas = Arrays.asList(lemmatizedText.split("\\s+"));
		
		if(textTokens.size() >=1 || textTokens.size() <=2){
			//case single token fragments
			if(textTokens.size() == 1 && textLemmas.size() == 1) {
				String tokenText = textTokens.get(0);
				String [] lemmas = getLemmas(textLemmas.get(0));
				for(String lemma : lemmas){
					permutations.addAll(getRelatedLemmas(lemma, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation));
				}
				permutations.add(tokenText);
				permutations.add(tokenText.toLowerCase());
			}
			
			//case two token fragments
			else if(textTokens.size() == 2 && textLemmas.size() == 2) {
				//TODO: deal with missing decomposition lemma
				Set<String> extendedToken_1 = new HashSet<String>();
				Set<String> extendedToken_2 = new HashSet<String>();
				
				for (int i=0; i < textLemmas.size(); i++){
					//extend first and second token of dependency relation by related lemmas
					String [] lemmas = getLemmas(textLemmas.get(i));
					for(String lemma : lemmas){
						if(i==0){
							extendedToken_1.addAll(getRelatedLemmas(lemma, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation));
							extendedToken_1.add(textTokens.get(0).toLowerCase());
						}
						else if(i==1){
							extendedToken_2.addAll(getRelatedLemmas(lemma, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation));
							extendedToken_2.add(textTokens.get(1).toLowerCase());
						}
					}
				}
				
				permutations = getPermutations(extendedToken_1, extendedToken_2, true);
			}
			else {
				logger.error("Can't get related text if the number of tokens in the input text differ from the number of tokens in the lemmatized form of the input");
			}
		}
		else {
			logger.error("Can't get related text for an input text having no or more than two tokens");
		}
		
		return permutations;
	}
	
	
	/**
	 *  Get set of related lemma given the lexical resource
	 * DerivBaseResource, GermaNet or GermanWordSplitter or negation maper.
	 * 
	 * @param lemma
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param mapNegation
	 * @return
	 * @throws LexicalResourceException
	 */
	public static Set<String> getRelatedLemmas(String lemma, DerivBaseResource derivBaseResource, 
			GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations, 
			GermanWordSplitter splitter, boolean mapNegation) 
					throws LexicalResourceException{
		
		Set<String> lemmas = new HashSet<String>();
		lemmas.add(lemma);
		lemmas.addAll(getRelatedLemmas(lemma, splitter));
		
		Set<String> relatedLemmas = new HashSet<String>();	
		for(String tempLemma : lemmas){
			relatedLemmas.add(tempLemma);
			relatedLemmas.addAll(getRelatedLemmas(tempLemma, germaNetWrapper, germaNetRelations));
			relatedLemmas.addAll(getRelatedLemmas(tempLemma, derivBaseResource));
		}

		if(mapNegation && isNegationWordDE(lemma)){
			relatedLemmas.addAll(Arrays.asList(getNegationLemmasDE()));
		}
		
		return relatedLemmas;
	}
	
	/**
	 * Get set of related lemma given the lexical resource DerivBaseResource
	 * 
	 * @param lemma
	 * @param derivBaseResource
	 * @return
	 * @throws LexicalResourceException
	 */
	private static Set<String> getRelatedLemmas(String lemma, DerivBaseResource derivBaseResource) 
			throws LexicalResourceException{
		Set<String> relatedLemmas = new HashSet<String>();
		if(derivBaseResource != null){
			for(LexicalRule<?> rule : derivBaseResource.getRulesForLeft(lemma, null)){
				relatedLemmas.add(rule.getRLemma());
			}
		}
		return relatedLemmas;
	}
	
	/**
	 * Get lemmas of German negation words
	 * @return --  {"keine", "nicht", "nichts", "ohne"}
	 */
	private static String [] getNegationLemmasDE(){
		String [] negationLemmas = {"keine", "keinerlei", "nicht", "nichts", "ohne"};
		return negationLemmas;
	}
	
	/**
	 * Check if the input word is a German negation word
	 * @param word
	 * @return
	 */
	private static boolean isNegationWordDE(String word){
		List<String> negationWords = Arrays.asList(new String [] {"kein", "keins", "keine", "keinem", "keinem", "keiner", "keins", "keinerlei", "nicht", "nichts", "ohne"});
		return negationWords.contains(word);
	}
	
	/**
	 * Get set of related lemma given the resource GermanWordSplitter
	 * 
	 * @param lemma
	 * @param splitter
	 * @return
	 */
	private static Set<String> getRelatedLemmas(String lemma, GermanWordSplitter splitter){
		Set<String> splits = new HashSet<String>();
		splits.add(lemma);
		if(splitter != null){
			for(String hyphenSplit : lemma.split("[-]")){ //to deal with compounds "XML-Daten", where the strict method of GermanWordSplitter fails
				splits.add(hyphenSplit);
				for(String split : splitter.splitWord(hyphenSplit)){
					Character ch = split.charAt(0);
					if(Character.isLetter(ch)){
						Character firstLetterUpperCase = Character.toUpperCase(ch);
						String splitFirstLetterUpperCase = split.replaceFirst(ch.toString(), firstLetterUpperCase.toString());
						splits.add(splitFirstLetterUpperCase);
					}
				}
			}
		}
		return splits;
	}
	
	/**
	 * Get set of related lemma given the lexical resource GermaNet
	 * 
	 * @param lemma
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @return
	 * @throws LexicalResourceException
	 */
	private static Set<String> getRelatedLemmas(String lemma, GermaNetWrapper germaNetWrapper, 
			List<GermaNetRelation> germaNetRelations) throws LexicalResourceException{
		
		Set<String> relatedLemmas = new HashSet<String>();
		if(germaNetWrapper != null) {
			for(GermaNetRelation gnRelation : germaNetRelations){
				if(gnRelation.toGermaNetString().equals("has_hyponym")){
					for(LexicalRule<?> rule : germaNetWrapper.getRulesForRight(lemma, null, gnRelation)){
						relatedLemmas.add(rule.getLLemma());
					}
				}
				else {
					for(LexicalRule<?> rule : germaNetWrapper.getRulesForLeft(lemma, null, gnRelation)){ 
						relatedLemmas.add(rule.getRLemma());
					}
				}
			}
		}
		return relatedLemmas;
	}
	
	/**
	 * Split the lemma string around matches of the regular expression \\|
	 * (to deal with lemma type: lemma|lemma, which is sometimes return by TreeTagger).
	 * 
	 * @param lemma
	 * @return
	 */
	private static String [] getLemmas(String lemma) {
		return lemma.split("\\|");
	}
	
	/**
	 * Get Set<EntailmentUnit> with a given lemmatized text
	 * 
	 * @param egr
	 * @param lemmatizedText
	 * @param ignoreCase
	 * @return
	 */
	public static Set<EntailmentUnit> getLemmatizedVertex(EntailmentGraphRaw egr, String lemmatizedText, boolean ignoreCase) {
		Set<EntailmentUnit> resultSet = new HashSet<EntailmentUnit>();
		if(ignoreCase) {
			lemmatizedText = lemmatizedText.toLowerCase();
		}
		if(egr.hasLemmatizedLabel()){
			for(EntailmentUnit eu : egr.vertexSet()){
				
				String euLemmatizedText = eu.getLemmatizedText();
				if(ignoreCase){
					euLemmatizedText = euLemmatizedText.toLowerCase();
				}
				
				if (euLemmatizedText.equals(lemmatizedText)){
					resultSet.add(eu);
				}
			}
		}
		return  resultSet;
	}
	
	/**
	 * Get Set<EntailmentUnit> with given lemmatized text from a Set<String> lemmatizedTextSet
	 * 
	 * @param egr
	 * @param lemmatizedTextSet
	 * @param ignoreCase
	 * @return
	 */
	public static Set<EntailmentUnit> getLemmatizedVertex(EntailmentGraphRaw egr, Set<String> lemmatizedTextSet, boolean ignoreCase) {
		Set<EntailmentUnit> resultSet = new HashSet<EntailmentUnit>();
		Set<String> textsToFind = new HashSet<String>();
		
		if(ignoreCase){
			for(String lemmatizedText : lemmatizedTextSet){
				textsToFind.add(lemmatizedText.toLowerCase());
			}
		}else {
			textsToFind = lemmatizedTextSet;
		}
		
		if(egr.hasLemmatizedLabel()){
			for(EntailmentUnit eu : egr.vertexSet()){
				String lemmatizedText = eu.getLemmatizedText();
				if(!lemmatizedText.isEmpty()){
					if(ignoreCase){
						lemmatizedText = lemmatizedText.toLowerCase();
					}
					if(textsToFind.contains(lemmatizedText)){
						resultSet.add(eu);
					}
				}
			}
		}
		return  resultSet;
	}
	
	/**
	 * Given two sets A and B, combine every element of A and B to a string "a b" and "b a"
	 * Return a list of all combinations.
	 * 
	 * @param aSet
	 * @param bSet
	 * @param useLowerCase
	 * @return
	 */
	private static Set<String> getPermutations(Set<String> aSet, Set<String> bSet, boolean useLowerCase){
		Set<String> permutations = new HashSet<String>();
		for(String a : aSet){
			for(String b : bSet){
				if(useLowerCase){
					permutations.add(a.toLowerCase() + " " + b.toLowerCase());
					permutations.add(b.toLowerCase() + " " + a.toLowerCase());
				}
				else {
					permutations.add(a + " " + b);
					permutations.add(b + " " + a);
				}
			}
		}
		return permutations;
	}

}

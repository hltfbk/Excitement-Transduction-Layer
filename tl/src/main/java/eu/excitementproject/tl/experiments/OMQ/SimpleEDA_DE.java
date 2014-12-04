package eu.excitementproject.tl.experiments.OMQ;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalRule;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ClassificationTEDecision;
import eu.excitementproject.eop.core.component.lexicalknowledge.derivbase.DerivBaseInfo;
import eu.excitementproject.eop.core.component.lexicalknowledge.derivbase.DerivBaseResource;
import eu.excitementproject.eop.core.component.lexicalknowledge.dewakdistributional.GermanDistSim;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetInfo;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetRelation;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetWrapper;
import eu.excitementproject.eop.core.component.lexicalknowledge.transDm.GermanTransDmResource;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;

/**
 * SimpleEDA_DE returns reliable entailment decisions for very short text fragments. 
 * 
 * SimpleEDA_DE returns ENTAILMENT if every token of the hypothesis can be mapped on any token of the text, 
 * otherwise the EDA returns NONENTAILMENT. This EDA cannot be trained, the decision and the confidence are based 
 * directly on the given input text-hypothesis-pair and the found matchings between
 * 
 * SimpleEDA_DE was written for UseCase2 evaluations with single-token and two-token fragments as TH pairs.
 * This EDA is not suitable for sentence fragments as TH pairs.
 * 
 * @author Aleksandra, October 2014
 *
 */
public class SimpleEDA_DE implements EDABasic<ClassificationTEDecision> {

	private DerivBaseResource dbr;
	private GermaNetWrapper gn;
	private Set<GermaNetRelation> gnRelations;
	//TODO: replace by dkpro decompunder?
	private GermanWordSplitter splitter;
	//TODO: integrate similarity measure resources (GermanTransDmResource and GermanDistSim)
	@SuppressWarnings("unused")
	private GermanTransDmResource gtdm;
	
	@SuppressWarnings("unused")
	private GermanDistSim gds;
	private static Logger logger = Logger.getLogger("SimpleEDA_DE");
	
	/*******************************************************************
	 **   CONSTRUCTORS                                                **
	 *******************************************************************/
	
	/**
	 * Constructor to activate the following scoring components:
	 * - base configuration (word form and lemma matching)
	 */
	public SimpleEDA_DE(){}
	
	/**
	 * Constructor to activate the following scoring components:
	 * - base configuration (word form and lemma matching)
	 * - decomposition relation
	 * 
	 * @param splitter -- GermanWordSplitter
	 */
	
	public SimpleEDA_DE(GermanWordSplitter splitter){
		this.splitter = splitter;
	}
	
	/**
	 * Constructor to activate the following scoring components:
	 * - base configuration scoring (word form and lemma matching)
	 * - derivation relation (resource: DerivBase)
	 * 
	 * @param derivSteps -- the maximum accepted amount of derivation steps between two derivationally related lemmas
	 */
	public SimpleEDA_DE(int derivSteps){
		try {
			boolean useDerivBaseScores = true;
			this.dbr = new DerivBaseResource(useDerivBaseScores, derivSteps);
		} 
		catch (ConfigurationException | ComponentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor to activate the following scoring components:
	 * - base configuration (word form and lemma matching are activated)
	 * - decomposition relation
	 * - derivation relation
	 * 
	 * @param splitter -- GermanWordSplitter
	 * @param derivSteps -- the maximum accepted amount of derivation steps between two derivationally related lemmas
	 */
	public SimpleEDA_DE(GermanWordSplitter splitter, int derivSteps){
		try {
			this.splitter = splitter;
			boolean useDerivBaseScores = true;
			this.dbr = new DerivBaseResource(useDerivBaseScores, derivSteps);
		} catch (ConfigurationException | ComponentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor activates the following scoring components:
	 * - base configuration (word form and lemma matching)
	 * - GermaNet relation 
	 * 
	 * @param pathToGermaNet -- path to GermanNet top directory, which stores XML version of the resource (GN_V{version}_XML) 
	 * @param useSynonymRelation - set to true to activate GermaNetRelation.has_synonym relation
	 * @param useHypernymRelation - set to true to activate GermaNetRelation.has_hypernym relation
	 * @param useEntailsRelation - set to true to activate GermaNetRelation.entails relation
	 * @param useCausesRelation - set to true to activate GermaNetRelation.causes relation
	 */
	public SimpleEDA_DE(String pathToGermaNet, boolean useSynonymRelation, boolean useHypernymRelation, 
			boolean useEntailsRelation, boolean useCausesRelation){
		try {
			this.gn = new GermaNetWrapper(pathToGermaNet); 
			gnRelations = new HashSet<GermaNetRelation>();
			if(useSynonymRelation) gnRelations.add(GermaNetRelation.has_synonym);
			if(useHypernymRelation) gnRelations.add(GermaNetRelation.has_hypernym);
			if(useEntailsRelation) gnRelations.add(GermaNetRelation.entails);
			if(useCausesRelation) gnRelations.add(GermaNetRelation.causes);
		} 
		catch (ConfigurationException | ComponentException e) {
				e.printStackTrace();
		}
	}
	
	/**
	 * Constructor activates the following scoring components:
	 * - base configuration (word form and lemma matching are activated)
	 * - derivation relation
	 * - GermaNet relation 
	 * 
	 * @param derivSteps -- the maximum accepted amount of derivation steps between two derivationally related lemmas
	 * @param pathToGermaNet -- path to GermanNet top directory, which stores XML version of the resource (GN_V{version}_XML) 
	 * @param useSynonymRelation - set to true to activate GermaNetRelation.has_synonym relation
	 * @param useHypernymRelation - set to true to activate GermaNetRelation.has_hypernym relation
	 * @param useEntailsRelation - set to true to activate GermaNetRelation.entails relation
	 * @param useCausesRelation - set to true to activate GermaNetRelation.causes relation
	 */
	public SimpleEDA_DE(int derivSteps, String pathToGermaNet, boolean useSynonymRelation, boolean useHypernymRelation, 
			boolean useEntailsRelation, boolean useCausesRelation){
		try{
			boolean useDerivBaseScores = true;
			this.dbr = new DerivBaseResource(useDerivBaseScores, derivSteps);
			this.gn = new GermaNetWrapper(pathToGermaNet); 
			gnRelations = new HashSet<GermaNetRelation>();
			if(useSynonymRelation) gnRelations.add(GermaNetRelation.has_synonym);
			if(useHypernymRelation) gnRelations.add(GermaNetRelation.has_hypernym);
			if(useEntailsRelation) gnRelations.add(GermaNetRelation.entails);
			if(useCausesRelation) gnRelations.add(GermaNetRelation.causes);
		} catch (ConfigurationException | ComponentException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Constructor activates the following scoring components:
	 * - base configuration (word form and lemma matching are activated)
	 * - decomposition relation
	 * - derivation relation
	 * - GermaNet relation 
	 * 
	 * @param splitter -- GermanWordSplitter
	 * @param derivSteps -- the maximum accepted amount of derivation steps between two derivationally related lemmas
	 * @param pathToGermaNet -- path to GermanNet top directory, which stores XML version of the resource (GN_V{version}_XML) 
	 * @param useSynonymRelation - set to true to activate GermaNetRelation.has_synonym relation
	 * @param useHypernymRelation - set to true to activate GermaNetRelation.has_hypernym relation
	 * @param useEntailsRelation - set to true to activate GermaNetRelation.entails relation
	 * @param useCausesRelation - set to true to activate GermaNetRelation.causes relation
	 */
	public SimpleEDA_DE(GermanWordSplitter splitter, int derivSteps, 
			String pathToGermaNet, boolean useSynonymRelation, boolean useHypernymRelation, boolean useEntailsRelation, boolean useCausesRelation){
		try {
			this.splitter = splitter;
			boolean useDerivBaseScores = true;
			this.dbr = new DerivBaseResource(useDerivBaseScores, derivSteps);
			this.gn = new GermaNetWrapper(pathToGermaNet); 
			gnRelations = new HashSet<GermaNetRelation>();
			if(useSynonymRelation) gnRelations.add(GermaNetRelation.has_synonym);
			if(useHypernymRelation) gnRelations.add(GermaNetRelation.has_hypernym);
			if(useEntailsRelation) gnRelations.add(GermaNetRelation.entails);
			if(useCausesRelation) gnRelations.add(GermaNetRelation.causes);
		} catch (ConfigurationException | ComponentException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public ClassificationTEDecision process(JCas thPair) throws EDAException,
			ComponentException {
		
		JCas textCas = null;
		String text = null;
		String hypothesis = null;
		JCas hypothesisCas = null;
		Set<String> textExtended = null;
		Set<String> hypothesisTokens = null;
		List<String> negationInflectedWords = Arrays.asList(new String [] {"kein", "keins", "keines", "keine", "keiner", "keinem", "keinen"});
		List<String> allNegationWords = Arrays.asList(new String [] {"kein", "keins", "keines", "keine", "keiner", "keinem", "keinen", "nicht", "ohne"});
		try 
		{
			textCas = thPair.getView("TextView");
			text = textCas.getDocumentText();
			hypothesisCas = thPair.getView("HypothesisView");
			hypothesis = hypothesisCas.getDocumentText();
		
			// quick decision: return entailment if text and hypothesis has identical content
			if(text.equalsIgnoreCase(hypothesis)){
				logger.info("exact matching: " + text + " --> " + hypothesis);
				return new ClassificationTEDecision(DecisionLabel.Entailment, 1.0, null);
			}
			
			
			//TODO: integrate GermanDistSim && GermanTransDmResource
			
			//represent hypothesis as bag of words and lemmas
			Iterator<Annotation> hypothesisIter = hypothesisCas.getAnnotationIndex(Token.type).iterator();
			hypothesisTokens = new HashSet<String>();
			while (hypothesisIter.hasNext()) 
			{
				Token hToken = (Token) hypothesisIter.next();
				String hText = hToken.getCoveredText(); 
				String hLemma = hToken.getLemma().getValue();
				
				//TODO: deal with lemmas Anzeigen|Anzeige
				if(!hLemma.equalsIgnoreCase("@card@")){
					
					//add negation relations
					if(negationInflectedWords.contains(hToken)){
						hypothesisTokens.add("kein");
					}
					
					//add decompounded words from germanet
					if(splitter != null){
						Collection<String> hLemmaDecompounded = splitter.splitWord(hLemma);
						for(String hLemmaCompoundPart : hLemmaDecompounded){
							hLemmaCompoundPart = hLemmaCompoundPart.replace("-", "");
							hypothesisTokens.add(hLemmaCompoundPart.toLowerCase()); 
						} 
					} else {
						hypothesisTokens.add(hLemma.toLowerCase());
					}
				}else {
					hypothesisTokens.add(hText.toLowerCase());
				}
			}
			
			//extend text with lemmas from lexical knowledge bases
			textExtended = new HashSet<String>();
			Iterator<Annotation> textIter = textCas.getAnnotationIndex(Token.type).iterator();
			Set<String> tempExtendedLemmas = new HashSet<String>(); 
			while (textIter.hasNext()) 
			{
				Token tToken = (Token) textIter.next();
				String tText = tToken.getCoveredText(); 
				String tLemma = tToken.getLemma().getValue();
				
				//TODO: deal with lemmas Anzeigen|Anzeige
				if(!tLemma.equalsIgnoreCase("@card@")){
					tempExtendedLemmas.add(tLemma); 
					textExtended.add(tLemma.toLowerCase());
					
					//add decompounded words from germanet
					if(splitter != null){
						Collection<String> tLemmaDecompounded = splitter.splitWord(tLemma);
						for(String tLemmaCompoundPart : tLemmaDecompounded){
							tLemmaCompoundPart = tLemmaCompoundPart.replace("-", "");
							textExtended.add(tLemmaCompoundPart.toLowerCase());
							String firstLetter = (String) tLemmaCompoundPart.subSequence(0, 1);
							String tLemmaCompoundPartFirstLetterUpperCase = tLemmaCompoundPart.replaceFirst(firstLetter, firstLetter.toUpperCase());
							String tLemmaCompoundPartFirstLowerCase = tLemmaCompoundPart.replaceFirst(firstLetter, firstLetter.toLowerCase());
							tempExtendedLemmas.add(tLemmaCompoundPart); 
							tempExtendedLemmas.add(tLemmaCompoundPartFirstLetterUpperCase);
							tempExtendedLemmas.add(tLemmaCompoundPartFirstLowerCase); 
						}
					}
					
					//add lemmas from derivbase
					if(dbr != null){
						for(String tExtendedLemma : tempExtendedLemmas){
							for(LexicalRule<? extends DerivBaseInfo> rule : dbr.getRulesForLeft(tExtendedLemma, null)){
								textExtended.add(rule.getRLemma().toLowerCase()); 
							}
						}
					}
					
					//add lemmas from germanet
					if(gn != null){
						for(String tExtendedLemma : tempExtendedLemmas){
							for(GermaNetRelation gnRelation : gnRelations){
								for(LexicalRule<? extends GermaNetInfo>  rule : gn.getRulesForLeft(tExtendedLemma, null, gnRelation)){
									if(!rule.getRLemma().equalsIgnoreCase("gnroot")){
										textExtended.add(rule.getRLemma().toLowerCase());
									}
								}
							}
						}
						
						//add negation relations
						if(allNegationWords.contains(tLemma)){
							textExtended.add("kein");
							textExtended.add("nicht");
							textExtended.add("ohne");
						}
					}
					
					//add token text and token lemma
					textExtended.add(tText.toLowerCase());
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
//		System.out.print("text: " + textExtended); //TODO: REMOVE
//		System.out.print("hypothesis: " + hypothesisTokens); //TODO: REMOVE
		//if text extended with entailed lemmas contains all words of hypothesis lemmas
		if(!textExtended.isEmpty() && !hypothesisTokens.isEmpty() && textExtended.containsAll(hypothesisTokens)){
			logger.info("SEDA SAYS: ENTAILMENT: " + text + " --> " + hypothesis);
			return new ClassificationTEDecision(DecisionLabel.Entailment, 0.91 , null);
		}
		else{
			logger.info("SEDA SAYS: NONENTAILMENT: " + text + " --> " + hypothesis);
			return new ClassificationTEDecision(DecisionLabel.NonEntailment, 0.0 , null);
		}
	}
	
	@Override
	public void initialize(CommonConfig config) throws ConfigurationException,
			EDAException, ComponentException {
		//TODO: create configuration files and implement this method 
		//for now the scoring components are initialized via constructor	
	}
	
	@Override
	public void shutdown() {
		//this eda has nothing to shut down
	}

	@Override
	public void startTraining(CommonConfig arg0) throws ConfigurationException,
			EDAException, ComponentException {
		// for now this eda can't be trained
		// the scores are based only on the input text-hypothesis pair
	}
	
	/**
	* @param aCas
	* @return returns the pairID of the T-H pair
	*/
	//TODO: delete?
	/*
	String getPairID(JCas aCas) {
	  FSIterator<TOP> pairIter = aCas.getJFSIndexRepository().getAllIndexedFS(Pair.type);
	  Pair p = (Pair) pairIter.next();
	  return p.getPairID();
	}
	*/
	
	/**
	* @param aCas
	* @return if the T-H pair contains the gold answer, returns it; otherwise, returns null
	*/
	//TODO: delete?
	/*
	String getGoldLabel(JCas aCas) {
	  FSIterator<TOP> pairIter = aCas.getJFSIndexRepository().getAllIndexedFS(Pair.type);
	  Pair p = (Pair) pairIter.next();
	  if (null == p.getGoldAnswer() || p.getGoldAnswer().equals("") || p.getGoldAnswer().equals("ABSTAIN")) {
	    return null;
	  } else {
	  return p.getGoldAnswer();
	  }
	}
	*/
	
	
	public static void main (String [] args){
		
		try 
		{	
			CachedLAPAccess lap = new CachedLAPAccess(new LemmaLevelLapDE());
			GermanWordSplitter splitter = new GermanWordSplitter();
			int derivSteps = 2;
			String pathToGermaNet = "C:/germanet/germanet-7.0/germanet-7.0/GN_V70/GN_V70_XML";
			boolean useSynonymRelation = true;
			boolean useHypernymRelation = true;
			boolean useEntailsRelation = true;
			boolean useCausesRelation = true;
			
			SimpleEDA_DE seda = new SimpleEDA_DE(splitter, derivSteps, pathToGermaNet, useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);
			
			System.out.println("\n ENTAILMENT ");
			/*
			 * doesn't work because of lemma word|word
			seda.process(lap.generateSingleTHPairCAS("Anzeigen", "angezeigt")); //conversion
			seda.process(lap.generateSingleTHPairCAS("angezeigt", "Anzeigen")); //conversion
			seda.process(lap.generateSingleTHPairCAS("erworben", "erworbenen")); //conversion
			seda.process(lap.generateSingleTHPairCAS("Brennen", "gebrannte")); //conversion
			seda.process(lap.generateSingleTHPairCAS("Speichern", "gespeichert")); //conversion
			*/
			
			seda.process(lap.generateSingleTHPairCAS("Pudel", "Hund")); //hypernym
			seda.process(lap.generateSingleTHPairCAS("bekommt", "erhalten")); //synonym
			seda.process(lap.generateSingleTHPairCAS("erhalten", "bekommt")); //synonym
			seda.process(lap.generateSingleTHPairCAS("Meldungen", "meldet")); //derivation
			seda.process(lap.generateSingleTHPairCAS("meldet", "Meldungen")); //derivation
			seda.process(lap.generateSingleTHPairCAS("Erhalten der Meldung", "bekomme Meldung")); //synonym + conversion
			seda.process(lap.generateSingleTHPairCAS("Fehlermeldung erhalten", "Meldungen")); //decomposition + lemma
			seda.process(lap.generateSingleTHPairCAS("Fehlermeldung", "Meldungen")); //decomposition + lemma
			seda.process(lap.generateSingleTHPairCAS("Fehlermeldungen", "Meldung")); //decomposition + lemma
			seda.process(lap.generateSingleTHPairCAS("Fehlermeldung", "Fehler Meldungen")); //decomposition
			seda.process(lap.generateSingleTHPairCAS("Fehler Meldungen", "Fehlermeldung")); //decomposition
			seda.process(lap.generateSingleTHPairCAS("gebracht", "Bringen")); //derivation
			seda.process(lap.generateSingleTHPairCAS("Fehler Bringen", "Fehlers Bringen"));
			seda.process(lap.generateSingleTHPairCAS("Programm installieren", "Softwareinstallation")); //decomposition + hypernym
			seda.process(lap.generateSingleTHPairCAS("kein Ton", "ohne Ton")); //negation
			seda.process(lap.generateSingleTHPairCAS("keine Bearbeitung", "nicht bearbeitet"));
			seda.process(lap.generateSingleTHPairCAS("XML-Daten", "XML Daten")); //TODO: doesn't work yet
			
			System.out.println("\n NONENTAILMENT ");
			seda.process(lap.generateSingleTHPairCAS("2007", "2004"));
			seda.process(lap.generateSingleTHPairCAS("gebracht", "Fehlers gebracht"));
			seda.process(lap.generateSingleTHPairCAS("Meldung", "Fehlermeldungen"));
			seda.process(lap.generateSingleTHPairCAS("Hund", "Pudel"));
			
		} 
		catch (EDAException | ComponentException | IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
package eu.excitementproject.tl.experiments.OMQ;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalResourceException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ClassificationTEDecision;
import eu.excitementproject.eop.core.component.lexicalknowledge.derivbase.DerivBaseResource;
import eu.excitementproject.eop.core.component.lexicalknowledge.dewakdistributional.GermanDistSim;
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
	//TODO: replace by dkpro decompunder?
	private GermanWordSplitter splitter;
	//TODO: integrate similarity measure resources (GermanTransDmResource and GermanDistSim)
	private GermanTransDmResource gtdm;
	private GermanDistSim gds;
	private static Logger logger = Logger.getLogger("SimpleEDA_DE");
	
	/*******************************************************************
	 **   CONSTRUCTORS                                                **
	 *******************************************************************/
	
	/**
	 * Constructor activates the following scoring components:
	 * - base configuration (word form and lemma matching)
	 */
	public SimpleEDA_DE(){}
	
	/**
	 * Constructor activates the following scoring components:
	 * - base configuration (word form and lemma matching)
	 * - decomposition relation
	 * 
	 * @param splitter -- GermanWordSplitter
	 */
	
	public SimpleEDA_DE(GermanWordSplitter splitter){
		try {
			this.splitter = new GermanWordSplitter();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor activates the following scoring components:
	 * - base configuration scoring (word form and lemma matching)
	 * - derivation relation (resource: DerivBase)
	 * 
	 * @param derivSteps -- the maximum accepted amount of derivation steps between two derivationally related lemmas
	 */
	public SimpleEDA_DE(int derivSteps){
		try {
			boolean useScores = true;
			this.dbr = new DerivBaseResource(useScores, derivSteps);
		} 
		catch (ConfigurationException | ComponentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor activates the following scoring components:
	 * - base configuration (word form and lemma matching)
	 * - GermaNet relation 
	 * 
	 * @param germanetPath -- path to GermanNet top directory, which stores XML version of the resource (GN_V{version}_XML) 
	 */
	public SimpleEDA_DE(String germanetPath){
		try {
			this.gn = new GermaNetWrapper(germanetPath); 
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
	 * @param germanetPath -- path to GermanNet top directory, which stores XML version of the resource (GN_V{version}_XML) 
	 */
	public SimpleEDA_DE(int derivSteps, String germanetPath){
		try {
			this.dbr = new DerivBaseResource(true, derivSteps);
			this.gn = new GermaNetWrapper(germanetPath); 
		} catch (ConfigurationException | ComponentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor activates the following scoring components:
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
			this.dbr = new DerivBaseResource(true, derivSteps);
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
	 * @param germanetPath -- path to GermanNet top directory, which stores XML version of the resource (GN_V{version}_XML) 
	 */
	public SimpleEDA_DE(GermanWordSplitter splitter, int derivSteps, String germanetPath){
		try {
			this.splitter = splitter;
			this.dbr = new DerivBaseResource(true, derivSteps);
			this.gn = new GermaNetWrapper(germanetPath); 
		} catch (ConfigurationException | ComponentException e) {
			e.printStackTrace();
		}
	}
	
	
	/**********************************************************
	 **   METHODS                                            **
	 **********************************************************/
	
	@Override
	public ClassificationTEDecision process(JCas thPair) throws EDAException,
			ComponentException {
		
		int hTokenCount = 0; 		//count tokens in hypothesis	
		int hMappedTokenCount = 0; //count tokens mapped from t to h
		double confidence = 0;
		JCas textCas = null;
		String text = null;
		String hypothesis = null;
		JCas hypothesisCas = null;
		
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
		
		Iterator<Annotation> hypothesisIter = hypothesisCas.getAnnotationIndex(Token.type).iterator();
		while (hypothesisIter.hasNext()) 
		{
			Token hToken = (Token) hypothesisIter.next();
			String hText = hToken.getCoveredText(); 
			String hLemma = hToken.getLemma().getValue();
			
			hTokenCount++; 
			
			Iterator<Annotation> textIter = textCas.getAnnotationIndex(Token.type).iterator();
			tt: while (textIter.hasNext()) 
			{
				Token tToken = (Token) textIter.next();
				String tText = tToken.getCoveredText(); 
				String tLemma = tToken.getLemma().getValue();
				
				if(tText.equalsIgnoreCase(hText)){
					confidence +=1; 	
					hMappedTokenCount++; 
					logger.info("token matching (exact)  " + "(" + tText + " --> " + hText + ")"); 
					break tt;
				}
			
				if(!tLemma.equalsIgnoreCase("@card@") || !hLemma.equalsIgnoreCase("@card@"))
				{
					if(tLemma.equalsIgnoreCase(hLemma)){ 
						confidence +=0.98; 
						hMappedTokenCount++; 
						logger.info("token matching (lemmas or conversion): " + "(" + tText +" --> " + hText + ")");
						break tt;
					}
					
					//decomposition 
					if(splitter != null){
						//TODO: delete the whole if statement because break tt?
						if(!tLemma.equalsIgnoreCase(hLemma)){ 
							Collection<String> tLemmaDecompounded = splitter.splitWord(tLemma);
							for(String tLemmaCompoundPart : tLemmaDecompounded){
								if(tLemmaCompoundPart.replace("-", "").equalsIgnoreCase(hLemma)){
									confidence +=0.93; 
									hMappedTokenCount++; 
									logger.info("token matching (decomposition): " + "(" + tText + " --> " + hText + ")");
									break tt;
								}
							}
						}
					}
					
					//derivbase
					if(dbr != null){
						//TODO: delete the whole if statement because break tt?
						if(!tLemma.equalsIgnoreCase(hLemma)){ 
							if(!dbr.getRules(tLemma, null, hLemma, null).isEmpty()){
								confidence +=0.95; 
								hMappedTokenCount++; 
								logger.info("token matching: (derivation): " + "(" + tText + " --> " + hText + ")");
								break tt;
							}
						}
					}
					
					//germanet
					if(gn != null){
						//TODO: delete the whole if statement because break tt?
						if(!tLemma.equalsIgnoreCase(hLemma) ){ 
							if(!gn.getRules(tLemma, null, hLemma, null).isEmpty()){
								confidence +=0.901; 
								hMappedTokenCount++; 
								logger.info("token matching (germanet): "  + "(" + tText + " --> " + hText + ")");
								break tt;
							}
						}
					}
				} 
			}
		}
		}catch(LexicalResourceException | CASException e){
			e.printStackTrace();
		}
		
		if(hMappedTokenCount > 0 && hMappedTokenCount == hTokenCount){
			confidence = confidence/hTokenCount;
			logger.info("SEDA SAYS: ENTAILMENT (" + confidence + "): " + text + " --> " + hypothesis + " (" + hMappedTokenCount + "/" + hTokenCount + ")");
			return new ClassificationTEDecision(DecisionLabel.Entailment, confidence , null);
		}
		else{
			logger.info("SEDA SAYS: NONENTAILMENT (" + confidence + "): " + text + " VS. " + hypothesis  + " (" + hMappedTokenCount + "/" + hTokenCount + ")");
			return new ClassificationTEDecision(DecisionLabel.NonEntailment, 0.0, null);
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
	
	/*
	public static void main (String [] args){
		try 
		{
			GermanWordSplitter splitter = new GermanWordSplitter();
			String germanetPath = "C:/germanet/germanet-7.0/germanet-7.0/GN_V70/GN_V70_XML";
			int derivSteps = 2;
			SimpleEDA_DE seda = new SimpleEDA_DE(splitter, derivSteps, germanetPath);
			
			System.out.println("\n ENTAILMENT ");
			CachedLAPAccess lap = new CachedLAPAccess(new LemmaLevelLapDE());
			seda.process(lap.generateSingleTHPairCAS("Meldungen", "meldet"));
			seda.process(lap.generateSingleTHPairCAS("meldet", "Meldungen"));
			seda.process(lap.generateSingleTHPairCAS("bekomme Meldung", "bekomme meldet"));
			seda.process(lap.generateSingleTHPairCAS("Erhaltung Meldung", "erhält meldet"));
			seda.process(lap.generateSingleTHPairCAS("Fehlermeldung erhalten", "Meldungen"));
			seda.process(lap.generateSingleTHPairCAS("Fehlermeldung", "Meldungen"));
			seda.process(lap.generateSingleTHPairCAS("Fehlermeldungen", "Meldung"));
			seda.process(lap.generateSingleTHPairCAS("Fehlermeldung", "Fehler Meldungen"));
			seda.process(lap.generateSingleTHPairCAS("meldet", "Meldung"));
			seda.process(lap.generateSingleTHPairCAS("gebracht", "Bringen"));
			seda.process(lap.generateSingleTHPairCAS("meldete Fehler", "Fehlers Meldung"));
			seda.process(lap.generateSingleTHPairCAS("Fehler Bringen", "Fehlers Bringen"));
			
			System.out.println("\n NONENTAILMENT ");
			seda.process(lap.generateSingleTHPairCAS("2007", "2004"));
			seda.process(lap.generateSingleTHPairCAS("gebracht", "Fehlers gebracht"));
			seda.process(lap.generateSingleTHPairCAS("meldete Fehler", "Software Fehlers gemeldet"));
			seda.process(lap.generateSingleTHPairCAS("Fehler Meldungen", "Fehlermeldung"));
			seda.process(lap.generateSingleTHPairCAS("Meldung", "Fehlermeldungen"));

		} 
		catch (EDAException | ComponentException | IOException e) {
			e.printStackTrace();
		}
	}*/
	
}
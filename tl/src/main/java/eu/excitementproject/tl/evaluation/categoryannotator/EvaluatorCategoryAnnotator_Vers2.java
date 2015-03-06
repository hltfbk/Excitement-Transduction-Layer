package eu.excitementproject.tl.evaluation.categoryannotator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.uima.jcas.JCas;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalResourceException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.core.component.lexicalknowledge.derivbase.DerivBaseResource;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetRelation;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetWrapper;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.api.ConfidenceCalculator;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.composition.api.NodeMatcherWithIndex;
import eu.excitementproject.tl.composition.categoryannotator.CategoryAnnotatorAllCats;
import eu.excitementproject.tl.composition.confidencecalculator.ConfidenceCalculatorCategoricalFrequencyDistribution;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.ConfidenceCalculatorException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.composition.graphmerger.LegacyAutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLongestOnly;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLuceneSimple;
import eu.excitementproject.tl.composition.nodematcher.SEDANodeMatcherLongestOnly;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.DependencyAsFragmentAnnotatorForGerman;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.TokenAsFragmentAnnotatorForGerman;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAdjPPAsModifierAnnotator;
import eu.excitementproject.tl.experiments.OMQ.SEDAGraphBuilder;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.laputils.CategoryReader;
import eu.excitementproject.tl.laputils.DependencyLevelLapDE;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.laputils.POSTag_DE;
import eu.excitementproject.tl.laputils.WordDecompositionType;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;

/**
 * 
 * This class evaluates the category annotation on an incoming email (use case 2). 
 * 
 * It first reads in a dataset of emails associated to categories and splits it into a training and test set. 
 * It then builds an entailment graph (collapsed) from the training set, and annotates the emails in the test set
 * based on the generated entailment graph. Finally, it compares the automatically created categories to the 
 * manually annotated categories in the test set.
 * 
 * As the manually assigned categories are per email, whereas the automatically generated ones are assigend per 
 * entailment unit mention, we first calculate a combined score for each automatically assigned category
 * by summing up all confidences per category to get the "best" category (the one with the highest sum). 
 * This best category is then compared to the manually assigned one. 

 * @author Kathrin Eichler & Aleksandra Gabryszak
 *
 */

public class EvaluatorCategoryAnnotator_Vers2 {
	
	static Logger logger = Logger.getLogger(EvaluatorCategoryAnnotator_Vers2.class); 
	
	private static File temp; 
	private static File tempResult;
    private static PrintWriter writer; 
    private static PrintWriter writerResult; 
    public static PrintWriter writerFoEvalResult; 
	
	/** set automatically **/
	private static NodeMatcher nodeMatcher;
	private static NodeMatcherWithIndex nodeMatcherWithIndex;
	
	private static GraphOptimizer graphOptimizer;// set automatically;
	private static ConfidenceCalculator confidenceCalculator; //set automatically
	private static CategoryAnnotator categoryAnnotator; //set automatically
	
	private static String fragmentTypeName;//set automatically depending on setupToken, setupDependency, setupSentence
	
	private static CachedLAPAccess lap; //set automatically (if setupDependency > -1 || setupSentence > -1 then DependencyLevelLAPDE, else LemmaLevelLAPDE) 
	private static FragmentAnnotator fragAnnotatorToken; //set automatically (null if setupToken = -1)
	private static FragmentAnnotator fragAnnotatorDependency;//set automatically (null if setupDependency = -1)
	private static FragmentAnnotator fragAnnotatorSentence;//set automatically (null if setupSentence = -1)
	
	private static EDABasic<?> edaToken; //set automatically depending on setupToken (TIE or SEDA or null)
	private static EDABasic<?> edaDependency; //set automatically depending on setupDependency (TIE or SEDA or null)
	private static EDABasic<?> edaSentence; //set automatically depending on setupSentence (TIE or null)
	
	private static CommonConfig configEDAToken; //set automatically depending on setupToken 
	private static CommonConfig configEDASentence; //set automatically depending on setupSentence
	//no configEDADependency needed because SEDA-GraphBuilder should be used for dependencies
	
	private static SEDAGraphBuilder sedaGraphBuilderToken; //set automatically depending on setupToken (SEDAGraphBuilder or null)
	private static SEDAGraphBuilder sedaGraphBuilderDependency; //set automatically depending on setupDependency (SEDAGraphBuilder or null)
	//no sedaGraphBuilder for sentences needed because SEDA-GraphBuilder shouldn't be used for dependencies
	
	static char[] methodDocument = new char[3];
	
	/**** 
	 * SET CONFIGURATION 
	 * ******/
	private static FragmentGraphGenerator fragmentGraphGenerator;//to set in setup (change there if needed)
	private static ModifierAnnotator modAnnotatorSentence; //to set in setup (change there if needed), modifier annotator is not used for token and dependencies 
    private static String pathToGermaNet = "C:/germanet/germanet-7.0/germanet-7.0/GN_V70/GN_V70_XML"; //TODO:
    
    private static boolean LuceneSearch = false;
    private static int minTokenOccurrence = 1; //minimum occurrence of an EMAIL token for the corresponding node to be merged into the graph
    private static int minTokenOccurrenceInCategories = 1; //minimum occurrence of a CATEGORY token for the corresponding node to be merged into the graph
  	private static double thresholdForOptimizing = 0.8;
  	private static double thresholdForEDA = 0.8; //TODO @ALEKS: thresholdForSEDAGraphBuilder?
  	private static boolean processTrainingData = false; //TODO: needed for edaToken and edaDependency???
  	private static boolean trainEDAToken = false;
  	private static boolean trainEDASentence = false;
	private static int categoryBoost = 0; //boost confidence of category fragments in collapsed graph relevant when building the graph
	private static boolean lengthBoost = false; //relevant when categorizing new emails
	private static boolean addLemmaLabel = true; //will be overwritten with false if setupToken or setupDependency or setupSentence < 1
	private static boolean relevantTextProvided = true;
   
	//filters for fragments
    private static List<POSTag_DE> tokenPosFilter = Arrays.asList(new POSTag_DE [] 
    		{POSTag_DE.ADJA, POSTag_DE.ADJD, POSTag_DE.NE, POSTag_DE.NN, POSTag_DE.CARD, 
    		POSTag_DE.VVFIN, POSTag_DE.VVINF, POSTag_DE.VVIZU, POSTag_DE.VVIMP, POSTag_DE.VVPP}); //"ADV" = adverb, "FM" = foreign language material
  	
    private static List<POSTag_DE> governorPosFilter = Arrays.asList(new POSTag_DE [] 
  			{POSTag_DE.ADJA, POSTag_DE.ADJD, POSTag_DE.NE, POSTag_DE.NN, POSTag_DE.CARD,
  			POSTag_DE.VVFIN, POSTag_DE.VVINF, POSTag_DE.VVIZU, POSTag_DE.VVIMP, POSTag_DE.VVPP, 
  			POSTag_DE.PTKNEG, POSTag_DE.PTKVZ}); //"ADV" = adverb, "FM" = foreign language material
  	
    private static List<POSTag_DE> dependentPosFilter = governorPosFilter;
  	private static List<String> governorWordFilter = Arrays.asList(new String []{"ohne"});
  	private static List<String> dependentWordFilter = Arrays.asList(new String []{"keine", "keinerlei", "nicht", "nichts"});
  	
    /* SMART notation for tf-idf variants, as in Manning et al., chapter 6, p.128 */
	// Query (email) weighting: --> relevant when categorizing new emails
  	private static char termFrequencyQuery = 'l'; // n (natural), b (boolean: 1 if tf > 0), l (logarithm, sublinear tf scaling, as described by Manning et al. (2008), p. 126f.) 
  	private static char documentFrequencyQuery = 't'; // n (no), t (idf) + d (idf ohne log --> not part of SMART notation!)
  	private static char normalizationQuery = 'c'; // n (none), c (cosine)
	// Document (category) weighting: --> relevant when building the graph
  	private static char termFrequencyDocument = 'l'; // n (natural), l (logarithm)
	private static char documentFrequencyDocument = 't'; // n (no), t (idf)
	private static char normalizationDocument = 'c'; // n (none), c (cosine) //TODO: Implement? Don't think it's needed, as
	private static String method = "tfidf";
	
	private static int setupToken = -1; //use topArrays to set the parameter, set to -1 if no token fragments should be processed
	private static int setupDependency = -1; //use topArrays to set the parameter, set to -1 if no dependency fragments should be processed
	private static int setupSentence = -1;//use topArrays to set the parameter, set to -1 if no sentence fragments should be processed
	
	private static int [][] setupArrays = {
				{0, -1, -1}, //{0, -1, -1} = setupToken = 0 (no eda), setupDependency = -1, setupSentence = -1 (no token or dependency fragments processed)
				
				/*
				//TOKENS
				{201, -1, -1}, {202, -1, -1}, {212, -1, -1}, {203, -1, -1}, {204, -1, -1}, {214, -1, -1},
				
				//TOKEN + DEPENDENCy
				{0,  0, -1}
				{201,  201, -1}, {201,  202, -1}, {201,  212, -1}, {201,  203, -1}, {201,  204, -1}, {201,  214, -1},
				{202,  201, -1}, {202,  202, -1}, {202,  212, -1}, {202,  203, -1}, {202,  204, -1}, {202,  214, -1},
				{212,  201, -1}, {212,  202, -1}, {212,  212, -1}, {212,  203, -1}, {212,  204, -1}, {212,  214, -1},
				{204,  201, -1}, {204,  202, -1}, {204,  212, -1}, {204,  203, -1}, {204,  204, -1}, {204,  214, -1},
				{214,  201, -1}, {214,  202, -1}, {214,  212, -1}, {214,  203, -1}, {214,  204, -1}, {214,  214, -1},
				{207,  201, -1}, {207,  202, -1}, {207,  212, -1}, {207,  203, -1}, {207,  204, -1}, {207,  214, -1},
				{217,  201, -1}, {217,  202, -1}, {217,  212, -1}, {217,  203, -1}, {217,  204, -1}, {217,  214, -1},
				
				//TOKEN + SENTENCE
				{203, 207, 25}
				*/
				}; 
	private static int topN; //use topNArray to set the parameter
	private static int [] topNArray = {1};
	
	private static WordDecompositionType decompTypeToken = WordDecompositionType.NONE; 
	private static WordDecompositionType decompTypeDependency = WordDecompositionType.NONE; 
	/*
	private static WordDecompositionType decompTypeToken = WordDecompositionType.NO_RESTRICTION; 	
	private static WordDecompositionType decompTypeDependency = WordDecompositionType.NO_RESTRICTION; 
	*/
	
	private static boolean addLemmaEdgesDependencyToToken = false;
	private boolean removeTokenMatches = false; //set to true only if addLemmaEdgesDependencyToToken = true
	private static boolean bestNodeOnly = true; //if no edges are to follow while (relevant when categorizing new emails)
	private static double entailedNodeScore = 0.0; //has effect on evaluation only if > 0.0: then the entailed nodes are scored with the value, else the entailed nodes are scored based on graph edge confidences 
	private static boolean applyTransitiveClosure = false;
	
	private static boolean addSecondDataSetForGraphBuilding = false; 
	
	private static boolean skipEval = false;
	private static boolean readCollpasedGraphFromFile = false; //read collapsed graph from raw graph file
	private static boolean buildCollapsedGraphFromRawGraphFile = false; //build collapsed graph from raw graph File
	
	public static void main (String [] args) {
		
		String inputFoldername = "src/main/resources/exci/omq/emails/"; //dataset to be evaluated
		String outputGraphFoldername = "src/main/resources/exci/omq/graphs/"; //confidential/"; //output directory (for generated entailment graph)
		String categoriesFilename = inputFoldername + "omq_public_categories.xml"; 
		
		try {
			File tempEvalResult = File.createTempFile(("_eval_results_" + System.currentTimeMillis()), ".tmp");
			writerFoEvalResult = new PrintWriter(new FileOutputStream(tempEvalResult), true);
			
			boolean tmpReadCollpasedGraphFromFile = readCollpasedGraphFromFile;
		    boolean tmpBuildCollapsedGraphFromRawGraphFile = buildCollapsedGraphFromRawGraphFile;
		    boolean tmpAddLemmaLabel = addLemmaLabel;
		    
			for(int [] setup : setupArrays){
				setupToken = setup[0];
				setupDependency = setup[1];
				setupSentence = setup[2];
				if(setupToken == 0 || setupDependency == 0 || setupSentence == 0){
		    		addLemmaLabel = false;
		    	}
				
				EvaluatorCategoryAnnotator_Vers2 evc = new EvaluatorCategoryAnnotator_Vers2(setupToken, setupDependency, setupSentence);
				writerFoEvalResult.print("**************************************************" +
						setupToken + " " + setupDependency + " " + setupSentence +
						" *****************************************************");
				
				if(skipEval){
					topNArray = new int [] {1};
	            }
				
				for (int i = 0; i<topNArray.length;i++){
					topN = topNArray[i]; 
                    if (i > 0){
                        readCollpasedGraphFromFile = true;
                        buildCollapsedGraphFromRawGraphFile = false;
                    }
//               	evc.runIncrementalEvaluation(inputFoldername, outputGraphFoldername, categoriesFilename);
                    evc.runEvaluationThreeFoldCross(inputFoldername, outputGraphFoldername, categoriesFilename);
                    writerFoEvalResult.flush();
				}
				readCollpasedGraphFromFile = tmpReadCollpasedGraphFromFile;
				buildCollapsedGraphFromRawGraphFile = tmpBuildCollapsedGraphFromRawGraphFile;
				addLemmaLabel = tmpAddLemmaLabel;
				writer.close();
				writerResult.close();
			}
			writerFoEvalResult.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public EvaluatorCategoryAnnotator_Vers2(int setupToken, int setupDependency, int setupSentence) 
			throws ConfigurationException, EDAException, ComponentException, GraphMergerException, 
			FragmentAnnotatorException, ModifierAnnotatorException {
		setup(setupToken, setupDependency, setupSentence);
	}

	
	/**
	 * Setup for token, dependency and sentence fragments
	 * Set LAP, FragmentAnnotators, ModifierAnnotatorsfor, EDA, SEDAGraphBuilder,
	 * GraphMerger, GraphOptimizer, evaluation method, ConfidenceCalculator
	 * and CategoryAnnotator
	 * 
	 * @param setupToken
	 * @param setupDependency
	 * @param setupSentence
	 * @throws GraphMergerException
	 * @throws FragmentAnnotatorException
	 * @throws ConfigurationException
	 * @throws EDAException
	 * @throws ComponentException
	 * @throws ModifierAnnotatorException
	 */
	private void setup(int setupToken, int setupDependency, int setupSentence) 
			throws GraphMergerException, FragmentAnnotatorException, ConfigurationException, EDAException, 
			ComponentException, ModifierAnnotatorException{
		
		if((setupToken == 0 && (setupDependency >=1 || setupSentence >=1))
				|| (setupDependency == 0 && (setupToken >=1 || setupSentence >=1))
				|| (setupSentence == 0 && (setupToken >=1 || setupDependency >=1))){
			System.err.print("WRONG SETUP: no combination of setup == 0 and setup > 0 possible ");
			System.exit(1);
		}
		
		setFragmentTypeName(setupToken, setupDependency, setupSentence);
		setLAP(setupToken, setupDependency, setupSentence);
		
		fragmentGraphGenerator = new FragmentGraphGeneratorFromCAS(); //FragmentGraphGeneratorFromCAS()FragmentGraphLiteGeneratorFromCAS();
		//setup for token fragments and graphs
		if(setupToken > -1){
			fragAnnotatorToken = new TokenAsFragmentAnnotatorForGerman(lap, tokenPosFilter, decompTypeToken);
			if(setupToken > 0){
				edaToken = getEDA(setupToken);
				configEDAToken = getCommonConfig(setupToken);
				if(edaToken == null){
					sedaGraphBuilderToken = getSEDAGraphBuilder(setupToken);
					if(sedaGraphBuilderToken == null){
						System.err.print("PLease, set EDA od SEDAGraphBuilder for setupToken: " + setupToken);
						System.exit(1);
					}
				}
			}
		}
		
		//setup for dependency fragments
		if(setupDependency > -1){
			fragAnnotatorDependency = new DependencyAsFragmentAnnotatorForGerman(lap, governorPosFilter, 
					governorWordFilter, dependentPosFilter, dependentWordFilter, decompTypeDependency);
			if(setupDependency > 0){
				edaDependency = getEDA(setupDependency);
				if(edaDependency == null){
					sedaGraphBuilderDependency = getSEDAGraphBuilder(setupDependency);
					if(sedaGraphBuilderDependency == null){
						System.err.print("PLease, set EDA od SEDAGraphBuilder for setupDependency: " + setupToken);
						System.exit(1);
					}
				}
			}
		}
		
		//setup for sentence fragments and graphs
		if(setupSentence > -1){
			fragAnnotatorSentence = new SentenceAsFragmentAnnotator(lap);
			modAnnotatorSentence = new AdvAdjPPAsModifierAnnotator(lap); //AdvAdjPPAsModifierAnnotator(lap, checkNegation), AdvAdjAsModifierAnnotator(lap); 
			if(setupSentence > 0){
				edaSentence = getEDA(setupSentence);
				configEDASentence = getCommonConfig(setupSentence);
				if(edaSentence == null){
					System.err.print("PLease, set EDA for setupSentence: " + setupToken);
					System.exit(1);
				}
			} 
		}
		
		//setup collapsed graph
		methodDocument[0] = termFrequencyDocument;
		methodDocument[1] = documentFrequencyDocument;
		methodDocument[2] = normalizationDocument;
		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		categoryAnnotator = new CategoryAnnotatorAllCats();
		
		 try {
			 temp = File.createTempFile(createCollapsedGraphFileName("debug_" + System.currentTimeMillis()), ".tmp");
			 writer = new PrintWriter(temp, "UTF-8");
			 System.out.println("Created file at " + temp.getAbsolutePath());
			 tempResult = File.createTempFile(createCollapsedGraphFileName("debug_result_" + System.currentTimeMillis()), ".tmp");
			 writerResult = new PrintWriter(tempResult, "UTF-8");
			 System.out.println("Created file at " + tempResult.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Set LAPAccess
	 * 
	 * @param setupToken
	 * @param setupDependency
	 * @param setupSentence
	 * @throws LAPException
	 */
	private static void setLAP(int setupToken, int setupDependency, int setupSentence) throws LAPException{
		if(setupDependency > -1 || setupSentence > -1){
			lap = new CachedLAPAccess(new DependencyLevelLapDE());
		}
		else {
			lap = new CachedLAPAccess(new LemmaLevelLapDE());
		}
	}

	/**
	 * 
	 * @param setupToken
	 * @param setupDependency
	 * @param setupSentence
	 */
	private static void setFragmentTypeName(int setupToken,
			int setupDependency, int setupSentence) {
		fragmentTypeName = "";
		if(setupToken > -1){
			fragmentTypeName += "T";
		}
		if(setupDependency > -1){
			fragmentTypeName += "D";
		}
		if(setupSentence > -1){
			fragmentTypeName += "S";
		}
		fragmentTypeName += "F";
	}
	
	/**
	 * 
	 * @param setup
	 * @return
	 * @throws ConfigurationException
	 */
	private static CommonConfig getCommonConfig(int setup) throws ConfigurationException{
		CommonConfig config;
		
		switch(setup){
		case 21: //TIE ADAPTED BASE (no mapping @CARD@ --> @CARD@)
    			//CHANGE IN POM EOP CORE VERSION TO 1.1.3_ADAPTED (only available for Kathrin, Aleksandra, Florian)
	    		String configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml";
	    		File configFile = new File(configFilename);
	    		config = new ImplCommonConfig(configFile);
	    		return config;
			case 23:  //TIE ADAPTED BASE (no mapping @CARD@ --> @CARD@) + DERIVBASE (no POS restriction, derivSteps2)
	    		//CHANGE IN POM EOP CORE VERSION TO 1.1.3_ADAPTED (only available for Kathrin, Aleksandra, Florian)
	    		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+DB2_DE.xml";
	    		configFile = new File(configFilename);
	    		config = new ImplCommonConfig(configFile);
	    		return config;
			case 25: //TIE with base configuration + GN+DS+DB+TP+TPPos+TS_DE.xml
        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+GN+DS+DB+TP+TPPos+TS_DE.xml";
        		configFile = new File(configFilename);
        		config = new ImplCommonConfig(configFile);
	    		return config;
		}
		return null;
	}
	
	/**
	 * 
	 * @param setup
	 * @return
	 * @throws ConfigurationException
	 * @throws EDAException
	 * @throws ComponentException
	 */
	private static EDABasic<?> getEDA(int setup) throws ConfigurationException, EDAException, ComponentException {
		//TODO: change the implementation if 
		//SEDA and not SEDAGRaphBuilder should be used
		CommonConfig config = getCommonConfig(setup); 
		EDABasic<?> eda = null;
		if(config!=null){
			eda = new MaxEntClassificationEDA();
			eda.initialize(config);
		}
		return eda;
	}
	
	/**
	 * 
	 * @param setup
	 * @return
	 * @throws ConfigurationException
	 * @throws ComponentException
	 */
	private static SEDAGraphBuilder getSEDAGraphBuilder(int setup) 
			throws ConfigurationException, ComponentException{
		
		DerivBaseResource dbr;
		GermaNetWrapper gnw;
		GermaNetRelation [] relations = {GermaNetRelation.has_synonym, GermaNetRelation.has_hypernym, GermaNetRelation.causes, 
				GermaNetRelation.entails, GermaNetRelation.has_hyponym};
		List<GermaNetRelation> germaNetRelations; 
		GermanWordSplitter splitter; 
		boolean mapNegation; 
		boolean onlyBidirectionalEdges;
		
		switch(setup){
		/** 201 - 204 are almost the same as TIE. The differences are: 
		 * - SEDA always includes Conversion (even if DerivBase is not activ)
		 * - SEDA performs better if lemma has form lemma|lemma
		 * - if decomposition is used, then there might be some errors in building edges due to not optimal lemmatizing **/
		case 201: //Lemma+Conversion
			dbr = null;
			gnw = null;
			germaNetRelations = null;
			splitter = null;
			onlyBidirectionalEdges = false;
			mapNegation = false;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
		case 202: //Lemma+Conversion, GermaNet (all TE relations)
			dbr = null;
			gnw = new GermaNetWrapper(pathToGermaNet);
			germaNetRelations =  Arrays.asList(relations);
			splitter = null;
			onlyBidirectionalEdges = false;
			mapNegation = false;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
		case 212: //Lemma+Conversion, GermaNet (only synonyms)
			dbr = null;
			gnw = new GermaNetWrapper(pathToGermaNet);
			germaNetRelations =  Arrays.asList(relations);
			splitter = null;
			onlyBidirectionalEdges = true;
			mapNegation = false;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
		case 203: //Lemma+Conversion, Derivation 2
			dbr = new DerivBaseResource(true, 2);
			gnw = null;
			germaNetRelations =  null;
			splitter = null;
			onlyBidirectionalEdges = false;
			mapNegation = false;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
		case 204: //Lemma+Conversion, Derivation,  GermaNet (all TE relations)
			dbr = new DerivBaseResource(true, 2);
			gnw = new GermaNetWrapper(pathToGermaNet);
			germaNetRelations =  Arrays.asList(relations);
			splitter = null;
			onlyBidirectionalEdges = false;
			mapNegation = false;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
		case 214: //Lemma+Conversion, Derivation, GermaNet (only synonyms)
			dbr = new DerivBaseResource(true, 2);
			gnw = new GermaNetWrapper(pathToGermaNet);
			germaNetRelations =  Arrays.asList(relations);
			splitter = null;
			onlyBidirectionalEdges = true;
			mapNegation = false;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
    	case 207: //Lemma+Conversion, Derivation, GermaNet (all TE relations), mapping negation
			dbr = new DerivBaseResource(true, 2);
			gnw = new GermaNetWrapper(pathToGermaNet);
			germaNetRelations =  Arrays.asList(relations);
			splitter = null;
			onlyBidirectionalEdges = false;
			mapNegation = true;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
    	case 217: //Lemma+Conversion, Derivation, GermaNet (only synonyms), mapping negation
			dbr = new DerivBaseResource(true, 2);
			gnw = new GermaNetWrapper(pathToGermaNet);
			germaNetRelations =  Arrays.asList(relations);
			splitter = null;
			onlyBidirectionalEdges = true;
			mapNegation = true;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
    	
    	case 208://207 without mapNegation
			dbr = new DerivBaseResource(true, 2);
			gnw = new GermaNetWrapper(pathToGermaNet);
			germaNetRelations =  Arrays.asList(relations);
			splitter = null;
			onlyBidirectionalEdges = false;
			mapNegation = false;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
    	case 209: //207 without GermaNet
			dbr = new DerivBaseResource(true, 2);
			gnw = null;
			germaNetRelations =  null;
			splitter = null;
			onlyBidirectionalEdges = false;
			mapNegation = true;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
    	case 210: //207 without DerivBase
    		dbr = null;
			gnw = new GermaNetWrapper(pathToGermaNet);
			germaNetRelations =  Arrays.asList(relations);
			splitter = null;
			onlyBidirectionalEdges = false;
			mapNegation = true;
			return new SEDAGraphBuilder(dbr, gnw, germaNetRelations, splitter, mapNegation, onlyBidirectionalEdges, applyTransitiveClosure);
		}
		return null;
	}
	
	/**************************************************************************************************
	 *                METHODS TO  BUILD FRAGMENT GRAPHS                                               *
	 **************************************************************************************************/
	
	/**
	 * 
	 * @param interaction
	 * @param relevantTextProvided
	 * @param fragmentAnnotator
	 * @param modifierAnnotator
	 * @param fragmentGraphGenerator
	 * @return
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 * @throws LAPException
	 */
	private Set<FragmentGraph> buildFragmentGraphs(Interaction interaction, boolean relevantTextProvided, 
			FragmentAnnotator fragmentAnnotator, ModifierAnnotator modifierAnnotator, 
			FragmentGraphGenerator fragmentGraphGenerator) throws FragmentAnnotatorException,
				ModifierAnnotatorException, FragmentGraphGeneratorException, LAPException {

		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();
		logger.info("-----------------------------------------------------");
		logger.info("Processing graph interaction " + interaction.getInteractionId() + " with category " + interaction.getCategoryString());
		List<JCas> cases;
		JCas cas;
		try {
			cases = interaction.createAndFillInputCASes(relevantTextProvided);
			for (int j=0; j<cases.size(); j++) {
				cas = cases.get(j);
				logger.info("Gold category/ies: " + CASUtils.getTLMetaData(cas).getCategory());
				if (relevantTextProvided && CASUtils.getTLMetaData(cas).getCategory().contains(",")) { //Shouldn't happen because we create a separate CAS for each category assignment
					logger.info("Category contains comma in " + this.getClass());
					System.exit(0);
				}
				fragmentAnnotator.annotateFragments(cas);
				if (modifierAnnotator != null && 
						cas.getAnnotationIndex(DeterminedFragment.type).size() > 0) {
					modifierAnnotator.annotateModifiers(cas);
				}
				logger.debug("Adding fragment graphs for text: " + cas.getDocumentText());
				fgs.addAll(fragmentGraphGenerator.generateFragmentGraphs(cas));
			}
		} catch (LAPException e) {
			e.printStackTrace();
		}
		return fgs;
	}
	
	/**
	 * 
	 * @param interactions
	 * @param setupToken
	 * @param setupDependency
	 * @param setupSentence
	 * @param relevantTextProvided
	 * @return
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 */
	private Set<FragmentGraph> buildFragmentGraphs(List<Interaction> interactions, 
			int setupToken, int setupDependency, int setupSentence, boolean relevantTextProvided) 
					throws LAPException, FragmentAnnotatorException, ModifierAnnotatorException, FragmentGraphGeneratorException {
		Set<FragmentGraph> fragmentGraphs = new HashSet<FragmentGraph>();
		for(Interaction interaction : interactions){
			fragmentGraphs.addAll(
					buildFragmentGraphs(interaction, setupToken, setupDependency, setupSentence, relevantTextProvided));
		}
		return fragmentGraphs;
	}
	
	/**
	 * 
	 * @param interaction
	 * @param setupToken
	 * @param setupDependency
	 * @param setupSentence
	 * @param relevantTextProvided
	 * @return
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 */
	private Set<FragmentGraph> buildFragmentGraphs(Interaction interaction, 
			int setupToken, int setupDependency, int setupSentence, boolean relevantTextProvided) 
					throws LAPException, FragmentAnnotatorException, ModifierAnnotatorException, FragmentGraphGeneratorException {
		Set<FragmentGraph> fragmentGraphs = new HashSet<FragmentGraph>();
		if(setupToken > -1){
			fragmentGraphs.addAll(
					buildFragmentGraphs(interaction, relevantTextProvided, fragAnnotatorToken, null, fragmentGraphGenerator));
		}
		if(setupDependency > -1){
			fragmentGraphs.addAll(
					buildFragmentGraphs(interaction, relevantTextProvided, fragAnnotatorDependency, null, fragmentGraphGenerator));
		}
		if(setupSentence > -1){
			fragmentGraphs.addAll(
					buildFragmentGraphs(interaction, relevantTextProvided, fragAnnotatorSentence, modAnnotatorSentence, fragmentGraphGenerator));
		}
		return fragmentGraphs;
	}
	
	/**************************************************************************************************
	 *                METHODS TO  BUILD GRAPHS                                                        *
	 **************************************************************************************************/
	
	/**
	 * 
	 * @param eda
	 * @param config
	 * @param edaTrainingFilename
	 * @param processTrainingData
	 * @throws IOException
	 * @throws ConfigurationException
	 * @throws EDAException
	 * @throws ComponentException
	 */
	private void trainEDA(EDABasic<?> eda, CommonConfig config, String edaTrainingFilename, boolean processTrainingData) 
			throws IOException, ConfigurationException, EDAException, ComponentException {
		if(eda instanceof MaxEntClassificationEDA){
			//Step 1: process training data
			if (processTrainingData) { 
				logger.info("Setting EDA training file " + edaTrainingFilename);	    
				File trainingFile = new File(edaTrainingFilename); //training input file
				File outputDir = new File("./target/DE/dev/"); // output dir as written in configuration!
				if (!outputDir.exists()) outputDir.mkdirs();
				logger.info("Reading " + trainingFile.getCanonicalPath());
				lap.processRawInputFormat(trainingFile, outputDir); //process training data and store output
				logger.info("Processing training data."); 
			} 
			//Step 2: train EDA
			eda.startTraining(config); //train EDA (may take a some time) //TODO: comment in
			logger.info("Training completed."); 
		}		
	}
	
	/**
	 * Builds raw graph from a list of fragment graphs. 
	 * 
	 * @param fgs
	 * @param eda
	 * @param sedaGraphBuilder
	 * @param egr
	 * @param minOccurrence
	 * @return
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 * @throws GraphMergerException
	 * @throws TransformerException
	 * @throws EntailmentGraphRawException
	 * @throws LexicalResourceException
	 */
	private EntailmentGraphRaw buildRawGraph(Set<FragmentGraph> fgs, 
			EDABasic<?> eda, SEDAGraphBuilder sedaGraphBuilder, EntailmentGraphRaw egr, int minOccurrence) 
			throws LAPException, FragmentAnnotatorException, ModifierAnnotatorException, FragmentGraphGeneratorException, GraphMergerException, TransformerException, EntailmentGraphRawException, LexicalResourceException{
		
		if(eda == null && sedaGraphBuilder == null){
//			System.out.println("Building Raw Graph without EDA....");
			for(FragmentGraph fg : fgs){
				for (EntailmentUnitMention eum : fg.vertexSet()) {
					egr.addEntailmentUnitMention(eum, fg.getCompleteStatement().getTextWithoutDoubleSpaces());					
				}
			}
		} else {
			Set<FragmentGraph> fgsReduced = new HashSet<FragmentGraph>();
			Set<FragmentGraph> fgsRest = new HashSet<FragmentGraph>();
			String text = "";
			HashMap<String, Integer> tokenOccurrences = computeTokenOccurrences(fgs);
			for (FragmentGraph fg : fgs) {
				text = fg.getBaseStatement().getTextWithoutDoubleSpaces();
				//TODO: get lemma and use it as node name?
				if (
					//	GermaNetLexicon.contains(text) ||  	// merge only fragments with an entry in GermaNet
					tokenOccurrences.get(text.toLowerCase()) >= minOccurrence) {// merge only fragments occurring at least as often as the threshold
					fgsReduced.add(fg);  
				}
				else { 
					fgsRest.add(fg); //add remaining fragments to graph (no EDA call --> no edges)
				}
			}
			logger.info("fgs contains " + fgs.size() + " fgs");
			logger.info("fgsreduced contains " + fgsReduced.size() + " fgs");
			
			if(eda != null){
//				System.out.println("Building Raw Graph using " + eda.getClass().getName());
				if(trainEDASentence){
					
				}
				GraphMerger graphMerger = new LegacyAutomateWP2ProcedureGraphMerger(lap, eda);
				graphMerger.setEntailmentConfidenceThreshold(thresholdForEDA);
				egr = graphMerger.mergeGraphs(fgsReduced, egr);
			}
			else if(sedaGraphBuilder != null){
//				System.out.println("Building Raw Graph using " + sedaGraphBuilder.getClass().getName());
					egr = sedaGraphBuilder.mergeIntoGraph(fgsReduced, egr);
			}
			
			logger.info("Merged graph: " +egr.vertexSet().size()+ " nodes");
			
			for (FragmentGraph fg : fgsRest) {
				for (EntailmentUnitMention eum : fg.vertexSet()) {
					egr.addEntailmentUnitMention(eum, fg.getCompleteStatement().getTextWithoutDoubleSpaces());	
				}
			}
			logger.info("Added " + fgsRest.size() + " remaining mentions");
		}
		
//		System.out.println("Raw graph has nodes: " + egr.vertexSet().size());
//		System.out.println("Raw graph has edges: " + egr.edgeSet().size());
		return egr;
	}
	
	/**
	 * Builds raw graph from a list of interactions. 
	 * 
	 * @param interactions
	 * @param setupToken
	 * @param singleTokenRawGraph
	 * @param setupDependency
	 * @param dependencyRawGraph
	 * @param setupSentence
	 * @param sentenceRawGraph
	 * @param relevantTextProvided
	 * @param minOccurrence
	 * @return
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 * @throws GraphMergerException
	 * @throws TransformerException
	 * @throws EntailmentGraphRawException
	 * @throws LexicalResourceException
	 */
	private EntailmentGraphRaw buildRawGraph(List<Interaction> interactions, 
			int setupToken,  EntailmentGraphRaw singleTokenRawGraph,  
			int setupDependency, EntailmentGraphRaw dependencyRawGraph,
			int setupSentence, EntailmentGraphRaw sentenceRawGraph,
			boolean relevantTextProvided, int minOccurrence) 
					throws LAPException, FragmentAnnotatorException, ModifierAnnotatorException, FragmentGraphGeneratorException, GraphMergerException, TransformerException, EntailmentGraphRawException, LexicalResourceException{
		
		//build token graph
		if(setupToken > -1){
			Set<FragmentGraph> tokenfragmentGraphs = buildFragmentGraphs(interactions, setupToken, -1, -1, relevantTextProvided);
			singleTokenRawGraph = buildRawGraph(tokenfragmentGraphs, edaToken, sedaGraphBuilderToken, singleTokenRawGraph, minOccurrence);
		}
		//build dependency graph
		if(setupDependency > -1){
			Set<FragmentGraph> dependencyfragmentGraphs = buildFragmentGraphs(interactions, -1,  setupDependency, -1, relevantTextProvided);
			dependencyRawGraph = buildRawGraph(dependencyfragmentGraphs, edaDependency, sedaGraphBuilderDependency, dependencyRawGraph, minOccurrence);
		}
		//build sentence graph
		if(setupSentence > -1){
			Set<FragmentGraph> sentencefragmentGraphs = buildFragmentGraphs(interactions, -1,  -1, setupSentence, relevantTextProvided);
			sentenceRawGraph = buildRawGraph(sentencefragmentGraphs, edaSentence, null, dependencyRawGraph, minOccurrence);
			
		}
		
		//copy single graphs into a big one
		EntailmentGraphRaw egr;
		if(addLemmaEdgesDependencyToToken && setupToken > 0 && setupDependency > 0){//case: copy and create lemma edges between dependency and token nodes
			EntailmentGraphRaw dependAndTokenGraph = this.addLemmaEdgesDependencyToToken(dependencyRawGraph, singleTokenRawGraph);
			egr = dependAndTokenGraph;
			egr.copyRawGraphNodesAndAllEdges(sentenceRawGraph);
		}
		else{//case: no lemma edges between dependency and token nodes
			egr = new EntailmentGraphRaw(addLemmaLabel); 
			egr.copyRawGraphNodesAndAllEdges(singleTokenRawGraph);
			egr.copyRawGraphNodesAndAllEdges(dependencyRawGraph);
			egr.copyRawGraphNodesAndAllEdges(sentenceRawGraph);
		}
		
//		System.out.println("Raw graph has nodes: " + egr.vertexSet().size());
//		System.out.println("Raw graph has edges: " + egr.edgeSet().size());
		return egr;
	}
	
	/**
	 * Copy dependency and single token graph with lemma labels 
	 * into a new created graph and add lemma edges between dependencies and tokens
	 * 
	 * @param dependencyEgr
	 * @param tokenEgr
	 * @return
	 */
	private EntailmentGraphRaw addLemmaEdgesDependencyToToken(EntailmentGraphRaw dependencyEgr, EntailmentGraphRaw tokenEgr){
		EntailmentGraphRaw egr = null;
		
		if(dependencyEgr.hasLemmatizedLabel() && tokenEgr.hasLemmatizedLabel()){
			//set lemma label
			boolean addLemmaLabel = true;
			egr = new EntailmentGraphRaw(addLemmaLabel);
			//copy both input graphs into the new one
			egr.copyRawGraphNodesAndAllEdges(dependencyEgr);
			egr.copyRawGraphNodesAndAllEdges(tokenEgr);
			Set<EntailmentUnit> dependencyVertexSet = dependencyEgr.vertexSet();
			Set<EntailmentUnit> tokenVertexSet = tokenEgr.vertexSet();	
			//add edges between the dependencies and tokens
			for(EntailmentUnit dependencyVertex : dependencyVertexSet){
				List<String> depTextParts = Arrays.asList(dependencyVertex.getTextWithoutDoubleSpaces().split("\\s+"));
				List<String> depLemmaParts = Arrays.asList(dependencyVertex.getLemmatizedText().split("\\s+"));
				for(EntailmentUnit tokenVertex : tokenVertexSet){
					if(depTextParts.size() == 2){
						if(depTextParts.contains(tokenVertex.getTextWithoutDoubleSpaces())
								|| depLemmaParts.contains(tokenVertex.getLemmatizedText())){
							EntailmentUnit source = egr.getVertexWithText(dependencyVertex.getTextWithoutDoubleSpaces());
							EntailmentUnit target = egr.getVertexWithText(tokenVertex.getTextWithoutDoubleSpaces());
							if(!egr.isEntailment(source, target)){
								egr.addEdgeByInduction(source, target, DecisionLabel.Entailment, 0.91);
							}
						}
					}
				}
			}
		}
		
		return egr;
	}
	
	/**
	 * 
	 * @param egr
	 * @return
	 * @throws GraphOptimizerException
	 * @throws ConfidenceCalculatorException
	 */
	private EntailmentGraphCollapsed buildCollapsedGraphWithCategoryInfo(
			EntailmentGraphRaw egr) throws GraphOptimizerException,
			ConfidenceCalculatorException {
		logger.info("Merged graph contains " + egr.vertexSet().size() + " nodes and " + egr.edgeSet().size() + " edges");
		EntailmentGraphCollapsed graph = graphOptimizer.optimizeGraph(egr, thresholdForOptimizing);
		logger.info("Built collapsed graph.");		
		confidenceCalculator.computeCategoryConfidences(graph);
		logger.info("Computed category confidences and added them to graph.");		
		return graph;
	}
	
	/**
	 * Creates name for a collapsed graph file
	 * @param prefix
	 * @return
	 */
	private String createCollapsedGraphFileName(String prefix) {
		String name = prefix + "_" + fragmentTypeName + "__"
				+ setupToken + "_TF_dec" + decompTypeToken + "_" 
				+ setupDependency + "_DF_dec" + decompTypeDependency + "_"  
				+ setupSentence + "_SF_" 
				+ method + "_" +  termFrequencyDocument + documentFrequencyDocument + normalizationDocument 
				+ "_CB_" + categoryBoost 
				+ "_TRESHOPT_" + thresholdForOptimizing 
				+ "_LE_" + addLemmaEdgesDependencyToToken
				+ "_TC_" + applyTransitiveClosure
				+ "_ADDDATA_" + addSecondDataSetForGraphBuilding
				+ ".xml";
		return name;
	}

	/**
	 * Creates name for a raw graph file
	 * 
	 * @param prefix
	 * @return
	 */
	private String createRawGraphFileName(String prefix) {
		String name = prefix + "_" + fragmentTypeName + "__"
				+ setupToken + "_TF_dec" + decompTypeToken + "_" 
				+ setupDependency + "_DF_dec" + decompTypeDependency + "_"  
				+ setupSentence + "_SF_" 
				+ "treshgb" + thresholdForEDA
				+ "_LE" + addLemmaEdgesDependencyToToken 
				+ "_TC_" + applyTransitiveClosure
				+ "_ADDDATA_" + addSecondDataSetForGraphBuilding
				+ ".xml";
		return name;
	}
	
	/**************************************************************************************************
	 *                EVALUATION METHODS                                                              *
	 **************************************************************************************************/
	
	/**
	 * This evaluation simulates the real use case. It goes through the list of interactions, 
	 * creates a graph from the first interaction and annotates the second interaction based on this graph. 
	 * It then adds the second one to the graph and annotates the third, and so on, 
	 * evaluating the categorization result in each step. 
	 * 
	 * This evaluation can also be used to find out what's the best graph size
	 * (adding new nodes doesn't improve the result)
	 * 
	 * @throws Exception 
	 */
	public void runIncrementalEvaluation(String inputDataFoldername, String outputGraphFoldername, String categoriesFilename) throws Exception {

		//some prints for debugging, don't change it to logger!!!
		String setupMetaData = createEvaluationDescription();
        System.out.println(setupMetaData);
        
    	writer.println(setupMetaData);
    	writerResult.println(setupMetaData);
		
		List<Double> accuracyPerRun = new ArrayList<Double>();
		String emailFileName = "omq_public_1_emails_unsorted.xml";
//		String emailFileName = "omq_public_emails_all_unsorted.xml";
		String documentsFilename = inputDataFoldername + emailFileName; //TODO: replace 1 by "all" at some point
		
		JCas cas = CASUtils.createNewInputCas();
		String mostProbableCat; 
		int countPositive = 0;
		int run = 1;

		//Read and documents for annotation and sort by interaction ID
		logger.info("Reading documents from " + documentsFilename);	    		
		List<Interaction> docs = new ArrayList<Interaction>();
		docs.addAll(InteractionReader.readInteractionXML(new File(documentsFilename)));
        
        //TODO: @KATHRIN, REMOVE?
        //Build fragment graphs and compute token occurrences (for filtering fragments to be merged)
        /*
        Set<FragmentGraph> allFgs = new HashSet<FragmentGraph>();
		Set<FragmentGraph> fgs = buildFragmentGraphs(docs, relevantTextProvided, 
				null, null, null);
		allFgs.addAll(fgs);
		HashMap<String,Integer> tokenOccurrences = computeTokenOccurrences(allFgs);
         */
        //TODO: Think about how to integrate filtering using different kinds of frags

        
        /** STEP 1: Build graph from category texts (assuming they are available beforehand) 
         * **/
      	List<Interaction> graphDocs = new ArrayList<Interaction>(); 
      	graphDocs.addAll(CategoryReader.readCategoryXML(new File(categoriesFilename)));
      	logger.info("Added " + graphDocs.size() + " categories");
      	
    	String graphFolderName = outputGraphFoldername + "/incremental/" + emailFileName.replace(".xml", "") 
    			+ "_" + setupToken + "_" + setupDependency + "_" + setupSentence;
//      	File graphFolder = new File(graphFolderName);
      
      	String collapsedGraphName = graphFolderName + "/" +  createCollapsedGraphFileName("omq_public_categories_" + run + "_collapsed_graph");
		String rawGraphName = graphFolderName + "/" + createRawGraphFileName("omq_public_categories_" + run + "_raw_graph");
		File collapsedGraphFile = new File(collapsedGraphName);
    	File rawGraphFile = new File(rawGraphName);
    	EntailmentGraphCollapsed egc = new EntailmentGraphCollapsed();
    	EntailmentGraphRaw egr = new EntailmentGraphRaw(addLemmaLabel);
    	EntailmentGraphRaw singleTokenRawGraph = new EntailmentGraphRaw(addLemmaLabel);
      	EntailmentGraphRaw dependencyRawGraph = new EntailmentGraphRaw(addLemmaLabel);
      	EntailmentGraphRaw sentenceRawGraph = new EntailmentGraphRaw(addLemmaLabel);
      	
    	if (readCollpasedGraphFromFile) { // read graph
    			logger.info("Reading collapsed graph from " + collapsedGraphFile.getAbsolutePath());
	    		egc = new EntailmentGraphCollapsed(collapsedGraphFile);
	    		logger.info("Collapsed graph: number of nodes / edges " + egc.vertexSet().size() + egc.edgeSet().size());
    	}
		else if(buildCollapsedGraphFromRawGraphFile){
    			// build collapsed graph from existing raw graph
    			// useful to build a new collapsed graph with new method
    			logger.info("Reading raw graph from " + rawGraphFile.getAbsolutePath());
    			egr = new EntailmentGraphRaw(rawGraphFile);
    			logger.info("Building collapsed graph from an existing raw graph file");
    			egc = this.buildCollapsedGraphWithCategoryInfo(egr);
    			logger.info("Collapsed graph: number of nodes / edges " + egc.vertexSet().size() + egc.edgeSet().size());
	    		XMLFileWriter.write(egc.toXML(), collapsedGraphFile.getAbsolutePath());			
	    		logger.info("Wrote graph to : " + collapsedGraphFile.getAbsolutePath());
		} 
		else { 
			//create folder for graphs
			File graphFolder = new File(graphFolderName);
			if(graphFolder.exists()) FileUtils.deleteDirectory(graphFolder);
	      	if(!graphFolder.exists()) graphFolder.mkdirs();
	      	
	      	//build raw graph with tokens, dependencies and sentences from category texts (relevantTextProvided = false!)
	      	egr = new EntailmentGraphRaw(addLemmaLabel);
	      	singleTokenRawGraph = new EntailmentGraphRaw(addLemmaLabel);
	      	dependencyRawGraph = new EntailmentGraphRaw(addLemmaLabel);
	      	sentenceRawGraph = new EntailmentGraphRaw(addLemmaLabel);
	      	egr = buildRawGraph(graphDocs, setupToken, singleTokenRawGraph, 
	      			setupDependency, dependencyRawGraph, setupSentence, sentenceRawGraph, false, minTokenOccurrenceInCategories);
			XMLFileWriter.write(egr.toXML(), rawGraphFile.getAbsolutePath());			
			logger.info("Wrote graph to : " + rawGraphFile.getAbsolutePath());
			
	      	//build collapsed graph from category texts
			logger.info("Number of nodes in (category) raw graph: " + egr.vertexSet().size());
			logger.info("Number of edges in (category) raw graph: " + egr.edgeSet().size());
			egc = buildCollapsedGraphWithCategoryInfo(egr);
			logger.info("Number of nodes in (category) collapsed graph: " + egc.vertexSet().size());
			logger.info("Number of edges in (category) collapsed graph: " + egc.edgeSet().size());
			XMLFileWriter.write(egc.toXML(), collapsedGraphFile.getAbsolutePath());		
		}
		
       //Iterate through interactions, annotate each using existing graph and then add interaction to graph 
		for (Interaction doc : docs) {	
			logger.info("Processing document " + run + " out of " + docs.size());
			writer.println("Processing test interaction " + doc.getInteractionId() + " with category " + doc.getCategoryString());
//			if (run > 50) break; //TODO: Remove (debugging only)
			
			/** 
			 * STEP 2: ANNOTATE INTERACTION 
			 * **/
	    	//Index graph nodes and initialize search
			if (LuceneSearch) {
				nodeMatcherWithIndex = new NodeMatcherLuceneSimple(egc, "./src/test/resources/Lucene_index/incremental/", new GermanAnalyzer(Version.LUCENE_44));
				nodeMatcherWithIndex.indexGraphNodes();
				nodeMatcherWithIndex.initializeSearch();
			} else {
				if(entailedNodeScore > 0.0){
					nodeMatcher = new NodeMatcherLongestOnly(egc, bestNodeOnly, entailedNodeScore);
				} else {
//					nodeMatcher = new NodeMatcherLongestOnly(egc, bestNodeOnly);
					//TODO: integrate SEDANodeMatcher with ressources
					nodeMatcher = new SEDANodeMatcherLongestOnly(egc, bestNodeOnly, null, null, null, null, false, entailedNodeScore);
				}
			}
				
			//build token fragments for evaluation (relevantTextProvided = false!) **/
			Set<FragmentGraph> fgsAllForEval  = buildFragmentGraphs(doc, setupToken, setupDependency, setupSentence, false); 
			logger.info(fgsAllForEval.size() + " fragment graphs for evaluation");
			
			//TODO: see above (filtering with diff. frags?)
			/*
			writer.println(doc.getInteractionId() + " : ");
			writer.println(fgAll.size() + " fragment graphs ");
			*/
			
			//evaluate
			Set<NodeMatch> matches = getMatches(egc, fgsAllForEval,removeTokenMatches);
			
			writer.println(egc.vertexSet().size() + " nodes in graph");
			writer.println(matches.size() + " matches to evaluate: ");
			for (NodeMatch nm : matches) {
				writer.println("... " + nm.getMention());
				writer.println("... " + nm.getScores().size());
			}
						
			//add category annotation to CAS
			categoryAnnotator.addCategoryAnnotation(cas, matches);
			/*
			writer.println(cas.getDocumentText() + "");
			writer.println(CASUtils.getCategoryAnnotationsInCAS(cas).size() + " category annotations");
			 */
			
			Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
//			writer.println(decisions.size() + " decisions ");
			int counPositiveOld = countPositive;
			
			//compare annotation of interaction to manual annotation
			logger.info("Graph contains " + egc.vertexSet().size() + " nodes ");
			mostProbableCat = EvaluatorUtils.computeMostFrequentCategory(egc); //compute most frequent category in graph
			countPositive = EvaluatorUtils.compareDecisionsForInteraction(countPositive,
					doc, decisions, mostProbableCat, egc, matches, topN, 
					method, bestNodeOnly, documentFrequencyQuery, termFrequencyQuery, 
					lengthBoost);
		
			double accuracy = (double) countPositive / (double) run;	
			if(counPositiveOld == countPositive) {
				System.out.println("Run " + run + ":\t" + accuracy + "\t " + countPositive + "\t"+ false +"\t " +  doc.getInteractionId() + "\t " +  doc.getCategoryString());
				writerResult.println("Run " + run + ":\t" + accuracy + "\t " + countPositive + "\t"+ false +"\t " +  doc.getInteractionId() + "\t " +  doc.getCategoryString());
				writer.println("categorization of interaction " + doc.getInteractionId() + " was successful?: " + false);
			} else {
				System.out.println("Run " + run + ":\t" + accuracy + "\t " + countPositive + "\t"+ true +"\t " +  doc.getInteractionId() + "\t " +  doc.getCategoryString());
				writerResult.println("Run " + run + ":\t" + accuracy + "\t " + countPositive + "\t"+ true +"\t " +  doc.getInteractionId() + "\t " +  doc.getCategoryString());
				writer.println("categorization of interaction " + doc.getInteractionId() + " was successful?: " + true);
			}
				
			accuracyPerRun.add(accuracy);
			run++;
			//TODO: @KATHRIN, REMOVE?
			/*
			allFgs.addAll(fragmentGraphs);
			tokenOccurrences = computeTokenOccurrences(allFgs);
			writer.println("fragmentGraphs: " + fragmentGraphs.iterator().next().getCompleteStatement());
			*/

			/** STEP 3 EXTEND GRAPH BY FRAGMENTS OF THE INTERACTION**/
			//add the current email to the raw graph and build new collapsed graph
			//TODO: if readRawGraph
			collapsedGraphName = graphFolderName + "/" +  createCollapsedGraphFileName("omq_public_emails_" + run + "_collapsed_graph");
			rawGraphName = graphFolderName + "/" + createRawGraphFileName("omq_public_emails_" + run + "_raw_graph");
			collapsedGraphFile = new File(collapsedGraphName);
			rawGraphFile = new File(rawGraphName);
			if (readCollpasedGraphFromFile) { // read graph
    			logger.info("Reading collapsed graph from " + collapsedGraphFile.getAbsolutePath());
	    		egc = new EntailmentGraphCollapsed(collapsedGraphFile);
	    		logger.info("Collapsed graph: number of nodes / edges " + egc.vertexSet().size() + egc.edgeSet().size());
			}
			else if(buildCollapsedGraphFromRawGraphFile){
    			// build collapsed graph from existing raw graph
    			// useful to build a new collapsed graph with new method
    			logger.info("Reading raw graph from " + rawGraphFile.getAbsolutePath());
    			egr = new EntailmentGraphRaw(rawGraphFile);
    			logger.info("Building collapsed graph from an existing raw graph file");
    			egc = this.buildCollapsedGraphWithCategoryInfo(egr);
    			logger.info("Collapsed graph: number of nodes / edges " + egc.vertexSet().size() + egc.edgeSet().size());
	    		XMLFileWriter.write(egc.toXML(), collapsedGraphFile.getAbsolutePath());			
	    		logger.info("Wrote graph to : " + collapsedGraphFile.getAbsolutePath());
			} 
			else {
				graphDocs.clear(); 
				graphDocs.add(doc);
				egr = buildRawGraph(graphDocs, setupToken, singleTokenRawGraph, 
		      			setupDependency, dependencyRawGraph, setupSentence, sentenceRawGraph, relevantTextProvided, minTokenOccurrenceInCategories);
				
				rawGraphFile = new File(rawGraphName);
	//			rawGraphFile = new File(graphFolderName + "/" + createRawGraphFileName("omq_public_emails_" + run + "_raw_graph"));
				XMLFileWriter.write(egr.toXML(), rawGraphFile.getAbsolutePath());
				logger.info("Wrote graph to : " + rawGraphFile.getAbsolutePath());
				
				/*
				allFgs.addAll(fragmentGraphs);
				tokenOccurrences = computeTokenOccurrences(allFgs);
				writer.println("fragmentGraphs: " + fragmentGraphs.iterator().next().getCompleteStatement());
				*/
				//TODO: see above (filtering with diff. frags?)
				//logger.info("Number of fragment graphs: " + fragmentGraphs.size());
				
				//Build collapsed graph from extended raw graph
				egc = buildCollapsedGraphWithCategoryInfo(egr);	
				collapsedGraphFile = new File(collapsedGraphName);
	//	    	collapsedGraphFile = new File(graphFolderName + "/" +  createCollapsedGraphFileName("omq_public_emails_" + run + "_collapsed_graph"));
				XMLFileWriter.write(egc.toXML(), collapsedGraphFile.getAbsolutePath());			
				logger.info("Wrote graph to : " + collapsedGraphFile.getAbsolutePath());
			}
		}
		
		System.out.println("FINAL EGC: " + egc.vertexSet().size() + " / " +egc.edgeSet().size());
		System.out.println("FINAL EGR: " + egr.vertexSet().size() + " / " +egr.edgeSet().size());
		
		for (int i=1; i<accuracyPerRun.size(); i++) {
 			logger.info(accuracyPerRun.get(i));
		}

	}
	
	/**
	 * Runs three-fold cross-validation on the files found in the input directory. This directory must contain
	 * exactly three email files (named "omq_public_[123]_emails.xml") plus exactly one TH pair file for each of these email
	 * files (same file name but ending with "_th.xml"). 
	 * 
	 * For each fold, this method uses one of these email files for testing. 
	 * 
	 * If trainEDA is set to true, it uses one of the remaining interaction files for building the entailment graph
	 * and the TH pair file associated to the other one for training the EDA. 
	 * 
	 * If trainEDA is set to false, it uses both remaining email files for building the entailment graph. 
	 * 
	 * @param inputDataFoldername
	 * @param categoriesFilename 
	 * @param outputGraphFilename
	 * @throws Exception
	 */
	public void runEvaluationThreeFoldCross(String inputDataFoldername, String outputGraphFoldername, String categoriesFilename) throws Exception {
		Map<Integer, File> fileIndex = indexFilesinDirectory(inputDataFoldername);	    	

		String setupMetaData = createEvaluationDescription();
        System.out.println(setupMetaData);
        writer.println(setupMetaData);
    	writerResult.println(setupMetaData);
		
	    //check if there are enough files in the dir
	    double numberOfFolds = 3;
	    if (processTrainingData && fileIndex.size() < 6) { //TODO: elaborate this check (is the type of file correct: three interaction and three TH pair files)
    		logger.warn("Please specify a folder with three email and three T/H pair files (for EDA training + graph building + testing)!");
    		return;
	    } else {	     
	    	logger.info("Creating " + numberOfFolds + " folds.");
	    }
	    
	    //Reading categories
	  	List<Interaction> categoryDocs = new ArrayList<Interaction>();
	  	categoryDocs = CategoryReader.readCategoryXML(new File(categoriesFilename));
	   	List<Interaction> emailDocs = new ArrayList<Interaction>();
		List<Interaction> testDocs = new ArrayList<Interaction>();
		List<Interaction> graphDocs = new ArrayList<Interaction>();
		
		HashMap<Integer, Double> foldAccuracies = new HashMap<>();
	   	HashMap<Integer, Integer> foldCountPositive = new HashMap<>();

	   	for (int i=1; i<=numberOfFolds; i++) { //Create a fold for each of the three input files
//	    for (int i=3; i<=3; i++) { //Create one fold only
	        logger.info("Creating fold " + i);
			int j=i+1;
			if (j>3)j-=3; 
    		int k=j+1;
    		if (k>3)k-=3;
	    	emailDocs.clear();
	    	testDocs.clear();
	    	graphDocs.clear();
	    	
	    	//For each fold, read entailment graph EG or generate it from training set
	    	EntailmentGraphCollapsed egc = new EntailmentGraphCollapsed();
	    	EntailmentGraphRaw egr;
	    	File rawGraphFile = new File(outputGraphFoldername + "/" + createRawGraphFileName("omq_public_emails_" + i + "_raw_graph"));
	    	File collapsedGraphFile = new File(outputGraphFoldername + "/" + createCollapsedGraphFileName("omq_public_emails_" + i + "_collapsed_graph"));
	    	
    		String mostProbableCat;
    		if (readCollpasedGraphFromFile) { // read graph
    			logger.info("Reading collapsed graph from " + collapsedGraphFile.getAbsolutePath());
	    		egc = new EntailmentGraphCollapsed(collapsedGraphFile);
	    		logger.info("Collapsed graph: number of nodes / edges " + egc.vertexSet().size() + egc.edgeSet().size());
    		}
    		else if(buildCollapsedGraphFromRawGraphFile){
	    			// build collapsed graph from existing raw graph
	    			// useful to build a new collapsed graph with new method
	    			logger.info("Reading raw graph from " + rawGraphFile.getAbsolutePath());
	    			egr = new EntailmentGraphRaw(rawGraphFile);
	    			logger.info("Building collapsed graph from an existing raw graph file");
	    			egc = this.buildCollapsedGraphWithCategoryInfo(egr);
	    			logger.info("Collapsed graph: number of nodes / edges " + egc.vertexSet().size() + egc.edgeSet().size());
		    		XMLFileWriter.write(egc.toXML(), collapsedGraphFile.getAbsolutePath());			
		    		logger.info("Wrote graph to : " + collapsedGraphFile.getAbsolutePath());
    		} 
    		else { // build graph    	    		
				//read interactions for graph building
				String graphDocumentsFilename = inputDataFoldername + "omq_public_"+j+"_emails.xml";
				logger.info("Reading documents for graph building from " + graphDocumentsFilename);	    			
				emailDocs.addAll(InteractionReader.readInteractionXML(new File(graphDocumentsFilename)));
				logger.info("Graph set of fold "+i+" now contains " + emailDocs.size() + " documents");
				
				//add documents to graph creation set --> don't, dataset will be too large for graph building!
				if(trainEDAToken || trainEDASentence){
					String edaTrainingFilename = inputDataFoldername + "omq_public_"+k+"_th.xml";
					logger.info("Setting EDA training file " + edaTrainingFilename);	    
					if(trainEDAToken) {
						trainEDA(edaToken, configEDAToken, edaTrainingFilename, processTrainingData);
					}
					if(trainEDASentence) {
						trainEDA(edaSentence, configEDASentence, edaTrainingFilename, processTrainingData);
					}
				
				} else if (addSecondDataSetForGraphBuilding) {
					String secondGraphFilename = inputDataFoldername + "omq_public_"+k+"_emails.xml";
	    			logger.info("Reading second graph file " + secondGraphFilename);	    			
	    			emailDocs.addAll(InteractionReader.readInteractionXML(new File(secondGraphFilename)));
				}
				//graphDocs = graphDocs.subList(1, 2); //TODO: REMOVE for real test! @KATHRIN, REMOVE?
				logger.info("Graph set of fold "+i+" now contains " + emailDocs.size() + " documents");
				
				//emailDocs = reduceTrainingDataSize(emailDocs, 20); //reduce the number of emails on which the graph is built
				//logger.info("Reduced training set contains " +emailDocs.size()+ " documents.");
				
				//build raw graph from emails and categories
				egr = new EntailmentGraphRaw(addLemmaLabel);
				EntailmentGraphRaw singleTokenRawGraph = new EntailmentGraphRaw(addLemmaLabel);
				EntailmentGraphRaw dependencyRawGraph = new EntailmentGraphRaw(addLemmaLabel);
				EntailmentGraphRaw sentenceRawGraph = new EntailmentGraphRaw(addLemmaLabel);
				egr = buildRawGraph(emailDocs, setupToken, singleTokenRawGraph, setupDependency, dependencyRawGraph, 
						setupSentence, sentenceRawGraph, relevantTextProvided, minTokenOccurrenceInCategories);
				egr = buildRawGraph(categoryDocs, setupToken, singleTokenRawGraph, setupDependency, dependencyRawGraph, 
						setupSentence, sentenceRawGraph, false, minTokenOccurrence);
				System.out.println("Big Raw Graph has nodes: " + egr.vertexSet().size());
				System.out.println("Big Raw Graph has edges: " + egr.edgeSet().size());
				XMLFileWriter.write(egr.toXML(), rawGraphFile.getAbsolutePath());			
	    		logger.info("Wrote graph to : " + collapsedGraphFile.getAbsolutePath());
	    		
	    		//build collapsed graph
	    		egc = buildCollapsedGraphWithCategoryInfo(egr);
	    		System.out.println("Number of nodes in collapsed graph: number of nodes: " + egc.vertexSet().size());
	    		System.out.println("Number of nodes in collapsed graph: number of edges: " + egc.edgeSet().size());
	    		
	    		XMLFileWriter.write(egc.toXML(), collapsedGraphFile.getAbsolutePath());			
	    		logger.info("Wrote graph to : " + collapsedGraphFile.getAbsolutePath());
    		}
    		
    		mostProbableCat = EvaluatorUtils.computeMostFrequentCategory(egc);
			System.out.println("Most frequent category in graph: " + mostProbableCat);
    		
			//annotate interaction and evaluate
	    	if (!skipEval) {
	    		System.out.println("Evaluating fold: " + i);
	    		File testFile = new File(inputDataFoldername + "omq_public_"+i+"_emails.xml"); //TODO: replace?
				logger.info("Reading test file " + testFile.getName());	    			
				testDocs.addAll(InteractionReader.readInteractionXML(testFile));
				System.out.println("Test set of fold "+i+" now contains " + testDocs.size() + " documents");
		    	//For each email E in the test set, send it to nodematcher / category annotator and have it annotated
				int countPositive = 0;
				JCas casInteraction = CASUtils.createNewInputCas();
				for (Interaction interaction : testDocs) {
					logger.info("-----------------------------------------------------");
					logger.info("Processing test interaction " + interaction.getInteractionId() + " with category " + interaction.getCategoryString());
					writer.println("Processing test interaction " + interaction.getInteractionId() + " with category " + interaction.getCategoryString());
					
					interaction.fillInputCAS(casInteraction);	
					logger.info("category: " + CASUtils.getTLMetaData(casInteraction).getCategory());
					
					//build token fragments for evaluation
					Set<FragmentGraph> fragmentGraphs = buildFragmentGraphs(interaction, setupToken, setupDependency, setupSentence, false);
					logger.debug("Number of all fragment graphs for evaluation: " + fragmentGraphs.size());
					
					/* @KATHRIN, REMOVE?
					 * REMOVED THIS PART, AS TESTING IS DONE ON THE COMPLETE TEXT
					List<JCas> casesRelevantTexts = interaction.createAndFillInputCASes(false);
					logger.info("Number of cases: " + casesRelevantTexts.size());
					Set<FragmentGraph> fragmentGraphs = new HashSet<FragmentGraph>();
					for (int l=0; l<casesRelevantTexts.size(); l++) {
						JCas cas = casesRelevantTexts.get(l);
						logger.info("category: " + CASUtils.getTLMetaData(cas).getCategory());
						fragmentAnnotatorForNewInput.annotateFragments(cas);
						if (cas.getAnnotationIndex(DeterminedFragment.type).size() > 0) {
							modifierAnnotator.annotateModifiers(cas);
						}
						logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
						fragmentGraphs.addAll(fragmentGraphGenerator.generateFragmentGraphs(cas));
						//logger.info("Number of fragment graphs: " + fragmentGraphs.size());
					}*/
					
					//get matches
					Set<NodeMatch> matches = getMatches(egc, fragmentGraphs,removeTokenMatches);	
					logger.debug("Number of matches: " + matches.size());
					
					for (NodeMatch match : matches) {
						for (PerNodeScore score : match.getScores()) {
							logger.debug("match score for "+ score.getNode().getLabel() + ": " + score.getNode().getCategoryConfidences());
						}
					}
					
					//add category annotation to CAS
					categoryAnnotator.addCategoryAnnotation(casInteraction, matches);
					
					//@KATHRIN, REMOVE?
					//print CAS category
					//CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
					
			    	//Compare automatic to manual annotation
					Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(casInteraction);
										
					countPositive = EvaluatorUtils.compareDecisionsForInteraction(countPositive,
							interaction, decisions, mostProbableCat, egc, matches, topN, 
							method, bestNodeOnly, documentFrequencyQuery, termFrequencyQuery, 
							lengthBoost);			
				}
				
		    	logger.info("Count positive: " + countPositive);
		    	double countTotal = countTotalNumberOfCategories(testDocs);
                double accuracyInThisFold = ((double)countPositive / countTotal);
                foldAccuracies.put(i, accuracyInThisFold);
                foldCountPositive.put(i, countPositive);
                for (int fold : foldAccuracies.keySet()) {
                    countPositive = foldCountPositive.get(fold);
                 }
                printResult(topN, numberOfFolds, foldAccuracies, foldCountPositive);
                
	    	} // if skipEval	
	    }// for every fold
	}
	
	/**
	 * Compute and print final result
	 * 
	 * @param numberOfFolds
	 * @param foldAccuracy
	 */
	private void printResult(int topN, double numberOfFolds, 
			Map<Integer, Double> foldAccuracy, Map<Integer, Integer> foldCountPositive) {

            double sumAccuracies = 0;
            int sumCountPositive = 0;
            double accuracy = 0;
            int countPositive = 0;
                
            for (int fold : foldAccuracy.keySet()) {
                accuracy = foldAccuracy.get(fold);
                countPositive = foldCountPositive.get(fold);
                sumAccuracies += accuracy;
                sumCountPositive += countPositive;
            }
            
            if(foldAccuracy.size() == numberOfFolds){ //TODO: use it if all folds are evaluated to print only the end result
            	String setupMetaData = createEvaluationDescription();
	            System.out.println(setupMetaData);
	            writerFoEvalResult.println(setupMetaData);
	            for (int fold : foldAccuracy.keySet()) {
	            	System.out.println("Fold_" + fold + ": " + foldCountPositive.get(fold) + " / " + foldAccuracy.get(fold));
	            	writerFoEvalResult.println("Fold_" + fold + ": " + foldCountPositive.get(fold) + " / " + foldAccuracy.get(fold));
	            }
	            System.out.println("");
	
	            // just display the overall accuracy if all folds are there
	            if (foldAccuracy.keySet().size() >= numberOfFolds){
	            	System.out.println("ALL: " + sumCountPositive + " / " + (sumAccuracies / (double)numberOfFolds) +"\n");
	            	writerFoEvalResult.println("ALL: " + sumCountPositive + " / " + (sumAccuracies / (double)numberOfFolds) + "\n");
	            }
            }
	}
	
	private String createEvaluationDescription(){
		String setupMetaData = "\n" +"Setup for token= " + setupToken + " " + decompTypeToken +"\n"
				+ "Setup for dependency= " + setupDependency + " " + decompTypeDependency +"\n"
				+ "Setup for sentence= " + setupSentence + "\n"
				+ "topN= " + topN + "\n"
				+ "method= "+ method + " " 
				+ termFrequencyQuery + documentFrequencyQuery + normalizationQuery 
				+ "." + String.valueOf(methodDocument) + "\n" 
				+ "categoryBoost=" + categoryBoost  + "\n"
				+ "bestNodeOnly=" + bestNodeOnly  + "\n";
		
		if(!bestNodeOnly){
			if(entailedNodeScore > 0.0){
				setupMetaData += "entailedNodeScore: " + entailedNodeScore + "\n";
			}
			else {
				setupMetaData += "entailedNodeScore: " + "based on edgeConfidences" + "\n";
			}
		}
		
		setupMetaData += "lengthBoost=" + lengthBoost  + "\n"
				+ "removeTokensFromMatches=" + removeTokenMatches  + "\n"
				+ "applyTransitiveClosure=" + applyTransitiveClosure + "\n"
				+ "addLemmaLabel=" + addLemmaLabel + "\n"
				+ "dep-->token=" + addLemmaEdgesDependencyToToken + "\n"
				+ "addSecondData=" + addSecondDataSetForGraphBuilding + "\n";
		
		return setupMetaData;
	}
	
	/**
	 * 
	 * @param graph
	 * @param fragmentGraphs
	 * @param removeTokenMatches for fragments of length > 1: if a matching node for the fragment is found in the graph, remove matches for tokens contained in the fragment
	 * @return
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 * @throws NodeMatcherException
	 * @throws CategoryAnnotatorException
	 */
	private Set<NodeMatch> getMatches(EntailmentGraphCollapsed graph,
			Set<FragmentGraph> fragmentGraphs, boolean removeTokenMatches) throws LAPException,
			FragmentAnnotatorException, ModifierAnnotatorException,
			FragmentGraphGeneratorException, NodeMatcherException,
			CategoryAnnotatorException {		
		//call node matcher on each fragment graph
		Set<NodeMatch> matches = new HashSet<NodeMatch>();
		logger.info("fragmentsGraphs: " + fragmentGraphs);
		writer.println(fragmentGraphs.size() + "FGs");
		List<FragmentGraph> fgList = new ArrayList<FragmentGraph>();
		for (FragmentGraph fg : fragmentGraphs) {
			fgList.add(fg);
		}
//		Collections.sort(fgList); //@KATHRIN, REMOVE?
		for (FragmentGraph fragmentGraph: fgList) {
			logger.info("fragment graph: " + fragmentGraph.getCompleteStatement());
			writer.println("FG: " + fragmentGraph.getCompleteStatement());
			if (LuceneSearch) {
				matches.addAll(nodeMatcherWithIndex.findMatchingNodesInGraph(fragmentGraph));
			} else {
				if(entailedNodeScore > 0.0){
					nodeMatcher = new NodeMatcherLongestOnly(graph, bestNodeOnly, entailedNodeScore);
				} else {
//					nodeMatcher = new NodeMatcherLongestOnly(graph, bestNodeOnly);
					//TODO: integrate SEDANodeMatcher with ressources
					nodeMatcher = new SEDANodeMatcherLongestOnly(graph, bestNodeOnly, null, null, null, null, false, entailedNodeScore);
				}
				matches.addAll(nodeMatcher.findMatchingNodesInGraph(fragmentGraph));
			}
			logger.info("Number of matches: " + matches.size());
		}
		
		int removedMatchesCount = 0;
		if(removeTokenMatches){
			//find tokens which are already included in bigger matches
			//and then remove them
			Set<String> tokensToRemove = new HashSet<String>();
			for (NodeMatch nm : matches) {
				String[] tokens = nm.getMention().getTextWithoutDoubleSpaces().split("\\s+");
				if (tokens.length > 1) {
					tokensToRemove.addAll(Arrays.asList(tokens));
				}
			}
			Set<NodeMatch> tmpMatches = new HashSet<>(matches);
			for (NodeMatch nm : tmpMatches) {
				String mentionText = nm.getMention().getTextWithoutDoubleSpaces();
				if(tokensToRemove.contains(mentionText)){
					matches.remove(nm);
				}
			}
			removedMatchesCount  = tmpMatches.size() - matches.size();
		}
		
		writer.println("Number of removed single token matches: " + removedMatchesCount);
		for (NodeMatch match : matches) writer.println("nodematch: " + match);
		return matches;
	}
	
	/**
	 * 
	 * @param fgs
	 * @return
	 */
	private HashMap<String, Integer> computeTokenOccurrences(
			Set<FragmentGraph> fgs) {
		String text;
		HashMap<String, Integer> tokenOccurrences = new HashMap<String,Integer>();
		for (FragmentGraph fg : fgs) {  // compute the number of occurrences of each fragment text
			int countOccurrence = 0;
			text = fg.getBaseStatement().getTextWithoutDoubleSpaces();
			if (tokenOccurrences.containsKey(text.toLowerCase())) countOccurrence = tokenOccurrences.get(text.toLowerCase());
			countOccurrence++;
			tokenOccurrences.put(text.toLowerCase(), countOccurrence);
		}
		return tokenOccurrences;
	}
	
	/**
	 * 
	 * @param testDocs
	 * @return
	 */
	private double countTotalNumberOfCategories(List<Interaction> testDocs) {
		double count = 0;
		for (Interaction i : testDocs) {
			count += i.getCategories().length;
		}
		return count;
	}

	/**
	 * Read and index all files in the input folder 
	 * 
	 * @param inputDataFoldername
	 * @return
	 */
	private Map<Integer, File> indexFilesinDirectory(
			String inputDataFoldername) {
		File folder = new File(inputDataFoldername);
		Map<Integer,File> fileIndex = new HashMap<Integer, File>();
		int countFiles = 0;
		logger.info("Number of files: " + folder.listFiles().length);
	    for (File fileEntry : folder.listFiles()) {
	    	if (fileEntry.isFile()) {
	    		fileIndex.put(countFiles+1, fileEntry);
	    		countFiles++;
	    	}
	    }
		return fileIndex;
	}
	
	@SuppressWarnings("unused")
	private Set<Interaction> reduceTrainingDataSize(
			Set<Interaction> trainingDocs, int i) {
		Set<Interaction> interactions = new HashSet<Interaction>();
		Iterator<Interaction> interactionsIt = trainingDocs.iterator();
		int count = 0;
		while (interactionsIt.hasNext() && count < i) {
			count++;
			interactions.add(interactionsIt.next());
		}
		return interactions;
	}
	
}

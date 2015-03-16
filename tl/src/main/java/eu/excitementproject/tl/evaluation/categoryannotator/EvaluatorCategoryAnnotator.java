package eu.excitementproject.tl.evaluation.categoryannotator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.uima.jcas.JCas;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.LexUnit;
import eu.excitement.type.tl.CategoryAnnotation;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.alignmentedas.p1eda.P1EDATemplate;
import eu.excitementproject.eop.alignmentedas.p1eda.sandbox.FNR_DE;
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
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.composition.graphmerger.LegacyAutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLongestOnly;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLuceneSimple;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.DependencyAsFragmentAnnotatorForGerman;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.TokenAsFragmentAnnotatorForGerman;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.experiments.OMQ.SimpleEDA_DE;
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
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

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

 * @author Kathrin Eichler
 *
 */

public class EvaluatorCategoryAnnotator { 
	
	static Logger logger = Logger.getLogger(EvaluatorCategoryAnnotator.class); 
    static long startTime = System.currentTimeMillis();
    static long endTime = 0;
    
    static CachedLAPAccess lapForDecisions;
    static CachedLAPAccess lapForFragments;
	static CachedLAPAccess lapLemma;
	static CachedLAPAccess lapDependency;
    static CommonConfig config;
	static String configFilename; //config file for EDA
	static EDABasic<?> eda;
	static P1EDATemplate alignmenteda;
    static FragmentAnnotator fragmentAnnotatorForGraphBuilding;
    static FragmentAnnotator fragmentAnnotatorForNewInput;
    static ModifierAnnotator modifierAnnotator;
    static FragmentGraphGenerator fragmentGraphGenerator;
    static GraphMerger graphMerger;
    static GraphOptimizer graphOptimizer;
    static NodeMatcher nodeMatcher;
    static NodeMatcherWithIndex nodeMatcherWithIndex;
	static CategoryAnnotator categoryAnnotator;
	static ConfidenceCalculator confidenceCalculator;
    static File configFile;
    static DerivBaseResource dbr;
//  static String pathToGermaNet = "D:/DFKI/EXCITEMENT/Linguistic Analysis/germanet-7.0/germanet-7.0/GN_V70/GN_V70_XML";
    static String pathToGermaNet = "C:/germanet/germanet-7.0/germanet-7.0/GN_V70/GN_V70_XML";
    static GermaNetRelation [] relations = {GermaNetRelation.has_synonym, GermaNetRelation.has_hypernym, GermaNetRelation.causes, 
			GermaNetRelation.entails, GermaNetRelation.has_hyponym};
    static List<GermaNetRelation> germaNetRelations; //set autoamatically
    static GermaNetWrapper gnw; //set autoamtically
	static GermaNet germanet;
	static Set<String> GermaNetLexicon; 
	static GermanWordSplitter splitter;
	static String edaName; //configurated in setup()
    
	//CHOOSE CONFIGURATION:
	
	static double thresholdForOptimizing = 0.51; //minium EDA confidence for leaving an edge in the final graph
	static double thresholdForRawGraphBuilding = 0.51; // EDA confidence for leaving an edge in the raw graph
	static double thresholdForSEDA = 0.9; //minium EDA confidence for leaving an edge in the final graph
	static double thresholdForEDA = 0.9; // EDA confidence for leaving an edge in the raw graph
	static int minTokenOccurrence = 1; //minimum occurrence of an EMAIL token for the corresponding node to be merged into the graph
	static int minTokenOccurrenceInCategories = 1; //minimum occurrence of a CATEGORY token for the corresponding node to be merged into the graph
    
	/* SMART notation for tf-idf variants, as in Manning et al., chapter 6, p.128 */
	// Query (email) weighting: --> relevant when categorizing new emails
	static char termFrequencyQuery = 'n'; // n (natural), b (boolean: 1 if tf > 0), l (logarithm, sublinear tf scaling, as described by Manning et al. (2008), p. 126f.) 
	static char documentFrequencyQuery = 't'; // n (no), t (idf) + d (idf ohne log --> not part of SMART notation!)
	static char normalizationQuery = 'n'; // n (none), c (cosine)
	// Document (category) weighting: --> relevant when building the graph
	static char termFrequencyDocument = 'n'; // n (natural), l (logarithm)
	static char documentFrequencyDocument = 't'; // n (no), t (idf)
	static char normalizationDocument = 'n'; // n (none), c (cosine) //TODO: Implement? Don't think it's needed, as
	
	//INFO: Evaluating different TFIDF-configurations (with no EDA): bd[nc].n[nt]n
	//ndn.ntn --> acc. in fold 1: 0.56, 131+ (corresponds to the original "tfidf_sum" implementation!)
	//ldn.ntn --> acc. in fold 1: 0.57, 133+
	//bdn.ntn --> acc. in fold 1: 0.58, 135+
	//bnn.ntn --> acc. in fold 1: 0.53, 123+
	//btn.ntn --> acc. in fold 1: 0.53, 123+
	//bdc.ntn --> acc. in fold 1: 0.58, 135+ 
	//bdn.ltn --> acc. in fold 1: 0.54, 126+
	//bdn.nnn --> acc. in fold 1: 0.58, 135+
	//bdn.nnc --> acc. in fold 1: 0.49, 114+
	//bdn.nnn with vsm --> acc. in fold 1: 0.38, 89+ (TODO: find out why)

	static char[] methodDocument = new char[3]; 
	
//	static String method = "tfidf_vsm"; //Vector Space Model as described by Manning et al. (2008), p. 123f. 
	static String method = "tfidf"; //add up TFIDF scores, as in the "overlap score measure" described by Manning et al. (2008), p. 119 (TFIDF scores of terms occurring several times in the document are added up several times)	
	
	//static String method = "bayes"; //Naive Bayes 
	//static String method = "bayes_log"; //Naive Bayes with logarithm
	
	private boolean lengthBoost = false; //if set to true: boost fragments according to number of contained tokens
	private int categoryBoost = 0; //if categoryBoost > 0: if EC has fragments from a category text, then boost the weight of these categories in raw graph (add categoryBoost)
	
    static boolean LuceneSearch = false;
   
	static int derivSteps; //set in the setup(i)
	static boolean useSynonymRelation = true;
	static boolean useHypernymRelation = true;
	static boolean useEntailsRelation = true;
	static boolean useCausesRelation = true;
	static boolean onlyBidirectionalEdges;//set automatically
	static boolean mapNegation;//set automatically
    
	static List<POSTag_DE> tokenPosFilter = Arrays.asList(
	    		new POSTag_DE []{POSTag_DE.ADJA, POSTag_DE.ADJD, POSTag_DE.NE, POSTag_DE.NN, POSTag_DE.CARD, 
	    				POSTag_DE.VVFIN, POSTag_DE.VVINF, POSTag_DE.VVIZU, POSTag_DE.VVIMP, POSTag_DE.VVPP}); //"ADV" = adverb, "FM" = foreign language material
	static List<POSTag_DE> governorPosFilter = Arrays.asList(
    		new POSTag_DE []{POSTag_DE.ADJA, POSTag_DE.ADJD, POSTag_DE.NE, POSTag_DE.NN, POSTag_DE.CARD,
    						 POSTag_DE.VVFIN, POSTag_DE.VVINF, POSTag_DE.VVIZU, POSTag_DE.VVIMP, POSTag_DE.VVPP, POSTag_DE.PTKNEG, POSTag_DE.PTKVZ}); //"ADV" = adverb, "FM" = foreign language material
	static List<POSTag_DE> dependentPosFilter = governorPosFilter;
	private List<String> governorWordFilter = Arrays.asList(new String []{"ohne"});
	private List<String> dependentWordFilter = Arrays.asList(new String []{"keine", "keinerlei", "nicht", "nichts"});
//    static WordDecompositionType decompositionType = WordDecompositionType.ONLY_HYPHEN;
//    static WordDecompositionType decompositionType = WordDecompositionType.NO_RESTRICTION;
    static WordDecompositionType decompositionType = WordDecompositionType.NONE;
    
    
    private int setup; //use setupArray to set the parameter
    //use setup 5 and 25 only for sentences
    static int[] setupArray = {0}; //if setup 21 - 24 change in POM EOP DEPENDECIES  TO 1.1.3_ADAPTED
    private int setupSEDA = 101; //configure SEDA (for incremental evaluation)
    private int setupEDA = 1; //configure EDA (for incremental evaluation)
    static String fragmentTypeNameGraph; //use fragmentTypeNameGraphArray to set the parameter 
  	static String[] fragmentTypeNameGraphArray = {"TF"}; //for setup >= 110 this variable will be overwritten!
//    static String[] fragmentTypeNameGraphArray = {"TF", "DF", "SF"};
  	static String fragmentTypeNameEval; //set automatically!
  	
    /* begin OBS: The following parameters do NOT affect incremental evaluation: */
    static int topN; //use topNArray to set the parameter
    static int[] topNArray = {1}; //evaluate accuracy considerung the topN best categories returned by the system
    static boolean readGraphFromFile = false;
    static boolean readMergedGraphFromFile = false;
    static String inputMergedGraphFileName;
    static File inputMergedGraphFile;
    
    //Training parameters
    static boolean trainEDA = false;
    static boolean addDataSetForGraphBuilding = false;
    static boolean processTrainingData = false;
    static File xmiDir;
    static File modelBaseName;
    /* end OBS */
    
    static boolean relevantTextProvided = true;
    
    static boolean bestNodeOnly = true;
    //build graphs without graph merger class 
    static boolean useGraphMerger = true; //overwritten to false in setup 201 - 204
    static boolean addLemmaLabel = true; //overwritten to true in setup 201 - 204
    static boolean skipEval = false;
    
	static File temp; 
    static PrintWriter writer; 
    static PrintWriter writerResult; 
    	
	public static void main(String[] args) {
		
		String inputFoldername = "src/main/resources/exci/omq/emails/"; //dataset to be evaluated
		String outputGraphFoldername = "src/main/resources/exci/omq/graphs/"; //output directory (for generated entailment graph)
		String categoriesFilename = inputFoldername + "omq_public_categories.xml"; 

		try {
			lapLemma = new CachedLAPAccess(new LemmaLevelLapDE());
			lapDependency= new CachedLAPAccess(new DependencyLevelLapDE());
		} catch (LAPException e1) {
			e1.printStackTrace();
		}

		xmiDir = new File(inputFoldername+"xmis/");
		if (!xmiDir.exists()) xmiDir.mkdirs();
		modelBaseName = new File(inputFoldername+"model");
		
		/*
		try {
			GermaNetLexicon = getGermaNetLexicon();
			logger.info("GermaNet lexicon contains " + GermaNetLexicon.size() + " entries");
		} catch (LAPException | FragmentAnnotatorException | XMLStreamException | IOException e1) {
			e1.printStackTrace();
		}	
	*/	
		
		
        boolean tmpReadGraphFromFile = readGraphFromFile;
        boolean tmpReadMergedGraphFromFile = readMergedGraphFromFile;
        
        for (String tempFragmentTypeNameGraph : fragmentTypeNameGraphArray){
        	fragmentTypeNameGraph = tempFragmentTypeNameGraph;
            
        	for (int setupN : setupArray){
                EvaluatorCategoryAnnotator eca = new EvaluatorCategoryAnnotator(setupN);
                
                if(skipEval){
                	topNArray = new int [] {1};
                }
                
                for (int i = 0; i<topNArray.length;i++){
                    topN = topNArray[i]; 
                    
                    if (i > 0){
                        readGraphFromFile = true;
                        readMergedGraphFromFile = false;
                    }

                    try {
                            eca.runEvaluationThreeFoldCross(inputFoldername, outputGraphFoldername, categoriesFilename);
                          
//                            eca.runIncrementalEvaluation(inputFoldername, outputGraphFoldername, categoriesFilename);
                    } catch (Exception e) {
                            e.printStackTrace();
                    }
                }
                
                readGraphFromFile = tmpReadGraphFromFile;
                readMergedGraphFromFile = tmpReadMergedGraphFromFile;
                writer.close();
        		writerResult.close();
            }
        }
		
		logger.info("Finished evaluation");
	}

	EvaluatorCategoryAnnotator(int setup) {
                this.setup = setup;
		setup(setup);		
		try {
			 temp = File.createTempFile("debugging"+System.currentTimeMillis(), ".tmp");
			 logger.info("Created file at " + temp.getAbsolutePath());
			 writer = new PrintWriter(temp, "UTF-8");
			 writerResult = new PrintWriter(temp.getAbsolutePath().replace("debugging", "result"
					 ), "UTF-8");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	EvaluatorCategoryAnnotator() {
                this.setup = 1;
                fragmentTypeNameGraph = "TF";
		setup(1);		
		try {
			 temp = File.createTempFile("debugging"+System.currentTimeMillis(), ".tmp");
			 writer = new PrintWriter(temp, "UTF-8");
			 writerResult = new PrintWriter(temp.getAbsolutePath().replace("debugging", "result"), "UTF-8");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Pick a specific evaluation setup.
	 * 
	 * @param i
	 */
	private void setup(int i) {
		methodDocument[0] = termFrequencyDocument;
		methodDocument[1] = documentFrequencyDocument;
		methodDocument[2] = normalizationDocument;
		try { 
			switch(i){
	        	case 0: //no EDA use --> graph with non-connected nodes
	        		edaName = "NO EDA";
	        		trainEDA = false;
	        		processTrainingData = false;
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;    	
	        	case 1: //TIE with base configuration (inflection only)
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 11: //Alignment-based EDA 
	        		configFilename = "./src/test/resources/EOP_models/fnr_de.model";
	        		configFile = new File(configFilename);
	        		alignmenteda = new FNR_DE(); 
	        		edaName = "FNR_DE";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, alignmenteda);
		    		graphMerger.setEntailmentConfidenceThreshold(0.6);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 2: //TIE with base configuration + GermaNet 
	    			configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+GN_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer();// new GlobalGraphOptimizer(); --> don't use
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 3: //TIE with base configuration + DERIVBASE (POS restriction, derivSteps = 2)
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+DBPos2_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 4:  //TIE with base configuration + GermaNet (POS restriction) + DERIVBASE (POS restriction, derivSteps = 2)
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+GNPos+DBPos2_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 5:  //TIE with base configuration + GNPos+DS+DBPos+TP+TPPos+TS_DE.xml
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+GNPos+DS+DBPos+TP+TPPos+TS_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 21: //TIE ADAPTED BASE (no mapping @CARD@ --> @CARD@)
	        		//CHANGE IN POM EOP CORE VERSION TO 1.1.3_ADAPTED (only available for Kathrin, Aleksandra, Florian)
		    		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE_ADAPTED";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 22:  //TIE ADAPTED BASE (no mapping @CARD@ --> @CARD@) + GERMANET (NE --> NP) 
	        		//CHANGE IN POM EOP CORE VERSION TO 1.1.3_ADAPTED (only available for Kathrin, Aleksandra, Florian)
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+GN_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE_ADAPTED";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer();// new GlobalGraphOptimizer(); --> don't use
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 23:  //TIE ADAPTED BASE (no mapping @CARD@ --> @CARD@) + DERIVBASE (no POS restriction, derivSteps2)
	        		//CHANGE IN POM EOP CORE VERSION TO 1.1.3_ADAPTED (only available for Kathrin, Aleksandra, Florian)
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+DB2_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE_ADAPTED";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 24:  //TIE ADAPTED BASE (no mapping @CARD@ --> @CARD@) + DERIVBASE (no POS restriction, derivSteps2) + GERMANET (NE --> NP)
	        		//CHANGE IN POM EOP CORE VERSION TO 1.1.3_ADAPTED (only available for Kathrin, Aleksandra, Florian)
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+GN+DB2_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE_ADAPTED";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 25: //TIE with base configuration + GN+DS+DB+TP+TPPos+TS_DE.xml
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+GN+DS+DB+TP+TPPos+TS_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
	        		edaName = "TIE_ADAPTED";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
		    	
			    // SimpleEDA_DE SETUPS
	        	case 101: //SimpleEDA_DE, LEMMA + CONVERSION
	        		eda = new SimpleEDA_DE();
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;	
	        	case 102: //SimpleEDA_DE, LEMMA + CONVERSION + GERMANET
	        		eda = new SimpleEDA_DE(pathToGermaNet, useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;	
	        	case 103: //SimpleEDA_DE, LEMMA + CONVERSION + DERIVATION (2 derivation steps, no POS in query)
	        		derivSteps = 2;
	        		eda = new SimpleEDA_DE(derivSteps);
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 104: //SimpleEDA_DE, LEMMA + CONVERSION + DECOMPOSITION
	        		splitter = new GermanWordSplitter();
					eda = new SimpleEDA_DE(splitter);
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 105: //SimpleEDA_DE, LEMMA + CONVERSION + DERIVATION + GERMANET (2 derivation steps, no POS in query)
	        		derivSteps = 2;
	        		eda = new SimpleEDA_DE(derivSteps, pathToGermaNet, 
	        				useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 106: //SimpleEDA_DE, LEMMA + DECOMPOSITION + DERIVATION + GERMANET (2 derivation steps, no POS in query)
	        		splitter = new GermanWordSplitter();
	        		derivSteps = 2;
	        		eda = new SimpleEDA_DE(splitter, derivSteps, pathToGermaNet, 
	        				useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 110: //SimpleEDA_DE: Read in 101 graph built on token fragments; add 101 graph for dependency fragments
	        		readMergedGraphFromFile = true;
	        		inputMergedGraphFileName = "src/main/resources/exci/omq/graphs/omq_public_FOLD_merged_graph_101_1_TF_tfidf_SEDA.xml";
	        		eda = new SimpleEDA_DE();
	        		edaName = "SEDA";
	        		fragmentTypeNameGraph = "DF";
	        		fragmentTypeNameEval = "TDF";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph, fragmentTypeNameEval);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 111: //SimpleEDA_DE: ead in 101 graph built on tokens; add 106 graph for dependency-fragments 
	        		readMergedGraphFromFile = true;
	        		inputMergedGraphFileName = "src/main/resources/exci/omq/graphs/omq_public_FOLD_merged_graph_101_1_TF_tfidf_SEDA.xml";
	        		splitter = new GermanWordSplitter();
	        		derivSteps = 2;
	        		eda = new SimpleEDA_DE(splitter, derivSteps, pathToGermaNet, 
	        				useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);
	        		edaName = "SEDA";
	        		fragmentTypeNameGraph = "DF";
	        		fragmentTypeNameEval = "TDF";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph, fragmentTypeNameEval);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 112: //SimpleEDA_DE: Read in 101 graph built on tokens; add 104 graph for dependency-fragments 
	        		readMergedGraphFromFile = true;
	        		inputMergedGraphFileName = "src/main/resources/exci/omq/graphs/omq_public_FOLD_merged_graph_104_1_TF_tfidf_SEDA.xml";
	        		splitter = new GermanWordSplitter();
					eda = new SimpleEDA_DE(splitter);
	        		edaName = "SEDA";
	        		fragmentTypeNameGraph = "DF";
	        		fragmentTypeNameEval = "TDF";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph, fragmentTypeNameEval);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 113: //SimpleEDA_DE: Read in 106 graph built on DF; add 104 graph for TF
	        		readMergedGraphFromFile = true;
	        		inputMergedGraphFileName = "src/main/resources/exci/omq/graphs/omq_public_FOLD_merged_graph_106_1_DF_tfidf_SEDA.xml";
	        		splitter = new GermanWordSplitter();
					eda = new SimpleEDA_DE(splitter);
	        		edaName = "SEDA";
	        		fragmentTypeNameGraph = "TF";
	        		fragmentTypeNameEval = "TDF";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph, fragmentTypeNameEval);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new LegacyAutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphMerger.setEntailmentConfidenceThreshold(thresholdForRawGraphBuilding);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
		    	//no stamdard graph merger uses
	        	case 207: //Lemma+Conversion, Derivation, GermaNet, mapping negation
					dbr = new DerivBaseResource(true, 2);
					gnw = new GermaNetWrapper(pathToGermaNet);
					germaNetRelations =  Arrays.asList(relations);
					splitter = null;
					onlyBidirectionalEdges = false;
					mapNegation = true;
					//lap, fragment annotator, eda name and others are set at the end of the methodss
					break;
	        	case 208://201 without mapNegation
					dbr = new DerivBaseResource(true, 2);
					gnw = new GermaNetWrapper(pathToGermaNet);
					germaNetRelations =  Arrays.asList(relations);
					splitter = null;
					onlyBidirectionalEdges = false;
					mapNegation = false;
					//lap, fragment annotator, eda name and others are set at the end of the methods
					break;
	        	case 209: //201 without GermaNet
					dbr = new DerivBaseResource(true, 2);
					gnw = null;
					germaNetRelations =  null;
					splitter = null;
					onlyBidirectionalEdges = false;
					mapNegation = true;
					//lap, fragment annotator, eda name and others are set at the end of the methods
					break;
	        	case 210: //201 without DerivBase
	        		dbr = null;
					gnw = new GermaNetWrapper(pathToGermaNet);
					germaNetRelations =  Arrays.asList(relations);
					splitter = null;
					onlyBidirectionalEdges = false;
					mapNegation = true;
					//lap, fragment annotator, eda name and others are set at the end of the methods
					break;
					
				/** setups for joined graphs **/
		    	//only evaluating of existing graphs TF + DF for now
		    	//TODO: creating and evaluating of joined graphs with all possible fragment combinations
	        	case 101101: //SimpleEDA_DE: Read joined graphs 101_TF + 101_DF
	        		readMergedGraphFromFile = false;
	        		readGraphFromFile = true;
	        		fragmentTypeNameGraph = "TDF";
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 101102: //SimpleEDA_DE: Read joined graphs 101_TF + 102_DF
	        		readMergedGraphFromFile = false;
	        		readGraphFromFile = true;
	        		fragmentTypeNameGraph = "TDF";
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 101103: //SimpleEDA_DE: Read joined graphs 101_TF + 103_DF
	        		readMergedGraphFromFile = false;
	        		readGraphFromFile = true;
	        		fragmentTypeNameGraph = "TDF";
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 101104: //SimpleEDA_DE: Read joined graphs 101_TF + 104_DF
	        		readMergedGraphFromFile = false;
	        		readGraphFromFile = true;
	        		fragmentTypeNameGraph = "TDF";
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 101105: //SimpleEDA_DE: Read joined graphs 101_TF + 105_DF
	        		readMergedGraphFromFile = false;
	        		readGraphFromFile = true;
	        		fragmentTypeNameGraph = "TDF";
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 101106: //SimpleEDA_DE: Read joined graphs 101_TF + 106_DF
	        		readMergedGraphFromFile = false;
	        		readGraphFromFile = true;
	        		fragmentTypeNameGraph = "TDF";
	        		edaName = "SEDA";
	        		setLapAndFragmentAnnotator(fragmentTypeNameGraph);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
			}
			if(i >= 207 && i <= 210) {
	    		addLemmaLabel = true;
	    		useGraphMerger = false;
				edaName = "NGM"; //methods of EvaluatorUtils used for merging instead of standard graph merger used
				setLapAndFragmentAnnotator(fragmentTypeNameGraph);
	    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
	    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
	    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
	    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(methodDocument, categoryBoost);
	    		categoryAnnotator = new CategoryAnnotatorAllCats();
	    		//resources are 
	    	}
		} catch (ModifierAnnotatorException | ConfigurationException e) {
			e.printStackTrace();
		} catch (GraphMergerException e) {
			e.printStackTrace();
		} catch (EDAException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 			
	}

	/**
	 * 
	 * @param inputFilename
	 * @param outputDirname
	 * @param configFilename
	 * @param setup
	 * @param topN
	 * @param method
	 * @param bestNodeOnly
	 * @param documentFrequencyQuery
	 * @param termFrequencyQuery
	 * @return
	 * @throws IOException
	 */
	public double runEvaluationOnTrainTestDataset(String inputFilename, String outputDirname, 
			String configFilename, int setup, int topN, String method, boolean bestNodeOnly, 
			char documentFrequencyQuery, char termFrequencyQuery) throws IOException {
		
		setup(setup);		
		UseCaseOneRunnerPrototype use1;
		
		// Read in all emails with their associated categories and split into train/test set
		logger.info("Reading input " + inputFilename);
		String[] files =  {inputFilename};
		File f;
		Set<Interaction> docs = new HashSet<Interaction>();

		try {
			for (String name: files) {
				logger.info("Reading file " + name);
				f = new File(name);
				docs.addAll(InteractionReader.readInteractionXML(f));
			}
			
			logger.info("Added " + docs.size() + " documents");
			
			// Split emails into train and test set
			Set<Interaction> docsTrain = new HashSet<Interaction>();
			Set<Interaction> docsTest = new HashSet<Interaction>();
			for (Interaction doc : docs) {
				if (Integer.parseInt(doc.getInteractionId()) % 3 == 0) docsTest.add(doc);
				else docsTrain.add(doc);
			}
			logger.info("Training set contains " + docsTrain.size() + " documents.");
			logger.info("Test set contains " + docsTest.size() + " documents.");
			
			File theDir = new File(outputDirname);
			
			// if the directory does not exist, create it
			if (!theDir.exists()) {
				logger.debug("Creating directory: " + outputDirname);
				boolean result = theDir.mkdir();
				if(result){
					logger.debug("DIR created");
				} else {
					logger.debug("Could not create the output directory. No output files will be created.");
					outputDirname=null;
				}
			}
			
			if (setup == 11) {
				logger.error("Method not implemented for setup 11!");
				System.exit(1);
			}
			else eda.initialize(config);
			logger.info("Initialized config.");
			logger.info("LAP: " + lapForFragments.getComponentName());

			double threshold = 0.99;
			
			if (setup == 11) {
				use1 = new UseCaseOneRunnerPrototype(lapForFragments, alignmenteda, 
		    		fragmentAnnotatorForGraphBuilding, modifierAnnotator, 
		    		fragmentGraphGenerator, graphMerger, graphOptimizer);
			} else {
				use1 = new UseCaseOneRunnerPrototype(lapForFragments, eda, 
			    		fragmentAnnotatorForGraphBuilding, modifierAnnotator, 
			    		fragmentGraphGenerator, graphMerger, graphOptimizer);				
			}
			EntailmentGraphCollapsed graph = use1.buildCollapsedGraph(docsTrain, threshold);
			logger.info("Built collapsed graph.");
			
			confidenceCalculator.computeCategoryConfidences(graph);
			String outputFile = outputDirname + "german_dummy_data_for_evaluator_test_graph.xml";
			XMLFileWriter.write(graph.toXML(), outputFile);		
			logger.info("Wrote collapsed graph to " + outputFile);
			graph = new EntailmentGraphCollapsed(new File(outputFile));

			/**
			//building fragment graphs
			JCas cas = CASUtils.createNewInputCas();
			List<Interaction> graphDocs = new ArrayList<Interaction>();
			for (Interaction i : docsTrain) graphDocs.add(i);
			Set<FragmentGraph> fgs = buildFragmentGraphs(graphDocs, cas);
			for (FragmentGraph fg : fgs) {
				logger.info(fg.toString());
			}			
			logger.info("Built fragment graphs.");
			
			//merging fragment graphs
			EntailmentGraphRaw egr = graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw(addLemmaLabel));
			String outputFile = outputDirname + "test.rawgraph.xml";
			XMLFileWriter.write(egr.toXML(), outputFile);			
			logger.info("Wrote raw graph to " + outputFile);
			logger.info("Number of nodes in raw graph: " + egr.vertexSet().size());
			logger.info("Number of edges in raw graph: " + egr.edgeSet().size());
			
			//collapsing graph
			EntailmentGraphCollapsed egc = graphOptimizer.optimizeGraph(egr, thresholdForOptimizing);
			logger.info("Number of nodes in collapsed graph: " + egc.vertexSet().size());
			logger.info("Number of edges in collapsed graph: " + egc.edgeSet().size());
			logger.info("Wrote collapsed graph to " + outputFile);
			outputFile = outputDirname + "test.collapsedgraph.xml";
			XMLFileWriter.write(egc.toXML(), outputFile);					

			//adding combined category confidences
			confidenceCalculator.computeCategoryConfidences(graph);
			logger.info("Computed and added category confidences.");
			outputFile = outputDirname + "test.collapsedgraph_confidences.xml";
			//XMLFileWriter.write(egc.toXML(), outputFile);			
			//logger.info("Wrote collapsed graph with confidences to " + outputFile);
			*/

			/**
			//reading previously built graph from file
			egc = new EntailmentGraphCollapsed(new File(outputFile));
			logger.info("Read collapsed graph with confidences from " + outputFile);
			*/
			/**
			confidenceCalculator.computeCategoryConfidences(egc);
			logger.info("Computed and added category confidences.");
			outputFile = outputDirname + "test.collapsedgraph_confidences.xml";
			XMLFileWriter.write(egc.toXML(), outputFile);			
			logger.info("Wrote collapsed graph with confidences to " + outputFile);

			//reading previously built graph from file
			egc = new EntailmentGraphCollapsed(new File(outputFile));
			logger.info("Read collapsed graph with confidences from " + outputFile);
			*/
		
			// Send each email in test data + graph to node use case 2 and have it annotated
			int countPositive = 0;
			for (Interaction doc : docsTest) {
				/*
				JCas cas;
				cas = doc.createAndFillInputCAS();
				use2 = new UseCaseTwoRunnerPrototype(lapForFragments, eda);
				use2.annotateCategories(cas, graph);
				*/
				JCas cas = doc.createAndFillInputCAS();
				fragmentAnnotatorForNewInput.annotateFragments(cas);
				if (cas.getAnnotationIndex(DeterminedFragment.type).size() > 0) {
					modifierAnnotator.annotateModifiers(cas);
				}
				//logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
				Set<FragmentGraph> fragmentGraphs = fragmentGraphGenerator.generateFragmentGraphs(cas);
				if (null != fragmentGraphs) {
					logger.debug("Number of fragment graphs: " + fragmentGraphs.size());
					Set<NodeMatch> matches = getMatches(graph, fragmentGraphs);	
					//add category annotation to CAS
					categoryAnnotator.addCategoryAnnotation(cas, matches);
					logger.debug("_________________________________________________________");
					Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
					logger.debug("Found " + decisions.size() + " decisions in CAS for interaction " + doc.getInteractionId());
					CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
					
					countPositive = compareDecisionsForInteraction(countPositive,
							doc, decisions, graph, matches, topN, method, bestNodeOnly, 
							documentFrequencyQuery, termFrequencyQuery);

				}
				/*
//				JCas cas;
				cas = doc.createAndFillInputCAS();
				use2 = new UseCaseTwoRunnerPrototype(lapForFragments, eda);
				use2.annotateCategories(cas, egc);
				logger.info("_________________________________________________________");
				Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
				logger.info("Found " + decisions.size() + " decisions in CAS for interaction " + doc.getInteractionId());
				CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
				
				countPositive = compareDecisionsForInteraction(countPositive,
						doc, decisions);				
				*/
			}
			
			// Compute and print result	
			double result = (double) countPositive / (double) docsTest.size();
			logger.info("Final result: " + result);
			return result;
			
		} catch (ConfigurationException | EDAException | ComponentException 
			| FragmentAnnotatorException | ModifierAnnotatorException 
			| GraphMergerException
			| GraphOptimizerException 
			| FragmentGraphGeneratorException 
			| ConfidenceCalculatorException 
			| NodeMatcherException 
			| CategoryAnnotatorException | DataReaderException | EntailmentGraphCollapsedException | TransformerException e) {
			e.printStackTrace();
			return -1;
		}
	}
	

	/**
	 * Compare automatic to manual annotation on interaction level (with no "most probable" category)
	 * 
	 * @param countPositive
	 * @param doc
	 * @param decisions
	 * @return
	 */
	private int compareDecisionsForInteraction(int countPositive,
			Interaction doc, Set<CategoryDecision> decisions, EntailmentGraphCollapsed graph, 
			Set<NodeMatch> matches, int topN, String method, boolean bestNodeOnly, 
			char documentFrequencyQuery, char termFrequencyQuery) {
		return EvaluatorUtils.compareDecisionsForInteraction(countPositive, doc, decisions, "N/A", 
				graph, matches, topN, method, bestNodeOnly, documentFrequencyQuery, termFrequencyQuery, 
				lengthBoost);
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
	    	
	    //check if there are enough files in the dir
	    double numberOfFolds = 3;
	    if (processTrainingData && fileIndex.size() < 6) { //TODO: elaborate this check (is the type of file correct: three interaction and three TH pair files)
    		logger.warn("Please specify a folder with three email and three T/H pair files (for EDA training + graph building + testing)!");
    		return;
	    } else {	     
	    	logger.info("Creating " + numberOfFolds + " folds.");
	    }
	    
	   	HashMap<Integer, Double> foldAccuracies = new HashMap<>();
	   	HashMap<Integer, Integer> foldCountPositive = new HashMap<>();

	   	//Reading categories
	   	List<Interaction> categoryDocs = new ArrayList<Interaction>();
	   	categoryDocs = CategoryReader.readCategoryXML(new File(categoriesFilename));

	   	List<Interaction> emailDocs = new ArrayList<Interaction>();
		List<Interaction> testDocs = new ArrayList<Interaction>();
		List<Interaction> graphDocs = new ArrayList<Interaction>();
		String edaTrainingFilename;
		
        //double sumAccuracies;
        //int sumCountPositive;
	    for (int i=1; i<=numberOfFolds; i++) { //Create a fold for each of the three input files
//	    for (int i=2; i<=2; i++) { //Create one fold only
	        logger.info("Creating fold " + i);
			int j=i+1;
			if (j>3)j-=3; 
    		int k=j+1;
    		if (k>3)k-=3;
	    	edaTrainingFilename = "";
	    	emailDocs.clear();
	    	testDocs.clear();
	    	graphDocs.clear();
	    	
	    	//Add test documents
			File testFile = new File(inputDataFoldername + "omq_public_"+i+"_emails.xml"); //TODO: replace?
			logger.info("Reading test file " + testFile.getName());	    			
			testDocs.addAll(InteractionReader.readInteractionXML(testFile));
			logger.info("Test set of fold "+i+" now contains " + testDocs.size() + " documents");
			
			//For each fold, read entailment graph EG or generate it from training set
	    	EntailmentGraphCollapsed graph = new EntailmentGraphCollapsed();
    		File graphFile = new File(outputGraphFoldername + "omq_public_"+i+"_collapsed_graph_"+setup + "_" + minTokenOccurrence + "_"
    				+ fragmentTypeNameEval + "_" + decompositionType + "_" + method + "_" + termFrequencyDocument + documentFrequencyDocument + normalizationDocument 
    				+ "_cb" + categoryBoost + "_" + thresholdForOptimizing + "_" + edaName + ".xml");
    		File mergedGraphFile = new File(outputGraphFoldername + "omq_public_"+i+"_merged_graph_" + setup + "_" + minTokenOccurrence 
    				+ "_" + fragmentTypeNameEval + "_" + decompositionType + "_" + thresholdForRawGraphBuilding + "_" + edaName +".xml");
    		
    		//DEBUGGING
    		//graphFile = new File(outputGraphFoldername + "omq_public_1_collapsed_graph_112_1_TDF_tfidf_nnn_SEDA_BACKUP.xml");
    		EntailmentGraphRaw egr;
    		String mostProbableCat;
	    	if (readGraphFromFile) { // read graph
	    		if(readMergedGraphFromFile){
	    			// build collapsed graph from an existing raw graph
	    			// useful to build a new entailment graph with new method
	    			logger.info("Building collapsed graph from raw graph");
	    			logger.info("Reading raw graph from " + mergedGraphFile.getAbsolutePath());
	    			egr = new EntailmentGraphRaw(mergedGraphFile);
	    			graph = this.buildCollapsedGraphWithCategoryInfo(egr);
	    			logger.info("Number of nodes in collapsed graph: " + graph.vertexSet().size());
	    			logger.info("Number of edges in collapsed graph: " + graph.edgeSet().size());
		    		XMLFileWriter.write(graph.toXML(), graphFile.getAbsolutePath());			
		    		logger.info("Wrote graph to : " + graphFile.getAbsolutePath());
	    		} else {
	    			logger.info("Reading graph from " + graphFile.getAbsolutePath());
		    		graph = new EntailmentGraphCollapsed(graphFile);
		    		//graph = new EntailmentGraphCollapsed(new File("src/main/resources/exci/omq/graphs/omq_public_1_collapse_graph_6_1_tfidf_nnn_TDF_SEDA_LEMMA+DB2+GERMANET+SDEKOMPO+NEGATION+PARTIKEL.xml"));
		    		//TODO: REMOVE; DEBUGGING ONLY
	    		}
	    	} else { // build graph
	    		String graphDocumentsFilename = inputDataFoldername + "omq_public_"+j+"_emails.xml";
				logger.info("Reading documents for graph building from " + graphDocumentsFilename);	    			
				emailDocs.addAll(InteractionReader.readInteractionXML(new File(graphDocumentsFilename)));
				logger.info("Graph set of fold "+i+" now contains " + emailDocs.size() + " documents");
				if (trainEDA) { // train EDA
					if (processTrainingData) { //process training data
						edaTrainingFilename = inputDataFoldername + "omq_public_"+k+"_th.xml";
						logger.info("Setting EDA training file " + edaTrainingFilename);	    
						File trainingFile = new File(edaTrainingFilename); //training input file
						File outputDir;
						if (setup == 11) outputDir = xmiDir;
						else outputDir = new File("./target/DE/dev/"); // output dir as written in configuration!
						if (!outputDir.exists()) outputDir.mkdirs();
						logger.info("Reading " + trainingFile.getCanonicalPath());
						lapForDecisions.processRawInputFormat(trainingFile, outputDir); //process training data and store output
						logger.info("Processing training data."); 
					} // training data already exists
					if (setup == 11) {
						alignmenteda.startTraining(xmiDir, modelBaseName); 
					}
					else eda.startTraining(config); //train EDA (may take a some time)
					logger.info("Training completed."); 
				} else { //add documents to graph creation set --> don't, dataset will be too large for graph building!
					if(addDataSetForGraphBuilding) {
						String secondGraphFilename = inputDataFoldername + "omq_public_"+k+"_emails.xml";
		    			logger.info("Reading second graph file " + secondGraphFilename);	    			
		    			emailDocs.addAll(InteractionReader.readInteractionXML(new File(secondGraphFilename)));
					}
				}
				//graphDocs = graphDocs.subList(1, 2); //TODO: REMOVE for real test!
				logger.info("Graph set of fold "+i+" now contains " + emailDocs.size() + " documents");
				
				if (readMergedGraphFromFile) { //Read in a previously built merged graph
					inputMergedGraphFile = new File(inputMergedGraphFileName.replaceAll("FOLD", ""+i));
					logger.info("Reading merged graph from inputMergedGraphFile: "+ inputMergedGraphFile.getAbsolutePath());
					egr = new EntailmentGraphRaw(inputMergedGraphFile);
				}
				else egr = new EntailmentGraphRaw(addLemmaLabel);

				//emailDocs = reduceTrainingDataSize(emailDocs, 20); //reduce the number of emails on which the graph is built
				//logger.info("Reduced training set contains " +emailDocs.size()+ " documents.");
			
				//Add email texts
				logger.info("Nummber of nodes: " + egr.vertexSet().size());
				egr = buildRawGraph(emailDocs, relevantTextProvided, mergedGraphFile, egr, minTokenOccurrence);
				logger.info("Nummber of nodes: " + egr.vertexSet().size());

	    		//Add category texts
	    		logger.info("Adding " + categoryDocs.size() + " categories");
    			egr = buildRawGraph(categoryDocs, false, mergedGraphFile, egr, minTokenOccurrenceInCategories);//TODO: set category text als relevantText in XML
	    		logger.info("Number of nodes in raw graph: " + egr.vertexSet().size());
	    		logger.info("Number of edges in raw graph: " + egr.edgeSet().size());
	    		graph = buildCollapsedGraphWithCategoryInfo(egr);
	    		logger.info("Number of nodes in collapsed graph: " + graph.vertexSet().size());
	    		logger.info("Number of edges in collapsed graph: " + graph.edgeSet().size());
	    		XMLFileWriter.write(graph.toXML(), graphFile.getAbsolutePath());			
	    		logger.info("Wrote graph to : " + graphFile.getAbsolutePath());
	    	}

	    	mostProbableCat = EvaluatorUtils.computeMostFrequentCategory(graph);
			logger.info("Most frequent category in graph: " + mostProbableCat);
			
			logger.info("Collapsed graph " + graphFile.getAbsolutePath() + " contains " + graph.vertexSet().size() + " nodes and " + graph.edgeSet().size() + " edges");
			
	    	//indexing graph nodes and initializing search
			if (LuceneSearch) {
				nodeMatcherWithIndex = new NodeMatcherLuceneSimple(graph, "./src/test/resources/Lucene_index/", new GermanAnalyzer(Version.LUCENE_44));
				nodeMatcherWithIndex.indexGraphNodes();
				nodeMatcherWithIndex.initializeSearch();
			} else {
				nodeMatcher = new NodeMatcherLongestOnly(graph, bestNodeOnly);
			}
			
			if (graph.getNumberOfEquivalenceClasses() < 1) {
				logger.error("Empty graph!");
				System.exit(1);
			}
			
	    	if (!skipEval) {
		    	//For each email E in the test set, send it to nodematcher / category annotator and have it annotated
				int countPositive = 0;
				JCas casInteraction = CASUtils.createNewInputCas();
				for (Interaction interaction : testDocs) {
					logger.info("-----------------------------------------------------");
					logger.info("Processing test interaction " + interaction.getInteractionId() + " with category " + interaction.getCategoryString());
					writer.println("Processing test interaction " + interaction.getInteractionId() + " with category " + interaction.getCategoryString());
					interaction.fillInputCAS(casInteraction);	
					logger.info("category: " + CASUtils.getTLMetaData(casInteraction).getCategory());
					fragmentAnnotatorForNewInput.annotateFragments(casInteraction);
					Set<FragmentGraph> fragmentGraphs = new HashSet<FragmentGraph>();
					if (casInteraction.getAnnotationIndex(DeterminedFragment.type).size() > 0) {
						modifierAnnotator.annotateModifiers(casInteraction);
					}
					logger.debug("Adding fragment graphs for text: " + casInteraction.getDocumentText());
					fragmentGraphs.addAll(fragmentGraphGenerator.generateFragmentGraphs(casInteraction));
					//logger.info("Number of fragment graphs: " + fragmentGraphs.size());
				
					
					/* REMOVED THIS PART, AS TESTING IS DONE ON THE COMPLETE TEXT
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
						
					Set<NodeMatch> matches = getMatches(graph, fragmentGraphs);	
					
					logger.debug("Number of matches: " + matches.size());
					
					for (NodeMatch match : matches) {
						for (PerNodeScore score : match.getScores()) {
							logger.debug("match score for "+score.getNode().getLabel()+": " + score.getNode().getCategoryConfidences());
						}
					}

					//add category annotation to CAS
					categoryAnnotator.addCategoryAnnotation(casInteraction, matches);
					
					

					//print CAS category
					//CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
					
			    	//Compare automatic to manual annotation
					logger.info("annotating interaction " + interaction.getInteractionId());
				//		logger.info("interaction text: " + interaction.getInteractionString());
					Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(casInteraction);

				//	for (CategoryDecision catDec : decisions) logger.debug("decision" + catDec.getCategoryId() + " : " + catDec.getConfidence());
										
					countPositive = EvaluatorUtils.compareDecisionsForInteraction(countPositive,
							interaction, decisions, mostProbableCat, graph, matches, topN, 
							method, bestNodeOnly, documentFrequencyQuery, termFrequencyQuery, 
							lengthBoost);				
				}
		    	logger.info("Count positive: " + countPositive);
		    	double countTotal = countTotalNumberOfCategories(testDocs);
                double accuracyInThisFold = ((double)countPositive / countTotal);
                foldAccuracies.put(i, accuracyInThisFold);
                foldCountPositive.put(i, countPositive);
                //sumAccuracies = 0;
                //sumCountPositive = 0;
                //double accuracy;
                for (int fold : foldAccuracies.keySet()) {
                    //accuracy = foldAccuracies.get(fold);
                    countPositive = foldCountPositive.get(fold);
                    //sumAccuracies += accuracy;
                    //sumCountPositive += countPositive;
                 }
                printResult(topN, numberOfFolds, foldAccuracies, foldCountPositive);
	    	} // if skipEval	
	    } // for folds
	}

	private double countTotalNumberOfCategories(List<Interaction> testDocs) {
		double count = 0;
		for (Interaction i : testDocs) {
			count += i.getCategories().length;
		}
		return count;
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

	/**
	 * Compute and print final result
	 * 
	 * @param numberOfFolds
	 * @param foldAccuracy
	 */
	private void printResult(int topN,
                                double numberOfFolds,
                                Map<Integer, Double> foldAccuracy, 
                                Map<Integer, Integer> foldCountPositive) {

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
	            System.out.println("");
	            System.out.println("Setup: " + setup + "/ topN: " + topN);
	            System.out.println("method: "+ method + " " + termFrequencyQuery+documentFrequencyQuery+normalizationQuery + "." + String.valueOf(methodDocument));
	            System.out.println("categoryBoost: " + categoryBoost);
	            System.out.println("bestNodeOnly: " + bestNodeOnly);
	            System.out.println("lengthBoost: " + lengthBoost);
	            for (int fold : foldAccuracy.keySet()) {
	            	System.out.println("Fold_" + fold + ": " + foldCountPositive.get(fold) + " / " + foldAccuracy.get(fold));
	            }
	            System.out.println("");
	
	            // just display the overall accuracy if all folds are there
	            if (foldAccuracy.keySet().size() >= numberOfFolds){
	            	System.out.println("ALL: " + sumCountPositive + " / " + (sumAccuracies / (double)numberOfFolds));
	            	System.out.println("");
	            }
            }
	}
	

	/**
	 * Annotate interaction using entailment graph
	 * 
	 * @param graph
	 * @param interaction
	 * @return
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 * @throws NodeMatcherException
	 * @throws CategoryAnnotatorException
	 */
	private Set<NodeMatch> getMatches(EntailmentGraphCollapsed graph,
			Set<FragmentGraph> fragmentGraphs) throws LAPException,
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
//		Collections.sort(fgList);
		for (FragmentGraph fragmentGraph: fgList) {
			logger.info("fragment graph: " + fragmentGraph.getCompleteStatement());
			writer.println("FG: " + fragmentGraph.getCompleteStatement());
			if (LuceneSearch) {
				matches.addAll(nodeMatcherWithIndex.findMatchingNodesInGraph(fragmentGraph));
			} else {
				nodeMatcher = new NodeMatcherLongestOnly(graph, bestNodeOnly);
				matches.addAll(nodeMatcher.findMatchingNodesInGraph(fragmentGraph));
			}
			logger.info("Number of matches: " + matches.size());
		}
		for (NodeMatch match : matches) writer.println("nodematch: " + match);
		return matches;
	}

	/**
	 * Compute most frequent category in training set.
	 * 
	 * @param trainingDocs
	 * @return
	 */
	private String computeMostFrequentCategory(
			List<Interaction> trainingDocs) {
		Map<String, Float> categoryOccurrences = new HashMap<String, Float>();
		for (Interaction interaction : trainingDocs) {
			String[] cats = interaction.getCategories();
			Set<String> catsSet = new HashSet<String>(Arrays.asList(cats));
			for (String cat : catsSet) {
				float occ = 1;
				if (categoryOccurrences.containsKey(cat)) {
					occ += categoryOccurrences.get(cat);
				}
				categoryOccurrences.put(cat, occ);
			}
		}
		ValueComparator bvc =  new ValueComparator(categoryOccurrences);
		Map<String,Float> sortedMap = new TreeMap<String,Float>(bvc);
		sortedMap.putAll(categoryOccurrences);
		logger.debug("category sums:  " + sortedMap);
		String mostFrequentCat = "N/A";
		if (sortedMap.size() > 0) {
			mostFrequentCat = sortedMap.keySet().iterator().next().toString();
			logger.info("Most probable category: " + mostFrequentCat);
			logger.info("Occurs " + categoryOccurrences.get(mostFrequentCat) + " times");
			logger.info("Number of training docs " + trainingDocs.size());
			logger.info("Baseline: " + (double) categoryOccurrences.get(mostFrequentCat)/ (double) trainingDocs.size());
		}
		
//		System.exit(0);
		return mostFrequentCat;
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

	private EntailmentGraphCollapsed buildCollapsedGraphWithCategoryInfo(
			EntailmentGraphRaw egr) throws GraphOptimizerException,
			ConfidenceCalculatorException {
		logger.info("Merged graph contains " + egr.vertexSet().size() + " nodes and " + egr.edgeSet().size() + " edges");
		EntailmentGraphCollapsed graph = graphOptimizer.optimizeGraph(egr, thresholdForOptimizing);
		logger.info("Built collapsed graph.");		
		startTime = endTime;
		endTime   = System.currentTimeMillis();
		logger.info((endTime - startTime));		
		confidenceCalculator.computeCategoryConfidences(graph);
		logger.info("Computed category confidences and added them to graph.");		
		startTime = endTime;
		endTime   = System.currentTimeMillis();
		logger.info((endTime - startTime));
		return graph;
	}

	/**
	 * Builds raw graph from a list of interactions. 
	 * 
	 * @param graphDocs
	 * @param mergedGraphFile
	 * @param egr
	 * @param minOccurrence
	 * @return
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 * @throws ConfigurationException
	 * @throws EDAException
	 * @throws ComponentException
	 * @throws GraphMergerException
	 * @throws LAPException
	 * @throws TransformerException
	 * @throws EntailmentGraphRawException
	 * @throws LexicalResourceException 
	 */	
	private EntailmentGraphRaw buildRawGraph(List<Interaction> graphDocs, boolean relevantTextProvided, 
			File mergedGraphFile, EntailmentGraphRaw egr, int minOccurrence)
			throws FragmentAnnotatorException, ModifierAnnotatorException,
			FragmentGraphGeneratorException, ConfigurationException,
			EDAException, ComponentException, GraphMergerException,
			LAPException, TransformerException, EntailmentGraphRawException, LexicalResourceException {
		logger.info("Initialized config.");
		
		Set<FragmentGraph> fgs = buildFragmentGraphs(graphDocs, relevantTextProvided, 
				fragmentAnnotatorForGraphBuilding, modifierAnnotator, fragmentGraphGenerator);
		
		if (setup == 0) {
			int count = 0; 
			for (FragmentGraph fg : fgs) {
				count++;
				logger.info("Adding fragment graph " +count+ " out of " + fgs.size());
				for (EntailmentUnitMention eum : fg.vertexSet()) {
					egr.addEntailmentUnitMention(eum, fg.getCompleteStatement().getTextWithoutDoubleSpaces());					
				}
			}			
		}  else if (!useGraphMerger) { //build graphs without graph merger (use methods from EvaluatorUtils)
				if(!(setup >=207 && setup <= 210)) {
					logger.error("Wrong setup number for building graph without graph merger");
					System.exit(1);
				}
				if(fragmentTypeNameGraph.equals("TF")){
					EvaluatorUtils.mergeIntoTokenGraph(egr, fgs, dbr, gnw, germaNetRelations, splitter, mapNegation);
				}
				else if(fragmentTypeNameGraph.equals("DF")){
					EvaluatorUtils.mergeIntoDependencyGraph(egr, fgs, dbr, gnw, germaNetRelations, splitter, onlyBidirectionalEdges, mapNegation);
				}
				else {
					logger.error("Wrong fragment type for building graph without graph merger");
					System.exit(1);
				}
			
		} else { //merge graph --> takes a really long time and uses too much memory: TODO reduce number of fgs
			if (setup == 11) alignmenteda.initialize(configFile);
			else eda.initialize(config);
			Set<FragmentGraph> fgsReduced = new HashSet<FragmentGraph>();
			Set<FragmentGraph> fgsRest = new HashSet<FragmentGraph>();
			String text = "";
			HashMap<String, Integer> tokenOccurrences = computeTokenOccurrences(fgs);
			for (FragmentGraph fg : fgs) {
				text = fg.getBaseStatement().getTextWithoutDoubleSpaces();
				//TODO: get lemma and use it as node name?
				if (
					//	GermaNetLexicon.contains(text) ||  	// merge only fragments with an entry in GermaNet
						tokenOccurrences.get(text.toLowerCase()) >= minOccurrence) fgsReduced.add(fg); 
							// merge only fragments occurring at least as often as the threshold
				else fgsRest.add(fg); //add remaining fragments to graph (no EDA call --> no edges)
			}
			logger.info("fgs contains " + fgs.size() + " fgs");
			logger.info("fgsreduced contains " + fgsReduced.size() + " fgs");
			egr = graphMerger.mergeGraphs(fgsReduced, egr);
			logger.info("Merged graph: " +egr.vertexSet().size()+ " nodes");
			for (FragmentGraph fg : fgsRest) {
				for (EntailmentUnitMention eum : fg.vertexSet()) {
					egr.addEntailmentUnitMention(eum, fg.getCompleteStatement().getTextWithoutDoubleSpaces());	
				}
			}
			logger.info("Added " + fgsRest.size() + " remaining mentions");
			logger.info("Merged graph + remaining mentions: " +egr.vertexSet().size()+ " nodes");
		}
		logger.info("Writing merged graph to " + mergedGraphFile.getAbsolutePath()); 
		XMLFileWriter.write(egr.toXML(), mergedGraphFile.getAbsolutePath());
		logger.info("Merged graph + remaining mentions: " +egr.vertexSet().size()+ " nodes");	
		return egr; 
	}

	private Set<FragmentGraph> buildFragmentGraphs(List<Interaction> graphDocs, 
			boolean relevantTextProvided, FragmentAnnotator fragmentAnnotator, 
			ModifierAnnotator modifierAnnotator, FragmentGraphGenerator fragmentGraphGenerator
			) throws FragmentAnnotatorException,
			ModifierAnnotatorException, FragmentGraphGeneratorException {
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();	
		for(Interaction interaction: graphDocs) {
			logger.info("-----------------------------------------------------");
			logger.info("Processing graph interaction " + interaction.getInteractionId() + " with category " + interaction.getCategoryString());
			List<JCas> cases;
			JCas cas;
			try {
				cases = interaction.createAndFillInputCASes(relevantTextProvided);
				for (int j=0; j<cases.size(); j++) {
					cas = cases.get(j);
					logger.info("Gold category/ies: " + CASUtils.getTLMetaData(cas).getCategory());
					if (CASUtils.getTLMetaData(cas).getCategory().contains(",")) { //Shouldn't happen because we create a separate CAS for each category assignment
						logger.info("Category contains comma in " + this.getClass());
						System.exit(0);
					}
					fragmentAnnotator.annotateFragments(cas);
					if (cas.getAnnotationIndex(DeterminedFragment.type).size() > 0) {
						modifierAnnotator.annotateModifiers(cas);
					}
					logger.debug("Adding fragment graphs for text: " + cas.getDocumentText());
					fgs.addAll(fragmentGraphGenerator.generateFragmentGraphs(cas));
				}			
			} catch (LAPException e) {
				e.printStackTrace();
			}
		}
		logger.info("Built fragment graphs: " +fgs.size()+ " graphs.");
		return fgs;
	}

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
	 * This evaluation is expected to be more time-consuming!
	 */
	public void runEvaluationOnSingleDataset() {
		//1. read in all emails with their associated categories 
		
		//for each email E
			//2. read in entailment graph G generated on remaining emails (from resources)
		 	//3. send E + G to node matcher / category annotator and have it annotated
		
		//4. compare automatic to manual annotation
		
		//5. compute and print result		
		
	}
	
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
		
		List<Double> accuracyPerRun = new ArrayList<Double>();
		String documentsFilename = inputDataFoldername + "omq_public_1_emails_unsorted.xml"; //TODO: replace 1 by "all" at some point
//		String documentsFilename = inputDataFoldername + "omq_public_emails_all_unsorted.xml"; 
		JCas cas = CASUtils.createNewInputCas();
		String mostProbableCat; 
		int countPositive = 0;
		int run = 1;

		//Read and documents for annotation and sort by interaction ID
		logger.info("Reading documents from " + documentsFilename);	    		
		List<Interaction> docs = new ArrayList<Interaction>();
		docs.addAll(InteractionReader.readInteractionXML(new File(documentsFilename)));
        Collections.sort(docs);
        
        //Build fragment graphs and compute token occurrences (for filtering fragments to be merged)
        /*
        Set<FragmentGraph> allFgs = new HashSet<FragmentGraph>();
		Set<FragmentGraph> fgs = buildFragmentGraphs(docs, relevantTextProvided, 
				null, null, null);
		allFgs.addAll(fgs);
		HashMap<String,Integer> tokenOccurrences = computeTokenOccurrences(allFgs);
         */
        //TODO: Think about how to integrate filtering using different kinds of frags

        
		//Build graph from category texts (assuming they are available beforehand)
		List<Interaction> graphDocs = new ArrayList<Interaction>(); 
		graphDocs.addAll(CategoryReader.readCategoryXML(new File(categoriesFilename)));
		logger.info("Added " + graphDocs.size() + " categories");
		File mergedGraphFile = new File(outputGraphFoldername + "/incremental/omq_public_"+run+"_merged_graph_categories_"  + setup + "_" 
		+ minTokenOccurrenceInCategories + "_" + fragmentTypeNameEval + "_" + decompositionType + "_" + thresholdForRawGraphBuilding + "_" + edaName + ".xml");	
		FragmentAnnotator faTokens = new TokenAsFragmentAnnotatorForGerman(lapLemma, tokenPosFilter, decompositionType); 
		FragmentAnnotator faDeps = new DependencyAsFragmentAnnotatorForGerman(lapDependency, governorPosFilter, dependentPosFilter, decompositionType);
		FragmentAnnotator faSents = new SentenceAsFragmentAnnotator(lapDependency);
		ModifierAnnotator maAdv = new AdvAsModifierAnnotator(lapLemma);
		FragmentGraphGenerator fgg = new FragmentGraphLiteGeneratorFromCAS();
		
		EntailmentGraphRaw egr = new EntailmentGraphRaw(); //complete raw graph
		EntailmentGraphRaw singleTokenGraph = new EntailmentGraphRaw(); //single token graph
		EntailmentGraphRaw twoTokenGraph = new EntailmentGraphRaw(); //two token graph
		EntailmentGraphRaw sentenceGraph = new EntailmentGraphRaw(); //sentence graph
		
		GraphMerger graphMergerSEDA = null; 
		GraphMerger graphMergerEDA = null; 
		
		//Step 1: Create graph from token fragments and add it to graph
		singleTokenGraph = new EntailmentGraphRaw(true); //addLemmaLabel must be true!
		Set<FragmentGraph> fgs = buildFragmentGraphs(graphDocs, false, faTokens, maAdv, fgg); //TODO: set category text als relevantText in XML 
		EvaluatorUtils.mergeIntoLemmaTokenGraph(singleTokenGraph, fgs);
		egr.copyRawGraphNodesAndAllEdges(singleTokenGraph); //TODO: check if this works properly!
			
		//Step 2: Create graph from dependency fragments and add it to graph
		if (getSEDA() != null) {
			fgs = buildFragmentGraphs(graphDocs, relevantTextProvided, faDeps, maAdv, fgg);
			graphMergerSEDA = new LegacyAutomateWP2ProcedureGraphMerger(lapDependency, getSEDA());
			graphMergerSEDA.setEntailmentConfidenceThreshold(thresholdForSEDA);
			twoTokenGraph = graphMergerSEDA.mergeGraphs(fgs, new EntailmentGraphRaw());
			//TODO: maybe replace by new call based on Aleksandras latest changes
			egr.copyRawGraphNodesAndAllEdges(twoTokenGraph); //TODO: check if this works properly!
		}
		
		//Step 3: Create graph from sentence fragments and add it to graph
		if (getEDA() != null) {
			fgs = buildFragmentGraphs(graphDocs, relevantTextProvided, faSents, maAdv, fgg);
			graphMergerEDA = new LegacyAutomateWP2ProcedureGraphMerger(lapDependency, getEDA());
			graphMergerEDA.setEntailmentConfidenceThreshold(thresholdForEDA);
			sentenceGraph = graphMergerEDA.mergeGraphs(fgs, new EntailmentGraphRaw());
			egr.copyRawGraphNodesAndAllEdges(sentenceGraph); //TODO: check if this works properly!
		}
		
		logger.info("Number of nodes in (category) raw graph: " + egr.vertexSet().size());
		logger.info("Number of edges in (category) raw graph: " + egr.edgeSet().size());
		EntailmentGraphCollapsed egc = buildCollapsedGraphWithCategoryInfo(egr);
		logger.info("Number of nodes in (category) collapsed graph: " + egc.vertexSet().size());
		logger.info("Number of edges in (category) collapsed graph: " + egc.edgeSet().size());
		File graphFile = new File(outputGraphFoldername + "/incremental/omq_public_"+run+"_graph_categories_"+setup + "_" + minTokenOccurrenceInCategories + "_"
				+ fragmentTypeNameEval + "_" + decompositionType + "_" + method + "_" + termFrequencyDocument + documentFrequencyDocument + normalizationDocument 
				+ "_cb" + categoryBoost + "_" + thresholdForOptimizing + "_" + edaName + ".xml");
		XMLFileWriter.write(egc.toXML(), graphFile.getAbsolutePath());			

		//Iterate through interactions, annotate each using existing graph and then add interaction to graph 
		for (Interaction doc : docs) {	
			logger.info("Processing doument " + run + " out of " + docs.size());
			//if (run > 50) break; //TODO: Remove (debugging only)
			//Annotate document and compare to manual annotation
			logger.info("Graph contains " + egc.vertexSet().size() + " nodes ");
			mostProbableCat = EvaluatorUtils.computeMostFrequentCategory(egc); //compute most frequent category in graph
	    	//Index graph nodes and initialize search
			if (LuceneSearch) {
				nodeMatcherWithIndex = new NodeMatcherLuceneSimple(egc, "./src/test/resources/Lucene_index/incremental/", new GermanAnalyzer(Version.LUCENE_44));
				nodeMatcherWithIndex.indexGraphNodes();
				nodeMatcherWithIndex.initializeSearch();
			} else {
				nodeMatcher = new NodeMatcherLongestOnly(egc, bestNodeOnly);
			}
			
			//Annotate new interaction using existing collapsed graph
			//First: collect all fragment graphs (at all three levels)
			Set<FragmentGraph> fgAll = new HashSet<FragmentGraph>();
			logger.info("Annotating interaction " + doc.getInteractionId());
			doc.fillInputCAS(cas);
			//Again, three steps: token, dependency and sentence fragments
			faTokens.annotateFragments(cas);
			Set<FragmentGraph> fgTokens = fgg.generateFragmentGraphs(cas);
			fgAll.addAll(fgTokens);
			EvaluatorUtils.mergeIntoLemmaTokenGraph(singleTokenGraph, fgTokens);
			cas.reset();
			doc.fillInputCAS(cas);
			faDeps.annotateFragments(cas);
			Set<FragmentGraph> fgDeps = fgg.generateFragmentGraphs(cas);
			fgAll.addAll(fgDeps);
			if (getSEDA() != null) { //add new fragments to complete raw graph
				twoTokenGraph = graphMergerSEDA.mergeGraphs(fgDeps, twoTokenGraph);
				egr.copyRawGraphNodesAndAllEdges(twoTokenGraph); //TODO: check if this works properly!
			}
			cas.reset();
			doc.fillInputCAS(cas);
			faSents.annotateFragments(cas);
			if(cas.getAnnotationIndex(DeterminedFragment.type).size() > 0){
				modifierAnnotator.annotateModifiers(cas);
			}
			Set<FragmentGraph> fgSents = fgg.generateFragmentGraphs(cas);
			fgAll.addAll(fgSents);
			if (getEDA() != null) { //add new fragments to complete raw graph
				sentenceGraph = graphMergerEDA.mergeGraphs(fgSents, sentenceGraph);
				egr.copyRawGraphNodesAndAllEdges(sentenceGraph); //TODO: check if this works properly!
			}
			
			/*
			allFgs.addAll(fragmentGraphs);
			tokenOccurrences = computeTokenOccurrences(allFgs);
			writer.println("fragmentGraphs: " + fragmentGraphs.iterator().next().getCompleteStatement());
			*/
			//TODO: see above (filtering with diff. frags?)
			//logger.info("Number of fragment graphs: " + fragmentGraphs.size());
			
			writer.println(doc.getInteractionId() + " : ");
			writer.println(fgAll.size() + " fragment graphs ");
			logger.info(fgAll.size() + " fragment graphs");
			Set<NodeMatch> matches = getMatches(egc, fgAll);	
			writer.println(egc.vertexSet().size() + " nodes in graph");
			writer.println(matches.size() + " matches");
			for (NodeMatch nm : matches) {
				writer.println("... " + nm.getMention());
				writer.println("... " + nm.getScores().size());
			}
			
			//add category annotation to CAS
			categoryAnnotator.addCategoryAnnotation(cas, matches);

	    	//Compare automatic to manual annotation
			writer.println(cas.getDocumentText() + "");
			writer.println(CASUtils.getCategoryAnnotationsInCAS(cas).size() + " category annotations");
			
			Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
			writer.println(decisions.size() + " decisions ");
			
			countPositive = EvaluatorUtils.compareDecisionsForInteraction(countPositive,
					doc, decisions, mostProbableCat, egc, matches, topN, 
					method, bestNodeOnly, documentFrequencyQuery, termFrequencyQuery, 
					lengthBoost);
			
			writer.println(doc.getInteractionId() + " : " + countPositive);
			
			double accuracy = (double) countPositive / (double) run;
			logger.info("Accuracy in run " + run + ": " + accuracy);
			accuracyPerRun.add(accuracy);
			run++;
			
			// Build collapsed graph from extended raw graph
			egc = buildCollapsedGraphWithCategoryInfo(egr);			
			mergedGraphFile = new File(outputGraphFoldername + "/incremental/omq_public_"+run+"_merged_graph_"+setup + "_" 
					+ minTokenOccurrence + "_" + fragmentTypeNameEval + "_" + decompositionType + "_" + thresholdForRawGraphBuilding + "_" + edaName + ".xml");	
			graphFile = new File(outputGraphFoldername + "/incremental/omq_public_"+run+"_graph_"+setup + "_" + minTokenOccurrence + "_"
					+ fragmentTypeNameEval + "_" + decompositionType + "_" + method + "_" + termFrequencyDocument + documentFrequencyDocument + normalizationDocument 
					+ "_cb" + categoryBoost + "_" + thresholdForOptimizing + "_" + edaName + ".xml");
			XMLFileWriter.write(egr.toXML(), mergedGraphFile.getAbsolutePath());			
			XMLFileWriter.write(egc.toXML(), graphFile.getAbsolutePath());			
		}
		for (int i=1; i<accuracyPerRun.size(); i++) {
 			logger.info(accuracyPerRun.get(i));
		}
	}
	
	private SimpleEDA_DE getSEDA() throws IOException {
		switch (setupSEDA) {
			case 101: //SimpleEDA_DE, LEMMA + CONVERSION
				return new SimpleEDA_DE();
			case 102: //SimpleEDA_DE, LEMMA + CONVERSION + GERMANET
				return new SimpleEDA_DE(pathToGermaNet, useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);
			case 103: //SimpleEDA_DE, LEMMA + CONVERSION + DERIVATION (2 derivation steps, no POS in query)
				return new SimpleEDA_DE(derivSteps);
			case 104: //SimpleEDA_DE, LEMMA + CONVERSION + DECOMPOSITION
				splitter = new GermanWordSplitter();
				return new SimpleEDA_DE(splitter);
			case 105: //SimpleEDA_DE, LEMMA + CONVERSION + DERIVATION + GERMANET (2 derivation steps, no POS in query)
				return new SimpleEDA_DE(derivSteps, pathToGermaNet, 
				useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);
			case 106: //SimpleEDA_DE, LEMMA + DECOMPOSITION + DERIVATION + GERMANET (2 derivation steps, no POS in query)
				splitter = new GermanWordSplitter();
				eda = new SimpleEDA_DE(splitter, derivSteps, pathToGermaNet, 
				useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);			
		}
		return null;
	}

	private EDABasic<?> getEDA() throws ConfigurationException, EDAException, ComponentException {
		switch (setupEDA) {
			case 1: //TIE with base configuration (inflection only)
        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml";
        		configFile = new File(configFilename);
        		config = new ImplCommonConfig(configFile);
				eda = new MaxEntClassificationEDA();
				eda.initialize(config);
				return eda;
			case 2: //TIE with base configuration + GermaNet 
				configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+GN_DE.xml";;
				configFile = new File(configFilename);
				config = new ImplCommonConfig(configFile);
				eda = new MaxEntClassificationEDA();
				eda.initialize(config);
				return eda;
		}
		return null;
	}

	/**
	 * This method initializes CachedLapAccess and FragmentAnnotator
	 * depending on passed short fragment name
	 * fragment name = "TF" for token fragments
	 * fragment name = "DF" for dependency fragments
	 * fragment name = "TDF" for token and dependency fragments
	 * fragment name = "SF" for sentence fragments
	 * @param fragmentTypeName - short name for fragments
	 */
	private void setLapAndFragmentAnnotator(String fragmentTypeNameGraph){
		fragmentTypeNameEval = fragmentTypeNameGraph;
		setLapAndFragmentAnnotator(fragmentTypeNameGraph, fragmentTypeNameEval);
	}
	
	/**
	 * This method initializes CachedLapAccess and FragmentAnnotator
	 * depending on passed short fragment name
	 * fragment name = "TF" for token fragments
	 * fragment name = "DF" for dependency fragments
	 * fragment name = "TDF" for token and dependency fragments
	 * fragment name = "SF" for sentence fragments
	 * @param fragmentTypeName - short name for fragments
	 */
	private void setLapAndFragmentAnnotator(String fragmentTypeNameGraph, String fragmentTypeNameEval){
		
		try{
			if(fragmentTypeNameGraph.equalsIgnoreCase("TF")){
				lapForFragments = new CachedLAPAccess(new LemmaLevelLapDE());//TreeTaggerDE()
	    		fragmentAnnotatorForGraphBuilding = new TokenAsFragmentAnnotatorForGerman(lapForFragments, tokenPosFilter, decompositionType);
			} else if(fragmentTypeNameGraph.equalsIgnoreCase("DF")){
				lapForFragments = new CachedLAPAccess(new DependencyLevelLapDE());//MaltParserDE();
        		fragmentAnnotatorForGraphBuilding = new DependencyAsFragmentAnnotatorForGerman(lapForFragments, 
        				governorPosFilter, governorWordFilter, dependentPosFilter, dependentWordFilter, decompositionType);
        	} else if(fragmentTypeNameGraph.equalsIgnoreCase("SF")){
		    	lapForFragments = new CachedLAPAccess(new DependencyLevelLapDE());//MaltParserDE();
        		fragmentAnnotatorForGraphBuilding = new SentenceAsFragmentAnnotator(lapForFragments);
        	}
			if(fragmentTypeNameEval.equalsIgnoreCase("TF")){
				lapForDecisions = new CachedLAPAccess(new LemmaLevelLapDE());//TreeTaggerDE()
				fragmentAnnotatorForNewInput = new TokenAsFragmentAnnotatorForGerman(lapForDecisions, tokenPosFilter, decompositionType);
			} else if(fragmentTypeNameEval.equalsIgnoreCase("DF")){
        		lapForDecisions = new CachedLAPAccess(new DependencyLevelLapDE());//MaltParserDE();
        		fragmentAnnotatorForNewInput = new DependencyAsFragmentAnnotatorForGerman(lapForFragments, 
        				governorPosFilter, governorWordFilter, dependentPosFilter, dependentWordFilter, decompositionType);
		    } else if(fragmentTypeNameEval.equalsIgnoreCase("SF")){
        		lapForDecisions = new CachedLAPAccess(new DependencyLevelLapDE());//MaltParserDE();
        		fragmentAnnotatorForNewInput = new SentenceAsFragmentAnnotator(lapForDecisions);
			}
		} catch (FragmentAnnotatorException | LAPException e) {
			e.printStackTrace();
		} 			
	}
	
	
	@SuppressWarnings("unused")
	private static Set<String> getGermaNetLexicon() throws LAPException, FragmentAnnotatorException, FileNotFoundException, XMLStreamException, IOException {
		// check token annotation is there or not 
		germanet = new GermaNet(pathToGermaNet);
		Set<String> lexicon = new HashSet<String>(); 
		List<LexUnit> lexunits = germanet.getLexUnits();
		for (LexUnit lexunit : lexunits) {
			lexicon.addAll(lexunit.getOrthForms());
		}
		return lexicon;
	}			
}


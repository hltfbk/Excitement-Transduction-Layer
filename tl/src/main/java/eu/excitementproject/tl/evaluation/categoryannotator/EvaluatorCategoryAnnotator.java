package eu.excitementproject.tl.evaluation.categoryannotator;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.uima.jcas.JCas;

import eu.excitement.type.tl.CategoryAnnotation;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.api.ConfidenceCalculator;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.api.NodeMatcherWithIndex;
import eu.excitementproject.tl.composition.categoryannotator.CategoryAnnotatorAllCats;
import eu.excitementproject.tl.composition.confidencecalculator.ConfidenceCalculatorCategoricalFrequencyDistribution;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.ConfidenceCalculatorException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLuceneSimple;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;
import eu.excitementproject.tl.toplevel.usecasetworunner.UseCaseTwoRunnerPrototype;

/**
 * 
 * @author Kathrin
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
 */

public class EvaluatorCategoryAnnotator { 
	
    private static Logger logger = Logger.getLogger(EvaluatorCategoryAnnotator.class); 
    
    static LAPAccess lap;
    static CommonConfig config;
	static String configFilename; //config file for EDA
	static EDABasic<?> eda;
    static FragmentAnnotator fragmentAnnotatorForGraphBuilding;
    static FragmentAnnotator fragmentAnnotatorForNewInput;
    static ModifierAnnotator modifierAnnotator;
    static FragmentGraphGenerator fragmentGraphGenerator;
    static GraphMerger graphMerger;
    static GraphOptimizer graphOptimizer;
    static NodeMatcherWithIndex nodeMatcher;
	static CategoryAnnotator categoryAnnotator;
	static ConfidenceCalculator confidenceCalculator;
	
	static double thresholdForOptimizing = 0.99;

    static int setup;
    
    static boolean readGraphFromFile = false;
    
	public static void main(String[] args) {
		String inputFoldername = "./src/test/resources/WP2_public_data_XML/OMQ/"; //dataset to be evaluated
		String outputGraphFilename = "./src/test/resources/sample_graphs/graph.xml"; //output directory (for generated entailment graph)
				
		setup(1);
		
		//runEvaluationOnTrainTestDataset(inputFilename, outputDirname, configFilename);
		try {
			runEvaluationXFoldCross(inputFoldername, outputGraphFilename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pick a specific evaluation setup.
	 * 
	 * @param i
	 */
	private static void setup(int i) {
		try {
			switch(i){
	        	case 1:
	        		lap = new LemmaLevelLapDE(); //lap = new MaltParserDE();
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml";
	        		File configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();	
		    		fragmentAnnotatorForGraphBuilding = new SentenceAsFragmentAnnotator(lap);
		    		fragmentAnnotatorForNewInput = new SentenceAsFragmentAnnotator(lap);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lap); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new AutomateWP2ProcedureGraphMerger(lap, eda);
		    		graphOptimizer = new SimpleGraphOptimizer();
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution();
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
			}
		} catch (LAPException | FragmentAnnotatorException | ModifierAnnotatorException | ConfigurationException e) {
			e.printStackTrace();
		} catch (GraphMergerException e) {
			e.printStackTrace();
		} 			
	}

	public static double runEvaluationOnTrainTestDataset(String inputFilename, String outputDirname, String configFilename) {
		
		UseCaseOneRunnerPrototype use1;
		UseCaseTwoRunnerPrototype use2;
		
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
			
			eda.initialize(config);
			logger.info("Initialized config.");
			use1 = new UseCaseOneRunnerPrototype(lap, eda, outputDirname);
			double threshold = 0.99;
			EntailmentGraphCollapsed graph = use1.buildCollapsedGraph(docsTrain, threshold);
			logger.info("Built collapsed graph.");
			confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution();
			confidenceCalculator.computeCategoryConfidences(graph);
			String outputFile = outputDirname + "/test.graph.xml";
			graph.toXML(outputFile);			
			graph = new EntailmentGraphCollapsed(new File(outputFile));
			//GraphViewer.drawGraph(graph);

			logger.info("Computed and added category confidences.");

			// Send each email in test data + graph to node use case 2 and have it annotated
			int countPositive = 0;
			for (Interaction doc : docsTest) {
				JCas cas;
				cas = doc.createAndFillInputCAS();
				use2 = new UseCaseTwoRunnerPrototype(lap, eda);
				use2.annotateCategories(cas, graph);
				logger.info("_________________________________________________________");
				Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
				logger.info("Found " + decisions.size() + " decisions in CAS for interaction " + doc.getInteractionId());
				CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
				
				countPositive = compareDecisionsForInteraction(countPositive,
						doc, decisions);				
			}
			
			// Compute and print result	
			double result = (double) countPositive / (double) docsTest.size();
			logger.info("Final result: " + result);
			return result;
			
		} catch (ConfigurationException | EDAException | ComponentException 
			| FragmentAnnotatorException | ModifierAnnotatorException 
			| GraphMergerException | IOException 
			| GraphOptimizerException 
			| FragmentGraphGeneratorException 
			| ConfidenceCalculatorException 
			| NodeMatcherException 
			| CategoryAnnotatorException | DataReaderException | EntailmentGraphCollapsedException e) {
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
	private static int compareDecisionsForInteraction(int countPositive,
			Interaction doc, Set<CategoryDecision> decisions) {
		return compareDecisionsForInteraction(countPositive, doc, decisions, "N/A");
	}
	
	/**
	 * Compare automatic to manual annotation on interaction level
	 * 
	 * @param countPositive
	 * @param doc
	 * @param decisions
	 * @param mostProbableCat
	 * @return
	 */
	private static int compareDecisionsForInteraction(int countPositive,
			Interaction doc, Set<CategoryDecision> decisions, String mostProbableCat) {
		String bestCat = "";
		HashMap<String,Double> categoryScores = new HashMap<String,Double>();
		logger.debug("Number of decisions: " + decisions.size());
		for (CategoryDecision decision: decisions) {
			String category = decision.getCategoryId();
			double sum = 0;
			if (categoryScores.containsKey(category)) {
				sum = categoryScores.get(category);
			}
			//add up all scores for each category on the CAS
			sum += decision.getConfidence();
			categoryScores.put(category, sum);
		}
		// get the category with the highest sum
		ValueComparator bvc =  new ValueComparator(categoryScores);
		Map<String,Double> sortedMap = new TreeMap<String,Double>(bvc);
		sortedMap.putAll(categoryScores);
		logger.debug("category sums:  " + sortedMap);
		if (sortedMap.size() > 0) {
			bestCat = sortedMap.keySet().iterator().next().toString();
			logger.info("Best category: " + bestCat);
			logger.info("Correct category: " + doc.getCategory());
		} else bestCat = mostProbableCat;
		if (doc.getCategory().equals(bestCat)) {
			countPositive++;
		} 
		return countPositive;
	}
	
	/**
	 * Runs X-fold cross-validation on X files found in the input directory. For each fold, it uses one input
	 * file for testing and the remaining input files for building the entailment graph used for category
	 * annotation. 
	 * 
	 * @param inputDataFoldername
	 * @param outputGraphFilename
	 * @throws Exception
	 */
	public static void runEvaluationXFoldCross(String inputDataFoldername, String outputGraphFilename) throws Exception {
		Map<Integer, File> fileIndex = indexFilesinDirectory(inputDataFoldername);	    	
	    
	    //check if there are at least two files in the dir
	    double numberOfFolds = fileIndex.size();
	    if (numberOfFolds < 2) {
    		logger.warn("Please specify a folder with at least two files (train + test)!");
    		return;
	    }	    	
	    
	   	logger.info("Creating " + numberOfFolds + " folds.");
	   	Map<Integer, Double> foldAccuracies = new HashMap<Integer, Double>();
	   			
	    for (int i=1; i<=fileIndex.size(); i++) { //for each input file
	    	logger.info("Creating fold " + i);
	    	//Create a fold for each file, with the file being the test data and the 
	    	//remaining files being used for building the graph
    		Set<Interaction> trainingDocs = new HashSet<Interaction>();
    		Set<Interaction> testDocs = new HashSet<Interaction>();
	    	for (int j=1; j<=fileIndex.size(); j++) {
				File f = fileIndex.get(j);
	    		if (i==j) { //add documents in test file to test set
	    			logger.info("Reading file " + f.getName());	    			
					testDocs.addAll(InteractionReader.readInteractionXML(f));
	    			logger.info("Test set of fold "+i+" now contains " + testDocs.size() + " documents");
	    		} else { //add documents in remaining files to training set
	    			logger.info("Reading file " + f.getName());	    			
					trainingDocs.addAll(InteractionReader.readInteractionXML(f));
	    			logger.info("Training set of fold "+i+" now contains " + trainingDocs.size() + " documents");
	    		}
	        }
	    	
	    	//For each fold, generate entailment graph EG from training set
	    	EntailmentGraphCollapsed graph;
	    	if (readGraphFromFile) {
	    		graph = new EntailmentGraphCollapsed(new File(outputGraphFilename));
	    	} else {
		    	graph = buildGraph(trainingDocs);
				graph.toXML(outputGraphFilename);			
	    	}
	    	
    		String mostProbableCat = computeMostProbablyCategory(trainingDocs);
	    	
	    	//For each email E in the test set, send it to nodematcher / category annotator and have it annotated
			int countPositive = 0;
			for (Interaction interaction : testDocs) {
				JCas cas = annotateInteraction(graph, interaction);	
				
				//print CAS category
				CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
				
		    	//Compare automatic to manual annotation
				Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
				countPositive = compareDecisionsForInteraction(countPositive,
						interaction, decisions, mostProbableCat);				
			}
	    	logger.info("Count positive: " + countPositive);
		    double accuracyInThisFold = ((double)countPositive / (double)testDocs.size());
		    foldAccuracies.put(i, accuracyInThisFold);
	    }	
	    printResult(numberOfFolds, foldAccuracies);
			
	}

	/**
	 * Compute and print final result
	 * 
	 * @param numberOfFolds
	 * @param foldAccuracy
	 */
	private static void printResult(double numberOfFolds,
			Map<Integer, Double> foldAccuracy) {
		double sumAccuracies = 0;
	    for (int fold : foldAccuracy.keySet()) {
	    	double accuracy = foldAccuracy.get(fold);
	    	logger.info("Accuracy in fold " + fold + ": " + accuracy);
	    	sumAccuracies += accuracy;
	    }
	    logger.info("Overall accurracy: " + (sumAccuracies / (double)numberOfFolds));
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
	private static JCas annotateInteraction(EntailmentGraphCollapsed graph,
			Interaction interaction) throws LAPException,
			FragmentAnnotatorException, ModifierAnnotatorException,
			FragmentGraphGeneratorException, NodeMatcherException,
			CategoryAnnotatorException {
		JCas cas = interaction.createAndFillInputCAS();
		fragmentAnnotatorForNewInput.annotateFragments(cas);
		modifierAnnotator.annotateModifiers(cas);
		logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
		Set<FragmentGraph> fragmentGraphs = fragmentGraphGenerator.generateFragmentGraphs(cas);
		logger.info("Number of fragment graphs: " + fragmentGraphs.size());

		//call node matcher on each fragment graph
		nodeMatcher = new NodeMatcherLuceneSimple(graph, "./src/test/resources/Lucene_index/", new StandardAnalyzer(Version.LUCENE_44));
		nodeMatcher.indexGraphNodes();
		nodeMatcher.initializeSearch();
		for (FragmentGraph fragmentGraph: fragmentGraphs) {
			System.out.println("fragment graph: " + fragmentGraph.getCompleteStatement());
			Set<NodeMatch> matches = nodeMatcher.findMatchingNodesInGraph(fragmentGraph);
			System.out.println("matches: " + matches.size());
			//add category annotation to CAS
			categoryAnnotator.addCategoryAnnotation(cas, matches);
		}
		return cas;
	}

	/**
	 * Compute most probable category in training set
	 * 
	 * @param trainingDocs
	 * @return
	 */
	private static String computeMostProbablyCategory(
			Set<Interaction> trainingDocs) {
		Map<String, Double> categoryOccurrences = new HashMap<String,Double>();
		for (Interaction interaction : trainingDocs) {
			String cat = interaction.getCategory();
			double occ = 1;
			if (categoryOccurrences.containsKey(cat)) {
				occ += categoryOccurrences.get(cat);
			}
			categoryOccurrences.put(cat, occ);
		}
		ValueComparator bvc =  new ValueComparator(categoryOccurrences);
		Map<String,Double> sortedMap = new TreeMap<String,Double>(bvc);
		sortedMap.putAll(categoryOccurrences);
		logger.debug("category sums:  " + sortedMap);
		String mostProbableCat = "N/A";
		if (sortedMap.size() > 0) {
			mostProbableCat = sortedMap.keySet().iterator().next().toString();
			logger.info("Most probably category: " + mostProbableCat);
		}
		return mostProbableCat;
	}

	/**
	 * Read and index all files in the input folder 
	 * 
	 * @param inputDataFoldername
	 * @return
	 */
	private static Map<Integer, File> indexFilesinDirectory(
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

	/**
	 * Build collapsed graph from interactions, including category information.
	 * 
	 * @param trainingDocs
	 * @return
	 * @throws Exception 
	 */
	private static EntailmentGraphCollapsed buildGraph(Set<Interaction> trainingDocs) throws Exception {
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();			
		JCas cas = CASUtils.createNewInputCas();
		for(Interaction i: trainingDocs) {
			i.fillInputCAS(cas); 
			fragmentAnnotatorForGraphBuilding.annotateFragments(cas);
			modifierAnnotator.annotateModifiers(cas);
			logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
			fgs.addAll(fragmentGraphGenerator.generateFragmentGraphs(cas));
		}
		logger.info("Created " + fgs.size() + " fragment graphs.");
		eda.initialize(config);
		logger.info("Initialized config.");
		EntailmentGraphRaw egr = graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());
		logger.info("Merged graphs.");
		EntailmentGraphCollapsed graph = graphOptimizer.optimizeGraph(egr, thresholdForOptimizing);
		logger.info("Built collapsed graph.");		
		confidenceCalculator.computeCategoryConfidences(graph);
		logger.info("Computed category confidences and added them to graph.");		

		return graph;
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
	 * This creates, for each email in the dataset, an entailment graph (collapsed)
	 * from the remaining emails in the set
	 */
	public void generateEntailmentGraphsForEvaluation() {
		//1. read in all emails
		
		//for each email
			//2. create entailment graph from remaining emails
			//3. store graph in resources
	}
}

class ValueComparator implements Comparator<String> {

    Map<String, Double> base;
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } 
    }
}

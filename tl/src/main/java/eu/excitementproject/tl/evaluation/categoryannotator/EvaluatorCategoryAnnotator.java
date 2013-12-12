package eu.excitementproject.tl.evaluation.categoryannotator;

import java.io.File;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.transform.TransformerException;

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
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
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
import eu.excitementproject.tl.composition.graphoptimizer.GlobalGraphOptimizer;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLuceneSimple;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.TokenAsFragmentAnnotatorForGerman;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.NoEDA;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;
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
    static long startTime = System.currentTimeMillis();
    static long endTime = 0;
    
    static CachedLAPAccess lapForDecisions;
    static CachedLAPAccess lapForFragments;
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
    
    static boolean readGraphFromFile = true;
    
    static boolean trainEDA = false;
    static boolean processTrainingData = false;
    
    static String scoreCombination = "sum"; //how to combine the scores for different fragments to a final score for the interaction
    
	public static void main(String[] args) {
		String inputFoldername = "./src/test/resources/WP2_public_data_XML/OMQ/"; //dataset to be evaluated
		String outputGraphFoldername = "./src/test/resources/sample_graphs/"; //output directory (for generated entailment graph)
			
		EvaluatorCategoryAnnotator eca = new EvaluatorCategoryAnnotator();
		
		try {
			eca.runEvaluationThreeFoldCross(inputFoldername, outputGraphFoldername);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	EvaluatorCategoryAnnotator(int setup) {
		setup(setup);		
	}

	EvaluatorCategoryAnnotator() {
		setup(1);		
	}
	
	/**
	 * Pick a specific evaluation setup.
	 * 
	 * @param i
	 */
	private void setup(int i) {
		try {
			switch(i){
	        	case 1:
	        		lapForDecisions = new CachedLAPAccess(new LemmaLevelLapDE());//MaltParserDE();
	        		lapForFragments = new CachedLAPAccess(new LemmaLevelLapDE()); //lap = new MaltParserDE();
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml";
	        		File configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new NoEDA();
		    		fragmentAnnotatorForGraphBuilding = new TokenAsFragmentAnnotatorForGerman(lapForFragments);
		    		fragmentAnnotatorForNewInput = new TokenAsFragmentAnnotatorForGerman(lapForFragments);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new AutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphOptimizer = new GlobalGraphOptimizer();
		    		boolean tfidf = true;
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(tfidf);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
			}
		} catch (FragmentAnnotatorException | ModifierAnnotatorException | ConfigurationException e) {
			e.printStackTrace();
		} catch (GraphMergerException e) {
			e.printStackTrace();
		} catch (LAPException e) {
			e.printStackTrace();
		} 			
	}

	public double runEvaluationOnTrainTestDataset(String inputFilename, String outputDirname, String configFilename) {
		
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
			
			setup(1);
			eda.initialize(config);
			logger.info("Initialized config.");
			use1 = new UseCaseOneRunnerPrototype(lapForFragments, eda, outputDirname);
			double threshold = 0.99;
			EntailmentGraphCollapsed graph = use1.buildCollapsedGraph(docsTrain, threshold);
			logger.info("Built collapsed graph.");
			confidenceCalculator.computeCategoryConfidences(graph);
			String outputFile = outputDirname + "/test.graph.xml";
			XMLFileWriter.write(graph.toXML(), outputFile);			
			graph = new EntailmentGraphCollapsed(new File(outputFile));
			//GraphViewer.drawGraph(graph);

			logger.info("Computed and added category confidences.");

			// Send each email in test data + graph to node use case 2 and have it annotated
			int countPositive = 0;
			for (Interaction doc : docsTest) {
				JCas cas;
				cas = doc.createAndFillInputCAS();
				use2 = new UseCaseTwoRunnerPrototype(lapForFragments, eda);
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
	private int compareDecisionsForInteraction(int countPositive,
			Interaction doc, Set<CategoryDecision> decisions, String mostProbableCat) {
		String bestCat = "";
		bestCat = computeBestCat(decisions, mostProbableCat);
		logger.info("Correct category: " + doc.getCategory());
		if (doc.getCategory().equals(bestCat)) {
			countPositive++;
		} 
		return countPositive;
	}

	/**
	 * Computes the best category given the set of category decisions. 
	 * 
	 * @param doc
	 * @param decisions
	 * @param mostProbableCat
	 * @return
	 */
	private String computeBestCat(Set<CategoryDecision> decisions, String mostProbableCat) {
		logger.debug("Computing best category");
		logger.debug("Number of decisions: " + decisions.size());
		String bestCat;
		HashMap<String,Double> categoryScores = new HashMap<String,Double>();
		if (scoreCombination.equals("sum")) {
			for (CategoryDecision decision: decisions) {
				String category = decision.getCategoryId();
				double sum = 0; //the sum of scores for a particular category 
				if (categoryScores.containsKey(category)) {
					sum = categoryScores.get(category);
				}
				//add up all scores for each category on the CAS
				sum += decision.getConfidence();
				categoryScores.put(category, sum);
			}
		}
		// get the category with the highest value
		ValueComparator bvc =  new ValueComparator(categoryScores);
		Map<String,Double> sortedMap = new TreeMap<String,Double>(bvc);
		sortedMap.putAll(categoryScores);
		logger.debug("category sums:  " + sortedMap);
		if (sortedMap.size() > 0) {
			bestCat = sortedMap.keySet().iterator().next().toString();
			logger.info("Best category: " + bestCat);
		} else bestCat = mostProbableCat;
		return bestCat;
	}
	
	/**
	 * Runs three-fold cross-validation on the files found in the input directory. This directory must contain
	 * exactly three email files (ending with "_emails.xml" plus exactly one TH pair file for each of these email
	 * files (same file but ending with "_th.xml"). 
	 * 
	 * For each fold, this method uses one of these email files for testing. 
	 * 
	 * If trainEDA is set to true, it uses one of the remaining interaction files for building the entailment graph
	 * and the TH pair file associated to the other one for training the EDA. 
	 * 
	 * If trainEDA is set to false, it uses both remaining email files for building the entailment graph. 
	 * 
	 * @param inputDataFoldername
	 * @param outputGraphFilename
	 * @throws Exception
	 */
	public void runEvaluationThreeFoldCross(String inputDataFoldername, String outputGraphFoldername) throws Exception {
		Map<Integer, File> fileIndex = indexFilesinDirectory(inputDataFoldername);	    	
	    	
	    //check if there are enough files in the dir
	    double numberOfFolds = fileIndex.size()/2;
	    if (processTrainingData && numberOfFolds != 3) { //TODO: elaborate this check (is the type of file correct: three interaction and three TH pair files)
    		logger.warn("Please specify a folder with three email and three T/H pair files (for EDA training + graph building + testing)!");
    		return;
	    } else {	     
	    	logger.info("Creating " + numberOfFolds + " folds.");
	    }
	    
	   	Map<Integer, Double> foldAccuracies = new HashMap<Integer, Double>();

	   	Set<Interaction> graphDocs = new HashSet<Interaction>();
		Set<Interaction> testDocs = new HashSet<Interaction>();
		String edaTrainingFilename;
		
	    for (int i=1; i<=3; i++) { //Create a fold for each of the three input files
		    logger.info("Creating fold " + i);
			int j=i+1;
			if (j>3)j-=3; 
    		int k=j+1;
    		if (k>3)k-=3;
	    	edaTrainingFilename = "";
	    	graphDocs.clear();
	    	testDocs.clear();
	    	
	    	//Add test documents
			File testFile = new File(inputDataFoldername + "omq_public_"+i+"_emails.xml"); //TODO: replace?
			logger.info("Reading test file " + testFile.getName());	    			
			testDocs.addAll(InteractionReader.readInteractionXML(testFile));
			logger.info("Test set of fold "+i+" now contains " + testDocs.size() + " documents");
        	
			//For each fold, read entailment graph EG or generate it from training set
	    	EntailmentGraphCollapsed graph;
    		File graphFile = new File(outputGraphFoldername + "omq_public_"+i+"_graph.xml");
    		String mostProbableCat;
	    	if (readGraphFromFile) { // read graph
	    		logger.info("Reading graph from " + graphFile.getAbsolutePath());
	    		graph = new EntailmentGraphCollapsed(graphFile);
				mostProbableCat = computeMostFrequentCategory(graph);
	    	} else { // build graph
	    		String graphDocumentsFilename = inputDataFoldername + "omq_public_"+j+"_emails.xml";
				logger.info("Reading documents for graph building from " + graphDocumentsFilename);	    			
				graphDocs.addAll(InteractionReader.readInteractionXML(new File(graphDocumentsFilename)));
				logger.info("Graph set of fold "+i+" now contains " + graphDocs.size() + " documents");
				if (trainEDA) { // train EDA
					if (processTrainingData) { //process training data
						edaTrainingFilename = inputDataFoldername + "omq_public_"+k+"_th.xml";
						logger.info("Setting EDA training file " + edaTrainingFilename);	    
						File trainingFile = new File(edaTrainingFilename); //training input file
						File outputDir = new File("./target/DE/dev/"); // output dir as written in configuration!
						if (!outputDir.exists()) outputDir.mkdirs();
						logger.info("Reading " + trainingFile.getCanonicalPath());
						lapForDecisions.processRawInputFormat(trainingFile, outputDir); //process training data and store output
						logger.info("Processing training data."); 
					} // training data already exists
					eda.startTraining(config); //train EDA (may take a some time)
					logger.info("Training completed."); 
				} else { //add documents to graph creation set
					String secondGraphFilename = inputDataFoldername + "omq_public_"+k+"_emails.xml";
	    			logger.info("Reading second graph file " + secondGraphFilename);	    			
	    			graphDocs.addAll(InteractionReader.readInteractionXML(new File(graphDocumentsFilename)));		  
				}
				logger.info("Graph set of fold "+i+" now contains " + graphDocs.size() + " documents");

				mostProbableCat = computeMostFrequentCategory(graphDocs);

				//graphDocs = reduceTrainingDataSize(graphDocs, 20); //reduce the number of emails on which the graph is built
				logger.info("Reduced training set contains " +graphDocs.size()+ " documents.");
				logger.info("Building graph..."); 
		    	graph = new EntailmentGraphCollapsed();
//				graph = buildGraph(graphDocs);
				logger.info("Writing graph to " + graphFile.getAbsolutePath()); 
				XMLFileWriter.write(graph.toXML(), graphFile.getAbsolutePath());			
	    	}


	    	boolean skipEval = false;
	    	if (!skipEval) {
		    	//For each email E in the test set, send it to nodematcher / category annotator and have it annotated
				int countPositive = 0;
				for (Interaction interaction : testDocs) {
					logger.info("annotating interaction " + interaction.getInteractionId());
					logger.info("interaction text: " + interaction.getInteractionString());
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
	}

	private String computeMostFrequentCategory(EntailmentGraphCollapsed graph) {
		int numberOfTextualInputs = graph.getNumberOfTextualInputs();
		logger.info("num of textual inputs: " + numberOfTextualInputs);
		Map<String, Double> categoryOccurrences = new HashMap<String,Double>();
		Set<String> processedInteractions = new HashSet<String>();
		for (EquivalenceClass ec : graph.vertexSet()) {
			for (EntailmentUnit eu : ec.getEntailmentUnits()) {
				for (EntailmentUnitMention eum : eu.getMentions()) {
					String interactionId = eum.getInteractionId();
					if (!processedInteractions.contains(interactionId)) {
						String cat = eum.getCategoryId();					
						double occ = 1;
						if (categoryOccurrences.containsKey(cat)) {
							occ += categoryOccurrences.get(cat);
						}
						categoryOccurrences.put(cat, occ);
						processedInteractions.add(interactionId);
					}
				}
			}
		}
		for (String cat : categoryOccurrences.keySet()) System.out.println("cat: " + cat);		
		ValueComparator bvc =  new ValueComparator(categoryOccurrences);
		Map<String,Double> sortedMap = new TreeMap<String,Double>(bvc);
		sortedMap.putAll(categoryOccurrences);
		for (String cat : sortedMap.keySet()) System.out.println("cat: " + cat + " / val: " + categoryOccurrences.get(cat));		
System.exit(1);
		logger.debug("category sums:  " + sortedMap);
		String mostFrequentCat = "N/A";
		if (sortedMap.size() > 0) {
			mostFrequentCat = sortedMap.keySet().iterator().next().toString();
			logger.info("Most probably category: " + mostFrequentCat);
			logger.info("Occurs " + categoryOccurrences.get(mostFrequentCat) + " times");
			logger.info("Number of processed interactions " + processedInteractions.size());
			logger.info("Baseline: " + (double) categoryOccurrences.get(mostFrequentCat)/ (double) processedInteractions.size());
		}
		
//		System.exit(0);
		return mostFrequentCat;

	}

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
	private void printResult(double numberOfFolds,
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
	private JCas annotateInteraction(EntailmentGraphCollapsed graph,
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
		
		if (fragmentGraphs.size() == 0) {
			System.exit(0);
		}

		//call node matcher on each fragment graph
		nodeMatcher = new NodeMatcherLuceneSimple(graph, "./src/test/resources/Lucene_index/", new StandardAnalyzer(Version.LUCENE_44));
		nodeMatcher.indexGraphNodes();
		nodeMatcher.initializeSearch();
		for (FragmentGraph fragmentGraph: fragmentGraphs) {
			logger.info("fragment graph: " + fragmentGraph.getCompleteStatement());
			Set<NodeMatch> matches = nodeMatcher.findMatchingNodesInGraph(fragmentGraph);
			logger.info("matches: " + matches.size());
			//add category annotation to CAS
			categoryAnnotator.addCategoryAnnotation(cas, matches);
		}
		return cas;
	}

	/**
	 * Compute most frequent category in training set.
	 * 
	 * @param trainingDocs
	 * @return
	 */
	private String computeMostFrequentCategory(
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
		String mostFrequentCat = "N/A";
		if (sortedMap.size() > 0) {
			mostFrequentCat = sortedMap.keySet().iterator().next().toString();
			logger.info("Most probably category: " + mostFrequentCat);
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

	/**
	 * Build collapsed graph from interactions, including category information.
	 * 
	 * @param trainingDocs
	 * @return
	 * @throws Exception 
	 */
	private EntailmentGraphCollapsed buildGraph(Set<Interaction> trainingDocs) throws Exception {
		startTime = endTime;
		endTime   = System.currentTimeMillis();
		logger.info((endTime - startTime));		
		JCas cas = CASUtils.createNewInputCas();
		EntailmentGraphRaw egr = new EntailmentGraphRaw();
		eda.initialize(config);
		logger.info("Initialized config.");
		int count = 1;
		
		//build fragment graphs from input data
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();	
		for(Interaction i: trainingDocs) {
			logger.info("relevantText: " + i.getRelevantText());
			if (i.getRelevantText() == null) System.exit(1);
			i.fillInputCAS(cas); 
			fragmentAnnotatorForGraphBuilding.annotateFragments(cas);
			modifierAnnotator.annotateModifiers(cas);
			logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
			fgs.addAll(fragmentGraphGenerator.generateFragmentGraphs(cas));
		}
		logger.info("Built fragment graphs: " +fgs.size()+ " graphs.");

		
		//merge one doc after the other --> takes ages with EOP EDA!!
		/*
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();
		for(Interaction i: trainingDocs) {
			logger.info("Processing interaction " + count + " of " + trainingDocs.size());
			logger.info("Interaction text:  " + i.getInteractionString());
			count++;
			i.fillInputCAS(cas); 
			fragmentAnnotatorForGraphBuilding.annotateFragments(cas);
			modifierAnnotator.annotateModifiers(cas);
			logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
			fgs = fragmentGraphGenerator.generateFragmentGraphs(cas);
			logger.info("Adding " + fgs.size() + " fragment graphs");
			egr = graphMerger.mergeGraphs(fgs, egr);
			logger.info("Merged graph now has " + egr.vertexSet().size() + " nodes");
			startTime = endTime;
			endTime   = System.currentTimeMillis();
			logger.info("Merging took " + ((double)(endTime - startTime)/60000) + " minutes");		
		}*/
		
		
		//merge graph --> takes a really long time!
		/*
		eda.initialize(config);
		egr = graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());
		logger.info("Merged graph: " +egr.vertexSet().size()+ " nodes");
		*/
		
		
		//merge graph - baseline
		count = 0; 
		for (FragmentGraph fg : fgs) {
			count++;
			logger.info("Adding fragment graph " +count+ " out of " + fgs.size());
			for (EntailmentUnitMention eum : fg.vertexSet()) {
				egr.addEntailmentUnitMention(eum, fg.getCompleteStatement().getTextWithoutDoubleSpaces());					
			}
		}			
		logger.info("Merged graph: " +egr.vertexSet().size()+ " nodes");

		
		startTime = endTime;
		endTime   = System.currentTimeMillis();
		logger.info((endTime - startTime));		
		logger.info("Merged all fragment graphs");
		
		logger.info(egr.toStringDetailed());
		//System.exit(0);
				
		
		//EntailmentGraphRaw egr = graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());
		
		logger.info("Merged graphs.");
		startTime = endTime;
		endTime   = System.currentTimeMillis();
		logger.info((endTime - startTime));		
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
	 * This evaluation simulates the real use case. Can be used to find out what's the best graph size
	 * (adding new nodes doesn't improve the result)
	 */
	public void runIncrementalEvaluation() {
		//init map<int,int> positivesPerCount
		//for each run (maybe 10, 100,...)
			//order emails randomly
			//create empty graph
			//init count
			//for each email
				//compute category based on graph
				//compare category to real category for email
				//result = positivesPerCount.get(count)
				//if match: result++
				//positivesPerCount.put(count, result)
				//add email with category info to graph
				//count++
		//compute overall result (divide positivesPerCount by number of runs)
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
        if (base.get(a) > base.get(b)) {
            return -1;
    	} else {
            return 1;
        } 
    }
}

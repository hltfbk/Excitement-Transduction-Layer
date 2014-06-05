package eu.excitementproject.tl.evaluation.categoryannotator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import de.tuebingen.uni.sfs.germanet.api.LexUnit;
import eu.excitement.type.tl.CategoryAnnotation;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
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
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
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
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.TokenAsFragmentAnnotatorForGerman;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.laputils.CategoryReader;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;
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
	
	static Logger logger = Logger.getLogger(EvaluatorCategoryAnnotator.class); 
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
    static NodeMatcher nodeMatcher;
    static NodeMatcherWithIndex nodeMatcherWithIndex;
	static CategoryAnnotator categoryAnnotator;
	static ConfidenceCalculator confidenceCalculator;
    static File configFile;
	static GermaNet germanet;
	static Set<String> GermaNetLexicon; 
    
	//CHOOSE CONFIGURATION:
	
	static double thresholdForOptimizing = 0.8;
	static int minTokenOccurrence = 5;
	static int minTokenOccurrenceInCategories = 2;
    
	static boolean tfidf = true;
    static boolean LuceneSearch = false;
    
    static int setup = 1;
    
    static boolean readGraphFromFile = false;
    static boolean readMergedGraphFromFile = false;
    
    static boolean trainEDA = false;
    static boolean processTrainingData = false;

    static boolean relevantTextProvided = true;
    
//    static String scoreCombination = "sum"; //how to combine the scores for different fragments to a final score for the interaction    
    static String scoreCombination = "vsm"; //how to combine the scores for different fragments to a final score for the interaction    

    static PrintWriter writer; 
    	
	public static void main(String[] args) {
		
		String inputFoldername = "src/test/resources/WP2_public_data_XML/OMQ/"; //dataset to be evaluated
		String outputGraphFoldername = "src/test/resources/sample_graphs/"; //output directory (for generated entailment graph)
		String categoriesFilename = inputFoldername + "omq_public_categories.xml"; 
		
		/*
		try {
			GermaNetLexicon = getGermaNetLexicon();
			logger.info("GermaNet lexicon contains " + GermaNetLexicon.size() + " entries");
		} catch (LAPException | FragmentAnnotatorException | XMLStreamException | IOException e1) {
			e1.printStackTrace();
		}	
	*/	
		EvaluatorCategoryAnnotator eca = new EvaluatorCategoryAnnotator(setup);
		
		try {
			//eca.runEvaluationThreeFoldCross(inputFoldername, outputGraphFoldername, categoriesFilename);
			eca.runIncrementalEvaluation(inputFoldername, outputGraphFoldername, categoriesFilename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		writer.close();
	}

	EvaluatorCategoryAnnotator(int setup) {
		setup(setup);		
		try {
			writer = new PrintWriter("C:/Temp/debugging"+System.currentTimeMillis()+".txt", "UTF-8");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	EvaluatorCategoryAnnotator() {
		setup(1);		
		try {
			writer = new PrintWriter("C:/Temp/debugging"+System.currentTimeMillis()+".txt", "UTF-8");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * Pick a specific evaluation setup.
	 * 
	 * @param i
	 */
	private void setup(int i) {
		try {
			switch(i){
	        	case 0: //simple tfidf (no EDA use --> graph with non-connected nodes)
	        		lapForDecisions = new CachedLAPAccess(new LemmaLevelLapDE());//MaltParserDE();
	        		lapForFragments = new CachedLAPAccess(new LemmaLevelLapDE()); //lap = new MaltParserDE();
	        		fragmentAnnotatorForGraphBuilding = new TokenAsFragmentAnnotatorForGerman(lapForFragments);
		    		fragmentAnnotatorForNewInput = new TokenAsFragmentAnnotatorForGerman(lapForFragments);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(tfidf);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 1: //TIE with base configuration (inflection only)
	        		lapForDecisions = new CachedLAPAccess(new LemmaLevelLapDE());//MaltParserDE();
	        		lapForFragments = new CachedLAPAccess(new LemmaLevelLapDE()); //lap = new MaltParserDE();
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
		    		fragmentAnnotatorForGraphBuilding = new TokenAsFragmentAnnotatorForGerman(lapForFragments);
		    		fragmentAnnotatorForNewInput = new TokenAsFragmentAnnotatorForGerman(lapForFragments);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new AutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(tfidf);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 2: //TIE with base configuration + GermaNet 
	        		lapForDecisions = new CachedLAPAccess(new LemmaLevelLapDE());//MaltParserDE();
	        		lapForFragments = new CachedLAPAccess(new LemmaLevelLapDE()); //lap = new MaltParserDE();
	    			configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+GN_DE.xml";;
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
		    		fragmentAnnotatorForGraphBuilding = new TokenAsFragmentAnnotatorForGerman(lapForFragments);
		    		fragmentAnnotatorForNewInput = new TokenAsFragmentAnnotatorForGerman(lapForFragments);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new AutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphOptimizer = new SimpleGraphOptimizer();// new GlobalGraphOptimizer(); --> don't use
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(tfidf);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
	        	case 99: //TIE with base configuration (inflection only) and sentences as fragments
	        		lapForDecisions = new CachedLAPAccess(new LemmaLevelLapDE());//MaltParserDE();
	        		lapForFragments = new CachedLAPAccess(new LemmaLevelLapDE()); //lap = new MaltParserDE();
	        		configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml";
	        		configFile = new File(configFilename);
	        		config = new ImplCommonConfig(configFile);
	        		eda = new MaxEntClassificationEDA();
		    		fragmentAnnotatorForGraphBuilding = new SentenceAsFragmentAnnotator(lapForFragments);
		    		fragmentAnnotatorForNewInput = new SentenceAsFragmentAnnotator(lapForFragments);
		    		modifierAnnotator = new AdvAsModifierAnnotator(lapForFragments); 		
		    		fragmentGraphGenerator = new FragmentGraphLiteGeneratorFromCAS();
		    		graphMerger =  new AutomateWP2ProcedureGraphMerger(lapForDecisions, eda);
		    		graphOptimizer = new SimpleGraphOptimizer(); //new GlobalGraphOptimizer(); --> don't use!
		    		confidenceCalculator = new ConfidenceCalculatorCategoricalFrequencyDistribution(tfidf);
		    		categoryAnnotator = new CategoryAnnotatorAllCats();
		    		break;
			}
		} catch (FragmentAnnotatorException | ModifierAnnotatorException | ConfigurationException e) {
			e.printStackTrace();
		} catch (GraphMergerException e) {
			e.printStackTrace();
		} catch (LAPException e) {
			e.printStackTrace();
		} 			
	}

	/**
	 * 
	 * @param inputFilename
	 * @param outputDirname
	 * @param configFilename
	 * @return
	 * @throws IOException 
	 */
	public double runEvaluationOnTrainTestDataset(String inputFilename, String outputDirname, String configFilename) throws IOException {
		
		@SuppressWarnings("unused")
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
			
			setup(99);
			eda.initialize(config);
			logger.info("Initialized config.");
			logger.info("LAP: " + lapForFragments.getComponentName());

			double threshold = 0.99;
			
			use1 = new UseCaseOneRunnerPrototype(lapForFragments, eda, 
		    		fragmentAnnotatorForGraphBuilding, modifierAnnotator, 
		    		fragmentGraphGenerator, graphMerger, graphOptimizer);
			EntailmentGraphCollapsed graph = use1.buildCollapsedGraph(docsTrain, threshold);
			logger.info("Built collapsed graph.");
			
			confidenceCalculator.computeCategoryConfidences(graph);
			String outputFile = outputDirname + "/test.graph.xml";
			XMLFileWriter.write(graph.toXML(), outputFile);			
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
			EntailmentGraphRaw egr = graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());
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
				modifierAnnotator.annotateModifiers(cas);
				//logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
				Set<FragmentGraph> fragmentGraphs = fragmentGraphGenerator.generateFragmentGraphs(cas);
				if (null != fragmentGraphs) {
					logger.info("Number of fragment graphs: " + fragmentGraphs.size());
					Set<NodeMatch> matches = getMatches(graph, fragmentGraphs);	
					//add category annotation to CAS
					categoryAnnotator.addCategoryAnnotation(cas, matches);
					logger.info("_________________________________________________________");
					Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
					logger.info("Found " + decisions.size() + " decisions in CAS for interaction " + doc.getInteractionId());
					CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
					
					countPositive = compareDecisionsForInteraction(countPositive,
							doc, decisions, graph, matches);				
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
			Interaction doc, Set<CategoryDecision> decisions, EntailmentGraphCollapsed graph, Set<NodeMatch> matches) {
		return compareDecisionsForInteraction(countPositive, doc, decisions, "N/A", graph, matches);
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
			Interaction doc, Set<CategoryDecision> decisions, String mostProbableCat, 
			EntailmentGraphCollapsed graph, Set<NodeMatch> matches) {
		String bestCat = "";
		logger.info("Number of decisions for interaction "+doc.getInteractionId()+": " + decisions.size());
		bestCat = computeBestCat(decisions, mostProbableCat, doc.getCategories(), graph, matches);
		logger.info("Correct category: " + doc.getCategoryString());
		logger.info("Best category: " + bestCat);
		if ((new HashSet<String>(Arrays.asList(doc.getCategories())).contains(bestCat))) { //adapted to deal with multiple categories
			countPositive++;
		} 
		return countPositive;
	}

	/**
	 * Computes the best category given the set of category decisions. 
	 * 
	 * In the "sum" implementation, we simply add up all confidences for a particular category, 
	 * and pick the category with the highest overall score to be the best. 
	 * 
	 * In the "vsm" implementation we combine the scores based on the vector space model. 
	 * 
	 * @param doc
	 * @param decisions
	 * @param mostProbableCat
	 * @return
	 */
	private String computeBestCat(Set<CategoryDecision> decisions, String mostProbableCat, 
			String[] correctCats, EntailmentGraphCollapsed graph, Set<NodeMatch> matches) {
		logger.debug("Computing best category");
		logger.debug("Number of decisions: " + decisions.size());
		String bestCat;
		HashMap<String,Float> categoryScores = new HashMap<String,Float>();
		if (scoreCombination.equals("sum")) {
			for (CategoryDecision decision: decisions) {
				String category = decision.getCategoryId();
				float sum = 0; //the sum of scores for a particular category 
				if (categoryScores.containsKey(category)) {
					sum = categoryScores.get(category);
				}
				//add up all scores for each category on the CAS
				sum += decision.getConfidence();
				categoryScores.put(category, sum);
			}
		} else if (scoreCombination.equals("vsm")) { //vector space model
			//collect mention tf in query
			HashMap<String,Integer> tfQueryMap = new HashMap<String,Integer>();
			int count = 0;
			for (NodeMatch match : matches) { //for each matching mention
				String mentionText = match.getMention().getTextWithoutDoubleSpaces();
				count = 1;
				if (tfQueryMap.containsKey(mentionText)) {
					count += tfQueryMap.get(mentionText); 
				}
				tfQueryMap.put(mentionText, count);
			}
			logger.debug("Collected tf for queries " + tfQueryMap.size());
			
			writer.println("Collected tf for queries:");
			for (String query : tfQueryMap.keySet()) {
				writer.println(query + " : " + tfQueryMap.get(query));
			}
			
			HashMap<String,Double[]> categoryCosine = new HashMap<String,Double[]>();
			double N = graph.getNumberOfCategories(); //overall number of categories
			double sumQ2 = 0.0;
			
			for (NodeMatch match : matches) { //for each matching mention				

				//get best-matching node for this mention
				double maxScore = 0;
				EquivalenceClass bestNode = null;
				for (PerNodeScore perNodeScore : match.getScores()) {
					double score = perNodeScore.getScore();
					if (maxScore < score) {
						maxScore = score;
						bestNode = perNodeScore.getNode();
					}
				}
				
				writer.println("bestNode for match "+match.getMention().getTextWithoutDoubleSpaces()
						+": " + bestNode.getLabel());
				
				//retrieve tfidf for "document" (category)
				Map<String,Double> tfidfScoresForCategories = bestNode.getCategoryConfidences();
				
				//compute tfidf for query
				double n = bestNode.getCategoryConfidences().size();
				double idfForQuery = Math.log(N/n);
				writer.println("number of category confidences on best node: " + n);
				writer.println("idfForquery: " + idfForQuery);

				//compute sums for computing cosine similarity of the query to each of the categories
				Double[] cosineValues;
				double tfForQuery = tfQueryMap.get(match.getMention().getTextWithoutDoubleSpaces());
				double Q = tfForQuery*idfForQuery;
				sumQ2 += Math.pow(Q, 2); //this part does not depend on the category!
				
				for (String category : bestNode.getCategoryConfidences().keySet()) { //for each category associated to this node
					cosineValues = new Double[2];
					double D = tfidfScoresForCategories.get(category);
					double sumQD = Q*D;
					double sumD2 = Math.pow(D, 2);
					
					if (categoryCosine.containsKey(category)) {
						sumQD += categoryCosine.get(category)[0];
						sumD2 += categoryCosine.get(category)[1];
					}
					cosineValues[0] = sumQD;
					cosineValues[1] = sumD2;
					categoryCosine.put(category, cosineValues);
				}
			}

			//annotate category confidences in CAS based on cosine similarity (per document, not per mention!)
			for (String category : categoryCosine.keySet()) { //for each matching EG node for this mention
				Double[] cosineValues = categoryCosine.get(category);
				writer.println("cosine values for category " + category + ": " + cosineValues[0] + ", " + cosineValues[1] + ", " + sumQ2);
				Double cosQD = cosineValues[0] / ((Math.sqrt(cosineValues[1]) * Math.sqrt(sumQ2)));
				categoryScores.put(category, cosQD.floatValue());					
				writer.println(category + " : " + cosQD.floatValue());
			}			
		}
		
		// get the category with the highest value
		ValueComparator bvc =  new ValueComparator(categoryScores);
		Map<String,Float> sortedMap = new TreeMap<String,Float>(bvc);
		sortedMap.putAll(categoryScores);
		logger.debug("category sums:  " + sortedMap);
		if (sortedMap.size() > 0) {
			bestCat = sortedMap.keySet().iterator().next().toString();
			logger.info("Best category: " + bestCat);
			writer.println("best category: " + bestCat + ", value: " + sortedMap.get(bestCat));
			Set<String> correctCategories = new HashSet<String>(Arrays.asList(correctCats));
			if (correctCategories.size() > 1) logger.warn("Contains more than one category!");
			for (String correctCat : correctCategories) {
				if (categoryScores.keySet().contains(correctCat)) {
					logger.info("Computed confidence for correct category ("+correctCat+"): " + categoryScores.get(correctCat));
					writer.println("Computed confidence for correct category ("+correctCat+"): " + categoryScores.get(correctCat));
				}
				else {
					logger.info("Computed confidence for correct category ("+correctCat+"): N/A");
					writer.println("Computed confidence for correct category ("+correctCat+"): N/A");
				}
			}
		} else bestCat = mostProbableCat;
		return bestCat;
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
	    
	   	Map<Integer, Double> foldAccuracies = new HashMap<Integer, Double>();

	   	List<Interaction> graphDocs = new ArrayList<Interaction>();
		List<Interaction> testDocs = new ArrayList<Interaction>();
		String edaTrainingFilename;
		
//	    for (int i=1; i<=numberOfFolds; i++) { //Create a fold for each of the three input files
	    for (int i=1; i<=1; i++) { //Create a fold for each of the three input files
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
    		File graphFile = new File(outputGraphFoldername + "omq_public_"+i+"_graph_"+setup + "_" + minTokenOccurrence + ".xml");
    		File mergedGraphFile = new File(outputGraphFoldername + "omq_public_"+i+"_merged_graph_"+setup + "_" + minTokenOccurrence + ".xml");
    		
    		String mostProbableCat;
	    	if (readGraphFromFile) { // read graph
	    		logger.info("Reading graph from " + graphFile.getAbsolutePath());
	    		graph = new EntailmentGraphCollapsed(graphFile);
				mostProbableCat = computeMostFrequentCategory(graph);
				logger.info("Most frequent category in graph: " + mostProbableCat);
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
				} else { //add documents to graph creation set --> don't, dataset will be too large for graph building!
					/*
					String secondGraphFilename = inputDataFoldername + "omq_public_"+k+"_emails.xml";
	    			logger.info("Reading second graph file " + secondGraphFilename);	    			
	    			graphDocs.addAll(InteractionReader.readInteractionXML(new File(graphDocumentsFilename)));	
	    			*/	  
				}
				logger.info("Graph set of fold "+i+" now contains " + graphDocs.size() + " documents");

	    		//Add category texts
	    		graphDocs.addAll(CategoryReader.readCategoryXML(new File(categoriesFilename)));
	    		logger.info("added " + graphDocs.size() + " categories");
	    		EntailmentGraphRaw egr = buildRawGraph(graphDocs, mergedGraphFile, new EntailmentGraphRaw(), minTokenOccurrenceInCategories);
	    		logger.info("Number of nodes in raw graph: " + egr.vertexSet().size());
	    		logger.info("Number of edges in raw graph: " + egr.edgeSet().size());
	    		graph = buildCollapsedGraphWithCategoryInfo(egr);
	    		logger.info("Number of nodes in collapsed graph: " + graph.vertexSet().size());
	    		logger.info("Number of edges in collapsed graph: " + graph.edgeSet().size());
	    		XMLFileWriter.write(graph.toXML(), graphFile.getAbsolutePath());			
	    		
				mostProbableCat = computeMostFrequentCategory(graphDocs);

				//graphDocs = reduceTrainingDataSize(graphDocs, 20); //reduce the number of emails on which the graph is built
				//logger.info("Reduced training set contains " +graphDocs.size()+ " documents.");
			
				logger.info("Building graph..."); 
		    	logger.info("Writing collapsed graph to " + graphFile.getAbsolutePath()); 
				XMLFileWriter.write(graph.toXML(), graphFile.getAbsolutePath());			
	    	}

			logger.info("Collapsed graph " + graphFile.getAbsolutePath() + " contains " + graph.vertexSet().size() + " nodes and " + graph.edgeSet().size() + " edges");
	    	
	    	//indexing graph nodes and initializing search
			if (LuceneSearch) {
				nodeMatcherWithIndex = new NodeMatcherLuceneSimple(graph, "./src/test/resources/Lucene_index/", new GermanAnalyzer(Version.LUCENE_44));
				nodeMatcherWithIndex.indexGraphNodes();
				nodeMatcherWithIndex.initializeSearch();
			} else {
				nodeMatcher = new NodeMatcherLongestOnly(graph);
			}
	    	boolean skipEval = false;
	    	if (!skipEval) {
		    	//For each email E in the test set, send it to nodematcher / category annotator and have it annotated
				int countPositive = 0;
				
				for (Interaction interaction : testDocs) {
					JCas cas = interaction.createAndFillInputCAS();
					fragmentAnnotatorForNewInput.annotateFragments(cas);
					modifierAnnotator.annotateModifiers(cas);
					//logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
					Set<FragmentGraph> fragmentGraphs = fragmentGraphGenerator.generateFragmentGraphs(cas);
					//logger.info("Number of fragment graphs: " + fragmentGraphs.size());
					Set<NodeMatch> matches = getMatches(graph, fragmentGraphs);	
					//add category annotation to CAS
					categoryAnnotator.addCategoryAnnotation(cas, matches);

					//print CAS category
					//CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
					
			    	//Compare automatic to manual annotation
					logger.info("annotating interaction " + interaction.getInteractionId());
			//		logger.info("interaction text: " + interaction.getInteractionString());
					Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
		
					countPositive = compareDecisionsForInteraction(countPositive,
							interaction, decisions, mostProbableCat, graph, matches);				
				}
		    	logger.info("Count positive: " + countPositive);
		    	double countTotal = countTotalNumberOfCategories(testDocs);
			    double accuracyInThisFold = ((double)countPositive / countTotal);
			    foldAccuracies.put(i, accuracyInThisFold);
		    }	
		    printResult(numberOfFolds, foldAccuracies);
	    }			
	}

	private double countTotalNumberOfCategories(List<Interaction> testDocs) {
		double count = 0;
		for (Interaction i : testDocs) {
			count += i.getCategories().length;
		}
		return count;
	}

	/**
	 * Find out what's the most frequent category stored in the graph.
	 * 
	 * @param graph
	 * @return most frequent category
	 */
	
	private String computeMostFrequentCategory(EntailmentGraphCollapsed graph) {
		int numberOfTextualInputs = graph.getNumberOfTextualInputs();
		logger.info("num of textual inputs: " + numberOfTextualInputs);
		Map<String, Float> categoryOccurrences = new HashMap<String,Float>();
		Set<String> processedInteractions = new HashSet<String>();
		for (EquivalenceClass ec : graph.vertexSet()) {
			for (EntailmentUnit eu : ec.getEntailmentUnits()) {
				for (EntailmentUnitMention eum : eu.getMentions()) {
					String interactionId = eum.getInteractionId();
					if (!processedInteractions.contains(interactionId)) {
						String cat = eum.getCategoryId();					
						float occ = 1;
						if (categoryOccurrences.containsKey(cat)) {
							occ += categoryOccurrences.get(cat);
						}
						categoryOccurrences.put(cat, occ);
						processedInteractions.add(interactionId);
					}
				}
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
			logger.info("Number of processed interactions " + processedInteractions.size());
			logger.info("Baseline: " + (double) categoryOccurrences.get(mostFrequentCat)/ (double) processedInteractions.size());
		}
		return mostFrequentCat;

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
	private Set<NodeMatch> getMatches(EntailmentGraphCollapsed graph,
			Set<FragmentGraph> fragmentGraphs) throws LAPException,
			FragmentAnnotatorException, ModifierAnnotatorException,
			FragmentGraphGeneratorException, NodeMatcherException,
			CategoryAnnotatorException {
		//call node matcher on each fragment graph
		Set<NodeMatch> matches = new HashSet<NodeMatch>();
		System.out.println("fragmentsGraphs: " + fragmentGraphs);
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
				nodeMatcher = new NodeMatcherLongestOnly(graph);
				matches.addAll(nodeMatcher.findMatchingNodesInGraph(fragmentGraph));
			}
			logger.info("matches: " + matches.size());
		}
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
			float occ = 1;
			Set<String> catsSet = new HashSet<String>(Arrays.asList(cats));
			for (String cat : catsSet) {
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

	/**
	 * Build collapsed graph from interactions, including category information.
	 * 
	 * @param graphDocs
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	private EntailmentGraphCollapsed buildGraph(List<Interaction> graphDocs, File mergedGraphFile, int minOccurrence) throws Exception {
		EntailmentGraphRaw egr = new EntailmentGraphRaw();
		if (readMergedGraphFromFile) {
			egr = new EntailmentGraphRaw(mergedGraphFile);
		} else { //build fragment graphs from input data and merge them
			buildRawGraph(graphDocs, mergedGraphFile, egr, minOccurrence);			
		}		
		EntailmentGraphCollapsed graph = buildCollapsedGraphWithCategoryInfo(egr);		
		return graph;
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

	private EntailmentGraphRaw buildRawGraph(List<Interaction> graphDocs,
			File mergedGraphFile, EntailmentGraphRaw egr, int minOccurrence)
			throws FragmentAnnotatorException, ModifierAnnotatorException,
			FragmentGraphGeneratorException, ConfigurationException,
			EDAException, ComponentException, GraphMergerException,
			LAPException, TransformerException, EntailmentGraphRawException {
		logger.info("Initialized config.");
		
		Set<FragmentGraph> fgs = buildFragmentGraphs(graphDocs);
		
		if (setup == 0) {
			int count = 0; 
			for (FragmentGraph fg : fgs) {
				count++;
				logger.info("Adding fragment graph " +count+ " out of " + fgs.size());
				for (EntailmentUnitMention eum : fg.vertexSet()) {
					egr.addEntailmentUnitMention(eum, fg.getCompleteStatement().getTextWithoutDoubleSpaces());					
				}
			}			
		} else { //merge graph --> takes a really long time and uses too much memory: TODO reduce number of fgs
			eda.initialize(config);
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
			egr = graphMerger.mergeGraphs(fgsReduced, new EntailmentGraphRaw());
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

	private Set<FragmentGraph> buildFragmentGraphs(List<Interaction> graphDocs) throws FragmentAnnotatorException,
			ModifierAnnotatorException, FragmentGraphGeneratorException {
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();	
		for(Interaction i: graphDocs) {
			List<JCas> cases;
			try {
				cases = i.createAndFillInputCASes(relevantTextProvided);
				for (int j=0; j<cases.size(); j++) {
					JCas cas = cases.get(j);
					fragmentAnnotatorForGraphBuilding.annotateFragments(cas);
					modifierAnnotator.annotateModifiers(cas);
					logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
					fgs.addAll(fragmentGraphGenerator.generateFragmentGraphs(cas));
				}			
			} catch (LAPException e) {
				// TODO Auto-generated catch block
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
		String documentsFilename = inputDataFoldername + "omq_public_1_emails.xml"; //TODO: replace 1 by "all" at some point
		JCas cas = CASUtils.createNewInputCas();
		String mostProbableCat; 
		int countPositive = 0;
		int run = 1;

		//Read documents for annotation, compute token occurrences (for filtering fragments to be merged)
		logger.info("Reading documents from " + documentsFilename);	    		
		List<Interaction> docs = new ArrayList<Interaction>();
		docs.addAll(InteractionReader.readInteractionXML(new File(documentsFilename)));
        Collections.sort(docs);
        
        /*
        for (Interaction i : docs) {
        	System.out.println(i.getInteractionId());
        }
        */
        
		Set<FragmentGraph> fgs = buildFragmentGraphs(docs);
		HashMap<String,Integer> tokenOccurrences = computeTokenOccurrences(fgs);

		//Initialize graph with category texts (assuming they are available beforehand)
		List<Interaction> graphDocs = new ArrayList<Interaction>(); 
		graphDocs.addAll(CategoryReader.readCategoryXML(new File(categoriesFilename)));
		logger.info("added " + graphDocs.size() + " categories");
		File mergedGraphFile = new File(outputGraphFoldername + "/incremental/omq_public_"+run+"_merged_graph_categories_"+setup + "_" + minTokenOccurrenceInCategories + ".xml");	
		EntailmentGraphRaw egr = buildRawGraph(graphDocs, mergedGraphFile, new EntailmentGraphRaw(), minTokenOccurrenceInCategories);
		logger.info("Number of nodes in raw graph: " + egr.vertexSet().size());
		logger.info("Number of edges in raw graph: " + egr.edgeSet().size());
		EntailmentGraphCollapsed egc = buildCollapsedGraphWithCategoryInfo(egr);
		logger.info("Number of nodes in collapsed graph: " + egc.vertexSet().size());
		logger.info("Number of edges in collapsed graph: " + egc.edgeSet().size());
		File graphFile = new File(outputGraphFoldername + "/incremental/omq_public_"+run+"_graph_categories_"+setup + "_" + minTokenOccurrenceInCategories + ".xml");
		XMLFileWriter.write(egc.toXML(), graphFile.getAbsolutePath());			

		//Iterate through interactions, annotate each using existing graph and then add interaction to graph 
		for (Interaction doc : docs) {	
			logger.info("Processing doument " + run + " out of " + docs.size());
			//if (run > 50) break; //TODO: Remove (debugging only)
			//Annotate document and compare to manual annotation
			logger.info("graph contains " + egc.vertexSet().size() + " nodes ");
			mostProbableCat = computeMostFrequentCategory(egc); //compute most frequent category in graph
	    	//indexing graph nodes and initializing search
			if (LuceneSearch) {
				nodeMatcherWithIndex = new NodeMatcherLuceneSimple(egc, "./src/test/resources/Lucene_index/incremental/", new GermanAnalyzer(Version.LUCENE_44));
				nodeMatcherWithIndex.indexGraphNodes();
				nodeMatcherWithIndex.initializeSearch();
			} else {
				nodeMatcher = new NodeMatcherLongestOnly(egc);
			}
			
			//Annotate interaction using graph
			logger.info("annotating interaction " + doc.getInteractionId());
			cas = doc.createAndFillInputCAS();
			fragmentAnnotatorForNewInput.annotateFragments(cas);
			modifierAnnotator.annotateModifiers(cas);
			//logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
			Set<FragmentGraph> fragmentGraphs = fragmentGraphGenerator.generateFragmentGraphs(cas);
			//logger.info("Number of fragment graphs: " + fragmentGraphs.size());
			writer.println(doc.getInteractionId() + " : ");
			writer.println(fragmentGraphs.size() + " fragment graphs ");
			Set<NodeMatch> matches = getMatches(egc, fragmentGraphs);	
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
			
			countPositive = compareDecisionsForInteraction(countPositive,
					doc, decisions, mostProbableCat, egc, matches);
			
			writer.println(doc.getInteractionId() + " : " + countPositive);
			
			double accuracy = (double) countPositive / (double) run;
			logger.info("Accuracy in run " + run + ": " + accuracy);
			accuracyPerRun.add(accuracy);
			run++;
			
			//Add document to existing graph
			if (setup == 0) {
				for (FragmentGraph fg : fragmentGraphs) { //just add the statement, no EDA call
					egr.addEntailmentUnitMention(fg.getCompleteStatement(), fg.getCompleteStatement().getTextWithoutDoubleSpaces());					
				}
			} else {
				for (FragmentGraph fg : fragmentGraphs) { //if token occurrence is above threshold, merge it into graph
					if (tokenOccurrences.get(fg.getCompleteStatement().getTextWithoutDoubleSpaces().toLowerCase()) > minTokenOccurrence) {
						logger.info("Merging " + fg.getCompleteStatement());
						egr = graphMerger.mergeGraphs(fg, egr); 
					} else { //if it's below the threshold, just add it
						logger.info("Adding " + fg.getCompleteStatement());
						egr.addEntailmentUnitMention(fg.getCompleteStatement(), fg.getCompleteStatement().getTextWithoutDoubleSpaces());					
					}
				}
			}
			egc = buildCollapsedGraphWithCategoryInfo(egr);			
			mergedGraphFile = new File(outputGraphFoldername + "/incremental/omq_public_"+run+"_merged_graph_"+setup + "_" + minTokenOccurrence +".xml");	
			graphFile = new File(outputGraphFoldername + "/incremental/omq_public_"+run+"_graph_"+setup + "_" + minTokenOccurrence +".xml");
			XMLFileWriter.write(egr.toXML(), mergedGraphFile.getAbsolutePath());			
			XMLFileWriter.write(egc.toXML(), graphFile.getAbsolutePath());			
		}
		for (int i=1; i<accuracyPerRun.size(); i++) {
			System.out.println(accuracyPerRun.get(i));
		}
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

	@SuppressWarnings("unused")
	private static Set<String> getGermaNetLexicon() throws LAPException, FragmentAnnotatorException, FileNotFoundException, XMLStreamException, IOException {
		// check token annotation is there or not 
		germanet = new GermaNet("D:/DFKI/EXCITEMENT/Linguistic Analysis/germanet-7.0/germanet-7.0/GN_V70/GN_V70_XML");
		Set<String> lexicon = new HashSet<String>(); 
		List<LexUnit> lexunits = germanet.getLexUnits();
		for (LexUnit lexunit : lexunits) {
			lexicon.addAll(lexunit.getOrthForms());
		}
		return lexicon;
	}			
}


class ValueComparator implements Comparator<String> {

    Map<String, Float> base;
    public ValueComparator(Map<String, Float> base) {
        this.base = base;
    }
    
    public int compare(String a, String b) {
        if (base.get(a) > base.get(b) && (Math.abs(base.get(a)-base.get(b))>0.01)) {
        	//check if difference between the values is large enough (otherwise, results differ in each run, due to rounding differences!)
            return -1;
        }
        if (base.get(a) < base.get(b) && (Math.abs(base.get(a)-base.get(b))>0.01)) {
        	return 1;
        }
        //if both have (nearly) the same value, decide based on key:
		if (Integer.parseInt(a) > Integer.parseInt(b)) {
			return -1;
		}
		if (Integer.parseInt(a) < Integer.parseInt(b)) {
	        return 1;
		}
		return 0;
    }
}

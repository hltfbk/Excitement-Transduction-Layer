package eu.excitementproject.tl.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitementproject.eop.alignmentedas.p1eda.P1EDATemplate;
import eu.excitementproject.eop.alignmentedas.p1eda.sandbox.FNR_DEvar1;
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
import eu.excitementproject.tl.composition.categoryannotator.CategoryAnnotatorAllCats;
import eu.excitementproject.tl.composition.confidencecalculator.ConfidenceCalculatorCategoricalFrequencyDistribution;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.ConfidenceCalculatorException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.composition.graphmerger.LegacyAutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLongestOnly;
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
import eu.excitementproject.tl.evaluation.categoryannotator.EvaluatorUtils;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.laputils.DependencyLevelLapDE;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.laputils.POSTag_DE;
import eu.excitementproject.tl.laputils.WordDecompositionType;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;

/**
 * This demo shows how to build and evaluate an entailment graph for use case 2. 
 *
 * For graph building, a three-step approach is used: 
 * 
 * 1) Build token fragments and merge them based on their lemma. 
 * 2) Build two-token-dependency-relation fragments and merge them using SimpleEDA (written by Aleksandra)
 * 3) Build sentence fragments (or keyword-based fragments, if keywords are available!) and merge them using 
 * the alignment-EDA with DerivBase as a resource
 * 
 * In a final step, the nodes of all three graphs are copied into the final graph. 
 * (OBS: The final graph contains bidirectional entailments only (encoded in the equivalence classes), 
 * but no directional edges, which seems to be sufficient for the requirements of use case 2. 
 * 
 * Evaluation and graph building is done block-wise (incrementally). The block size can be configured. 
 *  
 * @author Kathrin Eichler
 *
 */
public class DemoUseCase2OMQGermanNew {
	
	static CommonConfig config;
	static CachedLAPAccess lapLemma;
	static CachedLAPAccess lapDependency;
	static EDABasic<?> tie; 
	static EDABasic<?> seda; 
	static P1EDATemplate alignmenteda;
	static FragmentAnnotator fragAnotLemma;
	static FragmentAnnotator fragAnotDependency;
	static FragmentAnnotator fragAnotCombined;
	static FragmentAnnotator fragAnotSentence;
	static ModifierAnnotator modAnot;
	static FragmentGraphGenerator fragGen;
	static GraphMerger graphMerger;
	static GraphOptimizer graphOptimizer; 
	static ConfidenceCalculator cc; 
	static CategoryAnnotator catAnot;
	static int topN = 1;
//    static String pathToGermaNet = "D:/DFKI/EXCITEMENT/Linguistic Analysis/germanet-7.0/germanet-7.0/GN_V70/GN_V70_XML";
    static String pathToGermaNet = "C:/germanet/germanet-7.0/germanet-7.0/GN_V70/GN_V70_XML";
	static DerivBaseResource dbr;
	static int derivSteps;
	static GermaNetWrapper gnw;
	static List <GermaNetRelation> germaNetRelations; 
	static GermanWordSplitter splitter;
	static boolean mapNegation;
	static boolean addLemmaLabel;
	
	static boolean keywordsProvided = true; //if true: input dataset contains keyword metadata
	static boolean relevantTextProvided = false; //if true; input dataset contains relevantText annotation
	
	static String xmlDataFoldername = "src/test/resources/OMQ/test/";
	static String xmlDataFilename = "omq_public_1_emails.xml";
	static String xmlGraphFoldername = "src/test/resources/sample_graphs/";
	static String configFilenameAlignment = "./src/test/resources/EOP_models/omq/fnr_de_1.model";
	static String configFilenameTIE = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE_OMQ.xml";
	static File configFile;

	private final static Logger logger = Logger.getLogger(DemoUseCase2OMQGermanNew.class.getName());
	
	//determine POS tags to be used for fragment annotation
	 static List<POSTag_DE> tokenPosFilter = Arrays.asList(
	    		new POSTag_DE []{POSTag_DE.ADJA, POSTag_DE.ADJD, POSTag_DE.NE, POSTag_DE.NE, POSTag_DE.CARD,
	    						 POSTag_DE.VVFIN, POSTag_DE.VVINF, POSTag_DE.VVIZU, POSTag_DE.VVIMP, POSTag_DE.VVPP}); //"ADV" = adverb, "FM" = foreign language material
	 static List<POSTag_DE> governorPosFilter = Arrays.asList(
	    		new POSTag_DE []{POSTag_DE.ADJA, POSTag_DE.ADJD, POSTag_DE.NE, POSTag_DE.NN, POSTag_DE.CARD,
	    						 POSTag_DE.VVFIN, POSTag_DE.VVINF, POSTag_DE.VVIZU, POSTag_DE.VVIMP, POSTag_DE.VVPP, POSTag_DE.PTKNEG, POSTag_DE.PTKVZ}); //"ADV" = adverb, "FM" = foreign language material
	static List<POSTag_DE> dependentPosFilter = governorPosFilter;
	private static List<String> governorWordFilter = Arrays.asList(new String []{"ohne"});
	private static List<String> dependentWordFilter = Arrays.asList(new String []{"keine", "keinerlei", "nicht", "nichts"});

    //some configurations relating to evaluation
    static String method = "tfidf";
	static boolean bestNodeOnly = true; 
	static char documentFrequencyQuery = 'd';
	static char termFrequencyQuery = 'n';
	
	static boolean lengthBoost = true;
	
	static double confidenceThresholdAlignment = 0.9;
	static double confidenceThresholdTIE = 0.9;
	static double confidenceThresholdSEDA = 0.9;

	public static void main(String[] args) {
		
		/** Step 0: Initialization */
		try {
			configureSetup();
		} catch (ConfigurationException
				| FragmentAnnotatorException | ModifierAnnotatorException | EDAException | ComponentException e) {
			e.printStackTrace();
			System.exit(1);
		}
				
		/** Step 1: Read in data from XML file */
		String[] files = {xmlDataFoldername+xmlDataFilename,};
		File f;
		List<Interaction> docs = new ArrayList<Interaction>();
		for (String name: files) {
			f = new File(name);
			try {
				docs.addAll(InteractionReader.readInteractionXML(f));
			} catch (DataReaderException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

		/** Steps 2&3: Graph building and category annotation */
		EntailmentGraphRaw singleTokenGraph = new EntailmentGraphRaw(addLemmaLabel);
		EntailmentGraphRaw twoTokenGraph = new EntailmentGraphRaw(addLemmaLabel);
		EntailmentGraphRaw sentenceGraph = new EntailmentGraphRaw();
		EntailmentGraphCollapsed singleTokenCollapsedGraph = new EntailmentGraphCollapsed();
		EntailmentGraphCollapsed twoTokenCollapsedGraph = new EntailmentGraphCollapsed();
		EntailmentGraphCollapsed sentenceCollapsedGraph = new EntailmentGraphCollapsed();
		EntailmentGraphCollapsed finalCollapsedGraph = new EntailmentGraphCollapsed();
		int blockSize = 20; 
		JCas cas = null;
		try {
			cas = CASUtils.createNewInputCas();
		} catch (LAPException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		logger.info("Number of documents: " + docs.size());
		int countFrags = 0;
		int countDocs = 0;
		int countBlocks = 0;
		Map<Integer,Double> accuracyPerBlock = new HashMap<Integer,Double>();

		for (int i=0; i<docs.size(); i+=blockSize) { //for each block
			countBlocks++;
			/** Step 2: Annotate documents in current block based on existing graph (starting with second block) */
			if (countBlocks>1) {
				int countPositive = 0;
				int countProcessed = 0;
				String mostProbableCat = EvaluatorUtils.computeMostFrequentCategory(finalCollapsedGraph);
				for (int j=i; j<(countBlocks*blockSize) && j<docs.size(); j++) { //for each interaction in the current block
					countDocs++;
					countProcessed++;
					try {
						Interaction doc = docs.get(j);
						Set<NodeMatch> matches = addCategoryAnnotation(finalCollapsedGraph, cas, doc);
						logger.info("number of matches: " + matches.size());
						Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
						countPositive = EvaluatorUtils.compareDecisionsForInteraction(countPositive,
								doc, decisions, mostProbableCat, finalCollapsedGraph, matches, topN,
								method, bestNodeOnly, documentFrequencyQuery, termFrequencyQuery, 
								lengthBoost);
						logger.info("countPositive: " + countPositive);
					} catch (LAPException | FragmentAnnotatorException
							| ModifierAnnotatorException
							| FragmentGraphGeneratorException
							| NodeMatcherException | CategoryAnnotatorException e) {
						e.printStackTrace();
						System.exit(1);
					}				
				}
				double accuracy = (double) countPositive / (double) countProcessed;
				logger.info("acc: " + accuracy);
				accuracyPerBlock.put(countBlocks, new Double(accuracy));
			}
			/** Step 3: Extend graph with documents in current block */
			Set<FragmentGraph> fragmentGraphsAllTokens = new HashSet<FragmentGraph>();
			Set<FragmentGraph> fragmentGraphsAllDependencies = new HashSet<FragmentGraph>();
//			Set<FragmentGraph> fragmentGraphsAllSentences = new HashSet<FragmentGraph>();
			for (int j=i; j<(countBlocks*blockSize) && j<docs.size(); j++) { //for each interaction in the current block
				Interaction doc = docs.get(j);
				try {
					/** Step 3a: build fragments for single token graph*/
					fragmentGraphsAllTokens.addAll(EvaluatorUtils.buildFragmentGraphs(doc, relevantTextProvided, fragAnotLemma, null, fragGen));
					/** Step 3b: fragments for two token graph*/
					fragmentGraphsAllDependencies.addAll(EvaluatorUtils.buildFragmentGraphs(doc, relevantTextProvided, fragAnotDependency, null, fragGen));
					/** Step 3c: fragments for sentence graph*/
//					fragmentGraphsAllSentences.addAll(EvaluatorUtils.buildFragmentGraphs(doc, relevantTextProvided, fragAnotSentence, modAnot, fragGen));
				} catch (FragmentAnnotatorException | FragmentGraphGeneratorException | LAPException | ModifierAnnotatorException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			try {
				try {
					/** Step3d: merge fragments into single token graph */	
//					EvaluatorUtils.mergeIntoLemmaTokenGraph(singleTokenGraph, fragmentGraphsAllTokens); //only lemma edges
					EvaluatorUtils.mergeIntoTokenGraph(singleTokenGraph, fragmentGraphsAllTokens, dbr, null, null, null, true); //lemma + derivation edges
					/** Step3e: Merge fragments into two-token dependency graph without calling SEDA or GraphMerger */	
					EvaluatorUtils.mergeIntoDependencyGraph(twoTokenGraph, fragmentGraphsAllDependencies, dbr, gnw, germaNetRelations, splitter, mapNegation, true);
				} catch (LexicalResourceException e) {
					e.printStackTrace();
				}
				
				/** Step3d: Merge fragments into two-token dependency graph (calling SEDA) */					
				graphMerger = new LegacyAutomateWP2ProcedureGraphMerger(lapDependency, seda);
				graphMerger.setEntailmentConfidenceThreshold(confidenceThresholdSEDA);
				twoTokenGraph = graphMerger.mergeGraphs(fragmentGraphsAllDependencies, twoTokenGraph);
				/** Step3e: Merge fragments into sentence graph (calling alignment EDA) */		
				
				/*
				graphMerger = new LegacyAutomateWP2ProcedureGraphMerger(lapDependency, alignmenteda); 
				graphMerger.setEntailmentConfidenceThreshold(confidenceThresholdAlignment);
				sentenceGraph = graphMerger.mergeGraphs(fragmentGraphsAllSentences, sentenceGraph);	
				/** Step3e: Merge fragments into sentence graph (calling TIE EDA) */					
				/*
				graphMerger = new LegacyAutomateWP2ProcedureGraphMerger(lapDependency, tie); 
				graphMerger.setEntailmentConfidenceThreshold(confidenceThresholdTIE);
				sentenceGraph = graphMerger.mergeGraphs(fragmentGraphsAllSentences, sentenceGraph);	
				*/
				
				/** Step3f: Optimize all graphs and add tokens to final graph */					
				finalCollapsedGraph = new EntailmentGraphCollapsed();
				singleTokenCollapsedGraph = graphOptimizer.optimizeGraph(singleTokenGraph);
				for (EquivalenceClass ec : singleTokenCollapsedGraph.vertexSet()) {
					finalCollapsedGraph.addVertex(ec);
				}
				twoTokenCollapsedGraph = graphOptimizer.optimizeGraph(twoTokenGraph);
				for (EquivalenceClass ec : twoTokenCollapsedGraph.vertexSet()) {
					finalCollapsedGraph.addVertex(ec);
				}
				sentenceCollapsedGraph = graphOptimizer.optimizeGraph(sentenceGraph);
				for (EquivalenceClass ec : sentenceCollapsedGraph.vertexSet()) {
					finalCollapsedGraph.addVertex(ec);
				}
				cc.computeCategoryConfidences(finalCollapsedGraph);
				logger.info("Final graph contains: ");
				logger.info(finalCollapsedGraph.vertexSet().size() + " nodes");
				logger.info(finalCollapsedGraph.edgeSet().size() + " edges");
				logger.info(countDocs + " docs");
				logger.info(countFrags + " fragments");
				String xmlGraphFilename = xmlGraphFoldername 
							+ xmlDataFilename.substring(0,xmlDataFilename.length()-4) 
							+ "_blockSize" + blockSize + "_block" + i +  "_collapsedgraph.xml";
				XMLFileWriter.write(finalCollapsedGraph.toXML(), xmlGraphFilename);			
			} catch (GraphOptimizerException | ConfidenceCalculatorException | TransformerException | EntailmentGraphCollapsedException | GraphMergerException  e) {
				e.printStackTrace();
				System.exit(1);
			}			
		}		
		for (int i=2; i<accuracyPerBlock.size()+2; i++) {
			logger.info("Accuracy in block " + i + ": " + accuracyPerBlock.get(i));
		}
	}

	private static Set<NodeMatch> addCategoryAnnotation(
			EntailmentGraphCollapsed finalCollapsedGraph, JCas cas,
			Interaction doc)
			throws FragmentAnnotatorException, ModifierAnnotatorException,
			FragmentGraphGeneratorException, NodeMatcherException,
			CategoryAnnotatorException, LAPException {
		NodeMatcher nodeMatcher;
		nodeMatcher = new NodeMatcherLongestOnly(finalCollapsedGraph, bestNodeOnly);			
		logger.info("annotating interaction " + doc.getInteractionId());
		
		//build fragment graphs
		Set<FragmentGraph> fragmentGraphs = new HashSet<FragmentGraph>();
		fragmentGraphs.addAll(EvaluatorUtils.buildFragmentGraphs(doc, false, fragAnotLemma, null, fragGen));
		fragmentGraphs.addAll(EvaluatorUtils.buildFragmentGraphs(doc, false, fragAnotDependency, null, fragGen));
//		fragmentGraphs.addAll(EvaluatorUtils.buildFragmentGraphs(doc, false, fragAnotSentence, modAnot, fragGen));
		Set<NodeMatch> matches = new HashSet<NodeMatch>();
		List<FragmentGraph> fgList = new ArrayList<FragmentGraph>();
		for (FragmentGraph fg : fragmentGraphs) {
			fgList.add(fg);
		}
		//find matchings 
		for (FragmentGraph fragmentGraph: fgList) {
			matches.addAll(nodeMatcher.findMatchingNodesInGraph(fragmentGraph));
		}
		logger.info("Number of matches: " + matches.size());
		//annotate interaction
		catAnot.addCategoryAnnotation(cas, matches);
		return matches;
	}

	private static void configureSetup() throws ConfigurationException,
			FragmentAnnotatorException,
			ModifierAnnotatorException, EDAException, ComponentException {
		try{
			addLemmaLabel = true;
			lapLemma = new CachedLAPAccess(new LemmaLevelLapDE());
			lapDependency = new CachedLAPAccess(new DependencyLevelLapDE());
			fragAnotLemma = new TokenAsFragmentAnnotatorForGerman(lapLemma, tokenPosFilter, WordDecompositionType.ONLY_HYPHEN);
			fragAnotDependency = new DependencyAsFragmentAnnotatorForGerman(lapDependency, governorPosFilter, governorWordFilter, 
					dependentPosFilter, dependentWordFilter, WordDecompositionType.NO_RESTRICTION);
			fragAnotSentence = new SentenceAsFragmentAnnotator(lapDependency);
				//TODO: use KeywordBasedFragmentAnnotator if keywords are available!
	 		modAnot = new AdvAsModifierAnnotator(lapLemma); 		
			//initialize EDAs (Simple EDA and alignment EDA)
			
	 		/*
			seda = new SimpleEDA_DE(splitter, derivSteps, pathToGermaNet, 
					useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);
					*/
	 		//initialize resources for building dependency graph
	 		splitter = new GermanWordSplitter();
			splitter.setStrictMode(true);
			gnw = new GermaNetWrapper(pathToGermaNet);
			GermaNetRelation [] relations = {GermaNetRelation.has_synonym, GermaNetRelation.has_hypernym, GermaNetRelation.causes, 
				GermaNetRelation.entails, GermaNetRelation.has_hyponym};
			germaNetRelations =  Arrays.asList(relations); 
			derivSteps = 2;
			dbr = new DerivBaseResource(true, derivSteps);
			splitter = new GermanWordSplitter();
			mapNegation = true;
			
			alignmenteda = new FNR_DEvar1(); 
			alignmenteda.initialize(new File(configFilenameAlignment));
			tie = new MaxEntClassificationEDA();	
			tie.initialize(new ImplCommonConfig(new File(configFilenameTIE)));
			fragGen = new FragmentGraphLiteGeneratorFromCAS();
			graphOptimizer = new SimpleGraphOptimizer();
			cc = new ConfidenceCalculatorCategoricalFrequencyDistribution();
			catAnot = new CategoryAnnotatorAllCats();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


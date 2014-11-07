package eu.excitementproject.tl.demo;

import java.io.File;
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
import de.tuebingen.uni.sfs.germanet.api.GermaNet;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.alignmentedas.p1eda.P1EDATemplate;
import eu.excitementproject.eop.alignmentedas.p1eda.sandbox.FNR_DEvar1;
import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.MaltParserDE;
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
import eu.excitementproject.tl.decomposition.fragmentannotator.DependencyAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.TokenAndDependencyAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.TokenAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.evaluation.categoryannotator.EvaluatorUtils;
import eu.excitementproject.tl.experiments.OMQ.SimpleEDA_DE;
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
    static String pathToGermaNet = "D:/DFKI/EXCITEMENT/Linguistic Analysis/germanet-7.0/germanet-7.0/GN_V70/GN_V70_XML";
	static GermaNet germanet;
	static Set<String> GermaNetLexicon; 
	static GermanWordSplitter splitter;
	static boolean addLemmaLabel;
	
	static boolean keywordsProvided = true; //if true: input dataset contains keyword metadata
	static boolean relevantTextProvided = false; //if true; input dataset contains relevantText annotation
	
	static String xmlDataFoldername = "src/test/resources/OMQ/test/";
	static String xmlDataFilename = "omq_public_1_emails.xml";
	static String xmlGraphFoldername = "src/test/resources/sample_graphs/";
	static String configFilename = "./src/test/resources/EOP_models/omq/fnr_de_1.model";
	static File configFile;

	private final static Logger logger = Logger.getLogger(DemoUseCase2OMQGermanNew.class.getName());

	//configurations relating to lexical resources in SEDA
	static int derivSteps = 2;
	static boolean useSynonymRelation = true;
	static boolean useHypernymRelation = true;
	static boolean useEntailsRelation = true;
	static boolean useCausesRelation = true;
	
	//determine POS tags to be used for fragment annotation
    static List<String> tokenPosFilter = Arrays.asList(
    		new String []{"ADJA", "ADJD", "NN", "NE", "VVFIN", "VVINF", "VVIZU", "VVIMP", "VVPP",  "CARD"}); //"ADV" = adverb, "FM" = foreign language material
    static List<String> governorPosFilter = Arrays.asList(
    		new String []{"ADJA", "ADJD", "NN", "NE", "VVFIN", "VVINF", "VVIZU", "VVIMP", "VVPP", "CARD", "PTKNEG", "PTKVZ"}); //"VVIMP", CARD
    static List<String> dependentPosFilter = Arrays.asList(
    		new String []{"ADJA", "ADJD", "NN", "NE", "VVFIN", "VVINF", "VVIZU", "VVIMP", "VVPP", "CARD", "PTKNEG", "PTKVZ"}); //"VVIMP", CARD
    static List<String> dependencyTypeFilter = null;

    //some configurations relating to evaluation
    static String method = "tfidf";
	static boolean bestNodeOnly = true; 
	static char documentFrequencyQuery = 'd';
	static char termFrequencyQuery = 'n';
	
	static double confidenceThresholdAlignment = 0.9;
	static double confidenceThresholdSEDA = 0.9;

	public static void main(String[] args) {
		
		/** Step 0: Initialization */
		try {
			configureSetup();
		} catch (LAPException | ConfigurationException
				| FragmentAnnotatorException | ModifierAnnotatorException | EDAException e) {
			e.printStackTrace();
			System.exit(1);
		}
				
		/** Step 1: Read in data from XML file */
		String[] files = {xmlDataFoldername+xmlDataFilename,};
		File f;
		Set<Interaction> docs = new HashSet<Interaction>();
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
		EntailmentGraphRaw twoTokenGraph = new EntailmentGraphRaw();
		EntailmentGraphRaw sentenceGraph = new EntailmentGraphRaw();
		EntailmentGraphCollapsed singleTokenCollapsedGraph = new EntailmentGraphCollapsed();
		EntailmentGraphCollapsed twoTokenCollapsedGraph = new EntailmentGraphCollapsed();
		EntailmentGraphCollapsed sentenceCollapsedGraph = new EntailmentGraphCollapsed();
		EntailmentGraphCollapsed finalCollapsedGraph = new EntailmentGraphCollapsed();
		int blockSize = 2;
		JCas cas = null;
		try {
			cas = CASUtils.createNewInputCas();
		} catch (LAPException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		List<Interaction> interactionList = new ArrayList<Interaction>();
		for (Interaction doc : docs) { //for each interaction
			interactionList.add(doc);
			if (interactionList.size() > 4) break; //TODO: Remove (Debugging!)
		}
		logger.info("Number of documents: " + interactionList.size());
		int countFrags = 0;
		int countDocs = 0;
		int countBlocks = 0;
		Map<Integer,Double> accuracyPerBlock = new HashMap<Integer,Double>();

		for (int i=0; i<interactionList.size(); i+=blockSize) { //for each block
			countBlocks++;
			/** Step 2: Annotate documents in current block based on existing graph (starting with second block) */
			if (countBlocks>1) {
				int countPositive = 0;
				int countProcessed = 0;
				String mostProbableCat = EvaluatorUtils.computeMostFrequentCategory(finalCollapsedGraph);
				for (int j=i; j<(countBlocks*blockSize) && j<interactionList.size(); j++) { //for each interaction in the current block
					countDocs++;
					countProcessed++;
					try {
						Interaction doc = interactionList.get(j);
						Set<NodeMatch> matches = addCategoryAnnotation(finalCollapsedGraph, cas, doc);
						logger.info("number of matches: " + matches.size());
						Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
						countPositive = EvaluatorUtils.compareDecisionsForInteraction(countPositive,
								doc, decisions, mostProbableCat, finalCollapsedGraph, matches, topN,
								method, bestNodeOnly, documentFrequencyQuery, termFrequencyQuery);
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
			Set<FragmentGraph> fragmentGraphsAllDependencies = new HashSet<FragmentGraph>();
			Set<FragmentGraph> fragmentGraphsAllSentences = new HashSet<FragmentGraph>();
			for (int j=i; j<(countBlocks*blockSize) && j<interactionList.size(); j++) { //for each interaction in the current block
				Interaction doc = interactionList.get(j);
				/** Step 3a: Build token graph */
				try {
					//doc.fillInputCAS(cas);
					List<JCas> cases = doc.createAndFillInputCASes(relevantTextProvided);
					for (JCas casGraph : cases) {
						fragAnotLemma.annotateFragments(casGraph);
						Set<FragmentGraph> fragmentGraphs = fragGen.generateFragmentGraphs(casGraph);
						for (FragmentGraph fg : fragmentGraphs) {
							countFrags++;
							logger.info("Merging " + fg.getCompleteStatement());
							for(EntailmentUnitMention eum: fg.vertexSet()){
								//add mention to raw graph
								singleTokenGraph.addEntailmentUnitMention(eum, fg.getCompleteStatement().getTextWithoutDoubleSpaces());
								EntailmentUnit aEU = singleTokenGraph.getVertexWithText(eum.getTextWithoutDoubleSpaces());
								for(EntailmentUnit bEU : singleTokenGraph.vertexSet()){
									if(!singleTokenGraph.isEntailmentInAnyDirection(aEU, bEU)){
										if(!bEU.getTextWithoutDoubleSpaces().equalsIgnoreCase(aEU.getTextWithoutDoubleSpaces()) 
												&& bEU.getLemmatizedText().equalsIgnoreCase(aEU.getLemmatizedText())){
											singleTokenGraph.addEdgeByInduction(aEU, bEU, DecisionLabel.Entailment, 0.98);
											singleTokenGraph.addEdgeByInduction(bEU, aEU, DecisionLabel.Entailment, 0.98);
											break;
										}
									}
								}
							}
						}
						/** Step3b: Build fragments for two-token dependency graph (calling SEDA) */
						casGraph.reset();
						doc.fillInputCAS(casGraph);
						fragAnotDependency.annotateFragments(casGraph);
						fragmentGraphs = fragGen.generateFragmentGraphs(casGraph);
						fragmentGraphsAllDependencies.addAll(fragmentGraphs);
						/** Step3c: Build fragments for sentence graph (calling alignment EDA) */
						casGraph.reset();
						doc.fillInputCAS(casGraph);
						fragAnotSentence.annotateFragments(casGraph);
						fragmentGraphs = fragGen.generateFragmentGraphs(casGraph);
						fragmentGraphsAllSentences.addAll(fragmentGraphs);
					}
				} catch (FragmentAnnotatorException | FragmentGraphGeneratorException | LAPException e) {
					e.printStackTrace();
					System.exit(1);
				}
				try {
					/** Step3d: Merge fragments into two-token dependency graph (calling SEDA) */					
					graphMerger = new LegacyAutomateWP2ProcedureGraphMerger(lapDependency, seda);
					graphMerger.setEntailmentConfidenceThreshold(confidenceThresholdSEDA);
					twoTokenGraph = graphMerger.mergeGraphs(fragmentGraphsAllDependencies, twoTokenGraph);
					/** Step3e: Merge fragments into sentence graph (calling alignment EDA) */					
					graphMerger = new LegacyAutomateWP2ProcedureGraphMerger(lapDependency, alignmenteda); 
					graphMerger.setEntailmentConfidenceThreshold(confidenceThresholdAlignment);
					sentenceGraph = graphMerger.mergeGraphs(fragmentGraphsAllSentences, sentenceGraph);					
				} catch (GraphMergerException | LAPException e) {
					e.printStackTrace();
				} 
			}
			try {
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
			} catch (GraphOptimizerException | ConfidenceCalculatorException | TransformerException | EntailmentGraphCollapsedException e) {
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
		nodeMatcher = new NodeMatcherLongestOnly(finalCollapsedGraph);			
		logger.info("annotating interaction " + doc.getInteractionId());
		doc.fillInputCAS(cas);
		fragAnotCombined.annotateFragments(cas);
		if(cas.getAnnotationIndex(DeterminedFragment.type).size() > 0){
			modAnot.annotateModifiers(cas);
		}
		Set<FragmentGraph> fragmentGraphs = fragGen.generateFragmentGraphs(cas);
		Set<NodeMatch> matches = new HashSet<NodeMatch>();
		List<FragmentGraph> fgList = new ArrayList<FragmentGraph>();
		for (FragmentGraph fg : fragmentGraphs) {
			fgList.add(fg);
		}
		for (FragmentGraph fragmentGraph: fgList) {
			matches.addAll(nodeMatcher.findMatchingNodesInGraph(fragmentGraph));
		}
		logger.info("Number of matches: " + matches.size());
		catAnot.addCategoryAnnotation(cas, matches);
		return matches;
	}

	private static void configureSetup() throws ConfigurationException,
			LAPException, FragmentAnnotatorException,
			ModifierAnnotatorException, EDAException {
		addLemmaLabel = true;
		lapLemma = new CachedLAPAccess(new LemmaLevelLapDE());
		lapDependency = new CachedLAPAccess(new MaltParserDE());
		fragAnotLemma = new TokenAsFragmentAnnotator(lapLemma, tokenPosFilter);
		fragAnotDependency = new DependencyAsFragmentAnnotator(lapDependency, dependencyTypeFilter, governorPosFilter, dependentPosFilter);
		fragAnotCombined = new TokenAndDependencyAsFragmentAnnotator(lapDependency, tokenPosFilter, dependencyTypeFilter, governorPosFilter, dependentPosFilter);
		fragAnotSentence = new SentenceAsFragmentAnnotator(lapDependency);
			//TODO: use KeywordBasedFragmentAnnotator if keywords are available!
 		modAnot = new AdvAsModifierAnnotator(lapLemma); 		
		//initialize EDAs (Simple EDA and alignment EDA)
		seda = new SimpleEDA_DE(splitter, derivSteps, pathToGermaNet, 
				useSynonymRelation, useHypernymRelation, useEntailsRelation, useCausesRelation);
		alignmenteda = new FNR_DEvar1(); 
		configFile = new File(configFilename);
		alignmenteda.initialize(configFile);
		fragGen = new FragmentGraphLiteGeneratorFromCAS();
		graphOptimizer = new SimpleGraphOptimizer();
		cc = new ConfidenceCalculatorCategoricalFrequencyDistribution();
		catAnot = new CategoryAnnotatorAllCats();
	}
}

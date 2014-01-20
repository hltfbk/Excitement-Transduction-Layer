package eu.excitementproject.tl.demo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.uima.jcas.JCas;

import eu.excitement.type.tl.CategoryAnnotation;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.MaltParserDE;
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
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
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
import eu.excitementproject.tl.decomposition.fragmentannotator.KeywordBasedFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;
import eu.excitementproject.tl.structures.visualization.GraphViewer;

/**
* Shows OMQ use case data flow.
*
* @param args
* @throws FragmentAnnotatorException
* @throws ModifierAnnotatorException
* @throws FragmentGraphGeneratorException
* @throws NodeMatcherException
* @throws CategoryAnnotatorException
* @throws LAPException
*/
public class DemoUseCase2OMQGerman {
	
	static String configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE_OMQ.xml";
	static String xmlDataFoldername = "src/test/resources/WP2_public_data_XML/OMQ/";
	static String xmlDataFilename = "smallest_a.xml";
//	static String xmlDataFilename = "omq_public_1_emails.xml";
//	static String xmlDataFilename = "keywordAnnotations3.xml";
	static String xmlGraphFoldername = "src/test/resources/sample_graphs/";
	static String fragmentGraphOutputFoldername = "src/test/resources/";
	static String edaTrainingFilename = "./src/test/resources/WP2_public_RTE_pair_data/omq_public_1_th.xml";
	
	static boolean readGraph = false; //if true: read previously created graph instead of creating it
	static boolean processTrainingData = false; //if true: process the data in "edaTrainingFilename"
	static boolean trainEDA = false; //if true: train the EDA on the processed data
	static boolean keywordsProvided = false; //if true: input dataset contains keyword metadata
	static boolean relevantTextProvided = false; //if true; input dataset contains relevantText annotation
	
	private final static Logger logger = Logger.getLogger(DemoUseCase2OMQGerman.class.getName());

	static File configFile;
	static CommonConfig config;
	static CachedLAPAccess lap;
	static EDABasic<?> eda; 
	static FragmentAnnotator fragAnot;
	static ModifierAnnotator modAnot;
	static FragmentGraphGenerator fragGen;
	static GraphOptimizer graphOptimizer; 

	public static void main(String[] args) throws FragmentAnnotatorException, ModifierAnnotatorException,
		FragmentGraphGeneratorException, NodeMatcherException, CategoryAnnotatorException, EntailmentGraphRawException, IOException, TransformerException, ParserConfigurationException, EntailmentGraphCollapsedException, ConfigurationException, EDAException, ComponentException, GraphMergerException, DataReaderException, GraphOptimizerException, ConfidenceCalculatorException {
		
		BasicConfigurator.configure();
	
		/** Step 0: Initialization */
		
		configureSetup();
				
		/** Step 1: Building an entailment graph from existing data */
		
		long start = System.currentTimeMillis();
		String xmlGraphFilename = xmlGraphFoldername + xmlDataFilename.replace(".xml", "") + "_graph.xml";
		EntailmentGraphCollapsed graph = buildGraph(xmlGraphFilename);
		long end = System.currentTimeMillis();
		logger.info("Building graph took " + ((double)(end-start))/60000.0 + " minutes.");
		
		/** Step 2: Annotating an incoming email based on the entailment graph */

		String emailText = "Speicheranfrage ist ungültig.";
		JCas cas = annotateIncomingEmail(graph, emailText);
		Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
		for (CategoryDecision decision: decisions) {
			logger.info("decision: " + decision.getCategoryId() + ":" + decision.getConfidence());
		}
			
	}

	private static JCas annotateIncomingEmail(EntailmentGraphCollapsed graph, String text)
			throws LAPException, FragmentAnnotatorException,
			ModifierAnnotatorException, FragmentGraphGeneratorException,
			NodeMatcherException, CategoryAnnotatorException {
		//create some sample input
		JCas cas = CASUtils.createNewInputCas();
		cas.setDocumentLanguage("DE");
		cas.setDocumentText(text); 
		
		//add fragment annotation
		FragmentAnnotator fa = new SentenceAsFragmentAnnotator(lap); 
		fa.annotateFragments(cas);
		
		//add modifier annotation
		ModifierAnnotator ma = new AdvAsModifierAnnotator(lap);
		ma.annotateModifiers(cas);
		
		//create fragment graphs
		FragmentGraphGenerator fgg = new FragmentGraphGeneratorFromCAS();
		Set<FragmentGraph> fragmentGraphs = fgg.generateFragmentGraphs(cas);
		logger.info("Number of fragment graphs: " + fragmentGraphs.size());

		//call node matcher on each fragment graph
		NodeMatcherWithIndex nm = new NodeMatcherLuceneSimple(graph, "./src/test/resources/Lucene_index/", new GermanAnalyzer(Version.LUCENE_44));
		nm.indexGraphNodes();
		nm.initializeSearch();
		CategoryAnnotator ca = new CategoryAnnotatorAllCats();
		for (FragmentGraph fragmentGraph: fragmentGraphs) {
			Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraph);
			//add category annotation to CAS
			ca.addCategoryAnnotation(cas, matches);
		}	
		
		//print CAS category
		CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
		return cas;
	}

	private static EntailmentGraphCollapsed buildGraph(String xmlGraphFilename)
			throws EntailmentGraphCollapsedException, DataReaderException,
			LAPException, ConfigurationException, EDAException,
			ComponentException, FragmentAnnotatorException,
			ModifierAnnotatorException, FragmentGraphGeneratorException,
			GraphMergerException, GraphOptimizerException,
			ConfidenceCalculatorException, TransformerException, EntailmentGraphRawException {
		EntailmentGraphCollapsed graph = null;
		File graphFile = new File(xmlGraphFilename);
		
		if (readGraph && graphFile.exists()) {
			graph = new EntailmentGraphCollapsed(new File(xmlGraphFilename));
			logger.info("Read graph from " + xmlGraphFilename);
			logger.info("Number of graph nodes: " + graph.vertexSet().size());
		} else { 
			//Read in files
			String[] files = {xmlDataFoldername+xmlDataFilename,};
			File f;
			Set<Interaction> docs = new HashSet<Interaction>();
			
			for (String name: files) {
				f = new File(name);
				docs.addAll(InteractionReader.readInteractionXML(f));
			}
			
			// train EDA
			if (processTrainingData) {
				File trainingFile = new File(edaTrainingFilename); //training input file
				File outputDir = new File("./target/DE/dev/"); // output dir as written in configuration!
				if (!outputDir.exists()) outputDir.mkdirs();
				lap.processRawInputFormat(trainingFile, outputDir); //process training data and store output
				logger.info("Processing training data."); 
			}
			if (trainEDA) {
				eda.startTraining(config); //train EDA (may take a some time)
				logger.info("Training completed."); 
			}
			
			// prepare the output folder
			String outputFolder = fragmentGraphOutputFoldername+xmlDataFilename.replace(".xml", "");
			File theDir = new File(outputFolder);
			// if the directory does not exist, create it
			if (!theDir.exists()) {
				boolean result = theDir.mkdir();
				if(result){
					logger.info("Created dir " + theDir.getAbsolutePath());
				} else {
					logger.error("Could not create the output directory. No output files will be created.");
					outputFolder=null;
				}
			}
				
			//build fragment graphs from input data and merge them
			Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();	
			JCas cas = CASUtils.createNewInputCas();
			eda.initialize(config);
			GraphMerger graphMerger = new AutomateWP2ProcedureGraphMerger(lap, eda);
			EntailmentGraphRaw egr = null;
			for(Interaction i: docs) {
				i.fillInputCAS(cas, relevantTextProvided); 
				fragAnot.annotateFragments(cas);
				if (cas.getAnnotationIndex(FragmentAnnotation.type).size() > 0) {
					modAnot.annotateModifiers(cas);
					logger.info("Adding fragment graphs for text: " + cas.getDocumentText());
					fgs  = fragGen.generateFragmentGraphs(cas);
					logger.info("Built fragment graphs: " +fgs.size()+ " graphs.");
					egr = graphMerger.mergeGraphs(fgs, egr);
					logger.info("Merged graph: " +egr.vertexSet().size()+ " nodes");
					String xmlMergedGraphFilename = xmlGraphFoldername + xmlDataFilename.replace(".xml", "") + "_merged_graph.xml";
					XMLFileWriter.write(egr.toXML(), xmlMergedGraphFilename);			
					logger.info("Wrote to file " + xmlMergedGraphFilename);
				} else {
					logger.error("No fragment annotation found!");
				}
			}
			
			//optimize graph
			logger.info("Merged graph contains " + egr.edgeSet().size() + " edges");
			graph = graphOptimizer.optimizeGraph(egr, 0.8);
			logger.info("Optimized graph: " + graph.vertexSet().size() + " nodes");
			logger.info("Optimized graph contains " + graph.edgeSet().size() + " edges");
			
			// compute category confidences and add them to graph
			ConfidenceCalculator cc = new ConfidenceCalculatorCategoricalFrequencyDistribution();
			cc.computeCategoryConfidences(graph);
			logger.info("Computed category confidence");
			XMLFileWriter.write(graph.toXML(), xmlGraphFilename);			
			logger.info("Wrote to file " + xmlGraphFilename);
		}
		return graph;
	}

	private static void configureSetup() throws ConfigurationException,
			LAPException, FragmentAnnotatorException,
			ModifierAnnotatorException {
		configFile = new File(configFilename);	
		config = new ImplCommonConfig(configFile);
		if (keywordsProvided) { //keyword-based fragment annotation	
			lap = new CachedLAPAccess(new MaltParserDE()); 
			fragAnot = new KeywordBasedFragmentAnnotator(lap);	
			logger.info("Using keyword-based fragment annotator.");
		} else { //sentence-based fragment annotation
			lap = new CachedLAPAccess(new LemmaLevelLapDE()); 
			fragAnot = new SentenceAsFragmentAnnotator(lap);
			logger.info("Using sentence-as-fragment annotator.");
		}
		modAnot = new AdvAsModifierAnnotator(lap); 		
		eda = new MaxEntClassificationEDA();	
		fragGen = new FragmentGraphLiteGeneratorFromCAS();
		graphOptimizer = new GlobalGraphOptimizer();
	}
}

package eu.excitementproject.tl.demo;

import java.io.File;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.uima.jcas.JCas;

import eu.excitement.type.tl.CategoryAnnotation;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
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
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
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
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;

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
	static String xmlDataFoldername = "src/test/resources/WP2_public_data_XML/";
	static String xmlDataFilename = "keywordAnnotations.xml";
	static String xmlGraphFoldername = "src/test/resources/sample_graphs/";
	static String fragmentGraphOutputFoldername = "src/test/resources/";
	static String edaTrainingFilename = "./src/test/resources/WP2_public_RTE_pair_data/omq_public_1_th.xml";
	
	static boolean readGraph = true; //read previously created graph instead of creating it
	static boolean processTrainingData = false; //process the data in "edaTrainingFilename"
	static boolean trainEDA = false; //train the EDA on the processed data

	private final static Logger logger = Logger.getLogger(DemoUseCase2OMQGerman.class.getName());


	public static void main(String[] args) throws FragmentAnnotatorException, ModifierAnnotatorException,
		FragmentGraphGeneratorException, NodeMatcherException, CategoryAnnotatorException, EntailmentGraphRawException, IOException, TransformerException, ParserConfigurationException, EntailmentGraphCollapsedException, ConfigurationException, EDAException, ComponentException, GraphMergerException, DataReaderException, GraphOptimizerException, ConfidenceCalculatorException {
		
		BasicConfigurator.configure();
	
		/** Step 0: Initialization */
		
		File configFile = new File(configFilename);	
		CommonConfig config = new ImplCommonConfig(configFile);
		LAPAccess lap = new MaltParserDE(); 			
		EDABasic<?> eda = new MaxEntClassificationEDA();	
		FragmentAnnotator fragAnot = new KeywordBasedFragmentAnnotator(lap);
		ModifierAnnotator modAnot = new AdvAsModifierAnnotator(lap); 		
		FragmentGraphGenerator fragGen = new FragmentGraphLiteGeneratorFromCAS();
		GraphOptimizer graphOptimizer = new SimpleGraphOptimizer();
		
		/** Step 1: Building an entailment graph from existing data */
		
		EntailmentGraphCollapsed graph = null;
		String xmlGraphFilename = xmlGraphFoldername + xmlDataFilename.replace(".xml", "") + "_graph.xml";
		
		if (readGraph) {
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
				
			//build fragment graphs from input data
			Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();			
			for(Interaction i: docs) {
				JCas aJCas = i.createAndFillInputCAS();
				fragAnot.annotateFragments(aJCas);
				modAnot.annotateModifiers(aJCas);
				logger.info("Adding fragment graphs for text: " + aJCas.getDocumentText());
				fgs.addAll(fragGen.generateFragmentGraphs(aJCas));
			}
			
			//merge graph
			eda.initialize(config);
			GraphMerger graphMerger = new AutomateWP2ProcedureGraphMerger(lap, eda);
			EntailmentGraphRaw egr = graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());

			//optimize graph
			graph = graphOptimizer.optimizeGraph(egr, 0.99);
			
			// compute category confidences and add them to graph
			ConfidenceCalculator cc = new ConfidenceCalculatorCategoricalFrequencyDistribution();
			cc.computeCategoryConfidences(graph);
			XMLFileWriter.write(graph.toXML(), xmlGraphFilename);
		}
		//GraphViewer.drawGraph(graph);
			
		/** Step 2: Annotating an incoming email based on the entailment graph */
		
		//create some sample input
		JCas cas = CASUtils.createNewInputCas();
		cas.setDocumentLanguage("DE");
		cas.setDocumentText("Wie kann ich diese aendern?"); /*NOTE: This string does not appear in
		the emails on which the graph was built!*/
		
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
		NodeMatcherWithIndex nm = new NodeMatcherLuceneSimple(graph, "./src/test/resources/Lucene_index/", new StandardAnalyzer(Version.LUCENE_44));
		nm.indexGraphNodes();
		nm.initializeSearch();
		CategoryAnnotator ca = new CategoryAnnotatorAllCats();
		for (FragmentGraph fragmentGraph: fragmentGraphs) {
			System.out.println("fragment graph: " + fragmentGraph.getCompleteStatement());
			Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraph);
			System.out.println("matches: " + matches.size());
			//add category annotation to CAS
			ca.addCategoryAnnotation(cas, matches);
		}	
		
		//print CAS category
		CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
			
	}

}
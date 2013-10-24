package eu.excitementproject.tl.demo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
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
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.api.ConfidenceCalculator;
import eu.excitementproject.tl.composition.categoryannotator.CategoryAnnotatorAllCats;
import eu.excitementproject.tl.composition.confidencecalculator.ConfidenceCalculatorCategoricalFrequencyDistribution;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.composition.exceptions.ConfidenceCalculatorException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLucene;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

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
	static String xmlDataFilename = "german_dummy_data_for_evaluator_test.xml";
	static String xmlGraphFilename = "src/test/resources/sample_graphs/german_dummy_data_for_evaluator_test_graph.xml";
	static String outputFoldername = "src/test/resources/";
	
	public static void main(String[] args) throws FragmentAnnotatorException, ModifierAnnotatorException,
		FragmentGraphGeneratorException, NodeMatcherException, CategoryAnnotatorException, LAPException, EntailmentGraphRawException, IOException, TransformerException, ParserConfigurationException, EntailmentGraphCollapsedException {
		
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		
		File configFile = new File(configFilename);	
		CommonConfig config = null;
		LAPAccess lap;
		EDABasic<?> eda;
		UseCaseOneRunnerPrototype use1;
		EntailmentGraphCollapsed graph = null;
		
		/** Step 1: Building an entailment graph from existing data */
		
		//Read in files
		String[] files = {xmlDataFoldername+xmlDataFilename,};
		File f;
		Set<Interaction> docs = new HashSet<Interaction>();
		
		try {
			for (String name: files) {
				f = new File(name);
				docs.addAll(InteractionReader.readInteractionXML(f));
			}
			// initialize the lap
			lap = new LemmaLevelLapDE();
			
			config = new ImplCommonConfig(configFile);
			eda = new MaxEntClassificationEDA();	
			
			// train EDA
/*			File trainingFile = new File("./src/test/resources/EDA_training_data/omq_pairs_complete.xml"); //training input file
			File outputDir = new File("./target/DE/dev/"); // output dir as written in configuration!
			if (!outputDir.exists()) outputDir.mkdirs();
			lap.processRawInputFormat(trainingFile, outputDir); //process training data and store output
			eda.startTraining(config); //train EDA (may take a some time)
			System.out.print("Training completed."); */
			
			//initialize the eda
			eda.initialize(config);
			
			// prepare the output folder
			String outputFolder = outputFoldername+xmlDataFilename.replace(".xml", "");
			File theDir = new File(outputFolder);
			// if the directory does not exist, create it
			if (!theDir.exists()) {
				boolean result = theDir.mkdir();
				if(result){
					System.out.println("Created dir " + theDir.getAbsolutePath());
				} else {
					System.err.println("Could not create the output directory. No output files will be created.");
					outputFolder=null;
				}
			}
			
			// initialize use case one runner
			use1 = new UseCaseOneRunnerPrototype(lap, eda, outputFolder);
			
			// build collapsed graph
			graph = use1.buildCollapsedGraph(docs, 0.99);
			graph.toXML(xmlGraphFilename);
			//GraphViewer.drawGraph(graph);
			
			// compute category confidences and add them to graph
			ConfidenceCalculator cc = new ConfidenceCalculatorCategoricalFrequencyDistribution();
			cc.computeCategoryConfidences(graph);
			
			/** Step 2: Annotating an incoming email based on the entailment graph */
			//create some sample input
			JCas cas = CASUtils.createNewInputCas();
			cas.setDocumentLanguage("DE");
			cas.setDocumentText("Leider lösen die Punkte mein Problem nicht."); /*NOTE: This string does not appear in
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

			//call node matcher on each fragment graph
			NodeMatcherLucene nm = new NodeMatcherLucene(graph, "./src/test/resources/Lucene_index/", new StandardAnalyzer(Version.LUCENE_44));
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
			
			
		} catch (ConfigurationException | EDAException | ComponentException |
			FragmentAnnotatorException | FragmentGraphGeneratorException |
			ModifierAnnotatorException |
			GraphMergerException | CollapsedGraphGeneratorException | DataReaderException |
			 ConfidenceCalculatorException e) {
			e.printStackTrace();
		}
	}

}
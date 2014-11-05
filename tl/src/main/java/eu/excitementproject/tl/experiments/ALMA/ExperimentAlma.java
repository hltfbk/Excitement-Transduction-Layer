package eu.excitementproject.tl.experiments.ALMA;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.transform.TransformerException;

//import javax.xml.transform.TransformerException;


import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
//import eu.excitementproject.eop.common.DecisionLabel;
//import eu.excitementproject.eop.core.EditDistanceEDA;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerIT;
//import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.evaluation.utils.EvaluationAndAnalysisMeasures;
import eu.excitementproject.tl.experiments.AbstractExperiment;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

/**
 * Class to load ALMA data, build the graphs and evaluate them
 * @author Lili Kotlerman
 *
 */
public class ExperimentAlma extends AbstractExperiment {

	public String configFileName = "";
	
	public ExperimentAlma(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
		this.configFileName = configFileName;
		m_optimizer = new SimpleGraphOptimizer();
	}

	/**
	 * @param args
	 */
//	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {

//		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";
		String tlDir = "./";

		String dataDir = tlDir+"src/test/resources/XMIs";

//		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/ALMA_social_media_perFrag/";
//		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/ALMA_social_media_split/test/";
//		String dataDir = tlDir+"target/ALMA_toy_test/data/";


//		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/ALMA_Social_media_mergedGs_byClusterSplit/test/";
//		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/GRAPH-ITA-SPLIT-2014-03-14-FINAL/Dev";
//		String gsAnnotationsDir = tlDir+"target/ALMA_toy_test/gold_standard/";		
		String gsAnnotationsDir = tlDir+"src/test/resources/ALMA/ITA-DATASET-11.09.14/Test";
		
		int fileLimit = 1000;
		String outDir = dataDir.replace("resources", "outputs");
		
		File outputDir = new File(outDir);
		if (! outputDir.exists()) {
			try {
				Files.createDirectories(Paths.get(outDir));
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		
		String conf = tlDir+"src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_IT.xml";
//		String conf = tlDir+"src/test/resources/EOP_configurations/EditDistanceEDA_NonLexRes_IT.xml";
		
//		Class<?> edaClass = EditDistanceEDA.class;
		Class<?> edaClass = MaxEntClassificationEDA.class;
//		Class<?> edaClass = FakeEDA.class;

		Class<?> lapClass = TreeTaggerIT.class;
		
		
		ExperimentAlma e = null;
		EntailmentGraphRaw rawGraph = null;
		try {
			e = new ExperimentAlma(conf, dataDir, fileLimit, outDir, lapClass, edaClass);
			e.setFragmentGraphGenerator(new FragmentGraphLiteGeneratorFromCAS());
			rawGraph = e.buildRawGraph();

//	 		for the FakeEDA only -- set the returned decision to what we want
//			((FakeEDA) e.eda).initialize(DecisionLabel.Unknown);

			e.m_rawGraph.toXML(outDir+"/"+e.configFileName +"_rawGraph.xml");

		
		} catch (ConfigurationException | NoSuchMethodException
				| SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | EDAException | ComponentException
				| FragmentAnnotatorException | ModifierAnnotatorException
				| GraphMergerException | GraphOptimizerException
				| FragmentGraphGeneratorException | IOException
				| EntailmentGraphRawException | TransformerException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
				
		boolean isSingleClusterGS = false;

		for (double confidenceThreshold : e.getConfidenceThresholds()){
			EntailmentGraphRaw rawGraphWithThreshold = e.applyThreshold(rawGraph, confidenceThreshold);

			String setting = "raw without FG";
			EvaluationAndAnalysisMeasures res = e.evaluateRawGraph(rawGraphWithThreshold, gsAnnotationsDir, !includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "raw with FG";
			res = e.evaluateRawGraph(rawGraphWithThreshold, gsAnnotationsDir, includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "collapsed";
			EntailmentGraphCollapsed cgr = e.collapseGraph(rawGraphWithThreshold);
			res = e.evaluateCollapsedGraph(cgr, gsAnnotationsDir, includeFragmentGraphEdges, isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);

			setting = "collapsed+closure";
			cgr.applyTransitiveClosure(false);
			res = e.evaluateCollapsedGraph(cgr, gsAnnotationsDir, includeFragmentGraphEdges, isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);


			EntailmentGraphRaw plusClosureRawGraph = e.getPlusClosureGraph(rawGraphWithThreshold);
			
			setting = "plusClosure raw without FG";
			res = e.evaluateRawGraph(plusClosureRawGraph, gsAnnotationsDir, !includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "plusClosure raw with FG";
			res = e.evaluateRawGraph(plusClosureRawGraph, gsAnnotationsDir, includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "plusClosure collapsed";
			cgr = e.collapseGraph(plusClosureRawGraph);
			res = e.evaluateCollapsedGraph(cgr, gsAnnotationsDir, includeFragmentGraphEdges, isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "plusClosure collapsed+closure";
			cgr.applyTransitiveClosure(false);
			res = e.evaluateCollapsedGraph(cgr, gsAnnotationsDir, includeFragmentGraphEdges,  isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
		}
		
		
		e.printResults();
		System.out.println("Done");
	}

}

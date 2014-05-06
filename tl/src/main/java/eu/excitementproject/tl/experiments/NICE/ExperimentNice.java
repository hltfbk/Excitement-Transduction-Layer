package eu.excitementproject.tl.experiments.NICE;

import java.io.IOException;

//import javax.xml.transform.TransformerException;

//import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeEDA;
//import eu.excitementproject.eop.core.EditDistanceEDA;
//import eu.excitementproject.eop.core.DKProSimilaritySimpleEDA;
//import eu.excitementproject.eop.core.MaxEntClassificationEDA;
//import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
//import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
//import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.experiments.AbstractExperiment;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
//import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.utils.ProbabilisticEDA;
//import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;

/** 
 * Class to load NICE data, build the graphs and evaluate them
 * @author Lili Kotlerman
 * 
 */
public class ExperimentNice extends AbstractExperiment {

	public ExperimentNice(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
		m_optimizer = new SimpleGraphOptimizer();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		String tlDir = "C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/";
		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";

//		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_open_trainTest_byClusterSplit/test";
		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_open_byFrag_byClusterSplit/test";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-FINAL/Dev";
		
		int fileLimit = 1000000;
		String outDir = dataDir.replace("resources", "outputs");
		
		System.out.println(tlDir);
	//	System.out.println(System.getProperties());
		

		
/*		ExperimentNice eRandom = new ExperimentNice(
		tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file

		dataDir, fileLimit, outDir,

		TreeTaggerEN.class, //not used, just some available LAP
		RandomEDA.class
		);
*/		
		ExperimentNice eProb = new ExperimentNice(
		tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file

		dataDir, fileLimit, outDir,

		TreeTaggerEN.class, //not used, just some available LAP
		ProbabilisticEDA.class // to assign desired probability go to the EDA code (hard-coded in the beginning)
		);
		
		/*	ExperimentNice eTIEpos = new ExperimentNice(
				tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml",

				dataDir, fileLimit, outDir,

				TreeTaggerEN.class,
				MaxEntClassificationEDA.class
				);*/
		

	/*	ExperimentNice eTIEposRes = new ExperimentNice(
				tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",

				dataDir, fileLimit, outDir,

				TreeTaggerEN.class,
				MaxEntClassificationEDA.class
				);*/

				
/*		ExperimentNice eTIEparsedRes = new ExperimentNice(
				tlDir+"/src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml",

				dataDir, fileLimit, outDir,
				
				MaltParserEN.class,
				MaxEntClassificationEDA.class
				);
*/
		

		
	/*	ExperimentNice eBIUTEE = new ExperimentNice(
				tlDir+"src/test/resources/NICE_experiments/biutee_wp6_exci.xml",
//				tlDir+"src/test/resources/NICE_experiments/biutee.xml",
				
				dataDir, fileLimit, outDir,
				
				BIUFullLAP.class,
				BiuteeEDA.class
				);*/
		
/*		ExperimentNice EditDistBase = new ExperimentNice(
				tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_NonLexRes_EN.xml",

				dataDir, fileLimit, outDir,

				TreeTaggerEN.class,
				EditDistanceEDA.class
				);		*/
		
		/*		//TODO: find what lap to use + find the conf files + what EDA to use (simple vs classifier)
		Experiment eDKPro = new Experiment(
		"D:/EOPspace/eop-resources-1.0.2/configuration-files/biutee.xml",

		"./src/test/resources/WP2_public_data_CAS_XMI/NICE_open", 19,
		"/home/nastase/Projects/eop/excitement-transduction-layer/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS XMI/nice_email 1",
		???.class,
		DKProSimilaritySimpleEDA.class
		);
*/
			
		ExperimentNice e = eProb; 
		e.buildRawGraph();
		try {
			e.m_rawGraph.toXML(outDir+"/"+e.configFile.getName()+"_rawGraph.xml");
			e.m_rawGraph.toDOT(outDir+"/"+e.configFile.getName()+"_rawGraph.dot");
		} catch (IOException | EntailmentGraphRawException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		boolean isSingleClusterGS = false;

		
		//TODO Verify why FG edges are not found in the graph. Is it only for closure edges? -- solved for plusClosure data??? 
	//	System.out.println(gr);
		
		for (double confidenceThreshold : e.confidenceThresholds){
			System.out.println("Before applying threshold "+ confidenceThreshold+": Edges in raw graph=" + e.m_rawGraph.edgeSet().size());
			String setting = "raw without FG";
			EvaluationMeasures res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph, gsAnnotationsDir, !includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "raw with FG";
			res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph, gsAnnotationsDir, includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "collapsed";
			EntailmentGraphCollapsed cgr = e.collapseGraph(confidenceThreshold, false);
			res = e.evaluateCollapsedGraph(cgr, gsAnnotationsDir, isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);

			setting = "collapsed+closure";
			cgr.applyTransitiveClosure(false);
			res = e.evaluateCollapsedGraph(cgr, gsAnnotationsDir, isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);

		}

		for (double confidenceThreshold : e.confidenceThresholds){						
			System.out.println("Before applying threshold "+ confidenceThreshold+": Edges in raw graph with closure =" + e.m_rawGraph_plusClosure.edgeSet().size());
			String setting = "plusClosure raw without FG";
			EvaluationMeasures res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph_plusClosure, gsAnnotationsDir, !includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "plusClosure raw with FG";
			res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph_plusClosure, gsAnnotationsDir, includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "plusClosure collapsed";
			EntailmentGraphCollapsed cgr = e.collapseGraph(confidenceThreshold, true);
			res = e.evaluateCollapsedGraph(cgr, gsAnnotationsDir, isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "plusClosure collapsed+closure";
			cgr.applyTransitiveClosure(false);
			res = e.evaluateCollapsedGraph(cgr, gsAnnotationsDir, isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
		}
		
		
		e.printResults();
		System.out.println("Done");
		
	}

}

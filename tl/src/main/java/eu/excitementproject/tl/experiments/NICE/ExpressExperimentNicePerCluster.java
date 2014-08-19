package eu.excitementproject.tl.experiments.NICE;

import java.io.File;
import java.io.IOException;

//import javax.xml.transform.TransformerException;



import eu.excitementproject.eop.core.MaxEntClassificationEDA;
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
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
//import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
//import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.evaluation.utils.EvaluationAndAnalysisMeasures;
import eu.excitementproject.tl.experiments.AbstractExperiment;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
//import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
//import eu.excitementproject.tl.structures.rawgraph.utils.ProbabilisticEDA;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;

/** 
 * Class to load NICE data, build the graphs and evaluate them
 * @author Lili Kotlerman
 * 
 */
public class ExpressExperimentNicePerCluster extends AbstractExperiment {

	public ExpressExperimentNicePerCluster(String configFileName, String dataDir,
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

		String tlDir = "C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/";
//		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";

//		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_open_trainTest_byClusterSplit/test";
		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_open_perFrag/test";
//		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-FINAL/Dev";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/NICE_open_trainTest_byClusterSplit_reAnnotated/test";
		
		int fileLimit = 1000000;
		String outDir = dataDir.replace("resources", "outputs");
		
		System.out.println(tlDir);
	//	System.out.println(System.getProperties());
		

		
/*			
		File gsDir = new File(gsAnnotationsDir);
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			System.out.println(gsClusterDir);
			GoldStandardEdgesLoader gsLoader = new GoldStandardEdgesLoader(true);
			try {
				gsLoader.loadClusterAnnotations(gsClusterDir, false);
				System.out.println(gsLoader.getEdges().size());
			} catch (GraphEvaluatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

		
		
			
		boolean isSingleClusterGS = true;
		String results = "";
		File gsDir = new File(gsAnnotationsDir);
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			File clustGS = new File(gsClusterDir);
			if (!clustGS.isDirectory()) continue;
			System.out.println(gsClusterDir);

		/*	ExpressExperimentNicePerCluster eRand = new ExpressExperimentNicePerCluster(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file

					dataDir+"/"+clusterDir, fileLimit, outDir,

					TreeTaggerEN.class, //not used, just some available LAP
					RandomEDA.class 
					);*/

			/*	ExpressExperimentNicePerCluster eProb = new ExpressExperimentNicePerCluster(
			tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file

			dataDir+"/"+clusterDir, fileLimit, outDir,

			TreeTaggerEN.class, //not used, just some available LAP
			ProbabilisticEDA.class // to assign desired probability go to the EDA code (hard-coded in the beginning)
			);*/
	
	
			ExpressExperimentNicePerCluster eTIEpos = new ExpressExperimentNicePerCluster(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml",
		
					dataDir+"/"+clusterDir, fileLimit, outDir,
		
					TreeTaggerEN.class,
					MaxEntClassificationEDA.class
					);
			
		
		/*	ExperimentNice eTIEposRes = new ExperimentNice(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",
		
					dataDir+"/"+clusterDir, fileLimit, outDir,
		
					TreeTaggerEN.class,
					MaxEntClassificationEDA.class
					);*/
		
					
		/*	ExpressExperimentNicePerCluster eTIEparsedRes = new ExpressExperimentNicePerCluster(
					tlDir+"/src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml",
		
					dataDir+"/"+clusterDir, fileLimit, outDir,
					
					MaltParserEN.class,
					MaxEntClassificationEDA.class
					);
		*/
			
		
			
		/*	ExperimentNice eBIUTEE = new ExperimentNice(
					tlDir+"src/test/resources/NICE_experiments/biutee_wp6_exci.xml",
		//			tlDir+"src/test/resources/NICE_experiments/biutee.xml",
					
					dataDir+"/"+clusterDir, fileLimit, outDir,
					
					BIUFullLAP.class,
					BiuteeEDA.class
					);*/
			
		/*		ExperimentNice EditDistBase = new ExperimentNice(
					tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_NonLexRes_EN.xml",
		
					dataDir+"/"+clusterDir, fileLimit, outDir,
		
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
			
			
			ExpressExperimentNicePerCluster e = eTIEpos; 
			
			Double confidenceThreshold = e.confidenceThresholds.get(8); // get(6) = 0.8
			System.out.println("Threshold is "+confidenceThreshold);
			e.buildRawGraph(confidenceThreshold);
			try {
				e.m_rawGraph.toXML(outDir+"/"+e.configFile.getName()+"_"+String.valueOf(confidenceThreshold)+"_rawGraph.xml");
				e.m_rawGraph.toDOT(outDir+"/"+e.configFile.getName()+"_"+String.valueOf(confidenceThreshold)+"_rawGraph.dot");
			} catch (IOException | EntailmentGraphRawException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
			
			double threshold = confidenceThreshold;
	//		for (double threshold : e.confidenceThresholds){
			//	if (threshold < confidenceThreshold) continue;
				System.out.println("Before applying threshold "+ threshold+": Edges in raw graph=" + e.m_rawGraph.edgeSet().size());
				String setting = clusterDir + "\t" +  "raw without FG";
				EvaluationAndAnalysisMeasures res = new EvaluationAndAnalysisMeasures(e.evaluateRawGraph(threshold, e.m_rawGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS));		
				System.out.println(setting+"\t"+threshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(e.m_rawGraph, clustGS);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, threshold, res);
				
				
				setting = clusterDir + "\t" +  "raw with FG";
				res = new EvaluationAndAnalysisMeasures(e.evaluateRawGraph(threshold, e.m_rawGraph, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS));		
				System.out.println(setting+"\t"+threshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(e.m_rawGraph, clustGS);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, threshold, res);
				
				setting = clusterDir + "\t" +  "collapsed";
				EntailmentGraphCollapsed cgr = e.collapseGraph(threshold, false);
				res = new EvaluationAndAnalysisMeasures(e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS));
				System.out.println(setting+"\t"+threshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(cgr, clustGS);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, threshold, res);

				setting = clusterDir + "\t" +  "collapsed+closure";
				cgr.applyTransitiveClosure(false);
				res = new EvaluationAndAnalysisMeasures(e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS));
				System.out.println(setting+"\t"+threshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(cgr, clustGS);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, threshold, res);

	//		}

	/*//		for (double threshold : e.confidenceThresholds){						
		//		if (threshold < confidenceThreshold) continue;
				System.out.println("Before applying threshold "+ threshold+": Edges in raw graph with closure =" + e.m_rawGraph_plusClosure.edgeSet().size());
				setting = clusterDir + "\t" +  "plusClosure raw without FG";
				res = e.evaluateRawGraph(threshold, e.m_rawGraph_plusClosure, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);		
				System.out.println(setting+"\t"+threshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				e.addResult(setting, threshold, res);
				
				setting = clusterDir + "\t" +  "plusClosure raw with FG";
				res = e.evaluateRawGraph(threshold, e.m_rawGraph_plusClosure, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);		
				System.out.println(setting+"\t"+threshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				e.addResult(setting, threshold, res);
				
				setting = clusterDir + "\t" +  "plusClosure collapsed";
				cgr = e.collapseGraph(threshold, true);
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS);
				System.out.println(setting+"\t"+threshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				e.addResult(setting, threshold, res);
				
				setting = clusterDir + "\t" +  "plusClosure collapsed+closure";
				cgr.applyTransitiveClosure(false);
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS);
				System.out.println(setting+"\t"+threshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				e.addResult(setting, threshold, res);
	//		}			
*/			
				results+=e.printResults()+"\n";
		}
			
		
		System.out.println("Done");
		System.out.println(results);
		
		
	}

}

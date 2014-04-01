package eu.excitementproject.tl.experiments.NICE;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeEDA;
import eu.excitementproject.eop.core.EditDistanceEDA;
//import eu.excitementproject.eop.core.DKProSimilaritySimpleEDA;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.experiments.AbstractExperiment;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

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

//		String tlDir = "C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/";
		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";

//		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_open_trainTest_byClusterSplit/test";
		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_open_byFrag_byClusterSplit/test";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-FINAL/Dev";
		
		int fileLimit = 1000000;
		String outDir = dataDir.replace("resources", "outputs");
		
		System.out.println(tlDir);
	//	System.out.println(System.getProperties());
		
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

				
		ExpressExperimentNicePerCluster eTIEparsedRes = new ExpressExperimentNicePerCluster(
				tlDir+"/src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml",

				dataDir, fileLimit, outDir,
				
				MaltParserEN.class,
				MaxEntClassificationEDA.class
				);

		

		
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
			

		File gsDir = new File(gsAnnotationsDir);
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			System.out.println(gsClusterDir);
		}		

		/*		ExpressExperimentNicePerCluster e = eTIEparsedRes; 
		
		Double confidenceThreshold = 0.8;
			e.buildRawGraph(confidenceThreshold);
		try {
			e.m_rawGraph.toXML(outDir+"/"+e.configFile.getName()+"_"+String.valueOf(confidenceThreshold)+"_rawGraph.xml");
			e.m_rawGraph.toDOT(outDir+"/"+e.configFile.getName()+"_"+String.valueOf(confidenceThreshold)+"_rawGraph.dot");
		} catch (IOException | EntailmentGraphRawException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		boolean isSingleClusterGS = true;
		File gsDir = new File(gsAnnotationsDir);
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			System.out.println(gsClusterDir);
			System.out.println("With threshold "+ confidenceThreshold+": Edges in raw graph=" + e.m_rawGraph.edgeSet().size());
			String setting = "raw without FG"+"\t"+clusterDir;
			EvaluationMeasures res = e.evaluateRawGraph(e.m_rawGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "raw with FG"+"\t"+clusterDir;
			res = e.evaluateRawGraph(e.m_rawGraph, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);		
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
			
			setting = "collapsed"+"\t"+clusterDir;
			EntailmentGraphCollapsed cgr = e.collapseGraph(confidenceThreshold, false);
			res = e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);

			setting = "collapsed+closure"+"\t"+clusterDir;
			cgr.applyTransitiveClosure(false);
			res = e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS);
			System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			e.addResult(setting, confidenceThreshold, res);
	
		}
		
			
		e.printResults();
	*/	
		System.out.println("Done");
		
	}

}

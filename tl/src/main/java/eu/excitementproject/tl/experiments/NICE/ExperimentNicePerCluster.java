package eu.excitementproject.tl.experiments.NICE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.excitementproject.eop.common.DecisionLabel;
//import javax.xml.transform.TransformerException;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
//import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeEDA;
//import eu.excitementproject.eop.core.EditDistanceEDA;
//import eu.excitementproject.eop.core.DKProSimilaritySimpleEDA;
//import eu.excitementproject.eop.core.MaxEntClassificationEDA;
//import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
//import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
//import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
//import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;
import eu.excitementproject.eop.globalgraphoptimizer.defs.Pair;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.graphmerger.AllPairsGraphMergerWithNonEntailments;
import eu.excitementproject.tl.composition.graphoptimizer.GlobalGraphOptimizer;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.utils.EvaluationAndAnalysisMeasures;
import eu.excitementproject.tl.experiments.AbstractExperiment;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

/** 
 * Class to load NICE data, build the graphs and evaluate them
 * @author Lili Kotlerman
 * 
 */
public class ExperimentNicePerCluster extends AbstractExperiment {

	private static final Logger logger = Logger.getLogger(ExperimentNicePerCluster.class);

	public ExperimentNicePerCluster(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
//		m_optimizer = new SimpleGraphOptimizer();
		
		try {
			super.useOne.setGraphMerger(new AllPairsGraphMergerWithNonEntailments(super.lap, super.eda));
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		
		logger.info(tlDir);
	//	logger.info(System.getProperties());
			
		boolean isSingleClusterGS = true;
		String ress = "";
		File gsDir = new File(gsAnnotationsDir);
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			File clustGS = new File(gsClusterDir);
			if (!clustGS.isDirectory()) continue;
			logger.info(gsClusterDir);
				
			ExperimentNicePerCluster eTIEpos = new ExperimentNicePerCluster(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml",
		
					dataDir+"/"+clusterDir, fileLimit, outDir,
		
					TreeTaggerEN.class,
					MaxEntClassificationEDA.class
					);

			
			ExperimentNicePerCluster e = eTIEpos; 
			e.buildRawGraph();
				
				Set<Pair<String, String>> entailings = new HashSet<Pair<String, String>>();
				Set<Pair<String, String>> nonentailings = new HashSet<Pair<String, String>>();
				for (EntailmentRelation fge : e.m_rfg.edgeSet()){
					if (fge.getLabel().is(DecisionLabel.Entailment)){
						entailings.add(new Pair<String, String>(fge.getSource().getText(), fge.getTarget().getText()));
					}
					else{
						nonentailings.add(new Pair<String, String>(fge.getSource().getText(), fge.getTarget().getText()));
					}
				}
				e.m_optimizer = new GlobalGraphOptimizer(entailings, nonentailings);
				System.out.print(entailings);
				System.out.print(nonentailings);

				
			try {
				e.m_rawGraph.toXML(outDir+"/"+e.configFile.getName()+"_rawGraph.xml");
				e.m_rawGraph.toDOT(outDir+"/"+e.configFile.getName()+"_rawGraph.dot");
			} catch (IOException | EntailmentGraphRawException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			for (double confidenceThreshold : e.confidenceThresholds){
				logger.info("Before applying threshold "+ confidenceThreshold+": Edges in raw graph=" + e.m_rawGraph.edgeSet().size());
				String setting = "raw without FG "+clusterDir;
				EvaluationAndAnalysisMeasures res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);		
				logger.info(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(e.m_rawGraph);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());					
					res.setEdaCalls(e.getEdaCallsNumber());
				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, confidenceThreshold, res);
				
				setting = "raw with FG "+clusterDir;
				res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);		
				logger.info(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(e.m_rawGraph);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());

				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, confidenceThreshold, res);
				
				setting = "collapsed "+clusterDir;
				EntailmentGraphCollapsed cgr = e.collapseGraph(confidenceThreshold, false);
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS);
				logger.info(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(cgr);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());

				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, confidenceThreshold, res);

				setting = "collapsed+closure "+clusterDir;
				cgr.applyTransitiveClosure(false);
				for (EntailmentRelationCollapsed cole : cgr.edgeSet()){
					cole.setEdgeType(EdgeType.UNKNOWN);
				}
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS);
				logger.info(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(cgr);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());

				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, confidenceThreshold, res);
	
			}

			for (double confidenceThreshold : e.confidenceThresholds){						
				logger.info("Before applying threshold "+ confidenceThreshold+": Edges in raw graph with closure =" + e.m_rawGraph_plusClosure.edgeSet().size());

				String setting = "plusClosure raw without FG "+clusterDir;
				EvaluationAndAnalysisMeasures res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph_plusClosure, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);		
				logger.info(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(e.m_rawGraph_plusClosure);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());

				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, confidenceThreshold, res);
				
				setting = "plusClosure raw with FG "+clusterDir;
				res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph_plusClosure, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);		
				logger.info(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(e.m_rawGraph_plusClosure);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());

				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, confidenceThreshold, res);
				
				EntailmentGraphCollapsed cgr = e.collapseGraph(confidenceThreshold, true);

				setting = "plusClosure collapsed "+ clusterDir;
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS);
				logger.info(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(cgr);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());

				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, confidenceThreshold, res);
				
				setting = "plusClosure collapsed+closure "+clusterDir;
				cgr.applyTransitiveClosure(false);
				for (EntailmentRelationCollapsed cole : cgr.edgeSet()){
					cole.setEdgeType(EdgeType.UNKNOWN);
				}				
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, isSingleClusterGS);
				logger.info(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(cgr);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());
				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, confidenceThreshold, res);
			}
			
			
			ress+=e.printResults()+"\n";
			
			logger.info("Done");
			try {
				BufferedWriter outWriter = new BufferedWriter(new FileWriter(outDir+"/_NICE_experiment_results.txt"));
				outWriter.write(e.toString()+"\n");
				outWriter.write(ress);
				outWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		logger.info(ress);
	}

}

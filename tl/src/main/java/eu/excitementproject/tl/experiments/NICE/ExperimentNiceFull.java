package eu.excitementproject.tl.experiments.NICE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
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
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.rawgraph.utils.ProbabilisticEDA;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;
import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeEDA;
import eu.excitementproject.eop.core.EditDistanceEDA;
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

/** 
 * Class to load NICE data, build the graphs and evaluate them
 * @author Lili Kotlerman
 * 
 */
public class ExperimentNiceFull extends AbstractExperiment {

	public ExperimentNiceFull(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
//		m_optimizer = new SimpleGraphOptimizer();
		m_optimizer = new GlobalGraphOptimizer();
		
		try {
			super.useOne.setGraphMerger(new AllPairsGraphMergerWithNonEntailments(super.lap, super.eda));
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public enum EdaName{
		RANDOM,
		PROBABILISTIC,
		TIE_POS,
		TIE_POS_RES,
		TIE_PARSE_RES,
		BIUTEE,
		EDIT_DIST
	}
	
	public static ExperimentNiceFull initExperiment(EdaName edaName, String tlDir, String dataDir, int fileLimit, String outDir){
		
		if (edaName.equals(EdaName.BIUTEE)) {
			return new ExperimentNiceFull(
			tlDir+"src/test/resources/NICE_experiments/biutee.xml",
			dataDir, fileLimit, outDir,			
			BIUFullLAP.class,
			BiuteeEDA.class
			);
			
		}
		
		if (edaName.equals(EdaName.EDIT_DIST)) {
			return new ExperimentNiceFull(
					tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_NonLexRes_EN.xml",
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class,
					EditDistanceEDA.class
					);					
		}

		if (edaName.equals(EdaName.PROBABILISTIC)) {
			return 	new ExperimentNiceFull(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class, //not used, just some available LAP
					ProbabilisticEDA.class // to assign desired probability go to the EDA code (hard-coded in the beginning)
					);
		}

		if (edaName.equals(EdaName.RANDOM)) {
			return new ExperimentNiceFull(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class, //not used, just some available LAP
					RandomEDA.class
					);
		}

		if (edaName.equals(EdaName.TIE_PARSE_RES)) {
			return new ExperimentNiceFull(
			tlDir+"/src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml",
			dataDir, fileLimit, outDir,
			MaltParserEN.class,
			MaxEntClassificationEDA.class
			);			
		}
		
		if (edaName.equals(EdaName.TIE_POS)) {
			return new ExperimentNiceFull(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml",
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class,
					MaxEntClassificationEDA.class
					);
		}
		
		if (edaName.equals(EdaName.TIE_POS_RES)) {
			return new ExperimentNiceFull(
			tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",
			dataDir, fileLimit, outDir,
			TreeTaggerEN.class,
			MaxEntClassificationEDA.class
			);
		}
	
		
		/*		//TODO: find what lap to use + find the conf files + what EDA to use (simple vs classifier)
		Experiment eDKPro = new Experiment(
		"D:/EOPspace/eop-resources-1.0.2/configuration-files/biutee.xml",

		"./src/test/resources/WP2_public_data_CAS_XMI/NICE_open", 19,
		"/home/nastase/Projects/eop/excitement-transduction-layer/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS XMI/nice_email 1",
		???.class,
		DKProSimilaritySimpleEDA.class
		);
*/
		return null;
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
			
		boolean isSingleClusterGS = true;
		String ress = "";
		File gsDir = new File(gsAnnotationsDir);
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			File clustGS = new File(gsClusterDir);
			if (!clustGS.isDirectory()) continue;
			System.out.println(gsClusterDir);
				
			ExperimentNiceFull e = initExperiment(EdaName.TIE_POS, tlDir, dataDir+"/"+clusterDir, fileLimit, outDir); 
			EntailmentGraphRaw rawGraph = e.buildRawGraph();
				
/*				Set<Pair<String, String>> entailings = new HashSet<Pair<String, String>>();
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
*/
				
			try {
				e.m_rawGraph.toXML(outDir+"/"+e.configFile.getName()+"_rawGraph.xml");
				e.m_rawGraph.toDOT(outDir+"/"+e.configFile.getName()+"_rawGraph.dot");
			} catch (IOException | EntailmentGraphRawException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			for (double confidenceThreshold : e.getConfidenceThresholds()){
				EntailmentGraphRaw rawGraphWithThreshold = e.applyThreshold(rawGraph, confidenceThreshold);
				
				String setting = "raw without FG "+clusterDir;
				System.out.println("### "+ setting+ "###");
				System.out.println(rawGraphWithThreshold.toString());
				EvaluationAndAnalysisMeasures res = e.evaluateRawGraph(rawGraphWithThreshold, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);		
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(rawGraphWithThreshold);
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
				System.out.println("### "+ setting+ "###");
				System.out.println(rawGraphWithThreshold.toString());
				res = e.evaluateRawGraph(rawGraphWithThreshold, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);		
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(rawGraphWithThreshold);
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
				System.out.println("### "+ setting+ "###");
//				EntailmentGraphCollapsed cgr = e.collapseGraph(confidenceThreshold, false);
				EntailmentGraphCollapsed cgr = e.collapseGraph(rawGraphWithThreshold);
				System.out.println(cgr.toString());
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
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
				System.out.println("### "+ setting+ "###");
				cgr.applyTransitiveClosure(false);
				for (EntailmentRelationCollapsed cole : cgr.edgeSet()){
					if (!cole.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) cole.setEdgeType(EdgeType.UNKNOWN);
				}
				System.out.println(cgr.toString());
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
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
	
				// now get plusClosure graph for current m_rawGraph (with confidence threshold applied)
				EntailmentGraphRaw plusClosureRawGraph = e.getPlusClosureGraph(rawGraphWithThreshold);
				
				System.out.println("Before applying threshold "+ confidenceThreshold+": Edges in raw graph with closure =" + plusClosureRawGraph.edgeSet().size());

				setting = "plusClosure raw without FG "+clusterDir;
				System.out.println("### "+ setting+ "###");
				System.out.println(plusClosureRawGraph.toString());
				res = e.evaluateRawGraph(plusClosureRawGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);		
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(plusClosureRawGraph);
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
				System.out.println("### "+ setting+ "###");
				System.out.println(plusClosureRawGraph.toString());
				res = e.evaluateRawGraph(plusClosureRawGraph, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);		
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(plusClosureRawGraph);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());

				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.addResult(setting, confidenceThreshold, res);
				
//				EntailmentGraphCollapsed cgr = e.collapseGraph(confidenceThreshold, true);
				cgr = e.collapseGraph(plusClosureRawGraph);

				setting = "plusClosure collapsed "+ clusterDir;
				System.out.println("### "+ setting+ "###");
				System.out.println(cgr.toString());
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
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
				System.out.println("### "+ setting+ "###");
				cgr.applyTransitiveClosure(false);
				for (EntailmentRelationCollapsed cole : cgr.edgeSet()){
					if (!cole.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) cole.setEdgeType(EdgeType.UNKNOWN);
				}
				System.out.println(cgr.toString());
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
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
			
			System.out.println("Done");
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
		
		System.out.println(ress);
	}

}

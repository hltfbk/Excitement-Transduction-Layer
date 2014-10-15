package eu.excitementproject.tl.experiments.ALMA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eu.excitementproject.eop.lap.dkpro.TreeTaggerIT;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.graphmerger.LegacyAllPairsGraphMerger;
import eu.excitementproject.tl.composition.graphmerger.AllPairsGraphMerger;
import eu.excitementproject.tl.composition.graphmerger.LegacyAutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.composition.graphmerger.NoEdaGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.GlobalGraphOptimizer;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphoptimizer.EvaluatorGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationAndAnalysisMeasures;
import eu.excitementproject.tl.experiments.AbstractExperiment;
import eu.excitementproject.tl.experiments.ResultsContainer;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.rawgraph.utils.ProbabilisticEDA;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;
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
 * Class to load ALMA data, build the graphs and evaluate them
 * @author Lili Kotlerman
 * 
 */
public class ExperimentAlmaPerCluster extends AbstractExperiment {

	public ExperimentAlmaPerCluster(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
		m_optimizer = new SimpleGraphOptimizer();
		
		try {
//			super.useOne.setGraphMerger(new AllPairsGraphMerger(super.lap, super.eda));
//			super.useOne.setGraphMerger(new AllPairsGraphMergerWithNonEntailments(super.lap, super.eda));
			super.useOne.setGraphMerger(new AutomateWP2ProcedureGraphMerger(super.lap, super.eda));
//			super.useOne.setGraphMerger(new NoEdaGraphMerger(super.lap, super.eda));
			
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public enum EdaName{
		RANDOM,
		PROBABILISTIC,
		TIE_POS,
		EDIT_DIST
	}
	
	public static ExperimentAlmaPerCluster initExperiment(EdaName edaName, String tlDir, String dataDir, int fileLimit, String outDir){
		
		if (edaName.equals(EdaName.EDIT_DIST)) {
			return new ExperimentAlmaPerCluster(
					tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_NonLexRes_EN.xml",
//					tlDir+"src/test/resources/EOP_configurations/EditDistanceEDA_IT_WordNet.xml",
					dataDir, fileLimit, outDir,
					TreeTaggerIT.class,
					EditDistanceEDA.class
					);					
		}

		if (edaName.equals(EdaName.PROBABILISTIC)) {
			return 	new ExperimentAlmaPerCluster(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerIT.class, //not used, just some available LAP
					ProbabilisticEDA.class // to assign desired probability go to the EDA code (hard-coded in the beginning)
					);
		}

		if (edaName.equals(EdaName.RANDOM)) {
			return new ExperimentAlmaPerCluster(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerIT.class, //not used, just some available LAP
					RandomEDA.class
					);
		}

		
		if (edaName.equals(EdaName.TIE_POS)) {
			return new ExperimentAlmaPerCluster(
					tlDir+"src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_IT.xml",
					dataDir, fileLimit, outDir,
					TreeTaggerIT.class,
					MaxEntClassificationEDA.class
					);
		}
			
		
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String tlDir = "C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/";
//		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";

//		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_open_trainTest_byClusterSplit/test";
//		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_open_perFrag/test";
		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/ALMA_all/Test";
//		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_all/test";
		
//		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-FINAL/Dev";
//		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/NICE_open_trainTest_byClusterSplit_reAnnotated/test";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/ALMA_reAnnotated/Test";
//		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/NICE_EMAIL_reAnnotated/all/test";
		
		int fileLimit = 1000000;
		String outDir = dataDir.replace("resources", "outputs");

		ResultsContainer combinedExperimet = new ResultsContainer(); 

		System.out.println(tlDir);
	//	System.out.println(System.getProperties());
		
		GraphOptimizer globalOptimizer = new GlobalGraphOptimizer();
		
		boolean isSingleClusterGS = true;
		String ress = "";
		File gsDir = new File(gsAnnotationsDir);

//	EdaName[] names = {EdaName.EDIT_DIST, EdaName.TIE_POS, EdaName.TIE_POS_RES, EdaName.RANDOM};	
		EdaName[] names = {EdaName.EDIT_DIST};
//		EdaName[] names = {EdaName.TIE_POS};	
	
	for(EdaName name : names)	
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir;
			if (isSingleClusterGS) gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			if (!isSingleClusterGS) clusterDir="";
			File clustGS = new File(gsClusterDir);
			if (!clustGS.isDirectory()) continue;
			System.out.println(gsClusterDir);
				
			ExperimentAlmaPerCluster e = initExperiment(name, tlDir, dataDir+"/"+clusterDir, fileLimit, outDir); 
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
				
				String setting =name +" " + "raw-without-FG "+clusterDir;
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
				combinedExperimet.addResult(setting, confidenceThreshold, res);
				
				setting =name +" " + "raw-with-FG "+clusterDir;
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
				combinedExperimet.addResult(setting, confidenceThreshold, res);

				// now optimize the graph with global optimizer
				setting =name +" " + "global-raw-optimized-FG "+clusterDir;
				EntailmentGraphCollapsed globalOptimizedGraph = e.collapseGraph(rawGraphWithThreshold, globalOptimizer);
				System.out.println("### "+ setting+ "###");
				res = e.evaluateCollapsedGraph(globalOptimizedGraph, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(globalOptimizedGraph);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());
				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				e.addResult(setting, confidenceThreshold, res);
				combinedExperimet.addResult(setting, confidenceThreshold, res);

	
				// now get plusClosure graph for current m_rawGraph (with confidence threshold applied)
				EntailmentGraphRaw plusClosureRawGraph = e.getPlusClosureGraph(rawGraphWithThreshold);
				
				// now optimize the graph with global optimizer
				setting =name +" " + "global-plusClosure-optimized-FG "+clusterDir;
				globalOptimizedGraph = e.collapseGraph(plusClosureRawGraph, globalOptimizer);
				System.out.println("### "+ setting+ "###");
				res = e.evaluateCollapsedGraph(globalOptimizedGraph, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(globalOptimizedGraph);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());
				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				e.addResult(setting, confidenceThreshold, res);
				combinedExperimet.addResult(setting, confidenceThreshold, res);


				setting =name +" " + "plusClosure-raw-with-FG "+clusterDir;
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
				combinedExperimet.addResult(setting, confidenceThreshold, res);

				
				setting =name +" " + "plusClosure-raw-without-FG "+clusterDir;
				System.out.println("### "+ setting+ "###");
				System.out.println(plusClosureRawGraph.toString());
				res = e.evaluateRawGraph(plusClosureRawGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);		
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
				combinedExperimet.addResult(setting, confidenceThreshold, res);
				
				EntailmentGraphCollapsed cgr = e.collapseGraph(plusClosureRawGraph);

				setting =name +" " + "clique-FG "+clusterDir; //plusClosure-collapsed+closure
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
				combinedExperimet.addResult(setting, confidenceThreshold, res);

			
				setting =name +" " + "clique-without-FG "+clusterDir;
				res = e.evaluateCollapsedGraph(cgr, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);
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
				combinedExperimet.addResult(setting, confidenceThreshold, res);
				
				// now optimize the graph with global optimizer
				setting =name +" " + "global-clique-optimized-FG "+clusterDir;
				EntailmentGraphRaw clique = EvaluatorGraphOptimizer.getDecollapsedGraph(cgr);
				for (EntailmentRelation edge : rawGraphWithThreshold.edgeSet()){
					if (edge.getEdgeType().is(EdgeType.FRAGMENT_GRAPH)){
						clique.removeAllEdges(edge.getSource(), edge.getTarget());
						clique.addEdge(edge.getSource(), edge.getTarget(), edge);
					}
				}
				globalOptimizedGraph = e.collapseGraph(clique, globalOptimizer);
				System.out.println("### "+ setting+ "###");
				res = e.evaluateCollapsedGraph(globalOptimizedGraph, gsClusterDir, includeFragmentGraphEdges, isSingleClusterGS);
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(globalOptimizedGraph);
					res.setViolations(consistencyCheck.getViolations());
					res.setExtraFGedges(consistencyCheck.getExtraFGedges());
					res.setMissingFGedges(consistencyCheck.getMissingFGedges());
					res.setEdaCalls(e.getEdaCallsNumber());
				} catch (GraphEvaluatorException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}				
				e.addResult(setting, confidenceThreshold, res);
				combinedExperimet.addResult(setting, confidenceThreshold, res);

			
				setting =name +" " + "global-clique-optimized-without-FG "+clusterDir;
				res = e.evaluateCollapsedGraph(globalOptimizedGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);
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
				combinedExperimet.addResult(setting, confidenceThreshold, res);
			
			}
			

			
			ress+=e.printResults()+"\n";
			
			System.out.println("Done");
			
			if (!isSingleClusterGS) break; // if not by cluster - break after the first run
		}
		try {
			BufferedWriter outWriter = new BufferedWriter(new FileWriter(outDir+"/_NICE_experiment_results.txt"));
			outWriter.write(ress);
			System.out.println("======= AVG =======");
			outWriter.write(combinedExperimet.printAvgResults());
			outWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}

package eu.excitementproject.tl.experiments.NICE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMergerWithNonEntailment;
import eu.excitementproject.tl.composition.graphoptimizer.GlobalGraphOptimizer;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphoptimizer.EvaluatorGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationAndAnalysisMeasures;
import eu.excitementproject.tl.experiments.AbstractExperiment;
import eu.excitementproject.tl.experiments.ResultsContainer;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.utils.ProbabilisticEDA;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;
import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeEDA;
import eu.excitementproject.eop.core.EditDistanceEDA;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;

/** 
 * Class to load NICE data, build the graphs with WP2 merge procedure and evaluate them
 * @author Lili Kotlerman
 * 
 */
public class ExperimentNiceWP2Merge extends AbstractExperiment {
	
	public ExperimentNiceWP2Merge(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
		m_optimizer = new SimpleGraphOptimizer();
		
		try {
			super.useOne.setGraphMerger(new AutomateWP2ProcedureGraphMergerWithNonEntailment(super.lap, super.eda));
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
	
	public static ExperimentNiceWP2Merge initExperiment(EdaName edaName, String tlDir, String dataDir, int fileLimit, String outDir){
		
		if (edaName.equals(EdaName.BIUTEE)) {
			return new ExperimentNiceWP2Merge(
			tlDir+"src/test/resources/NICE_experiments/biutee.xml",
			dataDir, fileLimit, outDir,			
			BIUFullLAP.class,
			BiuteeEDA.class
			);
			
		}
		
		if (edaName.equals(EdaName.EDIT_DIST)) {
			return new ExperimentNiceWP2Merge(
//					tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_NonLexRes_EN.xml",
					tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_EN_nice.xml",
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class,
					EditDistanceEDA.class
					);					
		}

		if (edaName.equals(EdaName.PROBABILISTIC)) {
			return 	new ExperimentNiceWP2Merge(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class, //not used, just some available LAP
					ProbabilisticEDA.class // to assign desired probability go to the EDA code (hard-coded in the beginning)
					);
		}

		if (edaName.equals(EdaName.RANDOM)) {
			return new ExperimentNiceWP2Merge(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class, //not used, just some available LAP
					RandomEDA.class
					);
		}

		if (edaName.equals(EdaName.TIE_PARSE_RES)) {
			return new ExperimentNiceWP2Merge(
			tlDir+"/src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml",
			dataDir, fileLimit, outDir,
			MaltParserEN.class,
			MaxEntClassificationEDA.class
			);			
		}
		
		if (edaName.equals(EdaName.TIE_POS)) {
			return new ExperimentNiceWP2Merge(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml",
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class,
					MaxEntClassificationEDA.class
					);
		}
		
		if (edaName.equals(EdaName.TIE_POS_RES)) {
			return new ExperimentNiceWP2Merge(
			tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",
			dataDir, fileLimit, outDir,
			TreeTaggerEN.class,
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
		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_EMAIL_TEST2_perFrag";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/NICE_EMAIL_reAnnotated/Test2";
		
		int fileLimit = Integer.MAX_VALUE;
		String outDir = dataDir.replace("resources", "outputs");

		ResultsContainer combinedExperimet = new ResultsContainer(); 

		System.out.println(tlDir);
		
		GraphOptimizer globalOptimizer = new GlobalGraphOptimizer();
		
		boolean isSingleClusterGS = true;
		String ress = "";
		File gsDir = new File(gsAnnotationsDir);

//	EdaName[] names = {EdaName.EDIT_DIST, EdaName.TIE_POS, EdaName.TIE_POS_RES, EdaName.RANDOM};	
//		EdaName[] names = {EdaName.TIE_POS_RES};	
//		EdaName[] names = {EdaName.EDIT_DIST};	
//		EdaName[] names = {EdaName.BIUTEE, EdaName.TIE_POS_RES};	
		EdaName[] names = {EdaName.BIUTEE};	
//		EdaName[] names = {EdaName.TIE_POS_RES};	
	
	for(EdaName name : names)	
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir;
			if (isSingleClusterGS) gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			if (!isSingleClusterGS) clusterDir="";
			File clustGS = new File(gsClusterDir);
			if (!clustGS.isDirectory()) continue;
			System.out.println(gsClusterDir);
				
			ExperimentNiceWP2Merge e = initExperiment(name, tlDir, dataDir+"/"+clusterDir, fileLimit, outDir); 
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
				e.m_rawGraph.toXML(outDir+"/"+e.configFile.getName()+"_"+clusterDir+"_rawGraph.xml");
				e.m_rawGraph.toDOT(outDir+"/"+e.configFile.getName()+"_"+clusterDir+"_rawGraph.dot");
			} catch (IOException | EntailmentGraphRawException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			for (Double confidenceThreshold : e.getConfidenceThresholds()){
				// apply threshold
				EntailmentGraphRaw rawGraphWithThreshold = e.applyThreshold(rawGraph, confidenceThreshold);

				// now get plusClosure graph for current rawGraphWithThreshold  - this is the "local" graph for wp2 merging
				String setting =name +" " + "local-without-FG "+clusterDir;
				EntailmentGraphRaw plusClosureRawGraph = e.getPlusClosureGraph(rawGraphWithThreshold);
				EntailmentGraphCollapsed plusClosureCollapsedGraph = e.collapseGraph(plusClosureRawGraph);
				try {
					plusClosureRawGraph.toDOT(outDir+"/"+e.configFile.getName()+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_local_structureBased.dot");
					plusClosureCollapsedGraph.toDOT(outDir+"/"+e.configFile.getName()+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_local_structureBased_collapsed.dot");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				System.out.println("### "+ setting+ "###");
				System.out.println(plusClosureRawGraph.toString());
				EvaluationAndAnalysisMeasures res = e.evaluateRawGraph(plusClosureRawGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);		
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
				
				// compare with and without collapsing 
				System.out.println(clusterDir);
				compareClosureAndDecollapsed(plusClosureRawGraph.edgeSet(), EvaluatorGraphOptimizer.getDecollapsedGraph(plusClosureCollapsedGraph).edgeSet());

				
				// now optimize the local graph with global optimizer
				setting =name +" " + "local+global-without-FG "+clusterDir;
				EntailmentGraphCollapsed globalOptimizedGraph = e.collapseGraph(plusClosureRawGraph, globalOptimizer);
				
				try {
					globalOptimizedGraph.toDOT(outDir+"/"+e.configFile.getName()+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_local+global_structureBased_collapsed.dot");
				} catch (IOException  e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}								
				
				System.out.println("### "+ setting+ "###");
				res = e.evaluateCollapsedGraph(globalOptimizedGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);
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
			}
			

			
			ress+=e.printResults()+"\n";
			
			System.out.println("Done");
			
			if (!isSingleClusterGS) break; // if not by cluster - break after the first run
		}
		try {
			BufferedWriter outWriter = new BufferedWriter(new FileWriter(outDir+"/_NICE_experiment_results.txt"));
			outWriter.write(ress);
			System.out.println("======= Error examples =======");
			outWriter.write(combinedExperimet.printErrorExamples(5));			
			System.out.println("======= AVG =======");
			outWriter.write(combinedExperimet.printAvgResults());
			outWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

}

package eu.excitementproject.tl.experiments.NICE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.alignmentedas.p1eda.sandbox.FNR_EN;
import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeEDA;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.EditDistanceEDA;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.GlobalGraphOptimizer;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.edautils.ProbabilisticEDA;
import eu.excitementproject.tl.edautils.RandomEDA;
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

/** 
 * Class to load NICE data, build the graphs and evaluate them
 * @author Lili Kotlerman
 * 
 */
public class ExperimentNice extends AbstractExperiment {
	
	public String configFileFullName = "";
	public String configFileName = "";
	
	public ExperimentNice(String configFileFullName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		super(configFileFullName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
	
		this.configFileFullName = configFileFullName;
		this.configFileName = (new File(configFileFullName)).getName();
		
		m_optimizer = new SimpleGraphOptimizer();
		
		try {
//			super.setGraphMerger(new AllPairsGraphMerger(super.lap, super.eda));
//			super.setGraphMerger(new AllPairsGraphMergerWithNonEntailments(super.lap, super.eda));
			super.setGraphMerger(new AutomateWP2ProcedureGraphMerger(super.lap, super.eda));
//			super.setGraphMerger(new AutomateWP2ProcedureGraphMerger(super.lap, super.eda));
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
		EDIT_DIST,
		P1EDA
	}
	
	public static ExperimentNice initExperiment(EdaName edaName, String tlDir, String dataDir, int fileLimit, String outDir) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException{
		
		if (edaName.equals(EdaName.BIUTEE)) {
			return new ExperimentNice(
			tlDir+"src/test/resources/NICE_experiments/biutee.xml",
			dataDir, fileLimit, outDir,			
			BIUFullLAP.class,
			BiuteeEDA.class
			);
			
		}
		
		if (edaName.equals(EdaName.EDIT_DIST)) {
			return new ExperimentNice(
//					tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_NonLexRes_EN.xml",
//					tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_EN_nice.xml",
					tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_EN_nice_linux.xml",
					
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class,
					EditDistanceEDA.class
					);					
		}

		if (edaName.equals(EdaName.PROBABILISTIC)) {
			return 	new ExperimentNice(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class, //not used, just some available LAP
					ProbabilisticEDA.class // to assign desired probability go to the EDA code (hard-coded in the beginning)
					);
		}

		if (edaName.equals(EdaName.RANDOM)) {
			return new ExperimentNice(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class, //not used, just some available LAP
					RandomEDA.class
					);
		}

		if (edaName.equals(EdaName.TIE_PARSE_RES)) {
			return new ExperimentNice(
			tlDir+"/src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml",
			dataDir, fileLimit, outDir,
			MaltParserEN.class,
			MaxEntClassificationEDA.class
			);			
		}
		
		if (edaName.equals(EdaName.TIE_POS)) {
			return new ExperimentNice(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml",
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class,
					MaxEntClassificationEDA.class
					);
		}
		
		if (edaName.equals(EdaName.TIE_POS_RES)) {
			return new ExperimentNice(
			tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",
			dataDir, fileLimit, outDir,
			TreeTaggerEN.class,
			MaxEntClassificationEDA.class
			);
		}
	
		if (edaName.equals(EdaName.P1EDA)) {
			return new ExperimentNice(
					tlDir+"src/test/resources/EOP_configurations/P1EDA_Base_EN.xml",
					dataDir, fileLimit, outDir,
					TreeTaggerEN.class,
					FNR_EN.class
					);
		}
		
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String tlDir = "./";
		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_reAnnotated/perFrag/test";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/NICE_reAnnotated/test";
		
		int fileLimit = 10;
		String outDir = dataDir.replace("resources", "outputs");

		ResultsContainer combinedExperimet = new ResultsContainer(); 

		System.out.println(tlDir);
	//	System.out.println(System.getProperties());
		
		GraphOptimizer globalOptimizer = new GlobalGraphOptimizer();
		
		boolean isSingleClusterGS = true;
		String ress = "";
		File gsDir = new File(gsAnnotationsDir);

//	EdaName[] names = {EdaName.EDIT_DIST, EdaName.TIE_POS, EdaName.TIE_POS_RES, EdaName.RANDOM};	
//		EdaName[] names = {EdaName.TIE_POS_RES};	
		EdaName[] names = {EdaName.EDIT_DIST};	
//		EdaName[] names = {EdaName.BIUTEE, EdaName.TIE_POS_RES};	
//		EdaName[] names = {EdaName.BIUTEE};	
//		EdaName[] names = {EdaName.TIE_POS};	
	
	for(EdaName name : names)	
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir;
			if (isSingleClusterGS) gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			if (!isSingleClusterGS) clusterDir="";
			File clustGS = new File(gsClusterDir);
			if (!clustGS.isDirectory()) continue;
			System.out.println(gsClusterDir);
				
			ExperimentNice e = null;
			EntailmentGraphRaw rawGraph = null;
			try {
				e = initExperiment(name, tlDir, dataDir+"/"+clusterDir, fileLimit, outDir);
				rawGraph = e.buildRawGraph(0.95);
				
				e.m_rawGraph.toXML(outDir+"/"+e.configFileName+"_"+clusterDir+"_rawGraph.xml");
				e.m_rawGraph.toDOT(outDir+"/"+e.configFileName+"_"+clusterDir+"_rawGraph.dot");
				
			} catch (ConfigurationException | NoSuchMethodException
					| SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | EDAException
					| ComponentException | FragmentAnnotatorException
					| ModifierAnnotatorException | GraphMergerException
					| GraphOptimizerException | FragmentGraphGeneratorException
					| IOException | EntailmentGraphRawException
					| TransformerException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} 
			
			
			for (Double confidenceThreshold : e.getConfidenceThresholds()){
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
				
/*				setting =name +" " + "raw-with-FG "+clusterDir;
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
*/
				// now optimize the graph with global optimizer
				EntailmentGraphCollapsed globalOptimizedGraph = e.collapseGraph(rawGraphWithThreshold, globalOptimizer);

/*				setting =name +" " + "global-raw-optimized-FG "+clusterDir;
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
*/
	
				setting =name +" " + "global-raw-optimized-without-FG "+clusterDir;
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

				// now get plusClosure graph for current m_rawGraph (with confidence threshold applied)
				EntailmentGraphRaw plusClosureRawGraph = e.getPlusClosureGraph(rawGraphWithThreshold);
				
				// now optimize the graph with global optimizer
				setting =name +" " + "global-plusClosure-optimized-without-FG "+clusterDir;
				globalOptimizedGraph = e.collapseGraph(plusClosureRawGraph, globalOptimizer);
				
				if (confidenceThreshold.equals(0.5)||(confidenceThreshold.equals(0.9500000000000004)))
				try {
					globalOptimizedGraph.toXML(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_globalPlusClosure.xml");
					globalOptimizedGraph.toDOT(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_globalPlusClosure.dot");
				} catch (IOException | EntailmentGraphCollapsedException | TransformerException e1) {
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


/*				setting =name +" " + "plusClosure-raw-with-FG "+clusterDir;
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
*/
				
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

				cgr.applyTransitiveClosure(false);
				for (EntailmentRelationCollapsed cole : cgr.edgeSet()){
					if (!cole.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) cole.setEdgeType(EdgeType.UNKNOWN);
				}
				System.out.println(cgr.toString());

				if (confidenceThreshold.equals(0.5)||(confidenceThreshold.equals(0.9500000000000004)))
				try {
					cgr.toXML(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_cgr.xml");
					cgr.toDOT(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_cgr.dot");
				} catch (IOException | EntailmentGraphCollapsedException | TransformerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
/*				setting =name +" " + "clique-FG "+clusterDir; //plusClosure-collapsed+closure
				System.out.println("### "+ setting+ "###");
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
*/
			
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
				EntailmentGraphRaw clique = EvaluatorGraphOptimizer.getDecollapsedGraph(cgr);
				for (EntailmentRelation edge : rawGraphWithThreshold.edgeSet()){
					if (edge.getEdgeType().is(EdgeType.FRAGMENT_GRAPH)){
						clique.removeAllEdges(edge.getSource(), edge.getTarget());
						clique.addEdge(edge.getSource(), edge.getTarget(), edge);
					}
				}				
				
				globalOptimizedGraph = e.collapseGraph(clique, globalOptimizer);

/*				setting =name +" " + "global-clique-optimized-FG "+clusterDir;
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
*/
			
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

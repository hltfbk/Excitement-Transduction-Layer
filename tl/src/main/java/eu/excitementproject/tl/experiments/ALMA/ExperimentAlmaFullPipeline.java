package eu.excitementproject.tl.experiments.ALMA;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerIT;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.graphmerger.StructureBasedGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.GlobalGraphOptimizer;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.edautils.ProbabilisticEDA;
import eu.excitementproject.tl.edautils.RandomEDA;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphoptimizer.EvaluatorGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationAndAnalysisMeasures;
import eu.excitementproject.tl.experiments.AbstractExperimentNotAnnotXMIs;
import eu.excitementproject.tl.experiments.ResultsContainer;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;


/** 
 * Full pipeline experiment for ALMA (starting from XMIs with only the interaction text and possibly keyword annotations)
 * 
 * @author vivi@fbk & Lili Kotlerman -- very very little edited copy of Lili's ExperimentAlma class
 * 
 */
public class ExperimentAlmaFullPipeline extends AbstractExperimentNotAnnotXMIs {
	
	public String configFileFullName = "";
	public String configFileName = "";
	
	/**
	 * Constructor for the Experiment class based on the configuration file, the input data directory, file number limit for processing, output folder, the LAP and EDA classes
	 * 
	 * @param configFileFullName -- the full path for the configuration file for the EDA
	 * @param dataDir -- data with input XMIs
	 * @param fileNumberLimit -- limit on the number of files to process
	 * @param outputFolder
	 * @param lapClass -- LAP class (will override the one specified in the EDA's configuration file)
	 * @param edaClass -- EDA class (will override the one specified in the EDA's configuration file)
	 * 
	 * @throws ConfigurationException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws EDAException
	 * @throws ComponentException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws GraphMergerException
	 * @throws GraphOptimizerException
	 * @throws FragmentGraphGeneratorException
	 * @throws IOException
	 * @throws EntailmentGraphRawException
	 * @throws TransformerException
	 * @throws ClassNotFoundException 
	 */
	public ExperimentAlmaFullPipeline(String configFileFullName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException {
		
		super(configFileFullName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		
		this.configFileFullName = configFileFullName;
		this.configFileName = (new File(configFileFullName)).getName();

		m_optimizer = new SimpleGraphOptimizer();
		
		try {
//			super.setGraphMerger(new AllPairsGraphMerger(super.lap, super.eda));
//			super.setGraphMerger(new AllPairsGraphMergerWithNonEntailments(super.lap, super.eda));
			super.setGraphMerger(new StructureBasedGraphMerger(super.lap, super.eda));
//			super.setGraphMerger(new NoEdaGraphMerger(super.lap, super.eda));
			
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Constructor for the Experiment class based on the configuration file, input data directory, limit on the number of files to process, and outout folder
	 * 
	 * @param configFileFullName -- configuration file for the EDA. The EDA and LAP classes will be taken from the file
	 * @param dataDir -- directory with input XMIs
	 * @param fileNumberLimit
	 * @param outputFolder
	 * 
	 * @throws ConfigurationException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws EDAException
	 * @throws ComponentException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws GraphMergerException
	 * @throws GraphOptimizerException
	 * @throws FragmentGraphGeneratorException
	 * @throws IOException
	 * @throws EntailmentGraphRawException
	 * @throws TransformerException
	 * @throws ClassNotFoundException 
	 */
	public ExperimentAlmaFullPipeline(String configFileFullName, String dataDir,
			int fileNumberLimit, String outputFolder) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException {
		
		super(configFileFullName, dataDir, fileNumberLimit, outputFolder);
		
		this.configFileFullName = configFileFullName;
		this.configFileName = (new File(configFileFullName)).getName();

		m_optimizer = new SimpleGraphOptimizer();
		
		try {
//			super.setGraphMerger(new AllPairsGraphMerger(super.lap, super.eda));
//			super.setGraphMerger(new AllPairsGraphMergerWithNonEntailments(super.lap, super.eda));
			super.setGraphMerger(new StructureBasedGraphMerger(super.lap, super.eda));
//			super.setGraphMerger(new NoEdaGraphMerger(super.lap, super.eda));
			
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static ExperimentAlmaFullPipeline initExperiment(EdaName edaName, String tlDir, String dataDir, int fileLimit, String outDir) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException{
		
		if (edaName.equals(EdaName.EDIT_DIST)) {
			return new ExperimentAlmaFullPipeline(
					tlDir+"src/test/resources/NICE_experiments/EditDistanceEDA_NonLexRes_EN.xml",
//					tlDir+"src/test/resources/EOP_configurations/EditDistanceEDA_IT_WordNet.xml",
					dataDir, fileLimit, outDir);					
		}

		if (edaName.equals(EdaName.PROBABILISTIC)) {
			return 	new ExperimentAlmaFullPipeline(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerIT.class, //not used, just some available LAP
					ProbabilisticEDA.class // to assign desired probability go to the EDA code (hard-coded in the beginning)
					);
		}

		if (edaName.equals(EdaName.RANDOM)) {
			return new ExperimentAlmaFullPipeline(
					tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml", //not used, just some existing conf file
					dataDir, fileLimit, outDir,
					TreeTaggerIT.class, //not used, just some available LAP
					RandomEDA.class
					);
		}

		
		if (edaName.equals(EdaName.TIE_POS)) {
			return new ExperimentAlmaFullPipeline(
					tlDir+"src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_IT.xml",
					dataDir, fileLimit, outDir);
		}
		
		if (edaName.equals(EdaName.P1EDA)) {
			return new ExperimentAlmaFullPipeline(
					tlDir+"src/test/resources/EOP_configurations/P1EDA_Base_IT.xml",
					dataDir, fileLimit, outDir);
		}
		
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// ============= SET UP THE EXPERIMENT CONFIGURATION ============
		String tlDir = "./";
		String dataDir = tlDir+"src/main/resources/exci/alma/xmi_perFragmentGraph/test";
		String gsAnnotationsDir = tlDir+"src/main/resources/exci/alma/goldStandardAnnotation/test";
		int fileLimit = 1000000;
		String outDir = dataDir.replace("resources", "outputs");

		MergerType mergerType = MergerType.WP2_MERGE; // which merger to use
		boolean includeFragmentGraphEdges = true; // whether to include FG edges in the evaluations
		
		// which EDA(s) to use
		//	EdaName[] names = {EdaName.EDIT_DIST, EdaName.TIE_POS, EdaName.TIE_POS_RES, EdaName.RANDOM};	
		EdaName[] names = {EdaName.P1EDA};
		//		EdaName[] names = {EdaName.TIE_POS};	
		
		// ===== END OF SET-UP

		ResultsContainer combinedExperimet = new ResultsContainer(); 
		System.out.println(tlDir);
	//	System.out.println(System.getProperties());
		GraphOptimizer globalOptimizer = new GlobalGraphOptimizer();
		boolean isSingleClusterGS = true;
		String ress = "";
		File gsDir = new File(gsAnnotationsDir);
	
	for(EdaName name : names)	
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir;
			if (isSingleClusterGS) gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			if (!isSingleClusterGS) clusterDir="";
			File clustGS = new File(gsClusterDir);
			if (!clustGS.isDirectory()) continue;
			System.out.println(gsClusterDir);
				
			ExperimentAlmaFullPipeline e = null;
			EntailmentGraphRaw rawGraph = null;
			try {
				e = initExperiment(name, tlDir, dataDir+"/"+clusterDir, fileLimit, outDir);
				e.setFragmentGraphGenerator(new FragmentGraphLiteGeneratorFromCAS());
				rawGraph = e.buildRawGraph();

				e.m_rawGraph.toXML(outDir+"/"+e.configFileName +"_rawGraph.xml");
				e.m_rawGraph.toDOT(outDir+"/"+e.configFileName +"_rawGraph.dot");

				
			} catch (ConfigurationException | NoSuchMethodException
					| SecurityException | InstantiationException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | EDAException
					| ComponentException | FragmentAnnotatorException
					| ModifierAnnotatorException | GraphMergerException
					| GraphOptimizerException | FragmentGraphGeneratorException
					| IOException | EntailmentGraphRawException
					| TransformerException | ClassNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} 
				
			
			for (Double confidenceThreshold : e.getConfidenceThresholds()){
				// apply threshold - this is the "local" graph
				EntailmentGraphRaw rawGraphWithThreshold = e.applyThreshold(rawGraph, confidenceThreshold);
				EntailmentGraphCollapsed rawGraphWithThresholdCollapsed = e.collapseGraph(rawGraphWithThreshold);

				String setting = e.getSettingName(name, "local", includeFragmentGraphEdges, gsClusterDir); 
				if (mergerType.equals(MergerType.WP2_MERGE)) rawGraphWithThreshold.applyTransitiveClosure();  // for wp2 tr.closure is part of merging, it is added by the merger, but applying threshold ruins it, so I add it again  explicitly
				try {
					rawGraphWithThreshold.toDOT(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_local_"+mergerType+".dot");
					rawGraphWithThresholdCollapsed.toDOT(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_local_"+mergerType+"_collapsed.dot");
				} catch (EntailmentGraphCollapsedException | EntailmentGraphRawException e1) {
					e1.printStackTrace();
				}
				
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

				if (mergerType.equals(MergerType.ALL_PAIRS_MERGE)){
					// now get plusClosure graph for current rawGraphWithThreshold  - this is the "localClosure" graph for all-pair merging
					setting = e.getSettingName(name, "localClosure", includeFragmentGraphEdges, gsClusterDir); 
					EntailmentGraphRaw plusClosureRawGraph = e.getPlusClosureGraph(rawGraphWithThreshold);
					EntailmentGraphCollapsed plusClosureCollapsedGraph = e.collapseGraph(plusClosureRawGraph);
					try {
						plusClosureRawGraph.toDOT(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_localClosure_"+mergerType+".dot");
						plusClosureCollapsedGraph.toDOT(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_localClosure_"+mergerType+"_collapsed.dot");
					} catch (EntailmentGraphCollapsedException | EntailmentGraphRawException e1) {
						e1.printStackTrace();
					}
					
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
					combinedExperimet.addResult(setting, confidenceThreshold, res);	
					
					// now optimize the localClosure graph with global optimizer
					setting = e.getSettingName(name, "localClosure+global", includeFragmentGraphEdges, gsClusterDir);				EntailmentGraphCollapsed localClosureGloballyOptimizedGraph = e.collapseGraph(plusClosureRawGraph, globalOptimizer);					
					try {
						localClosureGloballyOptimizedGraph.toDOT(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_localClosure+global_"+mergerType+"_collapsed.dot");
					} catch (EntailmentGraphCollapsedException  e1) {
						e1.printStackTrace();
					}
					
					System.out.println("### "+ setting+ "###");
					res = e.evaluateCollapsedGraph(localClosureGloballyOptimizedGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);
					System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
					try {
						EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(localClosureGloballyOptimizedGraph);
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
				
				
				// now optimize the local graph with global optimizer
				setting = e.getSettingName(name, "local+global", includeFragmentGraphEdges, gsClusterDir);
				EntailmentGraphCollapsed localGloballyOptimizedGraph = e.collapseGraph(rawGraphWithThreshold, globalOptimizer);
				
				try {
					localGloballyOptimizedGraph.toDOT(outDir+"/"+e.configFileName+"_"+clusterDir+"_"+confidenceThreshold.toString()+"_local+global_"+mergerType+"_collapsed.dot");
				} catch (EntailmentGraphCollapsedException  e1) {
					e1.printStackTrace();
				}
				
				System.out.println("### "+ setting+ "###");
				res = e.evaluateCollapsedGraph(localGloballyOptimizedGraph, gsClusterDir, !includeFragmentGraphEdges, isSingleClusterGS);
				System.out.println(setting+"\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
				try {
					EvaluationAndAnalysisMeasures consistencyCheck = e.checkGraphConsistency(localGloballyOptimizedGraph);
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
			
				// addition for full-pipeline collapsed-graph evaluation
				EvaluatorGraphOptimizer collapsedEvaluator = new EvaluatorGraphOptimizer();
				EntailmentGraphCollapsed collapsedGoldStandardGraph = e.getCollapdedGSClusterGraph(gsClusterDir); //e.collapseGraph(e.gsloader.getRawGraph(), 0.0, new SimpleGraphOptimizer());
				collapsedEvaluator.evaluateEdges(collapsedGoldStandardGraph.vertexSet(), localGloballyOptimizedGraph.vertexSet(), collapsedGoldStandardGraph.edgeSet(), localGloballyOptimizedGraph.edgeSet(), true);

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

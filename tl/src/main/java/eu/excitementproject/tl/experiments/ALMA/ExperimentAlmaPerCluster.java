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
import eu.excitementproject.eop.core.EditDistanceEDA;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerIT;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.graphmerger.StructureBasedGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.GlobalGraphOptimizer;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.ModifierDependencyAnnotator;
import eu.excitementproject.tl.edautils.ProbabilisticEDA;
import eu.excitementproject.tl.edautils.RandomEDA;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphoptimizer.EvaluatorGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationAndAnalysisMeasures;
import eu.excitementproject.tl.experiments.AbstractExperiment;
import eu.excitementproject.tl.experiments.ResultsContainer;
import eu.excitementproject.tl.laputils.DependencyLevelLapIT;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;


/** 
 * Class to load ALMA data, build the graphs and evaluate them
 * @author Lili Kotlerman
 * 
 */
@SuppressWarnings("unused")
public class ExperimentAlmaPerCluster extends AbstractExperiment {
	
	public String configFileFullName = "";
	public String configFileName = "";
	
	
	/**
	 * Exposed ModifierAnnotator to add dependency relations between (gold standard) modifiers 
	 * 
	 * @param configFileFullName
	 * @param dataDir
	 * @param fileNumberLimit
	 * @param outputFolder
	 * @param lapClass
	 * @param edaClass
	 * @param modAnot
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
	 */
	public ExperimentAlmaPerCluster(String configFileFullName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass, ModifierAnnotator modAnot) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		
		super(configFileFullName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass, modAnot);
		
		this.configFileFullName = configFileFullName;
		this.configFileName = (new File(configFileFullName)).getName();

		m_optimizer = new SimpleGraphOptimizer();
		
		try {
			super.setGraphMerger(new StructureBasedGraphMerger(super.lap, super.eda));
			
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor from configuration file (which contains EDA and LAP classes)
	 * 
	 * @param configFileFullName -- EDA's configuration file (that contains the EDA and LAP classes)
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
	public ExperimentAlmaPerCluster(String configFileFullName, String dataDir,
			int fileNumberLimit, String outputFolder) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException {
		
		super(configFileFullName, dataDir, fileNumberLimit, outputFolder);
		
		this.configFileFullName = configFileFullName;
		this.configFileName = (new File(configFileFullName)).getName();

		m_optimizer = new SimpleGraphOptimizer();
		
		try {
			super.setGraphMerger(new StructureBasedGraphMerger(super.lap, super.eda));
			
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Constructor from configuration file (that includes EDA and LAP classes), and using explicit ModifierAnnotator
	 * 
	 * @param configFileFullName -- configuration file for the EDA (contains the EDA and LAP classes)
	 * @param dataDir -- directory with input XMIs
	 * @param fileNumberLimit
	 * @param outputFolder
	 * @param modAnot -- modifier annotator 
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
	public ExperimentAlmaPerCluster(String configFileFullName, String dataDir,
			int fileNumberLimit, String outputFolder, ModifierAnnotator modAnot) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException {
		
		super(configFileFullName, dataDir, fileNumberLimit, outputFolder, modAnot);
		
		this.configFileFullName = configFileFullName;
		this.configFileName = (new File(configFileFullName)).getName();

		m_optimizer = new SimpleGraphOptimizer();
		
		try {
			super.setGraphMerger(new StructureBasedGraphMerger(super.lap, super.eda));
			
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public ExperimentAlmaPerCluster(String configFileFullName, String dataDir,
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
			super.setGraphMerger(new StructureBasedGraphMerger(super.lap, super.eda));
//			super.setGraphMerger(new NoEdaGraphMerger(super.lap, super.eda));
			
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public enum EdaName{
		RANDOM,
		PROBABILISTIC,
		TIE_POS,
		EDIT_DIST,
		P1EDA
	}
	
	public static ExperimentAlmaPerCluster initExperiment(EdaName edaName, String tlDir, String dataDir, int fileLimit, String outDir) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException{
		
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
		
		if (edaName.equals(EdaName.P1EDA)) {
			return new ExperimentAlmaPerCluster(
					tlDir+"src/test/resources/EOP_configurations/P1EDA_Base_IT.xml",
					dataDir, fileLimit, outDir
					,new ModifierDependencyAnnotator(new DependencyLevelLapIT())
					);
		}
		
		return null;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String tlDir = "./";
		String dataDir = tlDir+"src/main/resources/exci/alma/xmi_perFragmentGraph/test/";
		String gsAnnotationsDir = tlDir+"src/main/resources/exci/alma/goldStandardAnnotation/test/";
		
		int fileLimit = 1000000;
		String outDir = dataDir.replace("resources", "outputs");

		ResultsContainer combinedExperimet = new ResultsContainer(); 

		System.out.println(tlDir);
	//	System.out.println(System.getProperties());
		
		GraphOptimizer globalOptimizer = new GlobalGraphOptimizer();
		
		boolean isSingleClusterGS = true;
		String ress = "";
		File gsDir = new File(gsAnnotationsDir);
		
		boolean includeFragmentGraphEdges = true; // whether to include FG edges in the evaluations
		
//	EdaName[] names = {EdaName.EDIT_DIST, EdaName.TIE_POS, EdaName.TIE_POS_RES, EdaName.RANDOM};	
		EdaName[] names = {EdaName.P1EDA};
//		EdaName[] names = {EdaName.TIE_POS};	
	
	for(EdaName name : names)	
		for (String clusterDir : gsDir.list()){
			String gsClusterDir = gsAnnotationsDir;
			if (isSingleClusterGS) gsClusterDir = gsAnnotationsDir+"/"+clusterDir;
			if (!isSingleClusterGS) clusterDir="";
			File clustGS = new File(gsClusterDir);
			if (!clustGS.isDirectory()) continue;
			System.out.println(gsClusterDir);
				
			ExperimentAlmaPerCluster e = null;
			EntailmentGraphRaw rawGraph = null;
			try {
				e = initExperiment(name, tlDir, dataDir+"/"+clusterDir, fileLimit, outDir);
//				e.setFragmentGraphGenerator(new FragmentGraphLiteGeneratorFromCAS());
				e.setFragmentGraphGenerator(new FragmentGraphGeneratorFromCAS());

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

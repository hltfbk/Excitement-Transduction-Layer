package eu.excitementproject.tl.experiments.NICE;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeEDA;
//import eu.excitementproject.eop.core.DKProSimilaritySimpleEDA;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.utils.XMLFileWriter;

public class Experiment extends AbstractExperiment {

	public Experiment(String configFileName, String dataDir,
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

		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";
		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/NICE_open_trainTest_byClusterSplit/test";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/NICE_open_trainTest_byClusterSplit/test";
		int fileLimit = 1000;
		String outDir = dataDir.replace("resources", "outputs").replace("test", "");
		
	/*	Experiment eTIEpos = new Experiment(
				tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml",

				dataDir, fileLimit, outDir,

				TreeTaggerEN.class,
				MaxEntClassificationEDA.class
				);
*/		

		Experiment eTIEposRes = new Experiment(
				tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",

				dataDir, fileLimit, outDir,

				TreeTaggerEN.class,
				MaxEntClassificationEDA.class
				);

				
/*		Experiment eTIEparsedRes = new Experiment(
				tlDir+"/src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml",

				dataDir, fileLimit, outDir,
				
				MaltParserEN.class,
				MaxEntClassificationEDA.class
				);
*/
		
/*		Experiment eBIUTEE = new Experiment(
				tlDir+"src/test/resources/NICE_experiments/biutee_wp6.xml",
				
				dataDir, fileLimit, outDir,
				
				BIUFullLAP.class,
				BiuteeEDA.class
				);
*/		
		
		/*		//TODO: find what lap to use + find the conf files + what EDA to use (simple vs classifier)
		Experiment eDKPro = new Experiment(
		"D:/EOPspace/eop-resources-1.0.2/configuration-files/biutee.xml",

		"./src/test/resources/WP2_public_data_CAS_XMI/NICE_open", 19,
		"/home/nastase/Projects/eop/excitement-transduction-layer/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS XMI/nice_email 1",
		???.class,
		DKProSimilaritySimpleEDA.class
		);
*/
			
		Experiment e = eTIEposRes; 
		e.buildRawGraph();
		try {
			e.m_rawGraph.toXML(outDir+"/"+e.configFile.getName()+"_rawGraph.xml");
			e.m_rawGraph.toDOT(outDir+"/"+e.configFile.getName()+"_rawGraph.dot");
		} catch (IOException | TransformerException | EntailmentGraphRawException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		boolean includeFragmentGraphEdges = true;

		//TODO Verify why FG edges are not found in the graph. Is it only for closure edges? 
	//	System.out.println(gr);
		for (double confidenceThreshold=0.5; confidenceThreshold<1; confidenceThreshold+=0.05){
			EvaluationMeasures res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph, gsAnnotationsDir, !includeFragmentGraphEdges);		
			System.out.println("raw without FG\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			EntailmentGraphCollapsed cgr= e.collapseGraph(confidenceThreshold);
			res = e.evaluateRawGraph(confidenceThreshold, e.m_rawGraph, gsAnnotationsDir, includeFragmentGraphEdges);		
			System.out.println("raw with FG\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
			res = e.evaluateCollapsedGraph(cgr, gsAnnotationsDir);
			System.out.println("collapsed\t"+confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());			
			try {
				cgr.toXML(outDir+"/"+e.configFile.getName()+String.valueOf(confidenceThreshold)+"_collapsedGraph.xml");
				cgr.toDOT(outDir+"/"+e.configFile.getName()+String.valueOf(confidenceThreshold)+"_collapsedGraph.dot");
			} catch (EntailmentGraphCollapsedException | TransformerException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		System.out.println("Done");
		
	}

}

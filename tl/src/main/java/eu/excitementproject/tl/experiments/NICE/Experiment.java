package eu.excitementproject.tl.experiments.NICE;

//import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeEDA;
//import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeMain;
//import eu.excitementproject.eop.core.DKProSimilaritySimpleEDA;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class Experiment extends AbstractExperiment {

	public Experiment(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder, Class<?> lapClass,
			Class<?> edaClass) {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/*			Experiment eTIEpos = new Experiment(
//				"./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml",
				"./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+WN+VO_EN.xml",

				"./src/test/resources/WP2_public_data_CAS_XMI/NICE_open", 1,
				"/home/nastase/Projects/eop/excitement-transduction-layer/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS XMI/nice_email 1",
				TreeTaggerEN.class,
				MaxEntClassificationEDA.class
				);
		
	
		Experiment eTIEparsed = new Experiment(
				"./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml",

				"./src/test/resources/WP2_public_data_CAS_XMI/NICE_open", 19,
				"/home/nastase/Projects/eop/excitement-transduction-layer/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS XMI/nice_email 1",
				MaltParserEN.class,
				MaxEntClassificationEDA.class
				);

		Experiment eDKPro = new Experiment(
				"D:/EOPspace/eop-resources-1.0.2/configuration-files/biutee.xml",

				"./src/test/resources/WP2_public_data_CAS_XMI/NICE_open", 19,
				"/home/nastase/Projects/eop/excitement-transduction-layer/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS XMI/nice_email 1",
				BIUFullLAP.class,
				DKProSimilaritySimpleEDA.class
				);*/
		
		Experiment eBIUTEE = new Experiment(
//				"./src/test/resources/EOP_configurations/biutee_wp6.xml",
				"D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/resources/EOP_configurations/biutee_wp6.xml",

				"D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/resources/WP2_public_data_CAS_XMI/nice_email_3", 2,
				"D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS XMI/nice_email_3",
				BIUFullLAP.class,
				MaxEntClassificationEDA.class
				);
		
		
		Experiment e = eBIUTEE; 
		String gsAnnotationsDir = "./src/test/resources/WP2_gold_standard_annotation/NICE_open";
		boolean includeFragmentGraphEdges = false;
		EntailmentGraphRaw gr = e.buildRawGraph();
		
		//TODO Verify why FG edges are not found in the graph. Is it only for closure edges? 
	//	System.out.println(gr);
		for (double confidenceThreshold=0.5; confidenceThreshold<1; confidenceThreshold+=0.05){
			EvaluationMeasures res = e.evaluateRawGraph(confidenceThreshold, gr, gsAnnotationsDir, includeFragmentGraphEdges);
			System.out.println(confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
		}
	}

}

package eu.excitementproject.tl.experiments.NICE;

import eu.excitementproject.eop.biutee.rteflow.systems.excitement.BiuteeEDA;
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

		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";
		
/*		Experiment eTIEpos = new Experiment(
				tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base_EN.xml",

				tlDir+"/src/test/resources/WP2_public_data_CAS_XMI/nice_email_3", 15,
				tlDir+"src/test/outputs/WP2_public_data_CAS XMI/nice_email_3",

				TreeTaggerEN.class,
				MaxEntClassificationEDA.class
				);
*/		

/*		Experiment eTIEposRes = new Experiment(
				tlDir+"src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",

				tlDir+"/src/test/resources/WP2_public_data_CAS_XMI/nice_email_3", 15,
				tlDir+"src/test/outputs/WP2_public_data_CAS XMI/nice_email_3",

				TreeTaggerEN.class,
				MaxEntClassificationEDA.class
				);
*/
				
/*		Experiment eTIEparsedRes = new Experiment(
				tlDir+"/src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml",

				tlDir+"/src/test/resources/WP2_public_data_CAS_XMI/nice_email_3", 15,
				tlDir+"src/test/outputs/WP2_public_data_CAS XMI/nice_email_3",
				
				MaltParserEN.class,
				MaxEntClassificationEDA.class
				);
*/
		
		Experiment eBIUTEE = new Experiment(
				tlDir+"src/test/resources/NICE_experiments/biutee_wp6.xml",
				
				tlDir+"src/test/resources/WP2_public_data_CAS_XMI/nice_email_3", 15,
				tlDir+"src/test/outputs/WP2_public_data_CAS XMI/nice_email_3",
				
				BIUFullLAP.class,
				BiuteeEDA.class
				);
		
		/*		//TODO: find what lap to use + find the conf files + what EDA to use (simple vs classifier)
		Experiment eDKPro = new Experiment(
		"D:/EOPspace/eop-resources-1.0.2/configuration-files/biutee.xml",

		"./src/test/resources/WP2_public_data_CAS_XMI/NICE_open", 19,
		"/home/nastase/Projects/eop/excitement-transduction-layer/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS XMI/nice_email 1",
		???.class,
		DKProSimilaritySimpleEDA.class
		);
*/
			
		Experiment e = eBIUTEE; 
		EntailmentGraphRaw gr = e.buildRawGraph();
		
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/NICE_open";
		boolean includeFragmentGraphEdges = false;

		//TODO Verify why FG edges are not found in the graph. Is it only for closure edges? 
	//	System.out.println(gr);
		for (double confidenceThreshold=0.5; confidenceThreshold<1; confidenceThreshold+=0.05){
			EvaluationMeasures res = e.evaluateRawGraph(confidenceThreshold, gr, gsAnnotationsDir, includeFragmentGraphEdges);
			System.out.println(confidenceThreshold+"\t"+res.getRecall()+"\t"+res.getPrecision()+"\t"+res.getF1());
		}
		System.out.println("Done");
	}

}

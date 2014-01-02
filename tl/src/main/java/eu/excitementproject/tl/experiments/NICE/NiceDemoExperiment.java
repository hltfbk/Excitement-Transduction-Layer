package eu.excitementproject.tl.experiments.NICE;

import java.io.File;

import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.demo.UseCaseOneDemo;
import eu.excitementproject.tl.evaluation.graphmerger.EvaluatorGraphMerger;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class NiceDemoExperiment  {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
/*		String configFileName = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml";
        String inputsDir = "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_3";
        String outputFolder = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS_XMI/nice_email_3";
                                       
//        int fileNumberLimit = getNumberOfFilesFromInputsDir(inputsDir);
        int fileNumberLimit = 33;
        
        UseCaseOneDemo demoEN = new UseCaseOneDemo(configFileName, inputsDir, fileNumberLimit, outputFolder,
        		TreeTaggerEN.class,
				MaxEntClassificationEDA.class);
        demoEN.inspectResults();
*/        

		String o1 = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS_XMI/nice_email_3_out1/";
		String o2 = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS_XMI/nice_email_3_out2/";
		try {
			EntailmentGraphRaw out1 = new EntailmentGraphRaw(new File(o1+"raw_graph.xml"));
			EntailmentGraphRaw out2 = new EntailmentGraphRaw(new File(o2+"raw_graph.xml"));
			
			System.out.println(EvaluatorGraphMerger.evaluate(out1.edgeSet(), out2.edgeSet(), false));
		} catch (EntailmentGraphRawException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

package eu.excitementproject.tl.experiments.NICE;

import eu.excitementproject.eop.core.MaxEntClassificationEDA;
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
		Experiment e = new Experiment("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml",
				"./src/test/resources/WP2_public_data_CAS_XMI/NICE_open", 15,
				"/home/nastase/Projects/eop/excitement-transduction-layer/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS XMI/nice_email 1",
				TreeTaggerEN.class,
				MaxEntClassificationEDA.class
				);
		
		String gsAnnotationsDir = "./src/test/resources/WP2_gold_standard_annotation/NICE_open";
		boolean includeFragmentGraphEdges = false;
		EntailmentGraphRaw gr = e.buildRawGraph();
		EvaluationMeasures res = e.evaluateRawGraph(gr, gsAnnotationsDir, includeFragmentGraphEdges);
		System.out.println(res.toString());
		
	}

}

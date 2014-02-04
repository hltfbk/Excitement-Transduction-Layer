package eu.excitementproject.tl.experiments.ALMA;


import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.experiments.AbstractExperiment;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;

/**
 * Class to load ALMA data, build the graphs and evaluate them
 * @author Lili Kotlerman
 *
 */
public class ExperimentAlma extends AbstractExperiment {

	public ExperimentAlma(String configFileName, String dataDir,
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

//		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";
		String tlDir = "/home/nastase/Projects/eop/excitement-transduction-layer/Excitement-Transduction-Layer/tl/";
		String dataDir = tlDir+"src/test/resources/WP2_public_data_CAS_XMI/ALMA_social_media/";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/ALMA_social_media_mergedGs/";
		int fileLimit = 1000;
		String outDir = dataDir.replace("resources", "outputs");
		
		
		String conf = tlDir+"src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_IT.xml";
		Class<?> lapClass = TreeTaggerEN.class;
		Class<?> edaClass = MaxEntClassificationEDA.class;

		ExperimentAlma e = new ExperimentAlma(conf, dataDir, fileLimit, outDir, lapClass, edaClass);			
		e.buildRawGraph();
		try {
			e.m_rawGraph.toXML(outDir+"/"+e.configFile.getName()+"_rawGraph.xml");
		} catch (TransformerException | EntailmentGraphRawException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		boolean includeFragmentGraphEdges = true;

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
			} catch (EntailmentGraphCollapsedException | TransformerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		System.out.println("Done");
		
	}

}

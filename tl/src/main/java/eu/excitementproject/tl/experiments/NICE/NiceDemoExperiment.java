package eu.excitementproject.tl.experiments.NICE;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.demo.UseCaseOneDemo;
import eu.excitementproject.tl.evaluation.graphmerger.EvaluatorGraphMerger;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

/**
 * Tmp class for the stuff needed to support NICE demo development
 * @author Lili Kotlerman
 *
 */
public class NiceDemoExperiment  {

	private static final Logger logger = Logger.getLogger(NiceDemoExperiment.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFileName = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml";
        String inputsDir = "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_3";
        String outputFolder = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS_XMI/nice_email_3";
                                       
        int fileNumberLimit = 33;
        
        UseCaseOneDemo demoEN = new UseCaseOneDemo(configFileName, inputsDir, fileNumberLimit, outputFolder,
        		TreeTaggerEN.class,
				MaxEntClassificationEDA.class);
        demoEN.inspectResults();
        

		String o1 = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS_XMI/nice_email_3/";
		String o2 = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS_XMI/nice_email_3_sort_out3/";
		try {
			EntailmentGraphRaw out1 = new EntailmentGraphRaw(new File(o1+"raw_graph.xml"));
			EntailmentGraphRaw out2 = new EntailmentGraphRaw(new File(o2+"raw_graph.xml"));
			
			logger.info(EvaluatorGraphMerger.evaluate(out1.edgeSet(), out2.edgeSet(), false));
			
			Set<String> o1e = new HashSet<String>();			
			Set<String> o2e = new HashSet<String>();
			for (EntailmentRelation edge : out1.edgeSet()){
				if (!edge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) o1e.add(EvaluatorGraphMerger.getSourceAndTargetString(edge)); 
			}
			for (EntailmentRelation edge : out2.edgeSet()){
				if (!edge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) o2e.add(EvaluatorGraphMerger.getSourceAndTargetString(edge));
			}
			logger.info(o1e.size()+"\t"+o2e.size());
			o1e.removeAll(o2e);
			logger.info(o1e.size());
		/*	for (String e : o1e){
				logger.info(e);
			}*/
			
				
		} catch (EntailmentGraphRawException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

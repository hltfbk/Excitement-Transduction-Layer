package eu.excitementproject.tl.experiments.NICE;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.evaluation.graphmerger.EvaluatorGraphMerger;
import eu.excitementproject.tl.laputils.DataUtils;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

/**
 * Tmp class for the stuff needed to support NICE demo development
 * @author Lili Kotlerman
 *
 */
public class NiceDemoExperiment  {

	private static final Logger logger = Logger.getLogger(NiceDemoExperiment.class);

	/**
	 * @param args
	 * @throws GraphMergerException 
	 * @throws ModifierAnnotatorException 
	 * @throws FragmentAnnotatorException 
	 * @throws ComponentException 
	 * @throws EDAException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws ConfigurationException 
	 * @throws TransformerException 
	 * @throws EntailmentGraphRawException 
	 * @throws IOException 
	 * @throws FragmentGraphGeneratorException 
	 * @throws GraphOptimizerException 
	 * @throws EntailmentGraphCollapsedException 
	 */
	public static void main(String[] args) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, EntailmentGraphCollapsedException {
		String configFileName = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml";
        String inputsDir = "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_3";
        String outputFolder = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/outputs/WP2_public_data_CAS_XMI/nice_email_3";
                                       
        int fileNumberLimit = 33;
        
        UseCaseOneRunnerPrototype demoEN = new UseCaseOneRunnerPrototype(configFileName, outputFolder,
        		TreeTaggerEN.class,
				MaxEntClassificationEDA.class);
        List<JCas> docs = DataUtils.loadData(inputsDir, fileNumberLimit);
        
		EntailmentGraphCollapsed graph = demoEN.buildCollapsedGraph(docs);
		demoEN.inspectGraph(graph);
                

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

package eu.excitementproject.tl.demo;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;
import eu.excitementproject.tl.toplevel.usecasetworunner.UseCaseTwoRunnerPrototype;

/**
 * Shows OMQ use case data flow. 
 * 
 * @param args
 * @throws FragmentAnnotatorException
 * @throws ModifierAnnotatorException
 * @throws FragmentGraphGeneratorException
 * @throws NodeMatcherException
 * @throws CategoryAnnotatorException
 * @throws LAPException 
 */
public class DemoUseCase2OMQGerman {
	
	public static void main(String[] args) throws FragmentAnnotatorException, ModifierAnnotatorException, 
		FragmentGraphGeneratorException, NodeMatcherException, CategoryAnnotatorException, LAPException, EntailmentGraphRawException, IOException, TransformerException, ParserConfigurationException {

		File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml");		
		CommonConfig config = null;
		LAPAccess lap;
		EDABasic<?> eda;
		UseCaseOneRunnerPrototype use1;
		UseCaseTwoRunnerPrototype use2;
		EntailmentGraphRaw graph = null;

		/** Step 1: Building an entailment graph from existing data */
		
		//Read in files
		String fileName = "OMQ_dummy_data_small.xml";
		String[] files = {"./src/test/resources/WP2_public_data_XML/" + fileName,};
		File f;
		Set<Interaction> docs = new HashSet<Interaction>();

		try {
			for (String name: files) {
				f = new File(name); 
				docs.addAll(InteractionReader.readInteractionXML(f));
			}
			// initialize the lap			
			lap = new TreeTaggerEN();
			
			// initialize the eda			
			config = new ImplCommonConfig(configFile);
			eda = new MaxEntClassificationEDA();	
			eda.initialize(config);
			
			// initialize use case one runner
			use1 = new UseCaseOneRunnerPrototype(lap, eda, "./src/test/outputs/");
			
			// build raw graph
			graph = use1.buildRawGraph(docs);
			graph.toDOT("./src/test/outputs/graph_"+ fileName + ".txt");

			use1.inspectGraph(graph);
		
			/** Step 2: Annotating an incoming email based on the entailment graph */
			//create some sample input
			JCas cas = CASUtils.createNewInputCas();
			cas.setDocumentLanguage("DE");
			cas.setDocumentText("Es ist bereits registriert.");
			
			// initialize use case two runner
			use2 = new UseCaseTwoRunnerPrototype(lap, eda);
			use2.annotateCategories(cas, graph);
			
			//print CAS
			CASUtils.dumpCAS(cas);

		} catch (ConfigurationException | EDAException | ComponentException | 
				FragmentAnnotatorException | FragmentGraphGeneratorException | 
				ModifierAnnotatorException | 
				GraphMergerException | DataReaderException e) {
			e.printStackTrace();
		}		
	}

}

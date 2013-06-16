package eu.excitementproject.tl.usecaseone;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.junit.Test;

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
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

public class UseCaseOneTest {

	@Test
	public void test(){
		File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml");		
		CommonConfig config = null;
		LAPAccess lap;
		EDABasic<?> eda;
		UseCaseOneRunnerPrototype use1;
		EntailmentGraphCollapsed graph;
		
		List<JCas> docs = new ArrayList<JCas>();
		String[] files = {"./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/100771.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/183009.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/213033.txt.xmi"
		};
		
		File f;
		JCas aJCas;

		try {

		
			for (String name: files) {
		
				f = new File(name); 
				aJCas = CASUtils.createNewInputCas(); 
				CASUtils.deserializeFromXmi(aJCas, f); 

				docs.add(aJCas);
			}
						
			// initialize the lap			
			lap = new TreeTaggerEN();
			
			// initialize the eda			
			config = new ImplCommonConfig(configFile);
			eda = new MaxEntClassificationEDA();	
			eda.initialize(config);
			
			// initialize use case one runner
			use1 = new UseCaseOneRunnerPrototype(lap, eda);
			
			// build collapsed graph
			graph = use1.buildCollapsedGraph(docs);

			UseCaseOneRunnerPrototype.inspectGraph(graph);
			
		} catch (ConfigurationException | EDAException | ComponentException | 
				FragmentAnnotatorException | FragmentGraphGeneratorException | 
				ModifierAnnotatorException | 
				GraphMergerException | CollapsedGraphGeneratorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

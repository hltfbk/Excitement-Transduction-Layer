package eu.excitementproject.tl.usecaseone;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
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

	public static void main(String[] argv){
		
		// turning on Log4J, with INFO level logs 
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		
		File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml");		
		CommonConfig config = null;
		LAPAccess lap;
		EDABasic<?> eda;
		UseCaseOneRunnerPrototype use1;
		EntailmentGraphCollapsed graph;
		
		List<JCas> docs = new ArrayList<JCas>();
/*		String[] files = {"./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/100771.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/183009.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/213033.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/218023.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/228632.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/327999.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/334406.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/415044.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/427082.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/427784.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/431092.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/443092.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/44805.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/450618.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/469143.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/470062.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/475555.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/478371.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/483394.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/512034.txt.xmi",
						  "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/72187.txt.xmi"
		};
*/		
		
		File dir = new File("./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1");
		
//		File f;
		JCas aJCas;

		try {

		
//			for (String name: files) {
			for (File f : dir.listFiles()) {
		
//				f = new File(name); 
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

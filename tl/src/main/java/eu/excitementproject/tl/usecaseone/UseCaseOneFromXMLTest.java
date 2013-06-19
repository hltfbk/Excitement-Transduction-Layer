package eu.excitementproject.tl.usecaseone;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


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
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

public class UseCaseOneFromXMLTest {

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
		
		Set<Interaction> docs = new HashSet<Interaction>();
//		String[] files = {"./src/test/resources/WP2_public_data_XML/D2.1.1 English-Email.xml"
//						//	,
//						// "./src/test/resources/WP2_public_data_XML/D2.1.1 English-Speech.xml"
//		};
		
		// with small example - Gil
		String[] files = {"./src/test/resources/WP2_public_data_XML/nice_email1_first_10.xml",};
		
		File f;

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
			use1 = new UseCaseOneRunnerPrototype(lap, eda);
			
			// build collapsed graph
			graph = use1.buildCollapsedGraph(docs);

			UseCaseOneRunnerPrototype.inspectGraph(graph);
			
		} catch (ConfigurationException | EDAException | ComponentException | 
				FragmentAnnotatorException | FragmentGraphGeneratorException | 
				ModifierAnnotatorException | 
				GraphMergerException | CollapsedGraphGeneratorException | DataReaderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

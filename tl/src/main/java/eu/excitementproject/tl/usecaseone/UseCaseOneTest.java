package eu.excitementproject.tl.usecaseone;

import java.io.File;
import java.io.IOException;
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
		int fileNumberLimit = 100000000;
		
//		File f;
		JCas aJCas;

		try {

			int i =0;
			for (File f : dir.listFiles()) {
				i++; 
				if (i>fileNumberLimit) break;
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
			
			// prepare the output folder
			String outputFolder = "./src/test/outputs/"+dir.getPath().replace(".\\src\\test\\resources\\","").replace("\\","/");
			File theDir = new File(outputFolder);
  		    // if the directory does not exist, create it
		    if (!theDir.exists())
		    {
		      System.out.println("creating directory: " + outputFolder);
		      boolean result = theDir.mkdir();  
		      if(result){    
		         System.out.println("DIR created");  
		     }
		      else {
		    	  System.err.println("Could not create the output directory. No output files will be created."); 
		    	  outputFolder=null;
		      }
		   }

		    // initialize use case one runner
			use1 = new UseCaseOneRunnerPrototype(lap, eda, outputFolder);
			
			// build collapsed graph
			graph = use1.buildCollapsedGraph(docs);

			use1.inspectGraph(graph);
			
		} catch (ConfigurationException | EDAException | ComponentException | 
				FragmentAnnotatorException | FragmentGraphGeneratorException | 
				ModifierAnnotatorException | 
				GraphMergerException | CollapsedGraphGeneratorException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

package eu.excitementproject.tl.demo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
import eu.excitementproject.eop.lap.dkpro.OpenNLPTaggerEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.demo.DemoUseCase1NICEEnglish;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

@SuppressWarnings("unused")
public class UseCaseOneDemo {

	protected File configFile;
	protected CommonConfig config = null;
	protected LAPAccess lap;
	protected EDABasic<?> eda;
	protected UseCaseOneRunnerPrototype useOne;
	protected EntailmentGraphCollapsed graph;
	
	public UseCaseOneDemo(String configFileName, String dataDir, String outputFolder, Class<?> lapClass, Class<?> edaClass) {
		
		// turning on Log4J, with INFO level logs 
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		
		try {
			configFile = new File(configFileName);
		
			List<JCas> docs = loadData(dataDir);

			initializeLap(lapClass);
			initializeEDA(edaClass);

			// prepare the output folder
			File theDir = new File(outputFolder);
			// if the directory does not exist, create it
			if (!theDir.exists())
			{
		      System.out.println("creating directory: " + outputFolder);
		      boolean result = theDir.mkdir();  
		      if(result){    
		         System.out.println("DIR created");  
		      } else {
		    	  System.err.println("Could not create the output directory. No output files will be created."); 
		    	  outputFolder=null;
		      }
			}

			// initialize use case one runner
			useOne = new UseCaseOneRunnerPrototype(lap, eda, outputFolder);
			
			// build collapsed graph
			graph = useOne.buildCollapsedGraph(docs);
			
		} catch (ConfigurationException | EDAException | ComponentException | 
				FragmentAnnotatorException | FragmentGraphGeneratorException | 
				ModifierAnnotatorException | TransformerException | ParserConfigurationException | 
				GraphMergerException | CollapsedGraphGeneratorException | IOException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initializeLap(Class<?> lapClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// initialize the lap
		Constructor<?> lapClassConstructor = lapClass.getConstructor();
		lap = (LAPAccess) lapClassConstructor.newInstance();
	}
	
	
	private void initializeEDA(Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException {
		// initialize the eda			
		config = new ImplCommonConfig(configFile);
		Constructor<?> edaClassConstructor = edaClass.getConstructor();
		eda = (EDABasic<?>) edaClassConstructor.newInstance();
		eda.initialize(config);		
	}


	private List<JCas> loadData(String dataDir) {
	
		List<JCas> docs = new ArrayList<JCas>();
		File dir = new File(dataDir);
		int fileNumberLimit = 3;

		//File f;
		JCas aJCas;

		try {
			int i =0;
			for (File f : dir.listFiles()) {
				i++; 
				if (i>fileNumberLimit) break;
				//		f = new File(name); 
				aJCas = CASUtils.createNewInputCas(); 
				CASUtils.deserializeFromXmi(aJCas, f); 
				docs.add(aJCas);
			}
		} catch (Exception e) {
			System.out.println("Problems loading data from directory " + dataDir);
			e.printStackTrace();
		}
		return docs;
	}
	
	public void inspectResults() {
		try {
			useOne.inspectGraph(graph);
		} catch (IOException | TransformerException
				| ParserConfigurationException e) {
			// TODO Auto-generated catch block
			System.out.println("Error inspecting results");
			e.printStackTrace();
		}
	}
	

	public static void main(String[] argv) {
		
		String configFileName = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml";
		String dataDir = "./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1";
		String outputFolder = "./src/test/outputs/"+dataDir.replace(".\\src\\test\\resources\\","").replace("\\","/");
		
		UseCaseOneDemo demoEN = new UseCaseOneDemo(configFileName, dataDir, outputFolder, TreeTaggerEN.class, MaxEntClassificationEDA.class);
		demoEN.inspectResults();
	}
	
}

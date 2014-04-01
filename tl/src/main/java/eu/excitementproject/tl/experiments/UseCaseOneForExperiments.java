package eu.excitementproject.tl.experiments;

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
import eu.excitementproject.eop.common.utilities.configuration.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
import eu.excitementproject.eop.lap.dkpro.OpenNLPTaggerEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.demo.DemoUseCase1NICEEnglish;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

@SuppressWarnings("unused")
public class UseCaseOneForExperiments {

	protected File configFile;
	protected CommonConfig config = null;
	protected CachedLAPAccess lap;
	protected EDABasic<?> eda;
	protected UseCaseOneRunnerPrototype useOne;
	protected EntailmentGraphCollapsed graph;
	protected List<JCas> docs;
	
	public UseCaseOneForExperiments(String configFileName, String dataDir, int fileNumberLimit, String outputFolder, Class<?> lapClass, Class<?> edaClass) {
		
		// turning on Log4J, with INFO level logs 
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		
		try {
			configFile = new File(configFileName);
			config = new ImplCommonConfig(configFile);
			
			docs = loadData(dataDir, fileNumberLimit);

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
						
		} catch (ConfigurationException | EDAException | ComponentException | 
				FragmentAnnotatorException |  
				ModifierAnnotatorException | 
				GraphMergerException | IOException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initializeLap(Class<?> lapClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// initialize the lap
		LAPAccess lapAc = null;
		if (lapClass.getName().contains("BIUFullLAP")){			
			try {
				//Constructor<?> lapClassConstructor = lapClass.getConstructor(CommonConfig.class);
				//lapAc = (LAPAccess) lapClassConstructor.newInstance(config);
				lapAc = new BIUFullLAP(config); 
			} catch (ConfigurationException | LAPException e) {
				e.printStackTrace();
			}
		}
		else{ // if not BIUFullLAP
			Constructor<?> lapClassConstructor = lapClass.getConstructor();
			lapAc = (LAPAccess) lapClassConstructor.newInstance();
		}

		try {
			lap = new CachedLAPAccess(lapAc);
		} catch (LAPException e) {
			e.printStackTrace();
		}
	}
	
	
	private void initializeEDA(Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException {
		// initialize the eda			
		Constructor<?> edaClassConstructor = edaClass.getConstructor();
		eda = (EDABasic<?>) edaClassConstructor.newInstance();
		eda.initialize(config);		
	}


	private List<JCas> loadData(String dataDir, int fileNumberLimit) {
	
		List<JCas> docs = new ArrayList<JCas>();
		File dir = new File(dataDir);
	//	int fileNumberLimit = 4; //commented by Lili 30.06 - now exposed in the constuctor

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
		} catch (IOException | TransformerException | EntailmentGraphCollapsedException e) {
			// TODO Auto-generated catch block
			System.out.println("Error inspecting results");
			e.printStackTrace();
		}
	}		
	
}

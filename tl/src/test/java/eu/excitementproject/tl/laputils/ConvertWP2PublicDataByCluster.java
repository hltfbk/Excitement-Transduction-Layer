package eu.excitementproject.tl.laputils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ConvertWP2PublicDataByCluster extends ConvertWP2PublicData{

	private static final Logger logger = Logger.getLogger(ConvertWP2PublicDataByCluster.class);

	/**
	 * This class reads WP2 fragment graph dump data from the /test/resources directory, 
	 * and generates InputCAS and store them as XMI (serialized CAS). 
	 * 
	 * <P> The stored XMI files can be read into InputCAS (a JCAS) by calling 
	 * CASUtils.deserializeFromXmi() 
	 * 
	 * <P> TODO: (update accordingly) Note that, the reader (InteractionReader.readWP2FragGraphDump()) for the moment only reads and generates continuous fragments. It will skip all non-continuous fragment annotations. New versions will be able to read such, after prototype. 
	 * 
	 * @param args no arguments will be processed 
	 * 
	 * @author Gil / Vivi@fbk / LiliKotlerman
	 */
	public static void main(String[] args) {

		// log4j setting
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.WARN);  

		int totalcount = 0; 
		Path dirFragmentGraphs = null; 
		Path dirInteractions = null; 
		Path outputdir = null; 
		Path outputdirPerFrag = null; 

		// This is the usage example. 
		// Use "processWP2Data()" for per-interaction XMI file generation. 
		// Use "processWP2DataPerFragment()" for per-fragment XMI file generation. 
		
		// DIR prepare:  
		// File names will be determined by "interaction name" (processWP2Data()), or 
		// "fragment XML name" (processWP2DataPerFramgnet()) 
		
		String clustersDirName = "./src/main/resources/exci/alma/goldStandardAnnotation/dev";
		
		String outputDirName = "./src/test/outputs/alma/goldStandardAnnotation/dev";
		File clustersDir = new File(clustersDirName);
		
		String language = "EN"; 
		
		for(String cluster: clustersDir.list()) {
			
			dirFragmentGraphs = Paths.get(clustersDirName + "/" + cluster + "/FragmentGraphs");
			dirInteractions = Paths.get(clustersDirName + "/" + cluster + "/Interactions");
		
			logger.info(dirFragmentGraphs.toFile().getAbsolutePath());
			outputdir = Paths.get(outputDirName+"_perInteraction/"+cluster);
			outputdirPerFrag = Paths.get(outputDirName + "_perFrag/" + cluster);
		
			// Actual call: use this for "per-fragment" XMI saving 
			totalcount += processWP2DataPerFragment(dirFragmentGraphs, dirInteractions, outputdirPerFrag, language); 
		
			// Actual call: Use this, for "per-interaction" XMI saving. 
			totalcount += processWP2Data(dirFragmentGraphs, dirInteractions, outputdir, language); 

			logger.info("Cummulative count: " + totalcount + " XMI files generated, over /target/ directories"); 
		}

		logger.info("In total: " + totalcount + " XMI files generated, over /target/ directories"); 
	}
	

	


}

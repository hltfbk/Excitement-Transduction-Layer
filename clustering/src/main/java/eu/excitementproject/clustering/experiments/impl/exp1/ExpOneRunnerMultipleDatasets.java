/**
 * 
 */
package eu.excitementproject.clustering.experiments.impl.exp1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFileDuplicateKeyException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;

/**
 * @author Lili Kotlerman
 *
 */
public class ExpOneRunnerMultipleDatasets {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	/*	String confDir = "./configurations/biu/exp1_noExpansion";
		String outdir = "./src/test/outputs/exp1_noExpansion";*/


/*		String confDir = "./configurations/biu/exp1_withWN";
		String outdir = "./src/test/outputs/exp1_withWN";*/
	
		
	/*	String confDir = "./configurations/biu/exp1_withWN_and_bap";
		String outdir = "./src/test/outputs/exp1_withWN_and_bap";*/
		
		
			// exp
		String confDir = args[0]; //".configurations/biu/exp2_noExpansion";
		String outdir = args[1];  //"./src/test/outputs/exp2_noExpansion";


		String current;
		try {
			current = new java.io.File( "." ).getCanonicalPath();
			System.out.println("Current dir:"+current);	
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        	

		File ldaDir = new File(outdir+"/lda");
		String dataDir = null;
						
		File configurationFile = new File(confDir+"/conf_multi.xml");

		System.out.println(configurationFile.getAbsolutePath());
		System.out.println(ldaDir.getAbsolutePath());
		
		try {
			ConfigurationFile conf = new ConfigurationFile(configurationFile);
			if(conf.isModuleExist("Data")){
				ConfigurationParams params = conf.getModuleConfiguration("Data");
				if(params.containsKey("data_dir")) dataDir = params.getString("data_dir");
				else throw new ConfigurationException("Configuration file must contain data_dir parameter in module Data to use this class");
			}			

			System.out.println(dataDir);

			BufferedWriter writer = new BufferedWriter(new FileWriter (new File(outdir+"/"+configurationFile.getName().replace(".xml", ".log.txt"))));
			BufferedWriter writer_ignore = new BufferedWriter(new FileWriter (new File(outdir+"/"+configurationFile.getName().replace(".xml", "._ignoreNonClass_log.txt"))));
			BufferedWriter writer_top30Percent = new BufferedWriter(new FileWriter (new File(outdir+"/"+configurationFile.getName().replace(".xml", "._top30PercentCutOff_log.txt"))));
			
			BufferedWriter expanWriter = new BufferedWriter(new FileWriter (new File(outdir+"/"+configurationFile.getName().replace(".xml", ".expansions.txt"))));
			for (File dataFile : new File(dataDir).listFiles()){
				if (!ldaDir.exists()) ldaDir.mkdir();
				if (dataFile.getName().contains("_domainVocab")) continue; // only read text collections, not domain vocabulary files
				if (dataFile.getName().endsWith(".txt")){
					System.out.println(dataFile.getAbsolutePath());					
					writer.write("=====================\n"+dataFile.getAbsolutePath()+"\n=====================\n");
					writer_ignore.write("=====================\n"+dataFile.getAbsolutePath()+"\n=====================\n");
					writer_top30Percent.write("=====================\n"+dataFile.getAbsolutePath()+"\n=====================\n");					
					expanWriter.write("=====================\n"+dataFile.getAbsolutePath()+"\n=====================\n");
					writer.flush();
					writer_ignore.flush();
					writer_top30Percent.flush();
					expanWriter.flush();
					ExpOneRunner exp = new ExpOneRunner(configurationFile.getAbsolutePath(), dataFile.getAbsolutePath());
					writer.write(exp.aboutDataset());
					writer_ignore.write(exp.aboutDataset());
					writer_top30Percent.write(exp.aboutDataset());
					expanWriter.write(exp.aboutDataset());
					expanWriter.write(exp.getSemanticRelatednessForPrint());
					exp.runExperiment(configurationFile.getAbsolutePath());
					writer.write(exp.printAllResults(0));
					writer.write(exp.printResultsInTable(0));
					writer.write(exp.printRecallPrecisionCurvesData(0));	

					writer_ignore.write(exp.printAllResults(2));
					writer_ignore.write(exp.printResultsInTable(2));
					writer_ignore.write(exp.printRecallPrecisionCurvesData(2));	
					
					writer_top30Percent.write(exp.printAllResults(1));
					writer_top30Percent.write(exp.printResultsInTable(1));
					writer_top30Percent.write(exp.printRecallPrecisionCurvesData(1));	

					// rename the lda directory
					File newLdaDir = new File(ldaDir.getAbsolutePath()+"_"+dataFile.getName().replace(".txt",""));
					if (ldaDir.isDirectory()) {
						ldaDir.renameTo(newLdaDir);
					}
					
				}				
			}
			writer.close();
			writer_ignore.close();
			writer_top30Percent.close();
			expanWriter.close();
		} catch (ConfigurationFileDuplicateKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LemmatizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}

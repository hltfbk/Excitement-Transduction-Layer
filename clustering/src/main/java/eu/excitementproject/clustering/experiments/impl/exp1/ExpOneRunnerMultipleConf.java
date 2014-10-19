/**
 * 
 */
package eu.excitementproject.clustering.experiments.impl.exp1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;

/**
 * @author Lili Kotlerman
 *
 */
public class ExpOneRunnerMultipleConf {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String confDir = "./configurations/biu/exp1_noExpansion";
		String outdir = "./src/test/outputs/exp1_noExpansion";
				
		for (File configurationFile : new File(confDir).listFiles()){
			if (configurationFile.getName().endsWith(".xml")){
				try {
					ExpOneRunner exp = new ExpOneRunner(configurationFile.getAbsolutePath());
					BufferedWriter writer = new BufferedWriter(new FileWriter (new File(outdir+"/"+configurationFile.getName().replace(".xml", ".log.txt"))));
					exp.runExperiment(configurationFile.getAbsolutePath());
					writer.write(exp.printAllResults(0));
					writer.write(exp.printRecallPrecisionCurvesData(0));
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LemmatizerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}

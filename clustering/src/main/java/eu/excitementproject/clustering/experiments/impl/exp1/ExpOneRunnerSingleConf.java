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
public class ExpOneRunnerSingleConf {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String outdir = "./src/test/outputs/exp1_noExpansion";
				
		File annotationFile = new File(args[0]);
		try {
			ExpOneRunner exp = new ExpOneRunner(annotationFile.getAbsolutePath());
			BufferedWriter writer = new BufferedWriter(new FileWriter (new File(outdir+"/"+annotationFile.getName().replace(".xml", ".log.txt"))));
			exp.runExperiment(args[0]);
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
		
		
/*			/////// test lemmatization
			ExcitementTextCollection coll = (ExcitementTextCollection) exp.m_textCollection;
			System.out.println(coll.getLemma("counterclaims"));
			System.out.println(coll.getLemma("killing"));
			System.out.println(coll.getLemma("killed"));
			System.out.println(coll.getLemma("cloudy"));
			System.out.println(coll.getLemma("credit cards"));
			System.out.println(coll.getLemma("credits cards"));
			System.out.println(coll.getLemma("vegetarian foods"));
			System.out.println(coll.getLemma("vegetarian eating"));*/
	}
}

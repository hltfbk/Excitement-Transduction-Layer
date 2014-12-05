package eu.excitementproject.tl.demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

/**
 * Demo class for building entailment graphs from ALMA data using all annotations from the gold-standard 
 * fragment XMIs
 * 
 * @author Vivi Nastase
 *
 */
public class DemoUseCaseOneFromXMIsALMAItalian {
	

	public static void main(String[] argv) {
	
		String configFileName = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_IT.xml";
		String dataDir = "./src/test/resources/WP2_public_data_CAS_XMI/ALMA_social_media";
		int fileNrLimit = 4;
		String outputFolder = "./src/test/outputs/WP2_public_data_CAS_XMI/ALMA_social_media";
		
		UseCaseOneFromXMIs demoIT;

		try {

			demoIT = new UseCaseOneFromXMIs(configFileName, dataDir, fileNrLimit, outputFolder);
			demoIT.inspectResults();

		} catch (ConfigurationException 
				| SecurityException 
				| IllegalArgumentException
				| ComponentException
				| FragmentAnnotatorException | ModifierAnnotatorException
				| GraphMergerException | GraphOptimizerException
				| FragmentGraphGeneratorException | IOException
				| EntailmentGraphRawException | TransformerException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | EDAException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

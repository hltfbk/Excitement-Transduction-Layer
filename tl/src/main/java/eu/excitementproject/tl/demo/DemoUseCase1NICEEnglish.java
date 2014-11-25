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
 * Demo class for graph building for use case 1 
 * Starts with annotated XMIs (that have fragments and modifier annotations)
 * 
 * It reads the EDA and LAP to use from the configuration file.
 * 
 * @author vivi@fbk
 *
 */
public class DemoUseCase1NICEEnglish {

	
	
	public static void main(String[] argv) {
				
		// to run for a different cluster change the cluster name
		String configFileName = "./src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml";
		String dataDir = "./src/main/resources/exci/nice/xmi_perFragmentGraph/test/EMAIL0001";
		int fileNumberLimit = 100;
		String outputFolder = "./src/test/outputs/WP2_public_data_CAS_XMI/NICE_reAnnotated/perFrag/test/EMAIL0001";

		UseCaseOneFromXMIs useOne;
		try {
			
			useOne = new UseCaseOneFromXMIs(configFileName, dataDir, fileNumberLimit, outputFolder);
			useOne.inspectResults();

		} catch (ConfigurationException | SecurityException | IllegalArgumentException
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

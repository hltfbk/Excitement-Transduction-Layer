package eu.excitementproject.tl.demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

/**
 * Demo class for building entailment graphs from NICE data using all annotations from the gold-standard fragment XMIs
 * 
 * @author vivi@fbk
 *
 */
public class DemoUseCaseOneFromXMIsNICEEnglish {

	public static void main(String[] argv) {
				
		// to run for a different cluster change the cluster name
		String configFileName = "./src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml";
		String dataDir = "./src/main/resources/exci/nice/xmi_perFragmentGraph/all/EMAIL0001";
		int fileNumberLimit = 4;
		String outputFolder = "./src/test/outputs/WP2_public_data_CAS_XMI/NICE_reAnnotated/perFrag/test/EMAIL0001";

		UseCaseOneFromXMIs useOne;
		
		try {
			
			useOne = new UseCaseOneFromXMIs(configFileName, dataDir, fileNumberLimit, outputFolder);
			useOne.inspectGraph(useOne.graph);

		} catch (ConfigurationException 
				| SecurityException 
				| IllegalArgumentException
				| ComponentException
				| FragmentAnnotatorException | ModifierAnnotatorException
				| GraphMergerException | GraphOptimizerException
				| FragmentGraphGeneratorException | IOException
				| EntailmentGraphRawException | TransformerException | EntailmentGraphCollapsedException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | EDAException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	
}

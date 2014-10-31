package eu.excitementproject.tl.demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.LemmaLevelLapIT;

public class DemoUseCaseOneFromXMIsALMAItalian extends UseCaseOneFromXMIs {
	
	
	public DemoUseCaseOneFromXMIsALMAItalian(String configFileName,
			String dataDir, int fileNumberLimit, String outputFolder,
			Class<?> lapClass, Class<?> edaClass)
			throws ConfigurationException, NoSuchMethodException,
			SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, EDAException,
			ComponentException, FragmentAnnotatorException,
			ModifierAnnotatorException, GraphMergerException,
			GraphOptimizerException, FragmentGraphGeneratorException,
			IOException, EntailmentGraphRawException, TransformerException {
		
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass,
				edaClass);
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] argv) {
	
		String configFileName = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_IT.xml";
		String dataDir = "./src/test/resources/WP2_public_data_CAS_XMI/ALMA_social_media";
		int fileNrLimit = 4;
		String outputFolder = "./src/test/outputs/WP2_public_data_CAS_XMI/ALMA_social_media";
		Class lapClass = LemmaLevelLapIT.class;
		Class edaClass = MaxEntClassificationEDA.class;
		
		DemoUseCaseOneFromXMIsALMAItalian demoIT;

		try {

			demoIT = new DemoUseCaseOneFromXMIsALMAItalian(configFileName, dataDir, fileNrLimit, outputFolder, lapClass, edaClass);
			demoIT.inspectGraph(demoIT.graph);

		} catch (ConfigurationException | NoSuchMethodException
				| SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | EDAException | ComponentException
				| FragmentAnnotatorException | ModifierAnnotatorException
				| GraphMergerException | GraphOptimizerException
				| FragmentGraphGeneratorException | IOException
				| EntailmentGraphRawException | TransformerException | EntailmentGraphCollapsedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

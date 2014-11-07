package eu.excitementproject.tl.demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;

import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

public class DemoUseCaseOneFromNotAnnotatedXMIsNICEEnglish extends UseCaseOneFromNotAnnotatedXMIs{

	public DemoUseCaseOneFromNotAnnotatedXMIsNICEEnglish(String configFileName, String dataDir, int fileNumberLimit, String outputFolder, Class<?> lapClass, Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass, edaClass);
	}
	
	
	public static void main(String[] argv) {

		// run the flow, create raw and collapsed entailment graphs and save them to files
		DemoUseCaseOneFromNotAnnotatedXMIsNICEEnglish demoEN;
		try {

			demoEN = new DemoUseCaseOneFromNotAnnotatedXMIsNICEEnglish("./src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",
					"./src/main/resources/exci/nice/xmi_perFragmentGraph/EMAIL0001", 100,
					"./src/test/outputs/WP2_public_data_CAS_XMI/NICE_reAnnotated/perFrag/test/EMAIL0001",
					TreeTaggerEN.class,
					MaxEntClassificationEDA.class);
			demoEN.inspectGraph(demoEN.graph);

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

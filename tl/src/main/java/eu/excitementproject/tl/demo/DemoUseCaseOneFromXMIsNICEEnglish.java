package eu.excitementproject.tl.demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.uima.jcas.JCas;

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
import eu.excitementproject.tl.laputils.DataUtils;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

public class DemoUseCaseOneFromXMIsNICEEnglish extends UseCaseOneRunnerPrototype{

	List<JCas> docs = null;
	EntailmentGraphCollapsed graph = null;
		
	public DemoUseCaseOneFromXMIsNICEEnglish(String configFileName, String dataDir, int fileNumberLimit, String outputFolder, Class<?> lapClass, Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		super(configFileName, outputFolder, lapClass, edaClass);
		
		docs = DataUtils.loadData(dataDir, fileNumberLimit);
		graph = this.buildCollapsedGraph(docs);
	}
	
	
	@SuppressWarnings("rawtypes")
	public static void main(String[] argv) {
				
		// to run for a different cluster change the cluster name
		String configFileName = "./src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml";
		String dataDir = "./src/main/resources/exci/nice/xmi_perFragmentGraph/EMAIL0001";
		int fileNumberLimit = 100;
		String outputFolder = "./src/test/outputs/WP2_public_data_CAS_XMI/NICE_reAnnotated/perFrag/test/EMAIL0001";
		Class lapClass = TreeTaggerEN.class;
		Class edaClass = MaxEntClassificationEDA.class;

		DemoUseCaseOneFromXMIsNICEEnglish useOne;
		try {
			
			useOne = new DemoUseCaseOneFromXMIsNICEEnglish(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass, edaClass);
			useOne.inspectGraph(useOne.graph);

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

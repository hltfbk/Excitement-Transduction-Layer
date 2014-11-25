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
 *  Demo class for building entailment graphs from NICE data using only the text (and keyword annotations) from the fragment XMIs
 * 
 * @author vivi@fbk
 *
 */
public class DemoUseCaseOneFromNotAnnotatedXMIsNICEEnglish {
	
	
	public static void main(String[] argv) {

		// run the flow, create raw and collapsed entailment graphs and save them to files
		UseCaseOneFromNotAnnotatedXMIs demoEN;
		try {

			demoEN = new UseCaseOneFromNotAnnotatedXMIs("./src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",
					"./src/main/resources/exci/nice/xmi_perFragmentGraph/all/EMAIL0001", 4,
					"./src/test/outputs/WP2_public_data_CAS_XMI/NICE_reAnnotated/perFrag/test/EMAIL0001");
			
			demoEN.inspectGraph(demoEN.graph);

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

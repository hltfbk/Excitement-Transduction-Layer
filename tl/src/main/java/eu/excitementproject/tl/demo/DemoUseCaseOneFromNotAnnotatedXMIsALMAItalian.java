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
 * Demo class for building entailment graphs from ALMA data using only the text (and keyword annotations) from the fragment XMIs
 * 
 * @author vivi@fbk
 *
 */
public class DemoUseCaseOneFromNotAnnotatedXMIsALMAItalian {

	
	public static void main(String[] argv) {
		UseCaseOneFromNotAnnotatedXMIs demoIT;
		try {
			
			demoIT = new UseCaseOneFromNotAnnotatedXMIs("./src/test/resources/EOP_configurations/P1EDA_Base_IT.xml",
					"./src/main/resources/exci/alma/xmi_perInteraction/test", 4,
					"./src/test/outputs/alma/test");
			demoIT.inspectGraph(demoIT.graph);

			
		} catch (ConfigurationException | SecurityException 
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

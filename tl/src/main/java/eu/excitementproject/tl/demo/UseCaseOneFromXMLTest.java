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

/**
 * 
 * @author Vivi Nastase
 *
 */
public class UseCaseOneFromXMLTest {

	public static void main(String[] argv){

		String[] files = {"./src/test/resources/OMQ/test/four_fragments.xml",};

		String configFileName = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml";
		String outputFolder = "./src/test/outputs/"+files[0].replace(".xml", "").replace("./src/test/resources/","");
		
		UseCaseOneFromXMLs useOne;
		try {
			useOne = new UseCaseOneFromXMLs(configFileName, files, outputFolder, TreeTaggerEN.class, MaxEntClassificationEDA.class);

			useOne.inspectGraph(useOne.graph);
			
		} catch (ConfigurationException | EDAException | ComponentException | TransformerException | 
				FragmentAnnotatorException | FragmentGraphGeneratorException | 
				ModifierAnnotatorException | EntailmentGraphRawException | EntailmentGraphCollapsedException |
				GraphMergerException | GraphOptimizerException | IOException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}

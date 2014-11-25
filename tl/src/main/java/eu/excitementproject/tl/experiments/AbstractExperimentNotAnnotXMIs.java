package eu.excitementproject.tl.experiments;

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
import eu.excitementproject.tl.laputils.DataUtils;

/**
 * Full-pipeline experiment -- which means starting with CAS objects that have only the given interaction text and possibly the keyword annotations
 * 
 * @author vivi@fbk
 *
 */
public class AbstractExperimentNotAnnotXMIs extends AbstractExperiment {

	/**
	 * Constructor from configuration file, input data directory, file number limit, output folder, and LAP and EDA classes
	 * 
	 * @param configFileName -- configuration file for the EDA
	 * @param dataDir -- data with input XMIs
	 * @param fileNumberLimit -- limit on the file number (used for testing)
	 * @param outputFolder
	 * @param lapClass -- LAP class (it overrides the one specified in the configuration file)
	 * @param edaClass -- EDA class (it overrides the one specified in the configuration file)
	 * 
	 * @throws ConfigurationException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws EDAException
	 * @throws ComponentException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws GraphMergerException
	 * @throws GraphOptimizerException
	 * @throws FragmentGraphGeneratorException
	 * @throws IOException
	 * @throws EntailmentGraphRawException
	 * @throws TransformerException
	 * @throws ClassNotFoundException 
	 */
	public AbstractExperimentNotAnnotXMIs(String configFileName,
			String dataDir, int fileNumberLimit, String outputFolder,
			Class<?> lapClass, Class<?> edaClass)
			throws ConfigurationException, NoSuchMethodException,
			SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, EDAException,
			ComponentException, FragmentAnnotatorException,
			ModifierAnnotatorException, GraphMergerException,
			GraphOptimizerException, FragmentGraphGeneratorException,
			IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException {
		
		super(configFileName, outputFolder);
		
		docs = DataUtils.loadDataNoAnnot(dataDir, fileNumberLimit);
		graph = this.buildCollapsedGraph(docs);
		
		results = new ResultsContainer();
	}

	
	/**
	 * Constructor from configuration file, input data directory, file limit, output folder. 
	 * It will take the EDA and LAP classes from the configuration file 
	 * 
	 * @param configFileName -- configuration file for the EDA. It will take the EDA and LAP classes from this file.
	 * @param dataDir -- directory with input XMIs
	 * @param fileNumberLimit -- limit on the number of files to process (used during testing)
	 * @param outputFolder
	 * 
	 * @throws ConfigurationException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws EDAException
	 * @throws ComponentException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws GraphMergerException
	 * @throws GraphOptimizerException
	 * @throws FragmentGraphGeneratorException
	 * @throws IOException
	 * @throws EntailmentGraphRawException
	 * @throws TransformerException
	 * @throws ClassNotFoundException 
	 */
	public AbstractExperimentNotAnnotXMIs(String configFileName,
			String dataDir, int fileNumberLimit, String outputFolder)
			throws ConfigurationException, NoSuchMethodException,
			SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, EDAException,
			ComponentException, FragmentAnnotatorException,
			ModifierAnnotatorException, GraphMergerException,
			GraphOptimizerException, FragmentGraphGeneratorException,
			IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException {
		
		super(configFileName, outputFolder);
		
		docs = DataUtils.loadDataNoAnnot(dataDir, fileNumberLimit);
		graph = this.buildCollapsedGraph(docs);
		
		results = new ResultsContainer();
	}
	
}

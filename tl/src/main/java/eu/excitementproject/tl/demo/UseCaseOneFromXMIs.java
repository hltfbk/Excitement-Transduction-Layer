package eu.excitementproject.tl.demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.uima.jcas.JCas;

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
import eu.excitementproject.tl.laputils.DataUtils;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

/**
 * Use case one using (gold-standard) fragment and modifier annotation from the input XMIs
 * 
 * @author Vivi Nastase
 *
 */
public class UseCaseOneFromXMIs extends UseCaseOneRunnerPrototype {

	// list of CAS objects read from the input XMIs
	List<JCas> docs = null;
	
	// final entailment graph
	EntailmentGraphCollapsed graph = null;
	
	/**
	 * Constructor from configuration file, input data directory, file number limit, output folder, EDA class and LAP class
	 * 
	 * It will load the XMIs from the given directory, with all (fragment and modifier in particular) annotations 
	 * 
	 * @param configFileName -- name of configuration file for the EDA
	 * @param dataDir -- directory with input XMIs
	 * @param fileNumberLimit
	 * @param outputFolder
	 * @param lapClass -- the class for the LAP (it will override what is written in the configuration file)
	 * @param edaClass -- the class for the EDA (it will override what is written in the configuration file)
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
	 */
	public UseCaseOneFromXMIs(String configFileName, String dataDir, int fileNumberLimit,
			String outputFolder, Class<?> lapClass, Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		
		super(configFileName, outputFolder, lapClass, edaClass);
		
		docs = DataUtils.loadData(dataDir, fileNumberLimit);
		graph = this.buildCollapsedGraph(docs);
	}	

	
	/**
	 * Constructor from configuration file, input data directory, file number limit, output folder
	 * The EDA and LAP classes are read from the configuration file
	 * 
	 * It will load the XMIs from the given directory, with all (fragment and modifier in particular) annotations 
	 * 
	 * @param configFileName -- name of configuration file for the EDA. It contains the classes for the EDA and the LAP
	 * @param dataDir -- directory with input XMIs
	 * @param fileNumberLimit
	 * @param outputFolder
	 * 
	 * @throws ConfigurationException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws GraphMergerException
	 * @throws GraphOptimizerException
	 * @throws FragmentGraphGeneratorException
	 * @throws IOException
	 * @throws EntailmentGraphRawException
	 * @throws TransformerException
	 * @throws ClassNotFoundException 
	 * @throws ComponentException 
	 * @throws EDAException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public UseCaseOneFromXMIs(String configFileName, String dataDir,
			int fileNumberLimit, String outputFolder) throws ConfigurationException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, ClassNotFoundException {
		
		super(configFileName, outputFolder);
		
		docs = DataUtils.loadData(dataDir, fileNumberLimit);
		graph = this.buildCollapsedGraph(docs);		
	}

	
	
	/**
	 * outputs the entailment graph built
	 */
	public void inspectResults() {
		try {
			this.inspectGraph(graph);
		} catch (IOException | EntailmentGraphCollapsedException
				| TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

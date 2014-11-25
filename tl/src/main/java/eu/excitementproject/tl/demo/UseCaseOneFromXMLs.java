package eu.excitementproject.tl.demo;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

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
import eu.excitementproject.tl.laputils.DataUtils;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

/**
 * Use case one, loading the data from XML files
 * 
 * @author vivi@fbk
 *
 */
public class UseCaseOneFromXMLs extends UseCaseOneRunnerPrototype {

	// set of interactions representing the input data
	Set<Interaction> docs = null;
	
	// final entailment graph
	EntailmentGraphCollapsed graph = null;
	
	/**
	 * Constructor from configuration file, directory with input XML files, classes for the LAP and EDA
	 * Use the default fragment (keyword-based fixed length) and modifier (adv & adj) annotators
	 * 
	 * @param configFileName -- configuration file for the EDA
	 * @param dataDir -- directory with input XML files
	 * @param outputFolder
	 * @param lapClass -- class for the LAP (it overrides the one in the configuration file)	 
	 * @param edaClass -- class for the EDA (it overrides the one in the configuration file)
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
	public UseCaseOneFromXMLs(String configFileName, String dataDir, 
			String outputFolder, Class<?> lapClass, Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		
		super(configFileName, outputFolder, lapClass, edaClass, "default");
		
		docs = DataUtils.loadXMLData(dataDir);
		graph = this.buildCollapsedGraph(docs);
	}
	
	
	/**
	 * Constructor from configuration file, an array of input XML files, classes for the LAP and EDA
	 * Use the default fragment (keyword-based fixed length) and modifier (adv & adj) annotators
	 * 
	 * @param configFileName -- configuration file for the EDA
	 * @param files -- array of input XML files
	 * @param outputFolder
	 * @param lapClass -- class for the LAP (it overrides the one in the configuration file)	 
	 * @param edaClass -- class for the EDA (it overrides the one in the configuration file)
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
	public UseCaseOneFromXMLs(String configFileName, String[] files, 
			String outputFolder, Class<?> lapClass, Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		
		super(configFileName, outputFolder, lapClass, edaClass, "default");
		
		docs = DataUtils.loadXMLData(files);
		graph = this.buildCollapsedGraph(docs);
	}

	
	/**
	 * Constructor from configuration file, directory with input XML files,
	 * Use the default fragment (keyword-based fixed length) and modifier (adv & adj) annotators
	 * 
	 * @param configFileName -- configuration file for the EDA. It gets the classes for the EDA and the LAP from this file.
	 * @param dataDir -- directory with input XML files
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
	public UseCaseOneFromXMLs(String configFileName, String dataDir, 
			String outputFolder) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException {
		
		super(configFileName, outputFolder);
		
		docs = DataUtils.loadXMLData(dataDir);
		graph = this.buildCollapsedGraph(docs);
	}
	
	
	/**
	 * Constructor from configuration file, an array of input XML files
	 * Use the default fragment (keyword-based fixed length) and modifier (adv & adj) annotators
	 * 
	 * @param configFileName -- configuration file for the EDA. It gets the classes for the EDA and the LAP from this file.
	 * @param files -- array of input XML files
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
	public UseCaseOneFromXMLs(String configFileName, String[] files, 
			String outputFolder) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException, ClassNotFoundException {
		
		super(configFileName, outputFolder);
		
		docs = DataUtils.loadXMLData(files);
		graph = this.buildCollapsedGraph(docs);
	}
	
	/**
	 * outputs the resulting entailment graph
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

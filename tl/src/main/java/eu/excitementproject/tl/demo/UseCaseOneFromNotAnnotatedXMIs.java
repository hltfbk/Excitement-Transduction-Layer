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
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.DataUtils;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

public class UseCaseOneFromNotAnnotatedXMIs extends UseCaseOneRunnerPrototype {

	List<JCas> docs = null;
	EntailmentGraphCollapsed graph = null;
	
	
	public UseCaseOneFromNotAnnotatedXMIs(String configFileName, String dataDir, int fileNumberLimit,
			String outputFolder, Class<?> lapClass, Class<?> edaClass) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		
		super(configFileName, outputFolder, lapClass, edaClass, "default");
		
		docs = DataUtils.loadData(dataDir, fileNumberLimit);
		graph = this.buildCollapsedGraph(docs);
	}
	
	
	public UseCaseOneFromNotAnnotatedXMIs(String configFileName, String dataDir, int fileNumberLimit,
			String outputFolder, Class<?> lapClass, Class<?> edaClass, FragmentAnnotator fragAnot, ModifierAnnotator modAnot) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, GraphOptimizerException, FragmentGraphGeneratorException, IOException, EntailmentGraphRawException, TransformerException {
		
		super(configFileName, outputFolder, lapClass, edaClass, fragAnot, modAnot);
		
		docs = DataUtils.loadDataNoAnnot(dataDir, fileNumberLimit);
		graph = this.buildCollapsedGraph(docs);
	}

	
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

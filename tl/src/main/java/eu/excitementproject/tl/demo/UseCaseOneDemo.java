package eu.excitementproject.tl.demo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;
import eu.excitementproject.eop.lap.dkpro.OpenNLPTaggerEN;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.demo.DemoUseCaseOneFromXMIsNICEEnglish;
import eu.excitementproject.tl.edautils.EDAUtils;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.laputils.DataUtils;
import eu.excitementproject.tl.laputils.LAPUtils;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

@SuppressWarnings("unused")
public class UseCaseOneDemo {

		
	public static void main(String[] argv) {
		
		String configFileName = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml";
		String dataDir = "./src/test/resources/NICE/XMIs/EMAIL0001";
		String outputFolder = "./src/test/outputs/"+dataDir.replace(".\\src\\test\\resources\\","").replace("\\","/");
		int fileNumberLimit = 4;
		
		Logger logger = Logger.getLogger(UseCaseOneDemo.class);

		
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		
			
		UseCaseOneRunnerPrototype useOne;
		try {
			useOne = new UseCaseOneRunnerPrototype(configFileName, outputFolder, TreeTaggerEN.class, MaxEntClassificationEDA.class);
			
			
			List<JCas> docs = DataUtils.loadData(dataDir, fileNumberLimit);

			// build collapsed graph
			EntailmentGraphCollapsed graph = useOne.buildCollapsedGraph(docs);
			useOne.inspectGraph(graph);

		} catch (ConfigurationException | NoSuchMethodException
				| SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | EDAException | ComponentException
				| FragmentAnnotatorException | ModifierAnnotatorException
				| GraphMergerException | GraphOptimizerException | FragmentGraphGeneratorException | IOException | EntailmentGraphRawException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EntailmentGraphCollapsedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

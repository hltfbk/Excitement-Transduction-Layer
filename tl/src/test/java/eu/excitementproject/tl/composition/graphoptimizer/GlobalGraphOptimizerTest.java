package eu.excitementproject.tl.composition.graphoptimizer;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class GlobalGraphOptimizerTest {

	@Ignore("Fails sometimes.") @Test
	public void test() {

					try {
						System.out.println("**** Test graph optimizer: merged graph ****");

						LAPAccess lap = new TreeTaggerEN();
						File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml");				
						CommonConfig config = null;
						config = new ImplCommonConfig(configFile);
						MaxEntClassificationEDA meceda = new MaxEntClassificationEDA();	
						meceda.initialize(config);  
						GraphMerger merger = new AutomateWP2ProcedureGraphMerger(lap,meceda); 
						
						Set<FragmentGraph> fragmentGraphs = FragmentGraph.getSampleOutput();
						System.out.println("Merged raw graph:");			
						EntailmentGraphRaw rawGraph = merger.mergeGraphs(fragmentGraphs);
						System.out.println(rawGraph.toString());
						rawGraph.toDOT("./src/test/outputs/rawGraph.txt");
						
						System.out.println("**** Optimizing the the raw graph ****");
						GraphOptimizer collapser = new GlobalGraphOptimizer();
						EntailmentGraphCollapsed finalGraph = collapser.optimizeGraph(rawGraph);
						System.out.println("Done:\n"+finalGraph.toString());
						finalGraph.toDOT("./src/test/outputs/collapsedGraph.txt");
						
						System.out.println("**** Test graph optimizer: sample graph ****");
						rawGraph = EntailmentGraphRaw.getSampleOuput(false);
						System.out.println(rawGraph.toString());
						rawGraph.toDOT("./src/test/outputs/sampleRawGraph.txt");
						System.out.println("**** Optimizing the the sample graph ****");
						finalGraph = collapser.optimizeGraph(rawGraph, 0.2);
						System.out.println("Done:\n"+finalGraph.toString());
						finalGraph.toDOT("./src/test/outputs/collapsedGraphFromSample.txt");						
					} catch (LAPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (EDAException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ComponentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (GraphMergerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (GraphOptimizerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			
			
	}

}

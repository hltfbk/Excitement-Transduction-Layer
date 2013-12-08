package eu.excitementproject.tl.composition.graphoptimizer;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;

//import eu.excitementproject.eop.common.EDABasic;
//import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;

import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class SimpleGraphOptimizerTest {

	@Test
	public void test() {

					try {
						System.out.println("**** Test collapsed graph generator: merged graph ****");

						LAPAccess lap = new TreeTaggerEN();
//					EDABasic<?> eda = new RandomEDA();
						File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml");				
						CommonConfig config = null;
						config = new ImplCommonConfig(configFile);
						MaxEntClassificationEDA meceda = new MaxEntClassificationEDA();	
						meceda.initialize(config);  

						
//					GraphMerger merger = new AutomateWP2ProcedureGraphMerger(lap,eda); 
						GraphMerger merger = new AutomateWP2ProcedureGraphMerger(lap,meceda); 
						
						Set<FragmentGraph> fragmentGraphs = FragmentGraph.getSampleOutput();
						System.out.println("Merged raw graph:");			
						EntailmentGraphRaw rawGraph = merger.mergeGraphs(fragmentGraphs);
						System.out.println(rawGraph.toString());
						rawGraph.toDOT("./src/test/outputs/rawGraph.txt");
						
						System.out.println("**** Collapsing the the raw graph ****");
						GraphOptimizer collapser = new SimpleGraphOptimizer();
						EntailmentGraphCollapsed finalGraph = collapser.optimizeGraph(rawGraph);
						System.out.println("Done:\n"+finalGraph.toString());
						finalGraph.toDOT("./src/test/outputs/collapsedGraph.txt");
						
						System.out.println("**** Test collapsed graph generator: sample graph ****");
						rawGraph = EntailmentGraphRaw.getSampleOuput(false);
						System.out.println(rawGraph.toString());
						rawGraph.toDOT("./src/test/outputs/sampleRawGraph.txt");
						System.out.println("**** Collapsing the the raw graph ****");
						finalGraph = collapser.optimizeGraph(rawGraph, 0.2);
						System.out.println("Done:\n"+finalGraph.toString());
						finalGraph.toDOT("./src/test/outputs/collapsedGraphFromSample.txt");
						
						for (EquivalenceClass node : finalGraph.sortNodesByNumberOfInteractions(5)){
							System.out.println(node.toString());
						}
					} catch (ConfigurationException | EDAException | ComponentException |
							GraphMergerException | IOException | GraphOptimizerException e) {
						e.printStackTrace();
						fail(e.getMessage());
					}
			
			
	}

}

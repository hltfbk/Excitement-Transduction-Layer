package eu.excitementproject.tl.composition.collapsedgraphgenerator;

import java.util.Set;

import org.junit.Test;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.api.CollapsedGraphGenerator;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;

public class SimpleCollapseGraphGeneratorTest {

	@Test
	public void test() {
			try {
				System.out.println("**** Test collapsed graph generator: merged graph ****");

				LAPAccess lap = new TreeTaggerEN();
				EDABasic<?> eda = new RandomEDA();
				
				GraphMerger merger = new AutomateWP2ProcedureGraphMerger(lap,eda); 
				Set<FragmentGraph> fragmentGraphs = FragmentGraph.getSampleOutput();
				System.out.println("Merged raw graph:");			
				EntailmentGraphRaw rawGraph = merger.mergeGraphs(fragmentGraphs);
				System.out.println(rawGraph.toString());
				
				System.out.println("**** Collapsing the the raw graph ****");
				CollapsedGraphGenerator collapser = new SimpleCollapseGraphGenerator();
				EntailmentGraphCollapsed finalGraph = collapser.generateCollapsedGraph(rawGraph);
				System.out.println("Done:\n"+finalGraph.toString());
				
				System.out.println("**** Test collapsed graph generator: sample graph ****");

				
				rawGraph = EntailmentGraphRaw.getSampleOuput(false);
				System.out.println(rawGraph.toString());
				
				System.out.println("**** Collapsing the the raw graph ****");
				finalGraph = collapser.generateCollapsedGraph(rawGraph, 0.2);
				System.out.println("Done:\n"+finalGraph.toString());
			} catch (LAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GraphMergerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CollapsedGraphGeneratorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			
	}

}

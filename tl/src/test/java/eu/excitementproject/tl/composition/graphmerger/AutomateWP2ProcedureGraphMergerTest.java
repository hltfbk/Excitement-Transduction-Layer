package eu.excitementproject.tl.composition.graphmerger;

import java.util.Set;
import org.junit.Test;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;

public class AutomateWP2ProcedureGraphMergerTest {

	@Test
	public void test() {
		
		try {
			LAPAccess lap = new TreeTaggerEN();
			EDABasic<?> eda = new RandomEDA();
			
			GraphMerger merger = new AutomateWP2ProcedureGraphMerger(lap,eda); 
			
			Set<FragmentGraph> fragmentGraphs = FragmentGraph.getSampleOutput();
			System.out.println("**** Merging the following fragment graphs with  RandomEDA (complete statements printed) ****");			
			for (FragmentGraph fg : fragmentGraphs) {
				System.out.println(fg.getCompleteStatement().getText());
			}
			
			System.out.println("Merged raw graph:");			
			EntailmentGraphRaw rawGraph = merger.mergeGraphs(fragmentGraphs);
			System.out.println(rawGraph.isEmpty());
			System.out.println(rawGraph.toString());
		} catch (LAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GraphMergerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

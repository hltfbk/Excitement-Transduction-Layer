package eu.excitementproject.tl.composition.graphmerger;

import static org.junit.Assert.fail;

import java.util.Set;

import org.junit.Test;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;

public class AutomateWP2ProcedureGraphMergerTest {

	@Test
	public void test() {
		
		try {
			CachedLAPAccess lap = new CachedLAPAccess(new TreeTaggerEN());
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
		} catch (LAPException | GraphMergerException e) {
			e.printStackTrace();
			fail(e.getMessage()); 
		}
		
	}

}

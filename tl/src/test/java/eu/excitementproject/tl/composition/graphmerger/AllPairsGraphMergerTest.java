package eu.excitementproject.tl.composition.graphmerger;

import static org.junit.Assert.fail;

import java.util.Set;

import org.apache.log4j.Logger;
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

public class AllPairsGraphMergerTest {
	private final Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void test() {
		
		try {
			CachedLAPAccess lap = new CachedLAPAccess(new TreeTaggerEN());
			EDABasic<?> eda = new RandomEDA();
			
			GraphMerger merger = new AllPairsGraphMerger(lap,eda); 
			
			Set<FragmentGraph> fragmentGraphs = FragmentGraph.getSampleOutput();
			logger.info("**** Merging the following fragment graphs with  RandomEDA (complete statements printed) ****");			
			for (FragmentGraph fg : fragmentGraphs) {
				logger.info(fg.getCompleteStatement().getText());
			}
			
			logger.info("Merged raw graph:");			
			EntailmentGraphRaw rawGraph = merger.mergeGraphs(fragmentGraphs);
			logger.info(rawGraph.isEmpty());
			logger.info(rawGraph.toString());
		} catch (LAPException | GraphMergerException e) {
			e.printStackTrace();
			fail(e.getMessage()); 
		}
		
	}

}

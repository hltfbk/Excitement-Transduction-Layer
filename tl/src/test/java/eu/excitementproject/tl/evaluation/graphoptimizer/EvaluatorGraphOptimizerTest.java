package eu.excitementproject.tl.evaluation.graphoptimizer;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.log4j.Logger;
import org.junit.Test;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;

/**
 * 
 * @author ??
 *
 */
public class EvaluatorGraphOptimizerTest {
	
	private final Logger logger = Logger.getLogger(this.getClass());

	@Test
	public void test() {
		boolean includeFragmentGraphEdges = false;
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(true);
		String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/_annotationExample.xml";
		try {
			loader.addAnnotationsFromFile(annotationFilename, false);
			logger.info("Loaded "+loader.getEdges().size()+" gold standard edges.");
			logger.info(EvaluatorGraphOptimizer.evaluateDecollapsedGraph(loader.getEdges(), new EntailmentGraphCollapsed(new File("./src/test/resources/sample_graphs/collapsed_graph_for_evaluator_test.xml")), includeFragmentGraphEdges));
		} catch (GraphEvaluatorException | EntailmentGraphCollapsedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

}

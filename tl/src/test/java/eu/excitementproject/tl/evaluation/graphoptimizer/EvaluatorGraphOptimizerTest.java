package eu.excitementproject.tl.evaluation.graphoptimizer;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;

public class EvaluatorGraphOptimizerTest {

	@Test
	public void test() {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(false);
		String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/_annotationExample.xml";
		try {
			loader.addAnnotations(annotationFilename);
			System.out.println("Loaded "+loader.getEdges().size()+" gold standard edges.");
			System.out.println(EvaluatorGraphOptimizer.evaluateDecollapsedGraph(loader.getEdges(), new EntailmentGraphCollapsed(new File("./src/test/resources/sample_graphs/collapsed_graph_for_evaluator_test.xml"))));
		} catch (GraphEvaluatorException | EntailmentGraphCollapsedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

}

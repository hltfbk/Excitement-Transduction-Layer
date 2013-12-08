package eu.excitementproject.tl.evaluation.graphmerger;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;

public class EvaluatorGraphMergerTest {

	@Test
	public void test() {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(false);
		String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/_annotationExample.xml";
		try {
			loader.addAnnotations(annotationFilename);
			System.out.println("Loaded "+loader.getEdges().size()+" gold standard edges.");
/*			int i=1;		
			for (EntailmentRelation edge : loader.getEdges){
				System.out.println(i+": "+edge);
				i++;
			}
*/			System.out.println(EvaluatorGraphMerger.evaluate(loader.getEdges(), loader.getEdges()));
		} catch (GraphEvaluatorException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}		
	}

}

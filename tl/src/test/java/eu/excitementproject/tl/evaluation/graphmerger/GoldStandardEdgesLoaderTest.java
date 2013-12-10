package eu.excitementproject.tl.evaluation.graphmerger;

import static org.junit.Assert.*;

import org.junit.Test;

import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;

public class GoldStandardEdgesLoaderTest {

	@Test
	public void test() {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(true);
		try {
			loader.addAllAnnotations("./src/test/resources/WP2_gold_standard_annotation/_example");
			System.out.println("\nLoaded "+loader.edges.size()+ " edges.");

			loader = new GoldStandardEdgesLoader(false);
			String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/_annotationExample.xml";			
			loader.addAnnotationsFromFile(annotationFilename);
	
		} catch (GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}			
	}
}

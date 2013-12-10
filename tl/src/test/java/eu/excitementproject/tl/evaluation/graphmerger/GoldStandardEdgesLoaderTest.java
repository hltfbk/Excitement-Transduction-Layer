package eu.excitementproject.tl.evaluation.graphmerger;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;

public class GoldStandardEdgesLoaderTest {

	@Test
	public void test() {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(true);
		try {
			loader.addAllAnnotations("./src/test/resources/WP2_gold_standard_annotation/_example");
//			loader.addAllAnnotations("./src/test/resources/WP2_gold_standard_annotation/_blind");
			System.out.println("\nLoaded "+loader.edges.size()+ " edges.");

			loader = new GoldStandardEdgesLoader(false);
			String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/_annotationExample.xml";			
//			String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/email0020.lost.xml";			
			loader.addAnnotationsFromFile(annotationFilename);
			try {
				ClusterStatistics.processCluster(new File(annotationFilename));
			} catch (ParserConfigurationException | SAXException | IOException e) {							
				e.printStackTrace();
			}			
	
		} catch (GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}			
	}
}

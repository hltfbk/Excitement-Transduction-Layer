package eu.excitementproject.tl.evaluation.graphmerger;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class GoldStandardEdgesLoaderTest {

	@Test
	public void test() {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader();
		try {
//			loader.addAllAnnotations("./src/test/resources/WP2_gold_standard_annotation/_example");
			loader.addAllAnnotations("./src/test/resources/WP2_gold_standard_annotation/_blind");
//			System.out.println("\nLoaded "+loader.edges.size()+ " edges.");

			loader = new GoldStandardEdgesLoader();
			String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/_annotationExample.xml";			
//			String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/email0020.lost.xml";			
//			String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/email0020.xml";			
			loader.addAnnotationsFromFile(annotationFilename);
			try {
				ClusterStatistics.processCluster(new File(annotationFilename));
				EntailmentGraphRaw gr = loader.getRawGraph();
				gr.toDOT(annotationFilename+".dot");
				EntailmentGraphCollapsed gc = loader.getCollapsedGraph();
				gc.toDOT(annotationFilename+".collapsed.dot");
			} catch (ParserConfigurationException | SAXException | IOException | GraphOptimizerException e) {							
				e.printStackTrace();
			}			
	
		} catch (GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}			
	}
}

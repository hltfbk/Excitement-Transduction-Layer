package eu.excitementproject.tl.evaluation.graphmerger;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class GoldStandardEdgesLoaderTest {

	@Ignore @Test
	public void test() {
		Logger.getRootLogger().setLevel(Level.INFO); 
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(true);
		try {
			loader.loadAllAnnotations("./src/test/resources/WP2_gold_standard_annotation/NICE_open", false);

	/*		try {
				EntailmentGraphRaw gr = loader.getRawGraph();
				gr.toDOT("./src/test/resources/WP2_gold_standard_annotation/_big/raw_full.dot");
				EntailmentGraphCollapsed gc = loader.getCollapsedGraph();
				gc.toDOT("./src/test/resources/WP2_gold_standard_annotation/_big/collapsed_full.dot");
			} catch (IOException | GraphOptimizerException e) {						
				e.printStackTrace();
			}		*/		
			
			loader = new GoldStandardEdgesLoader(true);
			String annotationFilename = "./src/test/resources/WP2_gold_standard_annotation/_annotationExample.xml";			
			loader.addAnnotationsFromFile(annotationFilename, false);
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

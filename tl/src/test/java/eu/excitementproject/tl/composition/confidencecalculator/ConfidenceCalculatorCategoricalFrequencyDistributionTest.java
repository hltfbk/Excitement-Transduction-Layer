package eu.excitementproject.tl.composition.confidencecalculator;

import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.api.ConfidenceCalculator;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class ConfidenceCalculatorCategoricalFrequencyDistributionTest {

	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.composition.confidencecalculator.test"); 
		
		try {					
			/************* TEST 1 ***************/
			testlogger.info("Reading sample raw entailment graph."); 			
			EntailmentGraphRaw rawGraph = EntailmentGraphRaw.getSampleOuputWithCategories(false); 	
			GraphOptimizer cgg = new SimpleGraphOptimizer();
			testlogger.info("Creating collapsed entailment graph from sample graph."); 			
			EntailmentGraphCollapsed entailmentGraph = cgg.optimizeGraph(rawGraph);
			testlogger.info("Adding confidence scores to graph.");
			ConfidenceCalculator cc = new ConfidenceCalculatorCategoricalFrequencyDistribution(false);
			cc.computeCategoryConfidences(entailmentGraph);		
			testlogger.info("Reading nodes from updated graph.");
			Set<EquivalenceClass> nodes = entailmentGraph.vertexSet();
			testlogger.info("Reading category confidences per node.");
			for (EquivalenceClass node : nodes) {
				Map<String,Double> categoryConfidences = node.getCategoryConfidences();
				testlogger.info("Category confidences for node \"" + node.getLabel() + "\"");
				double sum = 0.0;
				for (String category : categoryConfidences.keySet()) {
					double score = categoryConfidences.get(category);
					testlogger.info(category + " : " + score);		
					sum += score;
				}
				Assert.assertEquals(1.0, sum);
			}			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}

}

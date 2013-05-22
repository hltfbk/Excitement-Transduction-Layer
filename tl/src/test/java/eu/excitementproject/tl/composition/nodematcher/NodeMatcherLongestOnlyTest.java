package eu.excitementproject.tl.composition.nodematcher;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

public class NodeMatcherLongestOnlyTest {

	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.composition.nodematcher.test"); 
		
		try {
			testlogger.info("Reading in a sample entailment graph."); 
			EntailmentGraphRaw entailmentGraph = EntailmentGraphRaw.getSampleOuput(false); 
			
			/************* TEST 1 ***************/
			testlogger.info("Creating fragment graph for sentence 'Disappointed with the amount of legroom compared with other trains'."); 			
			String text = "Disappointed with the amount of legroom compared with other trains";
			Set<String> modifiers = new HashSet<String>();
			testlogger.info("Adding two modifiers: 'the amount of' and 'compared with other trains'."); 			
			modifiers.add("the amount of");
			modifiers.add("compared with other trains");
			FragmentGraph fragmentGraph = new FragmentGraph(text, modifiers);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			NodeMatcher nm = new NodeMatcherLongestOnly(); 
			Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraph, entailmentGraph);
			Assert.assertEquals(1, matches.size()); //should return a single match
			for (NodeMatch nodeMatch : matches) {
				Assert.assertEquals("Disappointed with the amount of legroom compared with other trains", nodeMatch.getMention().getText().trim());
				testlogger.info("Matching node: 'Disappointed with the amount of legroom compared with other trains'"); 			
				for (PerNodeScore score : nodeMatch.getScores()) {
					testlogger.info("Associated score: 1.0'"); 			
					Assert.assertEquals(1.0, score.getScore());
				}
			}


			/************* TEST 2 ***************/
			testlogger.info("Creating fragment graph for sentence 'Disappointed with the limited legroom'."); 			
			text = "Disappointed with the limited legroom";
			testlogger.info("Adding on modifier: 'the limited'."); 			
			modifiers = new HashSet<String>();
			modifiers.add("the limited");
			fragmentGraph = new FragmentGraph(text, modifiers);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			nm = new NodeMatcherLongestOnly(); 
			matches = nm.findMatchingNodesInGraph(fragmentGraph, entailmentGraph);
			Assert.assertEquals(1, matches.size()); 
			testlogger.info("NodeMatcher matches a single node."); 			
			for (NodeMatch nodeMatch : matches) {
				Assert.assertEquals("Disappointed with legroom", 
						nodeMatch.getMention().getText().replaceAll("\\s+", " ").trim());
				testlogger.info("Matching node: 'Disappointed with legroom'"); 			
				for (PerNodeScore score : nodeMatch.getScores()) {
					testlogger.info("Associated score: 1.0'"); 			
					Assert.assertEquals(1.0, score.getScore());
				}
			}

			/************* TEST 3 ***************/
			testlogger.info("Creating fragment graph for sentence 'Limited legroom'."); 			
			text = "Limited legroom";
			modifiers = new HashSet<String>();
			fragmentGraph = new FragmentGraph(text, modifiers);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			nm = new NodeMatcherLongestOnly(); 
			matches = nm.findMatchingNodesInGraph(fragmentGraph, entailmentGraph);
			Assert.assertEquals(0, matches.size()); 
			testlogger.info("NodeMatcher does not match a node."); 			

			testlogger.info("No problem observed on the test cases"); 
			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}
}

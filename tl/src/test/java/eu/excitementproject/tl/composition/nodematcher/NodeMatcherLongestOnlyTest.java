package eu.excitementproject.tl.composition.nodematcher;

import static org.junit.Assert.fail;

import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CASUtils.Region;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

/**
 * 
 * @author Kathrin Eichler
 *
 */
public class NodeMatcherLongestOnlyTest {

	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.composition.nodematcher.test"); 
		
		try {
			testlogger.info("Reading in a sample entailment graph."); 
			EntailmentGraphRaw rawGraph = EntailmentGraphRaw.getSampleOuput(false); 
			GraphOptimizer cgg = new SimpleGraphOptimizer();
			testlogger.info("Creating collapsed entailment graph from sample graph."); 			
			EntailmentGraphCollapsed entailmentGraph = cgg.optimizeGraph(rawGraph);
			
			/************* TEST 1 ***************/
			testlogger.info("Creating fragment graph for sentence 'Disappointed with the amount of legroom compared with other trains'."); 			
			JCas jcas = CASUtils.createNewInputCas();			
			String text = "Disappointed with the amount of legroom compared with other trains";
			jcas.setDocumentText(text);
			Region[] fragmentRegions = new Region[1];
			fragmentRegions[0] = new Region(0,text.length());
			CASUtils.annotateOneDeterminedFragment(jcas, fragmentRegions);
			testlogger.info("Adding two modifiers: 'the amount of' and 'compared with other trains'."); 			
			Region[] modifierRegions = new Region[2];
			modifierRegions[0] = new Region(18,31);
			modifierRegions[1] = new Region(40,text.length());
			CASUtils.annotateOneModifier(jcas, modifierRegions);
			testlogger.info("Creating fragment graph from CAS."); 			
			FragmentGraphGenerator fgg = new FragmentGraphGeneratorFromCAS();
			Set<FragmentGraph> fragmentGraphs = fgg.generateFragmentGraphs(jcas);
			testlogger.info("Calling node matcher on the fragment graph."); 	
			NodeMatcher nm = new NodeMatcherLongestOnly(entailmentGraph, true); 
			Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraphs.iterator().next());
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
			JCas jcas2 = CASUtils.createNewInputCas();			
			text = "Disappointed with the limited legroom";
			jcas2.setDocumentText(text);
			fragmentRegions = new Region[1];
			fragmentRegions[0] = new Region(0,text.length());
			CASUtils.annotateOneDeterminedFragment(jcas2, fragmentRegions);
			testlogger.info("Adding one modifier: 'the limited'."); 			
			modifierRegions = new Region[1];
			modifierRegions[0] = new Region(18,29);
			CASUtils.annotateOneModifier(jcas2, modifierRegions);
			testlogger.info("Creating fragment graph from CAS."); 			
			fragmentGraphs = fgg.generateFragmentGraphs(jcas2);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			matches = nm.findMatchingNodesInGraph(fragmentGraphs.iterator().next());
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
			JCas jcas3 = CASUtils.createNewInputCas();			
			text = "Limited legroom";
			jcas3.setDocumentText(text);
			fragmentRegions = new Region[1];
			fragmentRegions[0] = new Region(0,text.length());
			CASUtils.annotateOneDeterminedFragment(jcas3, fragmentRegions);
			fragmentGraphs = fgg.generateFragmentGraphs(jcas3);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			matches = nm.findMatchingNodesInGraph(fragmentGraphs.iterator().next());
			Assert.assertEquals(0, matches.size()); 
			testlogger.info("NodeMatcher does not match a node."); 		
			
			/************* TEST 4 ***************/
			testlogger.info("Creating fragment graph for sentence 'Disappointed with the amount of legroom compared with other trains'."); 			
			JCas jcas4 = CASUtils.createNewInputCas();			
			String text4 = "Disappointed with the amount of legroom compared with other trains";
			jcas4.setDocumentText(text4);
			fragmentRegions = new Region[1];
			fragmentRegions[0] = new Region(0,text4.length());
			CASUtils.annotateOneDeterminedFragment(jcas4, fragmentRegions);
			testlogger.info("Adding two modifiers: 'the amount of' and 'compared with other trains'."); 			
			modifierRegions = new Region[2];
			modifierRegions[0] = new Region(18,31);
			modifierRegions[1] = new Region(40,text4.length());
			CASUtils.annotateOneModifier(jcas4, modifierRegions);
			testlogger.info("Creating fragment graph from CAS."); 			
			fgg = new FragmentGraphGeneratorFromCAS();
			fragmentGraphs = fgg.generateFragmentGraphs(jcas4);
			testlogger.info("Calling node matcher on the fragment graph."); 	
			NodeMatcher nm1 = new NodeMatcherLongestOnly(entailmentGraph, false); 
			matches = nm1.findMatchingNodesInGraph(fragmentGraphs.iterator().next());
			Assert.assertEquals(1, matches.size()); //should return a single match
			for (NodeMatch nodeMatch : matches) {
				Assert.assertEquals("Disappointed with the amount of legroom compared with other trains", nodeMatch.getMention().getText().trim());
				testlogger.info("Matching node: 'Disappointed with the amount of legroom compared with other trains'"); 
				Assert.assertEquals(3, nodeMatch.getScores().size());
				for (PerNodeScore score : nodeMatch.getScores()) {
					testlogger.info("Matched node: " + score.getNode().getLabel());
					testlogger.info("Associated score: 1.0'"); 			
					Assert.assertEquals(1.0, score.getScore());
				}
			}

			testlogger.info("No problem observed on the test cases"); 
			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}
}

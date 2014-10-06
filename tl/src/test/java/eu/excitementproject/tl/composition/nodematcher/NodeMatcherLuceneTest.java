package eu.excitementproject.tl.composition.nodematcher;

import static org.junit.Assert.fail;


import java.io.File;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.CASUtils.Region;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.search.NodeMatch;

public class NodeMatcherLuceneTest {

	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.composition.nodematcher.test"); 
		
		try {
			String testGraphFilename = "src/test/resources/OMQ/graphs/german_dummy_data_for_evaluator_test_graph_sentence.xml";
			File testGraphFile = new File(testGraphFilename);
			testlogger.info("File " + testGraphFile.getAbsolutePath() + " exists? " + testGraphFile.exists());
			testlogger.info("Reading in a sample entailment graph from " + testGraphFile.getAbsolutePath()); 
			EntailmentGraphCollapsed entailmentGraph = new EntailmentGraphCollapsed(testGraphFile);

			/************* Indexing and search initialization **********************/
			NodeMatcherLuceneSimple nm = new NodeMatcherLuceneSimple(entailmentGraph); 
			nm.indexGraphNodes();
			nm.initializeSearch();
			
			/************* TEST 1: EXACT MATCH ***************/
			testlogger.info("--------------------- TEST 1 -------------------"); 			
			String text = "Die Punkte lösen mein Problem leider nicht";
			testlogger.info("Creating JCas for '"+text+"'."); 			
			JCas jcas = CASUtils.createNewInputCas();			
			jcas.setDocumentText(text);
			Region[] fragmentRegions = new Region[1];
			fragmentRegions[0] = new Region(0,text.length());
			CASUtils.annotateOneDeterminedFragment(jcas, fragmentRegions);
			testlogger.info("Adding modifier: 'leider'"); 			
			Region[] modifierRegions = new Region[1];
			modifierRegions[0] = new Region(17,21);
			CASUtils.annotateOneModifier(jcas, modifierRegions);
			testlogger.info("Creating fragment graph from CAS."); 			
			FragmentGraphGenerator fgg = new FragmentGraphGeneratorFromCAS();
			Set<FragmentGraph> fragmentGraphs = fgg.generateFragmentGraphs(jcas);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraphs.iterator().next());
			testlogger.info("Number of returned matches: " + matches.size());
			Assert.assertEquals(1, matches.size()); //should return a single match
			String expectedMatch = "Die Punkte lösen mein Problem leider nicht.";
			for (NodeMatch nodeMatch : matches) {
				Assert.assertEquals(expectedMatch, nodeMatch.getScores().get(0).getNode().getLabel().trim());
				testlogger.info("Matching node: " + expectedMatch); 			
				/*				for (PerNodeScore score : nodeMatch.getScores()) {
				Assert.assertEquals(1.0, score.getScore());
				testlogger.info("Associated score: 1.0'"); 			
			}*/
			}


			/************* TEST 2: EXACT MATCH IGNORING CAPITALIZATION ***************/
			testlogger.info("--------------------- TEST 2 -------------------"); 			
			text = "die punkte lösen mein problem nicht.";
			testlogger.info("Creating fragment graph for sentence '"+text+"'"); 			
			JCas jcas2 = CASUtils.createNewInputCas();			
			jcas2.setDocumentText(text);
			fragmentRegions = new Region[1];
			fragmentRegions[0] = new Region(0,text.length());
			CASUtils.annotateOneDeterminedFragment(jcas2, fragmentRegions);
			testlogger.info("Creating fragment graph from CAS."); 			
			fragmentGraphs = fgg.generateFragmentGraphs(jcas2);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			matches = nm.findMatchingNodesInGraph(fragmentGraphs.iterator().next());
			Assert.assertEquals(1, matches.size()); 
			testlogger.info("NodeMatcher matches a single node."); 			
			expectedMatch = "Die Punkte lösen mein Problem nicht.";
			for (NodeMatch nodeMatch : matches) {
				Assert.assertEquals(expectedMatch, nodeMatch.getScores().get(0).getNode().getLabel().trim());
				testlogger.info("Matching node: " + expectedMatch); 			
				/*				for (PerNodeScore score : nodeMatch.getScores()) {
				Assert.assertEquals(1.0, score.getScore());
				testlogger.info("Associated score: 1.0'"); 			
			}*/
			}

			/************* TEST 3: MATCH WITH DIFFERING WORD ORDER ***************/
			testlogger.info("--------------------- TEST 3 -------------------"); 			
			text = "Leider lösen die Punkte mein Problem nicht";
			testlogger.info("Creating fragment graph for sentence '"+text+"'."); 			
			JCas jcas3 = CASUtils.createNewInputCas();			
			jcas3.setDocumentText(text);
			fragmentRegions = new Region[1];
			fragmentRegions[0] = new Region(0,text.length());
			CASUtils.annotateOneDeterminedFragment(jcas3, fragmentRegions);
			testlogger.info("Adding modifier: 'leider'"); 			
			modifierRegions = new Region[1];
			modifierRegions[0] = new Region(1,6);
			CASUtils.annotateOneModifier(jcas3, modifierRegions);
			fragmentGraphs = fgg.generateFragmentGraphs(jcas3);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			matches = nm.findMatchingNodesInGraph(fragmentGraphs.iterator().next());
			Assert.assertEquals(1, matches.size()); //should return a single match
			expectedMatch = "Die Punkte lösen mein Problem leider nicht.";
			for (NodeMatch nodeMatch : matches) {
				Assert.assertEquals(expectedMatch, nodeMatch.getScores().get(0).getNode().getLabel().trim());
				testlogger.info("Matching node: " + expectedMatch); 			
				/*				for (PerNodeScore score : nodeMatch.getScores()) {
				Assert.assertEquals(1.0, score.getScore());
				testlogger.info("Associated score: 1.0'"); 			
			}*/
			}
			
			
			/************* TEST 4: PROPER FRAGMENT ANNOTATION (REMOVING EXTRA INFO) ***************/
			testlogger.info("--------------------- TEST 4 -------------------"); 			
			text = "Leider lösen die Punkte mein Problem nicht, wie doof";
			testlogger.info("Creating fragment graph for sentence '"+text+"'."); 			
			JCas jcas4 = CASUtils.createNewInputCas();			
			jcas4.setDocumentText(text);
			fragmentRegions = new Region[1];
			fragmentRegions[0] = new Region(0,text.length()-10); //Fragment annotation removes "wie doof"
			CASUtils.annotateOneDeterminedFragment(jcas4, fragmentRegions);
			testlogger.info("Adding modifier: 'leider'"); 			
			modifierRegions = new Region[1];
			modifierRegions[0] = new Region(1,6);
			CASUtils.annotateOneModifier(jcas4, modifierRegions);
			fragmentGraphs = fgg.generateFragmentGraphs(jcas4);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			matches = nm.findMatchingNodesInGraph(fragmentGraphs.iterator().next());
			Assert.assertEquals(1, matches.size()); //should return a single match
			expectedMatch = "Die Punkte lösen mein Problem leider nicht.";
			for (NodeMatch nodeMatch : matches) {
				Assert.assertEquals(expectedMatch, nodeMatch.getScores().get(0).getNode().getLabel().trim());
				testlogger.info("Matching node: " + expectedMatch); 			
/*				for (PerNodeScore score : nodeMatch.getScores()) {
					Assert.assertEquals(1.0, score.getScore());
					testlogger.info("Associated score: 1.0'"); 			
				}*/
			}
			
			testlogger.info("No problem observed on the test cases"); 
			
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}
}

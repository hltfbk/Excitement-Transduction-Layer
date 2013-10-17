package eu.excitementproject.tl.composition.categoryannotator;

import static org.junit.Assert.*;

import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import eu.excitement.type.tl.CategoryAnnotation;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.api.ConfidenceCalculator;
import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.composition.confidencecalculator.ConfidenceCalculatorCategoricalFrequencyDistribution;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLongestOnly;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.LemmaLevelLapEN;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;


public class CategoryAnnotatorAllCatsTest {

	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.composition.categoryannotator.test"); 
		
		try {					
			/************* TEST 1 ***************/
			testlogger.info("Reading sample raw entailment graph."); 			
			EntailmentGraphRaw rawGraph = EntailmentGraphRaw.getSampleOuputWithCategories(false); 	
			GraphOptimizer cgg = new SimpleGraphOptimizer();
			testlogger.info("Creating collapsed entailment graph from sample graph."); 			
			EntailmentGraphCollapsed entailmentGraph = cgg.optimizeGraph(rawGraph);
			ConfidenceCalculator cc = new ConfidenceCalculatorCategoricalFrequencyDistribution();
			cc.computeCategoryConfidences(entailmentGraph);
			testlogger.info("Creating a sample CAS.");
			JCas cas = CASUtils.createNewInputCas(); 
			cas.setDocumentText("Hello Mister Grey. Disappointed with legroom"); 
			cas.setDocumentLanguage("EN"); 						
			testlogger.info("Adding fragment annotation to CAS.");
			LAPAccess lap = new LemmaLevelLapEN();
			FragmentAnnotator fa = new SentenceAsFragmentAnnotator(lap); 
			fa.annotateFragments(cas);
			testlogger.info("Adding modifier annotation to CAS."); 			
			ModifierAnnotator ma = new AdvAsModifierAnnotator(lap);
			ma.annotateModifiers(cas);
			testlogger.info("Creating fragment graphs for CAS."); 			
			FragmentGraphGenerator fgg = new FragmentGraphGeneratorFromCAS();
			Set<FragmentGraph> fragmentGraphs = fgg.generateFragmentGraphs(cas);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			NodeMatcher nm = new NodeMatcherLongestOnly(entailmentGraph); 
			for (FragmentGraph fragmentGraph : fragmentGraphs) {
				Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraph);
				testlogger.info("Calling category annotator."); 			
				CategoryAnnotator ca = new CategoryAnnotatorAllCats();
				ca.addCategoryAnnotation(cas, matches);
			}
			Set<CategoryDecision> categoryDecisions = CASUtils.getCategoryAnnotationsInCAS(cas);
			
			Assert.assertEquals(1, categoryDecisions.size()); 
			testlogger.info("Dumping Category Annotation."); 			
			CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}
}

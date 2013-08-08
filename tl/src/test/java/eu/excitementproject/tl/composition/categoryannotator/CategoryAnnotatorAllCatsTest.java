package eu.excitementproject.tl.composition.categoryannotator;

import static org.junit.Assert.fail;

import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.api.CollapsedGraphGenerator;
import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.composition.collapsedgraphgenerator.SimpleCollapseGraphGenerator;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLongestOnly;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.laputils.CASUtils;
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
			CollapsedGraphGenerator cgg = new SimpleCollapseGraphGenerator();
			testlogger.info("Creating collapsed entailment graph from sample graph."); 			
			EntailmentGraphCollapsed entailmentGraph = cgg.generateCollapsedGraph(rawGraph);
			testlogger.info("Creating a sample CAS.");
			JCas cas = CASUtils.createNewInputCas(); 
			cas.setDocumentText("Hello Mister Grey. I have to say that I'm very Disappointed with the amount of legroom compared with other trains. Best, Sarah White"); 
			cas.setDocumentLanguage("EN"); 						
			testlogger.info("Adding fragment annotation to CAS.");
			/** TODO: uncomment when LAP is available 
			LAPAccess lap = null; //TODO: Replace by instantiated LAP
			FragmentAnnotator fa = new SentenceAsFragmentAnnotator(lap); 
			fa.annotateFragments(cas);
			testlogger.info("Adding modifier annotation to CAS."); 			
			ModifierAnnotator ma = new AdvAsModifierAnnotator(lap);
			ma.annotateModifiers(cas);
			*/
			testlogger.info("Creating fragment graphs for CAS."); 			
			FragmentGraphGenerator fgg = new FragmentGraphGeneratorFromCAS();
			Set<FragmentGraph> fragmentGraphs = fgg.generateFragmentGraphs(cas);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			NodeMatcher nm = new NodeMatcherLongestOnly(); 
			for (FragmentGraph fragmentGraph : fragmentGraphs) {
				Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraph, entailmentGraph);
				testlogger.info("Calling category annotator."); 			
				CategoryAnnotator ca = new CategoryAnnotatorAllCats();
				ca.addCategoryAnnotation(cas, matches);
			}
			//Assert.assertEquals("3", get-category-annotation-for-fragment); 
			//TODO: Find out how to do this!	
			testlogger.info("Dumping CAS."); 			
			CASUtils.dumpCAS(cas);
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}
}

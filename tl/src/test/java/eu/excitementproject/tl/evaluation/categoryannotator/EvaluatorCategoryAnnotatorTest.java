package eu.excitementproject.tl.evaluation.categoryannotator;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.api.CollapsedGraphGenerator;
import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.composition.categoryannotator.CategoryAnnotatorAllCats;
import eu.excitementproject.tl.composition.collapsedgraphgenerator.SimpleCollapseGraphGenerator;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLongestOnly;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;

public class EvaluatorCategoryAnnotatorTest {
	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.evaluation.categoryannotator.test"); 
		
		try {					
			/************* TEST 1 ***************/
			String filename = "./src/test/outputs/collapsed_graph_omq.xml";
			testlogger.info("Reading collapsed entailment graph from file " + filename); 	
			EntailmentGraphCollapsed graph = new EntailmentGraphCollapsed(new File(filename));
			testlogger.info("Creating a sample CAS.");
			JCas cas = CASUtils.createNewInputCas(); 
			cas.setDocumentText("Hello Mister Grey. I have to say that I'm very Disappointed with the amount of legroom compared with other trains. Best, Sarah White"); 
			cas.setDocumentLanguage("EN"); 						
			testlogger.info("Adding fragment annotation to CAS.");
			LAPAccess lap = new LemmaLevelLapDE();
			FragmentAnnotator fa = new SentenceAsFragmentAnnotator(lap); 
			fa.annotateFragments(cas);
			testlogger.info("Adding modifier annotation to CAS."); 			
			ModifierAnnotator ma = new AdvAsModifierAnnotator(lap);
			ma.annotateModifiers(cas);
			testlogger.info("Creating fragment graphs for CAS."); 			
			FragmentGraphGenerator fgg = new FragmentGraphGeneratorFromCAS();
			Set<FragmentGraph> fragmentGraphs = fgg.generateFragmentGraphs(cas);
			testlogger.info("Calling node matcher on the fragment graph."); 			
			NodeMatcher nm = new NodeMatcherLongestOnly(); 
			nm.setEntailmentGraph(graph);
			for (FragmentGraph fragmentGraph : fragmentGraphs) {
				Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraph);
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

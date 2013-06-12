package eu.excitementproject.tl.composition.categoryannotator;

import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.composition.categoryannotator.CategoryAnnotatorAllCats;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLongestOnly;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;


public class CategoryAnnotatorAllCatsTest {

	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.composition.categoryannotator.test"); 
		
		try {
			testlogger.info("Creating a sample CAS.");
			JCas aJCas = CASUtils.createNewInputCas(); 
			aJCas.setDocumentText("Hello Mister Grey. I have to say that I'm very Disappointed with the amount of legroom compared with other trains. Best, Sarah White"); 
			aJCas.setDocumentLanguage("EN"); 						
					
			/************* TEST 1 ***************/
			testlogger.info("Reading sample entailment graph."); 			
			EntailmentGraphRaw entailmentGraph = EntailmentGraphRaw.getSampleOuput(false); 
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
			testlogger.info("Calling category annotator."); 			
			CategoryAnnotator ca = new CategoryAnnotatorAllCats();
			ca.addCategoryAnnotation(aJCas, matches);
			testlogger.info("Dumping CAS."); 			
			CASUtils.dumpCAS(aJCas);
		}
		catch (Exception e)
		{
			fail(e.getMessage()); 
		}
	}
}

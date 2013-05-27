package eu.excitementproject.tl.demo;

import java.io.File;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;

/**
 * Shows OMQ use case data flow. 
 * 
 * @param args
 * @throws FragmentAnnotatorException
 * @throws ModifierAnnotatorException
 * @throws FragmentGraphGeneratorException
 * @throws NodeMatcherException
 * @throws CategoryAnnotatorException
 * @throws LAPException 
 */
public class DemoUseCase2OMQGerman {
	
	public static void main(String[] args) throws FragmentAnnotatorException, ModifierAnnotatorException, 
		FragmentGraphGeneratorException, NodeMatcherException, CategoryAnnotatorException, LAPException {

		//read in some entailment graph
		EntailmentGraphRaw entailmentGraph = EntailmentGraphRaw.getSampleOuput(true); //TODO: replace with OMQ example graph
		//System.out.println(entailmentGraph);
		
		//create some sample input
		JCas cas = CASUtils.createNewInputCas();
		//some CAS
		CASUtils.deserializeFromXmi(cas, new File("target/CASInput_example_4.xmi")); //TODO: replace with OMQ example
		
		//add fragment annotation
		LAPAccess lap = new TreeTaggerEN(); 			
		FragmentAnnotator fa = new SentenceAsFragmentAnnotator(lap); 
		fa.annotateFragments(cas);
		
		//add modifier annotation
		ModifierAnnotator ma = null; //TODO: replace with implemementation
		ma.annotateModifiers(cas);
		
		//create fragment graphs
		FragmentGraphGenerator fgg = null; // TODO: replace with implementation
		Set<FragmentGraph> fragmentGraphs = fgg.generateFragmentGraphs(cas);

		//call node matcher on each fragment graph
		NodeMatcher nm = null; // TODO: replace with implementation
		CategoryAnnotator ca = null;
		for (FragmentGraph fragmentGraph: fragmentGraphs) {
			Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraph, entailmentGraph);
			//add category annotation to CAS
			ca.addCategoryAnnotation(cas, matches);
		}
		
		//print CAS
		CASUtils.dumpCAS(cas);
	}

}

package eu.excitementproject.tl.toplevel.usecasetworunner;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import arkref.parsestuff.U;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;
import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.composition.categoryannotator.CategoryAnnotatorAllCats;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.composition.nodematcher.NodeMatcherLongestOnly;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.toplevel.api.UseCaseTwoRunner;

/**
 * The UseCaseTwoRunnerPrototype provides an implementation of the UseCaseTwoRunner interface, 
 * which has one interface method annotateCategories(JCas cas, EntailmentGraphRaw graph). 
 * In the current implementation, this method annotates categories on the given input CAS based 
 * on an input entailment graph using the following module implementations:
 * 
 * - SentenceAsFragmentAnnotator: This module adds fragment annotation to the input CAS.
 * - AdvAsModifierAnnotator: This module adds modifier annotation to the input CAS.
 * - FragmentGraphGeneratorFromCAS: This module generates fragment graphs for the annotated fragments, using the modifier annotation.
 * - NodeMatcherLongestOnly: This module compares the created fragment graphs to the input entailment graph to find matching nodes.
 * - CategoryAnnotatorAllCats: This module adds category annotation to the input CAS by combining category information from the matching nodes.
 * 
 * @author Kathrin Eichler
 *
 */

public class UseCaseTwoRunnerPrototype implements UseCaseTwoRunner {

	LAPAccess lap = null;
	EDABasic<?> eda = null;
	
	public UseCaseTwoRunnerPrototype(LAPAccess lap, EDABasic<?> eda) {
		this.lap = lap;
		this.eda = eda;
	}
	
	@Override
	public void annotateCategories(JCas cas, EntailmentGraphRaw graph) 
			throws FragmentAnnotatorException, ModifierAnnotatorException, FragmentGraphGeneratorException, LAPException, NodeMatcherException, CategoryAnnotatorException {
		//add fragment annotation
		FragmentAnnotator fa = new SentenceAsFragmentAnnotator(lap); 
		fa.annotateFragments(cas);
		
		//add modifier annotation
		ModifierAnnotator ma = new AdvAsModifierAnnotator(lap);
		ma.annotateModifiers(cas);
		
		//create fragment graphs
		FragmentGraphGenerator fgg = new FragmentGraphGeneratorFromCAS();
		Set<FragmentGraph> fragmentGraphs = fgg.generateFragmentGraphs(cas);

		//call node matcher on each fragment graph
		NodeMatcher nm = new NodeMatcherLongestOnly();
		CategoryAnnotator ca = new CategoryAnnotatorAllCats();
		for (FragmentGraph fragmentGraph: fragmentGraphs) {
			Set<NodeMatch> matches = nm.findMatchingNodesInGraph(fragmentGraph, graph);
			System.out.println("CATEGORY:" +
					matches.iterator().next().getScores().get(0).getNode().getMentions().iterator().next().getCategoryId());
			//add category annotation to CAS
			ca.addCategoryAnnotation(cas, matches);
		}	
	}
}

package eu.excitementproject.tl.decomposition.fragmentgraphgenerator;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;

public class FragmentGraphGeneratorFromCAS extends
		AbstractFragmentGraphGenerator {

	@Override
	public Set<FragmentGraph> generateFragmentGraphs(JCas text)
			throws FragmentGraphGeneratorException {
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();
				
		for(Annotation a : text.getAnnotationIndex(DeterminedFragment.type)) {
			fgs.add(new FragmentGraph(text,(DeterminedFragment) a));
		}
		
		return fgs;
	}
}

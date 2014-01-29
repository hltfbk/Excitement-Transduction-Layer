package eu.excitementproject.tl.decomposition.fragmentgraphgenerator;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphNoNeg;


public class FragmentGraphNoNegGeneratorFromCAS extends
		AbstractFragmentGraphGenerator {

	private final static Logger logger = Logger.getLogger(FragmentGraphNoNegGeneratorFromCAS.class.getName());
	
	@Override
	public Set<FragmentGraph> generateFragmentGraphs(JCas text)
			throws FragmentGraphGeneratorException {
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();
				
		for(Annotation a : text.getAnnotationIndex(DeterminedFragment.type)) {
			logger.info("Processing fragment: " + a.getCoveredText());
			fgs.add(new FragmentGraphNoNeg(text,(DeterminedFragment) a));
		}
		return fgs;
	}
		
}

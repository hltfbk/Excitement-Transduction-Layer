package eu.excitementproject.tl.decomposition.fragmentgraphgenerator;

import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphLite;


public class FragmentGraphLiteGeneratorFromCAS extends
		AbstractFragmentGraphGenerator {

	private final static Logger logger = Logger.getLogger(FragmentGraphLiteGeneratorFromCAS.class.getName());
	
	@Override
	public Set<FragmentGraph> generateFragmentGraphs(JCas text)
			throws FragmentGraphGeneratorException {
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();
				
		for(Annotation a : text.getAnnotationIndex(DeterminedFragment.type)) {
			logger.info("Processing fragment: " + a.getCoveredText());
//			if (!(a.getCoveredText().contains("Moonport") && a.getCoveredText().contains("Quasigo terminal"))) {
				fgs.add(new FragmentGraphLite(text,(DeterminedFragment) a));
//			}
		}
		return fgs;
	}
		
}

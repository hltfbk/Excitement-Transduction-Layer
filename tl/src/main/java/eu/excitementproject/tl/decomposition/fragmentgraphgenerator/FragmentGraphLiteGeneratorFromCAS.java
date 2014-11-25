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
	
	/**
	 * generates "lite" fragment graphs from each fragment annotation in a CAS object
	 * "lite" fragment graph means the graph will have at most 2 nodes, corresponding to the top statement (full fragment text)
	 * and to the base statement (the fragment without any of the identified modifiers). 
	 * 
	 * This was necessary because ALMA data had numerous modifiers which gave rise to huge fragment graphs
	 * 
	 * @param text -- a CAS object with fragment annotations
	 */
	@Override
	public Set<FragmentGraph> generateFragmentGraphs(JCas text)
			throws FragmentGraphGeneratorException {
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();
				
		for(Annotation a : text.getAnnotationIndex(DeterminedFragment.type)) {
			logger.info("Processing fragment: " + a.getCoveredText());
			fgs.add(new FragmentGraphLite(text,(DeterminedFragment) a));
		}
		return fgs;
	}
		
}

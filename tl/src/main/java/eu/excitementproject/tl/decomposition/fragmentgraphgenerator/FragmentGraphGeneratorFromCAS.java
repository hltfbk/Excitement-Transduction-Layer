package eu.excitementproject.tl.decomposition.fragmentgraphgenerator;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;

public class FragmentGraphGeneratorFromCAS extends
		AbstractFragmentGraphGenerator {

	@Override
	public Set<FragmentGraph> generateFragmentGraphs(JCas text)
			throws FragmentGraphGeneratorException {
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>();
		
		makeUniqueId(text);
		
		for(Annotation a : text.getAnnotationIndex(DeterminedFragment.type)) {
			fgs.add(new FragmentGraph(text,(DeterminedFragment) a));
		}
		
		return fgs;
	}

	private void makeUniqueId(JCas text) {
		String uniqueInteractionId = CASUtils.getTLMetaData(text).getInteractionId() + getTimestamp(); 
		CASUtils.getTLMetaData(text).setInteractionId(uniqueInteractionId);
	}
	
	private String getTimestamp(){
		java.util.Date date= new java.util.Date();
		 return new Timestamp(date.getTime()).toString();
	}
}

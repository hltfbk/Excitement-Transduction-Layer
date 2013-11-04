package eu.excitementproject.tl.toplevel.api;

import org.apache.uima.jcas.JCas;


import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;

/**
 * This top level interface configures and runs available TL components to instantiate one 
 * “instance” of the transduction layer that will work for WP7’s use case 2.
 * 
 * @author Kathrin Eichler
 *
 */

public interface UseCaseTwoRunner {
	
	void annotateCategories(JCas cas, EntailmentGraphCollapsed graph) 
			throws FragmentAnnotatorException, ModifierAnnotatorException, FragmentGraphGeneratorException, LAPException, NodeMatcherException, CategoryAnnotatorException;
	
}

package eu.excitementproject.tl.composition.categoryannotator;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.structures.search.NodeMatch;


/**
 An implementation of the {@link CategoryAnnotator} interface
 * @author Kathrin Eichler
 * 
 */
public abstract class AbstractCategoryAnnotator implements CategoryAnnotator {

	public void addCategoryAnnotation(JCas cas, Set<NodeMatch> matches) 
			throws CategoryAnnotatorException,
			LAPException {
	}
	
}

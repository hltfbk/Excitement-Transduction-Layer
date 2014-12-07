package eu.excitementproject.tl.composition.api;

import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.structures.search.NodeMatch;

/**
 * Use case 2 composition module "CategoryAnnotator" 
 * 
 * This module annotates category information on the input CAS.
 * Input: one input CAS (=JCas=), one set of NodeMatch-es (=Set<NodeMatch>=) 
 * Output: no new data, but the input CAS is now annotated with category information.
 * Failure: any annotation failure is reported as an exception. 
 * 
 * @author Kathrin Eichler
 *
 */

public interface CategoryAnnotator {

	/**
	 * @param cas - input CAS ({@link JCas})
	 * @param matches - set of matching nodes ({@link MatchingNode})
	 * @throws CategoryAnnotatorException if category annotation fails
	 * @throws LAPException 
	 */
	public void addCategoryAnnotation(JCas cas, Set<NodeMatch> matches) 
			throws CategoryAnnotatorException, LAPException;
	
}

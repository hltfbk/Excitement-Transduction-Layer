package eu.excitementproject.tl.composition.api;

import eu.excitementproject.tl.composition.exceptions.ConfidenceCalculatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;

/**
 * Confidence Calculator Module
 * 
 * This module reads category confidence scores stored in a collapsed graph, combines them 
 * to a final score per category per node and adds this information to the graph. 
 * 
 * If the implementation fails to compute the confidence scores or to add them to the graph, 
 * it will raise an exception.  
 * 
 * Input: one collapsed graph (=EntailmentGraphCollapsed=)
 * Output: the input collapsed graph with added category confidences per node (=EquivalenceClass=)
 * 
 *  @author Kathrin
 */

public interface ConfidenceCalculator {
	
	/**
	 * @param workGraph - one entailment graph ({@link EntailmentGraphCollapsed})
	 * @throws ConfidenceCalculatorException if the implementation cannot calculate or add confidence scores
	 */
	public void computeCategoryConfidences(EntailmentGraphCollapsed graph) 
			throws ConfidenceCalculatorException;


}

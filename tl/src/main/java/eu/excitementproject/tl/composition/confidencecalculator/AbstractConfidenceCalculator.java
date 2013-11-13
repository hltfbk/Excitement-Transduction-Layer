package eu.excitementproject.tl.composition.confidencecalculator;

import eu.excitementproject.tl.composition.api.ConfidenceCalculator;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;


/** An implementation of the {@link ConfidenceCalculator} interface
 * 
 * @author Kathrin Eichler
 * 
 */

public abstract class AbstractConfidenceCalculator implements ConfidenceCalculator {
	
	/**
	 * @param graph: the collapsed graph, which is enriched with confidence scores per node
	 */
	public void addConfidenceScores(EntailmentGraphCollapsed graph){
	}
}

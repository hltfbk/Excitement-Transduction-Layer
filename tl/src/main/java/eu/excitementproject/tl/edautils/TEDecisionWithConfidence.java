package eu.excitementproject.tl.edautils;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

/**
 * A {@link TEDecision}, which gets a confidence value and a {@link DecisionLabel} as input
 * @author Lili Kotlerman
 *
 */
public class TEDecisionWithConfidence implements TEDecision{
	
	/**
	 * Confidence of the decision
	 */
	final Double confidence;
	
	/**
	 * Label of the decision
	 */
	DecisionLabel decision;
	
	/** Constructor
	 * @param confidence
	 * @param decision
	 */
	public TEDecisionWithConfidence(Double confidence, DecisionLabel decision){
		this.confidence = confidence; 
		this.decision = decision;
	}
	
	@Override
	public DecisionLabel getDecision() {
		return decision;
	}

	@Override
	public double getConfidence() {
		return confidence;
	}

	@Override
	public String getPairID() {
		// TODO: understand how to extract this from the CAS (this method returns the entailment.Pair id as described in the CAS.)
		// meanwhile return null
		return null;
	}
	
}

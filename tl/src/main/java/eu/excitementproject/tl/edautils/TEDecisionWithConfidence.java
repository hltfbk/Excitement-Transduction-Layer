package eu.excitementproject.tl.edautils;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

/**
 * A TEDecision, which gets a confidence score as input and defines the corresponding DecisionLabel, based on a simple score heuristic
 * @author LiliKotlerman
 *
 */
public class TEDecisionWithConfidence implements TEDecision{
	
	final Double confidence;
	DecisionLabel decision;
	
	public TEDecisionWithConfidence(Double confidence, DecisionLabel decision){
		this.confidence = confidence; 
		this.decision = decision;
	}
	
	public DecisionLabel getDecision() {
		return decision;
	}

	public double getConfidence() {
		return confidence;
	}

	public String getPairID() {
		
		// TODO: understand how to extract this from the CAS (this method returns the entailment.Pair id as described in the CAS.)
		// meanwhile return null
		return null;
	}
	
}

package eu.excitementproject.tl.edautils;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

/**
 * A TEDecision, which gets a confidence score as input and defines the corresponding DecisionLabel, based on a simple score heuristic
 * @author LiliKotlerman
 *
 */
public class TEDecisionByScore implements TEDecision{
	
	final Double confidence;
	
	public TEDecisionByScore(Double confidence){
		this.confidence = confidence; 
	}
	
	public DecisionLabel getDecision() {
		if(confidence > 0.7) return DecisionLabel.Entailment;
		else if (confidence < 0.3) return DecisionLabel.NonEntailment;
		return DecisionLabel.Unknown;
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

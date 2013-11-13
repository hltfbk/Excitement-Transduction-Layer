package eu.excitementproject.tl.structures.rawgraph.utils;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

/**
 * A TEDecision with a random confidence score and a corresponding DecisionLabel, based on a simple score heuristic
 * @author LiliKotlerman
 *
 */
public class RandomTEDecision implements TEDecision{
	
	final Double randomConfidence;
	
	public RandomTEDecision(Double randomConfidence){
		this.randomConfidence = randomConfidence; 
	}
	
	public DecisionLabel getDecision() {
		if (randomConfidence > 0.95) return DecisionLabel.Paraphrase;
		else if(randomConfidence > 0.8) return DecisionLabel.Entailment;
		else if (randomConfidence < 0.5) return DecisionLabel.NonEntailment;
		return DecisionLabel.Unknown;
	}

	public double getConfidence() {
		return randomConfidence;
	}

	public String getPairID() {
		// TODO: understand how to extract this from the CAS (this method returns the entailment.Pair id as described in the CAS.)
		// meanwhile return null
		return null;
	}
	
}

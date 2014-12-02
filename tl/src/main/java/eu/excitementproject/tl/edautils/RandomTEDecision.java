package eu.excitementproject.tl.edautils;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

/**
 * A {@link TEDecision} with a random confidence score and a corresponding {@link DecisionLabel}, based on a simple score heuristic
 * @author Lili Kotlerman
 *
 */
public class RandomTEDecision implements TEDecision{
	
	/**
	 * Decision score, generated as a random double
	 */
	final Double randomConfidence;
	
	/** Constructor
	 * @param randomConfidence
	 */
	public RandomTEDecision(Double randomConfidence){
		this.randomConfidence = randomConfidence; 
	}
	
	@Override
	public DecisionLabel getDecision() {
		if (randomConfidence > 0.95) return DecisionLabel.Paraphrase;
		else if(randomConfidence > 0.8) return DecisionLabel.Entailment;
		else if (randomConfidence < 0.5) return DecisionLabel.NonEntailment;
		return DecisionLabel.Unknown;
	}

	@Override
	public double getConfidence() {
		return randomConfidence;
	}

	@Override
	public String getPairID() {
		// TODO: understand how to extract this from the CAS (this method returns the entailment.Pair id as described in the CAS.)
		// meanwhile return null
		return null;
	}
	
}

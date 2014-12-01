package eu.excitementproject.tl.edautils;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

/**
 * A {@link TEDecision}, which gets a confidence score as input 
 * and defines the corresponding {@link DecisionLabel}, based on a simple score heuristic
 * @author Lili Kotlerman
 *
 */
public class TEDecisionByScore implements TEDecision{
	
	/**
	 * Score, generated as a random double between 0.0 and 1.0
	 */
	final Double score;
	
	/** Constructor
	 * @param score
	 */
	public TEDecisionByScore(Double score){
		this.score = score; 
	}
	
	@Override
	public DecisionLabel getDecision() {
		if(score > 0.7) return DecisionLabel.Entailment;
		else if (score < 0.3) return DecisionLabel.NonEntailment;
		return DecisionLabel.Unknown;
	}

	@Override
	public double getConfidence() {
		return score;
	}

	@Override
	public String getPairID() {		
		// TODO: understand how to extract this from the CAS (this method returns the entailment.Pair id as described in the CAS.)
		// meanwhile return null
		return null;
	}
	
}

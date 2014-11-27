package eu.excitementproject.tl.edautils;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

/**
 * A {@link TEDecision} with a random confidence score and a corresponding {@link DecisionLabel}
 * @author Lili Kotlerman
 *
 */
public class ProbabilisticTEDecision implements TEDecision{
	
	/**
	 * Decision score, generated as a random double
	 */
	final Double decisionConfidence;
	
	/**
	 * The pre-defined probability of (positive) entailment decision 
	 */
	Double entailmentProbability;
	
	/**
	 * Probability of entailment to be used if {@link ProbabilisticTEDecision#entailmentProbability} is not defined
	 */
	final Double DEFAULT_PROBABILITY = 0.5; // if entailmentProbability not specified, use 0.5 (random)
	
	/** Constructor, which will set entailment probability to DEFAULT_PROBABILITY
	 * @param decisionConfidence
	 */
	public ProbabilisticTEDecision(Double decisionConfidence){
		this.decisionConfidence = decisionConfidence;
		entailmentProbability = DEFAULT_PROBABILITY;
	}
	
	/** Constructor
	 * @param decisionConfidence
	 * @param entailmentProbability
	 */
	public ProbabilisticTEDecision(Double decisionConfidence, Double entailmentProbability){
		this.decisionConfidence = decisionConfidence;
		if (entailmentProbability==null) entailmentProbability = DEFAULT_PROBABILITY;
		else this.entailmentProbability = entailmentProbability;
	}
	

	@Override
	public DecisionLabel getDecision() {
		if (decisionConfidence >= entailmentProbability) return DecisionLabel.Entailment;
		return DecisionLabel.NonEntailment;
	}
	
	@Override
	public double getConfidence() {
		return decisionConfidence;
	}

	@Override
	public String getPairID() {
		// TODO: understand how to extract this from the CAS (this method returns the entailment.Pair id as described in the CAS.)
		// meanwhile return null
		return null;
	}
	
}

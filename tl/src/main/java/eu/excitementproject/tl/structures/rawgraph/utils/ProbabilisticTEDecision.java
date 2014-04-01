package eu.excitementproject.tl.structures.rawgraph.utils;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

/**
 * A TEDecision with a random confidence score and a corresponding DecisionLabel, based on a simple score heuristic
 * @author LiliKotlerman
 *
 */
public class ProbabilisticTEDecision implements TEDecision{
	
	final Double decisionConfidence;
	Double entailmentProbability;
	final Double RANDOM_PROBABILITY = 0.5; // if entailmentProbability not specified, use 0.5 (random)
	
	public ProbabilisticTEDecision(Double decisionConfidence){
		this.decisionConfidence = decisionConfidence;
		entailmentProbability = RANDOM_PROBABILITY;
	}
	
	public ProbabilisticTEDecision(Double decisionConfidence, Double entailmentProbability){
		this.decisionConfidence = decisionConfidence;
		if (entailmentProbability==null) entailmentProbability = RANDOM_PROBABILITY;
		else this.entailmentProbability = entailmentProbability;
	}
	
/*	public DecisionLabel getDecision() {
		if (decisionConfidence > 0.95) return DecisionLabel.Paraphrase;
		else if(decisionConfidence > 0.8) return DecisionLabel.Entailment;
		else if (decisionConfidence < 0.5) return DecisionLabel.NonEntailment;
		return DecisionLabel.Unknown;
	}*/

	public DecisionLabel getDecision() {
		if (decisionConfidence >= entailmentProbability) return DecisionLabel.Entailment;
		return DecisionLabel.NonEntailment;
	}
	
	public double getConfidence() {
		return decisionConfidence;
	}

	public String getPairID() {
		// TODO: understand how to extract this from the CAS (this method returns the entailment.Pair id as described in the CAS.)
		// meanwhile return null
		return null;
	}
	
}

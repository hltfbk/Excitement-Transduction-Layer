package eu.excitementproject.tl.structures.rawgraph;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

public class RandomTEDecision implements TEDecision{
	
	final Double randomConfidence;
	
	public RandomTEDecision(Double randomConfidence){
		this.randomConfidence = randomConfidence; 
	}
	
	public DecisionLabel getDecision() {
		if (randomConfidence > 0.9) return DecisionLabel.Paraphrase;
		else if(randomConfidence > 0.7) return DecisionLabel.Entailment;
		else if (randomConfidence < 0.3) return DecisionLabel.NonEntailment;
		return DecisionLabel.Unknown;
	}

	public double getConfidence() {
		return randomConfidence;
	}

	public String getPairID() {
		// ToDo: understand how to extract this from the CAS (this method returns the entailment.Pair id as described in the CAS.)
		// meanwhile return null
		return null;
	}
	
}

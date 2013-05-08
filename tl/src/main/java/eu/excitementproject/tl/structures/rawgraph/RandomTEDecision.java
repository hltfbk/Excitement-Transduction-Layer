package eu.excitementproject.tl.structures.rawgraph;

import java.util.Random;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.TEDecision;

public class RandomTEDecision implements TEDecision{
	Random generator = new Random(); 
	final Double randomConfidence;
	
	public RandomTEDecision(){
		randomConfidence = generator.nextDouble();
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
		return null;
	}

}

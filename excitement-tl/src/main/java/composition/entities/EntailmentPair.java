package composition.entities;

import decomposition.entities.EntailmentUnit;

/**
 * This class represents the data structure that holds an entailment pair. 
 * Entailment pairs refer to candidate T/H pairs to be sent to the EDA.
 *  
 * @author Kathrin
 */

public class EntailmentPair {
	
	private EntailmentUnit hypothesis;
	private EntailmentUnit text;
//	private TEDecision decision; TAKEN FROM OP code!
	
	public EntailmentUnit getHypothesis() {
		return hypothesis;
	}
	
	public void setHypothesis(EntailmentUnit hypothesis) {
		this.hypothesis = hypothesis;
	}
	
	public EntailmentUnit getText() {
		return text;
	}
	
	public void setText(EntailmentUnit text) {
		this.text = text;
	}

	
}

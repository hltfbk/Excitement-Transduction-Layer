package common.graph.entities;

import java.util.Map;

/**
 * This class represents the data structure that holds an entailment relation. 
 * An entailment relation refers to an edge in the raw entailment graph. 
 *  
 * @author Kathrin
 */

public class EntailmentRelation {

//	private int ID;
//	private int from;
//	private int to;
	private Map<String, Float> confidenceScores; //should be a map, because we want to be able to store different scores for different EDAs
	
	public DecisionLabel label;	// the type: entailment, non-entailment, ... -- DecisionLabel will be imported from the EOP 

	/*
	public int getID() {
		return ID;
	}
	
	public void setID(int iD) {
		ID = iD;
	}

	public int getFrom() {
		return from;
	}
	
	public void setFrom(int from) {
		this.from = from;
	}
	
	public int getTo() {
		return to;
	}
	
	public void setTo(int to) {
		this.to = to;
	}
	*/
	public Map<String, Double> getConfidenceScores() {
		return confidenceScores;
	}
	
	public void setConfidenceScores(Map<String, Double> confidenceScores) {
		this.confidenceScores = confidenceScores;
	}
	
	
}

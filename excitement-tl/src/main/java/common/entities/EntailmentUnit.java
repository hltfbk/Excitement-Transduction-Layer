package decomposition.entities;

import graph.entities.Node;

import java.util.List;
import java.util.Map;

import org.apache.uima.jcas.JCas;

/**
 * This class represents the data structure that holds an entailment unit. 
 * Entailment units function as T and H part of a candidate T/H pair and as 
 * nodes in the EntailmentGraphRaw. 
 *  
 * @author Kathrin
 */

public class EntailmentUnit extends Node {

	private int ID;
	private List<EntailmentUnitMention> mentions;
	private String text;
	private List<Integer> containedEntailmentUnits;
	private List<Map<Integer,Integer>> modifiers;
	private JCas casRepresentation;
	private int level;
	private boolean correctText;
	
	
	public int getID() {
		return ID;
	}
	
	public void setID(int iD) {
		ID = iD;
	}
	
	public List<EntailmentUnitMention> getMentions() {
		return mentions;
	}
	
	public void setMentions(List<EntailmentUnitMention> mentions) {
		this.mentions = mentions;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public List<Integer> getContainedEntailmentUnits() {
		return containedEntailmentUnits;
	}
	
	public void setContainedEntailmentUnits(List<Integer> containedEntailmentUnits) {
		this.containedEntailmentUnits = containedEntailmentUnits;
	}
	
	public List<Map<Integer, Integer>> getModifiers() {
		return modifiers;
	}

	public void setModifiers(List<Map<Integer, Integer>> modifiers) {
		this.modifiers = modifiers;
	}

	public boolean isCorrectText() {
		return correctText;
	}
	
	public void setCorrectText(boolean correctText) {
		this.correctText = correctText;
	}
	
	public JCas getCasRepresentation() {
		return casRepresentation;
	}

	public void setCasRepresentation(JCas casRepresentation) {
		this.casRepresentation = casRepresentation;
	}

	// returns the distance from the base predicate (how many modifiers it has compared to the BP)
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

		
}

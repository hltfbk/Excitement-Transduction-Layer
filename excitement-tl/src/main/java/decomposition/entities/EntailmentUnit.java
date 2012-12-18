package decomposition.entities;

import java.util.List;

/**
 * This class represents the data structure that holds an entailment unit. 
 * Entailment units function as T and H part of a candidate T/H pair and as 
 * nodes in the EntailmentGraphRaw. 
 *  
 * @author Kathrin
 */

public class EntailmentUnit {

	private int ID;
	private List<EntailmentUnitMention> mentions;
	private String text;
	private List<Integer> containedEntailmentUnits;
	
	//META-DATA
	private String implicitContext;
	private boolean implicitStatement;
	private String implicitFocus;
	private boolean modality;
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
	
	public String getImplicitContext() {
		return implicitContext;
	}
	
	public void setImplicitContext(String implicitContext) {
		this.implicitContext = implicitContext;
	}
	
	public boolean isImplicitStatement() {
		return implicitStatement;
	}
	
	public void setImplicitStatement(boolean implicitStatement) {
		this.implicitStatement = implicitStatement;
	}
	
	public String getImplicitFocus() {
		return implicitFocus;
	}
	
	public void setImplicitFocus(String implicitFocus) {
		this.implicitFocus = implicitFocus;
	}
	
	public boolean isModality() {
		return modality;
	}
	
	public void setModality(boolean modality) {
		this.modality = modality;
	}
	
	public boolean isCorrectText() {
		return correctText;
	}
	
	public void setCorrectText(boolean correctText) {
		this.correctText = correctText;
	}
		
}

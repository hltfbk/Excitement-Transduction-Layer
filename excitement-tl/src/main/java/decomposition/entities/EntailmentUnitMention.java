package decomposition.entities;

/**
 * This class represents the data structure that holds an entailment unit mention. 
 * Entailment unit mentions refer to concrete instances of entailment units within
 * an input text.  
 *  
 * @author Kathrin
 */

public class EntailmentUnitMention {

	private int ID;
	private String textualInputId;
	private String text;
	private int startPos;
	private int endPos;
	private int entailmentUnitId;
	
	public int getID() {
		return ID;
	}
	
	public void setID(int iD) {
		ID = iD;
	}
	
	public String getTextualInputId() {
		return textualInputId;
	}
	
	public void setTextualInputId(String textualInputId) {
		this.textualInputId = textualInputId;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public int getStartPos() {
		return startPos;
	}
	
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}
	
	public int getEndPos() {
		return endPos;
	}
	
	public void setEndPos(int endPos) {
		this.endPos = endPos;
	}
	
	public int getEntailmentUnitId() {
		return entailmentUnitId;
	}
	
	public void setEntailmentUnitId(int entailmentUnitId) {
		this.entailmentUnitId = entailmentUnitId;
	}
	
}

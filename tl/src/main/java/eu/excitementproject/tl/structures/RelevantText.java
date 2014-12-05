package eu.excitementproject.tl.structures;

/**
 * The structure of a relevant text. 
 * 
 * @author Kathrin Eichler
 *
 */
public class RelevantText {
	
	String text; //The text contained in the relevant part
	String goldCategory; //The category associated to the relevant text part
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getGoldCategory() {
		return goldCategory;
	}
	public void setGoldCategory(String category) {
		this.goldCategory = category;
	}
	

}

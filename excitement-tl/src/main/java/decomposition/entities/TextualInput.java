package decomposition.entities;


/**
 * This class represents the data structure that holds the textual input provided as input to be processed. 
 *  
 * @author Kathrin
 */

public class TextualInput {

	private String textualInputId;
	private String documentId;
	private String languageCode; //based on ISO 639-1
	private String text;
	private String categoryId;
	
	public String getTextualInputId() {
		return textualInputId;
	}
	
	public void setTextualInputId(String textualInputId) {
		this.textualInputId = textualInputId;
	}
	
	public String getDocumentId() {
		return documentId;
	}
	
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	
	public String getLanguageCode() {
		return languageCode;
	}
	
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	
}

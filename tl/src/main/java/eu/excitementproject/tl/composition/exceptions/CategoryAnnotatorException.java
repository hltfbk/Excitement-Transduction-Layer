package eu.excitementproject.tl.composition.exceptions;

/** 
 * Exception thrown by {@link CategoryAnnotator} methods 
 * 
 * @author Kathrin Eichler
 *
 */
public class CategoryAnnotatorException extends Exception {

	private static final long serialVersionUID = -2290239170781685668L;

	/** 
	 * Exception thrown by {@link CategoryAnnotator} methods 
	 * 
	 * @param message
	 */
	public CategoryAnnotatorException(String message) {
		super(message);
	}

}

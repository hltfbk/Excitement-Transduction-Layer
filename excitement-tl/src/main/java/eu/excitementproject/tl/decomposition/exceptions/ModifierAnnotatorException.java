/**
 * 
 */
package  eu.excitementproject.tl.decomposition.exceptions;

/** Exception thrown by ModifierAnnotator if any of the needed data is missing in the input CAS or if couldn't annotate the modifiers
 * @author Lili
 *
 */
public class ModifierAnnotatorException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6791752728203555997L;

	/** Exception thrown by ModifierAnnotator if any of the needed data is missing in the input CAS or if couldn't annotate the modifiers
	 * @param message
	 */
	public ModifierAnnotatorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	 
}

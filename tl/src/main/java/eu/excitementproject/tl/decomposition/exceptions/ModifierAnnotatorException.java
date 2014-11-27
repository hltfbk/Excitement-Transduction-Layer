/**
 * 
 */
package  eu.excitementproject.tl.decomposition.exceptions;

import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;


/** Exception thrown by {@link ModifierAnnotator} if any of the needed data is missing 
 * in the input CAS or if it could not annotate the modifiers
 * @author Lili
 *
 */
public class ModifierAnnotatorException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6791752728203555997L;

	/** Exception thrown by {@link ModifierAnnotator} if any of the needed data is missing 
	 * in the input CAS or if it could not annotate the modifiers
	 * @param message
	 */
	public ModifierAnnotatorException(String message) {
		super(message);
	}

	/** Exception thrown by {@link ModifierAnnotator} if any of the needed data is missing 
	 * in the input CAS or if it could not annotate the modifiers
	 * @param message
	 * @param cause
	 */
	public ModifierAnnotatorException(String message, Throwable cause) {
		super(message, cause);
	}

	 
}

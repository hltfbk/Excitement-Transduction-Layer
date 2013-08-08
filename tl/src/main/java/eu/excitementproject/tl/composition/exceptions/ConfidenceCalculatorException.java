package eu.excitementproject.tl.composition.exceptions;

public class ConfidenceCalculatorException extends Exception {

/**
 * 
 */
	private static final long serialVersionUID = -2290239170781685668L;
	
	/** Exception thrown by CategoryAnnotatorException if category annotation fails.
	 * @param message
	 */
	public ConfidenceCalculatorException(String message) {
		super(message);
	}

}

package eu.excitementproject.tl.composition.exceptions;

import eu.excitementproject.tl.composition.api.ConfidenceCalculator;

/** 
 * Exception thrown by {@link ConfidenceCalculator} methods
 * 
 * @author Kathrin Eichler
 *
 */
public class ConfidenceCalculatorException extends Exception {

	private static final long serialVersionUID = -2290239170781685668L;
	
	/** 
	 * Exception thrown by {@link ConfidenceCalculator} methods
	 * 
	 * @param message
	 */
	public ConfidenceCalculatorException(String message) {
		super(message);
	}

}

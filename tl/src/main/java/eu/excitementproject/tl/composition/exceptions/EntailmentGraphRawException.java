/**
 * 
 */
package  eu.excitementproject.tl.composition.exceptions;

import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

/** Exception thrown by {@link EntailmentGraphRaw} methods
 * @author Lili Kotlerman
 *
 */
public class EntailmentGraphRawException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3794177609857945623L;

	/** Exception thrown by {@link EntailmentGraphRaw} methods
	 * @param message
	 */
	public EntailmentGraphRawException(String message) {
		super(message);
	}

	
}

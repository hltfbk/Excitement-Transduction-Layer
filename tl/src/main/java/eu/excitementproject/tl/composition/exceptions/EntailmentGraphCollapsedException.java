/**
 * 
 */
package  eu.excitementproject.tl.composition.exceptions;

import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;

/** Exception thrown by {@link EntailmentGraphCollapsed} methods
 * @author Lili Kotlerman
 *
 */
public class EntailmentGraphCollapsedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9027781615803218442L;

	/** Exception thrown by {@link EntailmentGraphCollapsed} methods 
	 * @param message
	 */
	public EntailmentGraphCollapsedException(String message) {
		super(message);
	}

	
}

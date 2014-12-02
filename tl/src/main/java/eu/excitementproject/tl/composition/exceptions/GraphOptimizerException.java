/**
 * 
 */
package eu.excitementproject.tl.composition.exceptions;

import eu.excitementproject.tl.composition.api.GraphOptimizer;

/** Exception thrown by {@link GraphOptimizer} if the implementation cannot produce the collapsed graph for some reason
 * @author Lili Kotlerman
 *
 */
public class GraphOptimizerException extends Exception {
	
	private static final long serialVersionUID = -2023803901835429501L;

	/** Exception thrown by {@link GraphOptimizer} if the implementation cannot produce the collapsed graph for some reason
	 * @param message
	 */
	public GraphOptimizerException(String message) {
		super(message);
	}

	
}

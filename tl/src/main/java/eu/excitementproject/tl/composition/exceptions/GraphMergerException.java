/**
 * 
 */
package  eu.excitementproject.tl.composition.exceptions;

import eu.excitementproject.tl.composition.api.GraphMerger;

/** Exception thrown by {@link GraphMerger} if the implementation cannot merge the graphs for some
	  reason
 * @author Lili Kotlerman
 *
 */
public class GraphMergerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8418882182114896584L;

	/** Exception thrown by {@link GraphMerger} if the implementation cannot merge the graphs for some
	  reason
	 * @param message
	 */
	public GraphMergerException(String message) {
		super(message);
	}

	
}

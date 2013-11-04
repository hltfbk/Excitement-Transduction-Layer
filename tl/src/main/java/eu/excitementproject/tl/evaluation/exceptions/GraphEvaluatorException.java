/**
 * 
 */
package eu.excitementproject.tl.evaluation.exceptions;

/** Exception caused by graph evaluation process
 * @author Lili
 *
 */
public class GraphEvaluatorException extends Exception {
	
	private static final long serialVersionUID = -4009085051567039486L;

	/** Exception thrown by CollapsedGraphGenerator if the implementation can't convert the graph for some
  reason
	 * @param message
	 */
	public GraphEvaluatorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	
}

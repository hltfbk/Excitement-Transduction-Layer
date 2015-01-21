package  eu.excitementproject.clustering.clustering.exceptions;

/** 
 * Exception thrown by a clustering process
 * @author Lili Kotlerman
 *
 */
public class ClusteringException extends Exception {

	private static final long serialVersionUID = -3699841585149384380L;

	/** Exception thrown by a clustering process 
	 * @param message
	 */
	public ClusteringException(String message) {
		super(message);
	}

	
}

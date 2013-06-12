package eu.excitementproject.tl.decomposition.exceptions;

/**
 * This exception represents an exception that the data file at hand has some 
 * "logical" problems. For example, reading a fragment where the fragment original text 
 * is not found in the interaction text, etc. 
 * 
 * @author Gil 
 *
 */
public class DataIntegrityFail extends Exception {

	private static final long serialVersionUID = 8722122697030932140L;

	public DataIntegrityFail(String message) {
		super(message);
	}
	
	public DataIntegrityFail(String message, Throwable cause) {
		super(message, cause);
	}

}

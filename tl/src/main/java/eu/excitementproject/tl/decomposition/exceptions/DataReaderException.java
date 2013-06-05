package eu.excitementproject.tl.decomposition.exceptions;

/**
 * Exception for data readers 
 * 
 * @author Gil 
 *
 */

public class DataReaderException extends Exception {

	private static final long serialVersionUID = -6216355131508517161L;
	
	public DataReaderException(String message) {
		super(message);
	}
	
	public DataReaderException(String message, Throwable cause) {
		super(message, cause);
	}
	
}

package  eu.excitementproject.tl.decomposition.exceptions;

public class FragmentAnnotatorException extends Exception {

	/** Exception thrown by FragmentAnnotator if any needed data is missing in the input JCas or (2) if couldn't successfully annotate the real_fragment annotations.
	 * @author Lili
	 *
	 */
	private static final long serialVersionUID = 1407927323508438326L;

	/**
	 * Exception thrown by FragmentAnnotator if any needed data is missing in the input JCas or if couldn't successfully annotate the real_fragment annotations.
	 * @param message
	 */
	public FragmentAnnotatorException(String message) {
			super(message);
	}
	public FragmentAnnotatorException(String message, Throwable cause) {
		super(message, cause);
	}
}


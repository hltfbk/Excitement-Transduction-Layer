package  eu.excitementproject.tl.decomposition.exceptions;

import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;

public class FragmentAnnotatorException extends Exception {

	/** Exception thrown by {@link FragmentAnnotator} if any needed data is missing in the input JCas or if could not successfully annotate the real fragment annotations.
	 * @author Lili Kotlerman
	 *
	 */
	private static final long serialVersionUID = 1407927323508438326L;

	/** Exception thrown by {@link FragmentAnnotator} if any needed data is missing in the input JCas or if could not successfully annotate the real fragment annotations.
	 * @param message
	 */
	public FragmentAnnotatorException(String message) {
			super(message);
	}

	/** Exception thrown by {@link FragmentAnnotator} if any needed data is missing in the input JCas or if could not successfully annotate the real fragment annotations.
	 * @param message
	 * @param cause
	 */
	public FragmentAnnotatorException(String message, Throwable cause) {
		super(message, cause);
	}
}


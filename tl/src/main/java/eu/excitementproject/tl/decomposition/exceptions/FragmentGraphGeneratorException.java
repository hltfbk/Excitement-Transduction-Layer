/**
 * 
 */
package  eu.excitementproject.tl.decomposition.exceptions;

import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;

/** Exception thrown by {@link FragmentGraphGenerator} if any of the needed data is missing 
 * in the input CAS or if the implementation cannot generate the graphs for some  reason
 * @author Lili Kotlerman
 *
 */
public class FragmentGraphGeneratorException extends Exception {
	
	private static final long serialVersionUID = 2565175967769513901L;

	/** Exception thrown by {@link FragmentGraphGenerator} if any of the needed data is missing 
	 * in the input CAS or if the implementation cannot generate the graphs for some  reason
	 * @param message
	 */
	public FragmentGraphGeneratorException(String message) {
		super(message);
	}

	
}

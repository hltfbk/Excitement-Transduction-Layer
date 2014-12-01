/**
 * 
 */
package eu.excitementproject.tl.decomposition.fragmentannotator;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;



/**
Abstract implementation of the {@link FragmentAnnotator} interface.
The class contains attributes and methods that are likely to be shared by the actual implementations, including
constructors with LAP configuration.

 * @author Lili Kotlerman && vivi@fbk
 */
public abstract class AbstractFragmentAnnotator implements FragmentAnnotator{

	/**
	 * The LAP to be used by the annotator
	 */
	private final LAPAccess lap;
	
	/** Constructor with a specific LAP
	 * @param lap
	 * @throws FragmentAnnotatorException
	 */
	public AbstractFragmentAnnotator(LAPAccess lap) throws FragmentAnnotatorException{
		this.lap=lap;
	}
	
	/**
	 * @return the LAP used by the annotator (passed in the constructor)
	 */
	public LAPAccess getLap() {
		return this.lap;
	}	
}

/**
 * 
 */
package  eu.excitementproject.tl.decomposition.fragmentannotator;

import eu.excitement.api.FragmentAnnotator;
import eu.excitement.api.exceptions.FragmentAnnotatorException;

/**
 * 
An implementation of the {@link FragmentAnnotator} interface.
May need to call LAP. The needed LAP should be passed via Constructor. 
Also, any additional configurable parameters of this module implementation should be clearly exposed in the Constructor.
 
 * If we decide to keep abstract implementations, they can hold the methods 
 * which we expect to be common for different implementations 

 * @author Lili
 */
public abstract class AbstractFragmentAnnotator implements FragmentAnnotator{

	private final LAP_TLAccess lap;
	
	/**
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @throws FragmentAnnotatorException
	 */
	public AbstractFragmentAnnotator(LAP_TLAccess lap) throws FragmentAnnotatorException{
		this.lap=lap;
	}
	
	/**
	 * @return the LAP passed in the constructor to the FragmentAnnotator implementation
	 */
	public LAP_TLAccess getLap() {
		return this.lap;
	}
	
	
	
}

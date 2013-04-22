/**
 * 
 */
package  eu.excitementproject.tl.decomposition.modifierannotator;

import eu.excitement.api.ModifierAnnotator;
import eu.excitement.api.exceptions.ModifierAnnotatorException;

/**
 *
 *An implementation of the {@link ModifierAnnotator} interface.
 May need to call LAP. The needed LAP should be passed via Constructor. Also, any additional
configurable parameters of this module implementation should be
clearly exposed in the Constructor.
 * @author Lili
 */
public abstract class AbstractModifierAnnotator implements ModifierAnnotator{
	
	private final LAP_TLAccess lap;
	
	/** May need to call LAP. The needed LAP should be passed via Constructor. Also, any additional
configurable parameters of this module implementation should be
clearly exposed in the Constructor.
	 * @param lap
	 * @throws ModifierAnnotatorException
	 */
	public AbstractModifierAnnotator(LAP_TLAccess lap) throws ModifierAnnotatorException {
		this.lap=lap;
	}
	
	public LAP_TLAccess getLap() {
		return this.lap;
	}
	
}

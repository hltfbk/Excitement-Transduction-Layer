/**
 * 
 */
package eu.excitementproject.tl.decomposition.modifierannotator;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;


/**
 *
 *An implementation of the {@link ModifierAnnotator} interface.
 May need to call LAP. The needed LAP should be passed via Constructor. Also, any additional
configurable parameters of this module implementation should be
clearly exposed in the Constructor.
 * @author Lili
 */
public abstract class AbstractModifierAnnotator implements ModifierAnnotator{
	
	private final LAPAccess lap;
	
	/** May need to call LAP. The needed LAP should be passed via Constructor. Also, any additional
configurable parameters of this module implementation should be
clearly exposed in the Constructor.
	 * @param lap
	 * @throws ModifierAnnotatorException
	 */
	public AbstractModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException {
		this.lap=lap;
	}
	
	public LAPAccess getLap() {
		return this.lap;
	}
	
}

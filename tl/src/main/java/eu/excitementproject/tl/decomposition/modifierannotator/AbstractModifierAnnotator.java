/**
 * 
 */
package eu.excitementproject.tl.decomposition.modifierannotator;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
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
	protected FragmentAnnotator fragAnn = null;
	
	/** May need to call LAP. The needed LAP should be passed via Constructor. Also, any additional
configurable parameters of this module implementation should be
clearly exposed in the Constructor.
	 * @param lap
	 * @throws ModifierAnnotatorException
	 */
	public AbstractModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException {
		this.lap=lap;
	}

	/** May need to call LAP and the fragment annotator. Modifiers are only annotated inside fragments. 
	 * 
	 * The needed LAP and any additional configurable parameters of this module implementation should be
	 * clearly exposed in the Constructor.
	 *
	 * Vivi@fbk
	 * 
	 * @param lap
	 * @param fragAnn
	 * @throws ModifierAnnotatorException
	 */
	public AbstractModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn) throws ModifierAnnotatorException {
		this.lap=lap;
		this.fragAnn = fragAnn;
	}

	
	/**
	 * 
	 * @return the LAP associated with the modifier annotator
	 */
	public LAPAccess getLap() {
		return this.lap;
	}
	
	/**
	 * 
	 * @return the fragment annotator associated with the modifier annotator
	 */
	public FragmentAnnotator getFragmentAnnotator() {
		return this.fragAnn;
	}
	
	/**
	 * 
	 * @param fragAnn -- the fragment annotator to be used for this modifier annotation
	 */
	public void setFragmentAnnotator(FragmentAnnotator fragAnn) {
		this.fragAnn = fragAnn;
	}
}

/**
 * 
 */
package eu.excitementproject.tl.decomposition.modifierannotator;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;


/**
Abstract implementation of the {@link ModifierAnnotator} interface.
The class contains attributes and methods that are likely to be shared by the actual implementations, including
constructors with LAP configuration and {@link FragmentAnnotator} to use.

 * @author Lili Kotlerman & Vivi@fbk
 */
public abstract class AbstractModifierAnnotator implements ModifierAnnotator{
	
	/**
	 * The LAP to be used by the annotator
	 */
	private final LAPAccess lap;
	
	/**
	 * The {@link FragmentAnnotator} to be used 
	 */
	protected FragmentAnnotator fragAnn = null;
	
	/** Constructor with a specific LAP.
	 * @param lap
	 * @throws ModifierAnnotatorException
	 */
	public AbstractModifierAnnotator(LAPAccess lap) throws ModifierAnnotatorException {
		this.lap=lap;
	}

	/** Constructor with a specific LAP
	 * and {@link FragmentAnnotator} (modifiers are only annotated inside fragments).
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
	 * @return the {@link FragmentAnnotator} associated with the modifier annotator
	 */
	public FragmentAnnotator getFragmentAnnotator() {
		return this.fragAnn;
	}
	
	/**
	 * 
	 * @param fragAnn -- the {@link FragmentAnnotator} to be used for this modifier annotation
	 */
	public void setFragmentAnnotator(FragmentAnnotator fragAnn) {
		this.fragAnn = fragAnn;
	}
}

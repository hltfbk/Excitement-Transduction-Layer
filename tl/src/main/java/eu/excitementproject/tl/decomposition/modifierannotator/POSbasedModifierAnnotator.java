package eu.excitementproject.tl.decomposition.modifierannotator;

import java.util.HashSet;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

/**
 * 
 * POS-based modifier annotator
 * extends the abstract POS-based modifier annotator
 * implements the method to build the set of desired POS classes (addPOSClasses())
 * 
 * All classes that implement a specific POS-based modifier annotator will extend this class
 * 
 * @author vivi@fbk
 *
 */
public class POSbasedModifierAnnotator extends
		AbstractPOSbasedModifierAnnotator {

	/**
	 * Constructor with LAP and empty set of POS classes and default negation check (false)
	 * 
	 * @param lap -- the Linguistic Analysis Pipeline for annotations
	 * @throws ModifierAnnotatorException
	 */
	public POSbasedModifierAnnotator(LAPAccess lap)
			throws ModifierAnnotatorException {
		super(lap);
		addPOSClasses();
	}

	/**
	 * Constructor with LAP and negation check, empty set of POS classes
	 * 
	 * @param lap -- the Linguistic Analysis Pipeline for annotations
	 * @param checkNegation -- check/not whether the modifier candidate is in the scope of a negation
	 * @throws ModifierAnnotatorException
	 */
	public POSbasedModifierAnnotator(LAPAccess lap, boolean checkNegation)
			throws ModifierAnnotatorException {
		super(lap);
		addPOSClasses();
		this.checkNegation = checkNegation;
	}

	
	/**
	 * Constructor with LAP and fragment annotator, default negation check (false) and empty set of POS
	 * 
	 * @param lap -- the Linguistic Analysis Pipeline for annotations
	 * @param fragAnn -- the fragment annotator to annotate fragments
	 * @throws ModifierAnnotatorException
	 */
	public POSbasedModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn)
			throws ModifierAnnotatorException {
		super(lap, fragAnn);
		addPOSClasses();
	}

	
	/**
	 * Constructor with LAP, fragment annotator and negation check, empty set of POS
	 * 
	 * @param lap -- the Linguistic Analysis Pipeline for annotations
	 * @param fragAnn -- the fragment annotator
	 * @param checkNegation -- check/not whether the modifier candidate is in the scope of a negation
	 * @throws ModifierAnnotatorException
	 */
	public POSbasedModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn, boolean checkNegation)
			throws ModifierAnnotatorException {
		super(lap, fragAnn);
		addPOSClasses();
		this.checkNegation = checkNegation;
	}

	
	/**
	 * The method to be implemented by the descendants, each according to what POS they want to consider
	 * Default empty. 
	 */
	@Override
	protected void addPOSClasses() {
		wantedClasses = new HashSet<Class<? extends POS>>();
	}
	
	
	/**
	 * Add the given set of POS classes as the wanted classes
	 * 
	 * @param classes -- a set of POS classes 
	 */
	public void addPOSClasses(Set<Class<? extends POS>> classes) {
		wantedClasses.addAll(classes);
	}
	
	/** 
	 * Add all parameters as wanted POS classes 
	 * 
	 * @param classes -- a number of parameters, each a POS class
	 */
	@SuppressWarnings("unchecked")
	public void addPOSClasses(Class<? extends POS>... classes ) {
		for(Class<? extends POS> cls: classes) {
			wantedClasses.add(cls);
		}
	}
}

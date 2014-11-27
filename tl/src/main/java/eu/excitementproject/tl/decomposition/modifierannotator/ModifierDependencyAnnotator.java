package eu.excitementproject.tl.decomposition.modifierannotator;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

public class ModifierDependencyAnnotator extends AbstractModifierAnnotator {

	public ModifierDependencyAnnotator(LAPAccess lap)
			throws ModifierAnnotatorException {
		super(lap);
	}

	public ModifierDependencyAnnotator(LAPAccess lap, FragmentAnnotator fragAnn)
			throws ModifierAnnotatorException {
		super(lap, fragAnn);
	}

	/**
	 * add dependencies between modifiers based on grammatical dependency relations
	 */
	@Override
	public int annotateModifiers(JCas text) throws ModifierAnnotatorException,
			FragmentAnnotatorException {
		
		addDependencies(text);
		
		return 0;
	}

}

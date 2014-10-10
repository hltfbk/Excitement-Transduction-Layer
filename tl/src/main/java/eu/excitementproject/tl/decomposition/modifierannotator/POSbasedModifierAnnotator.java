package eu.excitementproject.tl.decomposition.modifierannotator;

import java.util.HashSet;
import java.util.Set;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

public class POSbasedModifierAnnotator extends
		AbstractPOSbasedModifierAnnotator {

	public POSbasedModifierAnnotator(LAPAccess lap)
			throws ModifierAnnotatorException {
		super(lap);
		addPOSClasses();
	}

	public POSbasedModifierAnnotator(LAPAccess lap, boolean checkNegation)
			throws ModifierAnnotatorException {
		super(lap);
		addPOSClasses();
		this.checkNegation = checkNegation;
	}

	
	public POSbasedModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn)
			throws ModifierAnnotatorException {
		super(lap, fragAnn);
		addPOSClasses();
	}

	
	public POSbasedModifierAnnotator(LAPAccess lap, FragmentAnnotator fragAnn, boolean checkNegation)
			throws ModifierAnnotatorException {
		super(lap, fragAnn);
		addPOSClasses();
		this.checkNegation = checkNegation;
	}

	
	@Override
	protected void addPOSClasses() {
		wantedClasses = new HashSet<Class<? extends POS>>();
	}
	
	
	public void addPOSClasses(Set<Class<? extends POS>> classes) {
		wantedClasses.addAll(classes);
	}
	
	@SuppressWarnings("unchecked")
	public void addPOSClasses(Class<? extends POS>... classes ) {
		for(Class<? extends POS> cls: classes) {
			wantedClasses.add(cls);
		}
	}
}

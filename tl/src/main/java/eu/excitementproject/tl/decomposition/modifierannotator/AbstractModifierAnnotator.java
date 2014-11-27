/**
 * 
 */
package eu.excitementproject.tl.decomposition.modifierannotator;

import java.util.Collection;
import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
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
	
	
	
	/**
	 * Adds the "dependsOn" relation between dependent modifiers for all modifiers inside the CAS
	 * 
	 * @param aJCas
	 */
	public static void addDependencies(JCas aJCas) {
		addDependencies(aJCas, JCasUtil.selectSingle(aJCas, DocumentAnnotation.class));
	}

	/** 
	 * Adds the "dependsOn" relation between dependent modifiers for all modifiers inside the CAS 
	 * after running first the lap to add grammatical dependency information
	 *   
	 * @param aJCas
	 * @param lap
	 */
	public static void addDependencies(JCas aJCas, LAPAccess lap) {
		try {
			lap.addAnnotationOn(aJCas);
		} catch (LAPException e) {
			System.err.println("could not add LAP annotations on CAS (" + lap.getComponentName() + ")");
			e.printStackTrace();
		}
		addDependencies(aJCas);
	}

	
	/**
	 * Adds the "dependsOn" relation between dependent modifiers for all modifiers inside the current annotation (could be fragment, the entire document, ...)
	 * 
	 * @param aJCas
	 * @param annot
	 */
	public static void addDependencies(JCas aJCas, Annotation annot) {

		Collection<ModifierAnnotation> modifiers = JCasUtil.selectCovered(aJCas, ModifierAnnotation.class, annot);
		if (modifiers != null) {
			for (ModifierAnnotation ma: modifiers) {
				ModifierAnnotation dependsOn = findGovernor(aJCas, ma);
				if (dependsOn != null) {
					ma.setDependsOn(dependsOn);
				}
			}
		}
	}

	
	/**
	 * Finds the ModifierAnnotation that covers the governor of a dependency relation in which "ma" is the dependent 
	 * 
	 * @param aJCas -- a CAS object
	 * @param frag -- a fragment annotation
	 * @param ma -- a modifier annotation for which we seek a governor
	 * @return
	 */
	public static ModifierAnnotation findGovernor(JCas aJCas, ModifierAnnotation ma) {
		
	
		ModifierAnnotation dependsOn = null;
		
		List<Dependency> ma_dependencies = JCasUtil.selectCovering(aJCas,Dependency.class, ma.getBegin(), ma.getEnd());
		
		if (ma_dependencies != null) {
			for (Dependency dep: ma_dependencies) {
				Collection<ModifierAnnotation> deps_annots = JCasUtil.selectCovered(aJCas, ModifierAnnotation.class, dep.getDependent());
				if (deps_annots != null && deps_annots.contains(ma)) {
					deps_annots = JCasUtil.selectCovered(aJCas, ModifierAnnotation.class, dep.getGovernor());
					if (deps_annots != null) {
						// there should be at most one, actually
						for (ModifierAnnotation m_gov: deps_annots) {
							return m_gov;
						}
					}
				}
			}
		}
		
		return dependsOn;
	}


	
}

package eu.excitementproject.tl.decomposition.api;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

/**
 *
 *** Modifier Annotator Module Interface 
 *
<p><b>Input:</b> {@link JCas} with text, metadata, and (real) fragment annotation. If
  any of the needed data is missing, a {@link ModifierAnnotatorException} will be raised.
<p><b>output:</b> No additional output. When successfully run, the input CAS
  will be enriched with modifier annotations.
<p><b>Failures:</b> If it could not annotate the modifiers, it will raise a {@link ModifierAnnotatorException}.
<p><b>Implementations:</b> We expect a separate implementation per language.
<p><b>Dependency:</b> An implementation of this interface may need to call LAP. The
needed LAP should be passed via Constructor. Also, any additional
configurable parameters of this module implementation should be
clearly exposed in the Constructor.

 * @author Lili Kotlerman
 */
public interface ModifierAnnotator {

	/**
	 * @param text - {@link JCas} with text, metadata, and (real) fragment annotation.
	 * @return the input CAS enriched with modifier annotations
	 * @throws ModifierAnnotatorException if any of the needed data is missing in the input CAS or if couldn't annotate the modifiers
	 * @throws FragmentAnnotatorException 
	 */
	public int annotateModifiers(JCas text) throws ModifierAnnotatorException, FragmentAnnotatorException;
		
}

package eu.excitementproject.tl.decomposition.api;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;

/**
 *
 *** Modifier Identification Module
- input: =JCas= with text, metadata, and (real) fragment annotation. If
  any of the needed data is missing, it will raise an exception.
- output: no additional output. When successfully run, the input CAS
  will be enriched with modifier annotations.
- failues: if it couldn't annotate the modifiers, it will raise an
  exception. (We will define an exception for this module type).
- interface:
We can express the above mentioned contract with an interface with
one method.
Expected number of instances of this module for the prototype:
per-language.
- dependency:
An implementation of this implmenetation needs to call LAP. The
needed LAP should be passed via Constructor. Also, any additional
configurable parameters of this module implementation should be
clearly exposed in the Constructor.

 * @author Lili
 */
public interface ModifierAnnotator {

	/**
	 * @param text - =JCas= with text, metadata, and (real) fragment annotation. When successfully run, the input CAS
  will be enriched with modifier annotations.
	 * @return the input CAS enriched with modifier annotations
	 * @throws ModifierAnnotatorException if any of the needed data is missing in the input CAS or if couldn't annotate the modifiers
	 * @throws FragmentAnnotatorException 
	 */
	public int annotateModifiers(JCas text) throws ModifierAnnotatorException, FragmentAnnotatorException;
		
}

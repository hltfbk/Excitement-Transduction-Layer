/**
 * 
 */
package eu.excitementproject.tl.decomposition.api;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;


/** Fragment Annotator Module
- input: =JCas= with text & metadata. (It may additionally hold
  assumedFragment annotation). If any needed data is missing, it will
  raise an exception.
- output: no additional output. When successfully called, the input
  CAS will be enriched with (real) fragment annotations.
- failues: if it couldn't successfully annotate the real_fragment
  annotations, it will raise an Exception. (We will define and use
  one exception for this module)
- interface:
We can express the above mentioned contract with an interface with
one method. Expected number of instances of this module for the
prototype: per-application (also per-language).
- dependency:
An implementation of this implmenetation may need to call LAP. The
needed LAP should be passed via Constructor. Also, any additional
configurable parameters of this module implementation should be
clearly exposed in the Constructor.

 * @author Lili
 */
public interface FragmentAnnotator {
		
	/**
	 * @param text - =JCas= with text & metadata. (It may additionally hold
  assumedFragment annotation). 
	 * @return CAS enriched with (real) fragment annotations.
	 * @throws FragmentAnnotatorException if any needed data is missing in the input JCas or if couldn't successfully annotate the real_fragment
  annotations.
	 */
	public void annotateFragments(JCas text) throws FragmentAnnotatorException;
	
}


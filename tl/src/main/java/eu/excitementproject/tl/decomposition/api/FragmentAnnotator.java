/**
 * 
 */
package eu.excitementproject.tl.decomposition.api;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;


/** Fragment Annotator Module Interface

<p> <b>Input:</b> {@link JCas} with text & metadata. (It may additionally hold
  assumed fragment annotation). If any needed data is missing, 
  a {@link FragmentAnnotatorException} will be raised.
  
<p> <b>Output:</b> No additional output. When successfully called, the input
  CAS will be enriched with (real) fragment annotations.
  
<p> <b>Failures:</b> If it could not successfully annotate the real fragment
  annotations, it will raise a {@link FragmentAnnotatorException}.

<p> <b>Implementations:<b> We expect a separate implementation per-application and per-language.

<p> <b>Dependency>:</b>
An implementation of this intereface may need to call LAP. The
needed LAP should be passed via a Constructor. Also, any additional
configurable parameters of this module implementation should be
clearly exposed in the Constructor.

 * @author Lili Kotlerman
 */
public interface FragmentAnnotator {
		
	/**
	 * @param text - {@link JCas} with text & metadata. (It may additionally hold
  assumed fragment annotation). 
	 * @return CAS enriched with (real) fragment annotations.
	 * @throws FragmentAnnotatorException if any needed data is missing in the input JCas or if could not successfully annotate the real fragment
  annotations.
	 */
	public void annotateFragments(JCas text) throws FragmentAnnotatorException;
	
}


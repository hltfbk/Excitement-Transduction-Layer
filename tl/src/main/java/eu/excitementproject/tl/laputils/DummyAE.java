package eu.excitementproject.tl.laputils;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

// This AE does nothing;
// it is provided here so user-level codes can make a new JCas (or CAS)
// with all TL (EOP + TL's own) types, by using this AE.
// See the descriptor XML (/desc/type/TLDummyAE.xml); 
// it imports both EOP and TL type system definitions. 

public class DummyAE extends JCasAnnotator_ImplBase {
	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
	}
}
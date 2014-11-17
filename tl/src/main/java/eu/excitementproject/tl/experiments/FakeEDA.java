package eu.excitementproject.tl.experiments;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;

import eu.excitement.type.entailment.Pair;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.EditDistanceTEDecision;

/**
 * 
 * @author vivi@fbk
 * 
 * Fake EDA to be able to test performance without EDA decisions. This one will always return the same answer (now negative).
 *
 * @param <T>
 */
public class FakeEDA<T extends TEDecision> implements EDABasic<TEDecision> {

	private DecisionLabel decision = DecisionLabel.NonEntailment;
	
	public FakeEDA() {
		super();
	}
	
	public FakeEDA(DecisionLabel decision) {
		this.decision = decision;
	}
	
	@Override
	public void initialize(CommonConfig arg0) throws ConfigurationException,
			EDAException, ComponentException {
		// nothing to initialize
		
	}
	
	public void initialize(DecisionLabel decision) { 
		this.decision = decision;
	}

	@Override
	public TEDecision process(JCas pairJCas) throws EDAException,
			ComponentException {
		// always return negative
		return new EditDistanceTEDecision(decision, getPairId(pairJCas), 0);
	}

	@Override
	public void shutdown() {
		// Nothing to do
		
	}

	@Override
	public void startTraining(CommonConfig arg0) throws ConfigurationException,
			EDAException, ComponentException {
		// Nothing to do
		
	}

	/**
     * Returns the pair identifier of the pair contained in the specified CAS
     *
     * @param jcas the CAS
     * 
     * @return the pair identifier
     */
	private String getPairId(JCas jcas) {
		
		FSIterator<TOP> pairIter = jcas.getJFSIndexRepository().getAllIndexedFS(Pair.type);
		
		Pair p = null;
		if (pairIter.hasNext())
			p = (Pair)pairIter.next();
		
		if (p != null)
			return p.getPairID();
	
		return null;
		
	}
	
}

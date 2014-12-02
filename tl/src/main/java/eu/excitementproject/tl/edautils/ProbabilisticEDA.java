package eu.excitementproject.tl.edautils;

import java.util.Random;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;

/**
 * EDA, which generates random entailment decisions {@link RandomTEDecision}
 * @author Lili Kotlerman
 *
 */
public class ProbabilisticEDA implements EDABasic<ProbabilisticTEDecision>{

	private Random generator;
	
	/**
	 * Probability with which positive entailment decision will be generated
	 */
	private final Double entailmentProbability=0.8;
	
	
	/**
	 *  Constructor, which will initialize the EDA
	 */
	public ProbabilisticEDA() {
		try {
			initialize(null);
		} catch (ConfigurationException | EDAException | ComponentException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void initialize(CommonConfig config) throws ConfigurationException,
			EDAException, ComponentException {
		
		// init the generator of random numbers
		generator = new Random(); 
		
	}

	@Override
	public ProbabilisticTEDecision process(JCas aCas) {
		// generate a random double between 0.0 and 1.0 and decide on entailment according to entailmentProbability
		return new ProbabilisticTEDecision(generator.nextDouble(), entailmentProbability);
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void startTraining(CommonConfig c) throws ConfigurationException,
			EDAException, ComponentException {
	}
	
}

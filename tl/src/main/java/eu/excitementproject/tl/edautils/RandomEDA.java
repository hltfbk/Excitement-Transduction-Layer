package eu.excitementproject.tl.edautils;

import java.util.Random;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;

/**
 * EDA, which generates random entailment decisions ({@link RandomTEDecision})
 * @author LiliKotlerman
 *
 */
public class RandomEDA implements EDABasic<ProbabilisticTEDecision>{

	private Random generator;
	
	/**
	 *  Constructor, which will initialize the EDA
	 */
	public RandomEDA() {
		try {
			initialize(null);
		} catch (ConfigurationException | EDAException | ComponentException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void initialize(CommonConfig config) throws ConfigurationException,
			EDAException, ComponentException {
		// init new generator of random numbers
		generator = new Random(); 
		
	}

	@Override
	public ProbabilisticTEDecision process(JCas aCas) {
		return new ProbabilisticTEDecision(generator.nextDouble());
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void startTraining(CommonConfig c) throws ConfigurationException,
			EDAException, ComponentException {
	}

	
}

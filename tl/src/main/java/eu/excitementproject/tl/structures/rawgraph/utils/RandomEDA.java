package eu.excitementproject.tl.structures.rawgraph.utils;

import java.util.Random;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;

/**
 * EDA, which generates random entailment decisions (RandomTEDecision)
 * @author LiliKotlerman
 *
 */
public class RandomEDA implements EDABasic<RandomTEDecision>{

	private Random generator;
	
	public RandomEDA() {
		try {
			initialize(null);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EDAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void initialize(CommonConfig config) throws ConfigurationException,
			EDAException, ComponentException {
		
		generator = new Random(); 
		
	}

	public RandomTEDecision process(JCas aCas) {
		
		return new RandomTEDecision(generator.nextDouble());
	}

	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	public void startTraining(CommonConfig c) throws ConfigurationException,
			EDAException, ComponentException {
		// TODO Auto-generated method stub
		
	}

	
}

package eu.excitementproject.tl.structures.rawgraph;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;

public class RandomEDA implements EDABasic<RandomTEDecision>{

	public void initialize(CommonConfig config) throws ConfigurationException,
			EDAException, ComponentException {
		// TODO Auto-generated method stub
		
	}

	public RandomTEDecision process(JCas aCas) throws EDAException,
			ComponentException {
		// TODO Auto-generated method stub
		return null;
	}

	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	public void startTraining(CommonConfig c) throws ConfigurationException,
			EDAException, ComponentException {
		// TODO Auto-generated method stub
		
	}

}

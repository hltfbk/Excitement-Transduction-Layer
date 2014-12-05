package eu.excitementproject.tl.experiments.OMQ;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;

/**
 * 
 * @author ??
 *
 */
public class SimpleEDA_DETest {

	Logger testlogger = Logger.getLogger("eu.excitementproject.tl.experiments.OMQ.SimpleEDA_DETest"); 
	
	@Test
	public void test() {
		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO); 
		try 
		{
			testlogger.info("Initializating SimpleEDA_DE");
			SimpleEDA_DE seda = new SimpleEDA_DE();
			CachedLAPAccess lap = new CachedLAPAccess(new LemmaLevelLapDE());
			
			testlogger.info("Creating TH pair");
			String text = "Software bringt Fehlermeldung";
			String hypothesis = "Software brachte Fehlermeldungen";
			TEDecision decision = seda.process(lap.generateSingleTHPairCAS(text, hypothesis));
			
			testlogger.info("Decision of SimpleEDA_DE for pair: \n\t" + "Text: " + text + "\n\t" + "Hypothesis: " + hypothesis);
			testlogger.info("SimpleEDA_DE says: " + decision.getDecision() + " " + decision.getConfidence());
			Assert.assertEquals(decision.getDecision(), DecisionLabel.Entailment); 
		} 
		catch (EDAException | ComponentException e) {
			fail(e.getMessage()); 
		}
	}
}

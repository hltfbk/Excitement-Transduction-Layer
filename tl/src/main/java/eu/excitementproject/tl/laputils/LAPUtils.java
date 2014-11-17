package eu.excitementproject.tl.laputils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;

public class LAPUtils {


	public static CachedLAPAccess initializeLap(Class<?> lapClass) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// initialize the lap
		CachedLAPAccess lap = null;
		LAPAccess lapAc = null;

		Constructor<?> lapClassConstructor = lapClass.getConstructor();
		lapAc = (LAPAccess) lapClassConstructor.newInstance();

		try {
			lap = new CachedLAPAccess(lapAc);
		} catch (LAPException e) {
			e.printStackTrace();
		}
		
		return lap;
	}
	
	
	public static CachedLAPAccess initializeLap(Class<?> lapClass, CommonConfig config) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// initialize the lap
		CachedLAPAccess lap = null;
		LAPAccess lapAc = null;
		if (lapClass.getName().contains("BIUFullLAP")){			
			try {
				//Constructor<?> lapClassConstructor = lapClass.getConstructor(CommonConfig.class);
				//lapAc = (LAPAccess) lapClassConstructor.newInstance(config);
				lapAc = new BIUFullLAP(config); 
			} catch (ConfigurationException | LAPException e) {
				e.printStackTrace();
			}
		}
		else{ // if not BIUFullLAP
			Constructor<?> lapClassConstructor = lapClass.getConstructor();
			lapAc = (LAPAccess) lapClassConstructor.newInstance();
		}

		try {
			lap = new CachedLAPAccess(lapAc);
		} catch (LAPException e) {
			e.printStackTrace();
		}
		
		return lap;
	}

	
	public static LAPAccess initializeLAP(String language){
		
//		String lapClassName = "eu.excitementproject.tl.laputils.LemmaLevelLap" + language.toUpperCase();
		String lapClassName = "eu.excitementproject.tl.laputils.DependencyLevelLap" + language.toUpperCase();
		LAPAccess lap = null;
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.fragmentannotator.FragmentAnnotatorEvaluator:initializeLAP");
		logger.setLevel(Level.INFO);
		
		try {
			Class<?> lapClass = Class.forName(lapClassName);
			Constructor<?> lapClassConstructor = lapClass.getConstructor();
			lap = (LAPAccess) lapClassConstructor.newInstance();
			
			logger.info("LAP initialized from class : " + lapClassName);
			
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Error initializing LAP : " + e.getClass());
			e.printStackTrace();
		}
		
		return lap;
	}
	
}

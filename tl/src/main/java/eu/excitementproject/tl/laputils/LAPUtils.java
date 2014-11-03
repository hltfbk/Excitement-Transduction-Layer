package eu.excitementproject.tl.laputils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

}

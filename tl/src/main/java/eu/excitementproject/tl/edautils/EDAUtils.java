package eu.excitementproject.tl.edautils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;

public class EDAUtils {

	public static EDABasic<?> initializeEDA(Class<?> edaClass, CommonConfig config) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException {
		// initialize the eda
		
		EDABasic<?> eda = null;
		
		Constructor<?> edaClassConstructor = edaClass.getConstructor();
		eda = (EDABasic<?>) edaClassConstructor.newInstance();
		eda.initialize(config);
		
		return eda;
	}

}

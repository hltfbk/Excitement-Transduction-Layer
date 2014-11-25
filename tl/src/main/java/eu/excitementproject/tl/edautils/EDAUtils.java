package eu.excitementproject.tl.edautils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.tl.laputils.LAPUtils;

/**
 * A collection of static methods to help with the initialization and usage of EDAs
 * 
 * @author vivi@fbk
 *
 */
public class EDAUtils {

	/**
	 * Initializes an EDA object based on the given class and the given configuration
	 * 
	 * @param edaClass 
	 * @param config
	 * @return an instance of the EDA object corresponding to the given class and configuration
	 * 
	 * @throws ConfigurationException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws EDAException
	 * @throws ComponentException
	 */
	public static EDABasic<?> initializeEDA(Class<?> edaClass, CommonConfig config) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException {
		
		EDABasic<?> eda = null;
		
		Constructor<?> edaClassConstructor = edaClass.getConstructor();
		eda = (EDABasic<?>) edaClassConstructor.newInstance();
		eda.initialize(config);
		
		return eda;
	}

	/**
	 * Initializes an EDA object based on the given configuration. It obtains the EDA class from the configuration.
	 *  
	 * @param config -- configuration for the EDA. It contains the EDA class.
	 * @return an instance of the EDA object corresponding to the given configuration
	 * 
	 * @throws ClassNotFoundException 
	 * @throws ComponentException 
	 * @throws EDAException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws ConfigurationException 
	 */
	public static EDABasic<?> initializeEDA(CommonConfig config) throws ConfigurationException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EDAException, ComponentException, ClassNotFoundException {

		String EDAclass = LAPUtils.getAttribute("PlatformConfiguration", "activatedEDA", config);
		System.out.println("Initializing EDA from class: " + EDAclass);
		
		return initializeEDA(Class.forName(LAPUtils.getAttribute("PlatformConfiguration", "activatedEDA", config)), config);
	}

}

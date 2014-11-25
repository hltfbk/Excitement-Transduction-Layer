package eu.excitementproject.tl.laputils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.configuration.NameValueTable;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.biu.uima.BIUFullLAP;

/**
 * A collection of static methods to initialize and manipulate a LAP object
 * 
 * @author vivi@fbk
 *
 */
public class LAPUtils {


	/**
	 * Initialize a LAP object based on the given class
	 * 
	 * @param lapClass -- the class of the LAP object
	 * @return an instance of the given class
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
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
	
	
	/**
	 * Special class for initializing a LAP object. If the LAP is for BIUTEE, then it needs the configuration file
	 * 
	 * @param lapClass -- the class of the desired LAP object
	 * @param config -- configuration file (usually for the EDA, but BIUTEE needs this for initializing the LAP as well)
	 * @return an instance of the given LAP class
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
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

	
	/**
	 * A "default" LAP initializer, based on the language.
	 * 
	 * @param language -- EN|IT|DE
	 * 
	 * @return an instance of the DependencyLevelLap/LemmaLevelLap for the given language
	 */
	public static LAPAccess initializeLAP(String language){
		
//		String lapClassName = "eu.excitementproject.tl.laputils.LemmaLevelLap" + language.toUpperCase();
		String lapClassName = "eu.excitementproject.tl.laputils.DependencyLevelLap" + language.toUpperCase();
		LAPAccess lap = null;
		
		Logger logger = Logger.getLogger("eu.excitementproject.tl.laputils.LAPUtils:initializeLAP");
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


	/**
	 * Initialize the LAP based on information from the EDA's configuration
	 * 
	 * @param config -- configuration (for the EDA)
	 * 
	 * @return an instance of the LAP class read from the config
	 */
	public static CachedLAPAccess initializeLap(CommonConfig config) {
				
		try {
			return initializeLap(Class.forName(getAttribute("PlatformConfiguration","activatedLAP", config)), config);
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}


	/**
	 * Method for getting the value of desired attributes from a CommonConfig object 
	 * 
	 * @param section -- the section where the attribute is (e.g. "PlatformConfiguration")
	 * @param attr -- the name of the wanted attribute (e.g. "activatedLAP", "activatedEDA")
	 * @param config -- the EDA configuration (as CommonConfig object)
	 * 
	 * @return the value of the wanted attribute (e.g. the class of the LAP/EDA)
	 */
	public static String getAttribute(String section, String attr, CommonConfig config) {

		Logger logger = Logger.getLogger("eu.excitementproject.tl.laputils.LAPUtils:getAttribute");
		logger.setLevel(Level.INFO);
		
		String attrValue = "";
		
		logger.info("Extracting attribute " + attr + " from section " + section + " from configuration file " + config.getConfigurationFileName() );

		try {
			NameValueTable nameValueTable = config.getSection(section);
		
			if (nameValueTable != null) {			
				attrValue = nameValueTable.getString(attr);
				logger.info("Attribute value : " + attrValue);
			} else {				
				logger.info("No " + section + " section found in configuration file " + config.getConfigurationFileName());
			}
		} catch (ConfigurationException e) {
				logger.error("Attribute " + attr + " not found in section " + section + " of configuration file " + config.getConfigurationFileName());
				e.printStackTrace();
		}
		
		return attrValue;
	}
	
}

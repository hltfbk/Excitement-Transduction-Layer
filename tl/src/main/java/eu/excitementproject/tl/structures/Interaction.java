package eu.excitementproject.tl.structures;

import org.apache.uima.jcas.JCas;

/**
 * This class defines a simple data structure that reflects WP2 data on
 * "one interaction". Note that this class is to be used just as a simple data holder without much functionality.  
 * Also note that internally in WP6, the standard notion of interaction representation is the InputCAS, as defined in the dataflow document.   
 * 
 * <P> This class is provided mainly to be used as one of "top level" argument, from WP7. See TopLevel API document for more detail. 
 * @author Gil
 *
 */

public class Interaction {

	
	/**
	 * This method first generates a new CAS and set language ID and CAS text by the information of this Interaction. 
	 * <P> 
	 * Note that creating a new JCAS is a costly function; (compared to reusing existing ones) so you should use fillInputCAS(), if you are running this to work sequentially.  
	 * 
	 * @return JCas a newly created JCAS that holds this Interaction. No annotations in it. No linguistic annotations in it. 
	 */
	public JCas createAndFillInputCAS()
	{
		// TODO fill in the code 
		return null; 
	}
	
	/**
	 * This methods gets one JCAS, cleans it up, and fill it with this Interaction. set language ID and CAS text by the information of this Interaction.
	 * 
	 * @param aJCas
	 */
	public void fillInputCAS(JCas aJCas)
	{
		// TODO fill in the code 
		
	}
	
	
}

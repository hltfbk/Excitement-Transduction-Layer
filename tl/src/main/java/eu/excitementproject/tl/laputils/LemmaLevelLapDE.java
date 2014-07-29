package eu.excitementproject.tl.laputils;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;

/**
 * 
 * LAP based on TreeTagger and a sentence separator. 
 * Note that this one is 30 times faster than TreeTaggerEN of EOP 0.8.3 
 * (EOP 0.8.4 and later has this improved TreeTagger LAPAccess... but WP6 prototype uses 0.8.3 so. ) 
 *  
 * @author Gil
 *
 */
public class LemmaLevelLapDE extends LemmaLevelLapEN implements LAPAccess {

	public LemmaLevelLapDE() throws LAPException
	{
		super(); 
		this.languageIdentifier = "DE"; 
	}
	
}

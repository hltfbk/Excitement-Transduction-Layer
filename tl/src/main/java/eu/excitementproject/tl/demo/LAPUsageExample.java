package eu.excitementproject.tl.demo;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
//import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * 
 * Use case example for LAP; mainly to show how to access dependency parser results. 
 * Please see EOPUsageExample first, which holds basic LAPAccess and EDABasic usages. 
 * 
 * @author Tae-Gil Noh
 *
 */
public class LAPUsageExample {

	private static final Logger logger = Logger.getLogger(LAPUsageExample.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Initializing a MaltParser 
		// (TreeTagger POS tagging & Lemmatization, then MaltParser dependency parsing)
		// See MaltParserEN class of EOP (LAP module), for more detail 
		//  ( e.g. how to use models other than default one, etc) 
		LAPAccess malt = null; 
		try {
			malt = new MaltParserEN();  // without argument, it initialize with the default model 
			//malt = new MaltParserDE();  // use this for German 			
		}
		catch (LAPException e)
		{
			logger.info(e.getMessage());
			System.exit(1); 
		}
		
		// Now time to call the LAP module to annotate something. 
		// If you give your CAS to LAP, you have to set "text", and "language" before ask addAnnotation 
		JCas aJCas = null; 
		try {
			aJCas = CASUtils.createNewInputCas(); // get one via CASUtils 

			aJCas.setDocumentText("WP6 successfully developed the needed methods for the industrial use cases."); 
			aJCas.setDocumentLanguage("EN"); 
			malt.addAnnotationOn(aJCas);  // actual annotation is being done. takes some time. 
			// Once the call addAnnotationOn successes, the aJCas has "sentence", "token", "lemma", "POS" and "Dependency" annotations. 
		}
		catch (LAPException e)
		{
			logger.info(e.getMessage());
			System.exit(2); 			
		}

		// Here, we will try to see/access (mainly) the dependency annotations in aJCas  

		// Okay. now JCas holds text, sentence annotation, POS annotation, lemma annotation, 
		// Dependency annotations, etc. Let's iterate over the dependency edges, and 
		// see what they have. 
		

		for (Dependency dep : JCasUtil.select(aJCas, Dependency.class)) {
			
			// One Dependency annotation holds the information for a dependency edge. 
			// Basically, 3 things; 
			// It holds "Governor (points to a Token)", "Dependent (also to a Token)", 
			// and relationship between them (as a string)
			Token dependent = dep.getDependent(); 
			Token governor = dep.getGovernor();
			String dTypeStr = dep.getDependencyType(); 
			
			// lets print them with full token information (lemma, pos, loc) 
			// info for the dependent ... 
			int dBegin = dependent.getBegin(); 
			int dEnd = dependent.getEnd(); 
			String dTokenStr = dependent.getCoveredText(); 
			String dLemmaStr = dependent.getLemma().getValue(); 
			String dPosStr = dependent.getPos().getPosValue(); 
			
			// info for the governor ...  
			int gBegin = governor.getBegin(); 
			int gEnd = governor.getEnd(); 
			String gTokenStr = governor.getCoveredText(); 
			String gLemmaStr = governor.getLemma().getValue(); 
			String gPosStr = governor.getPos().getPosValue(); 

			// and finally print the edge with full info 
			logger.info(dBegin + "-" + dEnd + " " + dTokenStr + "/" + dLemmaStr + "/" + dPosStr);
			logger.info("\t ---"+ dTypeStr + " --> "); 
			logger.info("\t " + gBegin + "-" + gEnd + " " + gTokenStr + "/" + gLemmaStr + "/" + gPosStr);
		}
	
	}

}

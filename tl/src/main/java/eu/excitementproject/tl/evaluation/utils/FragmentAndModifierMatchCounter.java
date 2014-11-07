package eu.excitementproject.tl.evaluation.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.uimafit.util.JCasUtil; 
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token; 
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitementproject.eop.lap.LAPException;

/**
 * 
 * This class is designed to count the number of true/false positives and 
 * true/false negatives (on the word level) from (system, gold) CASes. 
 * 
 * They counts can be used to calculate the evaluation measures (like accuracy, 
 * or F-1). 
 * 
 * @author Gil Noh 
 *
 */
public class FragmentAndModifierMatchCounter {

	
	/**
	 * This method counts and returns the matches on words for Modifier Annotations,
	 * of a system output Jcas, compared to Gold data (also a JCas) . 
	 * 
	 * It returns four numbers (true positive, false positive, true negative, false negative) 
	 * 
	 *  (true positive, false positive, true negative, false negative) 
	 * 
	 * @param sysOut
	 * @param gold
	 * @return
	 */
	public static List<Integer> countModifierCounts(JCas sysOut, JCas gold) throws LAPException 
	{
		
		Logger log = Logger.getLogger(FragmentAndModifierMatchCounter.class.getName()); 

		int truePos=0; 
		int falsePos=0; 
		int trueNeg=0; 
		int falseNeg=0; 
		
		// iterate over each token, on both sides 
		
		// for each token on gold, check if covering Mod annotation 
		// if there is, 
		//     sysOut also a mod? --> TruePos++
		//     sysOut not a mod?  --> FalseNeg++ 
		// if it is not, 
		//     check sysOut 
		//     sysOut also not? --> TrueNeg++
		//     sysOut is a mod? --> FalsePos++ 
		
		Collection<Token> goldTokens =  JCasUtil.select(gold, Token.class);
		Collection<Token> sysTokens =  JCasUtil.select(sysOut, Token.class);
		
		Iterator<Token> goldTokenIter = goldTokens.iterator(); 
		Iterator<Token> sysTokenIter = sysTokens.iterator(); 

		while(goldTokenIter.hasNext())
		{
			// for each token on gold, check if covering Mod annotation 
			Token goldToken = goldTokenIter.next(); 
			
			if (!sysTokenIter.hasNext()) // sanity check 
				throw new LAPException("Two JCas (Gold & SysOut) have different number of tokens!"); 
			
			Token sysToken = sysTokenIter.next(); 			
			log.debug("Gold token: " + goldToken.getCoveredText() + "\tSysToken: " + sysToken.getCoveredText()); 
			
			if ( (sysToken.getBegin() != goldToken.getBegin()) || (sysToken.getEnd() != goldToken.getEnd()) ) // another sanity check 
				throw new LAPException("Token on both side (Gold & SysOut) have different begin or end!"); 				
			
			Collection<ModifierAnnotation> goldModIter = JCasUtil.selectCovering(gold, ModifierAnnotation.class, goldToken.getBegin(), goldToken.getEnd() ); 
			Collection<ModifierAnnotation> sysModIter = JCasUtil.selectCovering(sysOut, ModifierAnnotation.class, sysToken.getBegin(), sysToken.getEnd() ); 
			if (goldModIter.size() > 0) // if there is,
			{
				ModifierAnnotation goldMod = goldModIter.iterator().next(); 
				log.debug("Gold Modifier Annot covers gold token ( " + goldMod.getCoveredText() + " )"); 
			
				// sysOut also a mod? --> TruePos++
				if (sysModIter.size() != 0)
				{
					ModifierAnnotation sysMod = sysModIter.iterator().next(); 
					log.debug("[TruePos] Sys Modifier Annot covers also ( " + sysMod.getCoveredText() + " )"); 
					truePos++; // actual counting of token 
				}
				else
				{
					// sysOut not a mod?  --> FalseNeg++ 
					log.debug("[FalseNeg] Sys Modifier Annot does not cover"); 
					falseNeg++; 
				}
			}
			else 
			{   // if it is not covered by modifier on gold side, 
				//     check sysOut 
				log.debug("Gold Modifier Annot does not covers gold."); 
				if (sysModIter.size() == 0 )
				{
					// sysOut also not? --> TrueNeg++
					log.debug("[TrueNeg] Sys Modifier Annot also does not covers."); 
					trueNeg++;					
				}
				else 
				{
					// sysOut is a mod? --> FalsePos++ 
					ModifierAnnotation sysMod = sysModIter.iterator().next(); 
					log.debug("[FalsePos] Sys Modifier covers this token ( " + sysMod.getCoveredText() +" )"); 
					falsePos++; 
				}
			}
			// at least one count has changed. 
			log.debug(String.format("TruePos: %d, FalsePos: %d, TrueNeg: %d, FalseNeg: %d)", truePos, falsePos, trueNeg, falseNeg)); 
		}
		List<Integer> fourInts = new ArrayList<Integer>(); 
		fourInts.add(truePos); 
		fourInts.add(falsePos); 
		fourInts.add(trueNeg); 
		fourInts.add(falseNeg); 
		
		return fourInts; 
	}
	

	/**
	 * This method counts and returns the matches on words for Fragment Annotation,
	 * of a system output Jcas, compared to Gold data (also a JCas) . 
	 * 
	 * It returns four numbers (true positive, false positive, true negative, false negative) 
	 * 
	 * @param sysOut
	 * @param gold
	 * @return
	 */
	public static List<Integer> countFragmentCounts(JCas sysOut, JCas gold) throws LAPException 
	{
		
		Logger log = Logger.getLogger(FragmentAndModifierMatchCounter.class.getName()); 

		log.info("Counting fragments in: \n");
		log.info("\tgold -- " + gold.getDocumentText() + " (" + JCasUtil.select(gold, Token.class).size() + " tokens)" );
		log.info("\tsysOut -- " + sysOut.getDocumentText() + " (" + JCasUtil.select(sysOut, Token.class).size() + " tokens)" );
		
		
		int truePos=0; 
		int falsePos=0; 
		int trueNeg=0; 
		int falseNeg=0; 
		
		// iterate over each token, on both sides 
		
		// for each token on gold, check if covering Fragment annotations are there.  
		// if there is, 
		//     sysOut also a in frag? --> TruePos++
		//     sysOut not a in frag?  --> FalseNeg++ 
		// if it is not, 
		//     check sysOut 
		//     sysOut also not? --> TrueNeg++
		//     sysOut is a frag? --> FalsePos++ 
		
		Collection<Token> goldTokens =  JCasUtil.select(gold, Token.class);
		Collection<Token> sysTokens =  JCasUtil.select(sysOut, Token.class);
		
		Iterator<Token> goldTokenIter = goldTokens.iterator(); 
		Iterator<Token> sysTokenIter = sysTokens.iterator(); 

		while(goldTokenIter.hasNext())
		{
			// for each token on gold, check if covering frag annotation 
			Token goldToken = goldTokenIter.next(); 
			
			if (!sysTokenIter.hasNext()) // sanity check 
				throw new LAPException("Two JCas (Gold & SysOut) have different number of tokens!"); 
			
			Token sysToken = sysTokenIter.next(); 			
			log.debug("Gold token: " + goldToken.getCoveredText() + "\tSysToken: " + sysToken.getCoveredText()); 
			
			if ( (sysToken.getBegin() != goldToken.getBegin()) || (sysToken.getEnd() != goldToken.getEnd()) ) // another sanity check 
				throw new LAPException("Token on both side (Gold & SysOut) have different begin or end!"); 				
			
			Collection<DeterminedFragment> goldFragIter = JCasUtil.selectCovering(gold, DeterminedFragment.class, goldToken.getBegin(), goldToken.getEnd() ); 
			Collection<DeterminedFragment> sysFragIter = JCasUtil.selectCovering(sysOut, DeterminedFragment.class, sysToken.getBegin(), sysToken.getEnd() ); 
			if (goldFragIter.size() > 0) // if there is,
			{
				DeterminedFragment goldFrag = goldFragIter.iterator().next(); 
				log.debug("Gold Frag Annot covers gold token ( " + goldFrag.getCoveredText() + " )"); 
			
				// sysOut also a mod? --> TruePos++
				if (sysFragIter.size() != 0)
				{
					DeterminedFragment sysFrag = sysFragIter.iterator().next(); 
					log.debug("[TruePos] Sys Fragment Annot covers also ( " + sysFrag.getCoveredText() + " )"); 
					truePos++; // actual counting of token 
				}
				else
				{
					// sysOut not a mod?  --> FalseNeg++ 
					log.debug("[FalsePos] Sys Fragment Annot does not cover"); 
					falseNeg++; 
				}
			}
			else 
			{   // if it is not covered by modifier on gold side, 
				//     check sysOut 
				log.debug("Gold Frag Annot does not covers the token."); 
				if (sysFragIter.size() == 0 )
				{
					// sysOut also not? --> TrueNeg++
					log.debug("[TrueNeg] Sys Frag Annot also does not covers."); 
					trueNeg++;					
				}
				else 
				{
					// sysOut is a mod? --> FalsePos++ 
					DeterminedFragment sysFrag = sysFragIter.iterator().next(); 
					log.debug("[FalsePos] but Sys Frag covers this token ( " + sysFrag.getCoveredText() +" )"); 
					falsePos++; 
				}
			}
			// at least one count has changed. 
			log.debug(String.format("TruePos: %d, FalsePos: %d, TrueNeg: %d, FalseNeg: %d)", truePos, falsePos, trueNeg, falseNeg)); 
		}
		List<Integer> fourInts = new ArrayList<Integer>(); 
		fourInts.add(truePos); 
		fourInts.add(falsePos); 
		fourInts.add(trueNeg); 
		fourInts.add(falseNeg); 
		
		return fourInts; 
	}
	
}

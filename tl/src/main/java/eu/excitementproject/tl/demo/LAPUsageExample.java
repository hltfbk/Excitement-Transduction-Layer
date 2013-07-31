package eu.excitementproject.tl.demo;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.PlatformCASProber;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
import eu.excitementproject.eop.lap.dkpro.MaltParserDE;
import eu.excitementproject.tl.laputils.CASUtils;

import org.apache.uima.cas.text.AnnotationIndex;
//import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;
import java.util.Iterator;
import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

/**
 * 
 * Use case example for LAP; mainly to show how to access dependency parser results. 
 * Please see EOPUsageExample first, which holds basic LAPAccess and EDABasic usages. 
 * 
 * @author Gil
 *
 */
public class LAPUsageExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Initializing a MaltParser 
		// (TreeTagger POS tagging & Lemmatization, then MaltParser dependency parsing)
		// See MaltParserEN class of EOP (LAP module), for more detail 
		LAPAccess malt = null; 
		try {
			malt = new MaltParserEN();  // without argument, it initialize with the default model 
//			malt = new MaltParserDE();  // without argument, it initialize with the default model 
		}
		catch (LAPException e)
		{
			System.out.println(e.getMessage());
			System.exit(1); 
		}
		
		// Now time to call the LAP module to annotate something. 
		// If you give your CAS to LAP, you have to set "text", and "language" before ask addAnnotation 
		JCas aJCas = null; 
		try {
			aJCas = CASUtils.createNewInputCas(); // get one via CASUtils 

			aJCas.setDocumentText("Hello. This is a text."); 
			aJCas.setDocumentLanguage("EN"); 
//			aJCas.setDocumentText("Ich habe Hunger."); 
//			aJCas.setDocumentLanguage("DE"); 
			malt.addAnnotationOn(aJCas);  // actual annotation is being done. takes some time. 
			// Once the call addAnnotationOn successes, the aJCas has "sentence", "token", "lemma", "POS" and "Dependency" annotations. 
			CASUtils.dumpCAS(aJCas); 
		}
		catch (LAPException e)
		{
			System.out.println(e.getMessage());
			System.exit(2); 			
		}
		
		
		// Here, we will try to see/access (mainly) the dependency annotations in aJCas  

		// Okay. now JCas holds text, sentence annotation, POS annotation, lemma annotation, 
		// Dependency annotation, etc. 		
		// Let's iterate over the tokens, and each token, let's see what lemmas and 
		// dependencies they have. 

		AnnotationIndex<Annotation> depIndex = aJCas.getAnnotationIndex(Dependency.type);
		Iterator<Annotation> depItr = depIndex.iterator(); 

		while (depItr.hasNext())
		{
			Dependency d = (Dependency) depItr.next(); 
			String t = d.getCoveredText(); 
			int b = d.getBegin(); 
			int e = d.getEnd(); 
			System.out.println(b); 
			System.out.println(e); 
			System.out.println(t); 
		}
		System.exit(1); 
		
		AnnotationIndex<Annotation> tokenIndex = aJCas.getAnnotationIndex(Token.type);
		Iterator<Annotation> tokenItr = tokenIndex.iterator(); 

		System.out.println("start-end token/lemma/POS\n\t [DependencyRelation] --> [GovernerToken]"); 
		while (tokenItr.hasNext())
		{
			// we are getting l, 
			Token t = (Token) tokenItr.next(); 
			int begin = t.getBegin(); 
			int end = t.getEnd(); 
			Lemma l = t.getLemma();
			String tokenStr = t.getCoveredText(); 
			String lemmaStr = l.getValue(); 
			String posStr = t.getPos().getPosValue(); 
			
			// lets get Dependency type annotation, that covers this lemma 
			List<Dependency> dl = JCasUtil.selectCovered(aJCas, Dependency.class , begin, end);
			
			if (dl.size() == 0)
				continue; 
			
			Dependency d = dl.get(0); 
			String dTypeStr = d.getDependencyType(); 		
			String governerTokenStr = d.getGovernor().getCoveredText(); 
			System.out.println(begin + "-" + end + " " + tokenStr + "/" + lemmaStr + "/" + posStr);
			System.out.println("\t "+ dTypeStr + " --> " + governerTokenStr); 
			
		}
	
	}

}

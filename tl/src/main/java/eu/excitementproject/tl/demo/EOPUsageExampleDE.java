package eu.excitementproject.tl.demo;

import java.io.File;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.configuration.CommonConfig;
//import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.dkpro.OpenNLPTaggerDE;
import eu.excitementproject.tl.laputils.CASUtils;

public class EOPUsageExampleDE {

	/**
	 * This small example shows how you can access EDA and LAP from EOP. 
	 * 
	 * <P> Currently it only shows TreeTagger and TIE (MaxEndClassification) EDA. 
	 * <P> This setup actually works (on lexical level) of three languages. 
	 * <P> Note that it currently uses model trained on 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			//////
			// PART A: 1 minute LAP. 
			// All right, let's generate one LAP --- say, TreeTagger EN, 
			// What it does? It is a LAP pipeline, based on TreeTagger and some other tool, 
			// 1) sentence separation and add "sentence" annotation 
			// 2) add POS tag for each token 
			// 3) add Lemma for each token 
			// And it process only English. 

			OpenNLPTaggerDE tagger = new OpenNLPTaggerDE(); 
			JCas aJCas = CASUtils.createNewInputCas();
			
			
			// Note that all LAPAccess implementations have the following main two methods. 		
			// two of its methods are of our interests. 
			// First. addAnnotationOn() this adds "every annotation that the LAP can do" 
			// to the given CAS. 
			
			// If you give your CAS to LAP, you have to set "text", and "language" before ask addAnnotation 
			aJCas.setDocumentText("Hello. This is a text"); 
			aJCas.setDocumentLanguage("EN"); 
			tagger.addAnnotationOn(aJCas); 
			// so after the call, now the aJCas has "sentence", "token", "lemma" and "POS" annotations. 
			
			// Second. It knows how to generate the input for EDA. 
			JCas edaInputCas = tagger.generateSingleTHPairCAS("WP6 successfully delivered the first prototype.", "The first WP6 prototype is working."); 
			
			// Now this edaInputCAS holds all the information needed for EDA to process this 
			// CAS to make a decision. It has TEXTVIEW, HYPOTHESISVIEW, etc. 
			
			// Congratulation. Now you know the basic. 
			// If you want to know more about how to "peek inside" a CAS, 
			// please check Gil's CASAccessExample project. 
			// https://github.com/gilnoh/cas_access_example

			// Also, check LAPAccess Javadoc. 
			// (To see Javadoc of Maven module, don't forget to load JavaDoc Jar artifact --  
			//  right click on project package explorer, -> maven -> Download JavaDoc) 
			
			//////
			// PART B: 1 minute EDA
			
			// Okay. let's read a configuration for the EDA. 
			File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml");
			
			CommonConfig config = null;
			config = new ImplCommonConfig(configFile);

			// If you take a look at the configuration, it holds a configuration for the given EDA. 
			// For detailed explanation, you have to read that EDAs documentation or configuration example file. 
			
			// Okay, let's initialize one EDA with this configuration 

			// Our Config was for this specific EDA ... Since we know it, let's init it directly. 
			MaxEntClassificationEDA meceda = new MaxEntClassificationEDA();	
			meceda.initialize(config); // this will load model, etc. 
			
			// Okay. Run. 
			TEDecision d = meceda.process(edaInputCas); 
			System.out.println(d.getDecision().toString() + " with confidence score " + d.getConfidence()); 
			
			// As you see, this ends up with Entailment, and you can see some score, if you look into the decision object. 
			// All right. This was just one minute version. Maybe 10 min version would follow later on ... 
		
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(1); 
		}
	}

}

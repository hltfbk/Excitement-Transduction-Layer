package eu.excitementproject.tl.demo;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.utilities.configuration.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.dkpro.MaltParserEN;
//import eu.excitementproject.eop.lap.PlatformCASProber;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.laputils.LemmaLevelLapEN;
import eu.excitementproject.tl.laputils.LemmaLevelLapIT;

public class EOPUsageExample {

	private static final Logger logger = Logger.getLogger(EOPUsageExample.class);

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

		BasicConfigurator.resetConfiguration(); 
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.INFO);  // for UIMA (hiding < INFO) 
		Logger testlogger = Logger.getLogger("eu.excitementproject.tl.demo"); 

		try {

			//////
			// PART A: 1 minute LAP. 
			// All right, let's generate one LAP --- say, TreeTagger EN, 
			// What it does? It is a LAP pipeline, based on TreeTagger and some other tool, 
			// 1) sentence separation and add "sentence" annotation 
			// 2) add POS tag for each token 
			// 3) add Lemma for each token 
			// And it process only English. 

			LAPAccess treetaggerEN = new LemmaLevelLapEN(); 
			JCas aJCas = CASUtils.createNewInputCas();
			
			
			// Note that all LAPAccess implementations have the following main two methods. 		
			// two of its methods are of our interests. 
			// First. addAnnotationOn() this adds "every annotation that the LAP can do" 
			// to the given CAS. 
			
			// If you give your CAS to LAP, you have to set "text", and "language" before ask addAnnotation 
			aJCas.setDocumentText("Hello. This is a text"); 
			aJCas.setDocumentLanguage("EN"); 
			treetaggerEN.addAnnotationOn(aJCas); 
			// so after the call, now the aJCas has "sentence", "token", "lemma" and "POS" annotations. 
			
			// Second. It knows how to generate the input for EDA. 
			JCas edaInputCas = treetaggerEN.generateSingleTHPairCAS("WP6 successfully delivered the first prototype.", "The first WP6 prototype is working."); 			

			// Use the following method to check and see the content of a CAS for EDAs 
			//PlatformCASProber.probeCas(edaInputCas, System.out); // probe only 
			//PlatformCASProber.probeCasAndPrintContent(edaInputCas, System.out); // probe and print content  
			 
			
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
			File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml");
			
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
			testlogger.info("TIE with English Basic Configuration."); 
			testlogger.info(d.getDecision().toString() + " with confidence score " + d.getConfidence()); 
			//logger.info(d.getDecision().toString() + " with confidence score " + d.getConfidence()); 
			
			// As you see, this ends up with Entailment, and you can see some score, if you look into the decision object. 
			// All right. This was just one minute version. Maybe 10 min version would follow later on ... 
		
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
			System.exit(1); 
		}

		
		
		// Part #2. This is a small test for German 
		try 
		{
			// Prepare a German LAP, and generate the Entailment Pair  
			LAPAccess treetaggerDE = new LemmaLevelLapDE(); 
			JCas edaInputCas = treetaggerDE.generateSingleTHPairCAS("WP6 haben schließlich den ersten Prototypen geliefert.", "Der erste Prototyp der WP6 funktioniert gut."); 
			
			// Uncomment one of the following method to check and see the content of a CAS for EDAs 
			//PlatformCASProber.probeCas(edaInputCas, System.out); // probe only 
			//PlatformCASProber.probeCasAndPrintContent(edaInputCas, System.out); // probe and print content  
			
			// First, Read German configuration for the TIE EDA GERMAN 
			File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml");
			CommonConfig config = null;
			config = new ImplCommonConfig(configFile);

			// Init the EDA class with the configuration 
			MaxEntClassificationEDA meceda = new MaxEntClassificationEDA();	
			meceda.initialize(config); // this will load model, etc. 
			
			// Okay. Run. 
			TEDecision d = meceda.process(edaInputCas); 
			testlogger.info("TIE with German Basic Configuration."); 
			testlogger.info(d.getDecision().toString() + " with confidence score " + d.getConfidence()); 
			//logger.info(d.getDecision().toString() + " with confidence score " + d.getConfidence()); 
			
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
			System.exit(1); 
		}

		// Part #3. This is a small test for Italian 
		try 
		{
			// Prepare an Italian LAP, and generate the Entailment Pair  
			LAPAccess treetaggerIT = new LemmaLevelLapIT(); 
			// I don't know anything about Italian, so feel free to update :-) 
			JCas edaInputCas = treetaggerIT.generateSingleTHPairCAS("WP6 hanno consegnato con successo il primo prototipo.", "Il primo prototipo del WP6 funziona bene."); 

			// Uncomment one of the following method to check and see the content of a CAS for EDAs 
			//PlatformCASProber.probeCas(edaInputCas, System.out); // probe only 
			//PlatformCASProber.probeCasAndPrintContent(edaInputCas, System.out); // probe and print content  

			// First, Read Italian configuration for the TIE  
			File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_IT.xml");
			CommonConfig config = null;
			config = new ImplCommonConfig(configFile);

			// Init the EDA class with the configuration 
			MaxEntClassificationEDA meceda = new MaxEntClassificationEDA();	
			meceda.initialize(config); // this will load model, etc. 
			
			// Okay. Run. 
			TEDecision d = meceda.process(edaInputCas); 
			testlogger.info("TIE with Italian Basic Configuration."); 
			testlogger.info(d.getDecision().toString() + " with confidence score " + d.getConfidence()); 
			//logger.info(d.getDecision().toString() + " with confidence score " + d.getConfidence()); 
			
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
			System.exit(1); 
		}
		
		// Part #4. TIE with Lexical Resources 
		try 
		{
			// To run this configuration: 
			// 1) You need to download eop-resources-1.0.2.tar.gz from the following URL 
			// 		http://hlt-services4.fbk.eu:8080/artifactory/simple/private-internal/eu/excitementproject/eop-resources/1.0.2.tar/eop-resources-1.0.2.tar.gz			
			// 2) Unpack it in some path.  
			// 3) Update the following configuration file: it must point WordNet and VerbOcean path of the unpacked eop-resources. 
            //    (wordNetFilesPath and verbOceanFilePath) 

			File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+WN+VO_EN.xml");

			// Setting up EDA and init. If anything missing (missing path), it will raise exception. 
			// Log4j Info will show VerbOcean and WordNet loaded (or not). 
			CommonConfig config = null;
			config = new ImplCommonConfig(configFile);
			MaxEntClassificationEDA meceda = new MaxEntClassificationEDA();	
			meceda.initialize(config); 
			testlogger.info("TIE English Configuration with WordNet + VerbOcean initialized"); 
						
			// The configuration requires lemmas, and We use TreeTagger EN 
			LAPAccess treetaggerEN = new LemmaLevelLapEN(); 
			JCas edaInputCas = treetaggerEN.generateSingleTHPairCAS("WP6 successfully delivered the first prototype.", "The first WP6 prototype is working."); 			
			
			// Okay. Run. 
			TEDecision d = meceda.process(edaInputCas); 
			testlogger.info("TIE with English Lexical Resources."); 
			testlogger.info(d.getDecision().toString() + " with confidence score " + d.getConfidence()); 
						
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
			System.exit(1); 
		}
		
		// Part #5. TIE EN with Full configurations (Lexical Resources + Parser features) 
		try 
		{
			// To run this configuration: 
			// 1) You need to download eop-resources-1.0.2.tar.gz from the following URL 
			// 		http://hlt-services4.fbk.eu:8080/artifactory/simple/private-internal/eu/excitementproject/eop-resources/1.0.2.tar/eop-resources-1.0.2.tar.gz			
			// 2) Unpack it in some path.  
			// 3) Update the following configuration file: it must point WordNet and VerbOcean path of the unpacked eop-resources. 
			//    (wordNetFilesPath and verbOceanFilePath) 
			File configFile = new File("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base+WN+VO+TP+TPPos+TS_EN.xml");

			// Setting up EDA and init. If anything missing (missing path), it will raise exception. 
			// Log4j Info will show VerbOcean and WordNet loaded (or not). 
			CommonConfig config = null;
			config = new ImplCommonConfig(configFile);
			MaxEntClassificationEDA meceda = new MaxEntClassificationEDA();	
			meceda.initialize(config); 
			testlogger.info("TIE English Configuration with Lexical resources and Parser features initialized"); 
						
			// The configuration requires Parse tree results, in addition to lemmas
			// so we use MaltParser EN here. 
			LAPAccess lap = new MaltParserEN(); 
			JCas edaInputCas = lap.generateSingleTHPairCAS("WP6 successfully delivered the first prototype.", "The first WP6 prototype is working."); 			
			
			// Okay. Run. 
			TEDecision d = meceda.process(edaInputCas); 
			testlogger.info("TIE with Parser features & Resources."); 
			testlogger.info(d.getDecision().toString() + " with confidence score " + d.getConfidence()); 
						
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
			System.exit(1); 
		}		
	}
}

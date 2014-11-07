package eu.excitementproject.tl.laputils;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.DataIntegrityFail;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;

public class ConvertWP2PublicData {

	private static final Logger logger = Logger.getLogger(ConvertWP2PublicData.class);

	/**
	 * This class reads WP2 fragment graph dump data from the /test/resources directory, 
	 * and generates InputCAS and store them as XMI (serialized CAS). 
	 * 
	 * <P> The stored XMI files can be read into InputCAS (a JCAS) by calling 
	 * CASUtils.deserializeFromXmi() 
	 * 
	 * <P> TODO: (update accordingly) Note that, the reader (InteractionReader.readWP2FragGraphDump()) for the moment only reads and generates continuous fragments. It will skip all non-continuous fragment annotations. New versions will be able to read such, after prototype. 
	 * 
	 * @param args no arguments will be processed 
	 * 
	 * @author Gil / Vivi@fbk / LiliKotlerman
	 */
	public static void main(String[] args) {

		// log4j setting
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.WARN);  

		int totalcount = 0; 
		Path dir = null; 
		Path dirInteractions = null; 
		Path outputdir = null; 
		Path outputdirPerFrag = null; 

		// This is the usage example. 
		// Use "processWP2Data()" for per-interaction XMI file generation. 
		// Use "processWP2DataPerFragment()" for per-fragment XMI file generation. 
		
		// DIR prepare:  
		// File names will be determined by "interaction name" (processWP2Data()), or 
		// "fragment XML name" (processWP2DataPerFramgnet()) 
		
		String cluster="Test/EMAIL0330";
		dir = Paths.get("./src/test/resources/NICE/EMAIL-DATASET/"+cluster+"/FragmentGraphs");
		dirInteractions = Paths.get("./src/test/resources/NICE/EMAIL-DATASET/"+cluster+"/Interactions");
		

		logger.info(dir.toFile().getAbsolutePath());
//		outputdir = Paths.get("./target/WP2_public_data_CAS_XMI/ALMA_social_media_perFrag");
//		outputdir = Paths.get("./src/test/resources/WP2_public_data_CAS_XMI/ALMA_social_media"); 
	//	outputdir = Paths.get("./src/test/resources/WP2_public_data_CAS_XMI/NICE_open_byFrag"); 

		outputdir = Paths.get("./src/test/outputs/NICE_open/"+cluster);
		outputdirPerFrag = Paths.get("./src/test/outputs/NICE_open_perFrag/"+cluster);
		
		// Actual call: use this for "per-fragment" XMI saving 
		totalcount += processWP2DataPerFragment(dir, dirInteractions, outputdirPerFrag, "EN"); 
		
		// Actual call: Use this, for "per-interaction" XMI saving. 
		totalcount += processWP2Data(dir, dirInteractions, outputdir, "EN"); 

		// end of "processWP2DataPerFragment()" example. 
	
		//
		// The following codes will process WP2 open data directory and 
		// generate all interactions as XMI (per interaction) on /target/ dir. 
		// 
		
//		// Let's build NICE e-mail data. 
//		{
//		dir = Paths.get("./src/test/resources/WP2_public_data/nice_email_1/");
//		outputdir = Paths.get("./target/WP2_public_data_CAS_XMI/nice_email_1"); 
//
//		totalcount += processWP2Data(dir, outputdir, "EN"); 
//		
//		dir = Paths.get("./src/test/resources/WP2_public_data/nice_email_2/");
//		outputdir = Paths.get("./target/WP2_public_data_CAS_XMI/nice_email_2"); 
//		totalcount += processWP2Data(dir, outputdir, "EN"); 
//
//		dir = Paths.get("./src/test/resources/WP2_public_data/nice_email_3/");
//		outputdir = Paths.get("./target/WP2_public_data_CAS_XMI/nice_email_3"); 
//		totalcount += processWP2Data(dir, outputdir, "EN"); 
//
//		dir = Paths.get("./src/test/resources/WP2_public_data/nice_speech/");
//		outputdir = Paths.get("./target/WP2_public_data_CAS_XMI/nice_speech"); 
//		totalcount += processWP2Data(dir, outputdir, "EN"); 	
//		}
//
//		// and for ALMAwave 
//		{
//		dir = Paths.get("./src/test/resources/WP2_public_data/alma_social_media/");
//		outputdir = Paths.get("./target/WP2_public_data_CAS_XMI/alma_social_media"); 
//		totalcount += processWP2Data(dir, outputdir, "IT"); 		
//
//		dir = Paths.get("./src/test/resources/WP2_public_data/alma_speech/");
//		outputdir = Paths.get("./target/WP2_public_data_CAS_XMI/alma_speech"); 
//		totalcount += processWP2Data(dir, outputdir, "IT"); 		
//		}

		logger.info("In total: " + totalcount + " XMI files generated, over /target/ directories"); 
	}
	
	/**
	 * @param from Directory Path, that holds WP2 public data .txt and XML (They have to be in one directory) 
	 * @param to Directory Path, where the new XMI files will be generated. 
	 * @param langID language ID. WP2 frag-dump data does not have language ID. Thus we need this. 
	 */
	public static int processWP2Data(Path from, Path to, String languageID)
	{
		
		Path dir = from; 
		Path outputdir = to; 
		
		try {
			if (Files.notExists(outputdir))
			{
				Files.createDirectories(outputdir); 
			}
		}
		catch (IOException e){
			System.err.println(e); 
		}
		
		// The work JCAS 
		JCas aJCas = null; 
		try {
			aJCas = CASUtils.createNewInputCas(); 
		}
		catch (LAPException e)
		{
		    System.err.println(e);
		    System.exit(1); 
		}
		
		int generated = 0; 
		
		// Outer loop access Interaction Text file (.txt) 
		// while inner loop accesses associated "fragment (fragment graphs) XML"
		try (DirectoryStream<Path> stream =
			     Files.newDirectoryStream(dir, "*.txt")) {
			    for (Path entry: stream) {
			        logger.info(entry.getFileName()); 
			        try (DirectoryStream<Path> xmlstream = Files.newDirectoryStream(dir, entry.getFileName() + "_" + "*.xml"))
			        {
			        	aJCas.reset(); 
			        	for (Path xmlfile : xmlstream)
			        	{			
			        		// call the reader. Note that it loads multiple XML files (multiple fragments) with same interaction  
			        		logger.info("\t" + xmlfile.getFileName()) ;
			        		InteractionReader.readWP2FragGraphDump(entry.toFile(), xmlfile.toFile(), aJCas, languageID); 			        		
			        	}			        	
			        	// Now the JCAS has one or more fragment annotations, and associated modifier annotations.  
			        	// (each XML = one fragment)
			        	// lets store it. 
			        	String outPathString = outputdir.toString() + "/" + entry.getFileName() + ".xmi";
			        	Path xmiPath = Paths.get(outPathString); 
			        	CASUtils.serializeToXmi(aJCas, xmiPath.toFile()); 		
			        	logger.info(xmiPath.toString() + " generated." );
			        	generated++; 
			        }
			        catch (DataIntegrityFail x)
			        {
			        	System.err.println(x); 
			        	// simply pass to next for loop element 
			        	System.err.println("Unable to proceed on " + entry.getFileName() +". Pass to next entry"); 
			        	continue; 
			        }
			        catch (IOException | DirectoryIteratorException | DataReaderException | LAPException x) {
					    System.err.println(x);
					    System.exit(2); 
			        }
			  }
		} catch (IOException | DirectoryIteratorException x ) {
		    System.err.println(x);
		}		
		
		logger.info("In " + outputdir.toString() + " : " + generated + " XMI files generated"); 
		return generated; 
	}

	/**
	 * @param fromFGs Directory Path, that holds WP2 public data XML files  
	 * @param fromInteractions Directory Path, that holds WP2 public data TXT files  
	 * @param to Directory Path, where the new XMI files will be generated. 
	 * @param langID language ID. WP2 frag-dump data does not have language ID. Thus we need this. 
	 */
	public static int processWP2Data(Path fromFGs, Path fromInteractions, Path to, String languageID)
	{
		
		Path outputdir = to; 
		
		try {
			if (Files.notExists(outputdir))
			{
				Files.createDirectories(outputdir); 
			}
		}
		catch (IOException e){
			System.err.println(e); 
		}
		
		// The work JCAS 
		JCas aJCas = null; 
		try {
			aJCas = CASUtils.createNewInputCas(); 
		}
		catch (LAPException e)
		{
		    System.err.println(e);
		    System.exit(1); 
		}
		
		int generated = 0; 
		
		// Outer loop access Interaction Text file (.txt) 
		// while inner loop accesses associated "fragment (fragment graphs) XML"
		try (DirectoryStream<Path> stream =
			     Files.newDirectoryStream(fromInteractions, "*.txt")) {
			    for (Path entry: stream) {
			        logger.info(entry.getFileName()); 
			        try (DirectoryStream<Path> xmlstream = Files.newDirectoryStream(fromFGs, entry.getFileName() + "_" + "*.xml"))
			        {
			        	aJCas.reset(); 
			        	for (Path xmlfile : xmlstream)
			        	{			
			        		// call the reader. Note that it loads multiple XML files (multiple fragments) with same interaction  
			        		logger.info("\t" + xmlfile.getFileName()) ;
			        		InteractionReader.readWP2FragGraphDump(entry.toFile(), xmlfile.toFile(), aJCas, languageID); 			        		
			        	}			        	
			        	// Now the JCAS has one or more fragment annotations, and associated modifier annotations.  
			        	// (each XML = one fragment)
			        	// lets store it. 
			        	String outPathString = outputdir.toString() + "/" + entry.getFileName() + ".xmi";
			        	Path xmiPath = Paths.get(outPathString); 
			        	CASUtils.serializeToXmi(aJCas, xmiPath.toFile()); 		
			        	logger.info(xmiPath.toString() + " generated." );
			        	generated++; 
			        }
			        catch (DataIntegrityFail x)
			        {
			        	System.err.println(x); 
			        	// simply pass to next for loop element 
			        	System.err.println("Unable to proceed on " + entry.getFileName() +". Pass to next entry"); 
			        	continue; 
			        }
			        catch (IOException | DirectoryIteratorException | DataReaderException | LAPException x) {
					    System.err.println(x);
					    System.exit(2); 
			        }
			  }
		} catch (IOException | DirectoryIteratorException x ) {
		    System.err.println(x);
		}		
		
		logger.info("In " + outputdir.toString() + " : " + generated + " XMI files generated"); 
		return generated; 
	}
	
	/**
	 * This method is roughly equal to processWP2Data() method, however, this version 
	 * saves XMI files per-fragment, instead of per-interaction 
	 * @param from Directory Path, that holds WP2 public data .txt and XML (They have to be in one directory) 
	 * @param to Directory Path, where the new XMI files will be generated. 
	 * @param langID language ID. WP2 frag-dump data does not have language ID. Thus we need this. 
	 */

	public static int processWP2DataPerFragment(Path from, Path to, String languageID)
	{
		
		Path dir = from; 
		Path outputdir = to; 
		
		try {
			if (Files.notExists(outputdir))
			{
				Files.createDirectories(outputdir); 
			}
		}
		catch (IOException e){
			System.err.println(e); 
		}
		
		// The work JCAS 
		JCas aJCas = null; 
		try {
			aJCas = CASUtils.createNewInputCas(); 
		}
		catch (LAPException e)
		{
		    System.err.println(e);
		    System.exit(1); 
		}
		
		int generated = 0; 
		
		// Outer loop access Interaction Text file (.txt) 
		// while inner loop accesses associated "fragment (fragment graphs) XML"
		try (DirectoryStream<Path> stream =
			     Files.newDirectoryStream(dir, "*.txt")) {
			    for (Path entry: stream) {
			        logger.info(entry.getFileName()); 
			        try (DirectoryStream<Path> xmlstream = Files.newDirectoryStream(dir, entry.getFileName() + "_" + "*.xml"))
			        {
			        	for (Path xmlfile : xmlstream)
			        	{			
				        	aJCas.reset();       		
			        		// call the reader. Note that it loads multiple XML files (multiple fragments) with same interaction  
			        		logger.info("\t" + xmlfile.getFileName()) ;
			        		InteractionReader.readWP2FragGraphDump(entry.toFile(), xmlfile.toFile(), aJCas, languageID); 			        		
			        		
				        	// Now the JCAS has one fragment annotations, and associated modifier annotations.  
				        	// (each XML = one fragment)
				        	// lets store it. 
				        	String outPathString = outputdir.toString() + "/" + xmlfile.getFileName() + ".xmi";
				        	Path xmiPath = Paths.get(outPathString); 
				        	CASUtils.serializeToXmi(aJCas, xmiPath.toFile()); 		
				        	logger.info(xmiPath.toString() + " generated." );
				        	generated++; 

			        	}			        	
			        }
			        catch (DataIntegrityFail x)
			        {
			        	System.err.println(x); 
			        	// simply pass to next for loop element 
			        	System.err.println("Unable to proceed on " + entry.getFileName() +". Pass to next entry"); 
			        	continue; 
			        }
			        catch (IOException | DirectoryIteratorException | DataReaderException | LAPException x) {
					    System.err.println(x);
					    System.exit(2); 
			        }
			  }
		} catch (IOException | DirectoryIteratorException x ) {
		    System.err.println(x);
		}		
		
		logger.info("In " + outputdir.toString() + " : " + generated + " XMI files generated"); 
		return generated; 
	}

	/**
	 * This method is roughly equal to processWP2Data() method, however, this version 
	 * saves XMI files per-fragment, instead of per-interaction 
	 * @param fromFGs Directory Path, that holds WP2 public data XML files  
	 * @param fromInteractions Directory Path, that holds WP2 public data TXT files  
	 * @param to Directory Path, where the new XMI files will be generated. 
	 * @param langID language ID. WP2 frag-dump data does not have language ID. Thus we need this. 
	 */

	public static int processWP2DataPerFragment(Path fromFGs, Path fromInteractions, Path to, String languageID)
	{
		
		Path outputdir = to; 
		
		try {
			if (Files.notExists(outputdir))
			{
				Files.createDirectories(outputdir); 
			}
		}
		catch (IOException e){
			System.err.println(e); 
		}
		
		// The work JCAS 
		JCas aJCas = null; 
		try {
			aJCas = CASUtils.createNewInputCas(); 
		}
		catch (LAPException e)
		{
		    System.err.println(e);
		    System.exit(1); 
		}
		
		int generated = 0; 
		
		// Outer loop access Interaction Text file (.txt) 
		// while inner loop accesses associated "fragment (fragment graphs) XML"
		try (DirectoryStream<Path> stream =
			     Files.newDirectoryStream(fromInteractions, "*.txt")) {
			    for (Path entry: stream) {
			        logger.info(entry.getFileName()); 
			        try (DirectoryStream<Path> xmlstream = Files.newDirectoryStream(fromFGs, entry.getFileName() + "_" + "*.xml"))
			        {
			        	for (Path xmlfile : xmlstream)
			        	{			
				        	aJCas.reset();       		
			        		// call the reader. Note that it loads multiple XML files (multiple fragments) with same interaction  
			        		logger.info("\t" + xmlfile.getFileName()) ;
			        		InteractionReader.readWP2FragGraphDump(entry.toFile(), xmlfile.toFile(), aJCas, languageID); 			        		
			        		
				        	// Now the JCAS has one fragment annotations, and associated modifier annotations.  
				        	// (each XML = one fragment)
				        	// lets store it. 
				        	String outPathString = outputdir.toString() + "/" + xmlfile.getFileName() + ".xmi";
				        	Path xmiPath = Paths.get(outPathString); 
				        	CASUtils.serializeToXmi(aJCas, xmiPath.toFile()); 		
				        	logger.info(xmiPath.toString() + " generated." );
				        	generated++; 

			        	}			        	
			        }
			        catch (DataIntegrityFail x)
			        {
			        	System.err.println(x); 
			        	// simply pass to next for loop element 
			        	System.err.println("Unable to proceed on " + entry.getFileName() +". Pass to next entry"); 
			        	continue; 
			        }
			        catch (IOException | DirectoryIteratorException | DataReaderException | LAPException x) {
					    System.err.println(x);
					    System.exit(2); 
			        }
			  }
		} catch (IOException | DirectoryIteratorException x ) {
		    System.err.println(x);
		}		
		
		logger.info("In " + outputdir.toString() + " : " + generated + " XMI files generated"); 
		return generated; 
	}

}

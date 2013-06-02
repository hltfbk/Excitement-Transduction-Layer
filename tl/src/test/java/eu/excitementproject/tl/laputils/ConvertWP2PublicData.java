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
	 * @author Gil 
	 */
	public static void main(String[] args) {

		// log4j setting
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.WARN);  

		int totalcount = 0; 
		Path dir = null; 
		Path outputdir = null; 

		// Let's build NICE e-mail data. 
		{
		dir = Paths.get("./src/test/resources/WP2_public_data/nice_email_1/");
		outputdir = Paths.get("./target/WP2_public_data_XMI/nice_email_1"); 

		totalcount += processWP2Data(dir, outputdir, "EN"); 
		
		dir = Paths.get("./src/test/resources/WP2_public_data/nice_email_2/");
		outputdir = Paths.get("./target/WP2_public_data_XMI/nice_email_2"); 
		totalcount += processWP2Data(dir, outputdir, "EN"); 

		dir = Paths.get("./src/test/resources/WP2_public_data/nice_email_3/");
		outputdir = Paths.get("./target/WP2_public_data_XMI/nice_email_3"); 
		totalcount += processWP2Data(dir, outputdir, "EN"); 

		dir = Paths.get("./src/test/resources/WP2_public_data/nice_speech/");
		outputdir = Paths.get("./target/WP2_public_data_XMI/nice_speech"); 
		totalcount += processWP2Data(dir, outputdir, "EN"); 	
		}

		// and for ALMAwave 
		{
		dir = Paths.get("./src/test/resources/WP2_public_data/alma_social_media/");
		outputdir = Paths.get("./target/WP2_public_data_XMI/alma_social_media"); 
		totalcount += processWP2Data(dir, outputdir, "IT"); 		

		dir = Paths.get("./src/test/resources/WP2_public_data/alma_speech/");
		outputdir = Paths.get("./target/WP2_public_data_XMI/alma_speech"); 
		totalcount += processWP2Data(dir, outputdir, "IT"); 		
		}

		System.out.println("In total: " + totalcount + " XMI files generated, over /target/ directories"); 
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
			        System.out.println(entry.getFileName()); 
			        try (DirectoryStream<Path> xmlstream = Files.newDirectoryStream(dir, entry.getFileName() + "_" + "*.xml"))
			        {
			        	aJCas.reset(); 
			        	for (Path xmlfile : xmlstream)
			        	{			
			        		// call the reader. Note that it loads multiple XML files (multiple fragments) with same interaction  
			        		System.out.println("\t" + xmlfile.getFileName()) ;
			        		InteractionReader.readWP2FragGraphDump(entry.toFile(), xmlfile.toFile(), aJCas, languageID); 			        		
			        	}			        	
			        	// Now the JCAS has one or more fragment annotations, and associated modifier annotations.  
			        	// (each XML = one fragment)
			        	// lets store it. 
			        	String outPathString = outputdir.toString() + "/" + entry.getFileName() + ".xmi";
			        	Path xmiPath = Paths.get(outPathString); 
			        	CASUtils.serializeToXmi(aJCas, xmiPath.toFile()); 		
			        	System.out.println(xmiPath.toString() + " generated." );
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
		
		System.out.println("In " + outputdir.toString() + " : " + generated + " XMI files generated"); 
		return generated; 
	}
	
}

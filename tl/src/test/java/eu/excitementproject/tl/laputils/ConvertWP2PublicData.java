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
	 * @param args no arguments will be processed 
	 * 
	 * @author Gil 
	 */
	public static void main(String[] args) {

		// log4j setting
		BasicConfigurator.configure(); 
		Logger.getRootLogger().setLevel(Level.WARN);  

		// TODO: when adding more data, move the following to a method 
		// (e.g: processWP2DataInDir(Path) ) 
		Path dir = Paths.get("./src/test/resources/WP2_public_data/NICE_email_data1/");
		Path outputdir = Paths.get("./target/WP2_public_data_XMI/"); 
		try {
			if (Files.notExists(outputdir))
			{
				Files.createDirectory(outputdir); 
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
			        		InteractionReader.readWP2FragGraphDump(entry.toFile(), xmlfile.toFile(), aJCas); 			        		
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
		
		System.out.println("In total: " + generated + " XMI files generated"); 
	}
}

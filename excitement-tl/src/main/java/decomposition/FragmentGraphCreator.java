package decomposition;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import decomposition.buildgraph.Document;
import decomposition.buildgraph.ObjectFactory;
import decomposition.buildgraph.buildCieg;
import decomposition.buildgraph.Document.Markables;
import decomposition.buildgraph.Document.Markables.FRAGMENT;
import decomposition.buildgraph.ex;

/** 
 * Creates a fragment graph (what's called a CIEG in WP2) based on WP2's build-graph procedure. 
 * Works language-independently.
 * 
 * @author Kathrin
 * 
 */

public class FragmentGraphCreator {
	
	public static void main(String[] args) throws JAXBException, IOException {
		FragmentGraphCreator fgc = new FragmentGraphCreator();
		String currentDir = System.getProperty("user.dir");
		fgc.createFragmentGraph(currentDir+"/data/example.txt.xml");
	}

	/** At the moment, this method simply calls the two build-graph procedures that create a fragment graph. 
	 * @param fileName The file containing the input text tokens and fragment/modifier information.
	 * 
	 * See data/example.txt.xml for an example of the document structure.
	 */
	public String createFragmentGraph(String fileName) throws JAXBException, IOException {
		String filePathMy = ex.run(fileName);
		String outputFile = buildCieg.run(filePathMy);
		System.out.println("Output printed to " + outputFile);
		return outputFile;
	}
}

package eu.excitementproject.tl.structures.utils;

import java.io.File;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Class for saving xml data to the file system
 * @author Lili Kotlerman
 *
 */
public class XMLFileWriter {
	
	public static void write(DOMSource source, String filename) throws TransformerException{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		File f = new File(filename);
		StreamResult result = new StreamResult(f.toURI().getPath());

		transformer.transform(source, result);
	}
}

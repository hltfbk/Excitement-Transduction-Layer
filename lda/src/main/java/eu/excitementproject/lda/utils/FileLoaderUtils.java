package eu.excitementproject.lda.utils;


import gnu.trove.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
public class FileLoaderUtils {
	
	/**
	 * assumes the file tab-delimited and of the format <i>string</i> <i>id</i>
	 * @param file
	 * @return
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws BiDirectionalHashException
	 */
	
	
	public static TObjectIntHashMap<String> loadStringToIntFile(File file) throws NumberFormatException, IOException {

		TObjectIntHashMap<String> string2IntMap = new TObjectIntHashMap<String>();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while((line=reader.readLine())!=null) {
			String[] tokens = line.split("\t");
			string2IntMap.put(tokens[0], Integer.parseInt(tokens[1]));
		}
		reader.close();
		return string2IntMap;
	}
	
	
	
}

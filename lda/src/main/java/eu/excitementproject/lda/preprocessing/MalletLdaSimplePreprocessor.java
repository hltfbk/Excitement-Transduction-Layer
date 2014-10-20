package eu.excitementproject.lda.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import eu.excitementproject.lda.utils.MalletDocUtil;


/**
	 * <p> This is an example of MalletLdaPreprocessor implementation. This simple preprocessor assumes every line contains a document and its terms in the following format:
	 * <p> <code>[doc] \t : \t [term] \t [term] \t [term] \t [term] ... </code></p>
	 * <p> No stop-words filtering, lemmatizing etc. is performed - the terms are extracted exactly as given in the input file 

 * @author Lili Kotlerman
 */
public class MalletLdaSimplePreprocessor implements MalletLdaPreprocessor{

	/**
	 */	 	 
	
	public MalletLdaSimplePreprocessor() {
		super();
	}
	
	/**
	 * transform corpus to a format that allows importing with mallets <i>import</i> tool
	 * @param extractionFile
	 * @param stopWordFile
	 * @throws IOException
	 */
	public Map<String,Set<String>> preprocessData(File extractionFile, File outFile) throws IOException {
			
		Map<String,Set<String>> doc2TermsMap = new TreeMap<String,Set<String>>();
		BufferedReader reader = new BufferedReader(new FileReader(extractionFile));
		
		// format:  document in brackets + 2 tabs + tab-separated list of terms in brackets
		// [doc] \t : \t [term] \t [term] \t [term] \t [term] ... 
		String line;
		int i = 0;
		while((line=reader.readLine())!=null) {
			String[] tokens = line.split("\t:\t");
			String doc = MalletDocUtil.toMalletToken(tokens[0]);
			String termString = tokens[1];
			
			addTermsToDoc(doc, termString, doc2TermsMap);
			
			System.out.println("---"+doc+":"+termString);
		
			i++;
			if(i % 1000000 == 0)
				System.out.println("Lines: " + i);
		}
		reader.close();
		System.out.println("Number of documents: " + doc2TermsMap.size());
		
		PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));
		int docs = 0;
		long terms = 0;
		for(String pred: doc2TermsMap.keySet()) {
			
			writer.print(pred+"\tD\t"); // "D" is the 'label' of the instance (document) - needed for format compatibility with mallet code.  			
			for(String term: doc2TermsMap.get(pred)) {
				writer.print(term+"\t");
			}
			writer.println();
			docs++;
			terms += doc2TermsMap.get(pred).size();
		}
		writer.close();
		
		System.out.println("Number of lines (documents) written: "  + docs);
		System.out.println("Number of terms written: "  + terms);
		System.out.println("Average num of terms per doc: "  + terms/docs);
		
		return doc2TermsMap;

	}
	
	/**
	 * Get array of arguments from argument string
	 * @return
	 */
	protected static List<String> getTerms(String termString) {
		
		String[] argTokens;
		List<String> termList = new LinkedList<String>();
		argTokens = termString.split("\t");			
		for (String tok : argTokens) {
			termList.add(tok);
		}
		
		return termList;
		
	}
	
	private void addTermsToDoc(String doc, String termString, Map<String,Set<String>> doc2TermsMap) {		
		Set<String> docTerms = doc2TermsMap.get(doc);
		if(docTerms==null) {
			docTerms = new HashSet<String>();
			doc2TermsMap.put(doc, docTerms);
		}
		
		List<String> terms = getTerms(termString);
		for (String term : terms) {
			docTerms.add(MalletDocUtil.toMalletToken(term));
		}
	}
		
}

/**
 * 
 */
package eu.excitementproject.lda.utils;

/**
 * @author Oren Melamud
 * Used to convert to/from mallet string tokens representations 
 *
 */
public class MalletDocUtil {
	
	public static String toMalletToken(String term) {
		return ("[" + term + "]");
	}
	
	public static String fromMalletToken(String token) {
		return token.substring(1, token.length()-1);
	}
	
	public static String[] tokenize(String document) {
		return document.split("\t");
	}
	
	public static void main(String[] args) {
		String term = "one two";
		System.out.println("term: " + term);
		System.out.println("term with brackets: " + toMalletToken(term));
		
		String bracketArg = "[one two]";
		System.out.println("bracketToken: " + bracketArg);
		System.out.println("bracketToken without brackets: " + fromMalletToken(bracketArg));
		
	}

}


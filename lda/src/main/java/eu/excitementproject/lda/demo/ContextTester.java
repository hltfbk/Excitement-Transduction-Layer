package eu.excitementproject.lda.demo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;

import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.lda.core.MalletLda;

/**
 * @author Jonathan Berant & Oren Melamud & Lili Kotlerman
 *
 */
public abstract class ContextTester {
	
	ConfigurationFile confFile;
	
	public ContextTester(String configurationFileName) throws Exception {
		confFile = new ConfigurationFile(configurationFileName);		
	}

	public abstract Map<String,Set<String>> preprocessMalletLdaData(ConfigurationFile cf) throws IOException, ConfigurationException;	
	
	public abstract void printDocumentTopicProbabilities() throws Exception;
	
	protected static void printUsage() {
		System.out.println("ContextTester <config-file>");
	}

	public void runDemo() throws Exception {
				
		//EL.init(cf.getModuleConfiguration("logging"));		
		ConfigurationParams cp = confFile.getModuleConfiguration("context-tester");
		
		Map<String,Set<String>> doc2TermsMap = null;
		
		String[] steps = cp.get("execute-steps").split("\\s+");		
		for (String step : steps) {
			
			String[] stepParams = step.split("=");
			if (stepParams[1].equals("n")) {
				continue;
			}
			
		//	EL.info("Starting step: " + step);
			System.out.println("Starting step: " + step);
			
			if (stepParams[0].equals("preprocess")) {
				doc2TermsMap = this.preprocessMalletLdaData(confFile);
				continue;					
			}

			if (stepParams[0].equals("train")) {
				trainLda(confFile);
				continue;					
			}
			
			if (stepParams[0].equals("termprob")) {
				generateTermProbGivenTopicFile(confFile);
				continue;					
			}
			
			if (stepParams[0].equals("reverse-termprob")) {
				if (doc2TermsMap == null) {
					throw new Exception("doc2TermsMap is null");
				}
				generateTopicProbGivenDocAndTermFile(confFile, doc2TermsMap);
				continue;
			}

			if (stepParams[0].equals("topwords")) {
				printTopWords(confFile);
				continue;					
			}
		
			if (stepParams[0].equals("doctopics")) {
				printDocumentTopicProbabilities();
				continue;					
			}
	//		EL.error("Unsupported step requested: " + step);
			
		}
	}



	

	public static void trainLda(ConfigurationFile cf) throws Exception {
		MalletLda lda = new MalletLda(cf.getModuleConfiguration("lda"));
		ConfigurationParams cp = cf.getModuleConfiguration("context-tester");
		File corpusFile = new File(cp.get("extraction-documents-file"));
		lda.train(corpusFile);
	}

	public static void trainLda(ConfigurationFile cf, int numOfTopics) throws Exception {
		MalletLda lda = new MalletLda(cf.getModuleConfiguration("lda"), numOfTopics);
		ConfigurationParams cp = cf.getModuleConfiguration("context-tester");
		File corpusFile = new File(cp.get("extraction-documents-file"));
		lda.train(corpusFile);
	}

	public static void printTopWords(ConfigurationFile cf) throws Exception {

		ConfigurationParams cpLda = cf.getModuleConfiguration("lda");
		
		MalletLda lda = new MalletLda(cpLda);		
		
		ConfigurationParams cp = cf.getModuleConfiguration("context-tester");		
		File outFile = new File(cp.get("topic-top-words-file"));

		ParallelTopicModel model = lda.getModel();
		PrintWriter writer = new PrintWriter(outFile);
		Alphabet alphabet = model.getAlphabet();
		
		double sumAlpha = 0;
		for (double alpha : model.alpha) {
			sumAlpha += alpha;
		}
		
		List<TreeSet<IDSorter>> topicWords = lda.calcTermProbGivenTopic();
		int i=0;
		writer.println("Sum alpha: " + sumAlpha);
		writer.println("Beta: " + model.beta);
		writer.println();
		for (TreeSet<IDSorter> words : topicWords) {
			int j=0;
			writer.println("Topic " + i + " prior probability: " + model.alpha[i]/sumAlpha + "  Top 100 words:");
			writer.println("--------------------");
			for (IDSorter word : words) {
				writer.printf("%s\t%.5f\n", alphabet.lookupObject(word.getID()) ,word.getWeight());
				j++;
				if (j >= 100) {
					break;
				}
			}
			
			writer.println();
			
			i++;
		}
		
		writer.close();
	}


	public static void printTopWords(ConfigurationFile cf, int numOfTopics) throws Exception {

		ConfigurationParams cpLda = cf.getModuleConfiguration("lda");
		
		MalletLda lda = new MalletLda(cpLda);		
		
		ConfigurationParams cp = cf.getModuleConfiguration("context-tester");		
		File outFile = new File(cp.get("topic-top-words-file").replace(".txt","_"+String.valueOf(numOfTopics)+".txt"));

		ParallelTopicModel model = lda.getModel();
		PrintWriter writer = new PrintWriter(outFile);
		Alphabet alphabet = model.getAlphabet();
		
		double sumAlpha = 0;
		for (double alpha : model.alpha) {
			sumAlpha += alpha;
		}
		
		List<TreeSet<IDSorter>> topicWords = lda.calcTermProbGivenTopic();
		int i=0;
		writer.println("Sum alpha: " + sumAlpha);
		writer.println("Beta: " + model.beta);
		writer.println();
		for (TreeSet<IDSorter> words : topicWords) {
			int j=0;
			writer.println("Topic " + i + " prior probability: " + model.alpha[i]/sumAlpha + "  Top 100 words:");
			writer.println("--------------------");
			for (IDSorter word : words) {
				writer.printf("%s\t%.5f\n", alphabet.lookupObject(word.getID()) ,word.getWeight());
				j++;
				if (j >= 100) {
					break;
				}
			}
			
			writer.println();
			
			i++;
		}
		
		writer.close();
	}	
	public static void generateTermProbGivenTopicFile(ConfigurationFile cf) throws Exception {

		MalletLda mallet = new MalletLda(cf.getModuleConfiguration("lda"));
		ConfigurationParams cp = cf.getModuleConfiguration("context-tester");
		File outFile = new File(cp.get("term-topic-probabilities-file"));
		mallet.calcTermsProbGivenTopic(outFile);
	}
	
	public static void generateTopicProbGivenDocAndTermFile(ConfigurationFile cf, Map<String,Set<String>> doc2TermsMap) throws Exception {

		MalletLda mallet = new MalletLda(cf.getModuleConfiguration("lda"));
		ConfigurationParams cp = cf.getModuleConfiguration("context-tester");
		File outFile = new File(cp.get("topic-predicate-term-probabilities-file"));
		double minProb = cp.getDouble("min-topic-doc-term-probability");
		int maxRank = cp.getInt("max-topic-doc-term-rank");
		mallet.calcTopicProbGivenDocAndTerm(outFile, minProb, maxRank, doc2TermsMap);
	}		
}

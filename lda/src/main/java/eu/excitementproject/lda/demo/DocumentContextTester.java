package eu.excitementproject.lda.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.types.FeatureSequence;


import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.lda.core.MalletLda;
import eu.excitementproject.lda.core.TermIntersectionTopicDistCalculator;
import eu.excitementproject.lda.preprocessing.MalletLdaPreprocessor;
import eu.excitementproject.lda.preprocessing.MalletLdaSimplePreprocessor;
import eu.excitementproject.lda.utils.MalletDocUtil;
import eu.excitementproject.lda.utils.ModelUtils;

/**
 * @author Lili Kotlerman
 *
 */
public class DocumentContextTester extends ContextTester{
	
	public DocumentContextTester(String configurationFileName) throws Exception{
		super(configurationFileName);
	}

	public Map<String,Set<String>> preprocessMalletLdaData(ConfigurationFile cf) throws IOException, ConfigurationException {

		ConfigurationParams cp = cf.getModuleConfiguration("context-tester");
		File extractionFile = new File(cp.get("extractions-file"));
		File outFile = new File(cp.get("extraction-documents-file"));
		
		
//		EL.info("Extractions file is: " + extractionFile.getAbsolutePath());
		System.out.println("Extractions file is: " + extractionFile.getAbsolutePath());
		
		MalletLdaPreprocessor preprocessor = new MalletLdaSimplePreprocessor();
		return preprocessor.preprocessData(extractionFile, outFile);
	}


	
	public Map<String,String> getDocumentTopicProbabilities() throws Exception {
		MalletLda mallet = new MalletLda(confFile.getModuleConfiguration("lda"));
		
		System.out.println("Getting model...");
		ParallelTopicModel model = mallet.getModel();
		System.out.println("Done");
		
		Map<String,String> doc2TopicDist = new TreeMap<String,String>();
		
		ArrayList<TopicAssignment> data = model.getData();
		for(int i = 0; i < data.size() ; ++i) {
			
			int docLength = ((FeatureSequence) data.get(i).instance.getData()).getLength();
			String docName = (String) data.get(i).instance.getName();
			docName = MalletDocUtil.fromMalletToken(docName);	// remove brackets [doc]
			
			doc2TopicDist.put(docName, ModelUtils.convertTopicDist2Sparse(model, model.getTopicProbabilities(i),docLength).toString());
			
			if(i % 1000 == 0) 
				System.out.println("Finished document: " + i);
		}
		System.out.println();
		
		return doc2TopicDist;
	}
	
	public Map<String,String> getDocumentTopicProbabilities(String modelFilename) throws Exception {
			
		ConfigurationParams cp = confFile.getModuleConfiguration("context-tester");
		File corpusFile = new File(cp.get("extraction-documents-file"));

		System.out.println("Corpus file found");
		
		MalletLda mallet = new MalletLda(confFile.getModuleConfiguration("lda"), modelFilename);
		System.out.println("MalletLda init done");
		TermIntersectionTopicDistCalculator topicCalculator = new TermIntersectionTopicDistCalculator(mallet);		
		System.out.println("TermIntersectionTopicDistCalculator created");
		
		Map<String,String> doc2TopicDist = new TreeMap<String,String>();
		
		BufferedReader r = new BufferedReader(new FileReader(corpusFile));
		String line = r.readLine();
		while( line != null){
			String docName = line.split("\t")[0];
			docName = MalletDocUtil.fromMalletToken(docName);			
			String topicDist = topicCalculator.calculateSparseTopicDistribution(line).toString();
			doc2TopicDist.put(docName, topicDist);
			line = r.readLine();
		}
		r.close();
	
		return doc2TopicDist;
	}
	
	
	public void printDocumentTopicProbabilities() throws Exception {
		
		Map<String,String> doc2TopicDist = getDocumentTopicProbabilities();
		
		ConfigurationParams cp = confFile.getModuleConfiguration("context-tester");
		PrintWriter writer = new PrintWriter(new FileOutputStream(cp.get("document-topics-file")));
		
		for(String doc : doc2TopicDist.keySet()) 
			writer.println(doc+"\t"+doc2TopicDist.get(doc));

		writer.close();
	}
	
	public static void main(String[] args) throws Exception {
	//	String conf = "./src/test/resources/liliConf.xml";
		DocumentContextTester tester = new DocumentContextTester(args[0]);
		tester.runDemo();
	}

	
}

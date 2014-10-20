package eu.excitementproject.lda.core;

import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.lda.rep.DocTermTuple;
import eu.excitementproject.lda.rep.SparseTopicRep;
import eu.excitementproject.lda.rep.TopicIdProbPair;
import eu.excitementproject.lda.rep.TopicProbRankTuple;
import eu.excitementproject.lda.rep.records.TopicProbGivenDocAndTermRecord;
import eu.excitementproject.lda.utils.MalletDocUtil;
import eu.excitementproject.lda.utils.ModelUtils;
import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TShortFloatHashMap;


/**
 * @author Jonathan Berant, Oren Melamud, Lili Kotlerman
 *
 */
public class MalletLda {

	private int m_numOfTopics;
	private double m_alpha;
	private double m_beta;
	private int m_numOfThreads;
	private int m_numOfIterations;
	private int m_optimizeInterval;
	private String m_modelFileName;
	private String m_pipeFileName;
	private int m_verbose;
	private double[] perTopicWordCountSum;
	List<TreeSet<IDSorter>> m_topic2WordProb;
	
	private ParallelTopicModel m_model;


	public MalletLda(ConfigurationParams iParams) throws ConfigurationException {
		m_numOfTopics = iParams.getInt("num-of-topics");
		m_alpha = iParams.getDouble("alpha");
		m_beta = iParams.getDouble("beta");
		m_numOfThreads = iParams.getInt("num-of-threads");
		m_numOfIterations = iParams.getInt("num-of-iterations");
		m_optimizeInterval = iParams.getInt("optimize-interval");
		m_modelFileName = iParams.get("model-file");
		m_pipeFileName = iParams.get("pipe-file");
		m_verbose = iParams.getInt("verbose");
		m_topic2WordProb = null;
	}
	
	public MalletLda(ConfigurationParams iParams, int numOfTopics) throws ConfigurationException {
		m_numOfTopics = numOfTopics;
		m_alpha = iParams.getDouble("alpha");
		m_beta = iParams.getDouble("beta");
		m_numOfThreads = iParams.getInt("num-of-threads");
		m_numOfIterations = iParams.getInt("num-of-iterations");
		m_optimizeInterval = iParams.getInt("optimize-interval");
		m_modelFileName = iParams.get("model-file");
		m_pipeFileName = iParams.get("pipe-file");
		m_verbose = iParams.getInt("verbose");
		m_topic2WordProb = null;
	}

	public MalletLda(ConfigurationParams iParams, String modelFilename) throws ConfigurationException {
		m_modelFileName = modelFilename;
		m_numOfTopics = iParams.getInt("num-of-topics");
		m_alpha = iParams.getDouble("alpha");
		m_beta = iParams.getDouble("beta");
		m_numOfThreads = iParams.getInt("num-of-threads");
		m_numOfIterations = iParams.getInt("num-of-iterations");
		m_optimizeInterval = iParams.getInt("optimize-interval");		
		m_verbose = iParams.getInt("verbose");
		m_topic2WordProb = null;
		
		m_pipeFileName = iParams.get("pipe-file");
		
		System.out.println(iParams.get("pipe-file"));
	}	
	/**
	 * Returns for each topic the word probability - p(w|topic)
	 * @return
	 */
	public List<TreeSet<IDSorter>> getTopic2WordProb() {
		return m_topic2WordProb;
	}
	
	public void train(File corpusFile) throws IOException {
		System.out.println(CharSequence2TokenSequence.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		
		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
		// Pipes: tokenize and map to features
//		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\S+")) );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("[^\t]+")) );
		pipeList.add( new TokenSequence2FeatureSequence() );
		
		InstanceList instances = new InstanceList (new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(corpusFile), "UTF-8");
		
		// determines how to parse the documents file into <name, label, data> tuples
		instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^([^\t]+)[\\s]*(\\S*)[\\s]*(.*)$"),
				3, 2, 1)); // <data, label, name> fields

		// Create a model. Note that the alpha parameter is passed as the sum over topics, while
		//  the beta is the parameter for a single dimension of the Dirichlet prior.

		ParallelTopicModel model = new ParallelTopicModel(m_numOfTopics, m_numOfTopics*m_alpha, m_beta);
		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and combine
		//  statistics after every iteration.
		model.setNumThreads(m_numOfThreads);

		// Run the model 
		model.setNumIterations(m_numOfIterations);
		model.setOptimizeInterval(m_optimizeInterval);
		model.setBurninPeriod(m_numOfIterations/2);
		model.estimate();

		if(m_verbose>0) {

			Object[][] topWordsPerTopic = model.getTopWords(10);
			for(int i = 0; i < model.getNumTopics(); i++) {
				Object[] topWords = topWordsPerTopic[i];
				StringBuilder sb = new StringBuilder();
				sb.append("topic "+i+":\t");
				for(int j = 0; j < topWords.length;++j) {

					String word = (String) topWords[j];
					sb.append(word+ " ");
				}
		//		EL.info(sb.toString());
			}
		}

		// Save the model
		model.write(new File(m_modelFileName));
		//Save the pipe
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(m_pipeFileName));
		oos.writeObject(instances.getPipe());
		oos.close();
	}

	/**
	 * Takes a trained model and writes into a file <code>P(term|topic)</code> for any topic <code>topic</code> 
	 * and term <code>term</code> such that <code>term</code> is assigned to <code>topic</code> at least once.  
	 * @param model - a trained model
	 * @param outFile - file to write <code>>P(term|topic)</code>
	 * @throws Exception
	 */
	public void calcTermsProbGivenTopic(File outFile) throws Exception {

		ParallelTopicModel model = getModel();
		Alphabet dict = model.getAlphabet();
		PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));
		
		//calculate the norm constant for each topic
		double[] perTopicCountSum = getPerTopicCountSum();

	//	writer.println("arg\ttopic\tprob");
		writer.println("term\ttopic\tprob");
		ArrayList<TreeSet<IDSorter>> topic2WordCount = model.getSortedWords();
		for(int i = 0; i < topic2WordCount.size(); i++) {
			TreeSet<IDSorter> wordCount = topic2WordCount.get(i);
			for(IDSorter wordId: wordCount) {
				String word = (String) dict.lookupObject(wordId.getID());
				double termProbGivenTopic = (wordId.getWeight()+model.beta) /  
				(perTopicCountSum[i]+dict.size()*model.beta);
				writer.println(word+"\t"+i+"\t"+termProbGivenTopic);
			}
		}
		writer.close();
	}
	
	/**
	 * Takes a trained model and writes into a file p(topic|doc,term) for every document 'doc' and term 'term'
	 * Such that p(topic|doc,term) is greater than minProb and there are less than maxRank topics k for which p(k|doc,term) > p(topic|doc,term)
	 * @param outFile
	 * @param minProb
	 * @param maxRank
	 * @throws Exception 
	 */
	public void calcTopicProbGivenDocAndTerm(File outFile, double minProb, int maxRank, Map<String,Set<String>> doc2TermsMap) throws Exception {
						
		List<TreeSet<IDSorter>> topic2WordProb = calcTermProbGivenTopic();	// p(term|topic)
		
		ArrayList<TopicAssignment> data = m_model.getData();	// doc -> p(topic|doc)
		
		PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));
		writer.write(TopicProbGivenDocAndTermRecord.getHeaderLines());				
		
		for(int doc = 0; doc < data.size() ; ++doc) { // going over all documents (u)
			
			String docName = (String) data.get(doc).instance.getName();			
			Set<String> docTerms = doc2TermsMap.get(docName);
			
			docName = MalletDocUtil.fromMalletToken(docName);	// remove brackets [doc]
			double[] documentTopicDist = m_model.getTopicProbabilities(doc);	// p(topic|doc)
			
			Map<String, double[]> topicProbGivenTermMap = new HashMap<String, double[]>();	// doc, term -> p(topic|doc,term)
			Map<String, List<TopicIdProbPair>> sparseTopicProbGivenTermMap = new HashMap<String, List<TopicIdProbPair>>();	// doc, term -> p(topic|doc,term)

			
			for (int topic=0; topic<topic2WordProb.size(); topic++) {	// going over topics (t)
				
				TreeSet<IDSorter> wordProbSet = topic2WordProb.get(topic);
				
				for (IDSorter wordProb : wordProbSet) {	// going over terms
					int id = wordProb.getID();
					String word = m_model.getAlphabet().lookupObject(id).toString();	// term
					
					if (docTerms.contains(word)) {

						word = MalletDocUtil.fromMalletToken(word);
						double prob = wordProb.getWeight();									// p(term|topic)

						double[] topicProbGivenTerm = topicProbGivenTermMap.get(word);
						if (topicProbGivenTerm == null) {
							topicProbGivenTerm = new double[topic2WordProb.size()];
						}

						topicProbGivenTerm[topic] = prob * documentTopicDist[topic];		// p(term|topic) * p(topic|doc)

						topicProbGivenTermMap.put(word, topicProbGivenTerm);		
					}
					
				}
				
			}
			
			int docLength = ((FeatureSequence) data.get(doc).instance.getData()).getLength();
			
			
			for (Map.Entry<String, double[]> entry : topicProbGivenTermMap.entrySet()) {
				String term = entry.getKey();
				double[] topicDist = entry.getValue();
				
				double termProbGivenDocument = 0f;	// p(term|doc)
				for (int i=0; i<topicDist.length; i++) {
					termProbGivenDocument += topicDist[i];
				}
								
				for (int i=0; i<topicDist.length; i++) {	
					topicDist[i] = topicDist[i] / termProbGivenDocument;	// p(topic|term) = p(term|topic) * p(topic|doc) / p(term|doc)					
				}				
								
				SparseTopicRep sparseDist = ModelUtils.convertTopicDist2Sparse(m_model, topicDist,docLength);
				List<TopicIdProbPair> topicList = sparseDist.getSortedTopicIdProbPairs();
				sparseTopicProbGivenTermMap.put(term, topicList);
				
			}
			
			// dump to file
			
			for (Map.Entry<String, List<TopicIdProbPair>> entry : sparseTopicProbGivenTermMap.entrySet()) {
				String term = entry.getKey();
				List<TopicIdProbPair> topicDist = entry.getValue();
				for (int i=0; i<topicDist.size(); i++) {
					if ((i >= maxRank) || ((i>0) && (topicDist.get(i).getProb() < minProb))) {
						break;
					}
					TopicProbGivenDocAndTermRecord record = new TopicProbGivenDocAndTermRecord(docName, term, topicDist.get(i).getTopicId(), topicDist.get(i).getProb());
					writer.println(record.toLine());
				}
			}
			
			if(doc % 100 == 0) {
				System.out.println("Finished document: " + doc);
			}						
		}
		
		writer.close();			
	}
	
	/**
	 * Read p(term|topic) file into memory
	 * @param inFile
	 * @param featureDesc2IdMap - convert string feature representation to id
	 * @return
	 * @throws Exception
	 */
	public static TIntObjectHashMap<TShortFloatHashMap> uploadTermProbGivenTopicMap(File inFile, TObjectIntHashMap<String> featureDesc2IdMap) throws Exception {

		TIntObjectHashMap<TShortFloatHashMap> featureId2TopicId2ProbMap = new TIntObjectHashMap<TShortFloatHashMap>();
		
		BufferedReader featureProbGivenTopicReader = new BufferedReader(new FileReader(inFile));
		featureProbGivenTopicReader.readLine(); //skip title
		String line;
		while((line=featureProbGivenTopicReader.readLine())!=null){

			String[] tokens = line.split("\t");
//			String fixedFeature = tokens[0].replace("<", " ");
			String fixedFeature = MalletDocUtil.fromMalletToken(tokens[0]);
			short topic = Short.parseShort(tokens[1]);
			float prob = Float.parseFloat(tokens[2]);
			int featureId = featureDesc2IdMap.get(fixedFeature);

			TShortFloatHashMap topicId2ProbMap = featureId2TopicId2ProbMap.get(featureId);
			if(topicId2ProbMap==null) {
				topicId2ProbMap = new TShortFloatHashMap();
				featureId2TopicId2ProbMap.put(featureId, topicId2ProbMap);
			}
			topicId2ProbMap.put(topic, prob);
		}
		featureDesc2IdMap.clear();
		featureProbGivenTopicReader.close();
		return featureId2TopicId2ProbMap;
	}


	/**
	 * Read p(topic|doc,term) file into memory
	 * @param inFile
	 * @param featureDesc2IdMap - convert string feature representation to id
	 * @return
	 * @throws Exception
	 */
	public static Map<DocTermTuple, List<TopicProbRankTuple>> uploadTopicProbGivenDocAndTermMap(File inFile, TObjectIntHashMap<String> featureDesc2IdMap, TObjectIntHashMap<String> elementDesc2IdMap, int maxTopics) throws Exception {

		Map<DocTermTuple, List<TopicProbRankTuple>> docAndTerm2TopicProbMap = new HashMap<DocTermTuple, List<TopicProbRankTuple>>();
		
		BufferedReader topicProbGivenDocAndTermReader = new BufferedReader(new FileReader(inFile));
		topicProbGivenDocAndTermReader.readLine(); //skip title
		topicProbGivenDocAndTermReader.readLine(); //skip title
		
		String line;
		
		while((line=topicProbGivenDocAndTermReader.readLine())!=null){
			
			TopicProbGivenDocAndTermRecord record = new TopicProbGivenDocAndTermRecord(line);
			
			TopicProbRankTuple topicProbRankTuple = new TopicProbRankTuple();
			DocTermTuple docTermTuple = new DocTermTuple();
			
			topicProbRankTuple.topic = record.topic;
			topicProbRankTuple.probability = record.probability;
			topicProbRankTuple.rank = 0;
			
			docTermTuple.term = featureDesc2IdMap.get(record.term);
			docTermTuple.doc = elementDesc2IdMap.get(record.docName);
			
			List<TopicProbRankTuple> topicList = docAndTerm2TopicProbMap.get(docTermTuple);
			if (topicList == null) {
				topicList = new LinkedList<TopicProbRankTuple>();
				docAndTerm2TopicProbMap.put(docTermTuple, topicList);
			}
			
			if ((maxTopics < 0) || (topicList.size() < maxTopics)) {
				topicList.add(topicProbRankTuple);	
			}
		}
		
		topicProbGivenDocAndTermReader.close();
		return docAndTerm2TopicProbMap;
	}

	/**
	 * Takes a trained model and returns <code>P(term|topic)</code> for any topic <code>topic</code> 
	 * and term <code>term</code> such that <code>term</code> is assigned to <code>topic</code> at least once.  
	 * @throws Exception
	 */
	public List<TreeSet<IDSorter>> calcTermProbGivenTopic() throws Exception {
		
		ParallelTopicModel model = getModel();
		Alphabet dict = model.getAlphabet();
		
		// in addition to saving into file we save the results in memory
		m_topic2WordProb = new ArrayList<TreeSet<IDSorter>>();

		//calculate the norm constant for each topic
		double[] perTopicCountSum = getPerTopicCountSum();

		ArrayList<TreeSet<IDSorter>> topic2WordCount = model.getSortedWords();
		for(int i = 0; i < topic2WordCount.size(); i++) {
			TreeSet<IDSorter> wordCount = topic2WordCount.get(i);
			TreeSet<IDSorter> wordProb = new TreeSet<IDSorter>();
			for(IDSorter wordId: wordCount) {
				double termProbGivenTopic = (wordId.getWeight()+model.beta) /  
				(perTopicCountSum[i]+dict.size()*model.beta);
				wordProb.add(new IDSorter(wordId.getID(), termProbGivenTopic));
			}
			m_topic2WordProb.add(wordProb);
		}
		
		return m_topic2WordProb;
	}
	
	

	

	/**
	 * 
	 * @param model
	 * @return
	 * @throws Exception
	 */
	public TIntObjectHashMap<TIntDoubleHashMap> calcTermWeightGivenTopic() throws Exception {

		ParallelTopicModel model = getModel();
		TIntObjectHashMap<TIntDoubleHashMap> result = new TIntObjectHashMap<TIntDoubleHashMap>();

		ArrayList<TreeSet<IDSorter>> topic2WordCount = model.getSortedWords();
		for(int i = 0; i < topic2WordCount.size(); i++) {
			TreeSet<IDSorter> wordCount = topic2WordCount.get(i);
			TIntDoubleHashMap term2ProbMap = new TIntDoubleHashMap();
			result.put(i, term2ProbMap);
			for(IDSorter wordId: wordCount) {
				term2ProbMap.put(wordId.getID(), wordId.getWeight());
			}
		}
		return result;
	}

	public double[] getPerTopicCountSum() throws Exception {

		double[] result = null;

		if(perTopicWordCountSum!=null)
			result = perTopicWordCountSum;
		else {
			ParallelTopicModel model = getModel();
			result = new double[model.getNumTopics()];
			ArrayList<TreeSet<IDSorter>> topic2WordCount = model.getSortedWords();

			for(int i = 0; i < topic2WordCount.size(); i++) {

				TreeSet<IDSorter> wordCount = topic2WordCount.get(i);
				//sum the counts of all words for this topic
				for(IDSorter wordId: wordCount) 
					result[i]+=wordId.getWeight();	
			}
			System.gc();
		}
		return result;
	}

	public static double[] getPerTopicCountSum(ParallelTopicModel model) throws Exception {

		double[] result = null;

		result = new double[model.getNumTopics()];
		ArrayList<TreeSet<IDSorter>> topic2WordCount = model.getSortedWords();

		for(int i = 0; i < topic2WordCount.size(); i++) {

			TreeSet<IDSorter> wordCount = topic2WordCount.get(i);
			//sum the counts of all words for this topic
			for(IDSorter wordId: wordCount) 
				result[i]+=wordId.getWeight();	
		}
		System.gc();

		return result;
	}

	public void printModelSpec(File outFile) throws Exception {

		ParallelTopicModel model = ParallelTopicModel.read(new File(m_modelFileName));
		PrintWriter writer = new PrintWriter(new FileOutputStream(outFile));

		double[] alpha = model.alpha;
		for(int i = 0; i < alpha.length;i++)
			writer.println("alpha " + i + ": " + alpha[i]);
		writer.println("beta: "  + model.beta);
		writer.println("burn in: "  + model.burninPeriod);
		writer.println("number of iterations: "  + model.numIterations);
		writer.println("number of topics: "+ model.numTopics);
		writer.println("number of types " + model.numTypes);
		writer.println("optimize interval " + model.optimizeInterval);
		writer.close();
	}

	public static void example(String[] args) throws Exception {

		// Begin by importing documents from text to feature sequences
		ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.add( new CharSequenceLowercase() );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File("C:/Users/User/workspace/mallet/stoplists/en.txt"), "UTF-8", false, false, false) );
		pipeList.add( new TokenSequence2FeatureSequence() );

		InstanceList instances = new InstanceList (new SerialPipes(pipeList));

		Reader fileReader = new InputStreamReader(new FileInputStream(new File(args[0])), "UTF-8");
		instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
				3, 2, 1)); // data, label, name fields

		// Create a model with 300 topics, alpha_t = 0.01, beta_w = 0.01
		//  Note that the first parameter is passed as the sum over topics, while
		//  the second is the parameter for a single dimension of the Dirichlet prior.
		int numTopics = 300;
		ParallelTopicModel model = new ParallelTopicModel(numTopics, 3.0, 0.01);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and combine
		//  statistics after every iteration.
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only, 
		//  for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(50);

		//model.setOptimizeInterval(20);
		model.estimate();

		File modelFile = new File("C:/Users/User/Research/Temp/model"); 
		model.write(modelFile);
		model = ParallelTopicModel.read(modelFile);

		// Show the words and topics in the first instance

		// The data alphabet maps word IDs to strings

		//Alphabet dataAlphabet = instances.getDataAlphabet();
		Alphabet dataAlphabet = model.getAlphabet();

		// Get an array of sorted sets of word ID/count pairs
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		// Create a new instance with high probability of topic 0
		StringBuilder topicZeroText = new StringBuilder();
		Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

		int rank = 0;
		while (iterator.hasNext() && rank < 5) {
			IDSorter idCountPair = iterator.next();
			topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
			rank++;
		}

		// Pipes: lowercase, tokenize, remove stopwords, map to features
		pipeList.clear();
		pipeList.add( new CharSequenceLowercase() );
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		pipeList.add( new TokenSequenceRemoveStopwords(new File("C:/Users/User/workspace/mallet/stoplists/en.txt"), "UTF-8", false, false, false) );
		pipeList.add( new TokenSequence2FeatureSequence() );
		Pipe serialPipe = new SerialPipes(pipeList);
		serialPipe.setDataAlphabet(model.getAlphabet());

		// Create a new instance named "test instance" with empty target and source fields.
		InstanceList testing = new InstanceList(instances.getPipe());
		testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

		TopicInferencer inferencer = model.getInferencer();
		double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 100, 10, 10);
		System.out.println("0\t" + testProbabilities[0]);
	}

	public ParallelTopicModel getModel() throws Exception {

		if(m_model==null)
			m_model = ParallelTopicModel.read(new File(m_modelFileName));
		return m_model;
	}

	public void cleanModel() {
		m_model = null;
		System.gc();
	}

	public String getPipeFileName() {
		return m_pipeFileName;
	}

	public TopicInferencer getInferencer() throws Exception {
		ParallelTopicModel model = getModel();
		return model.getInferencer();
	}
	
}

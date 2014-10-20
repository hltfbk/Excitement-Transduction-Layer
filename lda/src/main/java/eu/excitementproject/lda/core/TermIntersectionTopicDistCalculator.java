package eu.excitementproject.lda.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import cc.mallet.pipe.Pipe;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.lda.rep.SparseTopicRep;
import eu.excitementproject.lda.rep.TermProbPerTopic;
import eu.excitementproject.lda.rep.TopicIdProbPair;
import eu.excitementproject.lda.utils.MalletDocUtil;
import eu.excitementproject.lda.utils.Serializer;

/**
 * @author Jonathan Berant, Lili Kotlerman
 *
 */
public class TermIntersectionTopicDistCalculator implements TopicDistributionCalculator {

	private final static int m_iterations = 150;
	private final static int m_thinning = 10;
	private final static int m_burnIn = 50;
	
	private ParallelTopicModel m_model;
	private TopicInferencer m_inferencer;
	private Pipe m_pipe;
	private double[] m_alphas;
	private double m_alphaSum;
	private TermProbPerTopic m_termProbPerTopic;
	
	
	public TermIntersectionTopicDistCalculator(MalletLda mallet) throws Exception {
		
		m_inferencer = mallet.getInferencer();
		System.out.println("Inferencer loaded");
	
		Serializer<Pipe> pipeSerializer = new Serializer<Pipe>();
		m_pipe = pipeSerializer.load(mallet.getPipeFileName());
		System.out.println("Pipe loaded");
		
		ParallelTopicModel model = mallet.getModel();
		System.out.println("Model loaded");
		m_alphas = new double[model.getNumTopics()];
		//copying rather than pointing so that there is no risk of model staying in memory later
		for(int i = 0; i < m_alphas.length; ++i) {
			m_alphas[i] = model.alpha[i];
		}
		m_alphaSum = model.alphaSum;
		
	}
	
	public TermIntersectionTopicDistCalculator(ConfigurationParams iParams) throws Exception {
		
		m_model = ParallelTopicModel.read(iParams.getFile("model-file"));
		m_inferencer = m_model.getInferencer();
		Serializer<Pipe> pipeSerializer = new Serializer<Pipe>();
		m_pipe = pipeSerializer.load(iParams.get("pipe-file-name"));
		
		m_alphas = new double[m_model.getNumTopics()];
		//copying rather than pointing so that there is no risk of model staying in memory later
		for(int i = 0; i < m_alphas.length; ++i) {
			m_alphas[i] = m_model.alpha[i];
		}
		m_alphaSum = m_model.alphaSum;
		m_termProbPerTopic = new TermProbPerTopic(iParams);
		
	}
	
	public int getTopicNum() {
		return m_model.getNumTopics();
	}
	
	@Override
	public double[] getTopicPriorProbabilities(){
		double[] priorProb = new double[m_model.getNumTopics()];
		for (int i=0; i<priorProb.length; i++) {
			priorProb[i] = m_alphas[i] / m_alphaSum;
		}
		
		return priorProb;
	}
	
	public TermProbPerTopic getTermProbPerTopic() {
		return this.m_termProbPerTopic;
	}
	
	public Object[][] getTopWords(int wordsPerTopic) {
		return m_model.getTopWords(wordsPerTopic);		
	}
	
	@Override
	public double[] calculateTopicDistribution(String document) {
		 // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(m_pipe);
        testing.addThruPipe(new Instance(document, null, "", null));
        return m_inferencer.getSampledDistribution(testing.get(0), m_iterations, m_thinning, m_burnIn);
	}
	

	public SparseTopicRep calculateSparseTopicDistribution(String document) {
		
		List<TopicIdProbPair> topicIdProbPairs = new LinkedList<TopicIdProbPair>();
		double[] topicDist = calculateTopicDistribution(document);
		
		double[] probs = new double[topicDist.length];
		String[] words = MalletDocUtil.tokenize(document);
		
		for(short i = 0; i < probs.length;++i) 
			probs[i] = m_alphas[i] / (m_alphaSum + words.length);
		
		for(short i = 0; i < topicDist.length;++i) {
			if(topicDist[i] > probs[i] + 0.001)
				topicIdProbPairs.add(new TopicIdProbPair(i, (float)topicDist[i]));
		}
		return new SparseTopicRep(topicIdProbPairs, words.length);
	}
	
	public SparseTopicRep convertTopicDist2Sparse(double[] dist, int docSize) {
		
		List<TopicIdProbPair> topicIdProbPairs = new LinkedList<TopicIdProbPair>();
		double[] probs = new double[dist.length];
		
		for(short i = 0; i < probs.length;++i) 
			probs[i] = m_alphas[i] / (m_alphaSum + docSize);
		
		for(short i = 0; i < dist.length;++i) {
			if(dist[i] > probs[i] + 0.001)
				topicIdProbPairs.add(new TopicIdProbPair(i, (float)dist[i]));
		}
		return new SparseTopicRep(topicIdProbPairs, docSize);	
	}
	
	public double[] converteSparseDist2NonSparse(SparseTopicRep sparseDist) {
		
		double[] result = new double[m_alphas.length];
		
		for(short i = 0; i < result.length;++i)
			result[i] = m_alphas[i] / (m_alphaSum + sparseDist.getDocSize());
		
		for(TopicIdProbPair pair: sparseDist.getTopicIdProbPairs()) 
			result[pair.getTopicId()] = pair.getProb();
	
		return result;
	}
	
	public List<Map.Entry<Integer, Double>> getTopicDistPerTermMap(String term, double[] docTopicDist) {
		
		Map<Integer, Double> topicGivenTerm = new HashMap<Integer, Double>();
		
		double totalProbMass = 0;
		
		for (int i=0; i<docTopicDist.length; i++) {
			double termGivenTopic = m_termProbPerTopic.getProbForTermAndTopic(term, (short)i);
			topicGivenTerm.put(i, new Double(docTopicDist[i]*termGivenTopic));
			totalProbMass += docTopicDist[i]*termGivenTopic;
		}
		
		for (int i=0; i<docTopicDist.length; i++) {
			double weight = topicGivenTerm.get(i);
			topicGivenTerm.put(i, new Double(weight/totalProbMass));
			
		}
		
		return TermIntersectionTopicDistCalculator.sortMap(topicGivenTerm);
	}
	
	public double[] getTopicDistPerTerm(String term, double[] docTopicDist) {
		
		List<Map.Entry<Integer, Double>> distMap = getTopicDistPerTermMap(term, docTopicDist);
		double[] dist = new double[docTopicDist.length];
		Arrays.fill(dist, 0f);
		for (Map.Entry<Integer, Double> entry : distMap) {
			dist[entry.getKey()] = entry.getValue();
		}
		
		return dist;
	}
		
		/**
		 * todo: move to infrastructure?
		 * Sorts a maps entry list according to entry's value.
		 * @param map
		 * @return the sorted entry list
		 */
		public static <T1, T2 extends Comparable<T2>> 
		List<Map.Entry<T1, T2>> sortMap(Map<T1, T2> map) {		
			List<Map.Entry<T1, T2>> list = new LinkedList<Map.Entry<T1, T2>>(map.entrySet());
			
			Collections.sort(list, 
					new Comparator<Map.Entry<T1, T2>>() {
						public int compare(Map.Entry<T1, T2> o1, Map.Entry<T1, T2> o2) {
							return ((o2.getValue()).compareTo(o1.getValue()));
						}
					}
			);		
			return list;
		}
		
		public static double[] intersectDistributions(double[] dist1, double[] dist2) {
			double[] resultDist = new double[dist1.length];
			double norm = 0;
			for (int i=0; i<dist1.length; i++) {
				resultDist[i] = Math.min(dist1[i], dist2[i]);
				norm += resultDist[i];
			}
			
			for (int i=0; i<resultDist.length; i++) {
				resultDist[i] = resultDist[i]/norm;				
			}
			
			return resultDist;
		}
		
				
		@Override
		public SparseTopicRep trimTopicDist(SparseTopicRep dist, double topicMass, double preferenceRatio) {
			
			List<TopicIdProbPair> sortedTopics = dist.getSortedTopicIdProbPairs();
			List<TopicIdProbPair> trimmedTopics = new LinkedList<TopicIdProbPair>();
			
			double mass = 0f;
			double highestProb = sortedTopics.get(0).getProb();
			for (TopicIdProbPair pair : sortedTopics) {
				
				if ((pair.getProb() / highestProb) < preferenceRatio) {
					break;
				}
				
				mass += pair.getProb();
				
				if (pair.getProb() >= 0.5f/m_alphas.length) {
					trimmedTopics.add(pair);
				}
				
				if (mass > topicMass) {
					break;
				}
			}
			
			SparseTopicRep trimmedRep = new SparseTopicRep(trimmedTopics, dist.getDocSize());
			return trimmedRep;
			
		}
		
		@Override
		public double[] convertTopicDist2Preference(SparseTopicRep dist) {
			
			List<TopicIdProbPair> sortedTopics = dist.getSortedTopicIdProbPairs();
			double highestProb = sortedTopics.get(0).getProb();

			double[] topicPref = new double[m_alphas.length];
			for(short i = 0; i < topicPref.length;++i) {
				topicPref[i] = (m_alphas[i] / (m_alphaSum + dist.getDocSize()))/highestProb;
			}
			
			
			for (TopicIdProbPair pair : sortedTopics) {			
				double preference = pair.getProb() / highestProb;
				topicPref[pair.getTopicId()] = preference;									
			}
			
			return topicPref;
		}
		
}



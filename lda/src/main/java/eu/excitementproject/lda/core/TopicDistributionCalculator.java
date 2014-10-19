package eu.excitementproject.lda.core;

import java.util.List;
import java.util.Map;

import eu.excitementproject.lda.rep.SparseTopicRep;
import eu.excitementproject.lda.rep.TermProbPerTopic;


/**
 * @author Jonathan Berant, Lili Kotlerman
 *
 */
public interface TopicDistributionCalculator {

	public double[] calculateTopicDistribution(String document);
	public SparseTopicRep calculateSparseTopicDistribution(String document);
	public SparseTopicRep convertTopicDist2Sparse(double[] dist, int docSize);
	public double[] converteSparseDist2NonSparse(SparseTopicRep sparseDist);
	
	/**
	 * Return an array with an element per each topic
	 * Each element contains the words of the topic sorted according to probability
	 * @param wordsPerTopic
	 * @return
	 */
	public Object[][] getTopWords(int wordsPerTopic);
	
	public TermProbPerTopic getTermProbPerTopic();
	
	public List<Map.Entry<Integer, Double>> getTopicDistPerTermMap(String term, double[] docTopicDist);
	public double[] getTopicDistPerTerm(String term, double[] docTopicDist);
	
	/**
	 * Trims the distribution such that only top topics are left
	 * @param dist
	 * @param topicMass
	 * @return
	 */
	public SparseTopicRep trimTopicDist(SparseTopicRep dist, double topicMass, double preferenceRatio);
	
	/**
	 * Converts the topic dist to a vector that includes a 'preference' score per topic
	 * @param dist
	 * @return
	 */
	public double[] convertTopicDist2Preference(SparseTopicRep dist);
	
	/**
	 * Returns of vector of the prior topic probabilities (proportional to the alpha parameters).
	 * @return
	 */
	public double[] getTopicPriorProbabilities();
	
}

package eu.excitementproject.lda.rep;

/**
 * @author Jonathan Berant
 *
 */
public class TopicIdProbPair {

	private short m_topicId;
	private double m_prob;
	
	public TopicIdProbPair(short topicId, float prob) {
		m_topicId = topicId;
		m_prob = prob;
	}
	
	public String toString() {
		return ""+m_topicId+"::"+m_prob;
	}
	
	public short getTopicId() {
		return m_topicId;
	}
	
	public double getProb() {
		return m_prob;
	}
}

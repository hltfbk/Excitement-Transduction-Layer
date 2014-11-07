package eu.excitementproject.lda.rep;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import eu.excitementproject.lda.utils.PrimitiveArrayIterator;


/**
 * @author Jonathan Berant
 *
 */
public class SparseTopicRep {

	private List<TopicIdProbPair> m_topicIdProbList;
	private int m_docSize;

	public SparseTopicRep(List<TopicIdProbPair> topicIdProbList, int docSize) {	
		m_docSize=docSize;
		m_topicIdProbList = topicIdProbList;
	}
	
	public SparseTopicRep(PrimitiveArrayIterator<String> tokensItr, String exclusiveLast) {
		this.fromTokens(tokensItr, exclusiveLast);
	}
	
	/**
	 * loading a topic distribution from a tokenized line
	 * @param tokens
	 * @param from - index where topic description starts inclusive
	 * @param to  - index where topic description ends exclusive
	 */
	public static SparseTopicRep loadFromTokenizedLine(String[] tokens, int from, int to) {

		SparseTopicRep result = null;
		if(!tokens[from].equals("null")) {
			int docSize = Integer.parseInt(tokens[from]);
			List<TopicIdProbPair> topicIdProbList = new LinkedList<TopicIdProbPair>();
			for(int i = from+1; i < to; ++i) {
				String[] topicIdProbPair = tokens[i].split("::");
				topicIdProbList.add(new TopicIdProbPair(Short.parseShort(topicIdProbPair[0]), Float.parseFloat(topicIdProbPair[1])));
			}
			result = new SparseTopicRep(topicIdProbList, docSize);
		}
		return result;
	}
	
	public void fromTokens(PrimitiveArrayIterator<String> tokensItr, String exclusiveLast) {

		m_topicIdProbList = new LinkedList<TopicIdProbPair>();
		String token = tokensItr.getNext();
		if (token.equals("null")) {
			m_docSize = 0;			
		} else {
			m_docSize = Integer.parseInt(token);
		}
		while ((tokensItr.hasNext()) && !(token = tokensItr.getNext()).equals(exclusiveLast)) {
			String[] topicIdProbPair = token.split("::");
			m_topicIdProbList.add(new TopicIdProbPair(Short.parseShort(topicIdProbPair[0]), Float.parseFloat(topicIdProbPair[1])));
		}

	}

	public List<TopicIdProbPair> getTopicIdProbPairs() {
		return m_topicIdProbList;
	}
	
	public List<TopicIdProbPair> getSortedTopicIdProbPairs() {
		List<TopicIdProbPair> sortedList = new LinkedList<TopicIdProbPair>(m_topicIdProbList);
		Collections.sort(sortedList, new Comparator<TopicIdProbPair>() {
			public int compare(TopicIdProbPair o1, TopicIdProbPair o2) {
				return ((new Double(o2.getProb()).compareTo(o1.getProb())));
			}
		});
		
		return sortedList;
		
		
	}

	public int getDocSize() {
		return m_docSize;
	}

	public String toString() {

		StringBuilder sb = new StringBuilder(""+m_docSize);
		for(TopicIdProbPair topicIdProb: m_topicIdProbList)
			sb.append("\t"+topicIdProb);
		return sb.toString();
	}

}

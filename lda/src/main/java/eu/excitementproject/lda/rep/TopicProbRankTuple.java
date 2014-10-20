/**
 * 
 */
package eu.excitementproject.lda.rep;

/**
 * @author Oren Melamud
 *
 */
public class TopicProbRankTuple {
	
	public short topic;
	public double probability;
	public int rank;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(probability);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + rank;
		result = prime * result + topic;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopicProbRankTuple other = (TopicProbRankTuple) obj;
		if (Double.doubleToLongBits(probability) != Double
				.doubleToLongBits(other.probability))
			return false;
		if (rank != other.rank)
			return false;
		if (topic != other.topic)
			return false;
		return true;
	}
	
	

}

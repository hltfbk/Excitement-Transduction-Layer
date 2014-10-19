package eu.excitementproject.clustering.clustering.impl.util.rep;

import java.util.Comparator;

public class ClusterMember<T> {
	
	public ClusterMember(T member, Double score) {
		super();
		this.member = member;
		this.score = score;
	}

	T member;
	Double score;

	public static class ReverseOrderByScoreComparator<T> implements Comparator<ClusterMember<T>> {
		@Override
		public int compare(ClusterMember<T> o1, ClusterMember<T> o2) {
			return -1 * o1.score.compareTo(o2.score);
		}			
	}
}
	


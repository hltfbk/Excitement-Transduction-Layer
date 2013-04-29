package eu.excitementproject.tl.structures.search;

import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * (class) PerNodeScore
 	- is a tuple (=EntailmentUnit=, =double= score)
   (e.g. so [(node a, score x), (node b, score y) ...] ) 

 * @author Kathrin Eichler
 *
 */
public class PerNodeScore {
	
	protected EntailmentUnit node;
	protected double score;
	
	public EntailmentUnit getNode() {
		return node;
	}
	public void setNode(EntailmentUnit node) {
		this.node = node;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
}

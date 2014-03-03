package eu.excitementproject.tl.structures.search;

import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;

/**
 * (class) PerNodeScore
 	- is a tuple (=EquivalenceClass=, =double= score)
   (e.g. so [(node a, score x), (node b, score y) ...] ) 

 * @author Kathrin Eichler
 *
 */
public class PerNodeScore {
	
	protected EquivalenceClass node;
	protected double score;
	
	public EquivalenceClass getNode() {
		return node;
	}
	public void setNode(EquivalenceClass node) {
		this.node = node;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
}

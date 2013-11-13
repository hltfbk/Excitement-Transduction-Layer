package eu.excitementproject.tl.structures.search;

import java.util.List;

import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;


/**
 * (class) NodeMatch
 - has one =EntailmentUnitMention=  (this mention matched the following nodes...)
 - has a list of =PerNodeScore= (keeps matched nodes with score)
       
 * @author Kathrin Eichler
 *
 */
public class NodeMatch {
	
	protected EntailmentUnitMention mention;
	protected List<PerNodeScore> scores;
	
	public EntailmentUnitMention getMention() {
		return mention;
	}
	public void setMention(EntailmentUnitMention mention) {
		this.mention = mention;
	}
	public List<PerNodeScore> getScores() {
		return scores;
	}
	public void setScores(List<PerNodeScore> scores) {
		this.scores = scores;
	}
}

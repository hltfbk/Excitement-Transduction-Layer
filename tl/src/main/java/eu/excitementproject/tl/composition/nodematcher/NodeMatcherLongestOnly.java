/**
 * 
 */
package eu.excitementproject.tl.composition.nodematcher;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

/**
 * This NodeMatcher compares an input fragment graph to an input entailment graph and tries to find
 * the longest match: It starts with the complete statement, on which the fragment graph was built, 
 * and tries to find a matching node in the entailment graph. If a match is found, it returns this node, 
 * if not, it tries to match the strings on the next level of the fragment graph, i.e. the strings
 * where one modifier is missing. Again, if a match is found, the node (or nodes) is (are) returned, 
 * if not, it keeps going like this until it reaches the base statement (all modifiers removed). 
 * If the longest match is found, then its entailed nodes can be added to the set of the matched nodes. 
 * 
 * @author Kathrin Eichler & Aleksandra Gabryszak
 *
 */
public class NodeMatcherLongestOnly extends AbstractNodeMatcher {
	
	static Logger logger = Logger.getLogger(NodeMatcherLongestOnly.class.getName());
	
	private EntailmentGraphCollapsed entailmentGraph;
	private boolean bestNodeOnly;
	private double entailedNodeScore; //only relevant if passed as parameter in constructor
	private boolean useGraphEdgeConfidence; //is set to true if the constructor with parameter entailedNodeScore is used
	
	/**
	 * Instantiates NodeMatcherLongestOnly.
	 * If the entailed nodes are to be included in matchings, then the score value for an entailed node
	 * is based on the score of the matched entailing node and the confidence of the edge: matched entailing node --> entailed node
	 * 
	 * @param graph
	 * @param bestNodeOnly - set to true, if entailed nodes are to be included in matchings
	 */
	public NodeMatcherLongestOnly(EntailmentGraphCollapsed graph, boolean bestNodeOnly) {
		this.entailmentGraph = graph;
		this.bestNodeOnly = bestNodeOnly;
		this.useGraphEdgeConfidence = true;
	}
	
	/**
	 * Instantiates NodeMatcherLongestOnly.
	 * If the entailed nodes are to be included in matchings, then the passed value of the parameter entailedNodeScore is used
	 * to score the entailed nodes, instead of computing the score based on the edge confidences in the collapsed graph
	 * 
	 * @param graph - collapsed graph
	 * @param bestNodeOnly - set to true, if entailed nodes are to be included in matchings
	 * @param entailedNodeScore - score for the entailed node
	 */
	public NodeMatcherLongestOnly(EntailmentGraphCollapsed graph, boolean bestNodeOnly, double entailedNodeScore) {
		this.entailmentGraph = graph;
		this.bestNodeOnly = bestNodeOnly;
		this.useGraphEdgeConfidence = false;
		this.entailedNodeScore = entailedNodeScore;
	}
	
	@Override
	public Set<NodeMatch> findMatchingNodesInGraph(FragmentGraph fragmentGraph) throws NodeMatcherException {
		
		//create empty node match set
		Set<NodeMatch> nodeMatches = new HashSet<NodeMatch>();
		
		//read fragment graph nodes, starting with the complete statement; break if match is found
		for (int i = fragmentGraph.getMaxLevel(); i >= 0; i--) {
			Set<EntailmentUnitMention> mentions = fragmentGraph.getNodes(i);
			for (EntailmentUnitMention mention : mentions) {
				NodeMatch match = findMatchingNodesForMention(mention, entailmentGraph);
				if (null != match) nodeMatches.add(match);
			}
			if (nodeMatches.size() > 0) break;
		}
				
		return nodeMatches;
	}

	/**
	 * Finds nodes in an entailment graph that match a mention.
	 * 
	 * Simple implementation: Go through all the nodes in the entailment graph and compare
	 * mention to be found to all mentions associated to the nodes. 
	 * 
	 * @param mention
	 * @param entailmentGraph
	 * @return
	 */
	private NodeMatch findMatchingNodesForMention(
			EntailmentUnitMention mentionToBeFound, EntailmentGraphCollapsed entailmentGraph) {
		//read entailment graph nodes
		Set<EquivalenceClass> vertexSet = entailmentGraph.vertexSet();
		List<PerNodeScore> scores = new ArrayList<PerNodeScore>();
		for (EquivalenceClass ec : vertexSet) { //for each node in the entailment graph
			double score = getNodeScore(mentionToBeFound, ec);	
			if (score > 0) { //add non-zero scores to list
				logger.debug("score for " + mentionToBeFound + "and " + ec.getLabel() + ": " + score);				
				PerNodeScore perNodeScore = new PerNodeScore();
				perNodeScore.setNode(ec);
				perNodeScore.setScore(score);
				scores.add(perNodeScore);
				if(!bestNodeOnly){
					Set<EquivalenceClass> entailedNodes = entailmentGraph.getEntailedNodes(ec);
					for(EquivalenceClass entailedNode : entailedNodes){
						double entailedNodeScore;
						if(useGraphEdgeConfidence){
							double edgeConfidence = entailmentGraph.getEdge(ec, entailedNode).getConfidence();
							entailedNodeScore = score * edgeConfidence;
						} else {
							entailedNodeScore = this.entailedNodeScore;
						}
						logger.debug("score for " + mentionToBeFound + "and " + entailedNode.getLabel() + ": " + entailedNodeScore);		
						perNodeScore = new PerNodeScore();
						perNodeScore.setNode(entailedNode);
						perNodeScore.setScore(entailedNodeScore);
						scores.add(perNodeScore);
					}
				}
			}
		}
		if (scores.size() > 0) { //at least one match found
			NodeMatch nodeMatch = new NodeMatch();
			nodeMatch.setMention(mentionToBeFound);
			nodeMatch.setScores(scores);
			return nodeMatch;
		}
		return null;
	}
	
	/**
	 * Compute a score expressing how well the mention matches the entailment graph node.
	 * 
	 * Simple implementation: If mention to be found exactly matches one of the mentions 
	 * the text or lemma associated to the node return 1, if not return 0.
	 *  
	 * @param mentionToBeFound
	 * @param eu
	 * @return
	 */
	private double getNodeScore(EntailmentUnitMention mentionToBeFound,
			EquivalenceClass ec) {		
		Set<EntailmentUnit> eus = ec.getEntailmentUnits();
		String mentionToBeFoundText = mentionToBeFound.getText().replaceAll("\\s+", " ").trim().toLowerCase();	
		for (EntailmentUnit eu : eus) { //compare mentionToBeFound with text and lemma of the entailment unit
			String unitText = eu.getTextWithoutDoubleSpaces().trim().toLowerCase();
			if (unitText.equals(mentionToBeFoundText)) {
				return 1;
			}
			if (null != eu.getLemmatizedText() && !eu.getLemmatizedText().isEmpty()) { 
				String unitTextLemmatized = eu.getLemmatizedText().trim().toLowerCase();
				if (unitTextLemmatized.equals(mentionToBeFoundText)) {
					return 1;
				}
			}
			
			/*Set<EntailmentUnitMention> mentions = eu.getMentions();
			for (EntailmentUnitMention mention : mentions) { //for each mention associated to this node	
				String mentionText = mention.getText().replaceAll("\\s+", " ").trim();
				String mentionToBeFoundText = mentionToBeFound.getText().replaceAll("\\s+", " ").trim();	
				if (mentionText.toLowerCase().trim().equals(mentionToBeFoundText.toLowerCase().trim())) { //compare to mention to be found
			//	if (mentionText.equals(mentionToBeFoundText)) { //compare to mention to be found
						return 1;
				}
			}*/
		}
		return 0;
	}
}

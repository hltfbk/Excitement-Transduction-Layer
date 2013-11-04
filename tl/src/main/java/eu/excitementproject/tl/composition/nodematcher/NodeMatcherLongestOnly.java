/**
 * 
 */
package eu.excitementproject.tl.composition.nodematcher;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * 
 * @author Kathrin Eichler
 *
 */
public class NodeMatcherLongestOnly extends AbstractNodeMatcher {
	
	private EntailmentGraphCollapsed entailmentGraph;
	
	public NodeMatcherLongestOnly(EntailmentGraphCollapsed graph) {
		entailmentGraph = graph;
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
				PerNodeScore perNodeScore = new PerNodeScore();
				perNodeScore.setNode(ec);
				perNodeScore.setScore(score);
				scores.add(perNodeScore);
			}
		}
		if (scores.size() > 0) { //a list one match found
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
	 * Simple implementation: If mention exactly matches one of the mentions associated to the node, return 1, if not return 0.
	 *  
	 * @param mentionToBeFound
	 * @param eu
	 * @return
	 */
	private double getNodeScore(EntailmentUnitMention mentionToBeFound,
			EquivalenceClass ec) {		
		Set<EntailmentUnit> eus = ec.getEntailmentUnits();
		for (EntailmentUnit eu : eus) {
			Set<EntailmentUnitMention> mentions = eu.getMentions();
			for (EntailmentUnitMention mention : mentions) { //for each mention associated to this node	
				String mentionText = mention.getText().replaceAll("\\s+", " ").trim();
				String mentionToBeFoundText = mentionToBeFound.getText().replaceAll("\\s+", " ").trim();	
				if (mentionText.equals(mentionToBeFoundText)) { //compare to mention to be found
					return 1;
				}
			}
		}
		return 0;
	}

}

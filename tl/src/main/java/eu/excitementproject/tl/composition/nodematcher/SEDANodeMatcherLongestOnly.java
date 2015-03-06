/**
 * 
 */
package eu.excitementproject.tl.composition.nodematcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalResourceException;
import eu.excitementproject.eop.core.component.lexicalknowledge.derivbase.DerivBaseResource;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetRelation;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetWrapper;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.experiments.OMQ.SEDAUtils;
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
 * Use this NodeMatcher for matching fragments containing up to two tokens!
 * 
 * @author Kathrin Eichler & Aleksandra Gabryszak
 *
 */
public class SEDANodeMatcherLongestOnly extends AbstractNodeMatcher {
	
	static Logger logger = Logger.getLogger(SEDANodeMatcherLongestOnly.class.getName());
	
	private EntailmentGraphCollapsed entailmentGraph;
	private boolean bestNodeOnly;
	private DerivBaseResource derivBaseResource;
	private GermaNetWrapper germaNetWrapper;
	private List<GermaNetRelation> germaNetRelations; 
	private GermanWordSplitter splitter; 
	private boolean mapNegation;
	private double entailedNodeScore; //only relevant if passed as parameter in constructor
	private boolean useGraphEdgeConfidence; //is set to true if the constructor with parameter entailedNodeScore is used
	
	/**
	 * 
	 * @param graph
	 * @param bestNodeOnly
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param mapNegation
	 */
	public SEDANodeMatcherLongestOnly(EntailmentGraphCollapsed graph, boolean bestNodeOnly, 
			DerivBaseResource derivBaseResource, GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations, 
			GermanWordSplitter splitter, boolean mapNegation) {
	
		this.derivBaseResource = derivBaseResource;
		this.germaNetWrapper = germaNetWrapper;
		this.germaNetRelations = germaNetRelations;
		this.splitter = splitter;
		this.mapNegation = mapNegation;
		this.entailmentGraph = graph;
		this.bestNodeOnly = bestNodeOnly;
		this.useGraphEdgeConfidence = true;
	}
	
	/**
	 * 
	 * @param graph
	 * @param bestNodeOnly
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param mapNegation
	 * @param entailedNodeScore
	 */
	public SEDANodeMatcherLongestOnly(EntailmentGraphCollapsed graph, boolean bestNodeOnly, 
			DerivBaseResource derivBaseResource, GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations, 
			GermanWordSplitter splitter, boolean mapNegation, double entailedNodeScore) {
		
		this.derivBaseResource = derivBaseResource;
		this.germaNetWrapper = germaNetWrapper;
		this.germaNetRelations = germaNetRelations;
		this.splitter = splitter;
		this.mapNegation = mapNegation;
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
		
		//compute to the mentionToBeFound related  text set using SEDA
		Set<String> relatedTextByLemmaDBSet = new HashSet<String>();
		Set<String> relatedTextByLemmaDBGNSet = new HashSet<String>();
		try{
			boolean addLemmatizedText = true;
			EntailmentUnit tmpEU = new EntailmentUnit(mentionToBeFound, mentionToBeFound.getTextWithoutDoubleSpaces(), addLemmatizedText);
			relatedTextByLemmaDBSet = SEDAUtils.getRelatedText(tmpEU.getText(), tmpEU.getLemmatizedText(), derivBaseResource, null, null, splitter, mapNegation);
			if(null != germaNetWrapper && null != germaNetRelations && !germaNetRelations.isEmpty()){
				relatedTextByLemmaDBGNSet = SEDAUtils.getRelatedText(tmpEU.getText(), tmpEU.getLemmatizedText(), derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			}
			
		} catch (LexicalResourceException e) {
			e.printStackTrace();
		}
		
		Set<EquivalenceClass> matchedNodes = new HashSet<EquivalenceClass>(); //stores matched nodes to avoid scoring nodes more than ones
		for (EquivalenceClass ec : vertexSet) { //for each node in the entailment graph
			if(!matchedNodes.contains(ec)) {
				double score = getNodeScore(mentionToBeFound, ec, relatedTextByLemmaDBSet, relatedTextByLemmaDBGNSet);
				if (score > 0) { //add non-zero scores to list
					logger.debug("score for " + mentionToBeFound + "and " + ec.getLabel() + ": " + score);				
					PerNodeScore perNodeScore = new PerNodeScore();
					perNodeScore.setNode(ec);
					perNodeScore.setScore(score);
					scores.add(perNodeScore);
					matchedNodes.add(ec);
					if(!bestNodeOnly){
						Set<EquivalenceClass> entailedNodes = entailmentGraph.getEntailedNodes(ec);
						for(EquivalenceClass entailedNode : entailedNodes){
							if(!matchedNodes.contains(entailedNode)) {
								double entailedNodeScore;
								if(useGraphEdgeConfidence){
									double edgeConfidence = entailmentGraph.getEdge(ec, entailedNode).getConfidence();
									entailedNodeScore = score * edgeConfidence;
								} else {
									entailedNodeScore = this.entailedNodeScore;
								}
								logger.debug("score for " + mentionToBeFound + "and " + entailedNode.getLabel() + ": " + entailedNodeScore);		
								if(entailedNodeScore > 0) {
									logger.debug("score for " + mentionToBeFound + "and " + entailedNode.getLabel() + ": " + entailedNodeScore);		
									perNodeScore = new PerNodeScore();
									perNodeScore.setNode(entailedNode);
									perNodeScore.setScore(entailedNodeScore);
									scores.add(perNodeScore);
								}
							}
						}
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
	 * If mention to be found exactly matches one of the mentions or
	 * their text are related by using resources like lemmatizer, DerivBase or GermaNet
	 * - return 1.0 (if mentions related by exact, lemma or DerivBase matching) 
	 * - return 0.9 (if at least one token of the mention must be matched by GermaNet)
	 * - otherwise return 0.
	 * 
	 * @param mentionToBeFound
	 * @param ec
	 * @param relatedTextByLemmaDBSet
	 * @param relatedTextByLemmaDBGNSet
	 * @return
	 */
	private double getNodeScore(EntailmentUnitMention mentionToBeFound,
			EquivalenceClass ec, Set<String> relatedTextByLemmaDBSet, Set<String> relatedTextByLemmaDBGNSet) {		
		
		Set<EntailmentUnit> eus = ec.getEntailmentUnits();
		String mentionToBeFoundText = mentionToBeFound.getText().replaceAll("\\s+", " ").trim().toLowerCase();	
		double max = 0.0;
		for (EntailmentUnit eu : eus) { //compare mentionToBeFound with text and lemma of the entailment unit
			
			String unitText = eu.getTextWithoutDoubleSpaces().trim().toLowerCase();
			if (unitText.equals(mentionToBeFoundText)) {
				return 1.0;
			}
			
			if (null != eu.getLemmatizedText() && !eu.getLemmatizedText().isEmpty()) { 
				String unitTextLemmatized = eu.getLemmatizedText().trim().toLowerCase();
				if (unitTextLemmatized.equals(mentionToBeFoundText)) {
					return 1.0;
				}
				
				List<String> unitLemmas = Arrays.asList(unitTextLemmatized.split("\\s+"));
				List<String> mentionToBeFoundLemmas = Arrays.asList(mentionToBeFoundText.split("\\s+"));
				if(unitLemmas.size() == mentionToBeFoundLemmas.size() 
						&& mentionToBeFoundLemmas.containsAll(unitLemmas)){
					return 1.0;
				}
				
				if(relatedTextByLemmaDBSet.contains(unitText) || relatedTextByLemmaDBSet.contains(eu.getLemmatizedText())){
					return 1.0;
				}
				
				if(relatedTextByLemmaDBGNSet.contains(unitText) || relatedTextByLemmaDBGNSet.contains(eu.getLemmatizedText())){
					//Score should be the same as if the confidence of the SEDA edge would connect the ECs with the text of mentions
					if(max < 0.91){
						max = 0.91;
					}
				}
				
			}
		}
		
		return max;
	}
}

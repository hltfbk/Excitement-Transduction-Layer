package eu.excitementproject.tl.composition.graphmerger;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.core.component.alignment.phraselink.MeteorPhraseTable;
import eu.excitementproject.eop.core.component.alignment.phraselink.MeteorPhraseTable.ScoredString;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.edautils.TEDecisionWithConfidence;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.SimpleModifier;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

/**
 * This graph merger implements a merger based on the WP2 manual procedure. 
 * As part of the merging, all transitive closure edges are added to the graph.
 * <p>Note that in this implementation both "entailment" and "non-entailment" edges are added during the merge. 
 * Yet, absence of an edge in the merged graph should be interpreted as "no entailment"  
 *
 * <p>Note that the resulting graph may contain conflicts, i.e. both an entailing and a non-entailing edge between the same source and target nodes.
 *
 * @author Lili Kotlerman
 *
 */
public class StructureAndParaphraseBasedGraphMerger extends StructureBasedGraphMerger {
	
	private MeteorPhraseTable m_phraseTable;
	
	/** Constructor, which calls the constructor of {@link AbstractGraphMerger} for the given LAP and EDA configurations.
	 * @param lap
	 * @param eda
	 * @throws GraphMergerException
	 */
	public StructureAndParaphraseBasedGraphMerger(CachedLAPAccess lap, EDABasic<?> eda, String meteorPhraseTableResourcePath)
			throws GraphMergerException {
		super(lap, eda);
		logger.info("Creating StructureAndParaphraseBasedGraphMerger with meteor phrase table...");
		try {
			m_phraseTable = new MeteorPhraseTable(meteorPhraseTableResourcePath);
		} catch (IOException e) {
			throw new GraphMergerException("Cannot create instance of MeteorPhraseTable for resource path: "+ meteorPhraseTableResourcePath+".\n"+e.getMessage());
		}
		logger.info("Done init StructureAndParaphraseBasedGraphMerger.");
	}

	/**
	 * This method is called when entailment relation A->B was detected between 2 base statements.
	 * It receives a work graph and two base statements - one from the work graph, and the other from the fragment graph that is currently being merged with the work graph
	 * The method checks for entailment in all pairs candidateEntailingtNode->candidateEntailedNode
	 * where candidateEntailingtNode-s are one-modifier (level=1) nodes, which entail A
	 * and candidateEntailedNode-s are one-modifier (level=1) nodes, which entail B. 
	 * The method returns the work  graph enriched with the entailmentRelations that were detected.  
	 * @param workGraph
	 * @param entailingBaseStatement - the base statement A, which entails the other base statement B (A->B)
	 * @param entailedBaseStatement - the base statement B, which is entailed by A
	 * @return set of EntailmentRelation-s that were detected 
	 * @throws LAPException 
	 */
	@Override
	protected EntailmentGraphRaw mergeFirstLevel(EntailmentGraphRaw workGraph, Hashtable<Integer, Set<EntailmentUnit>> candidateEntailingFragmentGraphNodes, Hashtable<Integer, Set<EntailmentUnit>> candidateEntailedFragmentGraphNodes) throws GraphMergerException{
		Set<EntailmentUnit> candidateEntailingNodes = candidateEntailingFragmentGraphNodes.get(1);
		Set<EntailmentUnit> candidateEntailedNodes = candidateEntailedFragmentGraphNodes.get(1);

		// each set of candidate's BaseStatements is supposed to contain a single node, since all candidate nodes in a Hashtable represent a single fragment graph 
		Set<EntailmentUnit> candidateEntailingBaseStatements = candidateEntailingFragmentGraphNodes.get(0);
		Set<EntailmentUnit> candidateEntailedBaseStatements = candidateEntailedFragmentGraphNodes.get(0);

		// get the candidates (1-modidier nodes, which entail each of the base statements) 
		for (EntailmentUnit candidateEntailingNode : candidateEntailingNodes){
			// get the modifier of the candidateEntailingNode
			String candidateEntailingModifier = getModifier(candidateEntailingNode);
			if (candidateEntailingModifier==null) continue; // if no modifier is found for some reason - go to the next candidateEntailingNode
			for (EntailmentUnit candidateEntailedNode : candidateEntailedNodes){				
				// check if there is already such an edge in the graph. If yes - go to the next candidate pair
				if (workGraph.isEntailment(candidateEntailingNode, candidateEntailedNode)) continue; 
				// if not - get the modifier of the candidateEntailedNode
				String candidateEntailedModifier = getModifier(candidateEntailedNode);
				if (candidateEntailedModifier==null) continue; // if no modifier is found for some reason - go to the next candidateEntailedgNode
				
				// check whether there is entailment between the modifiers. If yes - add to the set 
				EntailmentRelation edge = getEntailmentRelationFromModifiers(candidateEntailingNode, candidateEntailedNode, candidateEntailingModifier, candidateEntailedModifier);				
				if (edge!=null) {
					// update the confidence of the returned edge by multiplying it with the score of the corresponding base statement entailment  
					Double bsEntailmentConfidence = getBestEntailmentConfidence(workGraph, candidateEntailingBaseStatements,candidateEntailedBaseStatements);
					EntailmentRelation updatedEdge = new EntailmentRelation(edge.getSource(), edge.getTarget(), new TEDecisionWithConfidence(edge.getConfidence()*bsEntailmentConfidence, edge.getLabel()), edge.getEdgeType());
					workGraph.addEdge(updatedEdge.getSource(), updatedEdge.getTarget(), updatedEdge);
				}
			}
		}
		return workGraph;
	}
	

	private String getModifier(EntailmentUnit levelOneNode){
		// get the interaction Ids of the base statement(s)
		logger.debug("Retrieving modifier for <<"+levelOneNode.getText()+">>");
		if (levelOneNode.getMentions().size()>1){
			// now look through the mentions of the levelOneNode to find the one with the same text as the levelOneNode
			for (EntailmentUnitMention eum : levelOneNode.getMentions()){
				if (eum.getText().equals(levelOneNode.getText())){
					// when found, take its modifier - there should be only one
					for (SimpleModifier m : eum.getModifiers()){
						return m.getText().toLowerCase(); // so return the text of the first modifier
					}
				}
			}			
		}
		else{ // if only one mention - use it's modifier
			for (EntailmentUnitMention eum : levelOneNode.getMentions()){
				logger.debug("Using single EU mention.");
				for (SimpleModifier m : eum.getModifiers()){
					return m.getText().toLowerCase(); // so return the text of the first modifier
				}
			}						
		}
		return null;
	}
	
	/** Use entailment between modifiers instead of an EDA call and return the corresponding {@link EntailmentRelation} if there is entailment candidateEntailingNode's modifier -> candidateEntailedNode's modifier.
	 * 
	 * @param candidateEntailingNode
	 * @param candidateEntailedNode
	 * @param candidateEntailingModifier
	 * @param candidateEntailedModifier 
	 * 
	 * @return Return the corresponding {@link EntailmentRelation} if the conditions are fulfilled. Return null otherwise.
	 * @throws GraphMergerException 
	 */
	private EntailmentRelation getEntailmentRelationFromModifiers(EntailmentUnit candidateEntailingNode, EntailmentUnit candidateEntailedNode, String candidateEntailingModifier, String candidateEntailedModifier) throws GraphMergerException{	
		logger.info("Checking for entailment between modifiers: "+candidateEntailingModifier+" -> " +candidateEntailedModifier+".");
		if (candidateEntailingModifier.equals(candidateEntailedModifier)){
			logger.info("\tPositive! Confidence = 1.0 ");
			return new EntailmentRelation(candidateEntailingNode, candidateEntailedNode, new TEDecisionWithConfidence(1.0, DecisionLabel.Entailment), EdgeType.INDUCED);			
		}
		
		// check only one direction: candidateEntailingModifier -> candidateEntailedModifier

		String lhs = " " + candidateEntailingModifier + " ";
		if (lhs.contains(" "+candidateEntailedModifier+" ")) {
			logger.info("\tPositive (by inclusion of the rhs in the lhs! Confidence = 1.0 ");
			return new EntailmentRelation(candidateEntailingNode, candidateEntailedNode, new TEDecisionWithConfidence(1.0, DecisionLabel.Entailment), EdgeType.INDUCED);
		}
	
		// check if parts of rhs (candidateEntailedModifier) are included in the lhs (candidateEntailingModifier)
		Double confidence = getInclusionScore(candidateEntailingModifier, candidateEntailedModifier);
		logger.info("\tInclusion score = "+ confidence);
		if (confidence >= 1.0) {
			logger.info("\tPositive (by inclusion of terms)! Confidence = 1.0 ");
			return new EntailmentRelation(candidateEntailingNode, candidateEntailedNode, new TEDecisionWithConfidence(1.0, DecisionLabel.Entailment), EdgeType.INDUCED);			
		}
		
		// now look for paraphrases
		List<ScoredString> paraphrases = m_phraseTable.lookupParaphrasesFor(candidateEntailingModifier);
		logger.debug("\tRetrieved "+paraphrases.size()+" paraphrases for LHS modifier "+candidateEntailingModifier);
		Collections.sort(paraphrases, new ParaphraseScoreDescComparator()); // make sure the paraphrases are sorted by score in desc order

		int paraphraseRank = 0;
		for (ScoredString paraphrase : paraphrases){
			if (paraphrase.getString().equals(candidateEntailedModifier)){
				confidence += (1.0*paraphrases.size()-paraphraseRank)/paraphrases.size(); // i.e. confidence+=1 for the best-scoring paraphrase, lower for lower-scoring ones
				logger.info("\tParaphrase rank "+ paraphraseRank+" out of "+paraphrases.size()+". Confidence = "+confidence);
				break; // no need to look for more paraphrases
			}
			paraphraseRank++; // best-scoring one has rank of 0, lower-scoring ones have higher ranks from 1 to paraphrases(size)-1
		}
		if (confidence > 1.0) confidence = 1.0;
		if (confidence > 0) return new EntailmentRelation(candidateEntailingNode, candidateEntailedNode, new TEDecisionWithConfidence(confidence, DecisionLabel.Entailment), EdgeType.INDUCED);
		return null;
	}
	

	/**
	 * @param candidateEntailingModifier
	 * @param candidateEntailedModifier
	 * @return percent of candidateEntailedModifier's terms present (in surface form) in the candidateEntailingModifier. If none - return zero (0.0) 
	 */
	private Double getInclusionScore(String candidateEntailingModifier, String candidateEntailedModifier){
		String lhs = " " + candidateEntailingModifier + " ";
		String[] s = candidateEntailedModifier.split("\\s+");
		int included = 0;
		for (String term : s){
			if (lhs.contains(" "+term+" ")) included++;
		}
		if (included > 0) return 1.0*included/s.length;
		return 0.0;
	}
	
	/**
	 * Check if there is entailment for any pair of nodes candidate source -> candidate target
	 * If there is - return the confidence of the best-scoring (most confident) pair, otherwise return 0
	 * @param workGraph
	 * @param candidateSourceNodes
	 * @param candidateTargetNodes
	 * @return
	 */
	private Double getBestEntailmentConfidence(EntailmentGraphRaw workGraph, Set<EntailmentUnit> candidateSourceNodes, Set<EntailmentUnit> candidateTargetNodes){
		Double bestConfidence = 0.0;
		for (EntailmentUnit source : candidateSourceNodes){
			for (EntailmentUnit target : candidateTargetNodes){
				for (EntailmentRelation edge : workGraph.getAllEdges(source, target)){
					if (edge.getLabel().is(DecisionLabel.Entailment)){
						if (edge.getConfidence()>bestConfidence) bestConfidence = edge.getConfidence();
					}
				}
			}
		}
		return bestConfidence;
	}

	/**
	 * Comparator to sort paraphrases from the phrase table by score
	 * (since I am not sure they are returned sorted, although they come as a list)
	 */
	public static class ParaphraseScoreDescComparator implements Comparator<ScoredString> {
	    @Override
	    public int compare(ScoredString paraphraseA, ScoredString paraphraseB) {
	        Double scoreA = paraphraseA.getScore(); // turn double to Double to invoke the comparator for Double
	    	return -1*scoreA.compareTo(paraphraseB.getScore());
	    }
	}
	
	
}

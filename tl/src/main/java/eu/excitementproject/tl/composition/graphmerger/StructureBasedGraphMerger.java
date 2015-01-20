package eu.excitementproject.tl.composition.graphmerger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

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
public class StructureBasedGraphMerger extends AbstractGraphMerger {
	
	/** Constructor, which calls the constructor of {@link AbstractGraphMerger} for the given LAP and EDA configurations.
	 * @param lap
	 * @param eda
	 * @throws GraphMergerException
	 */
	public StructureBasedGraphMerger(CachedLAPAccess lap, EDABasic<?> eda)
			throws GraphMergerException {
		super(lap, eda);
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(Set<FragmentGraph> fragmentGraphs,
			EntailmentGraphRaw workGraph) throws GraphMergerException {

		List<FragmentGraph> fg = new LinkedList<FragmentGraph>(fragmentGraphs);
		Collections.sort(fg, new FragmentGraph.CompleteStatementComparator());
		// Iterate over the list of fragment graphs and merge them one by one
		for (FragmentGraph fragmentGraph : fg){
			workGraph=mergeGraphs(fragmentGraph, workGraph);
		}
		workGraph.applyTransitiveClosure();
		return workGraph;
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(FragmentGraph fragmentGraph,
			EntailmentGraphRaw workGraph) throws GraphMergerException {
		
		logger.info("Adding fragment graph:\n"+fragmentGraph.toString());
		
		// If the work graph is empty or null - just copy the fragment graph nodes/edges (there's nothing else to merge) and return the resulting graph
		if (workGraph==null) return new EntailmentGraphRaw(fragmentGraph, false);
		if (workGraph.isEmpty()) return new EntailmentGraphRaw(fragmentGraph, false, workGraph.hasLemmatizedLabel());
		
		 
		// else - Implement the WP2 flow		
		workGraph.copyFragmentGraphNodesAndAllEdges(fragmentGraph);
		
		// find the node corresponding to the fragment graph's base statement in the work graph
		EntailmentUnit newBaseStatement = workGraph.getVertexWithText(fragmentGraph.getBaseStatement().getText());
		/* It might be that we already had this base statement in the raw graph, but resulting from a different completeStatement
		* e.g. The old clerk was very rude -> ... -> The clerk was rude
		*  vs. The young clerk was too rude -> ...-> The clerk was rude
		* so we want to keep track of which fragment we're merging 
		* and thus we keep the complete statement of the fragment graph, which is currently being merged */
		
		String fgCompleteStatement = fragmentGraph.getCompleteStatement().getText();
		Hashtable<Integer, Set<EntailmentUnit>> newFragmentGraphNodes;
		try {
			newFragmentGraphNodes = workGraph.getFragmentGraphNodes(newBaseStatement, fgCompleteStatement);
		} catch (EntailmentGraphRawException e) {
			throw new GraphMergerException(e.getMessage());
		}
		
		// 2. For each base statement in the work graph, restore each of its corresponding fragment graphs and perform their merge with the current fragment graph		
		for (EntailmentUnit workGraphBaseStatement : workGraph.getBaseStatements()){
			// if (workGraphBaseStatement.equals(newBaseStatement)) continue; // DO check new (fragment graph's) base statement with itself, since it might be that we have this base statement from another fragment graph
			
			// restore each of the corresponding fragmentGraphs (the one whose nodes have the same complete statement)
			for (String bsCompleteStatement : workGraphBaseStatement.getCompleteStatementTexts()){
				Hashtable<Integer, Set<EntailmentUnit>> oldFragmentGraphNodes;
				try {
					oldFragmentGraphNodes = workGraph.getFragmentGraphNodes(workGraphBaseStatement, bsCompleteStatement);
				} catch (EntailmentGraphRawException e) {
					throw new GraphMergerException(e.getMessage());
				}
				// update the graph by merging the newfragmentGraph with the oldFragmentGraph
				workGraph = mergeFragmentGraphs(workGraph, newFragmentGraphNodes, oldFragmentGraphNodes, newBaseStatement, workGraphBaseStatement);				
			}
		}			
		workGraph.applyTransitiveClosure();
		return workGraph;		
	}
	
	/** Perform the actual merge of two FragmentGraphs within a given work graph
	 * @param workGraph
	 * @param newFragmentGraphNodes
	 * @param oldFragmentGraphNodes
	 * @param newBaseStatement
	 * @param workGraphBaseStatement
	 * @return updated work graph
	 * @throws GraphMergerException
	 */	
	private EntailmentGraphRaw mergeFragmentGraphs(EntailmentGraphRaw workGraph, Hashtable<Integer, Set<EntailmentUnit>> newFragmentGraphNodes, Hashtable<Integer, Set<EntailmentUnit>> oldFragmentGraphNodes, EntailmentUnit newBaseStatement,  EntailmentUnit workGraphBaseStatement) throws GraphMergerException{
		//Check if there is entailment between the two base statements
		Set<EntailmentRelation> edgesToAdd = mergeBaseStatements(workGraph, newBaseStatement, workGraphBaseStatement);
		if (edgesToAdd.isEmpty()) return workGraph; // empty set = no entailment, i.e. we are done  - there's nothing else to merge (return the current work graph)
		
		// add the entailment relation(s) between the 2 base statements
		for (EntailmentRelation edge : edgesToAdd){
			workGraph.addEdge(edge.getSource(), edge.getTarget(), edge);
		}			

		
		// If there was an existing entailment edge, or if we just found that there is entailment in either direction between the 2 base statements
		// Now we need to look at the direction of the entailment and check in this direction for 1st-level nodes
		// And then propagate for the upper-level nodes
//		logger.info("Checking entailment on upper levels");
		
		// check if both graph have 1-level nodes (maybe one of the 2 fragment graphs only has one node (base statement = complete statement))
		if (!oldFragmentGraphNodes.keySet().contains(1)) return workGraph; // no 1-level in "old fr.graph" => no need to continue merging
		if (!newFragmentGraphNodes.keySet().contains(1)) return workGraph; // no 1-level in "new fr.graph" => no need to continue merging
		
		// check the direction workGraphBaseStatement -> newBaseStatement
		if (workGraph.isEntailment(workGraphBaseStatement, newBaseStatement)) {
			// merge 1st-level nodes
			workGraph= mergeFirstLevel(workGraph, oldFragmentGraphNodes, newFragmentGraphNodes);
			// propagate to the upper levels
			workGraph = mergeUpperLevels(workGraph, oldFragmentGraphNodes, newFragmentGraphNodes);			
		}
		

		// check the direction newBaseStatement -> workGraphBaseStatement
		if (workGraph.isEntailment(newBaseStatement, workGraphBaseStatement)) {
			// merge 1st-level nodes
			workGraph= mergeFirstLevel(workGraph, newFragmentGraphNodes, oldFragmentGraphNodes);
			// propagate to the upper levels
			workGraph = mergeUpperLevels(workGraph, newFragmentGraphNodes, oldFragmentGraphNodes);			
		}

		return workGraph;
	}
	

	/** Check if base statements of two Fragment Graphs are entailing in any direcion
	 * @param workGraph
	 * @param newBaseStatement
	 * @param workGraphBaseStatement
	 * @return set of the corresponding entailment relations 
	 * @throws GraphMergerException
	 */
	protected Set<EntailmentRelation> mergeBaseStatements(EntailmentGraphRaw workGraph, EntailmentUnit newBaseStatement, EntailmentUnit workGraphBaseStatement) throws GraphMergerException{
		//Check if there is entailment between the two base statements
		// There might be an existing entailment edge because the two base statements were present in the work graph before
		Set<EntailmentRelation> edgesToAdd = new HashSet<EntailmentRelation>();
		if (!workGraph.isEntailmentInAnyDirection(newBaseStatement, workGraphBaseStatement)){
			// If there's no existing entailment, check if there is entailment between the base statements
//			logger.info("Checking entailment between base statements");
			edgesToAdd = getEntailmentRelations(workGraphBaseStatement, newBaseStatement);
		}
		return edgesToAdd;
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
	protected EntailmentGraphRaw mergeFirstLevel(EntailmentGraphRaw workGraph, Hashtable<Integer, Set<EntailmentUnit>> candidateEntailingFragmentGraphNodes, Hashtable<Integer, Set<EntailmentUnit>> candidateEntailedFragmentGraphNodes) throws GraphMergerException{
		Set<EntailmentUnit> candidateEntailingNodes = candidateEntailingFragmentGraphNodes.get(1);
		Set<EntailmentUnit> candidateEntailedNodes = candidateEntailedFragmentGraphNodes.get(1);
		// get the candidates (1-modidier nodes, which entail each of the base statements) 
		for (EntailmentUnit candidateEntailingNode : candidateEntailingNodes){
			for (EntailmentUnit candidateEntailedNode : candidateEntailedNodes){
				// check if there is already such an edge in the graph. If yes - go to the next candidate pair
				if (workGraph.isEntailment(candidateEntailingNode, candidateEntailedNode)) continue; 
				// check whether there is entailment. If yes - add to the set 
				EntailmentRelation edge = getEntailmentRelation(candidateEntailingNode, candidateEntailedNode);
				if (edge!=null) workGraph.addEdge(edge.getSource(), edge.getTarget(), edge);
			}
		}
		return workGraph;
	}
	

	/**
	 * Performs the merge of upper-level FragmentGraph nodes, after first-level nodes were merged
	 * @param workGraph
	 * @param candidateEntailingFragmentGraphNodes
	 * @param candidateEntailedFragmentGraphNodes
	 * @return the updated work graph
	 */
	private EntailmentGraphRaw mergeUpperLevels(EntailmentGraphRaw workGraph, Hashtable<Integer, Set<EntailmentUnit>> candidateEntailingFragmentGraphNodes, Hashtable<Integer, Set<EntailmentUnit>> candidateEntailedFragmentGraphNodes){
		// detect the highest common level (up to this level we need to propagate)
		int highestCommonLevel = Collections.max(candidateEntailingFragmentGraphNodes.keySet());
		int i = Collections.max(candidateEntailedFragmentGraphNodes.keySet());
		if (i<highestCommonLevel) highestCommonLevel=i;
		
		// propagate for each level starting from 2 till highestCommonLevel
		for (int level=2; level<=highestCommonLevel; level++){
			Set<EntailmentUnit> currentLevelCandidateEntailingNodes = candidateEntailingFragmentGraphNodes.get(level);
			Set<EntailmentUnit> currentLevelCandidateEntailedNodes = candidateEntailedFragmentGraphNodes.get(level);
			// now examine each pair of candidates (entailing -> entailed)
			if (currentLevelCandidateEntailingNodes != null && currentLevelCandidateEntailedNodes != null) {
				for (EntailmentUnit candidateEntailingNode : currentLevelCandidateEntailingNodes){
					for (EntailmentUnit candidateEntailedNode : currentLevelCandidateEntailedNodes){
						// if such edge (candidateEntailingNode -> candidateEntailedNode) does not yet exist
						if (!workGraph.isEntailment(candidateEntailingNode, candidateEntailedNode)){
							// check whether we can induce such edge from lower-level entailments (get confidence >0) 
							Double confidence = induceEntailment(workGraph, candidateEntailingNode, candidateEntailedNode);
							if (confidence>0){
								workGraph.addEdgeByInduction(candidateEntailingNode, candidateEntailedNode, DecisionLabel.Entailment, confidence);
							}
						}
					}
				}
			}
		}			
		return workGraph;
	}
	
	/** Decide on entailment edges induced from the structure of FragmentGraphs and previously made entailment decisions
	 * @param workGraph
	 * @param candidateEntailingNode
	 * @param candidateEntailedNode
	 * @return confidence of the induced decision
	 */
	private double induceEntailment(EntailmentGraphRaw workGraph, EntailmentUnit candidateEntailingNode, EntailmentUnit candidateEntailedNode){
		double confidence = 100.0;
		int childLevel = candidateEntailingNode.getLevel()-1;
		Set<EntailmentUnit> entailingChildNodes = workGraph.getEntailedNodesFromSameFragmentGraph(candidateEntailingNode, childLevel);
		Set<EntailmentUnit> entailedChildNodes = workGraph.getEntailedNodesFromSameFragmentGraph(candidateEntailedNode, childLevel);
		// if each of the entailingChildNodes entailes one of the entailedChildNodes - can induce entailment between the parents
		// TODO: need to entail a different entailedChildNode every time? need to have a closed list of entailedChildNodes, which were already "used"?
		int matchedPairs = 0;
		for (EntailmentUnit entailingChildNode : entailingChildNodes){
			for (EntailmentUnit entailedChildNode : entailedChildNodes){
				if (workGraph.isEntailment(entailingChildNode, entailedChildNode)){
					double localConfidence = workGraph.getEdge(entailingChildNode, entailedChildNode).getConfidence();
					if (confidence > localConfidence) confidence=localConfidence; // at the end the confidence will be equal to the smallest local confidence 
					matchedPairs++;
					break; // don't need to check other entailedChildNodes, go to the next entailingChildNode
				}
			}
		}
		if (matchedPairs==entailingChildNodes.size()) {
			return confidence; // if each of the entailingChildNodes found its pair among the entailedChildNodes - then can induce there is entailment between the parents 
		}
		return 0.0; // otherwise - return 0
	}

}

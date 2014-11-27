/**
 * 
 */
package eu.excitementproject.tl.composition.graphmerger;

import java.util.HashSet;

import java.util.Set;
import org.apache.log4j.Logger;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;


/**
 * 
Abstract implementation of the {@link GraphMerger} interface. 
The class contains methods that are likely to be shared by the actual implementations, including
constructors with LAP and EDA configurations.

 * @author Lili Kotlerman
 */
public abstract class AbstractGraphMerger implements GraphMerger{

	Logger logger = Logger.getLogger("eu.excitementproject.tl.composition.graphmerger");

	/******************************************************************************************
	 * ATTRIBUTES
	 * ****************************************************************************************/
	
	/**
	 * Holds the number of EDA calls performed by the merger. Is null if this information is not available.
	 * Should be initialized as zero in a constructor and updated after each actual EDA call. 
	 */
	public Integer edaCalls = null;

	/**
	 * Holds the predefined threshold for confidence of entailment decisions. 
	 * Is null when no threshold is defined. 
	 * <p>Should be set by the method {@link GraphMerger#setEntailmentConfidenceThreshold(Double)}.
	 */
	public Double entailmentConfidenceThreshold = null;
	
	/**
	 * The LAP to be used by the merger.
	 */
	private final CachedLAPAccess lap;
	
	/**
	 * The EDA to be used by the merger.
	 */
	private final EDABasic<?> eda;
	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/

	/** Constructor, which specifies the LAP and the EDA to be used by the merger.
	 * @param lap
	 * @param eda
	 * @throws GraphMergerException
	 */
	public AbstractGraphMerger(CachedLAPAccess lap, EDABasic<?> eda) throws GraphMergerException{
		this.lap=lap;
		this.eda=eda;
		edaCalls=0;
	}

	/******************************************************************************************
	 * IMPLEMENTATION OF THE INTERFACE METHODS
	 * ****************************************************************************************/

	@Override
	public EntailmentGraphRaw mergeGraphs(Set<FragmentGraph> fragmentGraphs)
			throws GraphMergerException {
		
		return mergeGraphs(fragmentGraphs, null);
	}	
	
	@Override
	public Integer getEdaCallsNumber() {
		return edaCalls;
	}

	@Override
	public void setEntailmentConfidenceThreshold(Double entailmentConfidenceThreshold) {
		this.entailmentConfidenceThreshold = entailmentConfidenceThreshold;
	}

	/******************************************************************************************
	 * ACCESS TO ATTRIBUTES
	 * ****************************************************************************************/

	/**
	 * @return the LAP defined for this merger (passed in the constructor)
	 */
	public CachedLAPAccess getLap() {
		return this.lap;
	}
	
	/**
	 * @return the EDA defined for this merger (passed in the constructor)
	 */
	public EDABasic<?> getEda() {
		return this.eda;
	}

	/**
	 * @return the entailmentConfidenceThreshold
	 */
	public Double getEntailmentConfidenceThreshold() {
		return entailmentConfidenceThreshold;
	}
	
	/******************************************************************************************
	 * AUXILIARY METHODS FOR MERGING
	 * ****************************************************************************************/

	/** Make EDA calls to check for entailment (in both directions) between nodeA and nodeB. If there is entailment in any direction, corresponding EntailmentRelation(s) ({@link EntailmentRelation}) will be returned.
	 * <p>The yes/no decision on entailment takes into considerartion the value of {@link AbstractGraphMerger#entailmentConfidenceThreshold}, if defined.   
	 * @param nodeA
	 * @param nodeB
	 * @return set of entailment relations found between the nodes. The set can contain 0 (no entailment), 1 (entailment in one direction) or 2 (bi-directional entailment, paraphrase) elements.
	 * @throws GraphMergerException
	 */
	protected Set<EntailmentRelation> getEntailmentRelations(EntailmentUnit nodeA, EntailmentUnit nodeB) throws GraphMergerException{
		Set<EntailmentRelation> entailmentRelations = new HashSet<EntailmentRelation>();
		
		// check one direction: nodeA -> nodeB
		EntailmentRelation r = getEntailmentRelation(nodeA, nodeB);
		// add the edge to the output only if observed entailment
		if (r!=null) entailmentRelations.add(r); 
		
	
		// check the other direction: nodeB -> nodeA
		r = getEntailmentRelation(nodeB, nodeA);
		if (r!=null) entailmentRelations.add(r); 
				
		return entailmentRelations;
	}

	/** Make an EDA call and return the corresponding {@link EntailmentRelation} if there is entailment candidateEntailingNode -> candidateEntailedNode.
	 * <p> Decision whether there is entailment is based on the {@link DecisionLabel} returned by the EDA, and takes into consideration the value of {@link AbstractGraphMerger#entailmentConfidenceThreshold}.
	 * 
	 * @param candidateEntailingNode
	 * @param candidateEntailedNode
	 * 
	 * @return Return the corresponding {@link EntailmentRelation} if the conditions are fulfilled. Return null otherwise.
	 * @throws GraphMergerException 
	 */
	protected EntailmentRelation getEntailmentRelation(EntailmentUnit candidateEntailingNode, EntailmentUnit candidateEntailedNode) throws GraphMergerException{	
		// check only one direction: candidateEntailingNode -> candidateEntailedNode
		EntailmentRelation r = getRelation(candidateEntailingNode, candidateEntailedNode);
		logger.info("\t'"+candidateEntailingNode.getTextWithoutDoubleSpaces() +"'\t->\t'"+candidateEntailedNode.getTextWithoutDoubleSpaces()+"'\t"+r.getLabel().toString());
		if (r.getLabel().is(DecisionLabel.Entailment)) {
			if (isSufficientConfidence(r.getConfidence()))  return r;			
		}
		return null;
	}

	/** Make EDA calls and return the resulting {@link EntailmentRelation}s (in both directions) between nodeA and nodeB.
	 * <p> Does not take into consideration the value of {@link AbstractGraphMerger#entailmentConfidenceThreshold}.
	 * @param nodeA
	 * @param nodeB
	 * @return set of {@link EntailmentRelation}s between the two nodes. The set should contains 2 elements, for the two entailment directions.
	 * @throws GraphMergerException
	 */
	protected Set<EntailmentRelation> getRelations(EntailmentUnit nodeA, EntailmentUnit nodeB) throws GraphMergerException{
		Set<EntailmentRelation> entailmentRelations = new HashSet<EntailmentRelation>();
		
		// check one direction: nodeA -> nodeB
			entailmentRelations.add(getRelation(nodeA, nodeB)); 
		// check the other direction: nodeB -> nodeA
			entailmentRelations.add(getRelation(nodeB, nodeA)); 
				
		return entailmentRelations;
	}

	
		 			
	/** Make an EDA call and return the resulting {@link EntailmentRelation} for candidateEntailingNode -> candidateEntailedNode.
	 * <p> Does not take into consideration the value of {@link AbstractGraphMerger#entailmentConfidenceThreshold}.
	 * <p> The method performs the actual EDA call, and thus updates the value of {@link AbstractGraphMerger#edaCalls}. 

	 * @param candidateEntailingNode
	 * @param candidateEntailedNode
	 * 
	 * @return Return the corresponding {@link EntailmentRelation}.
	 * @throws GraphMergerException 
	 */
	protected EntailmentRelation getRelation(EntailmentUnit candidateEntailingNode, EntailmentUnit candidateEntailedNode) throws GraphMergerException{	
		edaCalls++;
		// check only one direction: candidateEntailingNode -> candidateEntailedNode
		try {
			return new EntailmentRelation(candidateEntailingNode, candidateEntailedNode, this.getEda(), this.lap);
		} catch (EntailmentGraphRawException e) {
			throw new GraphMergerException(e.getMessage());
		}
	}

	/** Check if the given confidence is sufficient to consider an entailment decision valid, based on the predefined threshold {@link AbstractGraphMerger#entailmentConfidenceThreshold}.
	 * @param confidence - the given confidence to check
	 * @return true if the given confidence is >= the threshold, otherwise return false. If no threshold was defined, return true.
	 */
	private boolean isSufficientConfidence(double confidence){
		if (entailmentConfidenceThreshold==null) return true; //if entailmentConfidenceThreshold is not defined, then any confidence is sufficient to add an edge
		if (confidence >= entailmentConfidenceThreshold) return true; // if confidence >= threshold, then it is sufficient 
		return false; // otherwise - it's not sufficient
	}
	

}

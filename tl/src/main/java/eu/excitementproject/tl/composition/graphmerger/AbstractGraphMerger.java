/**
 * 
 */
package eu.excitementproject.tl.composition.graphmerger;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPException;
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
An implementation of the {@link GraphMerger} interface:
An implementation need to call LAP and EDA.
The needed LAP & EDA related configurations should be passed via the
Constructor (Thus, they are not defined in the interface). Also, any
additional configurable parameters of this module implementation
should be clearly exposed in the Constructor.

 * @author Lili
 */
public abstract class AbstractGraphMerger implements GraphMerger{

	private final CachedLAPAccess lap;
	private final EDABasic<?> eda;
	
	/** The needed LAP & EDA related configurations should be passed via the
Constructor (Thus, they are not defined in the interface). Also, any
additional configurable parameters of this module implementation
should be clearly exposed in the Constructor.
	 * @param lap
	 * @param eda
	 * @throws GraphMergerException
	 */
	public AbstractGraphMerger(CachedLAPAccess lap, EDABasic<?> eda) throws GraphMergerException{
		this.lap=lap;
		this.eda=eda;
	}

	@Override
	public EntailmentGraphRaw mergeGraphs(Set<FragmentGraph> fragmentGraphs)
			throws GraphMergerException, LAPException {
		
		return mergeGraphs(fragmentGraphs, null);
	}	
	
	/**
	 * @return the LAP passed in the constructor to the GraphMerger implementation
	 */
	public CachedLAPAccess getLap() {
		return this.lap;
	}
	
	/**
	 * @return the EDA passed in the constructor to the GraphMerger implementation
	 */
	public EDABasic<?> getEda() {
		return this.eda;
	}

	
	/** Checks for entailment (in both directions) between nodeA and nodeB. If there is entailment in any direction, corresponding EntailmentRelation(s) will be returned. 
	 * @param workGraph
	 * @param nodeA
	 * @param nodeB
	 * @return set of entailment relations found between the nodes. The set can contain 0 (no entailment), 1 (entailment in one direction) or 2 (bi-directional entailment, paraphrase) elements.
	 * @throws LAPException 
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


	protected Set<EntailmentRelation> getRelations(EntailmentUnit nodeA, EntailmentUnit nodeB) throws GraphMergerException{
		Set<EntailmentRelation> entailmentRelations = new HashSet<EntailmentRelation>();
		
		// check one direction: nodeA -> nodeB
			entailmentRelations.add(getRelation(nodeA, nodeB)); 
		// check the other direction: nodeB -> nodeA
			entailmentRelations.add(getRelation(nodeB, nodeA)); 
				
		return entailmentRelations;
	}

	
	/*
	 * viv@fbk: added lap parameter
	 */			
	protected EntailmentRelation getRelation(EntailmentUnit candidateEntailingNode, EntailmentUnit candidateEntailedNode) throws GraphMergerException{	
		// check only one direction: candidateEntailingNode -> candidateEntailedNode
		try {
			return new EntailmentRelation(candidateEntailingNode, candidateEntailedNode, this.getEda(), this.lap);
		} catch (EntailmentGraphRawException e) {
			throw new GraphMergerException(e.getMessage());
		}
	}

	/** Return the corresponding EntailmentRelation if there is entailment (DecisionLabel.Entailment) candidateEntailingNode -> candidateEntailedNode
	 * Return null otherwise 
	 * @param candidateEntailingNode
	 * @param candidateEntailedNode
	 * @return
	 * @throws LAPException 
	 */
	protected EntailmentRelation getEntailmentRelation(EntailmentUnit candidateEntailingNode, EntailmentUnit candidateEntailedNode) throws GraphMergerException{	
		// check only one direction: candidateEntailingNode -> candidateEntailedNode
		EntailmentRelation r = getRelation(candidateEntailingNode, candidateEntailedNode);
		if (r.getLabel().equals(DecisionLabel.Entailment)) return r;
		return null;
	}


}

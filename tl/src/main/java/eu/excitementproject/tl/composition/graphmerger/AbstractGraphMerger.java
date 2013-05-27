/**
 * 
 */
package eu.excitementproject.tl.composition.graphmerger;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
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

	private final LAPAccess lap;
	private final EDABasic<?> eda;
	
	/** The needed LAP & EDA related configurations should be passed via the
Constructor (Thus, they are not defined in the interface). Also, any
additional configurable parameters of this module implementation
should be clearly exposed in the Constructor.
	 * @param lap
	 * @param eda
	 * @throws GraphMergerException
	 */
	public AbstractGraphMerger(LAPAccess lap, EDABasic<?> eda) throws GraphMergerException{
		this.lap=lap;
		this.eda=eda;
	}
	
	/**
	 * @return the LAP passed in the constructor to the GraphMerger implementation
	 */
	public LAPAccess getLap() {
		return this.lap;
	}
	
	/**
	 * @return the EDA passed in the constructor to the GraphMerger implementation
	 */
	public EDABasic<?> getEda() {
		return this.eda;
	}

	
	/** Checks the given work graph for entailment (in both directions) between nodeA and nodeB. If there is entailment in any direction, corresponding EntailmentRelation(s) will be returned. 
	 * @param workGraph
	 * @param nodeA
	 * @param nodeB
	 * @return set of entailment relations found between the nodes. The set can contain 0 (no entailment), 1 (entailment in one direction) or 2 (bi-directional entailment, paraphrase) elements.
	 */
	protected Set<EntailmentRelation> getEntailmentRelations(EntailmentGraphRaw workGraph, EntailmentUnit nodeA, EntailmentUnit nodeB){
		Set<EntailmentRelation> entailmentRelations = new HashSet<EntailmentRelation>();
		
		// check one direction: nodeA -> nodeB
		EntailmentRelation r = new EntailmentRelation(nodeA, nodeB, this.getEda());
		if (r.getLabel().equals(DecisionLabel.Entailment)) {
			// add the edge to the output only if observed entailing, according to the WP2 algo
			// we don't need to store all the knowledge we have for WP2 graph merger
			entailmentRelations.add(r); 
		}
	
			// check the other direction: nodeB -> nodeA
		r = new EntailmentRelation(nodeB, nodeA, this.getEda());
		if (r.getLabel().equals(DecisionLabel.Entailment)) {
			entailmentRelations.add(r); 
		}
				
		return entailmentRelations;
	}
}

/**
 * 
 */
package  eu.excitementproject.tl.composition.graphmerger;

import workGraph.EntailmentRelation;
import workGraph.EntailmentUnit;
import fragmentGraph.EntailmentUnitMention;
import fragmentGraph.FragmentGraphEdge;
import eu.excitement.api.FragmentAnnotator;
import eu.excitement.api.GraphMerger;
import eu.excitement.api.exceptions.FragmentAnnotatorException;
import eu.excitement.api.exceptions.GraphMergerException;

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
public abstract class AbstractGraphMerger implements GraphMerger<EntailmentUnitMention, FragmentGraphEdge<EntailmentUnitMention>, 
	EntailmentUnit, EntailmentRelation<EntailmentUnit>>{

	private final LAP_TLAccess lap;
	private final EDAbasic<?> eda;
	
	/** The needed LAP & EDA related configurations should be passed via the
Constructor (Thus, they are not defined in the interface). Also, any
additional configurable parameters of this module implementation
should be clearly exposed in the Constructor.
	 * @param lap
	 * @param eda
	 * @throws GraphMergerException
	 */
	public AbstractGraphMerger(LAP_TLAccess lap, EDAbasic<?> eda) throws GraphMergerException{
		this.lap=lap;
		this.eda=eda;
	}
	
	/**
	 * @return the LAP passed in the constructor to the GraphMerger implementation
	 */
	public LAP_TLAccess getLap() {
		return this.lap;
	}
	
	/**
	 * @return the EDA passed in the constructor to the GraphMerger implementation
	 */
	public EDAbasic<?> getEda() {
		return this.eda;
	}
	
}

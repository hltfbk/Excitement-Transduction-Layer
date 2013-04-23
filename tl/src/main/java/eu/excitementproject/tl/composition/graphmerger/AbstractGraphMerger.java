/**
 * 
 */
package eu.excitementproject.tl.composition.graphmerger;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;


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
	
}

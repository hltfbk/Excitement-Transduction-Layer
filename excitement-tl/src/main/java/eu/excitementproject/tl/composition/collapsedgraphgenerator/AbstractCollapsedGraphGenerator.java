/**
 * 
 */
package  eu.excitementproject.tl.composition.collapsedgraphgenerator;


import workGraph.EntailmentRelation;
import workGraph.EntailmentUnit;
import finalGraph.EntailmentRelationCollapsed;
import finalGraph.EquivalenceClass;
import eu.excitement.api.CollapsedGraphGenerator;

/**
An implementation of the {@link CollapsedGraphGenerator} interface:
We do not foresee any external EOP component dependency for this
module. But this is not definite. The first prototype will shed some
light for us. Like other modules, if it needs any arguments or
configurable values, they will be exposed in the implementation
constructor.
 * @author Lili
 * 
 */
public abstract class AbstractCollapsedGraphGenerator implements CollapsedGraphGenerator<EntailmentUnit, EntailmentRelation<EntailmentUnit>, 
	EquivalenceClass, EntailmentRelationCollapsed<EquivalenceClass>>{

}

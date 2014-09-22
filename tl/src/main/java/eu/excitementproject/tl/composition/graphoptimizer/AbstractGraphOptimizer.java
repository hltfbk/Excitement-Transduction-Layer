/**
 * 
 */
package eu.excitementproject.tl.composition.graphoptimizer;

import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;



/**
An implementation of the {@link GraphOptimizer} interface:
We do not foresee any external EOP component dependency for this
module. But this is not definite. The first prototype will shed some
light for us. Like other modules, if it needs any arguments or
configurable values, they will be exposed in the implementation
constructor.

 * @author Lili Kotlerman
 */
public abstract class AbstractGraphOptimizer implements GraphOptimizer{

	
	/**
	 * @param workGrap
	 * @return the average of the confidence scores of entailment edges.
	 * If there are no edges in the graph that express entailment, the method returns null. 
	 */
	public Double getAverageConfidenceOfEntailment(EntailmentGraphRaw workGrap){
		double sum = 0.0;
		double n = 0.0;
		
		for (EntailmentRelation edge : workGrap.edgeSet()){
			if (edge.getLabel().is(DecisionLabel.Entailment)){
				sum+=edge.getConfidence();
				n++;
			}
		}
		
		if (n>0) return sum/n;
		return null;		
	}	
	
	/** Returns the equivalence class from the imput set, which includes the input entailment unit
	 * If such equivalence class is not found, returns null 
	 * @param equivalenceClasses - the set of equivalence classes
	 * @param eu the entailment unit
	 * @return the equivalence class from the imput set, which includes the input entailment unit
	 */
	public EquivalenceClass getEquivalenceClass(Set<EquivalenceClass> equivalenceClasses, EntailmentUnit eu){
		for (EquivalenceClass currentEquivalenceClass : equivalenceClasses){
			if (currentEquivalenceClass.containsEntailmentUnit(eu)) return currentEquivalenceClass;
		}
		return null;
	}
	
	/** Returns true if the input entailment unit is included in one of the equivalence classes in the input set of equivalence classes
	 * @param equivalenceClasses - the set of equivalence classes
	 * @param eu - the entailment unit
	 * @return true/false
	 */
	public boolean containsEntailmentUnit(Set<EquivalenceClass> equivalenceClasses, EntailmentUnit eu){
		if (getEquivalenceClass(equivalenceClasses, eu)!=null) return true;
		return false;
	}
}

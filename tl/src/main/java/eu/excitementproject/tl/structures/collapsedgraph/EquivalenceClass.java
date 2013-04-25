package  eu.excitementproject.tl.structures.collapsedgraph;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * 
 * @author vivi@fbk
 * 
 * The node of the collapsed entailment graph is an equivalence class. 
 * This type of node will contain all text fragments that are equivalent from
 * the point of view of textual entailment.
 *
 */
public class EquivalenceClass {
	
	int id;
	
	/** 
	 * the "canonical" text of the class 
	 */
	String label;
	
	Set<EntailmentUnit> entailmentUnits = null;
	
	/**
	 * Constructor for the EquivalenceClass 
	 * 
	 * @param eu -- an EntailmentUnit
	 */
	public EquivalenceClass(EntailmentUnit eu) {
		entailmentUnits = new HashSet<EntailmentUnit>();
		entailmentUnits.add(eu);
		label = eu.getText();
	}	
	
	/**
	 * Constructor for the EquivalenceClass
	 * 
	 * @param s_eu -- a set of EntailmentUnits
	 */
	public EquivalenceClass(Set<EntailmentUnit> s_eu) {
		entailmentUnits = new HashSet<EntailmentUnit>();
		entailmentUnits.addAll(s_eu);
// pick one element of the set and initialize the label associated with this node		
//		label = ... 
	}
	
	/**
	 * Constructor for the EquivalenceClass
	 * 
	 * @param text -- "canonical" text representing this equivalence class
	 * @param s_eu -- a set of entailment units
	 */
	public EquivalenceClass(String text, Set<EntailmentUnit> s_eu) {
		entailmentUnits = new HashSet<EntailmentUnit>();
		entailmentUnits.addAll(s_eu);
		label = text;
	}
	
}

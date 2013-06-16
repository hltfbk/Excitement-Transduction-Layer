package  eu.excitementproject.tl.structures.collapsedgraph;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

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
	
	// int id;
	
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
		// Pick one element of the set and initialize the label associated with this node
		// We pick entailment unit with max frequency (if there are several entailment units with such frequency, sorter text is favored) 
		int frequency = 0;
		for (EntailmentUnit candidateEntailmentUnit : entailmentUnits){
			if (candidateEntailmentUnit.getFrequency()>frequency){
				label = candidateEntailmentUnit.getText();
				frequency = candidateEntailmentUnit.getFrequency();
			}
			else if (candidateEntailmentUnit.getFrequency()==frequency){ // if current label has the same frequency as the candidate entailment unit
				if (candidateEntailmentUnit.getText().length() < label.length()) { // if the candidate text is shorter - make it the new label 
					label =  candidateEntailmentUnit.getText();
				}
			}
		}
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

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the entailmentUnits
	 */
	public Set<EntailmentUnit> getEntailmentUnits() {
		return entailmentUnits;
	}
	
	public boolean containsEntailmentUnit(EntailmentUnit eu){
		return entailmentUnits.contains(eu);
	}
	
	public void add(EntailmentUnit eu){
		entailmentUnits.add(eu);
	}
	
	public void add(Set<EntailmentUnit> s_eu){
		entailmentUnits.addAll(s_eu);
	}
	
	public Set<String> getInteractionIds(){
		Set<String> interactionIds = new HashSet<String>();
		for (EntailmentUnit eu : this.entailmentUnits){
			interactionIds.addAll(eu.getInteractionIds());
		}
		return interactionIds;		
	}
	
	@Override
	public String toString(){
		String s = "\""+label.trim().replaceAll(" +", " ")+"\" ("+this.getInteractionIds().size()+" interactions) :\n";
		int i=1;
		for (EntailmentUnit eu : entailmentUnits){
			s+="\t"+i+")\""+eu.getTextWithoutDoulbeSpaces()+"\"\n";
			i++;
		}
		s+="\n";
		return s;
	}
	
	public String toDOT(){
		String s = "\""+label.trim().replaceAll(" +", " ")+" ("+this.getInteractionIds().size()+" interactions) :";
		for (EntailmentUnit eu : entailmentUnits){
			if (eu.getText().equals(label)) continue;
			s+="\\n"+eu.getTextWithoutDoulbeSpaces();			
		}
		s+="\"";
		return s;
	}
	
	
}

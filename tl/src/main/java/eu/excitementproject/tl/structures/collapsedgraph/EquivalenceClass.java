package  eu.excitementproject.tl.structures.collapsedgraph;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 * 
 * @author vivi@fbk & Lili Kotlerman & Kathrin
 * 
 * The node of the collapsed entailment graph is an equivalence class. 
 * This type of node will contain all text fragments that are equivalent from
 * the point of view of textual entailment.
 *
 */
public class EquivalenceClass {
		
	/** 
	 * The "canonical" (representative) text of the class
	 * This attribute is final - once it's set it cannot be changed 
	 */
	final String label;
	
	Set<EntailmentUnit> entailmentUnits = null;
	
	Map<String,Double> categoryConfidences = null; //added for use case 2
	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/

	/**
	 * Generates a new equivalence class from the input entailment unit
	 * @param eu -- an EntailmentUnit
	 */
	public EquivalenceClass(EntailmentUnit eu) {
		entailmentUnits = new HashSet<EntailmentUnit>();
		entailmentUnits.add(eu);
		label = eu.getText();
	}	
	
	/**
	 * Generates a new equivalence class from the input set of entailment units
	 * @param s_eu -- the set of EntailmentUnits
	 */
	public EquivalenceClass(Set<EntailmentUnit> s_eu) {
		entailmentUnits = new HashSet<EntailmentUnit>();
		entailmentUnits.addAll(s_eu);
		// Pick one element of the set and initialize the label associated with this node
		// We pick entailment unit with max frequency (if there are several entailment units with such frequency, sorter text is favored) 
		int frequency = 0;
		String labelCandidate=""; 
		for (EntailmentUnit candidateEntailmentUnit : entailmentUnits){
			if (candidateEntailmentUnit.getNumberOfCompleteStatements()>frequency){
				labelCandidate = candidateEntailmentUnit.getText();
				frequency = candidateEntailmentUnit.getNumberOfCompleteStatements();
			}
			else if (candidateEntailmentUnit.getNumberOfCompleteStatements()==frequency){ // if current label has the same frequency as the candidate entailment unit
				if (candidateEntailmentUnit.getText().length() < labelCandidate.length()) { // if the candidate text is shorter - make it the new label 
					labelCandidate =  candidateEntailmentUnit.getText();
				}
			}
		}
		label = labelCandidate; // assign the winner-candidate to be the label 
	}
	
	/**
	 * Generates a new equivalence class from the input set of entailment units, 
	 * and assigns the input text as the equivalence class's label
	 * 
	 * @param text -- the label ("canonical" text representing this equivalence class)
	 * @param s_eu -- the set of entailment units
	 */
	public EquivalenceClass(String text, Set<EntailmentUnit> s_eu) {
		entailmentUnits = new HashSet<EntailmentUnit>();
		entailmentUnits.addAll(s_eu);
		label = text;
	}

	/******************************************************************************************
	 * SETTERS/GETTERS
	 * ****************************************************************************************/

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
	
	/**
	 * added for use case 2. 
	 * 
	 * @return the categoryConfidences
	 */
	public Map<String,Double> getCategoryConfidences() {
		return categoryConfidences;
	}

	/**
	 * added for use case 2. 
	 * 
	 * @return the categoryConfidences
	 */
	public void setCategoryConfidences(Map<String,Double> categoryConfidences) {
		this.categoryConfidences = categoryConfidences;
	}

	/** The  method  returns the ids of interactions that contain entailment units covered by the equivalence class
	 * @return the set of interaction ids
	 */
	public Set<String> getInteractionIds(){
		Set<String> interactionIds = new HashSet<String>();
		for (EntailmentUnit eu : this.entailmentUnits){
			interactionIds.addAll(eu.getInteractionIds());
		}
		return interactionIds;		
	}
	

	/******************************************************************************************
	 * COMPARATORS
	 * ****************************************************************************************/

	/**
	 * Comparator to sort equivalence classes in descending order by their number of interactions
	 */
	public static class DescendingNumberOfInteractionsComparator implements Comparator<EquivalenceClass> {
	    @Override
	    public int compare(EquivalenceClass nodeA, EquivalenceClass nodeB) {
	        return -1*Integer.compare(nodeA.getInteractionIds().size(),nodeB.getInteractionIds().size());
	    }
	}
	
	/******************************************************************************************
	 * OTHER AUXILIARY METHODS
	 * ****************************************************************************************/

	/** Returns true if the input entailment unit is contained in the equivalence class.
	 * Otherwise returns false
	 * @param eu - the input entailment unit
	 * @return true/false
	 */
	public boolean containsEntailmentUnit(EntailmentUnit eu){
		return entailmentUnits.contains(eu);
	}
	
	/** Adds the input entailment unit to the set of entailment units contained in the equivalence class.
	 * If the set already contains this entailment unit, it will not be added.
	 * The method DOES NOT re-select the representative text (label) of the equivalence class. 
	 * @param eu - the input entailment unit
	 */
	public void add(EntailmentUnit eu){
		entailmentUnits.add(eu);
	}
	
	/** Adds the input entailment units to the set of entailment units contained in the equivalence class.
	 * If the set already contains any of the input entailment units, it will not be added.
	 * The method DOES NOT re-select the representative text (label) of the equivalence class. 
	 * @param s_eu - the set of input entailment units
	 */
	public void add(Set<EntailmentUnit> s_eu){
		entailmentUnits.addAll(s_eu);
	}
	
	/******************************************************************************************
	 * PRINT
	 * ****************************************************************************************/

	@Override
	public String toString(){
		String s = "\""+label.trim().replaceAll(" +", " ")+"\" ("+this.getInteractionIds().size()+" interactions) :\n";
		int i=1;
		List<EntailmentUnit> eus = new LinkedList<EntailmentUnit>(entailmentUnits);
		Collections.sort(eus, new EntailmentUnit.TextComparator());
		for (EntailmentUnit eu : eus){
			s+="\t"+i+")\""+eu.getTextWithoutDoubleSpaces()+"\"\n";
			i++;
		}
		s+="\n";
		return s;
	}
	
	/** Returns a string with the node in DOT format for outputting the graph
	 * @return the generated string
	 */
	public String toDOT(){
		String s = "\""+label.trim().replaceAll(" +", " ")+" (Total: "+this.getInteractionIds().size()+" interaction(s)):";
		for (EntailmentUnit eu : entailmentUnits){
			//if (eu.getText().equals(label)) continue;
			s+="\\n"+eu.getTextWithoutDoubleSpaces()+" ("+eu.getInteractionIds().size()+" interaction(s))";			
		}
		s+="\"";
		return s;
	}
	

	/******************************************************************************************
	 * METHODS FOR INTERNAL TESTING PURPOSES
	 * ****************************************************************************************/

	/** Returns a string with the node in DOT format for outputting the graph (with node Ids added to text) 
	 * @return the generated string
	 */
	public String toDOT(Map<String,String> nodeTextById){
		Map<String,Set<String>> nodeIdsByText = new HashMap<String, Set<String>>();
		for (String nodeId : nodeTextById.keySet()){
			String text = nodeTextById.get(nodeId);
			Set<String> idsOfText = new HashSet<String>();
			if (nodeIdsByText.containsKey(text)) idsOfText = new HashSet<String>(nodeIdsByText.get(text));
			idsOfText.add(nodeId.replace("\t","").replace("\n",""));
			nodeIdsByText.put(text.replace("\t","").replace("\n",""), idsOfText);
		}	
		
		String s = "\"";
		List<EntailmentUnit> eus = new LinkedList<EntailmentUnit>(entailmentUnits);
		Collections.sort(eus, new EntailmentUnit.TextComparator());
		for (EntailmentUnit eu : eus){
			//if (eu.getText().equals(label)) continue;
			s+=eu.getTextWithoutDoubleSpaces().replace("\t","").replace("\n","")+" "+nodeIdsByText.get(eu.getText())+"\\n";			
		}
		s+="\"";
		return s;
	}	
		
	public String toStringWithIds(Map<String,Set<String>> nodeIdsByText){
		String s="";
		List<EntailmentUnit> eus = new LinkedList<EntailmentUnit>(entailmentUnits);
		Collections.sort(eus, new EntailmentUnit.TextComparator());
		for (EntailmentUnit eu : eus){
			s+="\t"+eu.getTextWithoutDoubleSpaces().replace("\t","").replace("\n","")+"\t"+nodeIdsByText.get(eu.getText())+"\n";			
		}
		return s;
	}
	
}

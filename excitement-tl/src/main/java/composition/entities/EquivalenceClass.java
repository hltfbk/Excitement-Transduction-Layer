package composition.entities;

import java.util.List;

import decomposition.entities.EntailmentUnit;


/**
 * This class represents the data structure that holds an equivalence class. 
 * An equivalence class refers to a node in the collapsed entailment graph. 
 *  
 * @author Kathrin
 */

public class EquivalenceClass {

	private int ID;
	private String label;
	private List<EntailmentUnit> entailmentUnits;
	
	public int getID() {
		return ID;
	}
	
	public void setID(int iD) {
		ID = iD;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public List<EntailmentUnit> getEntailmentUnits() {
		return entailmentUnits;
	}
	
	public void setEntailmentUnits(List<EntailmentUnit> entailmentUnits) {
		this.entailmentUnits = entailmentUnits;
	}
		
	public int countAssociatedTextualInputs() {
		//number of textual inputs directly associated to this node 
		return 0;
	}

}

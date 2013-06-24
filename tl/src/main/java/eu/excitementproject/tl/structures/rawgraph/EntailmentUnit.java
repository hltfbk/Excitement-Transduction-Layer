package eu.excitementproject.tl.structures.rawgraph;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;

/*
 * I implemented this structure according to my proposal that 
 * the nodes of the WorkGraph are in fact equivalence classes.
 * I think this would make the implementation more flexible 
 * (as I said in our discussions, you could initialize the work graph 
 * from either a Fragment-style graph (with singleton nodes), or from
 * a collapsed graph). If you want the nodes to be singletons, from 
 * an implementation point of view nothing would change, the equivalence class 
 * can contain only one node.
 * 
 * If we extend the EntailmentUnitMention, the JCas attribute could be the "canonical"
 * JCas object for this node -- that means, when merging edges that connect EntailmentUnit-s,
 * we do not iterate over each of the nodes in the equivalence class, but just use the "canonical"
 * one. 
 * 
 */
/**
 * 
 * @author vivi@fbk & Lili Kotlerman & Kathrin
 * 
 * The node for the work graph is an EntailmentUnit
 *
 */
public class EntailmentUnit{

	protected String text;
	
	protected Set<String> completeStatementTexts;
	
	protected Set<EntailmentUnitMention> mentions = null;
	
	protected Set<String> interactionIds = null;
	

	protected int level = -1; // negative value means "unknown"
	
	
	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/
	/**
	 * Create a new entailment unit based on a single entailment unit mention
	 * The entailment unit mention is assumed to originate from a fragment graph
	 * 
	 * @param eum -- the entailment unit mention
	 * @param completeStatementText	-- String holding the completeStatementText of the fragment graph, from which the current entailment unit mention is taken
	 * @param interactionId -- String holding the interactionId of the fragment graph, from which the current entailment unit mention is taken
	 */
	public EntailmentUnit(EntailmentUnitMention eum, String completeStatementText, String interactionId) {
		setAttributes(eum, completeStatementText, interactionId);
	}


	/** Constructor to create entailment units from xml files 
	 * @param text
	 * @param completeStatementTexts
	 * @param mentions
	 * @param interactionIds
	 * @param level
	 */
	public EntailmentUnit(String text, Set<String> completeStatementTexts,
			Set<EntailmentUnitMention> mentions, Set<String> interactionIds,
			int level) {
		super();
		this.text = text;
		this.completeStatementTexts = completeStatementTexts;
		this.mentions = mentions;
		this.interactionIds = interactionIds;
		this.level = level;
	}


	
	
	/******************************************************************************************
	 * SETTERS/GERRETS
	 * ****************************************************************************************/

	/** Setter for all the attributes  
	 * @param eum - entailment unit mention
	 * @param completeStatementText - the text of the complete statement of the fragment graph from which the input entailment unit mention originated  
	 * @param interactionId - the interaction id of the fragment graph from which the input entailment unit mention originated
	 */
	private void setAttributes(EntailmentUnitMention eum, String completeStatementText, String interactionId){
		mentions = new HashSet<EntailmentUnitMention>();
		mentions.add(eum);
		text = eum.getText();
		level = eum.getLevel();	
		completeStatementTexts = new HashSet<String>();
		completeStatementTexts.add(completeStatementText);
		interactionIds = new HashSet<String>();
		interactionIds.add(interactionId);
		
	}

	/**
	 * @return the completeStatementTexts
	 */
	public Set<String> getCompleteStatementTexts() {
		return completeStatementTexts;
	}
	
	/**
	 * @return the interactionIds
	 */
	public Set<String> getInteractionIds() {
		return interactionIds;
	}
	
	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * 
	 * @return -- the "canonical" text fragment of the node
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * 
	 * @return -- the set of entailment unit mentions associated to this node
	 */
	public Set<EntailmentUnitMention> getMentions() {
		return mentions;
	}

	
	/**
	 * @return -- a set of text fragments corresponding to all the entailment unit mentions covered by this node
	 */
	public Set<String> getMentionTexts() {
		Set<String> texts = new HashSet<String>();
		
		for(EntailmentUnitMention n : mentions) {
			texts.add(n.getText());
		}
		
		return texts;
	}

	/** The method returns the size of the set of completeStatementTexts,
	 * i.e. the number of fragment graphs, in which the entailmet unit was seen.
	 * @return the number of fragment graphs, in which the entailmet unit was seen.
	 */
	public int getNumberOfTextualInputs() {
		return this.completeStatementTexts.size();
	}


	/******************************************************************************************
	 * OTHER AUXILIARY METHODS
	 * ****************************************************************************************/

	/** Returns true if the entailment unit is a base statement (has no modifiers, level = 0).
	 * Otherwise returns false.
	 * @return true/false
	 */
	public boolean isBaseStatement() {
		if (level==0) return true;
		return false;
	}

	/**
	 * Adds a new entailment unit mention to the entailment unit.
	 * In addition, adds the input completeStatementText to the set of completeStatementTexts of the fragment graphs in which the entailment unit was seen
	 * If such completeStatementText is already present in the set, it will not be added.
	 * @param entailmentUnitMention -- the node to be added
	 * @param completeStatementText - the completeStatementText of the fragment graph, from which the input entailmentUnitMention originated 
	 */
	public void addMention(EntailmentUnitMention entailmentUnitMention, String completeStatementText) {
		mentions.add(entailmentUnitMention);
		completeStatementTexts.add(completeStatementText);
	}

	
	/******************************************************************************************
	 * Override hashCode() and equals(). 
	 * ****************************************************************************************/

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 * Two entailment units are equal if and only if their canonical texts are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntailmentUnit other = (EntailmentUnit) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}

	public String getTextWithoutDoulbeSpaces(){
		return this.getText().trim().replaceAll(" +", " ");
	}

	/******************************************************************************************
	 * PRINT
	 * ****************************************************************************************/

	@Override
	public String toString(){
		String s="\""+this.getText()+"\"";
		if(isBaseStatement()) s+=" (base statement)";
		else if(this.level>0) s+= " ("+this.level+" mod.)";
		else s+= " (level unknown)";
		if (!this.completeStatementTexts.isEmpty()) s+=" " + this.completeStatementTexts.size()+ " complete statements";
		return s;
	}

	/******************************************************************************************
	 * METHODS FOR INTERNAL TESTING PURPOSES
	 * ****************************************************************************************/

	/**
	 * This constructor is only used for internal testing purposes (generate sample output)
	 * 
	 * @param textFragment -- generate node directly from the text fragment
	 * @param level -- the number of modifiers in the textFragment
	 * @param completeStatementText - the complete statement of the corresponding fragment graph 
	 */
	public EntailmentUnit(String textFragment, int level, String completeStatementText, String interactionId) {
		EntailmentUnitMention eum = new EntailmentUnitMention(textFragment);
		setAttributes(eum, completeStatementText, interactionId);
	}
	
	/**
	 * This constructor is only used for internal testing purposes (generate sample output with category id)
	 * 
	 * @param textFragment -- generate node directly from the text fragment
	 * @param level -- the number of modifiers in the textFragment
	 * @param completeStatementText - the complete statement of the corresponding fragment graph 
	 * @param category - category id 
	 */
	public EntailmentUnit(String textFragment, int level, String completeStatementText, String category, String interactionId) {
		EntailmentUnitMention eum = new EntailmentUnitMention(textFragment);
		eum.setCategoryId(category);
		setAttributes(eum, completeStatementText, interactionId);
	}
	

	
	/******************************************************************************************
	 * LEGACY 
	 * ****************************************************************************************/

	
/*	*//** Adds the input completeStatementText to the set of completeStatementTexts of the fragment graphs in which the entailment unit was seen
	 * If such completeStatementText is already present in the set, it will not be added.
	 * @param completeStatementText - the input completeStatementText 
	 *//*
	public void addCompleteStatement(String completeStatementText){
		completeStatementTexts.add(completeStatementText);
	}
*/
	
	/*	protected Set<Long> fragmentGraphIds;
	
	
	*//**
	 * @return the fragmentGraphIds
	 *//*
	public Set<Long> getFragmentGraphIds() {
		return fragmentGraphIds;
	}

	public boolean isFromFragmentGraph(long id) {
		if (fragmentGraphIds.contains(id)) return true;
		return false;
	}

	public void addFragmentGraphId(long id) {
		if (fragmentGraphIds.isEmpty()) fragmentGraphIds = new HashSet<Long>(); 
		if (!fragmentGraphIds.contains(id)) fragmentGraphIds.add(id);
	}
*/
	
	/*	*//**
	 * @param frequency the frequency to set
	 *//*
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
*/	
	
/*	*//**
	 * Increase the value of frequency by 1 
	 *//*
	public void incrementFrequency(){
		this.frequency++;
	}
*/
	
/*	public void setBaseStatement(boolean isBaseStatement) {
		this.level=0;
	}
*/	
}	


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
 * @author vivi@fbk & LiliKotlerman & Kathrin
 * 
 * The node for the work graph is an EntailmentUnit
 *
 */
public class EntailmentUnit{

	protected String text;
	
	protected Set<EntailmentUnitMention> mentions = null;
	
//	protected boolean isBaseStatement = false;
	
	protected int level = -1; // negative value means "unknown"
	
	/**
	 * initialize the JCas attribute -- make the first fragment added to the 
	 * EntailmentUnit object the "canonical" element
	 * 
	 * @param textCAS -- JCas object of the type defined for the TL
	 * @param start	-- start index of the fragment
	 * @param end -- end index of the fragment
	 */
	EntailmentUnit(EntailmentUnitMention eum) {
		
		mentions = new HashSet<EntailmentUnitMention>();
		mentions.add(eum);
		text = eum.getText();
		if(eum.getLevel()==0) setBaseStatement(true);
		level = eum.getLevel();		
	}

	/**
	 * Same as before -- this will be the "canonical" element
	 * 
	 * @param textFragment -- generate node directly from the text fragment
	 */
	public EntailmentUnit(String textFragment) {
		EntailmentUnitMention n = new EntailmentUnitMention(textFragment);
		
		mentions = new HashSet<EntailmentUnitMention>();
		mentions.add(n);		
		text = textFragment;
	}
	
	
	/**
	 * Same as before -- this will be the "canonical" element
	 * 
	 * @param textFragment -- generate node directly from the text fragment
	 * @param level -- the number of modifiers in the textFragment
	 */
	public EntailmentUnit(String textFragment, int level) {
		EntailmentUnitMention n = new EntailmentUnitMention(textFragment);
		
		mentions = new HashSet<EntailmentUnitMention>();
		mentions.add(n);		
		text = textFragment;
		this.level =level; 
	}
	
	/**
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}


	/**
	 * Add a new node to the equivalence class
	 * 
	 * @param n -- the node to be added
	 */
	public void addMention(EntailmentUnitMention n) {
		mentions.add(n);
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

	public boolean isBaseStatement() {
		if (level==0) return true;
		return false;
	}

	public void setBaseStatement(boolean isBaseStatement) {
		this.level=0;
	}

	@Override
	public String toString(){
		String s="\""+this.getText()+"\"";
		if(isBaseStatement()) s+=" (base statement)";
		else if(this.level>0) s+= " ("+this.level+" mod.)";
		else s+= " (level unknown)";
		return s;
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
	
	

}

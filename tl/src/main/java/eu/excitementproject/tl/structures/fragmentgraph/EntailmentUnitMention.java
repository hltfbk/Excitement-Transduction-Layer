package  eu.excitementproject.tl.structures.fragmentgraph;

import org.apache.uima.jcas.JCas;


/**
 * @author vivi@fbk
 * 
 * Vertex class for the FragmentGraph, we call it EntailmentUnitMention
 * 
 * Each such vertex consists of a base statement + a number of modifiers.
 * 
 * 
 */
public class EntailmentUnitMention {
	
	protected int documentId;
	protected int categoryId;
	protected String text;
	protected int level;
		
	/**
	 * textFragment -- a text fragment from which we construct a node (with the corresponding annotations)
	 */
	public EntailmentUnitMention(String textFragment) {
		text = textFragment;
		level = 0;
	}
	
	/**
	 * 
	 * @param textFragment -- a text fragment from which we construct a node (with the corresponding annotations)
	 * @param level
	 */
	public EntailmentUnitMention(String textFragment, int level) {
		text = textFragment;
		this.level = level;
	}

	public EntailmentUnitMention(JCas textCAS, int start, int end) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @return -- the text of the current node object
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * 
	 * @return -- the level of the node (i.e. -- how many modifiers it has) -- it might be useful for merging
	 */
	public int getLevel() {
		return level;
	}
	
	/**
	 * we could probably use methods to obtain various annotation layers of the object
	 * This depends on what information we keep in the node. 
	 */
	
	public String toString() {
		return getText();
	}
}

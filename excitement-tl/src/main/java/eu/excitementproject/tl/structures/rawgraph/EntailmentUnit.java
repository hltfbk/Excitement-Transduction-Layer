package package eu.excitementproject.tl.structures.rawgraph;

import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import fragmentGraph.EntailmentUnitMention;

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
 * @author vivi@fbk
 * 
 * The node for the work graph is an EntailmentUnit
 *
 */
public class EntailmentUnit{

	String text;
	
	Set<EntailmentUnitMention> node = null;
	
	/**
	 * initialize the JCas attribute -- make the first fragment added to the 
	 * EntailmentUnit object the "canonical" element
	 * 
	 * @param textCAS -- JCas object of the type defined for the TL
	 * @param start	-- start index of the fragment
	 * @param end -- end index of the fragment
	 */
	EntailmentUnit(JCas textCAS, int start, int end) {
		EntailmentUnitMention n = new EntailmentUnitMention(textCAS, start, end);
		
		node = new HashSet<EntailmentUnitMention>();
		node.add(n);
		text = textCAS.getDocumentText();
		
		// TODO Auto-generated constructor stub
	}

	/**
	 * Same as before -- this will be the "canonical" element
	 * 
	 * @param textFragment -- generate node directly from the text fragment
	 */
	EntailmentUnit(String textFragment) {
		EntailmentUnitMention n = new EntailmentUnitMention(textFragment);
		
		node = new HashSet<EntailmentUnitMention>();
		node.add(n);		
		text = textFragment;
	}
		
	/**
	 * Add a new node to the equivalence class
	 * 
	 * @param n -- the node to be added
	 */
	public void addNode(EntailmentUnitMention n) {
		node.add(n);
	}
	
	/**
	 * @return -- a set of text fragments corresponding to all the entailment unit mentions covered by this node
	 */
	public Set<String> getTextSet() {
		Set<String> texts = new HashSet<String>();
		
		for(EntailmentUnitMention n : node) {
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
	
}

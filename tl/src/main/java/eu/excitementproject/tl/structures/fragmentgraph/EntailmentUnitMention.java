package  eu.excitementproject.tl.structures.fragmentgraph;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.jcas.JCas;

import eu.excitement.type.tl.CategoryDecision;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitement.type.tl.ModifierPart;


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
	
	Set<ModifierAnnotation> modifiers = null;

	protected String text;
	protected Set<SimpleModifier> modifiersText;
	protected int level;
	
	protected int begin;
	protected int end;
	
	protected String categoryId = null;
	
	/**
	 * @param textFragment -- a text fragment from which we construct a node (with the corresponding annotations)
	 */
	public EntailmentUnitMention(String textFragment) {
		text = textFragment;
		level = 0;
		modifiersText = new HashSet<SimpleModifier>();
		begin = 0;
		end = text.length();
	}
	
	/**
	 * 
	 * @param textFragment -- a text fragment from which we construct a node (with the corresponding annotations)
	 * @param level
	 */
	public EntailmentUnitMention(String textFragment, Set<String> modifiers, Set<String> allModifiers) {
		text = textFragment;
		modifiersText = addModifiers(textFragment, modifiers); 		
		this.level = modifiersText.size();
		
		String chars = textFragment;
		for(String ma: allModifiers) {
			if (! modifiers.contains(ma)) {
				chars = removeModifier(chars,ma);
			}
		}
		text = chars;
//		text.trim().replaceAll(" +", " ");
		begin = 0;
		end = text.length();
	}


	/**
	 * 
	 * Build an entailmentUnit based on the (determined) fragment annotation in a document CAS object, 
	 * and the set of modifiers it should contain 
	 * 
	 * @param aJCas -- the document CAS object with all annotations
	 * @param frag -- the (determined) fragment to which this object will correspond
	 * @param mods -- the modifiers included in this object 
	 */
	public EntailmentUnitMention(JCas aJCas, FragmentAnnotation frag, Set<ModifierAnnotation> mods) {
		
		modifiers = mods;
		Set<String> modsText = new HashSet<String>();
		level = mods.size();
		begin = frag.getBegin();
		end = frag.getEnd();
		
		CharSequence chars = frag.getText();
		for(ModifierAnnotation ma: FragmentGraph.getFragmentModifiers(aJCas, frag)) {
			if (! mods.contains(ma)) {
				chars = removeModifier(chars,ma);
			} else {
				modsText.add(ma.getCoveredText());
			}
		}
		text = chars.toString();
//		text.trim().replaceAll(" +", " ");
		modifiersText = addModifiers(text,modsText);
		categoryId = getCategoryId(aJCas);
	}

	
	private Set<SimpleModifier> addModifiers(String textFragment,
			Set<String> mods) {
		Set<SimpleModifier> sms = new HashSet<SimpleModifier>();
		for(String s: mods) {	
			if (textFragment.contains(s)) {
				sms.add(new SimpleModifier(s,textFragment.indexOf(s),textFragment.indexOf(s)+s.length()));
			}
		}
		return sms;
	}
	
	private String getCategoryId(JCas aJCas) {		
		return ((CategoryDecision)aJCas.getDocumentAnnotationFs()).getCategoryId();
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
	
	public Set<SimpleModifier> getModifiers() {
		return modifiersText;
	}
	
	public Set<ModifierAnnotation> getModifierAnnotations() {
		return modifiers;
	}
	
	/**
	 * Replaces modifiers that should not appear in this fragment with spaces
	 * 
	 * @param chars
	 * @param ma
	 * @return
	 */
	private CharSequence removeModifier(CharSequence chars, ModifierAnnotation ma) {
		CharSequence chs = chars;
		ModifierPart mp;
		for (int i = 0; i < ma.getModifierParts().size(); i++) {
			mp = ma.getModifierParts(i);
			chs = chs.subSequence(0, mp.getBegin()) + StringUtils.repeat(" ",mp.getEnd()-mp.getBegin()) + chs.subSequence(mp.getEnd(),chs.length());
		}
		return chs;
	}

	/**
	 * Replaces modifiers that should not appear in this fragment with spaces
	 * 
	 * @param chars
	 * @param ma
	 * @return
	 */
	private String removeModifier(String chars, String ma) {
		int start = chars.indexOf(ma);
		return chars.subSequence(0, start)  
						+ StringUtils.repeat(" ",ma.length()) 
						+ chars.subSequence(start + ma.length(),chars.length());
	}

	
	/**
	 * we could probably use methods to obtain various annotation layers of the object
	 * This depends on what information we keep in the node. 
	 */	
	public String toString() {
		return getText();
	}
	
	public int getBegin() {
		return begin;
	}
	
	public int getEnd() {
		return end;
	}
	
	public boolean equals(EntailmentUnitMention eum) {
		return eum.text.matches(text);
	}
	
}

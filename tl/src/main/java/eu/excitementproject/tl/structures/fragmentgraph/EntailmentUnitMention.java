package  eu.excitementproject.tl.structures.fragmentgraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import eu.excitement.type.tl.CategoryAnnotation;
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
		if (allModifiers != null) {
			for(String ma: allModifiers) {
				if (! modifiers.contains(ma)) {
					chars = removeModifier(chars,ma);
				}
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
		categoryId = getCategoryId(aJCas,frag);
	}

	
	private Set<SimpleModifier> addModifiers(String textFragment,
			Set<String> mods) {
		Set<SimpleModifier> sms = new HashSet<SimpleModifier>();
		if (mods != null) { 
			for(String s: mods) {	
				if (textFragment.contains(s)) {
					sms.add(new SimpleModifier(s,textFragment.indexOf(s),textFragment.indexOf(s)+s.length()));
				}
			}
		}
		return sms;
	}
	
	
	@SuppressWarnings("unused")
	private String getCategoryId(JCas aJCas) {		
		AnnotationIndex<Annotation> catIndex = aJCas.getAnnotationIndex(CategoryAnnotation.type);

		if (catIndex != null && catIndex.size() > 0) {
			Iterator<Annotation> catIt = catIndex.iterator(); 
		
			CategoryAnnotation cat = (CategoryAnnotation) catIt.next(); 

			// do a loop over all category decision, in real case. 
			CategoryDecision d = cat.getCategories(0);
			return d.getCategoryId();
		}
		return "N/A";
	}
	
	
	private String getCategoryId(JCas aJCas, FragmentAnnotation frag) {
		Set<CategoryAnnotation> cas = FragmentGraph.getFragmentCategories(aJCas, frag);
		if (cas != null && !cas.isEmpty()) {
			for (CategoryAnnotation cat: cas) {
				// assume one category annotation per fragment (or document, makes no difference here)
				if (cat != null && cat.getCategories().size() > 0) {
					return cat.getCategories(0).getCategoryId();
				}
			}
		}
		return "N/A";
	}
	
	
	public String getCategoryId(){
		return categoryId;
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
	 * 
	 * @return the set of modifiers in a simplified representation (text and span)
	 */
	public Set<SimpleModifier> getModifiers() {
		return modifiersText;
	}
	
	/**
	 * 
	 * @return the set of modifier annotations 
	 */
	public Set<ModifierAnnotation> getModifierAnnotations() {
		return modifiers;
	}
	
	/**
	 * Replaces modifiers that should not appear in this fragment with spaces
	 * 
	 * @param chars -- a sequence of characters
	 * @param ma -- a modifier annotation from the CAS
	 * @return the sequence of characters without the text corresponding to the given modifier annotation 
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
	 * @param chars -- a string
	 * @param ma -- a string corresponding to a modifier in chars
	 * @return the string (chars) without the modifier (ma)
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
		return getText() + " ( category: " + categoryId + ", span: " + begin + " -- " + end + ") ";
	}
	
	/**
	 * 
	 * @return the starting position of the text corresponding to this node, relative to the "parent" document  
	 */
	public int getBegin() {
		return begin;
	}
	
	/**
	 * 
	 * @return the end position of the text corresponding to this node, relative to the "parent" document
	 */
	public int getEnd() {
		return end;
	}
	
	public boolean equals(EntailmentUnitMention eum) {
		return eum.text.matches(text);
	}
	
}

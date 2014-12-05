package eu.excitementproject.tl.structures.fragmentgraph;

import eu.excitementproject.tl.laputils.CASUtils.Region;

/**
 * A class for a simple kind of modifier (text and span)
 * 
 * @author Vivi Nastase
 *
 */
public class SimpleModifier extends Region {

	private String modifier;
	
	public SimpleModifier(String text, int start, int end) {
		super(start, end);
		modifier = text;
	}
	
	public String getText() {
		return modifier;
	}
	
	public int getStart() {
		return getBegin();
	}
}

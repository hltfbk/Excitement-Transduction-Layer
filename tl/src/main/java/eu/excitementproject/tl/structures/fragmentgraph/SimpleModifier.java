package eu.excitementproject.tl.structures.fragmentgraph;

import eu.excitementproject.tl.decomposition.fragmentannotator.Span;

public class SimpleModifier extends Span {

	private String modifier;
	
	public SimpleModifier(String text, int start, int end) {
		super(start, end);
		modifier = text;
	}
	
	public String getText() {
		return modifier;
	}
	
	public int getStart() {
		return this.begin;
	}
}

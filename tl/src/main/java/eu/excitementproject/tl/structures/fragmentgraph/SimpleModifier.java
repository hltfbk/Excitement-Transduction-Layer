package eu.excitementproject.tl.structures.fragmentgraph;

public class SimpleModifier {

	private String modifier;
	private int start;
	private int end;
	
	public SimpleModifier(String text, int start, int end) {
		modifier = text;
		this.start = start;
		this.end = end;
	}
	
	public String getText() {
		return modifier;
	}
	
	public int getStart() {
		return start;
	}
	
	public int getEnd() {
		return end;
	}
}

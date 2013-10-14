package eu.excitementproject.tl.decomposition.fragmentannotator;

public class Span {

	protected int begin;
	protected int end;
	
	public Span(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
	
	
	public int getBegin() {
		return begin;
	}
	
	public int getEnd() {
		return end;
	}

}

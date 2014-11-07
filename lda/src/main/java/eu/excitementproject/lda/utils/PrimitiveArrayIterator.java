package eu.excitementproject.lda.utils;

public class PrimitiveArrayIterator <T> {
	
	private T[] array;
	private int index;
	
	public PrimitiveArrayIterator (T[] array) {
		
		this.array = array;
		this.index = 0;
		
	}
	
	public boolean hasNext() {
		return (this.index < this.array.length);  
	}
	
	public T getNext() {
		T next = array[this.index];
		this.index++;
		return next;
	}
	
}

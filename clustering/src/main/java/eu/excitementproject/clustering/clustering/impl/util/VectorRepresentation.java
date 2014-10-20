package eu.excitementproject.clustering.clustering.impl.util;

import java.util.Comparator;
import java.util.Map;

/**
 * @author Lili Kotlerman
 *
 */
public class VectorRepresentation {
	private Integer id;
	private Map<String,Double> vector;
	
	public VectorRepresentation(int id, Map<String, Double> vector) {
		this.id = id;
		this.vector = vector;
	}
	
	public Integer getId() {
		return id;
	}

	public Map<String, Double> getVector() {
		return vector;
	}	
	
	public String toString(){
		String s = "docId="+String.valueOf(id)+": [";
		for (String element : vector.keySet()){
			s+="("+element+":"+vector.get(element)+"),";
		}
		s+="]";
		s.replace(",]","]");
		return s;
	}
	
	/** 
	 * @param anotherVector
	 * @return true if the vector has the same set of words as the given another vector, and false otherwise  
	 */
	public boolean hasSameSetOfWords(VectorRepresentation anotherVector){
		if (anotherVector.getVector().size()!=this.vector.size()) return false;
		for (String word : anotherVector.getVector().keySet()){
			if (!this.vector.containsKey(word)) return false;
		}
		return true;		
	}
	
	/**
	 * Comparator to sort VectorRepresentation by their vector lengths 
	 */
	public static class VectorLengthComparator implements Comparator<VectorRepresentation> {
	    @Override
	    public int compare(VectorRepresentation vA, VectorRepresentation vB) {
	    	Integer sizeA = vA.getVector().size();
	    	Integer sizeB = vB.getVector().size();	    	
	    	return sizeA.compareTo(sizeB);
	    }
	}


	
	

}

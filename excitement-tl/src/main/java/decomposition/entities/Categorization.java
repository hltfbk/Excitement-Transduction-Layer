package decomposition.entities;

/**
 * Categorization of an entailment unit to a category (including confidence score). 
 * 
 * @author kaei01
 *
 */

public class Categorization implements Comparable<Categorization> {
	
	private int categoryId;
	private double confidence;

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public int compareTo(Categorization o) {
		// TODO Auto-generated method stub
		return 0;
	}

}

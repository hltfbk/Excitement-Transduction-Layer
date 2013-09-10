package eu.excitementproject.tl.evaluation;

public class EvaluationMeasures {
	Double recall;
	Double precision;

	/**
	 * @return the recall
	 */
	public Double getRecall() {
		return recall;
	}

	/**
	 * @param recall the recall to set
	 */
	public void setRecall(double recall) {
		this.recall = recall;
	}
	/**
	 * @return the precision
	 */
	public Double getPrecision() {
		return precision;
	}
	/**
	 * @param precision the precision to set
	 */
	public void setPrecision(double precision) {
		this.precision = precision;
	}
	/**
	 * @return the f1 measure
	 */
	public Double getF1() {
		if ((precision == null)||(recall == null)) return null;		
		return 2*precision*recall/(precision+recall);		
	}

}

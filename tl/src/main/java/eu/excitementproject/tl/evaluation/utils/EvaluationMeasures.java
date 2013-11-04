package eu.excitementproject.tl.evaluation.utils;

/**
 * This class is a container for the values of Recall, Precision and F1 calculated by different evaluators
 * @author Lili Kotlerman 
 */
public class EvaluationMeasures {
	Double recall;
	Double precision;
	
	public EvaluationMeasures() {
		this.recall = null;
		this.precision = null;
	}

	
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

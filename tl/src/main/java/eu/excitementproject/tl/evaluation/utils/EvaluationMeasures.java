package eu.excitementproject.tl.evaluation.utils;

/**
 * This class is a container for the values of evaluation measures calculated by different evaluators
 * @author Lili Kotlerman 
 */
public class EvaluationMeasures {
	Double recall;
	Double precision;
	Double purity;
	Double randIndex;
	
	public EvaluationMeasures() {
		this.recall = null;
		this.precision = null;
		this.purity = null;
		this.randIndex = null;
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

	public Double getPurity() {
		return purity;
	}

	public void setPurity(Double purity) {
		this.purity = purity;
	}

	public Double getRandIndex() {
		return randIndex;
	}

	public void setRandIndex(Double randIndex) {
		this.randIndex = randIndex;
	}
}

package eu.excitementproject.tl.evaluation.utils;

/**
 * This class is a container for the values of evaluation measures calculated by different evaluators
 * @author Lili Kotlerman 
 */
public class ExtendedEvaluationMeasures extends EvaluationMeasures{
	Double purity;
	Double randIndex;
	
	public ExtendedEvaluationMeasures() {
		super();
		this.purity = null;
		this.randIndex = null;
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

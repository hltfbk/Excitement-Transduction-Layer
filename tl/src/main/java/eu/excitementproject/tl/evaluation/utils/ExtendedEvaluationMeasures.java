package eu.excitementproject.tl.evaluation.utils;

import java.util.List;

/**
 * This class is a container for the values of evaluation measures calculated by different evaluators
 * 
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

	
	
	public ExtendedEvaluationMeasures(int tp, int fp, int tn, int fn) {
		super(tp, fp, tn, fn);
	}



	public ExtendedEvaluationMeasures(List<Integer> scores) {
		super(scores);
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
	
	public String toString(){
		return super.toString()+";\tPurity="+getPurity()+";\tRandIndex="+getRandIndex();
	}
}

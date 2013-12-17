package eu.excitementproject.tl.evaluation.utils;

import java.util.List;

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

	
	public EvaluationMeasures(int tp, int fp, int tn, int fn) {
		precision = 1.0 * tp / (tp + fp);
		recall = 1.0 * tp / (tp + fn);
	}
	
	
	/**
	 * The scores is the list of TP, FP, TN, FN
	 * @param scores
	 */
	public EvaluationMeasures(List<Integer> scores) {
		int tp = scores.get(0);
		int fp = scores.get(1);
		int fn = scores.get(3);
		precision = 1.0 * tp / (tp + fp);
		recall = 1.0 * tp / (tp + fn);
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
	
	@Override
	public String toString(){
		return "Recall="+getRecall()+";\tPrec="+getPrecision()+";\tF1="+getF1();		
	}
}

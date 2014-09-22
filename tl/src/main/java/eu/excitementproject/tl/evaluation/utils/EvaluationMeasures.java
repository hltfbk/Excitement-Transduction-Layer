package eu.excitementproject.tl.evaluation.utils;

import java.util.List;

/**
 * This class is a container for the values of Recall, Precision and F1 calculated by different evaluators
 * @author Lili Kotlerman 
 */
public class EvaluationMeasures {
	Double recall;
	Double precision;
		
	// memorize for future analysis
	Integer truePositives;
	Integer falsePositives;
	Integer trueNegatives;
	Integer falseNegatives;
	
	private void setRawNumbers(int tp, int fp, int tn, int fn){
		truePositives=tp;
		falsePositives=fp;
		trueNegatives=tn;
		falseNegatives=fn;
	}
	

	
	public EvaluationMeasures() {
		this.recall = null;
		this.precision = null;
		this.truePositives = null;
		this.falsePositives = null;
		this.trueNegatives = null;
		this.falseNegatives = null;
	}



	public EvaluationMeasures(int tp, int fp, int tn, int fn) {
		precision = 1.0 * tp / (tp + fp);
		recall = 1.0 * tp / (tp + fn);
		
		setRawNumbers(tp, fp, tn, fn);
	}
	
	
	/**
	 * The scores is the list of TP, FP, TN, FN
	 * @param scores
	 */
	public EvaluationMeasures(List<Integer> scores) {
		int tp = scores.get(0);
		int fp = scores.get(1);
		int tn = scores.get(2);
		int fn = scores.get(3);
				
		precision = 1.0 * tp / (tp + fp);
		recall = 1.0 * tp / (tp + fn);

		setRawNumbers(tp, fp, tn, fn);
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

	/**
	 * @return the truePositives
	 */
	public int getTruePositives() {
		return truePositives;
	}

	/**
	 * @return the falsePositives
	 */
	public int getFalsePositives() {
		return falsePositives;
	}

	/**
	 * @return the trueNegatives
	 */
	public int getTrueNegatives() {
		return trueNegatives;
	}

	/**
	 * @return the falseNegatives
	 */
	public int getFalseNegatives() {
		return falseNegatives;
	}
	
}

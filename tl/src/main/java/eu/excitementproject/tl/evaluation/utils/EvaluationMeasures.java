package eu.excitementproject.tl.evaluation.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is a container for the values of Recall, Precision and F1 calculated by different evaluators
 * 
 * @author Lili Kotlerman 
 */
public class EvaluationMeasures {
	Double recall;
	Double precision;
	
	Double averagePrecision;
		
	// memorize for future analysis
	Integer truePositives;
	Integer falsePositives;
	Integer trueNegatives;
	Integer falseNegatives;
	
	// memorize for micro-averaging across clusters
	Integer recallDenominator;
	Integer precisionDenominator;
	
	// for accuracy
	Double accuracy;
	Integer correctDecisions;
	Integer allDecisions;
	
	private void setRawNumbers(int tp, int fp, int tn, int fn){
		truePositives=tp;
		falsePositives=fp;
		trueNegatives=tn;
		falseNegatives=fn;
	}
	

	Set<String> falsePositiveExamples;
	Set<String> falseNegativeExamples;
	
	
	
	public EvaluationMeasures(EvaluationMeasures eval){
		this();
    	this.falseNegatives=eval.falseNegatives;
    	this.falsePositives = eval.falsePositives;
    	this.trueNegatives = eval.trueNegatives;
    	this.truePositives = eval.truePositives;
    	this.precision = eval.precision;
    	this.recall = eval.recall;
    	this.recallDenominator=eval.recallDenominator;
    	this.precisionDenominator = eval.precisionDenominator;
    	this.falseNegativeExamples = eval.falseNegativeExamples;
    	this.falsePositiveExamples = eval.falsePositiveExamples;
    	this.allDecisions = eval.allDecisions;
    	this.correctDecisions = eval.correctDecisions;
    	this.accuracy = eval.accuracy;	
    	this.averagePrecision = eval.averagePrecision;
	}

	public EvaluationMeasures() {
		this.recall = null;
		this.precision = null;
		this.truePositives = null;
		this.falsePositives = null;
		this.trueNegatives = null;
		this.falseNegatives = null;
		this.recallDenominator = null;
		this.precisionDenominator = null;
		this.falsePositiveExamples = new HashSet<String>();
		this.falseNegativeExamples = new HashSet<String>();
		this.correctDecisions=null;
		this.allDecisions=null;
		this.accuracy = null;
		this.averagePrecision = null;
	}



	public EvaluationMeasures(int tp, int fp, int tn, int fn) {
		this();
		precision = 1.0 * tp / (tp + fp);
		recall = 1.0 * tp / (tp + fn);
		
		if (tp+fp==0) precision = 1.0;
		if (tp+fn==0) recall = 1.0;
		
		setRawNumbers(tp, fp, tn, fn);
}
	
	public EvaluationMeasures(int tp, int recallDenominator, int precisionDenominator) {
		this();
		this.truePositives = tp;
		this.recallDenominator = recallDenominator;
		this.precisionDenominator = precisionDenominator;
		
		if (precisionDenominator == 0) precision = 1.0;
		else precision = 1.0 * tp / precisionDenominator;
		
		if (recallDenominator == 0) recall = 1.0;
		else recall = 1.0 * tp / recallDenominator;		
}
	
	/**
	 * The scores is the list of TP, FP, TN, FN
	 * @param scores
	 */
	public EvaluationMeasures(List<Integer> scores) {
		this();
		
		int tp = scores.get(0);
		int fp = scores.get(1);
		int tn = scores.get(2);
		int fn = scores.get(3);
						
		precision = 1.0 * tp / (tp + fp);
		recall = 1.0 * tp / (tp + fn);

		if (tp+fp==0) precision = 1.0;
		if (tp+fn==0) recall = 1.0;

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
	public Integer getTruePositives() {
		return truePositives;
	}

	public void setTruePositives(int tp) {
		truePositives=tp;
	}

	/**
	 * @return the falsePositives
	 */
	public Integer getFalsePositives() {
		return falsePositives;
	}

	/**
	 * @return the trueNegatives
	 */
	public Integer getTrueNegatives() {
		return trueNegatives;
	}

	/**
	 * @return the falseNegatives
	 */
	public Integer getFalseNegatives() {
		return falseNegatives;
	}



	public Integer getRecallDenominator() {
		return recallDenominator;
	}



	public void setRecallDenominator(int recallDenominator) {
		this.recallDenominator = recallDenominator;
	}



	public Integer getPrecisionDenominator() {
		return precisionDenominator;
	}



	public void setPrecisionDenominator(int precisionDenominator) {
		this.precisionDenominator = precisionDenominator;
	}
	
	public Set<String> getFalsePositiveExamples() {
		return falsePositiveExamples;
	}

	public Set<String> getFalsePositiveExamples(int limit) {
		
		if (falsePositiveExamples.size()<=limit) return falsePositiveExamples;
		
		Set<String> examples = new HashSet<String>(falsePositiveExamples);
		for (String s : falsePositiveExamples){
			examples.remove(s);
			if (examples.size()==limit) return examples;
		}
		
		return new HashSet<String>();
	}


	public Set<String> getFalseNegativeExamples(int limit) {
		
		if (falseNegativeExamples.size()<=limit) return falseNegativeExamples;
		
		Set<String> examples = new HashSet<String>(falseNegativeExamples);
		for (String s : falseNegativeExamples){
			examples.remove(s);
			if (examples.size()==limit) return examples;
		}
		
		return new HashSet<String>();
	}
	
	public Set<String> getFalseNegativeExamples() {
		return falseNegativeExamples;
	}
	
	public void addFalsePositiveExample(String fpe){
		falsePositiveExamples.add(fpe);
	}

	public void addFalseNegativeExample(String fne){
		falseNegativeExamples.add(fne);
	}



	public Double getAccuracy() {
		return accuracy;
	}


	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

	public void setAccuracy(int correctDecisions, int allDecisions) {
		this.correctDecisions = correctDecisions;
		this.allDecisions = allDecisions;
		if (allDecisions==0) accuracy = 1.0;
		else accuracy = 1.0*correctDecisions / allDecisions;
	}



	public Integer getCorrectDecisions() {
		return correctDecisions;
	}



	public Integer getAllDecisions() {
		return allDecisions;
	}

	public Double getAveragePrecision() {
		return averagePrecision;
	}

	public void setAveragePrecision(Double averagePrecision) {
		this.averagePrecision = averagePrecision;
	}
	
	
	
	
}

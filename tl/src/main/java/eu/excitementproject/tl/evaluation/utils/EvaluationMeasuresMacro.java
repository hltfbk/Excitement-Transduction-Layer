package eu.excitementproject.tl.evaluation.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for macro-evaluation. Gathers the evaluation measures per instance, and computes macro precision, recall and f1score 
 * 
 * @author vivi@fbk
 *
 */
public class EvaluationMeasuresMacro {
	
	// macro measures
	Double recall = null;
	Double precision = null;
	Double fscore = null;
	
	// the list of per-instance evaluation measures
	List<EvaluationMeasures> perInstanceEvals;
	
	public EvaluationMeasuresMacro() {
		perInstanceEvals = new ArrayList<EvaluationMeasures>();
	}
	
	public EvaluationMeasuresMacro(EvaluationMeasures em) {
		this();
		perInstanceEvals.add(em);
	}
	
	
	public void addScores(EvaluationMeasures em) {
		perInstanceEvals.add(em);
	}
	
	
	public void addScores(List<EvaluationMeasures> emls) {
		perInstanceEvals.addAll(emls);
	}
	
	
	public Double getPrecision() {
		if (precision == null) 
			computePrecision();
				
		return precision;
	}
	
	
	public Double getRecall() {
		if (recall == null) 
			computeRecall();
		
		return recall;
	}
	
	
	public Double getFscore() {
		if (fscore == null) 
			computeFscore();
		
		return fscore;
	}
	
	private void computePrecision() {		
		precision = 0.0;
		
		for (EvaluationMeasures em : perInstanceEvals) {
			precision += em.getPrecision();
		}		
		precision = precision / perInstanceEvals.size();
	}
	
	private void computeRecall() {
		recall = 0.0;
		
		for (EvaluationMeasures em: perInstanceEvals) {
			recall += em.getRecall();
		}
		recall = recall / perInstanceEvals.size();
	}
	
	
	private void computeFscore() {
		
		if (precision == null)
			computePrecision();
		
		if (recall == null)
			computeRecall();
		
		fscore = (2 * precision * recall) / (precision + recall);		
	}
	
	public int getNrOfInstances() {
		return perInstanceEvals.size();
	}
}

package eu.excitementproject.tl.evaluation.utils;

import java.util.ArrayList;
import java.util.List;

public class EvaluationMeasuresMacro {
	
	Double recall = null;
	Double precision = null;
	Double fscore = null;
	
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

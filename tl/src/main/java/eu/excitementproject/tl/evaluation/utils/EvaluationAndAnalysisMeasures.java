package eu.excitementproject.tl.evaluation.utils;

import java.util.List;

/**
 * This class contains EvaluationMeasures, as well as various numbers used for analysis

 * @author Lili Kotlerman 
 */
public class EvaluationAndAnalysisMeasures extends EvaluationMeasures{
	
	Integer violations; // number of transitivity violations in the evaluated graph
	Integer extraFGedges; // number of edges added to FGs (non-entailing in FG, entailing in the evaluated graph)
	Integer missingFGedges; // number of edges missing from FGs (entailing in FG, non-entailing in the evaluated graph)
	Integer edaCalls; // number of EDA calls made to build the evaluated graph
			
	
	public EvaluationAndAnalysisMeasures() {
		super();
		this.violations = null;
		this.extraFGedges = null;
		this.missingFGedges = null;
		this.edaCalls = null;
	}

	

	public EvaluationAndAnalysisMeasures(int tp, int fp, int tn, int fn) {
		super(tp, fp, tn, fn);
	}



	public EvaluationAndAnalysisMeasures(List<Integer> scores) {
		super(scores);
	}



	@Override
	public String toString(){
		return super.toString()+"\nVolations="+getViolations()+";\tExtraFgEdges="+getExtraFGedges()+";\tMissingFgEdges="+getMissingFGedges()+";\tEda calls="+getEdaCalls();		
	}



	public Integer getViolations() {
		return violations;
	}



	public void setViolations(Integer violations) {
		this.violations = violations;
	}



	public Integer getExtraFGedges() {
		return extraFGedges;
	}



	public void setExtraFGedges(Integer wrongFGedges) {
		this.extraFGedges = wrongFGedges;
	}



	public Integer getEdaCalls() {
		return edaCalls;
	}



	public void setEdaCalls(Integer edaCalls) {
		this.edaCalls = edaCalls;
	}



	public Integer getMissingFGedges() {
		return missingFGedges;
	}



	public void setMissingFGedges(Integer missingFGedges) {
		this.missingFGedges = missingFGedges;
	}
	
	
	
}

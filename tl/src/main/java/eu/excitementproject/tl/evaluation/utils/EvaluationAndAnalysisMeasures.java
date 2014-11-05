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
	Integer overallEdges;		

	public void init(){
		this.violations = 0;
		this.extraFGedges = 0;
		this.missingFGedges = 0;
		this.edaCalls = 0;
		this.overallEdges=0;
	}
	
	public EvaluationAndAnalysisMeasures() {
		super();
		init();
	}

	

	public EvaluationAndAnalysisMeasures(int tp, int recallDenominator,
			int precisionDenominator) {
		super(tp, recallDenominator, precisionDenominator);
		init();
	}

	public EvaluationAndAnalysisMeasures(int tp, int fp, int tn, int fn) {
		super(tp, fp, tn, fn);
		init();
	}

    public EvaluationAndAnalysisMeasures (EvaluationMeasures eval){
    	super(eval);
    	init();
    }

	public EvaluationAndAnalysisMeasures(List<Integer> scores) {
		super(scores);
		init();
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

	public Integer getOverallEdges() {
		return overallEdges;
	}

	public void setOverallEdges(Integer overallEdges) {
		this.overallEdges = overallEdges;
	}
	
	
	
}

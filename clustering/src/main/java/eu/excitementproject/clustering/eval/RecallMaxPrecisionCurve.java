package eu.excitementproject.clustering.eval;

import java.util.List;

/**
 * Recall-Precision curve generated using the following rule: 
 * for each standard recall value <code>r</code> from 0.0 to 1.0 with 0.1 increments, take the maximum precision obtained at any actual recall value greater or equal to <code>r</code>
 * @author Lili Kotlerman
 *
 */
public class RecallMaxPrecisionCurve extends RecallPrecisionCurve {
	
	List<Double> standardRecallValues;
	List<EvaluationValues> evaluationResults;
	List<List<Double>> interpolatedPrecisionValues;
	

	public RecallMaxPrecisionCurve(List<EvaluationValues> evaluationResults) {
		super(evaluationResults);
	}


	public Double getInterpolaredRecallValue(Double standardRecallValue, List<Double> precisionValues, List<Double> actualRecallValues){
		Double interP=0.0;
		for (int i=0; i<actualRecallValues.size(); i++){
			Double r = actualRecallValues.get(i);
			Double p = precisionValues.get(i);
			if (r > standardRecallValue) {
				if(interP < p) interP = p;
			}
		}
		return interP;
	}

}

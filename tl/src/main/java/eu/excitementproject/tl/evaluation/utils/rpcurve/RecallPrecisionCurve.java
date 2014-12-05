package eu.excitementproject.tl.evaluation.utils.rpcurve;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * Recall-Precision curve
 * 
 * @author Lili Kotlerman
 *
 */
public abstract class RecallPrecisionCurve {
	List<Double> standardRecallValues;
	List<ValuesForRecallPrecisionCurve> evaluationResults;
	List<List<Double>> interpolatedPrecisionValues;
	
	public RecallPrecisionCurve(List<ValuesForRecallPrecisionCurve> evaluationResults) {
		standardRecallValues = new LinkedList<Double>();
		for (double r=0.0; r<1.05; r+=0.1){
			standardRecallValues.add(r);
		}
		
		this.evaluationResults = evaluationResults;
		
		interpolatedPrecisionValues = new LinkedList<List<Double>>();
		for (ValuesForRecallPrecisionCurve res : evaluationResults){
			interpolatedPrecisionValues.add(getInterpolatedPrecisionValues(res.getPrecisionValues(), res.getRecallValues()));
		}
	}

	public List<Double> getInterpolatedPrecisionValues(List<Double> precisionValues, List<Double> actualRecallValues){
		List<Double> interpolatedPrecisionValues = new LinkedList<Double>();
		for (Double r : standardRecallValues){
			interpolatedPrecisionValues.add(getInterpolaredRecallValue(r, precisionValues, actualRecallValues));
		}
		return interpolatedPrecisionValues;
	}

	public abstract Double getInterpolaredRecallValue(Double standardRecallValue, List<Double> precisionValues, List<Double> actualRecallValues);

	public List<Double> getStandardRecallValues() {
		return standardRecallValues;
	}

	public List<List<Double>> getInterpolatedPrecisionValues() {
		return interpolatedPrecisionValues;
	}

	public String print(){
		String s = "Recall";
		for (ValuesForRecallPrecisionCurve res : evaluationResults){
			s+=("\t"+res.getExperimentName());
		}
		s+="\n";
		for (int i=0; i < standardRecallValues.size(); i++){
			s+=(printRecall(standardRecallValues.get(i)));
			for (List<Double> experimentInterPrecValues : interpolatedPrecisionValues){
				s+=("\t"+printPrec(experimentInterPrecValues.get(i)));
			}
			s+="\n";
		}
		System.out.println(s);
		return s;
	}
	
	public String printRecall(Double r){
		 return BigDecimal.valueOf(r).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
	}

	public String printPrec(Double p){
		 return BigDecimal.valueOf(p).setScale(3, BigDecimal.ROUND_HALF_UP).toString();
	}

}

package eu.excitementproject.clustering.experiments.eval;

import java.util.LinkedList;
import java.util.List;

/**
 * Recall-Precision curve generated using the following rule: for each standard recall value <code>r</code> from 0.0 to 1.0 with 0.1 increments, take the maximum precision obtained at any actual recall value greater or equal to <code>r</code>
 * @author Lili Kotlerman
 *
 */
public class RecallPrecisionPairCurve {
	
	List<Double> standardRecallValues;
	List<Double> interpolatedPrecisionValuesA;
	List<Double> interpolatedPrecisionValuesB;
	
	public RecallPrecisionPairCurve(List<Double> precisionValuesA, List<Double> actualRecallValuesA, List<Double> precisionValuesB, List<Double> actualRecallValuesB) {
		standardRecallValues = new LinkedList<Double>();
		for (double r=0.0; r<1.05; r+=0.1){
			standardRecallValues.add(r);
		}
		interpolatedPrecisionValuesA = getInterpolatedPrecisionValues(precisionValuesA, actualRecallValuesA);
		interpolatedPrecisionValuesB = getInterpolatedPrecisionValues(precisionValuesB, actualRecallValuesB);
	}

	public List<Double> getInterpolatedPrecisionValues(List<Double> precisionValues, List<Double> actualRecallValues){
		List<Double> interpolatedPrecisionValues = new LinkedList<Double>();
		for (Double r : standardRecallValues){
			interpolatedPrecisionValues.add(getInterpolaredRecallValue(r, precisionValues, actualRecallValues));
		}
		return interpolatedPrecisionValues;
	}

	public Double getInterpolaredRecallValue(Double standardRecallValue, List<Double> precisionValues, List<Double> actualRecallValues){
		Double interP=0.0;
		for (int i=0; i<actualRecallValues.size(); i++){
			Double r = actualRecallValues.get(i);
			Double p = precisionValues.get(i);
			if (r > standardRecallValue) break;
			if(interP < p) interP = p;
		}
		return interP;
	}

	public List<Double> getStandardRecallValues() {
		return standardRecallValues;
	}

	public List<Double> getInterpolatedPrecisionValuesA() {
		return interpolatedPrecisionValuesA;
	}

	public List<Double> getInterpolatedPrecisionValuesB() {
		return interpolatedPrecisionValuesB;
	}

	public void print(){
		System.out.println("Recall\tPrec A \tPrec B");
		for (int i=0; i < standardRecallValues.size(); i++){
			System.out.println(standardRecallValues.get(i)+"\t"+interpolatedPrecisionValuesA.get(i)+"\t"+interpolatedPrecisionValuesB.get(i));
		}
	}
	
	
}

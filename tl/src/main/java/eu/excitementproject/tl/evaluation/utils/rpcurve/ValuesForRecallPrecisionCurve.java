package eu.excitementproject.tl.evaluation.utils.rpcurve;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores Recall-Precision values of different runs of a system, as well as MAP if given 
 * @author Lili Kotlerman
 *
 */
public class ValuesForRecallPrecisionCurve {
	
	String experimentName;
	List<Double> precisionValues;
	List<Double> recallValues;
	List<Double> mapValues; //mean average precision - not a must to specify

	public ValuesForRecallPrecisionCurve(String experimentName, List<Double> precisionValues,
			List<Double> recallValues, List<Double> mapValues) {
		this.experimentName = experimentName;
		this.precisionValues = precisionValues;
		this.recallValues = recallValues;
		this.mapValues = mapValues;
	}

	public ValuesForRecallPrecisionCurve(String experimentName, List<Double> precisionValues,
			List<Double> recallValues) {
		this(experimentName, precisionValues, recallValues, new LinkedList<Double>());
	}
	
	public ValuesForRecallPrecisionCurve(String experimentName) {
		this(experimentName, new LinkedList<Double>(), new LinkedList<Double>());	
	}
	
	public void addResult(Double recall, Double precision){
		recallValues.add(recall);
		precisionValues.add(precision);
	}

	public List<Double> getPrecisionValues() {
		return precisionValues;
	}

	public List<Double> getRecallValues() {
		return recallValues;
	}

	public String getExperimentName() {
		return experimentName;
	}
	
	public void addMAP(Double map){
		mapValues.add(map);
	}
	
	/**
	 * @return the mapValues
	 */
	public List<Double> getMapValues() {
		return mapValues;
	}

	public static class ComparatorByName implements Comparator<ValuesForRecallPrecisionCurve> {
	    @Override
	    public int compare(ValuesForRecallPrecisionCurve vA, ValuesForRecallPrecisionCurve vB) {
	    	return vA.getExperimentName().compareTo(vB.getExperimentName());
	    }
	}

}

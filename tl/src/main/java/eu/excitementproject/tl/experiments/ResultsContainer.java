package eu.excitementproject.tl.experiments;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.tl.evaluation.utils.EvaluationAndAnalysisMeasures;
import eu.excitementproject.tl.evaluation.utils.rpcurve.RecallMaxPrecisionCurve;
import eu.excitementproject.tl.evaluation.utils.rpcurve.RecallPrecisionCurve;
import eu.excitementproject.tl.evaluation.utils.rpcurve.ValuesForRecallPrecisionCurve;

/**
 * 
 * @author ??
 *
 */
public class ResultsContainer {
	public Map<String,Map<Double,EvaluationAndAnalysisMeasures>> results;
	public List<Double> confidenceThresholds;
	protected Map<String,ValuesForRecallPrecisionCurve> resultsForRecallPrecisionCurve;

	public ResultsContainer() {
		super();
		results = new HashMap<String, Map<Double,EvaluationAndAnalysisMeasures>>();
		resultsForRecallPrecisionCurve = new HashMap<String, ValuesForRecallPrecisionCurve>();
		confidenceThresholds= new LinkedList<Double>();
		for (double confidenceThreshold=0.0; confidenceThreshold<1.01; confidenceThreshold+=0.05){
			confidenceThresholds.add(round(confidenceThreshold,2));
		}
		
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	public void addResult(String setting, double threshold, EvaluationAndAnalysisMeasures res){
		Map<Double,EvaluationAndAnalysisMeasures> resultsForSetting = new HashMap<Double,EvaluationAndAnalysisMeasures>();
		if(results.containsKey(setting)) resultsForSetting = results.get(setting);
		resultsForSetting.put(threshold, res);
		results.put(setting, resultsForSetting);
	}

	public String printResults(){
		String s = "";
		for (String setting : results.keySet()){
			System.out.println();
			for (Double threshold : confidenceThresholds){
				if (results.get(setting).containsKey(threshold)){
					EvaluationAndAnalysisMeasures res = results.get(setting).get(threshold);
					s += setting+"\t"+threshold.toString()+
						"\t"+res.getRecall().toString()+"\t"+res.getPrecision().toString()+"\t"+res.getF1().toString()+
						"\t"+res.getAccuracy().toString()+
						"\t"+res.getAveragePrecision().toString()+
						"\t"+res.getOverallEdges().toString()+"\t"+res.getViolations().toString()+
						"\t"+res.getExtraFGedges().toString()+"\t"+res.getMissingFGedges().toString()+
						"\t"+res.getEdaCalls().toString()+"\n";
				}
			}
		}
		System.out.println(s);	
		return s;
	}
	
	public String printRecallPrecisionCurves(){
		List<ValuesForRecallPrecisionCurve> data = new LinkedList<ValuesForRecallPrecisionCurve>(resultsForRecallPrecisionCurve.values());
		Collections.sort(data, new ValuesForRecallPrecisionCurve.ComparatorByName());
		RecallPrecisionCurve c = new RecallMaxPrecisionCurve(data);
		return c.print();							
	}
	
	public String printAvgResults(){
		addAverages();
		String s = "";
		for (String setting : results.keySet()){
			if (setting.contains("AVG")){
				for (Double threshold : confidenceThresholds){
					if (results.get(setting).containsKey(threshold)){
						EvaluationAndAnalysisMeasures res = results.get(setting).get(threshold);
						s += setting+"\t"+threshold.toString()
							+"\t"+res.getRecall().toString()+"\t"+res.getPrecision().toString()+
							"\t"+res.getF1().toString()+"\t"+res.getAccuracy().toString()+
							"\t"+ res.getAveragePrecision().toString()+
							"\t"+ res.getEdaCalls().toString()+
							"\n";
					}
				}				
			}
		}
		System.out.println(s);
		
		s+="\nRecall-Precision curves:\n"+printRecallPrecisionCurves();
		return s;
	}	

	public void addAverages(){
		HashSet<String> settings = new HashSet<String>();
		for (String s : results.keySet()){
			settings.add(getSetting(s));
		}
		for (String s : settings){
			addAverage(s);
		}
	}
	
	private EvaluationAndAnalysisMeasures getMacroAverage(Set<EvaluationAndAnalysisMeasures> set){
		Double recallAvg = 0.0;
		Double precAvg = 0.0;
		Double accuracyAvg = 0.0;
		Double map = 0.0;
		Integer edaCalls = 0;
		
		for (EvaluationAndAnalysisMeasures m : set){
			recallAvg += m.getRecall();				
			precAvg += m.getPrecision();   
			accuracyAvg += m.getAccuracy();
			map += m.getAveragePrecision();
			edaCalls += m.getEdaCalls();
		}
		recallAvg /= set.size();
		precAvg /= set.size();
		accuracyAvg /= set.size();
		map /= set.size();
		
		EvaluationAndAnalysisMeasures res = new EvaluationAndAnalysisMeasures();
		res.setPrecision(precAvg);
		res.setRecall(recallAvg);
		res.setAccuracy(accuracyAvg);
		res.setAveragePrecision(map);
		res.setEdaCalls(edaCalls);
		return res;
	}

	private EvaluationAndAnalysisMeasures getMicroAverage(Set<EvaluationAndAnalysisMeasures> set){
		Integer tp = 0;
		Integer precDenominator = 0;
		Integer recDenominator = 0;
		Integer correctDecisions = 0;
		Integer allDecisions = 0;
		Integer edaCalls = 0;
		
		for (EvaluationAndAnalysisMeasures m : set){
			if (m.getTruePositives()!=null) {
				tp+=m.getTruePositives();		// else consider it zero (no need to add anything to the sum)					}
			}
			if (m.getRecallDenominator()!=null){
				recDenominator+=m.getRecallDenominator();		// else consider it zero (no need to add anything to the sum)		
			}
			if (m.getPrecisionDenominator()!=null){
				precDenominator+=m.getPrecisionDenominator();		// else consider it zero (no need to add anything to the sum)		
			}
			if (m.getCorrectDecisions()!=null){
				correctDecisions+=m.getCorrectDecisions();		// else consider it zero (no need to add anything to the sum)		
			}
			if (m.getAllDecisions()!=null){
				allDecisions+=m.getAllDecisions();		// else consider it zero (no need to add anything to the sum)		
			}
			edaCalls += m.getEdaCalls();
		}
		EvaluationAndAnalysisMeasures res = new EvaluationAndAnalysisMeasures(tp, recDenominator, precDenominator);
		res.setAccuracy(correctDecisions, allDecisions);
		res.setAveragePrecision(Double.NaN); // map is not defined for micro average, and we do not calculate it over all edges, so set as NaN, not to report by mistake
		res.setEdaCalls(edaCalls);
		return res;
	}
	
	private void addAverage(String setting){
		Map<Double,Set<EvaluationAndAnalysisMeasures>> resForSetting = new HashMap<Double,Set<EvaluationAndAnalysisMeasures>>();

		for (String settingClusterKey : results.keySet()){
			if (settingClusterKey.startsWith(setting)){
				Map<Double, EvaluationAndAnalysisMeasures> r = results.get(settingClusterKey);
				for (Double thresholdKey : r.keySet()){
					Set<EvaluationAndAnalysisMeasures> evalSet = new HashSet<EvaluationAndAnalysisMeasures>();
					if(resForSetting.containsKey(thresholdKey)) evalSet = resForSetting.get(thresholdKey);
					evalSet.add(r.get(thresholdKey));
					resForSetting.put(thresholdKey, evalSet);
				}
			}
		}
		
		String avgSetting = setting+"MACRO AVG";

		Map<Double,EvaluationAndAnalysisMeasures> avgResults = new HashMap<Double,EvaluationAndAnalysisMeasures>();
		for (Double thresholdKey : resForSetting.keySet()){
			avgResults.put(thresholdKey, getMacroAverage(resForSetting.get(thresholdKey)));
			// save AVG data for recall-precision curve
			ValuesForRecallPrecisionCurve recallPrecisionPair; 
			if (resultsForRecallPrecisionCurve.containsKey(avgSetting)) recallPrecisionPair = resultsForRecallPrecisionCurve.get(avgSetting);
			else recallPrecisionPair = new ValuesForRecallPrecisionCurve(avgSetting);				
			recallPrecisionPair.addResult(avgResults.get(thresholdKey).getRecall(),avgResults.get(thresholdKey).getPrecision());
			resultsForRecallPrecisionCurve.put(avgSetting, recallPrecisionPair);
		}
		
		results.put(avgSetting, avgResults);
				
		avgSetting = setting+"MICRO AVG";

		avgResults = new HashMap<Double,EvaluationAndAnalysisMeasures>();
		for (Double thresholdKey : resForSetting.keySet()){
			avgResults.put(thresholdKey, getMicroAverage(resForSetting.get(thresholdKey)));
			// save AVG data for recall-precision curve
			ValuesForRecallPrecisionCurve recallPrecisionPair; 
			if (resultsForRecallPrecisionCurve.containsKey(avgSetting)) recallPrecisionPair = resultsForRecallPrecisionCurve.get(avgSetting);
			else recallPrecisionPair = new ValuesForRecallPrecisionCurve(avgSetting);				
			recallPrecisionPair.addResult(avgResults.get(thresholdKey).getRecall(),avgResults.get(thresholdKey).getPrecision());
			resultsForRecallPrecisionCurve.put(avgSetting, recallPrecisionPair);
		}
		
		results.put(avgSetting, avgResults);
	}

	private String getSetting(String settingClusterString){
		String[] s = settingClusterString.split(" ");
		String setting = "";
		int i = 1;
		for (String part : s){
			if (i == s.length) break;
			setting += part+" ";
			i++;
		}
		return setting;
	}
	
	/**
	 * @param confidenceThresholds the confidenceThresholds to set
	 */
	public void setConfidenceThresholds(List<Double> confidenceThresholds) {
		this.confidenceThresholds = confidenceThresholds;
	}

	public String printErrorExamples(int limit) {
		String s="";
		s+="\n ==== ERROR EXAMPLES: ====\n";
		for (String setting : results.keySet()){
			if (!setting.contains("raw-without-FG")) continue; // only sample errors from raw graph, FG edges are not interesting
			Double threshold = confidenceThresholds.get(0); // get the lowest threshold (0,5)	
				if (results.get(setting).containsKey(threshold)){
					EvaluationAndAnalysisMeasures res = results.get(setting).get(threshold);
					s += setting+"\t"+threshold.toString()+"\n";
					s+="\n ==== False Positives: ====\n";
					for (String ex : res.getFalsePositiveExamples(limit)){
						s+= ex+"\n";
					}
					s+="\n ==== False Negatives: ====\n";
					for (String ex : res.getFalseNegativeExamples(limit)){
						s+= ex+"\n";
					}
				}
		}
		System.out.println(s);	
		return s;
	}

}

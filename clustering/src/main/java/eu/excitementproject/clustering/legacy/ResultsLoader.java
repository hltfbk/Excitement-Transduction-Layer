package eu.excitementproject.clustering.legacy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

@Deprecated
public interface ResultsLoader {

	public void loadResults	(String resultsFilename, Map<String,Integer> termFrequencies,
			Map<String, Map<Integer,Integer>> reasonsByTerm, 
			Map<Integer, String> reasonsById) 
			throws IOException, FileNotFoundException;
	
	public Map<String,LinkedList<String>> getClustersToEvaluate();
	
	public Map<String,LinkedList<Integer>> getClustersToEvaluateById();

}

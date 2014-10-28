package eu.excitementproject.clustering.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.Clusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.data.api.LexicalExpander;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.clustering.data.impl.lexicalexpander.WordNetAndBapLexicalExpander;
import eu.excitementproject.clustering.data.impl.textcollection.TextCollectionWithAllTokenLemmas;
import eu.excitementproject.clustering.eval.EvaluationValues;
import eu.excitementproject.clustering.eval.MeanAveragePrecisionCalulator;
import eu.excitementproject.clustering.eval.RecallMaxPrecisionCurve;
import eu.excitementproject.clustering.eval.RecallPrecisionCurve;
import eu.excitementproject.eop.common.utilities.ClusteringResultsEvaluator;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;
import eu.excitementproject.eop.common.datastructures.Pair;

/**
 * @author Lili Kotlerman
 *
 */
public abstract class AbstractDemoRunner {

	public abstract void runDemo(String configurationFileName) throws ClusteringException;
	
	protected TextCollection m_textCollection;
	protected LexicalExpander m_lexicalExpander;
	
	protected boolean m_useExpandedCollection = false;
	protected boolean m_ignoreNonClusterResults = false;
	protected boolean m_evaluateTopKPercentsOfTheClusters = false;
	
	
	protected String m_configurationFileName;
	protected String m_out_dir;
	protected String m_externalModelFilename;
	protected String m_externalOutput;
	
	protected Map<String,EvaluationValues> m_evaluationResults;
	protected Map<Integer,Map<String,String>> m_resultsPerK;

	protected Map<String,EvaluationValues> m_evaluationResults_ignore;
	protected Map<Integer,Map<String,String>> m_resultsPerK_ignore;
	
	protected Map<String,EvaluationValues> m_evaluationResults_topPercent;
	protected Map<Integer,Map<String,String>> m_resultsPerK_topPercent;

	
	public AbstractDemoRunner(String configurationFileName) throws ClusteringException {
		this(configurationFileName, null);
	}

	public AbstractDemoRunner(String configurationFileName, String dataFilename) throws ClusteringException{
		m_configurationFileName = configurationFileName;
		try {
			init(new ConfigurationFile(m_configurationFileName), dataFilename);
		} catch (ConfigurationException e) {
			throw new ClusteringException("Problem initializing demo runner with a configuration file.\n"+e);
		}
	}

	private void init(ConfigurationFile conf, String dataFilename) throws ClusteringException
	{

		try {
			if(conf.isModuleExist("Experiment")){
				ConfigurationParams params = conf.getModuleConfiguration("Experiment");
				if(params.containsKey("expand")) {
					m_useExpandedCollection = params.getBoolean("expand");
					System.out.println("Using expansion: "+m_useExpandedCollection);
				}
				if(params.containsKey("output_directory")) {
					m_out_dir = params.getString("output_directory");
					System.out.println("Outputs will be written to: "+m_out_dir);
				}			
			}
		} catch (ConfigurationException e) {
			throw new ClusteringException("Problem loading configuration info for <<Experiment>> module.\n"+e);
		}
		
		try {
			if (conf.isModuleExist("lda")){
				ConfigurationParams params = conf.getModuleConfiguration("lda");
				if (params.containsKey("model-file")){
					m_externalModelFilename = params.getString("model-file");
					m_externalOutput = params.getString("model-output-dir");
				}
			}
		} catch (ConfigurationException e) {
			throw new ClusteringException("Problem loading configuration info for <<lda>> module.\n"+e);
		}
		
		try {
			if (dataFilename==null) m_textCollection = new TextCollectionWithAllTokenLemmas(m_configurationFileName);
			else m_textCollection = new TextCollectionWithAllTokenLemmas(m_configurationFileName, dataFilename);
			m_textCollection.loadNewCollection();
			if (m_useExpandedCollection){
				// load domain vocabulary (if provided)
				if (dataFilename!=null) m_textCollection.setDomainVocabulary(dataFilename.replace(".txt","_domainVocab.txt"));
				
				m_lexicalExpander = new WordNetAndBapLexicalExpander(m_configurationFileName, m_textCollection);
				m_textCollection.expandCollection(m_lexicalExpander);
			}
		} catch (ConfigurationException | MalformedURLException | LemmatizerException e) {
			throw new ClusteringException("Problem craeting or expanding text collection.\n"+e);
		}			
		
		
		m_evaluationResults = new HashMap<String, EvaluationValues>();
		m_resultsPerK = new HashMap<Integer, Map<String,String>>();
		m_evaluationResults_ignore = new HashMap<String, EvaluationValues>();
		m_resultsPerK_ignore = new HashMap<Integer, Map<String,String>>();
		m_evaluationResults_topPercent = new HashMap<String, EvaluationValues>();
		m_resultsPerK_topPercent = new HashMap<Integer, Map<String,String>>();
		
	}
	

	public void processResults(String settingName, Integer k, Map<String, List<Integer>> clusteringResults) throws ClusteringException{
		saveResults(settingName, k, clusteringResults);
		processResults(settingName, k, clusteringResults, false);
	}
	
	private void processResultsInternal(String settingName, Integer k, Map<String, List<Integer>> clusteringResults){				
		// create set of items we're evaluating
		Set<Integer> itemIdsTmp = new HashSet<Integer>();
		for (String clustId: clusteringResults.keySet()){
			itemIdsTmp.addAll(clusteringResults.get(clustId));
		}

		if (m_ignoreNonClusterResults){ // if ignore non-cluster documents for evaluation
			Map<String, List<Integer>> clusteringResultsTmp = new HashMap<String, List<Integer>>(clusteringResults);			
			
			String ignoreLabel = Clusterer.NON_CLUSTER_LABEL;
			if (clusteringResultsTmp.containsKey(ignoreLabel)){
				// for R-P-F1 need to pass only remaining documents as itemIds (remove ignored ids)
				for (Integer nonClustDocId : clusteringResultsTmp.get(ignoreLabel)){
					itemIdsTmp.remove(nonClustDocId);
				}
				// for purity and map need to remove the ignored documents themselves
				System.out.println("Removing "+ clusteringResultsTmp.get(ignoreLabel).size()+" non-cluster documents from the evaluated results");
				clusteringResultsTmp.remove(ignoreLabel);
			}
						
			EvaluationValues recallPrecisionPair;
			
			if (m_evaluateTopKPercentsOfTheClusters){
				if (m_evaluationResults_topPercent.containsKey(settingName)) recallPrecisionPair = m_evaluationResults_topPercent.get(settingName);
				else recallPrecisionPair = new EvaluationValues(settingName);			
				
				if (!m_resultsPerK_topPercent.containsKey(k)) m_resultsPerK_topPercent.put(k, new HashMap<String,String>());			
				Map<String,String> results = m_resultsPerK_topPercent.get(k);
		
				Map<String,Double> eval = ClusteringResultsEvaluator.calculateRecallPrecisionFmeasuresAndRandIndex(itemIdsTmp, m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResultsTmp);
				recallPrecisionPair.addResult(eval.get("R"),eval.get("P"));
				System.out.println(eval);
				m_evaluationResults_topPercent.put(settingName, recallPrecisionPair);
				
				double purity = ClusteringResultsEvaluator.calculatePurity(m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResultsTmp);
				System.out.println("Putiry="+purity);					
				
				double map = MeanAveragePrecisionCalulator.calculateMAP(m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResultsTmp);
				System.out.println("MAP="+map);					
				
				results.put(settingName, resultsToString(eval)+"\t"+String.valueOf(purity)+"\t"+String.valueOf(map));
				m_resultsPerK_topPercent.put(k, results);						
			}
			else{
				if (m_evaluationResults_ignore.containsKey(settingName)) recallPrecisionPair = m_evaluationResults_ignore.get(settingName);
				else recallPrecisionPair = new EvaluationValues(settingName);			
				
				if (!m_resultsPerK_ignore.containsKey(k)) m_resultsPerK_ignore.put(k, new HashMap<String,String>());			
				Map<String,String> results = m_resultsPerK_ignore.get(k);
		
				Map<String,Double> eval = ClusteringResultsEvaluator.calculateRecallPrecisionFmeasuresAndRandIndex(itemIdsTmp, m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResultsTmp);
				recallPrecisionPair.addResult(eval.get("R"),eval.get("P"));
				System.out.println(eval);
				m_evaluationResults_ignore.put(settingName, recallPrecisionPair);
				
				double purity = ClusteringResultsEvaluator.calculatePurity(m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResultsTmp);
				System.out.println("Putiry="+purity);					
				
				double map = MeanAveragePrecisionCalulator.calculateMAP(m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResultsTmp);
				System.out.println("MAP="+map);					
				
				results.put(settingName, resultsToString(eval)+"\t"+String.valueOf(purity)+"\t"+String.valueOf(map));
				m_resultsPerK_ignore.put(k, results);		
			}
		}
		
		else {
			Map<String, List<Integer>> clusteringResultsTmp = new HashMap<String, List<Integer>>(clusteringResults);			
			EvaluationValues recallPrecisionPair; 
			if (m_evaluationResults.containsKey(settingName)) recallPrecisionPair = m_evaluationResults.get(settingName);
			else recallPrecisionPair = new EvaluationValues(settingName);			
			
			if (!m_resultsPerK.containsKey(k)) m_resultsPerK.put(k, new HashMap<String,String>());			
			Map<String,String> results = m_resultsPerK.get(k);
	
			Map<String,Double> eval = ClusteringResultsEvaluator.calculateRecallPrecisionFmeasuresAndRandIndex(itemIdsTmp, m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResultsTmp);
			recallPrecisionPair.addResult(eval.get("R"),eval.get("P"));
			System.out.println(eval);
			m_evaluationResults.put(settingName, recallPrecisionPair);
			
			double purity = ClusteringResultsEvaluator.calculatePurity(m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResultsTmp);
			System.out.println("Putiry="+purity);					

			double map = MeanAveragePrecisionCalulator.calculateMAP(m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResultsTmp);
			System.out.println("MAP="+map);					
			
			results.put(settingName, resultsToString(eval)+"\t"+String.valueOf(purity)+"\t"+String.valueOf(map));
			m_resultsPerK.put(k, results);
		}
	}
	
	public void processResults(String settingName, Integer k, Map<String, List<Integer>> clusteringResults, double topKPercentsToRetain){
		if (topKPercentsToRetain < 1) {
			m_ignoreNonClusterResults = true;
			m_evaluateTopKPercentsOfTheClusters = true;
			processResultsInternal(settingName, k, cutOffClusters(clusteringResults,topKPercentsToRetain));
		}
		else processResults(settingName, k, clusteringResults, true);
	}

	public void processResults(String settingName, Integer k, Map<String, List<Integer>> clusteringResults, boolean ignoreNonClusterResults){		
		m_ignoreNonClusterResults = ignoreNonClusterResults;
		m_evaluateTopKPercentsOfTheClusters=false;
		processResultsInternal(settingName, k, clusteringResults);
	}
	
	public void saveResults(String settingName, Integer k, Map<String, List<Integer>> res) throws ClusteringException{
		try {
			File resDir = new File(m_out_dir+"/res/");
			if (!resDir.exists()) resDir.mkdir();
			String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
			BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
			resWriter.write(getDocumentClustersForPrint(res, true));
			resWriter.close();
		} catch (IOException e) {
			throw new ClusteringException("Cannot save document clusters.\n"+e);
		}		
	}
	
	protected Map<String, List<Integer>> cutOffClusters(Map<String, List<Integer>> clusteringResults, double topKPercentsToRetain){
		Map<String, List<Integer>> clusteringResultsCutOff = new HashMap<String, List<Integer>>();
		for (String clusterID : clusteringResults.keySet()){
			double limit = clusteringResults.get(clusterID).size() * topKPercentsToRetain;
			if (limit < 1) limit = 1;
			if (limit > clusteringResults.get(clusterID).size()) limit = clusteringResults.get(clusterID).size();
			List<Integer> cutOffCluster = new LinkedList<Integer>();
			for (int i=0; i<limit; i++){
				cutOffCluster.add(clusteringResults.get(clusterID).get(i));
			}
			clusteringResultsCutOff.put(clusterID, cutOffCluster);
		}
		return clusteringResultsCutOff;
	}
	
	/*protected void processResults(String settingName, Integer k, Map<String, List<Integer>> clusteringResults){
		RecallPrecisionValues recallPrecisionPair; 
		if (m_recallPrecisionResults.containsKey(settingName)) recallPrecisionPair = m_recallPrecisionResults.get(settingName);
		else recallPrecisionPair = new RecallPrecisionValues(settingName);			
		
		if (!m_allResultsPerK.containsKey(k)) m_allResultsPerK.put(k, new HashMap<String,String>());			
		Map<String,String> results = m_allResultsPerK.get(k);

		Map<String,Double> eval = ClusteringResultsEvaluator.calculateRecallPrecisionFmeasuresAndRandIndex(m_textCollection.getDocTextsByDocId().keySet(), m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResults);
		recallPrecisionPair.addResult(eval.get("R"),eval.get("P"));
		System.out.println(eval);
		double purity = ClusteringResultsEvaluator.calculatePurity(m_textCollection.getDocIdsPerGoldStandardCluster(), clusteringResults);
		System.out.println("Putiry="+purity);					
		m_recallPrecisionResults.put(settingName, recallPrecisionPair);
		
		results.put(settingName, resultsToString(eval)+"\t"+String.valueOf(purity));
		m_allResultsPerK.put(k, results);
	}*/
	
	protected String resultsToString(Map<String,Double> eval){		
		return  String.valueOf(eval.get("R"))+"\t"+String.valueOf(eval.get("P"))+"\t"+String.valueOf(eval.get("F1"));
	}

	public String printRecallPrecisionCurvesData(int choice){
		if (choice==1){
			if(m_evaluateTopKPercentsOfTheClusters){
				List<EvaluationValues> data = new LinkedList<EvaluationValues>(m_evaluationResults_topPercent.values());
				Collections.sort(data, new EvaluationValues.ComparatorByName());
				RecallPrecisionCurve c = new RecallMaxPrecisionCurve(data);
				return c.print();								
			}
			else return "ERROR: evaluaiton for cut-off clusters is not available";
		}
		else if (choice==2){
			if (m_ignoreNonClusterResults){
				List<EvaluationValues> data = new LinkedList<EvaluationValues>(m_evaluationResults_ignore.values());
				Collections.sort(data, new EvaluationValues.ComparatorByName());
				RecallPrecisionCurve c = new RecallMaxPrecisionCurve(data);
				return c.print();					
			}
			else return "ERROR: evaluaiton for full clusters + ignore non-cluss is not available";
		}
		else{
			List<EvaluationValues> data = new LinkedList<EvaluationValues>(m_evaluationResults.values());
			Collections.sort(data, new EvaluationValues.ComparatorByName());
			RecallPrecisionCurve c = new RecallMaxPrecisionCurve(data);
			return c.print();					
		}
	}
		
	public String printAllResults(int choice){
		String s ="\n"+m_configurationFileName+"\n";

		if (choice==1){
			if(m_evaluateTopKPercentsOfTheClusters){
				List<String> settings = new LinkedList<String>(m_evaluationResults_topPercent.keySet());
				Collections.sort(settings);		
				List<Integer> kValues = new LinkedList<Integer>(m_resultsPerK_topPercent.keySet());
				Collections.sort(kValues);
				s+=("k \t sysName \t R \t P \t F1 \t Purity \t MAP \n");
				for (int k : kValues){
					for (String setting : settings){
						s+=(String.valueOf(k)+"\t"+setting+"\t"+m_resultsPerK_topPercent.get(k).get(setting)+"\n");
					}
					s+="\n";
				}
				System.out.println(s);
				return s;			
			}
			else return "ERROR: evaluaiton for cut-off clusters is not available";
		}
		else if (choice==2){
			if (m_ignoreNonClusterResults){
				List<String> settings = new LinkedList<String>(m_evaluationResults_ignore.keySet());
				Collections.sort(settings);		
				List<Integer> kValues = new LinkedList<Integer>(m_resultsPerK_ignore.keySet());
				Collections.sort(kValues);
				s+=("k \t sysName \t R \t P \t F1 \t Purity \t MAP \n");
				for (int k : kValues){
					for (String setting : settings){
						s+=(String.valueOf(k)+"\t"+setting+"\t"+m_resultsPerK_ignore.get(k).get(setting)+"\n");
					}
					s+="\n";
				}
				System.out.println(s);
				return s;			
			}
			else return "ERROR: evaluaiton for full clusters + ignore non-cluss is not available";
		}
		else{
			List<String> settings = new LinkedList<String>(m_evaluationResults.keySet());
			Collections.sort(settings);		
			List<Integer> kValues = new LinkedList<Integer>(m_resultsPerK.keySet());
			Collections.sort(kValues);
			s+=("k \t sysName \t R \t P \t F1 \t Purity \t MAP \n");
			for (int k : kValues){
				for (String setting : settings){
					s+=(String.valueOf(k)+"\t"+setting+"\t"+m_resultsPerK.get(k).get(setting)+"\n");
				}
				s+="\n";
			}
			System.out.println(s);
			return s;			
		}
	}	
	
	public void printDocumentClusters(Map<String, List<Integer>> clusteringResults){
		for (String clustName : clusteringResults.keySet()){
			System.out.println("--- "+clustName+" ---");
			for (int docId : clusteringResults.get(clustName)){
				System.out.println("\t "+docId+" "+m_textCollection.getDocTextsByDocId().get(docId));
			}
		}
	}
	
	public String getSemanticRelatednessForPrint(){
		List<String> orig = new LinkedList<String>(m_textCollection.getDocIdsByOriginalTerm().keySet());
		Collections.sort(orig);
		String s = "Original vocabulary: "+orig;
		if (m_useExpandedCollection) {			
			s+="\nSemantically related terms:";
			for (String term : orig){
				if (m_textCollection.getTermSemanticRelatednessMap().getPairContaining(term)==null) continue;
				s+="\n'"+term+"':\n";
				for (Pair<String> p : m_textCollection.getTermSemanticRelatednessMap().getPairContaining(term)){
					s+="\t"+p.toSet().toString()+":"+String.valueOf(m_textCollection.getTermSemanticRelatednessMap().getValueOf(p));
				}
			}
			return s;
		}
		else return s+"\n";
	}
	
	public String getClusterLabel(List<Integer> docIds){
		Map<String,Double> terms = new HashMap<String,Double>();
		for (Integer docId : docIds){
			Map<String,Double> inDocFreqs = m_textCollection.getAllDocumentTermsAfterExpansion(docId);
			for (String term : inDocFreqs.keySet()){
				if(!terms.containsKey(term)) terms.put(term, 0.0);
				Double newFreq = terms.get(term);
				newFreq+=inDocFreqs.get(term);
				terms.put(term,newFreq);
			}
		}
		
		List<Double> freqs = new LinkedList<Double>(terms.values());
		Collections.sort(freqs, Collections.reverseOrder());
		List<String> closed = new LinkedList<String>();
		String s = "";
		int wordCnt = 0;
		for (Double freq : freqs){
			if (terms.containsValue(freq)){
				for(String term : terms.keySet()){
					if (terms.get(term).equals(freq)) {
						if (closed.contains(term)) continue;
						s+="["+term+"]:"+String.valueOf(terms.get(term))+" ";
						wordCnt++;
						closed.add(term);
					}
				}
			}
			if (wordCnt>=10) break;
		}
		return s;
	}
	
	public String getDocumentClustersForPrint(Map<String, List<Integer>> res){
		String s = "";
		for (String clustId : res.keySet()){
			s+=String.valueOf(res.get(clustId).size())+"\t"+getClusterLabel(res.get(clustId))+"\n";			
			
			for (Integer docId : res.get(clustId)){
				s+="\t"+String.valueOf(docId)+"\t"+m_textCollection.getDocTextsByDocId().get(docId)+"\n";				
			}
		}
		return s;
	}
	
	public String getTermClustersForPrint(Map<String, List<String>> clusters){
		String s = "";
		for (String clustId : clusters.keySet()){
			s+=clustId+"\t"+String.valueOf(clusters.get(clustId).size())+"\t"+clusters.get(clustId)+"\n";						
		}
		return s;
	}
	
	
	public String getDocumentClustersForPrint(Map<String, List<Integer>> res, boolean useKeysAsLabels){
		if (!useKeysAsLabels) return getDocumentClustersForPrint(res);
		String s = "";
		for (String clustId : res.keySet()){
			s+=String.valueOf(res.get(clustId).size())+"\t"+clustId+"\n";			
			
			for (Integer docId : res.get(clustId)){
				s+="\t"+String.valueOf(docId)+"\t"+m_textCollection.getDocTextsByDocId().get(docId)+"\n";				
			}
		}
		return s;
	}
		
	
	public String getClustersForPrint(Map<String, List<Integer>> res, Map<String, String> clusterLabels){
		String s = "";
		for (String clustId : res.keySet()){
			if (clusterLabels.containsKey(clustId)) s+=String.valueOf(res.get(clustId).size())+"\t"+clustId+":"+clusterLabels.get(clustId)+"\n";
			else s+=String.valueOf(res.get(clustId).size())+"\t"+clustId+":"+"! most-freq! "+getClusterLabel(res.get(clustId))+"\n";
			
			for (Integer docId : res.get(clustId)){
				s+="\t"+String.valueOf(docId)+"\t"+m_textCollection.getDocTextsByDocId().get(docId)+"\n";
			}
		}
		return s;
	}	
	
	public String printResultsInTable(int choice){
		String s ="\n\n";
		if(choice==1){
			if (m_evaluateTopKPercentsOfTheClusters){
				List<String> settings = new LinkedList<String>(m_evaluationResults_topPercent.keySet());
				Collections.sort(settings);		
				List<Integer> kValues = new LinkedList<Integer>(m_resultsPerK_topPercent.keySet());
				Collections.sort(kValues);
				s+=("system \t k \t R \t P \t F1 \t Purity \n");
						
				for (String setting : settings){
					for (int k : kValues){
						s+=(setting + "\t"+String.valueOf(k)+"\t"+m_resultsPerK_topPercent.get(k).get(setting)+"\n");
					}			
				}
				
				System.out.println(s);
				return s+"\n";
			}	
			else return "ERROR: evaluaiton for cut-off clusters is not available";			
		}
		else if(choice==2){
			if (m_ignoreNonClusterResults){
				List<String> settings = new LinkedList<String>(m_evaluationResults_ignore.keySet());
				Collections.sort(settings);		
				List<Integer> kValues = new LinkedList<Integer>(m_resultsPerK_ignore.keySet());
				Collections.sort(kValues);
				s+=("system \t k \t R \t P \t F1 \t Purity \n");
						
				for (String setting : settings){
					for (int k : kValues){
						s+=(setting + "\t"+String.valueOf(k)+"\t"+m_resultsPerK_ignore.get(k).get(setting)+"\n");
					}			
				}
				
				System.out.println(s);
				return s+"\n";
			}	
			else return "ERROR: evaluaiton for full clusters + ignore non-cluss is not available";
		}
		else{
			List<String> settings = new LinkedList<String>(m_evaluationResults.keySet());
			Collections.sort(settings);		
			List<Integer> kValues = new LinkedList<Integer>(m_resultsPerK.keySet());
			Collections.sort(kValues);
			s+=("system \t k \t R \t P \t F1 \t Purity \n");
					
			for (String setting : settings){
				for (int k : kValues){
					s+=(setting + "\t"+String.valueOf(k)+"\t"+m_resultsPerK.get(k).get(setting)+"\n");
				}			
			}
			
			System.out.println(s);
			return s+"\n";
		}
	}
	
	public String aboutDataset(){
		String s = m_textCollection.getDatasetName()+"\n";
		s += "Orig terms:" + String.valueOf(m_textCollection.getDocIdsByOriginalTerm().size())+".\n";
		s += "Expansion terms:" + String.valueOf(m_textCollection.getDocIdsByExpansionTerm().size())+".\n";
		s += "Total terms:" + String.valueOf(m_textCollection.getDocIdsByOriginalTerm().size()+m_textCollection.getDocIdsByExpansionTerm().size())+".\n";
		s += "GS clusters:" + String.valueOf(m_textCollection.getDocIdsPerGoldStandardCluster().size())+"\n";
		s += getDatasetTermsDF();
		return s;
	}
	
	public String getDatasetTermsDF(){
		String s ="Terms DF:\n";
		for (String term : m_textCollection.getDocIdsByOriginalTerm().keySet()){
			s+="\t"+term+"\t"+String.valueOf(m_textCollection.getDocIdsByOriginalTerm().get(term).size())+"\n";
		}
		return s;
	}
	
	public Map<String,String> getLdaTopicLabels(int k) throws IOException{
		Map<String,String> topicLabels = new HashMap<String, String>();
		File file = new File(m_out_dir+"/lda/out.topic_top_words_"+String.valueOf(k)+".txt");
		if (!file.exists()){
			file = new File (m_externalOutput+"/"+String.valueOf(k)+"_ukwac.out.topic_top_words.txt");
		}
		BufferedReader ldaReader = new BufferedReader(new FileReader(file));
		String line = ldaReader.readLine();
		while(line!=null){
			if (line.split(" ")[0].equals("Topic")){
				String topicId = line.split(" ")[1];
				int wordCnt = 0;
				String topicLabel="";
				while(wordCnt<10){
					line = ldaReader.readLine();
					if (line==null){
						topicLabels.put(topicId, topicLabel);
						ldaReader.close();
						return topicLabels;
					}
					if (line=="") break;
					if (line.split("\t").length!=2) continue;
					//topicLabel += line.split("\t")[0]+" ";
					topicLabel += line.replace("\n"," ").replace("\t", ":")+ " ";
					wordCnt++;
				}
				topicLabels.put(topicId, topicLabel);
			}
			line = ldaReader.readLine();
		}		
		ldaReader.close();	
		return topicLabels;
	}

}

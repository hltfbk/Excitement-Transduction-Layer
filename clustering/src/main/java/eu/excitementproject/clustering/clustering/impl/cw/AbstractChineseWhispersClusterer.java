package eu.excitementproject.clustering.clustering.impl.cw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.clustering.impl.util.rep.ClusterMember;
import eu.excitementproject.clustering.clustering.impl.util.rep.StringsPatrition;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;

/**
 * @author Lili Kotlerman
 * 
 * Wrapper for Chinese Whispers Clustering tool of
 * Biemann, Chris. "Chinese whispers: an efficient graph clustering algorithm and its application to natural language processing problems." Proceedings of the first workshop on graph based methods for natural language processing. Association for Computational Linguistics, 2006.
 * 
 * Returns the elements sorted (desc) by assignment score returned by the tool 
 */
public abstract class AbstractChineseWhispersClusterer<T> {

	boolean m_useExpandedCollection;
	WeightType m_weightType;
	double m_similarityThreshold; // similarity value, below which edge will not be added to the graph  
	String m_cwExecutable_dir;
	String m_workingDirectory;
	String m_inputFilenamePrefix;
	File m_resFile; // .read file with clustering results
	File m_resFileSorted; // .read file with clustering results
	
	Map<String,T> m_idsToElements;
	
	public AbstractChineseWhispersClusterer(boolean useExpandedCollection, TextCollection textCollection, WeightType weightType, String configurationFilename, Double similarityThreshold) throws ClusteringException {
		m_useExpandedCollection = useExpandedCollection;
		m_weightType = weightType;
		m_similarityThreshold = similarityThreshold;

		String m_conf = configurationFilename;
		try {
			ConfigurationFile conf = new ConfigurationFile(m_conf);
			m_cwExecutable_dir = conf.getModuleConfiguration("chinese-whispers").get("cw_dir");
			
			m_workingDirectory = conf.getModuleConfiguration("chinese-whispers").get("work_dir");
			File workDir = new File(m_workingDirectory);
			if (!workDir.exists()) workDir.mkdir();
			
			m_conf = conf.getConfFile().getName();
		} catch (ConfigurationException e) {
			throw new ClusteringException("Problem loading coclust directory path from "+m_conf+".\n"+e);
		}
		
		String dataset = textCollection.getDatasetName();
		String dataFile = textCollection.getInputFilename();
		if (dataset.contains("/")||dataset.contains(":")){
			dataFile.replace('/', '-').replace('\\', '-').replace(':', '-'); 
			for(String s : dataFile.replace('/', '#').replace('\\', '#').replace(':', '#').split("#")){
				if (s.contains(".txt")) dataset = s;
			}
		}
		m_inputFilenamePrefix = m_workingDirectory+"/"+dataset+"_"+m_weightType+"_CW_";
		
	}

	
	/** Calculate similarity between two elements. 
	 * @param elementA
	 * @param elementB
	 * @return similarity value in [0,1]
	 */
	protected abstract double getSimilarity(T elementA, T elementB);

	protected abstract String getElementString(T element);
	
	/**
	 * @param textCollection
	 * @return array with exactly 2 Strings, where the first string is the input to nodes.txt, and the 2nd - to edges.txt
	 */
	protected String[] prepareInput(Set<T> elements){
		String nodes = "";
		String edges = "";
		
		// create node-id mappings and the input for nodes.txt
		Map<T,Integer> nodesToIds = new HashMap<T,Integer>();
		m_idsToElements = new HashMap<String,T>();
		int nodeId=1;
		for (T element : elements){
			nodesToIds.put(element, nodeId);
			m_idsToElements.put(String.valueOf(nodeId), element);
			nodes+=String.valueOf(nodeId)+"\t"+getElementString(element)+"\n";
			nodeId++;
		}
		System.out.println("Detected "+nodeId+" nodes");
		
		// check each pair of nodes, and create an edge if similarity > m_similarityThreshold
		for (T nodeA : nodesToIds.keySet()){
			for (T nodeB : nodesToIds.keySet()){
				if(nodeA.equals(nodeB)) continue; //no self-loops
				double similarity = getSimilarity(nodeA, nodeB);
				if (similarity > m_similarityThreshold){
					int weight = 1; // (int) (1000*similarity);
/*					int weight = 0; //weights in the current CW verison are positive integers
					if (similarity >= 1) weight = 2;
					if (similarity < 1) weight = 1;
*/					edges+=String.valueOf(nodesToIds.get(nodeA))+"\t"+String.valueOf(nodesToIds.get(nodeB))+"\t"+String.valueOf(weight)+"\n";					
				}
			}
		}
		
		String[] res = {nodes, edges};
		return res;
	}

	/**
	 * Create and save two files (nodes and edges), in the working directory
	 */
	protected void saveInput(Set<T> elements){
		String[] input = prepareInput(elements);
		String nodesInputForClustering = input[0];
		String edgesInputForClustering = input[1];
		
		try {
			
			File inputNodes = new File(m_inputFilenamePrefix+"nodes.txt");
			BufferedWriter inputWriter = new BufferedWriter(new FileWriter(inputNodes));
			inputWriter.write(nodesInputForClustering);
			inputWriter.close();

			File inputEdges = new File(m_inputFilenamePrefix+"edges.txt");
			inputWriter = new BufferedWriter(new FileWriter(inputEdges));
			inputWriter.write(edgesInputForClustering);
			inputWriter.close();
			
			m_resFile = new File(m_inputFilenamePrefix+"clusteringResults.read");
			m_resFileSorted = new File(m_inputFilenamePrefix+"clusteringResults");			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}
	
	
/*	*//**
	 * @param elements
	 * return map of cluster ids to lists of strings, holding the elements (nodes) as Strings, as given in nodes.txt (as provided by getElementString() method), sorted arbitrarily
	 * @throws ClusteringException
	 *//*
	protected Map<String, List<String>> clusterElementsUnsorted(Set<T> elements) throws ClusteringException{
		saveInput(elements);
		
		// run cw command
		// example: java -jar CW.jar -F -i 20nodes.txt 20edges.txt -o clusteringResults
		try {
			String cmd = "java -jar "+m_cwExecutable_dir+"/"+"CW.jar -F -i "+ m_inputFilenamePrefix+"nodes.txt "+m_inputFilenamePrefix+"edges.txt "+"-o "+m_inputFilenamePrefix+"clusteringResults";
			System.out.println("Executing command: "+cmd);
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			
		}
		
		
	    // sleep 1/2 second until the output file is visible - for some reason the system cannot access the output files right after the command execution, but can do so after a second or so
		while (!m_resFile.exists()){
			try {
			    Thread.sleep(500);			    
			} catch (InterruptedException e) {
			    // recommended because catching InterruptedException clears interrupt flag
			    Thread.currentThread().interrupt();
			}
		}

		// file m_inputFilenamePrefix+"clusteringResults.read" will be generated 
		// read and translate the results into Map<T, List<String>>
		Map<String, List<String>> results = new HashMap<String, List<String>>();
		try {
			BufferedReader resReader = new BufferedReader(new FileReader(m_resFile));
		    // sleep 1/2 second until the reader is ready - for some reason the reader is not always ready to read from the file even 1 sec after the command was executed
			while (!resReader.ready()){
				try {
				    Thread.sleep(500);
				} catch (InterruptedException e) {
				    // recommended because catching InterruptedException clears interrupt flag
				    Thread.currentThread().interrupt();
				}				
			}
			
			String line = resReader.readLine();
			while(line!=null){
				String[] s = line.split("\t");
				if (s.length<3) break;
				String clusterId = s[0];
				Set<String> elementsInCluster = new HashSet<String>();
				for (String element : s[2].split(",")){
					elementsInCluster.add(element.trim());
				}
				results.put(clusterId, new LinkedList<String>(elementsInCluster));
				line = resReader.readLine();
			}
			resReader.close();
		} catch (IOException e) {
			throw new ClusteringException("Cannot read from output file: "+m_inputFilenamePrefix+"clusteringResults.read \n"+e.getMessage());
		}
		
		return results;
	}*/


	/**
	 * @param elements
	 * return map of cluster ids to lists of strings, holding the elements (nodes) as Strings, as given in nodes.txt (as provided by getElementString() method), sorted by score
	 * @throws ClusteringException
	 */
	protected Map<String, List<String>> clusterElements(Set<T> elements) throws ClusteringException{
		saveInput(elements);
		
		// run cw command
		// example: java -jar CW.jar -F -i 20nodes.txt 20edges.txt -o clusteringResults
		try {
			String cmd = "java -jar "+m_cwExecutable_dir+"/"+"CW.jar -F -i "+ m_inputFilenamePrefix+"nodes.txt "+m_inputFilenamePrefix+"edges.txt "+"-o "+m_inputFilenamePrefix+"clusteringResults";
			System.out.println("Executing command: "+cmd);
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			
		}
		
		
	    // sleep 1/2 second until the output file is visible - for some reason the system cannot access the output files right after the command execution, but can do so after a second or so
		while (!m_resFileSorted.exists()){
			try {
			    Thread.sleep(500);			    
			} catch (InterruptedException e) {
			    // recommended because catching InterruptedException clears interrupt flag
			    Thread.currentThread().interrupt();
			}
		}

		// file m_inputFilenamePrefix+"clusteringResults" will be generated
		// with lines like this (tab-delimited)
		// element_#	element	  1st_cluster_id	1st_cluster_id	1st_cluster_score	2nd_cluster_id	2nd_cluster_score
		
		// read and translate the results into Map<T, List<String>>
		Map<String, List<ClusterMember<String>>> resWithScores = new HashMap<String, List<ClusterMember<String>>>();
		try {
			BufferedReader resReader = new BufferedReader(new FileReader(m_resFileSorted));
		    // sleep 1/2 second until the reader is ready - for some reason the reader is not always ready to read from the file even 1 sec after the command was executed
			while (!resReader.ready()){
				try {
				    Thread.sleep(500);
				} catch (InterruptedException e) {
				    // recommended because catching InterruptedException clears interrupt flag
				    Thread.currentThread().interrupt();
				}				
			}
			
			String line = resReader.readLine();
			while(line!=null){
				String[] s = line.split("\t");
				if (s.length<6) break;
				String clusterId = s[2];
				String element = s[1].trim();
				Double score = Double.valueOf(s[4]);
				//TODO save with score into list of ClusterMembers and return sorted
				List<ClusterMember<String>> elementsInCluster = new LinkedList<ClusterMember<String>>();
				if (resWithScores.containsKey(clusterId)) elementsInCluster = resWithScores.get(clusterId);
				elementsInCluster.add(new ClusterMember<String>(element, score));
				resWithScores.put(clusterId, elementsInCluster);
				line = resReader.readLine();
			}
			resReader.close();

		} catch (IOException e) {
			throw new ClusteringException("Cannot read from output file: "+m_inputFilenamePrefix+"clusteringResults.read \n"+e.getMessage());
		}
		
		Map<String, List<String>> results = new HashMap<String, List<String>>();		
		for (String clusterId : resWithScores.keySet()){
			results.put(clusterId, StringsPatrition.getSortedByScoreCluster(resWithScores.get(clusterId)));
		}
		return results;
	}	

}

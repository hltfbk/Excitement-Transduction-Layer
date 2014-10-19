package eu.excitementproject.clustering.legacy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;

/**
 * @author Lili Kotlerman
 * 
 * Wrapper for Chinese Whispers Clustering tool of
 * Biemann, Chris. "Chinese whispers: an efficient graph clustering algorithm and its application to natural language processing problems." Proceedings of the first workshop on graph based methods for natural language processing. Association for Computational Linguistics, 2006.
 * 
 * Wrapper for the old version where node ids were listed in .read output file. In the new version elements (nodes) themselves are listed instead 
 */
public abstract class AbstractChineseWhispersClustererLegacy<T> {

	boolean m_useExpandedCollection;
	WeightType m_weightType;
	double m_similarityThreshold; // similarity value, below which edge will not be added to the graph  
	String m_cwExecutable_dir;
	String m_workingDirectory;
	String m_inputFilenamePrefix;
	File m_resFile; // .read file with clustering results
	
	Map<String,T> m_idsToElements;
	
	public AbstractChineseWhispersClustererLegacy(boolean useExpandedCollection, TextCollection textCollection, WeightType weightType, String configurationFilename, Double similarityThreshold) throws ClusteringException {
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
			nodes+=String.valueOf(nodeId)+"\t"+element.toString()+"\n";
			nodeId++;
		}
		System.out.println("Detected "+nodeId+" nodes");
		
		// check each pair of nodes, and create an edge if similarity > m_similarityThreshold
		for (T nodeA : nodesToIds.keySet()){
			for (T nodeB : nodesToIds.keySet()){
				if(nodeA.equals(nodeB)) continue; //no self-loops
				double similarity = getSimilarity(nodeA, nodeB);
				if (similarity > m_similarityThreshold){
					int weight = (int) similarity*1000; //weights in the current CW verison are positive integers
					edges+=String.valueOf(nodesToIds.get(nodeA))+"\t"+String.valueOf(nodesToIds.get(nodeB))+"\t"+String.valueOf(weight)+"\n";					
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
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
	}
	
	
	/**
	 * @param elements
	 * @return map of cluster ids to sets of strings, holding the elements
	 * @throws ClusteringException
	 */
	protected Map<String, Set<T>> clusterElements(Set<T> elements) throws ClusteringException{
		saveInput(elements);
		
		// run cw command
		// example: java -jar CW.jar -F -i 20nodes.txt 20edges.txt -o clusteringResults
		try {
			String cmd = "java -jar "+m_cwExecutable_dir+"/"+"CW.jar -F -i "+ m_inputFilenamePrefix+"nodes.txt "+m_inputFilenamePrefix+"edges.txt "+"-o "+m_inputFilenamePrefix+"clusteringResults";
			System.out.println("Executing command: "+cmd);
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			
		}
		
		// file m_inputFilenamePrefix+"clusteringResults.read" will be generated 
		// read and translate the results into Map<T, List<T>>
		Map<String, Set<T>> results = new HashMap<String, Set<T>>();
		try {
			BufferedReader resReader = new BufferedReader(new FileReader(m_resFile));
			String line = resReader.readLine();
			while(line!=null){
				String[] s = line.split("\t");
				if (s.length<3) break;
				String clusterId = s[0];
				Set<T> elementsInCluster = new HashSet<T>();
				for (String id : s[2].split(",")){
					T element = m_idsToElements.get(id); 
					elementsInCluster.add(element);
				}
				results.put(clusterId, elementsInCluster);
				line = resReader.readLine();
			}
			resReader.close();
		} catch (IOException e) {
			throw new ClusteringException("Cannot read from output file: "+m_inputFilenamePrefix+"clusteringResults.read \n"+e.getMessage());
		}
		
		return results;
	}


	

}

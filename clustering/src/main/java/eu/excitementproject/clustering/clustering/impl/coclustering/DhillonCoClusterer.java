package eu.excitementproject.clustering.clustering.impl.coclustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.CoClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.util.DocumentsToVectorsConverter;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.clustering.impl.util.rep.VectorsPartition;
import eu.excitementproject.clustering.clustering.impl.util.rep.StringsPatrition;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;

/**
 * This class is a wrapper for the co-clustering tool of Hyuk Cho (http://www.cs.utexas.edu/users/dml/Software/cocluster.html)
 * The wrapper uses the information-theoretic algorithm described in <i>"Information Theoretic Clustering of Sparse Co-Occurrence Data", I.S. Dhillon and Y. Guan, Proceedings of The Third IEEE International Conference on Data Mining, pages 517-520, November 2003. </i>
 *
 * <p>The wrapper generates a dense document-term matrix, with documents as rows and terms as columns.
 * <p>Since the tool does not provide scores/confidence values of term/document assignments to clusters, cluster members are sorted by similarity to centroid vector (for document clusterer) or by semantic relatedness to the cluster (for term clusterer) 
 * 
 *  @author Lili Kotlerman
 */
public class DhillonCoClusterer implements CoClusterer {

	private String m_termClusterIds;
	private String m_documentClusterIds;
	
	Integer m_termClustersNumber;
	Integer m_documentClustersNumber;

	boolean m_useExpandedCollection;
	String m_workingDirectory;
	String m_coclustExecutable;
	File m_inputMatrix;
	String m_dataFile;
	
	LinkedList<String> termsList;
	List<VectorRepresentation> documentsList;
	
	public DhillonCoClusterer(boolean useExpandedCollection, TextCollection textCollection, String configurationFilename, WeightType weightType) throws ClusteringException {
		m_useExpandedCollection = useExpandedCollection;
		String m_conf = configurationFilename;
		try {
			ConfigurationFile conf = new ConfigurationFile(m_conf);
			m_coclustExecutable = conf.getModuleConfiguration("coclust").get("coclust_dir");
			m_workingDirectory = conf.getModuleConfiguration("coclust").get("matrix_dir");
			m_conf = conf.getConfFile().getName();
		} catch (ConfigurationException e) {
			throw new ClusteringException("Problem loading coclust directory path from "+m_conf+".\n"+e);
		}
		init(textCollection, weightType);
	}

	private void init(TextCollection textCollection, WeightType weightType){
		File workDir = new File(m_workingDirectory);
		if (!workDir.exists()) workDir.mkdir();
		// save the name of the data set
		m_dataFile = textCollection.getInputFilename();
		// create ordered list of terms
		constructVocabulary(textCollection);
		// construct matrix
		prepareInput(textCollection, weightType);
	}
	
	/** Construct ordered list of terms for dense matrix generation
	 * @param textCollection
	 */
	private void constructVocabulary(TextCollection textCollection){
		Set<String> vocabulary = new HashSet<String>();
		for (Integer docId : textCollection.getDocTextsByDocId().keySet()){
			for (String term : textCollection.getOriginalTermsByDocId().get(docId).keySet()){
				vocabulary.add(term);
			}
			if(m_useExpandedCollection){
				if (!textCollection.getExpansionTermsByDocId().containsKey(docId)) continue; // if the document has no expansions - skip it
				for (String term : textCollection.getExpansionTermsByDocId().get(docId).keySet()){
					vocabulary.add(term);
				}				
			}
		}
		termsList = new LinkedList<String>(vocabulary);		
	}
	
	
	/** Prepare matrix (input for clustering) as string to be saved to file, and save the matrix to file 
	 * @param textCollection
	 */
	private void prepareInput(TextCollection textCollection, WeightType weightType){		
		documentsList = new LinkedList<VectorRepresentation>(DocumentsToVectorsConverter.convertDocumentsToDenseTermVectors(textCollection, m_useExpandedCollection, weightType)); 
		// create input matrix: rows=documents, columns=terms
		String inputForClustering = "";
		Integer nonZeroValues = 0;
		for (VectorRepresentation sparseDocumentVector : documentsList){ // iterate over documents in the order specified by documentsList
			// each document will be a row in the matrix
			for (String term : termsList){ // iterate over documents in the order specified by documentsList
				if (sparseDocumentVector.getVector().containsKey(term)){
					inputForClustering+=sparseDocumentVector.getVector().get(term)+" ";
					nonZeroValues++;
				}
				else{ // if the term is not present in sparse vector
					inputForClustering += "0 ";
				}
			}
			inputForClustering+="\n"; // go to next row in the matrix
		}
		// add matrix dimensions in a separate line in the beginning of the matrix file
		String dim = String.valueOf(documentsList.size())+" "+String.valueOf(termsList.size())+" "+nonZeroValues.toString()+"\n";
		inputForClustering = dim + inputForClustering;		
				
		// save matrix to file
		try {
			String dataset = textCollection.getDatasetName();
			if (dataset.contains("/")||dataset.contains(":")){
				m_dataFile.replace('/', '-').replace('\\', '-').replace(':', '-'); 
				for(String s : m_dataFile.replace('/', '#').replace('\\', '#').replace(':', '#').split("#")){
					if (s.contains(".txt")) dataset = s;
				}
			}
			
			String matrixFilename = m_workingDirectory+"/"+dataset+"_"+weightType+"_matrix";
			m_inputMatrix = new File(matrixFilename);
			BufferedWriter inputWriter = new BufferedWriter(new FileWriter(m_inputMatrix));
			inputWriter.write(inputForClustering);
			inputWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
	
	private void cluster(TextCollection textCollection) throws ClusteringException{						
		// run the clustering tool
		String colK = String.valueOf(m_termClustersNumber);
		String rowK = String.valueOf(m_documentClustersNumber);
		try {
			if (colK.equals("null") || rowK.equals("null")) throw new ClusteringException("Cannot perform clustering: number of term and/or document clusters is not set.");		
//			String cmd = m_coclustExecutable+"/cocluster-.exe -A i -C "+colK+" -I d t "+m_inputMatrix.getAbsolutePath()+" -R "+rowK+" -O c s 1 o "+m_inputMatrix.getAbsolutePath()+"OutputT"+colK+"D"+rowK+".txt";
			String cmd = m_coclustExecutable+" -A i -C "+colK+" -I d t "+m_inputMatrix.getAbsolutePath()+" -R "+rowK+" -O c s 1 o "+m_inputMatrix.getAbsolutePath()+"OutputT"+colK+"D"+rowK+".txt";
			System.out.println("Executing command: "+cmd);
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			throw new ClusteringException("Cannot execute the command.\n"+e.getMessage());
		}
		

		// read & save the output
		File matrixOutput = new File(m_inputMatrix.getAbsolutePath()+"OutputT"+colK+"D"+rowK+".txt"); 
	    // sleep 1/2 second until the output file is visible - for some reason the system cannot access the output files right after the command execution, but can do so after a second or so
		while (!matrixOutput.exists()){
			try {
			    Thread.sleep(500);			    
			} catch (InterruptedException e) {
			    // recommended because catching InterruptedException clears interrupt flag
			    Thread.currentThread().interrupt();
			}
		}

		try {
			BufferedReader outputReader =  new BufferedReader(new FileReader(matrixOutput));
			
		    // sleep 1/2 second more until the reader is ready - for some reason the reader is not always ready to read from the file even 1 sec after the command was executed
			while (!outputReader.ready()){
				try {
				    Thread.sleep(500);
				} catch (InterruptedException e) {
				    // recommended because catching InterruptedException clears interrupt flag
				    Thread.currentThread().interrupt();
				}				
			}
			try { // sleep 2 more seconds - sometimes the reader is ready, but the content is null for some reason...
			    Thread.sleep(2000);
			} catch (InterruptedException e) {
			    // recommended because catching InterruptedException clears interrupt flag
			    Thread.currentThread().interrupt();
			}				
			
			
			m_documentClusterIds = outputReader.readLine(); //the first contains cluster ids for the rows of the input matrix (documents)
			System.out.println("Done reading doc clusters: "+m_documentClusterIds);
			m_termClusterIds = outputReader.readLine(); // the second line contains cluster ids for columns (terms)
			System.out.println("Done reading term clusters: "+m_termClusterIds);
			outputReader.close();
			if (m_documentClusterIds==null) throw new Exception("Could not read document cluster ids (null)");
			if (m_termClusterIds==null) throw new Exception("Could not read term cluster ids (null)");
		} catch (Exception  e) {
			throw new ClusteringException("Cannot process the output.\n"+e.getMessage());
		}
	}
	
	@Override
	public Map<String, List<Integer>> clusterDocuments(
			TextCollection textCollection) throws ClusteringException{

		cluster(textCollection);
		
		// decode the clustering output
		Map<String, List<Integer>> output = new HashMap<String, List<Integer>>();  
		int docIndex = 0;
		System.out.println(m_documentClusterIds.split(" ").length+ " documents were clustered.");
		for (String clusterId : m_documentClusterIds.split(" ")){
			List<Integer> docIds = new LinkedList<Integer>(); // create list of documents for current cluster
			if (output.containsKey(clusterId)) docIds.addAll(output.get(clusterId)); // if the cluster is not empty, copy previous documents to the list
			docIds.add(documentsList.get(docIndex).getId()); // add the new (current) document to the list
			output.put(clusterId, docIds); // put the updated list to the output
			docIndex++;
		}
		
		VectorsPartition res = new VectorsPartition(output, new HashSet<VectorRepresentation>(documentsList));
		return res.getSortedClusters();
	}
	
	@Override
	public Map<String, List<String>> clusterTerms(TextCollection textCollection) throws ClusteringException {

		cluster(textCollection);
		
		// decode the clustering output
		Map<String, List<String>> output = new HashMap<String, List<String>>();  
		int termIndex = 0;
		for (String clusterId : m_termClusterIds.split(" ")){
			Set<String> terms = new HashSet<String>(); // create list of terms for current cluster
			if (output.containsKey(clusterId)) terms.addAll(output.get(clusterId)); // if the cluster is not empty, copy previous terms to the list
			terms.add(termsList.get(termIndex)); // add the new (current) term to the list
			output.put(clusterId, new LinkedList<String>(terms)); // put the updated list to the output
			termIndex++;
		}
		
		StringsPatrition res = new StringsPatrition(output, textCollection.getTermSemanticRelatednessMap());
		return res.getSortedClusters(textCollection.getTermSemanticRelatednessMap());		
	}

	@Override
	public void setNumberOfTermClusters(int K) {
		m_termClustersNumber = K;
		
		m_termClusterIds="";
		m_documentClusterIds="";				
	}

	@Override
	public void setNumberOfDocumentClusters(int K) {
		m_documentClustersNumber = K;

		m_termClusterIds="";
		m_documentClusterIds="";				
	}

}

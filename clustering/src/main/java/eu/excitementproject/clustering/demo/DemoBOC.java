/**
 * 
 */
package eu.excitementproject.clustering.demo;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.completeLink.DocumentsCompleteLinkClustererBOC;
import eu.excitementproject.clustering.clustering.impl.kmedoids.DocumentsKmedoidsClustererBOC;
import eu.excitementproject.clustering.clustering.impl.kmedoids.TermsKMedoidsClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;

/**
 * @author Lili Kotlerman
 *
 * Demonstrates bag-of-clusters (BOC) clustering usage
 *
 */
public class DemoBOC extends AbstractDemoRunner {

	public DemoBOC(String configurationFileName) throws ClusteringException{
		super(configurationFileName);
	}

	public DemoBOC(String configurationFileName, String dataFilename) throws ClusteringException{
		super(configurationFileName, dataFilename);
	}

	@Override
	public void runDemo(String configurationFileName) throws ClusteringException{
					
		System.out.println(configurationFileName);
						
		int topKfeatures = 1; // how many features to cut-off for the BOC representation
		int k = 20; // number of output clusters
		WeightType weightType = WeightType.TF_DF; // weighting scheme to use

		// for any BOC clustering first term clusters are to be obtained
		// here we obtain term clusters by using K-medoids clustering
		// for examples of other available term clustering algorithms see DemoTermClustering 

		// Use K-medoids term clustering  
		double similarityThreshold = 0.0; // (consider similarity score > 0 enough to connect to a cluster)
		TermClusterer tClusterer = new TermsKMedoidsClusterer(m_useExpandedCollection, weightType, similarityThreshold, false);
		tClusterer.setNumberOfTermClusters(k);
		Map<String, List<String>> termClusters;
		try {
			termClusters = tClusterer.clusterTerms(m_textCollection);
		} catch (ClusteringException e) {
			throw new ClusteringException("Cannot perform term clustering.\n"+e);
		}
		
		// save term clusters for analysis
		try {
			File termClustersDir = new File(m_out_dir+"/termClusters/");
			if (!termClustersDir.exists()) termClustersDir.mkdir();
			String tclustfile = termClustersDir.getAbsolutePath()+"/"+m_textCollection.getDatasetName()+"."+String.valueOf(k)+".termClusters.txt";
			BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
			tclustWriter.write(getTermClustersForPrint(termClusters));
			tclustWriter.close();
		} catch (IOException e) {
			throw new ClusteringException("Cannot save term clusters.\n"+e);
		}
		
		
		// K-medoids BOC clustering
		String settingName="K-medoids-BOC(K-Medoids)-"+weightType;
		System.out.println("\n"+settingName+"\n");

		// for documents let's set the threshold = 0.7
		double threshold = 0.7;
		DocumentsKmedoidsClustererBOC dClusterer = new DocumentsKmedoidsClustererBOC(false, termClusters, weightType, threshold);
		dClusterer.setNumberOfDocumentClusters(k);
		dClusterer.setTopKFeatures(topKfeatures); 
		Map<String, List<Integer>> res = dClusterer.clusterDocuments(m_textCollection);	
		processResults(settingName, k, res);
											
											
		// Complete link BOC clustering with the same term clusters (no top-K features cut-off applied)
		settingName="CompleteLink-BOC(K-Medoids)-"+weightType;
		System.out.println("\n"+settingName+"\n");

		DocumentsCompleteLinkClustererBOC dCLClusterer = new DocumentsCompleteLinkClustererBOC(false, termClusters, weightType);
		dCLClusterer.setNumberOfDocumentClusters(k);
		res = dCLClusterer.clusterDocuments(m_textCollection);						
		processResults(settingName, k, res);
				
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			File configurationFilename = new File(args[0]);
			DemoBOC demo = new DemoBOC(configurationFilename.getAbsolutePath());
			demo.runDemo(configurationFilename.getAbsolutePath());
			demo.printAllResults(0);
			demo.printResultsInTable(0);
			demo.printRecallPrecisionCurvesData(0);
		} catch (ClusteringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}		
}

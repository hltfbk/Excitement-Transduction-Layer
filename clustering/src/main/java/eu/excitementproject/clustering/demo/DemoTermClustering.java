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

import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.kmedoids.TermsKMedoidsClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.DocumentToBestLdaTopicClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.TermToBestLdaTopicByLocalModelClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.TermToBestLdaTopicClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;

/**
 * @author Lili Kotlerman
 *
 * Demonstrates the usage of several term clustering algorithms (other implementations of TermClusterer interface can be applied similarly)
 *
 */
public class DemoTermClustering extends AbstractDemoRunner {

	public DemoTermClustering(String configurationFileName) throws ClusteringException{
		super(configurationFileName);
	}

	public DemoTermClustering(String configurationFileName, String dataFilename) throws ClusteringException{
		super(configurationFileName, dataFilename);
	}

	@Override
	public void runDemo(String configurationFileName) throws ClusteringException{
					
		System.out.println(configurationFileName);
						
		int k = 20; // number of output term clusters
		WeightType weightType = WeightType.TF_DF; // weighting scheme to use

		// K-medoids term clustering  
		double similarityThreshold = 0.0; // (consider similarity score > 0 enough to connect to a cluster)
		TermClusterer tKmedoidsClusterer = new TermsKMedoidsClusterer(m_useExpandedCollection, weightType, similarityThreshold, false);
		tKmedoidsClusterer.setNumberOfTermClusters(k);
		Map<String, List<String>> termClusters;
		try {
			termClusters = tKmedoidsClusterer.clusterTerms(m_textCollection);
		} catch (ClusteringException e) {
			throw new ClusteringException("Cannot perform term clustering.\n"+e);
		}
		
		// LDA term clustering with local model (trains a model with k topics and outputs N<=k term clusters)
		// create localLDAClusterer, whose constructor trains a local model for k topics and creates a term-prob file 
		@SuppressWarnings("unused") // just to suppress warning, the clusterer is used to create the local model
		DocumentClusterer localLDAClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType);
		try {
			ConfigurationFile cf = new ConfigurationFile(m_configurationFileName);
			ConfigurationParams cp = cf.getModuleConfiguration("context-tester");		
			File newProbFile = new File(cp.get("topic-top-words-file").replace(".txt","_"+String.valueOf(k)+".txt"));
			TermToBestLdaTopicClusterer tLdaLocalClusterer = new TermToBestLdaTopicByLocalModelClusterer(m_textCollection, configurationFileName, newProbFile);
			termClusters = tLdaLocalClusterer.clusterTerms(m_textCollection);
		} catch (Exception e1) {
			throw new ClusteringException("Cannot perform term clustering with lda local model.\n"+e1);
		}
		
		//LDA term clustering with pre-trained external model (agnostic to the number of topics |T| in the model, will output N<=|T| term clusters)
		// (this one might take a while)
		TermClusterer tLdaExtClusterer = new TermToBestLdaTopicClusterer(m_textCollection, configurationFileName);
		termClusters = tLdaExtClusterer.clusterTerms(m_textCollection);
		
		// Save term clusters for analysis
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
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			File configurationFilename = new File(args[0]);
			DemoTermClustering demo = new DemoTermClustering(configurationFilename.getAbsolutePath());
			demo.runDemo(configurationFilename.getAbsolutePath());
		} catch (ClusteringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}		
}

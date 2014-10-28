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
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.coclustering.DhillonCoClusterer;
import eu.excitementproject.clustering.clustering.impl.completeLink.DocumentsCompleteLinkClustererBOW;
import eu.excitementproject.clustering.clustering.impl.kmedoids.DocumentsKmedoidsClustererBOW;
import eu.excitementproject.clustering.clustering.impl.lda.DocumentToBestLdaTopicClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;

/**
 * @author Lili Kotlerman
 *
 * Demonstrates bag-of-words (BOW) clustering usage
 *
 */
public class DemoBOW extends AbstractDemoRunner {

	public DemoBOW(String configurationFileName) throws ClusteringException{
		super(configurationFileName);
	}

	public DemoBOW(String configurationFileName, String dataFilename) throws ClusteringException{
		super(configurationFileName, dataFilename);
	}

	@Override
	public void runDemo(String configurationFileName) throws ClusteringException{
					
		System.out.println(configurationFileName);
						
		int k = 20; // number of output clusters
		WeightType weightType = WeightType.TF_DF; // weighting scheme to use
		// Note that m_useExpandedCollection variable (loaded from configuration file) defines whether to cluster expanded documents or original documents

		// K-medoids BOW clustering
		String settingName="K-medoids-BOW-"+weightType;
		System.out.println("\n"+settingName+"\n");

		// for documents let's set the threshold = 0.7
		double threshold = 0.7;
		DocumentsKmedoidsClustererBOW dClusterer = new DocumentsKmedoidsClustererBOW(m_useExpandedCollection, weightType, threshold);
		dClusterer.setNumberOfDocumentClusters(k);
		Map<String, List<Integer>> res = dClusterer.clusterDocuments(m_textCollection);			
		processResults(settingName, k, res);
											
											
		// Complete link BOW clustering 
		settingName="CompleteLink-BOW-"+weightType;
		System.out.println("\n"+settingName+"\n");

		DocumentsCompleteLinkClustererBOW dCLClusterer = new DocumentsCompleteLinkClustererBOW(m_useExpandedCollection, weightType);
		dCLClusterer.setNumberOfDocumentClusters(k);
		res = dCLClusterer.clusterDocuments(m_textCollection);						
		processResults(settingName, k, res);

		// Co-clustering into k document clusters using k term clusters 
		settingName="Co-clustering-BOW-"+weightType;
		System.out.println("\n"+settingName+"\n");

		DhillonCoClusterer clusterer = new DhillonCoClusterer(m_useExpandedCollection, m_textCollection, configurationFileName, weightType);
		clusterer.setNumberOfTermClusters(k);
		clusterer.setNumberOfDocumentClusters(k);	
		res = clusterer.clusterDocuments(m_textCollection);		
		processResults(settingName, k, res);
		
		// LDA with local model
		settingName="LDA-local-BOW"+weightType;
		System.out.println("\n"+settingName+"\n");										
		// create localLDAClusterer, whose constructor trains a local model for k topics and creates a term-prob file 
		DocumentClusterer localLDAClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType);
		// cluster documents with that model
		localLDAClusterer.setNumberOfDocumentClusters(k);
		res = localLDAClusterer.clusterDocuments(m_textCollection);	
		processResults(settingName, k, res);
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String outdir = "./src/test/outputs/exp1_noExpansion";
				
		try {
			File annotationFile = new File(args[0]);
			DemoBOW exp = new DemoBOW(annotationFile.getAbsolutePath());
			BufferedWriter writer = new BufferedWriter(new FileWriter (new File(outdir+"/"+annotationFile.getName().replace(".xml", ".log.txt"))));
			exp.runDemo(args[0]);
			writer.write(exp.printAllResults(0));
			writer.write(exp.printRecallPrecisionCurvesData(0));
			writer.close();
		} catch (IOException | ClusteringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}		
}

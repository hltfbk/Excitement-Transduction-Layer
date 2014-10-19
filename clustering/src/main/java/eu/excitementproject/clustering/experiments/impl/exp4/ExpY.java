/**
 * 
 */
package eu.excitementproject.clustering.experiments.impl.exp4;


import java.io.BufferedWriter;
import java.io.File;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.api.TermClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.coclustering.DhillonCoClusterer;
import eu.excitementproject.clustering.clustering.impl.completeLink.DocumentsAsConceptVectorsCompleteLinkClusterer;
import eu.excitementproject.clustering.clustering.impl.completeLink.DocumentsAsTermVectorsCompleteLinkClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.DocumentToBestLdaTopicClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.clustering.impl.yclust.DocumentsAsConceptVectorsYClusterer;
import eu.excitementproject.clustering.clustering.impl.yclust.DocumentsAsTermVectorsYClusterer;
import eu.excitementproject.clustering.clustering.impl.yclust.TermsYClusterer;
import eu.excitementproject.clustering.experiments.api.AbstractExperimentRunner;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;

/**
 * @author Lili Kotlerman
 *
 */
public class ExpY extends AbstractExperimentRunner {

	public ExpY(String configurationFileName) throws MalformedURLException, ConfigurationException, LemmatizerException{
		super(configurationFileName);
	}

	public ExpY(String configurationFileName, String dataFilename) throws MalformedURLException, ConfigurationException, LemmatizerException{
		super(configurationFileName, dataFilename);
	}

	@Override
	public void runExperiment(String configurationFileName) {
			
		double topKPercentsToRetain = 0.3;
		
		System.out.println(configurationFileName);
			
			List<WeightType> weightTypes = new LinkedList<WeightType>();
			weightTypes.add(WeightType.BINARY); weightTypes.add(WeightType.TF); weightTypes.add(WeightType.TF_IDF);
			weightTypes.add(WeightType.DF); weightTypes.add(WeightType.TF_DF);
			
			LinkedList<Integer> clusterNumbers = new LinkedList<Integer>();
			for (int k = 10; k<=30; k+=5){
				clusterNumbers.add(k);
			}
			
			System.out.println(clusterNumbers);
			
			File resDir = new File(m_out_dir+"/res/");
			if (!resDir.exists()) resDir.mkdir();
			File clDir = new File(m_out_dir+"/completeLink/");
			if (!clDir.exists()) clDir.mkdir();
			File yDir = new File(m_out_dir+"/y-topKtermClusters/");
			if (!yDir.exists()) yDir.mkdir();
			yDir = new File(m_out_dir+"/y-allTermClusters/");
			if (!yDir.exists()) yDir.mkdir();
			File cwDir = new File(m_out_dir+"/chineseWhispers-allTermClusters/");
			if (!cwDir.exists()) cwDir.mkdir();
			File ldaDir = new File(m_out_dir+"/lda-termClusters/");
			if (!ldaDir.exists()) ldaDir.mkdir();
			
			int[] featureCutOffs = {1,2,5};

			for (WeightType weightType : weightTypes){
				//Y-clustering top-K term clusters
				try {
					String settingName="Y-via-terms-top-K_"+weightType;
					System.out.println("\n"+settingName+"\n");
					// for terms consider score > 0 enough to connect to a cluster 
					TermClusterer tClusterer = new TermsYClusterer(m_useExpandedCollection, weightType, 0.0, false);
					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");

						tClusterer.setNumberOfTermClusters(k);
						Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
					//	System.out.println(termClusters);
						
						String tclustfile = m_out_dir+"/y-topKtermClusters/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".termClusters.txt";
						BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
						tclustWriter.write(getTermClustersForPrint(termClusters));
						tclustWriter.close();

						
						settingName="Y-via-terms-top-K_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentsAsConceptVectorsYClusterer dClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);

						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), true));
						resWriter.close();
						
						processResults(settingName, k, res);
												
						
						// now let's play with cutting top K features
						
						for(int topKfeatures : featureCutOffs){						
							settingName="Y-CUT-via-terms-top-K_topFeatures_"+String.valueOf(topKfeatures)+"_"+weightType;							
							dClusterer.setTopKFeatures(topKfeatures);
							res = dClusterer.clusterDocuments(m_textCollection);							
							resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
							resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
							resWriter.write(getDocumentClustersForPrint(res, true));
							resWriter.close();							
							processResults(settingName, k, res);														
						}												

					}
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
				
				
	
				
				
				//Y-clustering threshold = 0.7
				try {
					String settingName="Y-clust-th0.7-"+weightType;
					System.out.println("\n"+settingName+"\n");
					for (int k : clusterNumbers){
						settingName="Y-clust-th0.7-"+weightType;
						DocumentsAsTermVectorsYClusterer dClusterer = new DocumentsAsTermVectorsYClusterer(m_useExpandedCollection, weightType, 0.7);
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						
						processResults(settingName, k, res);
						
						for(int topKfeatures : featureCutOffs){						
							settingName="Y-CUT-clust-th0.7_topFeatures_"+String.valueOf(topKfeatures)+"_"+weightType;							
							dClusterer.setTopKFeatures(topKfeatures);
							res = dClusterer.clusterDocuments(m_textCollection);							
							resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
							resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
							resWriter.write(getDocumentClustersForPrint(res, true));
							resWriter.close();							
							processResults(settingName, k, res);														
						}
					}
				} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
				
				
			}
						

			// now run baselines for unexpanded collection 
			
			for (WeightType weightType : weightTypes){
				
		
				
				//Y-clustering threshold = 0.7
				try {
					String settingName="NONEXP-Y-clust-th0.7-"+weightType;
					System.out.println("\n"+settingName+"\n");
					for (int k : clusterNumbers){
						settingName="Y-clust-th0.7-NONEXP-"+weightType;
						DocumentsAsTermVectorsYClusterer dClusterer = new DocumentsAsTermVectorsYClusterer(m_useExpandedCollection, weightType, 0.7);
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						
						processResults(settingName, k, res);
						
						// now let's play with cutting top K features
						
						for(int topKfeatures : featureCutOffs){						
							settingName="Y-CUT-NONEXP-clust-th0.7_"+String.valueOf(topKfeatures)+"_"+weightType;							
							dClusterer.setTopKFeatures(topKfeatures);
							res = dClusterer.clusterDocuments(m_textCollection);							
							resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
							resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
							resWriter.write(getDocumentClustersForPrint(res, true));
							resWriter.close();							
							processResults(settingName, k, res);														
						}												

						
					}
				} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
				
				
			}			
			
			
			
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String outdir = "./src/test/outputs/exp1_noExpansion";
				
		File annotationFile = new File(args[0]);
		try {
			ExpY exp = new ExpY(annotationFile.getAbsolutePath());
			BufferedWriter writer = new BufferedWriter(new FileWriter (new File(outdir+"/"+annotationFile.getName().replace(".xml", ".log.txt"))));
			exp.runExperiment(args[0]);
			writer.write(exp.printAllResults(0));
			writer.write(exp.printRecallPrecisionCurvesData(0));
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LemmatizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
/*			/////// test lemmatization
			ExcitementTextCollection coll = (ExcitementTextCollection) exp.m_textCollection;
			System.out.println(coll.getLemma("counterclaims"));
			System.out.println(coll.getLemma("killing"));
			System.out.println(coll.getLemma("killed"));
			System.out.println(coll.getLemma("cloudy"));
			System.out.println(coll.getLemma("credit cards"));
			System.out.println(coll.getLemma("credits cards"));
			System.out.println(coll.getLemma("vegetarian foods"));
			System.out.println(coll.getLemma("vegetarian eating"));*/
	}
}

/**
 * 
 */
package eu.excitementproject.clustering.experiments.impl.exp3;


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
import eu.excitementproject.clustering.clustering.impl.chinesewhispers.DocumentsAsConceptVectorsCWClusterer;
import eu.excitementproject.clustering.clustering.impl.chinesewhispers.TermsCWClusterer;
import eu.excitementproject.clustering.clustering.impl.completeLink.DocumentsAsConceptVectorsCompleteLinkClusterer;
import eu.excitementproject.clustering.clustering.impl.completeLink.TermsCompleteLinkClusterer;
import eu.excitementproject.clustering.clustering.impl.kmedoids.DocumentsAsConceptVectorsYClusterer;
import eu.excitementproject.clustering.clustering.impl.kmedoids.TermsYClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.TermToAllLdaTopicsByRelatednessClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.TermToBestLdaTopicByModelClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.TermToBestLdaTopicByRelatednessClusterer;
import eu.excitementproject.clustering.clustering.impl.tc.DocumentByCategoryClusterer;
import eu.excitementproject.clustering.clustering.impl.tc.DocumentByTopKCategoriesClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.experiments.api.AbstractExperimentRunner;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;

/**
 * @author Lili Kotlerman
 *
 */
public class ExpThreeRunner extends AbstractExperimentRunner {

	public ExpThreeRunner(String configurationFileName) throws MalformedURLException, ConfigurationException, LemmatizerException{
		super(configurationFileName);
	}

	public ExpThreeRunner(String configurationFileName, String dataFilename) throws MalformedURLException, ConfigurationException, LemmatizerException{
		super(configurationFileName, dataFilename);
	}

	@Override
	public void runExperiment(String configurationFileName) {
			
		double topKPercentsToRetain = 0.3;
		
		System.out.println(configurationFileName);
			
			List<WeightType> weightTypes = new LinkedList<WeightType>();
			weightTypes.add(WeightType.BINARY); weightTypes.add(WeightType.TF); weightTypes.add(WeightType.TF_IDF);
			
			LinkedList<Integer> clusterNumbers = new LinkedList<Integer>();
			for (int k = 10; k<35; k+=10){
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
			
			for (WeightType weightType : weightTypes){

	
				
		/*		//CompleteLink
				try {
					String settingName="CompleteLink-via-terms-"+weightType;
					System.out.println("\n"+settingName+"\n");
					TermClusterer tClusterer = new TermsCompleteLinkClusterer();
					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");

						tClusterer.setNumberOfTermClusters(k);
						Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
						System.out.println(termClusters);
						
						String tclustfile = m_out_dir+"/completeLink/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".termClusters.txt";
						BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
						tclustWriter.write(getTermClustersForPrint(termClusters));
						tclustWriter.close();

						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(m_useExpandedCollection, termClusters, weightType);

						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();
						
						processResults(settingName, k, res);
						
						// now let's run TC-style clustering with the same term clusters 
						settingName="CompleteLink-via-terms__+TC"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						dClusterer = new DocumentByCategoryClusterer(termClusters, weightType, WeightType.DOCUMENT_FREQUENCY);
						// dClusterer.setNumberOfDocumentClusters(k); - not needed, have k term clusters as input
						res = dClusterer.clusterDocuments(m_textCollection);						
						resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();						
						processResults(settingName, k, res);

					}
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}*/

				//Y-clustering top-K term clusters
				try {
					String settingName="Y-via-terms-top-K"+weightType;
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
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsYClusterer(m_useExpandedCollection, termClusters, weightType, threshold);

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
						
						// now let's run TC-style clustering with the same term clusters 
						settingName="Y-via-terms-top-K__+TC_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						dClusterer = new DocumentByTopKCategoriesClusterer(m_textCollection, termClusters, weightType, WeightType.DF);
						dClusterer.setNumberOfDocumentClusters(k); 
						res = dClusterer.clusterDocuments(m_textCollection);						
						resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), true));
						resWriter.close();

						processResults(settingName, k, res);
						
						// now let's try complete link with the same term clusters
						settingName="Y-via-terms-top-K__+CompleteLink_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(m_useExpandedCollection, termClusters, weightType);
						dClusterer.setNumberOfDocumentClusters(k);
						res = dClusterer.clusterDocuments(m_textCollection);						
						resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();		
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();						
						processResults(settingName, k, res);						
					}
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
			
				//Y-clustering all term clusters
				try {
					String settingName="Y-via-terms-all"+weightType;
					System.out.println("\n"+settingName+"\n");
					// for terms consider score > 0 enough to connect to a cluster 
					TermClusterer tClusterer = new TermsYClusterer(m_useExpandedCollection, weightType, 0.0, false);
					tClusterer.setNumberOfTermClusters(-1); //negative = no limit
					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");

						Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
					//	System.out.println(termClusters);
						
						String tclustfile = m_out_dir+"/y-allTermClusters/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".termClusters.txt";
						BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
						tclustWriter.write(getTermClustersForPrint(termClusters));
						tclustWriter.close();

						
						settingName="Y-via-terms-all_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsYClusterer(m_useExpandedCollection, termClusters, weightType, threshold);

						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), true));
						resWriter.close();

						
						processResults(settingName, k, res);
						
						
						// now let's run TC-style clustering with the same term clusters 
						settingName="Y-via-terms-all__+TC"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						dClusterer = new DocumentByTopKCategoriesClusterer(m_textCollection, termClusters, weightType, WeightType.DF);
						dClusterer.setNumberOfDocumentClusters(k);
						res = dClusterer.clusterDocuments(m_textCollection);						
						resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), true));
						resWriter.close();
						
						processResults(settingName, k, res);
						
						if (termClusters.size()>=k){
							// now let's try complete link with the same term clusters
							settingName="Y-via-terms-all__+CompleteLink_"+weightType;
							System.out.println("\n"+settingName+"\n");
							System.out.println("********** "+k+" **********");
							dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(m_useExpandedCollection, termClusters, weightType);
							dClusterer.setNumberOfDocumentClusters(k);
							res = dClusterer.clusterDocuments(m_textCollection);						
							resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
							resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
							resWriter.write(getDocumentClustersForPrint(res));
							resWriter.close();	
							resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
							resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
							resWriter.close();

							processResults(settingName, k, res);
						}
						
					}
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}

				
				//CW-clustering all term clusters
				try {
					String settingName="CW-via-terms_"+weightType;
					System.out.println("\n"+settingName+"\n");
					// for terms consider score > 0 enough to connect to a cluster 
					TermClusterer tClusterer = new TermsCWClusterer(m_useExpandedCollection, m_textCollection, weightType, m_configurationFileName, 0.0); //similarity threshold = 0 for terms
					
					Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
				//	System.out.println(termClusters);

		/*			// let's run TC-style clustering with the resulting term clusters (no top-K limit) 
					settingName="CW-via-terms + TC_all_"+weightType;
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentByCategoryClusterer(termClusters, weightType, WeightType.DOCUMENT_FREQUENCY);
					Map<String, List<Integer>>  res = dClusterer.clusterDocuments(m_textCollection);						
					String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(termClusters.size())+".results.txt";
					BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
					resWriter.write(getDocumentClustersForPrint(res, true));
					resWriter.close();		
					resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
					resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), true));
					resWriter.close();
					processResults(settingName, termClusters.size(), res);						

					String tclustfile = cwDir.getAbsolutePath()+"/"+m_textCollection.getDatasetName()+"."+settingName+".termClusters.txt";
					BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
					tclustWriter.write(getTermClustersForPrint(termClusters));
					tclustWriter.close();*/

					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						
						settingName="CW-via-terms_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsCWClusterer(termClusters, m_useExpandedCollection, m_textCollection, weightType, m_configurationFileName, threshold);

						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, false));
						resWriter.close();
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), false));
						resWriter.close();

						
						processResults(settingName, k, res);
						
						
						// now let's run top-K TC-style clustering with the same term clusters 
						settingName="CW-via-terms + TC_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						dClusterer = new DocumentByTopKCategoriesClusterer(m_textCollection, termClusters, weightType, WeightType.DF);
						dClusterer.setNumberOfDocumentClusters(k);
						res = dClusterer.clusterDocuments(m_textCollection);						
						resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), true));
						resWriter.close();

						processResults(settingName, k, res);	
						
						if (termClusters.size()>=k){
							// now let's try complete link with the same term clusters
							settingName="CW-via-terms + CompleteLink_"+weightType;
							System.out.println("\n"+settingName+"\n");
							System.out.println("********** "+k+" **********");
							dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(m_useExpandedCollection, termClusters, weightType);
							dClusterer.setNumberOfDocumentClusters(k);
							res = dClusterer.clusterDocuments(m_textCollection);						
							resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
							resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
							resWriter.write(getDocumentClustersForPrint(res));
							resWriter.close();	
							resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
							resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
							resWriter.close();							
							processResults(settingName, k, res);
						}	
						
					}
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}	
				
				
				//LDA-term-clustering
				try {

					String settingName="LDA-terms-byRel-best"+weightType;
					System.out.println("\n"+settingName+"\n");										
					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");

						TermClusterer tClusterer = new TermToBestLdaTopicByRelatednessClusterer(m_useExpandedCollection, m_textCollection, configurationFileName, k, weightType);
						Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
					//	System.out.println(termClusters);
						
						String tclustfile = m_out_dir+"/lda-termClusters/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".termClusters.txt";
						BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
						tclustWriter.write(getTermClustersForPrint(termClusters));
						tclustWriter.close();

						
						// now let's try complete link with the same term clusters
						settingName="LDA-terms-byRel-best + CompleteLink_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(m_useExpandedCollection, termClusters, weightType);
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res = dClusterer.clusterDocuments(m_textCollection);						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();

						processResults(settingName, k, res);	
						
						// now let's run top-K TC-style clustering with the same term clusters 
						settingName="LDA-terms-byRel-best + TC_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						dClusterer = new DocumentByTopKCategoriesClusterer(m_textCollection, termClusters, weightType, WeightType.DF);
						dClusterer.setNumberOfDocumentClusters(k);
						res = dClusterer.clusterDocuments(m_textCollection);						
						resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), true));
						resWriter.close();

						processResults(settingName, k, res);	

					}
					

					settingName="LDA-terms-byModel-best"+weightType;
					System.out.println("\n"+settingName+"\n");										

					TermClusterer tClusterer = new TermToBestLdaTopicByModelClusterer(m_textCollection, configurationFileName);
					Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
				//	System.out.println(termClusters);
					
					String tclustfile = m_out_dir+"/lda-termClusters/"+m_textCollection.getDatasetName()+"."+settingName+".termClusters.txt";
					BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
					tclustWriter.write(getTermClustersForPrint(termClusters));
					tclustWriter.close();

					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						
						// now let's try complete link with the same term clusters
						settingName="LDA-terms-byModel-best + CompleteLink_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(m_useExpandedCollection, termClusters, weightType);
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res = dClusterer.clusterDocuments(m_textCollection);						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();

						processResults(settingName, k, res);		
						
						
						
						// now let's run top-K TC-style clustering with the same term clusters 
						settingName="LDA-terms-byModel-best + TC_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						dClusterer = new DocumentByTopKCategoriesClusterer(m_textCollection, termClusters, weightType, WeightType.DF);
						dClusterer.setNumberOfDocumentClusters(k);
						res = dClusterer.clusterDocuments(m_textCollection);						
						resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), true));
						resWriter.close();

						processResults(settingName, k, res);							
					}
				} catch (ClusteringException | IOException e) {
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
			ExpThreeRunner exp = new ExpThreeRunner(annotationFile.getAbsolutePath());
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

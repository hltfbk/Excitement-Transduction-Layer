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
import eu.excitementproject.clustering.clustering.impl.cw.DocumentsAsConceptVectorsCWClusterer;
import eu.excitementproject.clustering.clustering.impl.cw.OriginalTermsCWClusterer;
import eu.excitementproject.clustering.clustering.impl.cw.TermsCWClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.DocumentToBestLdaTopicClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.TermToBestLdaTopicByLocalModelClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.TermToBestLdaTopicByModelClusterer;
import eu.excitementproject.clustering.clustering.impl.tc.DocumentByCategoryClusterer;
import eu.excitementproject.clustering.clustering.impl.tc.DocumentByTopKCategoriesClusterer;
import eu.excitementproject.clustering.clustering.impl.tc.DocumentByTopKCategoriesClustererWThreshold;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.clustering.impl.yclust.DocumentsAsConceptVectorsYClusterer;
import eu.excitementproject.clustering.clustering.impl.yclust.DocumentsAsTermVectorsYClusterer;
import eu.excitementproject.clustering.clustering.impl.yclust.OriginalTermsYClusterer;
import eu.excitementproject.clustering.clustering.impl.yclust.TermsYClusterer;
import eu.excitementproject.clustering.experiments.api.AbstractExperimentRunner;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFileDuplicateKeyException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;

/**
 * @author Lili Kotlerman
 *
 */
public class ExpCombinedRunner extends AbstractExperimentRunner {

	public ExpCombinedRunner(String configurationFileName) throws MalformedURLException, ConfigurationException, LemmatizerException{
		super(configurationFileName);
	}

	public ExpCombinedRunner(String configurationFileName, String dataFilename) throws MalformedURLException, ConfigurationException, LemmatizerException{
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
			
			for (WeightType weightType : weightTypes){
				int[] featureCutOffs = {1,2,5};
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
							settingName="Y-via-terms-top-K_topFeatures_"+String.valueOf(topKfeatures)+"_"+weightType;							
							dClusterer.setTopKFeatures(topKfeatures);
							res = dClusterer.clusterDocuments(m_textCollection);							
							resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
							resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
							resWriter.write(getDocumentClustersForPrint(res, true));
							resWriter.close();							
							processResults(settingName, k, res);														
						}												
						
						// now let's try complete link with the same term clusters
						settingName="Y-via-terms-top-K__+CompleteLink_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						DocumentsAsConceptVectorsCompleteLinkClusterer dCLClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(false, termClusters, weightType);
						dCLClusterer.setNumberOfDocumentClusters(k);
						res = dCLClusterer.clusterDocuments(m_textCollection);						
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
				
				
				//Y-clustering top-K-multi term clusters
				try {
					String settingName="Y-via-terms-MULTI-top-K_"+weightType;
					System.out.println("\n"+settingName+"\n");
					// for terms consider score > 0 enough to connect to a cluster 
					TermClusterer tClusterer = new TermsYClusterer(m_useExpandedCollection, weightType, 0.0, true);
					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");

						tClusterer.setNumberOfTermClusters(k);
						Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
					//	System.out.println(termClusters);
						
						String tclustfile = m_out_dir+"/y-topKtermClusters/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".termClusters.txt";
						BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
						tclustWriter.write(getTermClustersForPrint(termClusters));
						tclustWriter.close();

						
						settingName="Y-via-terms-MULTI-top-K"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);

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
						
						// now let's try complete link with the same term clusters
						settingName="Y-via-terms-MULTI-top-K+CompleteLink_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(false, termClusters, weightType);
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
					
					Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
				//	System.out.println(termClusters);
					
					String tclustfile = m_out_dir+"/y-allTermClusters/"+m_textCollection.getDatasetName()+"."+settingName+".termClusters.txt";
					BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
					tclustWriter.write(getTermClustersForPrint(termClusters));
					tclustWriter.close();

					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						
						settingName="Y-via-terms-all_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);

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
						
						
						if (termClusters.size()>=k){
							// now let's try complete link with the same term clusters
							settingName="Y-via-terms-all__+CompleteLink_"+weightType;
							System.out.println("\n"+settingName+"\n");
							System.out.println("********** "+k+" **********");
							dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(false, termClusters, weightType);
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

				//Y-clustering all-multi term clusters
				try {
					String settingName="Y-via-terms-MULTI-all"+weightType;
					System.out.println("\n"+settingName+"\n");
					// for terms consider score > 0 enough to connect to a cluster 
					TermClusterer tClusterer = new TermsYClusterer(m_useExpandedCollection, weightType, 0.0, true);
					tClusterer.setNumberOfTermClusters(-1); //negative = no limit
					
					Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
				//	System.out.println(termClusters);
					
					String tclustfile = m_out_dir+"/y-allTermClusters/"+m_textCollection.getDatasetName()+"."+settingName+".termClusters.txt";
					BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
					tclustWriter.write(getTermClustersForPrint(termClusters));
					tclustWriter.close();

					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						
						settingName="Y-via-terms-MULTI-all_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);

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
						
							
						if (termClusters.size()>=k){
							// now let's try complete link with the same term clusters
							settingName="Y-via-terms-MULTI-all__+CompleteLink_"+weightType;
							System.out.println("\n"+settingName+"\n");
							System.out.println("********** "+k+" **********");
							dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(false, termClusters, weightType);
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
					TermClusterer tClusterer = new OriginalTermsCWClusterer(m_useExpandedCollection, m_textCollection, weightType, m_configurationFileName, 0.0); //similarity threshold = 0 for terms
					
					Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
				//	System.out.println(termClusters);

					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						
						settingName="CW-via-terms_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsCWClusterer(termClusters, false, m_textCollection, weightType, m_configurationFileName, threshold);

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
						
						
						
						// now let's run Y-doc clustering with CW term clusters
						settingName="CW-via-terms +Y-doc_"+weightType;
						// for documents let's set the threshold = 0.7
						threshold = 0.7;
						DocumentsAsConceptVectorsYClusterer dYClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);

						dYClusterer.setNumberOfDocumentClusters(k);
						res = dYClusterer.clusterDocuments(m_textCollection);
						
						resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain), true));
						resWriter.close();
						
						processResults(settingName, k, res);
												
						
						// now let's play with cutting top K features
						
						for(int topKfeatures : featureCutOffs){						
							settingName="CW-via-terms +Y-doc_topFeatures_"+String.valueOf(topKfeatures)+"_"+weightType;							
							dYClusterer.setTopKFeatures(topKfeatures);
							res = dYClusterer.clusterDocuments(m_textCollection);							
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
				
				//LDA-local(exp)- doc & term-clustering
				try {

					for (int k : clusterNumbers){

						// create localLDAClusterer, whose constructor trains a local model for k topics and creates a term-prob file 
						DocumentClusterer localLDAClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType);
						// cluster documents with that model
						String settingName="LDA-localExpModel-best"+weightType;
						System.out.println("\n"+settingName+"\n");										
						System.out.println("********** "+k+" **********");
						localLDAClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = localLDAClusterer.clusterDocuments(m_textCollection);	
														
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getClustersForPrint(res, getLdaTopicLabels(k)));
						resWriter.close();
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						
						processResults(settingName, k, res);

						
						
						
						// now use the newly-created file as a model for term clustering
												
						settingName="LDA-terms-localExpModel-best"+weightType;
						System.out.println("\n"+settingName+"\n");										
						System.out.println("********** "+k+" **********");						
						
						ConfigurationFile cf = new ConfigurationFile(m_configurationFileName);
						ConfigurationParams cp = cf.getModuleConfiguration("context-tester");		
						File newProbFile = new File(cp.get("topic-top-words-file").replace(".txt","_"+String.valueOf(k)+".txt"));

						TermToBestLdaTopicByModelClusterer tClusterer = new TermToBestLdaTopicByLocalModelClusterer(m_textCollection, configurationFileName, newProbFile);
						Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
						String tclustfile = m_out_dir+"/lda-termClusters/"+m_textCollection.getDatasetName()+"."+settingName+"_"+String.valueOf(k)+".termClusters.txt";
						BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
						tclustWriter.write(getTermClustersForPrint(termClusters));
						tclustWriter.close();
						
						
						
						// now let's try complete link with the same term clusters
						settingName="LDA-terms-localExpModel-best + CompleteLink_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(m_useExpandedCollection, termClusters, weightType);
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
						
						// now let's run Y doc clustering with the same term clusters 
						settingName="LDA-terms-localExpModel-best + Ydoc_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						dClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);
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
				} catch (ClusteringException | IOException | ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
				
				
				
				//LDA-ukwac-term-clustering
				try {

					String settingName="LDA-terms-byModel-best"+weightType;
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
						
						// now let's run Y doc clustering with the same term clusters 
						settingName="LDA-terms-byModel-best + Ydoc_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						dClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);
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
				
				
				//CompleteLink
				try {
					String settingName="CompleteLink-"+weightType;
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentsAsTermVectorsCompleteLinkClusterer(m_useExpandedCollection, weightType);
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();	
						
						
						processResults(settingName, k, res);
					}
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
				
				// Co-clusterer with number of term clusters = k
				try {
					String settingName="Co-clustering-"+weightType;
					DhillonCoClusterer clusterer = new DhillonCoClusterer(m_useExpandedCollection, m_textCollection, configurationFileName, weightType);
					System.out.println("\n"+settingName+"\n");
					for (int k : clusterNumbers){
						clusterer.setNumberOfTermClusters(k);
						System.out.println("********** "+k+" **********");
						clusterer.setNumberOfDocumentClusters(k);	
						Map<String, List<Integer>> res;
						res = clusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();	
						
						
						processResults(settingName, k, res);
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
							settingName="Y-clust-th0.7_topFeatures_"+String.valueOf(topKfeatures)+"_"+weightType;							
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
						

			// now do the same just use original term clustering (vs. expanded) 
			// or original unexpanded collections for methods not based on term clustering
			
			for (WeightType weightType : weightTypes){
				int[] featureCutOffs = {1,2,5};
				//Y-clustering top-K term clusters
				try {
					String settingName="Y-via-Orig-terms-top-K_"+weightType;
					System.out.println("\n"+settingName+"\n");
					// for terms consider score > 0 enough to connect to a cluster 
					TermClusterer tClusterer = new OriginalTermsYClusterer(m_useExpandedCollection, weightType, 0.0, false);
					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");

						tClusterer.setNumberOfTermClusters(k);
						Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
					//	System.out.println(termClusters);
						
						String tclustfile = m_out_dir+"/y-topKtermClusters/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".termClusters.txt";
						BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
						tclustWriter.write(getTermClustersForPrint(termClusters));
						tclustWriter.close();

						
						settingName="Y-via-Orig-terms-top-K_"+weightType;
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
							settingName="Y-via-Orig-terms-top-K_topFeatures_"+String.valueOf(topKfeatures)+"_"+weightType;							
							dClusterer.setTopKFeatures(topKfeatures);
							res = dClusterer.clusterDocuments(m_textCollection);							
							resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
							resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
							resWriter.write(getDocumentClustersForPrint(res, true));
							resWriter.close();							
							processResults(settingName, k, res);														
						}												
						
						// now let's try complete link with the same term clusters
						settingName="Y-via-Orig-terms-top-K__+CompleteLink_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						DocumentsAsConceptVectorsCompleteLinkClusterer dCLClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(false, termClusters, weightType);
						dCLClusterer.setNumberOfDocumentClusters(k);
						res = dCLClusterer.clusterDocuments(m_textCollection);						
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
				
				
				//Y-clustering top-K-multi term clusters
				try {
					String settingName="Y-via-Orig-terms-MULTI-top-K_"+weightType;
					System.out.println("\n"+settingName+"\n");
					// for terms consider score > 0 enough to connect to a cluster 
					TermClusterer tClusterer = new OriginalTermsYClusterer(m_useExpandedCollection, weightType, 0.0, true);
					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");

						tClusterer.setNumberOfTermClusters(k);
						Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
					//	System.out.println(termClusters);
						
						String tclustfile = m_out_dir+"/y-topKtermClusters/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".termClusters.txt";
						BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
						tclustWriter.write(getTermClustersForPrint(termClusters));
						tclustWriter.close();

						
						settingName="Y-via-Orig-terms-MULTI-top-K"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);

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
						
						// now let's try complete link with the same term clusters
						settingName="Y-via-Orig-terms-MULTI-top-K+CompleteLink_"+weightType;
						System.out.println("\n"+settingName+"\n");
						System.out.println("********** "+k+" **********");
						dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(false, termClusters, weightType);
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
					String settingName="Y-via-Orig-terms-all"+weightType;
					System.out.println("\n"+settingName+"\n");
					// for terms consider score > 0 enough to connect to a cluster 
					TermClusterer tClusterer = new OriginalTermsYClusterer(m_useExpandedCollection, weightType, 0.0, false);
					tClusterer.setNumberOfTermClusters(-1); //negative = no limit
					
					Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
				//	System.out.println(termClusters);
					
					String tclustfile = m_out_dir+"/y-allTermClusters/"+m_textCollection.getDatasetName()+"."+settingName+".termClusters.txt";
					BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
					tclustWriter.write(getTermClustersForPrint(termClusters));
					tclustWriter.close();

					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						
						settingName="Y-via-Orig-terms-all_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);

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
						
						
						if (termClusters.size()>=k){
							// now let's try complete link with the same term clusters
							settingName="Y-via-Orig-terms-all__+CompleteLink_"+weightType;
							System.out.println("\n"+settingName+"\n");
							System.out.println("********** "+k+" **********");
							dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(false, termClusters, weightType);
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

				//Y-clustering all-multi term clusters
				try {
					String settingName="Y-via-Orig-terms-MULTI-all"+weightType;
					System.out.println("\n"+settingName+"\n");
					// for terms consider score > 0 enough to connect to a cluster 
					TermClusterer tClusterer = new OriginalTermsYClusterer(m_useExpandedCollection, weightType, 0.0, true);
					tClusterer.setNumberOfTermClusters(-1); //negative = no limit
					
					Map<String, List<String>> termClusters = tClusterer.clusterTerms(m_textCollection);
				//	System.out.println(termClusters);
					
					String tclustfile = m_out_dir+"/y-allTermClusters/"+m_textCollection.getDatasetName()+"."+settingName+".termClusters.txt";
					BufferedWriter tclustWriter = new BufferedWriter(new FileWriter(new File(tclustfile)));
					tclustWriter.write(getTermClustersForPrint(termClusters));
					tclustWriter.close();

					
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						
						settingName="Y-via-Orig-terms-MULTI-all_"+weightType;
						// for documents let's set the threshold = 0.7
						double threshold = 0.7;
						DocumentClusterer dClusterer = new DocumentsAsConceptVectorsYClusterer(false, termClusters, weightType, threshold);

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
						
							
						if (termClusters.size()>=k){
							// now let's try complete link with the same term clusters
							settingName="Y-via-Orig-terms-MULTI-all__+CompleteLink_"+weightType;
							System.out.println("\n"+settingName+"\n");
							System.out.println("********** "+k+" **********");
							dClusterer = new DocumentsAsConceptVectorsCompleteLinkClusterer(false, termClusters, weightType);
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
				
				//LDA-local(non-expanded)- doc clustering
				try {

					for (int k : clusterNumbers){

						// create localLDAClusterer, whose constructor trains a local model for k topics and creates a term-prob file 
						DocumentClusterer localLDAClusterer = new DocumentToBestLdaTopicClusterer(false, m_textCollection, m_configurationFileName, k, weightType);
						// cluster documents with that model
						String settingName="LDA-local-best"+weightType;
						System.out.println("\n"+settingName+"\n");										
						System.out.println("********** "+k+" **********");
						localLDAClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = localLDAClusterer.clusterDocuments(m_textCollection);	
														
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getClustersForPrint(res, getLdaTopicLabels(k)));
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
				
				
				//CompleteLink
				try {
					String settingName="CompleteLink-NONEXP-"+weightType;
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentsAsTermVectorsCompleteLinkClusterer(false, weightType);
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();	
						
						
						processResults(settingName, k, res);
					}
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
				
				// Co-clusterer with number of term clusters = k
				try {
					String settingName="Co-clustering-NONEXP-"+weightType;
					DhillonCoClusterer clusterer = new DhillonCoClusterer(false, m_textCollection, configurationFileName, weightType);
					System.out.println("\n"+settingName+"\n");
					for (int k : clusterNumbers){
						clusterer.setNumberOfTermClusters(k);
						System.out.println("********** "+k+" **********");
						clusterer.setNumberOfDocumentClusters(k);	
						Map<String, List<Integer>> res;
						res = clusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();	
						
						
						processResults(settingName, k, res);
					}
				} catch (ClusteringException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				
				
				//Y-clustering threshold = 0.7
				try {
					String settingName="Y-clust-th0.7-NONEXP-"+weightType;
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
			ExpCombinedRunner exp = new ExpCombinedRunner(annotationFile.getAbsolutePath());
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
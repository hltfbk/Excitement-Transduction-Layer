/**
 * 
 */
package eu.excitementproject.clustering.experiments.impl.exp1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.chinesewhispers.DocumentsAsTermVectorsCWClusterer;
import eu.excitementproject.clustering.clustering.impl.coclustering.DhillonCoClusterer;
import eu.excitementproject.clustering.clustering.impl.completeLink.DocumentsAsTermVectorsCompleteLinkClusterer;
import eu.excitementproject.clustering.clustering.impl.kmedoids.DocumentsAsTermVectorsYClusterer;
import eu.excitementproject.clustering.clustering.impl.lda.DocumentToBestLdaTopicClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.experiments.api.AbstractExperimentRunner;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;

/**
 * @author Lili Kotlerman
 *
 */
public class ExpOneRunner extends AbstractExperimentRunner {

	public ExpOneRunner(String configurationFileName) throws MalformedURLException, ConfigurationException, LemmatizerException{
		super(configurationFileName);
	}

	public ExpOneRunner(String configurationFileName, String dataFilename) throws MalformedURLException, ConfigurationException, LemmatizerException{
		super(configurationFileName, dataFilename);
	}

	


	@Override
	public void runExperiment(String configurationFileName) {
		
		File resDir = new File(m_out_dir+"/res/");
		if (!resDir.exists()) resDir.mkdir();
			
		double topKPercentsToRetain = 0.3;
		
		System.out.println(configurationFileName);
			
			List<WeightType> weightTypes = new LinkedList<WeightType>();
			//		weightTypes.add(WeightType.BINARY); weightTypes.add(WeightType.TF); weightTypes.add(WeightType.TF_IDF);
			weightTypes.add(WeightType.TF); 
			
			LinkedList<Integer> clusterNumbers = new LinkedList<Integer>();
			for (int k = 10; k<=50; k+=5){
				clusterNumbers.add(k);
			}
			
/*			// uncomment to use various numbers of topics
			LinkedList<Integer> assumedConceptNumbers = new LinkedList<Integer>();
			assumedConceptNumbers.add(100); assumedConceptNumbers.add(50); assumedConceptNumbers.add(25); assumedConceptNumbers.add(10); 
			assumedConceptNumbers.add((m_textCollection.getDocIdsByOriginalTerm().size()+m_textCollection.getDocIdsByExpansionTerm().size())/2);
			assumedConceptNumbers.add((m_textCollection.getDocIdsByOriginalTerm().size()+m_textCollection.getDocIdsByExpansionTerm().size())/3);
			assumedConceptNumbers.add((m_textCollection.getDocIdsByOriginalTerm().size()+m_textCollection.getDocIdsByExpansionTerm().size())/4);
			assumedConceptNumbers.add((m_textCollection.getDocIdsByOriginalTerm().size()+m_textCollection.getDocIdsByExpansionTerm().size())/5);
			assumedConceptNumbers.add((m_textCollection.getDocIdsByOriginalTerm().size()+m_textCollection.getDocIdsByExpansionTerm().size())/10);
			
			for (int numOfTopics : assumedConceptNumbers){
				try {
					String settingName="LDA-local-Tf/"+String.valueOf(totalTerms/numOfTopics)+"("+String.valueOf(numOfTopics)+" of "+String.valueOf(totalTerms)+")";
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentAsBestLdaTopicCompleteLinkClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, numOfTopics, WeightType.TF);
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);					
						processResults(settingName, k, res);
					} 
				} catch (ClusteringException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
			}
			
			try {
				DhillonCoClusterer clusterer = new DhillonCoClusterer(m_useExpandedCollection, m_textCollection, configurationFileName, WeightType.TF_IDF);
				for (int assumedConceptNumber : assumedConceptNumbers){
					String settingName="Co-clustering-/"+String.valueOf(totalTerms/assumedConceptNumber)+"("+String.valueOf(assumedConceptNumber)+" of "+String.valueOf(totalTerms)+")";
					System.out.println("\n"+settingName+"\n");
					clusterer.setNumberOfTermClusters(assumedConceptNumber);
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						clusterer.setNumberOfDocumentClusters(k);	
						Map<String, List<Integer>> res;
						res = clusterer.clusterDocuments(m_textCollection);
						processResults(settingName, k, res);
					}
				}
			} catch (ClusteringException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}							
*/
			
			for (WeightType weightType : weightTypes){

				// LDA with number of topics = k
				try {					
					for (int k : clusterNumbers){
//						DocumentClusterer dClusterer = new DocumentAsBestLdaTopicCompleteLinkClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType);
	
						// clustering with local model
						String settingName="LDA-local-best-"+weightType;
						System.out.println("\n"+settingName+"\n");						
						DocumentClusterer dClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType);
	//					DocumentToBestLdaTopicClusterer dClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType, "C:/Users/Lili/git/nlp-lab/Trunk/Lili/lda/src/test/resources/outputs/mallet/out.serializedModel");

		/*				// clustering with external model
						settingName="LDA-ukwac-best-"+weightType;
//						String externalModelFilename = "C:/Users/Lili/Desktop/ukwac/"+String.valueOf(k)+"_ukwac.out.serializedModel";
						String externalModelFilename = m_externalModelFilename.replace("ukwac.out.serializedModel", String.valueOf(k)+"_ukwac.out.serializedModel");
						System.out.println("\n"+settingName+"\n");
						DocumentClusterer dClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType, externalModelFilename);
*/						
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);	
														
/*						File resDir = new File(m_out_dir+"/res/");
						if (!resDir.exists()) resDir.mkdir();
*/						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getClustersForPrint(res, getLdaTopicLabels(k)));
						resWriter.close();
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						

						processResults(settingName, k, res);
						
						//rename files 
						File ldaDir = new File(m_out_dir+"/lda");
						if (ldaDir.isDirectory()){
							for (String filename : ldaDir.list()){
								if (filename.contains("out.")){
									File oldFile = new File(m_out_dir+"/lda/"+filename);
									File newFile = new File(m_out_dir+"/lda/"+filename.replace("out.", settingName+"."));
									oldFile.renameTo(newFile);
								}
								else if (filename.equals("lda_docs.txt")){
									File oldFile = new File(m_out_dir+"/lda/"+filename);
									File newFile = new File(m_out_dir+"/lda/"+settingName+"_docs.txt");	
									oldFile.renameTo(newFile);
								}
							}
						}
						
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
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
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
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						
						processResults(settingName, k, res);
					}
				} catch (ClusteringException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}							
				
	/*			//Y-clustering threshold = 0.5
				try {
					String settingName="Y-clust-th0.5-"+weightType;
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentsAsTermVectorsYClusterer(m_useExpandedCollection, weightType, 0.5);
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						
						processResults(settingName, k, res);
					}
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}*/
			
				//Y-clustering threshold = 0.7
				try {
					String settingName="Y-clust-th0.7-"+weightType;
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentsAsTermVectorsYClusterer(m_useExpandedCollection, weightType, 0.7);
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
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

	/*			//CW-clustering threshold = 0.5
				try {
					String settingName="CW-clust-th0.5"+weightType;
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentsAsTermVectorsCWClusterer(m_useExpandedCollection, m_textCollection, weightType, m_configurationFileName, 0.5);
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						
						processResults(settingName, k, res);
					}
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}*/

			
				//CW-clustering threshold = 0.7
				try {
					String settingName="CW-clust-th0.7"+weightType;
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentsAsTermVectorsCWClusterer(m_useExpandedCollection, m_textCollection, weightType, m_configurationFileName, 0.7);
					for (int k : clusterNumbers){
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
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
				
			}
			
	}
	

	
	public void runExperiment(String configurationFileName, int k) {
		
		File resDir = new File(m_out_dir+"/res/");
		if (!resDir.exists()) resDir.mkdir();
			
		double topKPercentsToRetain = 0.3;
		
		System.out.println(configurationFileName);
			
			List<WeightType> weightTypes = new LinkedList<WeightType>();
			weightTypes.add(WeightType.BINARY); weightTypes.add(WeightType.TF); weightTypes.add(WeightType.TF_IDF);
			// weightTypes.add(WeightType.TF); 
			
	
	
			for (WeightType weightType : weightTypes){

				// LDA with number of topics = k
				try {					

//						DocumentClusterer dClusterer = new DocumentAsBestLdaTopicCompleteLinkClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType);
	
						// clustering with local model
						String settingName="LDA-local-best-"+weightType;
						System.out.println("\n"+settingName+"\n");						
						DocumentClusterer dClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType);
	//					DocumentToBestLdaTopicClusterer dClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType, "C:/Users/Lili/git/nlp-lab/Trunk/Lili/lda/src/test/resources/outputs/mallet/out.serializedModel");

		/*				// clustering with external model
						settingName="LDA-ukwac-best-"+weightType;
//						String externalModelFilename = "C:/Users/Lili/Desktop/ukwac/"+String.valueOf(k)+"_ukwac.out.serializedModel";
						String externalModelFilename = m_externalModelFilename.replace("ukwac.out.serializedModel", String.valueOf(k)+"_ukwac.out.serializedModel");
						System.out.println("\n"+settingName+"\n");
						DocumentClusterer dClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType, externalModelFilename);
*/						
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);	
														
/*						File resDir = new File(m_out_dir+"/res/");
						if (!resDir.exists()) resDir.mkdir();
*/						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getClustersForPrint(res, getLdaTopicLabels(k)));
						resWriter.close();
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						

						processResults(settingName, k, res);
						
						//rename files 
						File ldaDir = new File(m_out_dir+"/lda");
						if (ldaDir.isDirectory()){
							for (String filename : ldaDir.list()){
								if (filename.contains("out.")){
									File oldFile = new File(m_out_dir+"/lda/"+filename);
									File newFile = new File(m_out_dir+"/lda/"+filename.replace("out.", settingName+"."));
									oldFile.renameTo(newFile);
								}
								else if (filename.equals("lda_docs.txt")){
									File oldFile = new File(m_out_dir+"/lda/"+filename);
									File newFile = new File(m_out_dir+"/lda/"+settingName+"_docs.txt");	
									oldFile.renameTo(newFile);
								}
							}
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
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						
						processResults(settingName, k, res);

				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}
				
				// Co-clusterer with number of term clusters = k
				try {
					String settingName="Co-clustering-"+weightType;
					DhillonCoClusterer clusterer = new DhillonCoClusterer(m_useExpandedCollection, m_textCollection, configurationFileName, weightType);
					System.out.println("\n"+settingName+"\n");
						clusterer.setNumberOfTermClusters(k);
						System.out.println("********** "+k+" **********");
						clusterer.setNumberOfDocumentClusters(k);	
						Map<String, List<Integer>> res;
						res = clusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						
						processResults(settingName, k, res);

				} catch (ClusteringException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}							
			
				//Y-clustering threshold = 0.7
				try {
					String settingName="Y-clust-th0.7-"+weightType;
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentsAsTermVectorsYClusterer(m_useExpandedCollection, weightType, 0.7);
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						
						processResults(settingName, k, res);
				} catch (ClusteringException | IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
				}


			
				//CW-clustering threshold = 0.7
				try {
					String settingName="CW-clust-th0.7"+weightType;
					System.out.println("\n"+settingName+"\n");
					DocumentClusterer dClusterer = new DocumentsAsTermVectorsCWClusterer(m_useExpandedCollection, m_textCollection, weightType, m_configurationFileName, 0.7);
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);
						
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
						BufferedWriter resWriter = new BufferedWriter(new FileWriter(new File(resfile)));
						resWriter.write(getDocumentClustersForPrint(res, true));
						resWriter.close();	
						resWriter = new BufferedWriter(new FileWriter(new File(resfile.replace(".results.txt", ".resultsTop30Percent.txt"))));
						resWriter.write(getDocumentClustersForPrint(cutOffClusters(res, topKPercentsToRetain)));
						resWriter.close();
						
						processResults(settingName, k, res);
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
			ExpOneRunner exp = new ExpOneRunner(annotationFile.getAbsolutePath());
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

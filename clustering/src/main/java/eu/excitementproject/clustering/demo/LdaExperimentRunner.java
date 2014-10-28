/**
 * 
 */
package eu.excitementproject.clustering.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.exceptions.ClusteringException;
import eu.excitementproject.clustering.clustering.impl.lda.DocumentToBestLdaTopicClusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;

/**
 * @author Lili Kotlerman
 *
 */
public class LdaExperimentRunner extends AbstractDemoRunner {

	public LdaExperimentRunner(String configurationFileName) throws ClusteringException{
		super(configurationFileName);
	}

	public LdaExperimentRunner(String configurationFileName, String dataFilename) throws ClusteringException{
		super(configurationFileName, dataFilename);
	}


	@Override
	public void runDemo(String configurationFileName) {
		
			System.out.println(configurationFileName);
			
			List<WeightType> weightTypes = new LinkedList<WeightType>();
			weightTypes.add(WeightType.BINARY); weightTypes.add(WeightType.TF); weightTypes.add(WeightType.TF_IDF);
			weightTypes.add(WeightType.DF); weightTypes.add(WeightType.TF_DF);
			
			LinkedList<Integer> clusterNumbers = new LinkedList<Integer>();
			for (int k = 10; k<=30; k+=5){
				clusterNumbers.add(k);
			}

			File resDir = new File(m_out_dir+"/res/");
			if (!resDir.exists()) resDir.mkdir();

			
			for (WeightType weightType : weightTypes){

				// LDA-ukwac with number of topics = k
					
				for (int k : clusterNumbers){	
					try {
							// clustering with external model
						String settingName="LDA-ukwac-best-"+weightType;
//						String externalModelFilename = "C:/Users/Lili/Desktop/ukwac/"+String.valueOf(k)+"_ukwac.out.serializedModel";
						String externalModelFilename = m_externalModelFilename.replace("ukwac.out.serializedModel", String.valueOf(k)+"_ukwac.out.serializedModel");
						System.out.println("\n"+settingName+"\n");
						DocumentClusterer dClusterer = new DocumentToBestLdaTopicClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType, externalModelFilename);
						
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);	
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
						
						} catch (ClusteringException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
					}	

				} 

		/*		// LDA-ukwac with number of topics 10K
				try {					
					for (int k : clusterNumbers){	
						// clustering with external model
						String settingName="LDA-ukwac-best-10K-"+weightType;
						String externalModelFilename = m_externalModelFilename.replace("ukwac.out.serializedModel", "10000_ukwac.out.serializedModel");
						System.out.println("\n"+settingName+"\n");
						DocumentClusterer dClusterer = new DocumentToBestLdaTopicTopKClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType, externalModelFilename);
						
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);	
														
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
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
				}		*/		
				
				// LDA-ukwac with number of topics 1000
/*				for (int k : clusterNumbers){	
					try {					
						// clustering with external model
						String settingName="LDA-ukwac-best-1000-"+weightType;
						String externalModelFilename = m_externalModelFilename.replace("ukwac.out.serializedModel", "1000_ukwac.out.serializedModel");
						System.out.println("\n"+settingName+"\n");
						DocumentClusterer dClusterer = new DocumentToBestLdaTopicTopKClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType, externalModelFilename);
						
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);	
														
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
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
						
				} */
				

				
	/*			// LDA-ukwac with number of topics 100
				try {					
					for (int k : clusterNumbers){	
						// clustering with external model
						String settingName="LDA-ukwac-best-100-"+weightType;
						String externalModelFilename = m_externalModelFilename.replace("ukwac.out.serializedModel", "100_ukwac.out.serializedModel");
						System.out.println("\n"+settingName+"\n");
						DocumentClusterer dClusterer = new DocumentToBestLdaTopicTopKClusterer(m_useExpandedCollection, m_textCollection, m_configurationFileName, k, weightType, externalModelFilename);
						
						System.out.println("********** "+k+" **********");
						dClusterer.setNumberOfDocumentClusters(k);
						Map<String, List<Integer>> res;
						res = dClusterer.clusterDocuments(m_textCollection);	
														
						String resfile = m_out_dir+"/res/"+m_textCollection.getDatasetName()+"."+settingName+"."+String.valueOf(k)+".results.txt";
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
				}	*/
				
/*				//CompleteLink
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
						resWriter.write(getClustersForPrint(res));
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
						resWriter.write(getClustersForPrint(res));
						resWriter.close();						
						
						processResults(settingName, k, res);
					}
				} catch (ClusteringException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		*/					
				
			
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
			LdaExperimentRunner exp = new LdaExperimentRunner(annotationFile.getAbsolutePath());
			BufferedWriter writer = new BufferedWriter(new FileWriter (new File(outdir+"/"+annotationFile.getName().replace(".xml", ".log.txt"))));
			exp.runDemo(args[0]);
			writer.write(exp.printAllResults(0));
			writer.write(exp.printRecallPrecisionCurvesData(0));
			writer.close();
		} catch (ClusteringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

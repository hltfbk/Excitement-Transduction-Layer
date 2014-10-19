package eu.excitementproject.clustering.legacy;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import eu.excitementproject.clustering.clustering.api.DocumentClusterer;
import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 * 
 * Implementation of the Y-clustering algorithm by Hui Ye and Steve Young, "A clustering approach to semantic decoding." INTERSPEECH. 2006. *
 */
public abstract class AbstractDocumentYClustererOld implements DocumentClusterer {

	int m_topKclusters = 10;
	boolean m_useExpandedCollection;
	WeightType m_weightType;
	double m_similarityThreshold; // similarity value, which is enough to assign a document to a class (required by the algorithm)  
		
	public AbstractDocumentYClustererOld(boolean useExpandedCollection, WeightType weightType, Double similarityThreshold) {
		m_useExpandedCollection = useExpandedCollection;
		m_weightType = weightType;
		m_similarityThreshold = similarityThreshold;
	}

	@Override
	public Map<String, List<Integer>> clusterDocuments(
			TextCollection textCollection) {
		// represent documents as vectors and cluster the vectors using cosine distance
		return vectorClusterer(representDocuments(textCollection), m_topKclusters);
	}
	
	protected abstract Set<VectorRepresentation> representDocuments(TextCollection textCollection);
	
	private Map<String,List<Integer>> vectorClusterer(Set<VectorRepresentation> documents, int topKClusters){

		// step 1 - init
		// assign maxT = number of docs / 2 (heuristic decision) - the max number of templates (cluster centroids) to generate in each pass
		int maxT = documents.size()/2;
		
		
		// step 2 - init cont.
		Set<VectorRepresentation> outOfClassDocuments = new HashSet<VectorRepresentation>();
		List<VectorRepresentation> templates = new LinkedList<VectorRepresentation>(); // order is important for the following iterations

		int i=0;
		for (VectorRepresentation document : documents){
			outOfClassDocuments.add(document);
			i++;
			if (i <= maxT){
				templates.add(document); // use first maxT documents as the initial templates
			}
		}
		System.out.println(String.valueOf(outOfClassDocuments.size())+" documents are to be assigned");
		
		// no need to clean before each iteration - clustering results iteratively change, new documents being added and new keys being chosen for each cluster 
		Map<VectorRepresentation,List<VectorRepresentation>> clusteringResults = new HashMap<VectorRepresentation, List<VectorRepresentation>>();

		boolean stopCondition = false;
		while(!stopCondition){
			//step 3
			Set<VectorRepresentation> inClassDocuments = new HashSet<VectorRepresentation>(); 
					
			for (VectorRepresentation document : outOfClassDocuments){
				for (VectorRepresentation template : templates){
					double sim = SimilarityAndRelatednessCalculator.cosineSimilarity(document, template);
					if (sim > m_similarityThreshold){
						// assign current document to current template 
						List<VectorRepresentation> docsInCluster = new LinkedList<VectorRepresentation>();
						if (clusteringResults.containsKey(template)) docsInCluster=clusteringResults.get(template);
						docsInCluster.add(document);
						clusteringResults.put(template, docsInCluster);
						// save this document to the set of inClassDocuments - those, for which a high-confidence (>threshold) template was found
						inClassDocuments.add(document);
						// now go to the next document
						break; // leave the for(VectorRepresentation template : templates) cycle and go up to the next document
					}
				}
			}
			//update the outOfClass documents to exclude all the docs, which found their clusters
			System.out.println("Assigned "+String.valueOf(inClassDocuments.size())+" new documents");
			outOfClassDocuments.removeAll(inClassDocuments);
			System.out.println(String.valueOf(outOfClassDocuments.size())+" documents are still not assigned");
			
			//step 4-5
			// - sort clusters desc by the number of assigned sentences (step4) 
			// - remove clusters which ended up with no documents (step4)
			// - recalculate template for each cluster (step5)
			clusteringResults = updateClusteringResults(clusteringResults);
			templates = getSortedTemplates(clusteringResults);

			
			// step 6
			// if the number of outOfClass documents is NOT lower than in prev iteration = if there were NO new inClassDocuments
			// then stop condition is reached
			if (inClassDocuments.isEmpty()) stopCondition=true; 					
		}
		
		//step 7 - final iteration
		// - leave only top-K templates in the template list
		// - assign each document to the nearest template
		
		templates = getSortedTemplates(clusteringResults, m_topKclusters);
		Map<String,List<Integer>> finalResults = new HashMap<String, List<Integer>>();
		for (VectorRepresentation document : documents){ // assign to the template with the highest sim value
			double maxSim = 0.0;
			String bestTemplate = "";
			for (VectorRepresentation template : templates){	
				double sim = SimilarityAndRelatednessCalculator.cosineSimilarity(document, template);				
				if (sim > maxSim){
					maxSim = sim;
					bestTemplate = template.toString();
				}
			}
			if (bestTemplate=="") {
				try {
					System.out.println("Empty best template");
					System.in.read();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// assign current document to the best template (add the document to the cluster with label = template.toString()
			List<Integer> docsInCluster = new LinkedList<Integer>();
			if (finalResults.containsKey(bestTemplate)) docsInCluster=finalResults.get(bestTemplate);
			docsInCluster.add(document.getId());
			finalResults.put(bestTemplate, docsInCluster);
		}
		
		return finalResults;
	}

	@Override
	public void setNumberOfDocumentClusters(int K) {
		m_topKclusters = K;
	}	

	public VectorRepresentation getClusterTemplate(List<VectorRepresentation> documents){
		double maxInClassSimilarity = 0.0;
		VectorRepresentation template = null;
		for (VectorRepresentation documentA : documents){
			double docAInClassSimilarity = 0.0;
			for (VectorRepresentation documentB : documents){
				// don't exclude docA==docB - every doc will have +1 to its sum of cosines, so it's OK for comparing
				docAInClassSimilarity += SimilarityAndRelatednessCalculator.cosineSimilarity(documentA, documentB);
			}
			if (docAInClassSimilarity>maxInClassSimilarity){
				template = documentA;
				maxInClassSimilarity = docAInClassSimilarity; 
			}
		}
		return template;
	}
	
	public Map<VectorRepresentation,List<VectorRepresentation>> updateClusteringResults(Map<VectorRepresentation,List<VectorRepresentation>> clusteringResults){
		Map<VectorRepresentation,List<VectorRepresentation>> newResults = new HashMap<VectorRepresentation, List<VectorRepresentation>>(); 
		for (VectorRepresentation template : clusteringResults.keySet()){
			VectorRepresentation newTemplate = getClusterTemplate(clusteringResults.get(template));
			newResults.put(newTemplate, new LinkedList<VectorRepresentation>(clusteringResults.get(template)));
		}
		return newResults;
	}
	
	/** Should be called after updating the keys in the clustering results map - the method only sorts the given keys
	 * @param clusteringResults
	 * @param topK
	 * @return
	 */
	public List<VectorRepresentation> getSortedTemplates(Map<VectorRepresentation,List<VectorRepresentation>> clusteringResults, int topK){
		List<Integer> sizeList = new LinkedList<Integer>();
		for (VectorRepresentation template : clusteringResults.keySet()){
			sizeList.add(clusteringResults.get(template).size()); // size list will only contain non-zero-size clusters
		}
		Comparator<Integer> reverseCmp = Collections.reverseOrder(); 
		Collections.sort(sizeList, reverseCmp);
		List<VectorRepresentation> templates = new LinkedList<VectorRepresentation>(); // create sorted-by-cluster-size and recalculated templates
		int numOfTemplates = 0; 
		for (Integer size : sizeList){
			for (VectorRepresentation template : clusteringResults.keySet()){
				if (clusteringResults.get(template).size()==size){ 
					//if current cluster is of the required size - add the key to templates
					if (!templates.contains(template)) {
						templates.add(template); // make sure not to add the same cluster twice, since there might be clusters of the same size
//						System.out.println("Size: "+String.valueOf(size)+" - added template "+template);
						numOfTemplates++;
						if (numOfTemplates == topK) return templates; // if reached the desired number of templates - done
					}
				}
			}
		}
		return templates;
	}

	public List<VectorRepresentation> getSortedTemplates(Map<VectorRepresentation,List<VectorRepresentation>> clusteringResults){
		// no limitation on the numner of templates => return the same number as before, excluding zero-size clusters, which is clusteringResults.size()
		return getSortedTemplates(clusteringResults, clusteringResults.size());
	}

}

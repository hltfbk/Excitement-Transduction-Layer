package eu.excitementproject.clustering.clustering.impl.kmedoids;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;

/**
 * @author Lili Kotlerman
 * 
 * Implementation of the Y-clustering algorithm by Hui Ye and Steve Young, "A clustering approach to semantic decoding." INTERSPEECH. 2006. *
 * with some changes 
 */
public abstract class AbstractYClusterer<T> {

	protected int m_topKclusters = 10; // if set to a negative number, no cut-off will be applied in the final iteration     
	boolean m_useExpandedCollection;
	WeightType m_weightType;
	double m_similarityThreshold; // similarity value, which is enough to assign a document to a class (required by the algorithm)  
		
	public AbstractYClusterer(boolean useExpandedCollection, WeightType weightType, Double similarityThreshold) {
		m_useExpandedCollection = useExpandedCollection;
		m_weightType = weightType;
		m_similarityThreshold = similarityThreshold;
	}

	
	/** Calculate similarity between two elements. 
	 * @param elementA
	 * @param elementB
	 * @return similarity value in [0,1]
	 */
	protected abstract double getSimilarity(T elementA, T elementB);

	/**
	 * @return the key in the clustering results map, which will hold the "out-of-class" cluster in the final assignment
	 * This key will be used when none of the final templates yield >0 similarity with a document
	 */
	protected abstract T getOutOfClassKey();

	protected Map<T, Map<T,Double>> clusterElements(Set<T> elements, int topKClusters, boolean multi){

		// step 1 - init
		// assign maxT = number of elements / 2 (heuristic decision) - the max number of templates (cluster centroids) to generate in each pass
		int maxT = elements.size(); // elements.size()/2;
		
		
		// step 2 - init cont.
		Set<T> outOfClassElements = new HashSet<T>();
		List<T> templates = new LinkedList<T>(); // order is important for the following iterations

		int i=0;
		for (T element : elements){
			outOfClassElements.add(element);
			i++;
			if (i <= maxT){
				templates.add(element); // use first maxT documents as the initial templates
			}
		}
		System.out.println(String.valueOf(outOfClassElements.size())+" elements are to be assigned");
		
		// no need to clean before each iteration - clustering results iteratively change, new documents being added and new keys being chosen for each cluster 
		Map<T,List<T>> clusteringResults = new HashMap<T, List<T>>();

		boolean stopCondition = false;
		while(!stopCondition){
			//step 3
			Set<T> inClassElements = new HashSet<T>(); 
					
			for (T element : outOfClassElements){
				for (T template : templates){
					double sim = getSimilarity(element, template);
					if (sim > m_similarityThreshold){
						// assign current element to current template 
						List<T> elementsInCluster = new LinkedList<T>();
						if (clusteringResults.containsKey(template)) elementsInCluster=clusteringResults.get(template);
						elementsInCluster.add(element);
						clusteringResults.put(template, elementsInCluster);
						// save this document to the set of inClassElements - those, for which a high-confidence (>threshold) template was found
						inClassElements.add(element);
						// now go to the next document
						break; // leave the for(VectorRepresentation template : templates) cycle and go up to the next document
					}
				}
			}
			//update the outOfClass elements to exclude all the elements, which found their clusters
			System.out.println("Assigned "+String.valueOf(inClassElements.size())+" new elements");
			outOfClassElements.removeAll(inClassElements);
			System.out.println(String.valueOf(outOfClassElements.size())+" elements are still not assigned");
			
			//step 4-5
			// - sort clusters desc by the number of assigned sentences (step4) 
			// - remove clusters which ended up with no documents (step4)
			// - recalculate template for each cluster (step5)
			clusteringResults = updateClusteringResults(clusteringResults);
			templates = getSortedTemplates(clusteringResults);

			
			// step 6
			// if the number of outOfClass documents is NOT lower than in prev iteration = if there were NO new inClassDocuments
			// then stop condition is reached
			if (inClassElements.isEmpty()) stopCondition=true; 					
		}
		
		//step 7 - final iteration
		// - leave only top-K templates in the template list
		// - assign each document to the nearest template
		
		templates = getSortedTemplates(clusteringResults, m_topKclusters);	
		if (multi) return finalAssignmentMulti(elements, templates);
		return finalAssignment(elements, templates);
	}

	public T getClusterTemplate(List<T> elements){
		double maxInClassSimilarity = 0.0;
		T template = null;
		for (T elementA : elements){
			double elementAInClassSimilarity = 0.0;
			for (T elementB : elements){
				// don't exclude docA==docB - every doc will have +1 to its sum of cosines, so it's OK for comparing
				elementAInClassSimilarity += getSimilarity(elementA, elementB);
			}
			if (elementAInClassSimilarity > maxInClassSimilarity){
				template = elementA;
				maxInClassSimilarity = elementAInClassSimilarity; 
			}
		}
		return template;
	}
	

	public Map<T,List<T>> updateClusteringResults(Map<T,List<T>> clusteringResults){
		Map<T,List<T>> newResults = new HashMap<T, List<T>>(); 
		for (T template : clusteringResults.keySet()){
			T newTemplate = getClusterTemplate(clusteringResults.get(template));
			
			LinkedList<T> cluster = new LinkedList<T>(clusteringResults.get(template));
			
			// if the same template was chosen before by another cluster, merge that cluster with the current cluster (without adding duplicates)
			if (newResults.containsKey(newTemplate)){
				for (T element : newResults.get(newTemplate)){
					if (!cluster.contains(element)) cluster.add(element);
				}
			}
				
			newResults.put(newTemplate, cluster);
		}
		return newResults;
	}
	
	/** Should be called after updating the keys in the clustering results map - the method only sorts the given keys
	 * @param clusteringResults
	 * @param topK
	 * @return
	 */
	public List<T> getSortedTemplates(Map<T,List<T>> clusteringResults, int topK){
		List<Integer> sizeList = new LinkedList<Integer>();
		for (T template : clusteringResults.keySet()){
			sizeList.add(clusteringResults.get(template).size()); // size list will only contain non-zero-size clusters
		}
		Comparator<Integer> reverseCmp = Collections.reverseOrder(); 
		Collections.sort(sizeList, reverseCmp);
		List<T> templates = new LinkedList<T>(); // create sorted-by-cluster-size and recalculated templates
		int numOfTemplates = 0; 
		for (Integer size : sizeList){
			for (T template : clusteringResults.keySet()){
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

	public List<T> getSortedTemplates(Map<T,List<T>> clusteringResults){
		// no limitation on the number of templates => return the same number as before, excluding zero-size clusters, which is clusteringResults.size()
		return getSortedTemplates(clusteringResults, clusteringResults.size());
	}
	
	
	public Map<T,Map<T,Double>> finalAssignment(Set<T> elements, List<T> templates){
		Map<T,Map<T,Double>> finalResults = new HashMap<T, Map<T,Double>>();
		for (T element : elements){ // assign to the template with the highest sim value
			double maxSim = 0.0;
			T bestTemplate = getOutOfClassKey(); // this T will be the key if none of the templates yields >0 similarity to the current document

			if (templates.contains(element)){ // if the element is itself a template,then assign it to its own class
				bestTemplate = element;
			}
			else{ // find the best-scoring template for the current element
				for (T template : templates){	
					double sim = getSimilarity(element, template);	
					if (sim > maxSim){
						maxSim = sim;
						bestTemplate = template;
					}
				}				
			}
			
			// assign current element to the best template (add the element to the cluster with label = template
			Map<T,Double> elementsInCluster = new HashMap<T,Double>();
			if (finalResults.containsKey(bestTemplate)) elementsInCluster=finalResults.get(bestTemplate);
			elementsInCluster.put(element,maxSim);
			finalResults.put(bestTemplate, elementsInCluster);
		}	
		return finalResults;
	}
	
	public Map<T,Map<T,Double>> finalAssignmentMulti(Set<T> elements, List<T> templates){		
		Map<T,Map<T,Double>> finalResults = new HashMap<T, Map<T,Double>>();
		for (T element : elements){ // assign to the template with sim value > threshold
			boolean assigned = false;
			for (T template : templates){	
				double sim = getSimilarity(element, template);	
				if (sim > m_similarityThreshold){
					// assign current element to current template (add the element to the cluster with label = template
					assigned=true;
					Map<T,Double> elementsInCluster = new HashMap<T,Double>();
					if (finalResults.containsKey(template)) elementsInCluster=finalResults.get(template);
					elementsInCluster.put(element,sim);
					finalResults.put(template, elementsInCluster);
				}
			}
			if (!assigned) {
				Map<T,Double> elementsInCluster = new HashMap<T,Double>();
				if (finalResults.containsKey(getOutOfClassKey())) elementsInCluster=finalResults.get(getOutOfClassKey());
				elementsInCluster.put(element,0.0);
				finalResults.put(getOutOfClassKey(), elementsInCluster);				
			}
		}	
		return finalResults;
	}	

}

package eu.excitementproject.clustering.clustering.impl.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.clustering.clustering.api.Clusterer;
import eu.excitementproject.clustering.clustering.impl.util.WeightCalculator.WeightType;
import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 *
 */
public class DocumentsToVectorsConverter {
	
	/**
	 * if false, nonClust term cluster (concept) will split to single term concepts for creation of concept vectors
	 * if true, nonClust term cluster (concept) will be removed when creating concept vectors
	 */
	private static final boolean m_removeNonClustTermCluster = false;
	
	/** Represents the given text collection as a set of dense term vectors
	 * Dense vectors are needed to run the co-clustering tool
	 * @param textCollection
	 * @param useExpandedCollection
	 * @return
	 */
	public static Set<VectorRepresentation> convertDocumentsToDenseTermVectors(TextCollection textCollection, boolean useExpandedCollection, WeightType weightType) {
		Set<VectorRepresentation> documents = new HashSet<VectorRepresentation>();			
		for (int docId : textCollection.getDocTextsByDocId().keySet()){

			Map<String,Double> vector = new HashMap<String, Double>();

			if (useExpandedCollection){ //represent expanded document
				// init documentTerms with all the original terms from the document
				Set<String> documentTerms = new HashSet<String>(textCollection.getOriginalTermsByDocId().get(docId).keySet());
				// add to documentTerms all the expansions added to the document, if any 
				if (textCollection.getExpansionTermsByDocId().containsKey(docId)) documentTerms.addAll(textCollection.getExpansionTermsByDocId().get(docId).keySet());
				for (String term : documentTerms){
					vector.put(term,WeightCalculator.weight(textCollection.getTermFrequencyInDocumentAfterExpansion(term, docId), textCollection.getInvertedDocumentFrequencyAfterExpansion(term), weightType));
				}								
			}
			else { // represent original document (not expanded)
				for (String term : textCollection.getOriginalTermsByDocId().get(docId).keySet()){
					vector.put(term,WeightCalculator.weight(textCollection.getTermFrequencyInDocumentBeforeExpansion(term, docId), textCollection.getInvertedDocumentFrequencyBeforeExpansion(term), weightType));
				}				
			}
			documents.add(new VectorRepresentation(docId, vector));
			//System.out.println(textCollection.getDocTextsByDocId().get(docId)+"\t"+vector);
		}
		return documents;
	}
	

	/** Represents the given text collection as a set of dense term vectors and leaves only topK non-zero entries in each vector
	 * Dense vectors are needed to run the co-clustering tool
	 * @param textCollection
	 * @param useExpandedCollection
	 * @return
	 */
	public static Set<VectorRepresentation> convertDocumentsToDenseTermVectors(TextCollection textCollection, boolean useExpandedCollection, WeightType weightType, int topK) {
		Set<VectorRepresentation> fullVectors = convertDocumentsToDenseTermVectors(textCollection, useExpandedCollection, weightType);
		Set<VectorRepresentation> truncatedVectors = new HashSet<VectorRepresentation>();
		for (VectorRepresentation v : fullVectors){			
			truncatedVectors.add(new VectorRepresentation(v.getId(), retainTopKEntries(v.getVector(), topK, true)));
		}		
		return truncatedVectors;		
	}
	
	
	/** Represents the given text collection as a set of sparse concept vectors, where concepts are sets of terms
	 * @param textCollection
	 * @param useExpandedCollection
	 * @param termsByConcept - index of concepts: [concept name] [term, term, ...] 
	 * @return
	 */
	public static Set<VectorRepresentation> convertDocumentsToSparseConceptVectors(TextCollection textCollection, boolean useExpandedCollection, Map<String,List<String>> termsByConcept, WeightType weightType) {
		// first of all, clean termsByConcept - if a concept is non-cluster, either remove the concept or make each non-class term a concept on itself		
		if (m_removeNonClustTermCluster){
			termsByConcept.remove(Clusterer.NON_CLUSTER_LABEL);
		}
		else{ // make each non-class term a concept on itself		
			if (termsByConcept.containsKey(Clusterer.NON_CLUSTER_LABEL)){
				Set<String> nonClusterTerms = new HashSet<String>(termsByConcept.get(Clusterer.NON_CLUSTER_LABEL));
				termsByConcept.remove(Clusterer.NON_CLUSTER_LABEL);
				for (String term : nonClusterTerms){
					List<String> singleton = new LinkedList<String>();
					singleton.add(term);
					termsByConcept.put(term, singleton);
				}
			}			
		}
		
		Set<VectorRepresentation> documents = new HashSet<VectorRepresentation>();
		for (int docId : textCollection.getDocTextsByDocId().keySet()){
			Map<String,Double> vector = new HashMap<String, Double>();
			for (String concept : termsByConcept.keySet()){
				double tf = 0.0; // "term frequency" of the concept = how many times it occurred in the current document
				Set<Integer> conceptOccuredInDocuments = new HashSet<Integer>(); // set of docIds of the documents, in which the concept occurred (needed to calculate concept's idf)
				for (String term : termsByConcept.get(concept)){
					if(textCollection.getOriginalTermsByDocId().get(docId).containsKey(term)) {
						tf += textCollection.getOriginalTermsByDocId().get(docId).get(term);
					}
					if(textCollection.getDocIdsByOriginalTerm().containsKey(term)){
						conceptOccuredInDocuments.addAll(textCollection.getDocIdsByOriginalTerm().get(term).keySet());						
					}

					if (useExpandedCollection){ // if expanded document should be represented
						if(textCollection.getExpansionTermsByDocId().containsKey(docId)) {// if the document was expanded
							// increase "term frequency" with the occurrences of current term as expansion in current document
							if(textCollection.getExpansionTermsByDocId().get(docId).containsKey(term)) tf += textCollection.getExpansionTermsByDocId().get(docId).get(term);							
						}
						// include occurrences of expansion for ids calculation, if the current term occurred in any document as expansion
						if(textCollection.getDocIdsByExpansionTerm().containsKey(term)) conceptOccuredInDocuments.addAll(textCollection.getDocIdsByExpansionTerm().get(term).keySet());						
					}
				}
				if (tf > 0) vector.put(concept+"["+String.valueOf(termsByConcept.get(concept).size())+"]", WeightCalculator.weight(tf, 1.0/conceptOccuredInDocuments.size(), weightType));
			}
			documents.add(new VectorRepresentation(docId, vector));
		}
		return documents;
	}	
	

	/** Represents the given text collection as a set of sparse concept vectors, where concepts are sets of terms, and retain only top-K concepts in each vector
	 * @param textCollection
	 * @param useExpandedCollection
	 * @param termsByConcept - index of concepts: [concept name] [term, term, ...] 
	 * @return
	 */
	public static Set<VectorRepresentation> convertDocumentsToSparseConceptVectors(TextCollection textCollection, boolean useExpandedCollection, Map<String,List<String>> termsByConcept, WeightType weightType, int topK) {
		Set<VectorRepresentation> fullVectors = convertDocumentsToSparseConceptVectors(textCollection, useExpandedCollection, termsByConcept, weightType);
		Set<VectorRepresentation> truncatedVectors = new HashSet<VectorRepresentation>();
		for (VectorRepresentation v : fullVectors){			
			truncatedVectors.add(new VectorRepresentation(v.getId(), retainTopKEntries(v.getVector(), topK, false)));
		}		
		return truncatedVectors;		

	}		
	
	public static Map<String,Double> retainTopKEntries(Map<String,Double> vector, int topK, boolean isDense){

		if (topK==0) return vector;
		if (vector.size()<=topK) return vector;

		
		Map<String,Double> newVector = new HashMap<String, Double>();

		List<Double> entryWeights = new LinkedList<Double>(vector.values());
		Comparator<Double> reverseCmp = Collections.reverseOrder(); 
		Collections.sort(entryWeights, reverseCmp);
		Double threshold = entryWeights.get(topK-1);
		
		if (threshold.equals(0)) return vector; // in a dense vector, topK sorted entry can have 0 score. This means that the original vector had < topK non-zero features, so there's nothing to cut off  
		
		for (String entry : vector.keySet()){
			if (vector.get(entry)>=threshold) newVector.put(entry, vector.get(entry));
			else{
				if (isDense) newVector.put(entry, 0.0); // if a dense vector should be created, add non-topK entries with 0 weight
				// if a sparse vector is required, no need to add deleted entries 
			}
		}		
		return newVector;
	}
}

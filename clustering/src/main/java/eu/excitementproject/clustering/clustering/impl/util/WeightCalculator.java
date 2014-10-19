package eu.excitementproject.clustering.clustering.impl.util;

import java.util.Comparator;
import java.util.Map;

import eu.excitementproject.clustering.data.api.TextCollection;

/**
 * @author Lili Kotlerman
 *
 */
public class WeightCalculator {

	public enum WeightType{
		BINARY,
		TF,
		DF, //DOCUMENT_FREQUENCY
		TF_IDF,
		TF_DF,
	}
	
	public static double weight(Double tf, Double idf, WeightType type){
		if (type.equals(WeightType.TF_IDF)) return tf*idf; //tf-idf score of a term/concept
		else if (type.equals(WeightType.TF)) return tf; // tf score - how many times term/concept occurred in the given document
		else if (type.equals(WeightType.DF)) return 1/idf; // df score - in how many documents term/concept occurred
		else if (type.equals(WeightType.TF_DF)) return tf/idf; // = tf * (1/idf) = tf * df
		else if (tf > 0) return 1; // binary score - if term/concept occurred in the given document, return 1
		return 0; // otherwise - return 0
	}
	
	public static double getTermWeightInDocument(TextCollection textCollection, boolean useExpandedCollection, Integer docId, String term, WeightType weightType){
		if (useExpandedCollection){ //use expanded document
			return WeightCalculator.weight(textCollection.getTermFrequencyInDocumentAfterExpansion(term, docId), textCollection.getInvertedDocumentFrequencyAfterExpansion(term), weightType);
		}
		else { // use original document (not expanded)
			 return WeightCalculator.weight(textCollection.getTermFrequencyInDocumentBeforeExpansion(term, docId), textCollection.getInvertedDocumentFrequencyBeforeExpansion(term), weightType);
		}		
	}	
	
	
	/**
	 * Comparator to sort cluster members by score
	 */
	public static class ClusterMembersComparator<T> implements Comparator<T> {
		Map<T,Double> scoresMap;
		public ClusterMembersComparator(Map<T,Double> scoresMap) {
			this.scoresMap=scoresMap;
		}

		@Override
		public int compare(T o1, T o2) {
			if ((scoresMap.containsKey(o1))&&(scoresMap.containsKey(o2))) return scoresMap.get(01).compareTo(scoresMap.get(o2));
			return 0;
		}
	}	
	
}

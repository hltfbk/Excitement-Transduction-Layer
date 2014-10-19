package eu.excitementproject.clustering.clustering.impl.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.eop.common.datastructures.Pair;
import eu.excitementproject.eop.common.datastructures.PairMap;

/**
 * @author Lili Kotlerman
 *
 */
public class SimilarityAndRelatednessCalculator {
	
	private static double magnitude(Map<String, Double> vector){
		double magnitude = 0.0;
		for (String key : vector.keySet()){
			magnitude+=Math.pow(vector.get(key),2);
		}
		return Math.sqrt(magnitude);
	}
	
	public static double cosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB){
		double similarity = 0.0;
		Set<String> commonKeys = new HashSet<String>(vectorA.keySet());
		commonKeys.retainAll(vectorB.keySet());
		if (commonKeys.isEmpty()) return 0.0; 
		
		for (String key : commonKeys){
			similarity+=(vectorA.get(key)*vectorB.get(key));
		}
				
		
		double cos = similarity/(magnitude(vectorA)*magnitude(vectorB));
		if (cos>1) return 1.0; // detected results like 1.00000000002, which make LingPipe crazy
		return cos;
	}
	
	public static double cosineSimilarity(VectorRepresentation vectorA, VectorRepresentation vectorB){
		return cosineSimilarity(vectorA.getVector(), vectorB.getVector());
	}

	public static double semiNormalizedCosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB){
		double similarity = 0.0;
		Set<String> commonKeys = new HashSet<String>(vectorA.keySet());
		commonKeys.retainAll(vectorB.keySet());
		
		for (String key : commonKeys){
			similarity+=(vectorA.get(key)*vectorB.get(key));
		}
		
		double cos = similarity/magnitude(vectorA);
		if (cos>1) return 1.0; // detected results like 1.00000000002, which make LingPipe crazy
		return cos;
	}
	
	/**
	 * @param termA
	 * @param termB
	 * @param semanticRelatednessMap
	 * @return a number in [0,1]
	 */
	public static double semanticRelatedness(String termA, String termB, PairMap<String, Double> semanticRelatednessMap){
		if (termA.equals(termB)) return 1;
		Pair<String> termPair = new Pair<String>(termA, termB);
		if (semanticRelatednessMap.containsPair(termPair)) {
			double rel = semanticRelatednessMap.getValueOf(termPair);
			if (rel>1) return 1; //make sure 1 is not exceeded
			if (rel<0) return 0; //make sure returned value is not negative
			return rel;
		}
		return 0;
	}
	
	public static VectorRepresentation getCentroid(List<VectorRepresentation> documents){
		Map<String,Double> centroidVector = new HashMap<String,Double>(); 
		// add all terms to centroid vector, sum up weights of each term 
		for (VectorRepresentation doc : documents){
			for(String term : doc.getVector().keySet()){
				Double weight = doc.getVector().get(term);
				if(centroidVector.containsKey(term)) {
					Double newWeight = weight + centroidVector.get(term);
					centroidVector.put(term, newWeight);
				}
				else centroidVector.put(term, weight);
			}
		}
		// normalize to have avg weight per centroid term
		for (String term : centroidVector.keySet()){
			centroidVector.put(term, centroidVector.get(term)/documents.size());
		}
		return new VectorRepresentation(0, centroidVector);
	}
		
	public static double getCosineSimilarityToCentroid(VectorRepresentation document, List<VectorRepresentation> documentsInCluster){
		return cosineSimilarity(getCentroid(documentsInCluster), document);
	}
	
	public static double getCosineSimilarityToCentroid(VectorRepresentation document, VectorRepresentation centroid){
		return cosineSimilarity(centroid, document);
	}
	
	
	public static double getSemanticRelatednessToCluster(String term, Set<String> termsInCluster, PairMap<String, Double> semanticRelatednessMap){
		double res = 0.0;
		for (String clusterTerm : termsInCluster){
			res += semanticRelatedness(clusterTerm, term, semanticRelatednessMap);
		}		
		return res;
	}

}

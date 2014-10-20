package eu.excitementproject.clustering.clustering.impl.completeLink.util;

import com.aliasi.util.Distance;

import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.eop.common.datastructures.PairMap;

/**
 * @author Lili Kotlerman
 *
 */
public class SemanticDistance implements Distance<String>{

	private PairMap<String, Double> m_semanticRelatednessMap;
	
	
	public SemanticDistance(PairMap<String, Double> semanticRelatednessMap) {
		super();
		m_semanticRelatednessMap = semanticRelatednessMap;
	}

	@Override
	public double distance(String arg0, String arg1) {
		double dist = 1-SimilarityAndRelatednessCalculator.semanticRelatedness(arg0, arg1,m_semanticRelatednessMap);
		if (dist < 0 ) return 0.0; //		// if semanticRelatedness is more than 1, need to return 0 (and if it's 1 - sometimes the result is like 1.00000000002, which makes LingPipe crazy)
		return dist;
	}
	
}

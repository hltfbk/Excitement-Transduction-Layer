package eu.excitementproject.clustering.clustering.impl.completeLink.util;

import com.aliasi.util.Distance;

import eu.excitementproject.clustering.clustering.impl.util.SimilarityAndRelatednessCalculator;
import eu.excitementproject.clustering.clustering.impl.util.VectorRepresentation;

/**
 * @author Lili Kotlerman
 *
 */
public class CosineDistance implements Distance<VectorRepresentation>{

	@Override
	public double distance(VectorRepresentation arg0, VectorRepresentation arg1) {
		return 1-SimilarityAndRelatednessCalculator.cosineSimilarity(arg0.getVector(), arg1.getVector());
	}

}

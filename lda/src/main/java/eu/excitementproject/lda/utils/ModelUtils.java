package eu.excitementproject.lda.utils;


import java.util.LinkedList;
import java.util.List;

import cc.mallet.topics.ParallelTopicModel;
import eu.excitementproject.lda.rep.SparseTopicRep;
import eu.excitementproject.lda.rep.TopicIdProbPair;


public class ModelUtils {
	
	public static SparseTopicRep convertTopicDist2Sparse(ParallelTopicModel model, double[] dist, int docSize) {
		
		List<TopicIdProbPair> topicIdProbPairs = new LinkedList<TopicIdProbPair>();
		double[] probs = new double[dist.length];
		
		for(short i = 0; i < probs.length;++i) 
			probs[i] = model.alpha[i] / (model.alphaSum + docSize);
		
		for(short i = 0; i < dist.length;++i) {
			if(dist[i] > probs[i] + 0.001)
				topicIdProbPairs.add(new TopicIdProbPair(i, (float)dist[i]));
		}
		return new SparseTopicRep(topicIdProbPairs, docSize);	
	}
	
	public static double[] converteSparseDist2NonSparse(ParallelTopicModel model, SparseTopicRep sparseDist) {
		
		double[] result = new double[model.alpha.length];
		
		for(short i = 0; i < result.length;++i)
			result[i] = model.alpha[i] / (model.alphaSum + sparseDist.getDocSize());
		
		for(TopicIdProbPair pair: sparseDist.getTopicIdProbPairs()) 
			result[pair.getTopicId()] = pair.getProb();
	
		return result;
	}
}

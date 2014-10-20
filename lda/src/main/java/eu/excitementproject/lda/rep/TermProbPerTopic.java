package eu.excitementproject.lda.rep;

import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.lda.core.MalletLda;
import eu.excitementproject.lda.utils.FileLoaderUtils;
import eu.excitementproject.lda.utils.MalletDocUtil;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TShortFloatHashMap;

import java.io.BufferedReader;
import java.io.FileReader;

import cc.mallet.topics.ParallelTopicModel;

/**
 * @author Jonathan Berant & Lili Kotlerman
 *
 */
public class TermProbPerTopic {
	
	private TObjectIntHashMap<String> m_termDesc2Id;
	private TIntObjectHashMap<TShortFloatHashMap> m_term2Topic2ProbMap;
	private double[] m_perTopicZeroCountMass;
	
	public TermProbPerTopic(ConfigurationParams iParams) throws Exception {
		
		m_termDesc2Id = FileLoaderUtils.loadStringToIntFile(iParams.getFile("feature-encoding-file"));
		m_term2Topic2ProbMap = new TIntObjectHashMap<TShortFloatHashMap>();
		
		BufferedReader termProbGivenTopicReader = new BufferedReader(new FileReader(iParams.getFile("term-prob-per-topic-file")));
		termProbGivenTopicReader.readLine(); //skip title
		String line;
		while((line=termProbGivenTopicReader.readLine())!=null){

			String[] tokens = line.split("\t");
//			String fixedTerm = tokens[0].replace("<", " ");
			String fixedTerm = MalletDocUtil.fromMalletToken(tokens[0]);
			short topic = Short.parseShort(tokens[1]);
			float prob = Float.parseFloat(tokens[2]);
			int featureId = m_termDesc2Id.get(fixedTerm);
		
			TShortFloatHashMap topicId2ProbMap = m_term2Topic2ProbMap.get(featureId);
			if(topicId2ProbMap==null) {
				topicId2ProbMap = new TShortFloatHashMap();
				m_term2Topic2ProbMap.put(m_termDesc2Id.get(fixedTerm), topicId2ProbMap);
			}
			topicId2ProbMap.put(topic, prob);
			
		}
		termProbGivenTopicReader.close();
		
		ParallelTopicModel model = ParallelTopicModel.read(iParams.getFile("model-file"));
		double[] perTopicCountSum = MalletLda.getPerTopicCountSum(model);
		double beta = model.beta;
		int dictSize = model.getAlphabet().size();

		m_perTopicZeroCountMass = new double[perTopicCountSum.length];
		for(int i = 0; i < m_perTopicZeroCountMass.length; ++i)
			m_perTopicZeroCountMass[i] = beta / (perTopicCountSum[i]+dictSize*beta);
	}
	
	public double getProbForTermAndTopic(String term, short topicId) {
		
		int termId;
		if (!m_termDesc2Id.containsKey(term)) {
			termId = -1;
		} else {
			termId = m_termDesc2Id.get(term);
		}
		return computeTermProbGivenTopic(termId, topicId);
	}
	
	public double computeTermProbGivenTopic(int featureId, short topicId) {

		double prob = m_perTopicZeroCountMass[topicId];
		TShortFloatHashMap topic2Prob = m_term2Topic2ProbMap.get(featureId);

		if(topic2Prob!=null) {
			prob = topic2Prob.get(topicId);
			if(prob==0.0)
				prob = m_perTopicZeroCountMass[topicId];
		}		
		return prob;
	}
	
	
}

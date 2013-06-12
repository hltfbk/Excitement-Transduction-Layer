package eu.excitementproject.tl.composition.categoryannotator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitement.type.tl.CategoryDecision;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

public class CategoryAnnotatorAllCats extends AbstractCategoryAnnotator {

	@Override
	public void addCategoryAnnotation(JCas cas, Set<NodeMatch> matches)
			throws CategoryAnnotatorException, LAPException {
		
		for (NodeMatch match : matches) { //for each matching mention
			EntailmentUnitMention mentionInCAS = match.getMention(); 
			int startPosition = 1; //TODO: replace by mentionInCAS.getStartPositionInCAS();
			int endPosition = 10; //TODO: replace by mentionInCAS.getEndPositionInCAS();
			CASUtils.Region region = new CASUtils.Region(startPosition, endPosition);
			List<PerNodeScore> scores = match.getScores();
			HashMap<String, Double> categorySum = new HashMap<String, Double>();
			int sumNodeScores = scores.size();
			for (PerNodeScore score : scores) { //for each matching EG node for this mention
				EntailmentUnit eu = score.getNode();
				double nodeScore = score.getScore(); //score telling us how well this node matches the mentionInCAS
				//compute category distribution on node: how often does each category occur on this node?
				HashMap<String, Integer> categoryDistributionOnNode = new HashMap<String, Integer>();
				int sumMentions = 0;
				for (EntailmentUnitMention mentionOnNode : eu.getMentions()) { //for each mention associated to the node
					sumMentions++;
					String categoryMention = "1"; //TODO: replace by mentionOnNode.getCategory(); 
					int count = 0;
					if (categoryDistributionOnNode.containsKey(categoryMention)) {
						count = categoryDistributionOnNode.get(categoryMention);
					}
					count++;
					categoryDistributionOnNode.put(categoryMention, count);
				}
				//compute and add up scores for each category on this node
				for (String category : categoryDistributionOnNode.keySet()) {	
					double sum = 0.0;
					if (categorySum.containsKey(category)) {
						sum = categorySum.get(category);
					}
					/* Calculation of score: node score * category occurrence / number of mentions
					 * node score: how well does the node match the mention? 
					 * category occurrence: how many mentions on the node are associated to this category 
					 * number of mentions: total number of mentios associated to this node
					 * 
					 * All scores are summed up and later divided by the total number of matching nodes.
					 */
					sum += nodeScore * categoryDistributionOnNode.get(category) / (double)sumMentions;
					categorySum.put(category, sum);
				}
			}
			Map<String, Double> decisions = new HashMap<String, Double>();
			for (String category : categorySum.keySet()) {
				double confidence = categorySum.get(category) / (double) sumNodeScores;
				decisions.put(category, confidence);
			}
			//add annotation to CAS (per matching mention)
			CASUtils.annotateCategories(cas, region, decisions);
		}	
	}
}

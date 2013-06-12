package eu.excitementproject.tl.composition.categoryannotator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

/**
 * The CategoryAnnotator adds category annotation to an input CAS, based on an input set of 
 * NodeMatch-es. This requires the combination of category information in the NodeMatch-es
 * to category confidence scores. 
 * 
 * Each NodeMatch in the input set of NodeMatch-es holds exactly one EntailmentUnitMention M
 * (found in the input CAS), which is associated to a list of PerNodeScore-s P. 
 * PerNodeScore-s refer to tuples of an EntailmentUnit E (a node in a raw entailment graph) and
 * a confidence score C denoting the confidence of M matching E. 
 * 
 * In this implementation of the CategoryAnnotator module, category confidence scores are 
 * computed in the following way:
 * 
 * For each P, we first collect the category distribution on the node by retrieving the category
 * of each of the m mentions associated to P and storing the sum of occurrences of this category. 
 * Let's refer to the category as c, the sum as sum(c).  
 * 
 * In a second step, we now compute a score s(c) for each category occurring on the node using
 * the following formula: 
 * 
 * s(c) = C * sum(c) / m
 * 
 * To compute the final category confidence, all scores for a particular category are summed up 
 * and, in the end, divided by the total number of NodeMatch-es.
 * 
 * @author Kathrin Eichler
 *
 */
public class CategoryAnnotatorAllCats extends AbstractCategoryAnnotator {

	@Override
	public void addCategoryAnnotation(JCas cas, Set<NodeMatch> matches)
			throws CategoryAnnotatorException, LAPException {
		
		for (NodeMatch match : matches) { //for each matching mention
			EntailmentUnitMention mentionInCAS = match.getMention(); 
			int startPosition = mentionInCAS.getBegin();
			int endPosition = mentionInCAS.getEnd();
			CASUtils.Region region = new CASUtils.Region(startPosition, endPosition);
			List<PerNodeScore> scores = match.getScores();
			HashMap<String, Double> categorySum = new HashMap<String, Double>();
			//map storing a sum of scores for each category, needs to be divided by the total number 
			//of nodes (sumNodeScores):
			int sumNodeScores = scores.size();
			for (PerNodeScore score : scores) { //for each matching EG node for this mention
				EntailmentUnit eu = score.getNode();
				double nodeScore = score.getScore(); //score telling us how well this node matches the mentionInCAS
				//compute category distribution on node: how often does each category occur on this node?
				HashMap<String, Integer> categoryDistributionOnNode = new HashMap<String, Integer>();
				//this map collects the different categories and how often their occur on this node
				int sumMentions = 0;
				for (EntailmentUnitMention mentionOnNode : eu.getMentions()) { //for each mention associated to the node
					sumMentions++;
					String categoryMention = mentionOnNode.getCategoryId();
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
						sum = categorySum.get(category); //read sum in case we've stored a sum from a previous node
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
			CASUtils.annotateCategories(cas, region, mentionInCAS.getText(), decisions);
		}	
	}
}

package eu.excitementproject.tl.composition.categoryannotator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

/**
 * The CategoryAnnotator adds category annotation to an input CAS, based on an input set of 
 * NodeMatch-es. This requires the combination of category information in the NodeMatch-es
 * to final category confidence scores. 
 * 
 * Each NodeMatch in the input set of NodeMatch-es holds exactly one EntailmentUnitMention M
 * (found in the input CAS), which is associated to a list of PerNodeScore-s P. 
 * 
 * PerNodeScore-s refer to tuples of an EquivalenceClass E (a node in a collapsed entailment 
 * graph) and a confidence score C denoting the confidence of M matching E. 
 * 
 * In this implementation of the CategoryAnnotator module, category confidence scores for 
 * a particular M are computed by going through all the per node scores found for M, 
 * reading the category confidence scores associated to each node, and multiplying each 
 * category confidence with the confidence of the match. All combined confidences are summed
 * up per category and divided by the total number of category mentions to compute the final score
 * for each category.
 * 
 * The pseudocode is given in the following:
 * 
 * Init sumCAT[]; //the sum of all scores for a particular category 
 * Init sumMentions; //total # of mentions in the node scores
 * For each P[m] = <E,C> associated to M: //for each per node score / matching graph node (= equivalence class)
 *   For each CAT[n] in E[m]: //for each category in the graph node
 *  	score = CAT[n].score * C; //multiply category confidence with match confidence
 *      sumCAT[n] += score; //sum up the scores computed for this category
 *      sumCategoryMentions++; //count total # of category mentions
 * finalScore[n] = sumCAT[n] / sumCategoryMentions; //compute final score by dividing sum by # of category mentions
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
			HashMap<String,Double> sumCategoryConfidencesPerCategory = new HashMap<String,Double>();
			double sumCategoryMentions = 0.0;
			for (PerNodeScore score : scores) { //for each matching EG node for this mention
				EquivalenceClass E = score.getNode();
				double C = score.getScore(); //score telling us how well this node matches the mentionInCAS
				//category confidences on node
				Map<String, Double> categoryConfidencesOnNode = E.getCategoryConfidences();
				try {
					for (String category : categoryConfidencesOnNode.keySet()) {	
						double confidence = categoryConfidencesOnNode.get(category);
						double sumCategory = 0.0;
						if (sumCategoryConfidencesPerCategory.containsKey(category)) {
							sumCategory = sumCategoryConfidencesPerCategory.get(category); //read sum in case we've stored a sum from a previous node
						}
						sumCategory += confidence * C;
						sumCategoryConfidencesPerCategory.put(category, sumCategory);
						sumCategoryMentions ++;
					}
				} catch (NullPointerException e) {
					System.err.println("Missing category confidences. Run ConfidenceCalculator on graph!");
				}
			}
			Map<String, Double> decisions = new HashMap<String, Double>();
			for (String category : sumCategoryConfidencesPerCategory.keySet()) {
				double finalConfidence 
					= (double) sumCategoryConfidencesPerCategory.get(category) 
					/ (double) sumCategoryMentions;
				decisions.put(category, finalConfidence);
			}
			//add annotation to CAS (per matching mention)
			CASUtils.annotateCategories(cas, region, mentionInCAS.getText(), decisions);
		}	
	}
}

package eu.excitementproject.tl.composition.categoryannotator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.CategoryAnnotator;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.collapsedgraph.GraphStatistics;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

/**
 * The {@link CategoryAnnotator} adds category annotation to an input CAS based on an input set of 
 * {@link NodeMatch}es. This requires the combination of category information in the {@link NodeMatch}es
 * to final category confidence scores. 
 * 
 * Each {@link NodeMatch} in the input set holds exactly one {@link EntailmentUnitMention} M
 * (found in the input CAS), which is associated to a list of {@link PerNodeScore}s P. 
 * 
 * {@link PerNodeScore}s refer to tuples of an {@link EquivalenceClass} E 
 * (a node in a collapsed entailment graph) and a confidence score C denoting the confidence 
 * of M matching E. 
 * 
 * In this implementation of the {@link CategoryAnnotator} module, category confidence scores 
 * for a particular M are computed by going through all the {@link PerNodeScore}s found for M, 
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
	static Logger logger = Logger.getLogger(CategoryAnnotatorAllCats.class); 
	
	GraphStatistics graphStatistics = null;
	
	public CategoryAnnotatorAllCats(){
	}
	
	public CategoryAnnotatorAllCats(GraphStatistics graphStatistics) {
		this.graphStatistics = graphStatistics;
	}
	
	@Override
	public void addCategoryAnnotation(JCas cas, Set<NodeMatch> matches)
			throws CategoryAnnotatorException, LAPException {
		
		for (NodeMatch match : matches) { //for each matching mention (Bayes: for each w in D)
			EntailmentUnitMention mentionInCAS = match.getMention(); 
			int startPosition = mentionInCAS.getBegin();
			int endPosition = mentionInCAS.getEnd();
			CASUtils.Region region = new CASUtils.Region(startPosition, endPosition);
			List<PerNodeScore> scores = match.getScores();
			HashMap<String,Double> categoryConfidencesPerCategory = new HashMap<String,Double>();
			double sumCategoryMentions = 0.0;
			Map<String, Double> decisions = new HashMap<String, Double>();
			
			for (PerNodeScore score : scores) { //for each matching EG node for this mention
				EquivalenceClass E = score.getNode();
				double C = score.getScore(); //score telling us how well this node matches the mentionInCAS
				logger.info("E: " + E.getLabel());
				logger.info("C: " + C);
				//category confidences on node
				Map<String, Double> categoryConfidencesOnNode = E.getCategoryConfidences();
				logger.info("Number of category confidences on node: " + categoryConfidencesOnNode.size());
				try {
					for (String category : categoryConfidencesOnNode.keySet()) {	
						double confidence = categoryConfidencesOnNode.get(category);
						double sumCategory = 0.0;
						if (categoryConfidencesPerCategory.containsKey(category)) {
							sumCategory = categoryConfidencesPerCategory.get(category); //read sum in case we've stored a sum from a previous node
						}
						sumCategory += confidence * C;
						categoryConfidencesPerCategory.put(category, sumCategory);
						sumCategoryMentions ++;
					}
				} catch (NullPointerException e) {
					System.err.println("Missing category confidences. Run ConfidenceCalculator on graph!");
				}
			}
			for (String category : categoryConfidencesPerCategory.keySet()) {
				double finalConfidence 
					= (double) categoryConfidencesPerCategory.get(category) 
					/ (double) sumCategoryMentions;
				logger.info(category + " --> " + finalConfidence);
				decisions.put(category, finalConfidence);
			}
			//add annotation to CAS (per matching mention)
			CASUtils.annotateCategories(cas, region, mentionInCAS.getText(), decisions);
			logger.info(CASUtils.getCategoryAnnotationsInCAS(cas));
		}
	}
}

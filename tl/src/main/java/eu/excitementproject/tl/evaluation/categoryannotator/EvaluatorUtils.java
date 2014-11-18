package eu.excitementproject.tl.evaluation.categoryannotator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import eu.excitement.type.tl.CategoryDecision;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

public class EvaluatorUtils {
	static Logger logger = Logger.getLogger(EvaluatorUtils.class); 

	/**
	 * Find out what's the most frequent category stored in the graph.
	 * 
	 * @param graph
	 * @return most frequent category
	 */
	public static String computeMostFrequentCategory(EntailmentGraphCollapsed graph) {
		int numberOfTextualInputs = graph.getNumberOfTextualInputs();
		logger.info("num of textual inputs: " + numberOfTextualInputs);
		Map<String, Float> categoryOccurrences = new HashMap<String,Float>();
		Set<String> processedInteractions = new HashSet<String>();
		for (EquivalenceClass ec : graph.vertexSet()) {
			for (EntailmentUnit eu : ec.getEntailmentUnits()) {
				for (EntailmentUnitMention eum : eu.getMentions()) {
					String interactionId = eum.getInteractionId();
					if (!processedInteractions.contains(interactionId)) {
						String[] cats = eum.getCategoryId().split(",");	
						for (String cat : cats) {
							float occ = 1;
							if (categoryOccurrences.containsKey(cat)) {
								occ += categoryOccurrences.get(cat);
							}
							categoryOccurrences.put(cat, occ);
						}
						processedInteractions.add(interactionId);
					}
				}
			}
		}
		ValueComparator bvc =  new ValueComparator(categoryOccurrences);
		Map<String,Float> sortedMap = new TreeMap<String,Float>(bvc);
		sortedMap.putAll(categoryOccurrences);
		logger.debug("category sums:  " + sortedMap);
		String mostFrequentCat = "N/A";
		if (sortedMap.size() > 0) {
			mostFrequentCat = sortedMap.keySet().iterator().next().toString();
			logger.info("Most probable category: " + mostFrequentCat);
			logger.info("Occurs " + categoryOccurrences.get(mostFrequentCat) + " times");
			logger.info("Number of processed interactions " + processedInteractions.size());
			logger.info("Baseline: " + (double) categoryOccurrences.get(mostFrequentCat)/ (double) processedInteractions.size());
		}
		return mostFrequentCat;

	}

	/**
	 * Compare automatic to manual annotation on interaction level
	 * 
	 * @param countPositive
	 * @param doc
	 * @param decisions
	 * @param mostProbableCat
	 * @return
	 */
	public static int compareDecisionsForInteraction(int countPositive,
			Interaction doc, Set<CategoryDecision> decisions, String mostProbableCat, 
			EntailmentGraphCollapsed graph, Set<NodeMatch> matches, int topN, String method, 
			boolean bestNodeOnly, char documentFrequencyQuery, char termFrequencyQuery, 
			boolean lengthBoost) {
		String[] bestCats = new String[topN];
		logger.info("Number of decisions for interaction "+doc.getInteractionId()+": " + decisions.size());
		bestCats = computeBestCats(decisions, mostProbableCat, doc.getCategories(), graph, 
				matches, method, bestNodeOnly, topN, documentFrequencyQuery, termFrequencyQuery, 
				lengthBoost);
		logger.info("Correct category: " + doc.getCategoryString());
		Set<String> docCats = new HashSet<String>(Arrays.asList(doc.getCategories()));
		logger.info("docCats: " + docCats);
		for (int i=0; i<topN; i++) { //adapted to consider top N categories
			String cat = bestCats[i];
			logger.info("Top " + i + " category: " + cat);
			if (docCats.contains(cat)) { //adapted to deal with multiple categories
				countPositive++;
			} 
		}
		return countPositive;
	}
	
	/**
	 * Computes the best category given the set of category decisions, based on the defined method.
	 * Currently, two different methods have been implemented: TFIDF-based category ranking and
	 * a Naive Bayes classifier. 
	 * 
	 * @param doc
	 * @param decisions
	 * @param mostProbableCat
	 * @return
	 */
	public static String[] computeBestCats(Set<CategoryDecision> decisions, String mostProbableCat, 
			String[] correctCats, EntailmentGraphCollapsed graph, Set<NodeMatch> matches, 
			String method, boolean bestNodeOnly, int topN, char documentFrequencyQuery, 
			char termFrequencyQuery, boolean lengthBoost) {
		logger.debug("Computing best category");
		logger.debug("Number of decisions: " + decisions.size());
		HashMap<String,BigDecimal> categoryScoresBigDecimal = new HashMap<String,BigDecimal>();
		if (method.equals("tfidf_sum")) {  //corresponds to the "ndn.ntn" TFIDF-variant
			for (CategoryDecision decision: decisions) {
				String category = decision.getCategoryId();
				BigDecimal sum = new BigDecimal("0"); //the sum of scores for a particular category 
				if (categoryScoresBigDecimal.containsKey(category)) {
					sum = categoryScoresBigDecimal.get(category);
				}
				//add up all scores for each category on the CAS
				sum = sum.add(new BigDecimal(decision.getConfidence()));
				categoryScoresBigDecimal.put(category, sum);
			}
			logger.info("category scores big decimal in tfidf_sum: " + categoryScoresBigDecimal);
			
		} else if (method.startsWith("bayes")) { //Naive Bayes; TODO: extend to more than just the "best matching" node (as with TFIDF implementation)
			HashMap<String,BigDecimal> preliminaryCategoryScores = new HashMap<String,BigDecimal>();
			for (NodeMatch nodeMatch : matches) { //for each matching mention in the document
				EquivalenceClass bestNode = getBestMatchingNode(nodeMatch);
				//category confidences on node
				Map<String, Double> categoryConfidencesOnNode = bestNode.getCategoryConfidences();
				logger.info("Category confidences on node: " + categoryConfidencesOnNode);
				try { 
					if (method.equals("bayes")) { //multiply all values P(w_j|c_i) for j = 1..V (vocabulary size)
						for (String category : categoryConfidencesOnNode.keySet()) {	
							BigDecimal product = new BigDecimal("1");
							if (preliminaryCategoryScores.containsKey(category)) {
								product = preliminaryCategoryScores.get(category); //read product in case we've stored a product from a previous node
							}
							product = product.multiply(new BigDecimal(categoryConfidencesOnNode.get(category)));
							preliminaryCategoryScores.put(category, product);
						}
					} else if (method.equals("bayes_log")) {
						for (String category : categoryConfidencesOnNode.keySet()) {	
							BigDecimal log_sum = new BigDecimal("0");
							if (preliminaryCategoryScores.containsKey(category)) {
								log_sum = preliminaryCategoryScores.get(category); //read log sum in case we've stored it from a previous node
							}
							log_sum = log_sum.add(new BigDecimal(categoryConfidencesOnNode.get(category)));
							preliminaryCategoryScores.put(category, log_sum);
						}							
					} else {
						logger.error("Implementation missing for method " + method);
						System.exit(1);							
					}
				} catch (NullPointerException e) {
					logger.error("Missing category confidences. Run ConfidenceCalculator on graph!");
					System.exit(1);
				}					
			}
			logger.info("preliminaryCategoryScores: " + preliminaryCategoryScores);
			//calculate P(c_i|W) = P(c_i) x PRODUCT_j=1..V (P(w_j|c_i)
			//PRODUCT is already stored in preliminaryCategoryScores
			for (String category : preliminaryCategoryScores.keySet()) {	
				//estimate the priors from the sample: Math.log((double)count/knowledgeBase.n)
				BigDecimal finalConfidence = null;
				if (method.equals("bayes")) { //multiply prior with product
					BigDecimal prior = 
							new BigDecimal(graph.getGraphStatistics().getNumberOfMentionsPerCategory().get(category) 
	//						/ graph.getGraphStatistics().getTotalNumberOfMentions()  //can be ignored, as it's the same value for all categories
									);
					finalConfidence =  prior.multiply(preliminaryCategoryScores.get(category));
				} else if (method.equals("bayes_log")) { //sum up prior and log sum
					BigDecimal prior = new BigDecimal(Math.log(
						(double) graph.getGraphStatistics().getNumberOfMentionsPerCategory().get(category) 
						/ (double) graph.getGraphStatistics().getTotalNumberOfMentions())); 
					finalConfidence = prior.add(preliminaryCategoryScores.get(category));
				}
				categoryScoresBigDecimal.put(category, finalConfidence);
			}
			logger.info("category scores big decimal: " + categoryScoresBigDecimal);

		} else if (method.startsWith("tfidf")) { //TFIDF-based classification (query = new email; documents = categories)
			int numberOfAddedUpValues = 0;
			//collect mention tf in query
			HashMap<String,Integer> tfQueryMap = new HashMap<String,Integer>();
			int count = 0;
			for (NodeMatch match : matches) { //for each matching mention
				String mentionText = match.getMention().getTextWithoutDoubleSpaces();
				count = 1;
				if (tfQueryMap.containsKey(mentionText)) {
					count += tfQueryMap.get(mentionText); 
				}
				tfQueryMap.put(mentionText, count);
			}
			logger.debug("Collected tf for queries " + tfQueryMap.size());			
			//writer.println("Collected tf for queries:");
			//for (String query : tfQueryMap.keySet()) {
			//	writer.println(query + " : " + tfQueryMap.get(query));
			//}
			
			//Collect query cosine values for each category
			HashMap<String,Double[]> queryCosineValuesPerCategory = new HashMap<String,Double[]>();
			double N = graph.getNumberOfCategories(); //overall number of categories
			double sumQ2 = 0.0;
			Set<String> processedMentions = new HashSet<String>();
			int countDecisions = 0;
			
			BigDecimal overallSum = new BigDecimal("0");
			
			for (NodeMatch match : matches) { //for each matching mention	
				String mentionText = match.getMention().getTextWithoutDoubleSpaces();
				if (!processedMentions.contains(mentionText)) { //make sure to process each mention text only once!
					processedMentions.add(mentionText);
					boolean exit = false;	
					
					for (PerNodeScore perNodeScore : match.getScores()) { //deal with all nodes, not just the best one
						if (exit) {
							break;
						}
						EquivalenceClass node; 
						if (bestNodeOnly) {
							node = getBestMatchingNode(match);	
							exit = true;
						} else {
							node = perNodeScore.getNode();
						}
						//retrieve tfidf for "document" (category)
						Map<String,Double> tfidfScoresForCategories = node.getCategoryConfidences();				
						//compute tfidf for query TODO: LOOKS WRONG! CHECK AGAIN 
						double df = node.getCategoryConfidences().size(); //number of categories associated to the mention
						//if category assigned to the mention is not part of the node yet, add 1:
						//if (!bestNode.getCategoryConfidences().keySet().contains(match.getMention().getCategoryId())) n++;								
						double idfForQuery = 1; 
						if (documentFrequencyQuery =='d') idfForQuery = 1/df;
						if (documentFrequencyQuery == 'l') idfForQuery = Math.log(N/df);
						//writer.println("number of category confidences on best node: " + df);
						//writer.println("idfForquery: " + idfForQuery);
						//compute sums for computing cosine similarity of the query to each of the categories
						Double[] queryCosineValues;				
						double tfForQuery = tfQueryMap.get(match.getMention().getTextWithoutDoubleSpaces()); 
						
						//length boost
						double length = match.getMention().getTextWithoutDoubleSpaces().split("\\s+").length;
						
						countDecisions += (tfForQuery*df);
						if (termFrequencyQuery == 'l') { //logarithm
							tfForQuery = 1 + Math.log(tfForQuery); // = "wf-idf"
						} else if (termFrequencyQuery == 'b') { //boolean
							if (tfForQuery > 0) tfForQuery = 1;
							//TODO: Include non-matching terms of the query? 
						}
						double nodeScore = 1.0;
						if (!bestNodeOnly) nodeScore = perNodeScore.getScore();		
	
						//OBS! Slight change of original TF-IDF formular: We integrate the score associated to the node (representing the confidence of the match)
						double scoreForQuery = nodeScore*tfForQuery*idfForQuery;
						if (lengthBoost) scoreForQuery *= length;
						
						//VECTOR SPACE MODEL		
						if (method.endsWith("_vsm")) { //TODO: check implementation again
							double Q = scoreForQuery;
							sumQ2 += Math.pow(Q, 2); //this part does not depend on the category!
							for (String category : node.getCategoryConfidences().keySet()) { //for each category associated to this node
								queryCosineValues = new Double[2];
								double D = tfidfScoresForCategories.get(category);
								double sumQD = Q*D;
								double sumD2 = Math.pow(D, 2);						
								if (queryCosineValuesPerCategory.containsKey(category)) {
									sumQD += queryCosineValuesPerCategory.get(category)[0];
									sumD2 += queryCosineValuesPerCategory.get(category)[1];
								}
								queryCosineValues[0] = sumQD;
								queryCosineValues[1] = sumD2;
								queryCosineValuesPerCategory.put(category, queryCosineValues);
							}
						} else { //SIMPLE TF_IDF
							for (String category : node.getCategoryConfidences().keySet()) { //for each category associated to this node (do once per mention text)
								numberOfAddedUpValues++;
								double D = tfidfScoresForCategories.get(category); //category score in best-matching node
								BigDecimal sumScore = new BigDecimal(scoreForQuery*D); //multiply with scoreForQuery, e.g. simple tf
								overallSum = overallSum.add(sumScore);
								if (categoryScoresBigDecimal.containsKey(category)) {
									sumScore = categoryScoresBigDecimal.get(category).add(sumScore);
								}
								categoryScoresBigDecimal.put(category, sumScore);
							}					
						}
					}
				}
			}
			
			logger.debug("overallSum: " + overallSum);
			logger.debug("Number of node matches: " + matches.size());
			logger.debug("Number of added up Values: " + numberOfAddedUpValues);
			logger.debug("Number of processed mentions: " + processedMentions.size());
			logger.debug("Number of category decisions: " + countDecisions);
			logger.debug("Category scores big decimal in tfidf: " + categoryScoresBigDecimal);
			
			if (method.endsWith("_vsm")) {
				for (String category : queryCosineValuesPerCategory.keySet()) { //for each matching EG node for this mention
					//annotate category confidences in CAS based on cosine similarity (per document, not per mention!)
					//cos = A x B / |A|x|B| = SUM_i=1..n[Ai x Bi] / (ROOT(SUM_i=1..n(Ai2)) x ROOT(SUM_i=1..n(Bi2)))
					Double[] queryCosineValuesForCategory = queryCosineValuesPerCategory.get(category);
					Double sumQD = queryCosineValuesForCategory[0];
					Double sumD2 = queryCosineValuesForCategory[1];
					//writer.println("cosine values for category " + category + ": " + queryCosineValuesForCategory[0] + ", " + queryCosineValuesForCategory[1] + ", " + sumQ2);
					logger.info("cosine values for category " + category + ": " + queryCosineValuesForCategory[0] + ", " + queryCosineValuesForCategory[1] + ", " + sumQ2);
					BigDecimal cosQD = new BigDecimal(sumQD).divide(new BigDecimal(Math.sqrt(sumD2) * Math.sqrt(sumQ2)), MathContext.DECIMAL128);
					categoryScoresBigDecimal.put(category, cosQD);					
					//writer.println(category + " : " + cosQD);
				}
			}
		} else {
			logger.error("Method for query weighting not defined:" + method );
			System.exit(1);
		}
		return getTopNCategories(mostProbableCat, correctCats,
				categoryScoresBigDecimal, topN);

	}


	public static String[] getTopNCategories(String mostProbableCat,
			String[] correctCats,
			HashMap<String, BigDecimal> categoryScoresBigDecimal, int topN) {
		// get the N categories with the highest value
		ValueComparatorBigDecimal bvc =  new ValueComparatorBigDecimal(categoryScoresBigDecimal);

		Map<String,BigDecimal> sortedMapBigDecimal = new TreeMap<String,BigDecimal>(bvc);
		sortedMapBigDecimal.putAll(categoryScoresBigDecimal);		
		
		logger.info("category scores:  " + sortedMapBigDecimal);
		String[] bestCats = new String[topN];
		
		if (sortedMapBigDecimal.size() == 0) { //no category found
			bestCats[0] = mostProbableCat;
			for (int i=1; i<topN; i++) bestCats[i] = "N/A";
		} else {				
			Iterator<String> sortedMapIterator = sortedMapBigDecimal.keySet().iterator();
			for (int i=0; i<topN; i++) {
				if (sortedMapBigDecimal.size() > i) {
					bestCats[i] = sortedMapIterator.next().toString();
					logger.info("Best category: " + bestCats[i]);
					Set<String> correctCategories = new HashSet<String>(Arrays.asList(correctCats));
					if (correctCategories.size() > 1) logger.warn("Contains more than one category!");
					for (String correctCat : correctCategories) {
						if (categoryScoresBigDecimal.keySet().contains(correctCat)) {
							logger.info("Computed confidence for correct category ("+correctCat+"): " + categoryScoresBigDecimal.get(correctCat));
						}
						else {
							logger.info("Computed confidence for correct category ("+correctCat+"): N/A");
						}
					}
				} else bestCats[i] = "N/A";
			} 
		}
		return bestCats;
	}

	/**
	 * If the returned NodeMatch contains more than one matching node, 
	 * return the one with the highest match score. 
	 * 
	 * @param match
	 * @return
	 */
	public static EquivalenceClass getBestMatchingNode(NodeMatch match) {
		double maxScore = 0;
		EquivalenceClass bestNode = null;
		for (PerNodeScore perNodeScore : match.getScores()) {
			double score = perNodeScore.getScore();
			if (maxScore < score) {
				maxScore = score;
				bestNode = perNodeScore.getNode();
			}
		}
		// writer.println("bestNode for match "+match.getMention().getTextWithoutDoubleSpaces()
		//		+": " + bestNode.getLabel());		
		return bestNode;
	}
		

}

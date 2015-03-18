package eu.excitementproject.tl.evaluation.categoryannotator;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalResourceException;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalRule;
import eu.excitementproject.eop.core.component.lexicalknowledge.derivbase.DerivBaseResource;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetRelation;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetWrapper;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

/**
 * Some util methods needed for evaluating the usage of entailment graphs for the category annotation task. 
 * 
 * @author Kathrin Eichler and Aleksandra Gabryszak
 *
 */
public class EvaluatorUtils {
	static Logger logger = Logger.getLogger(EvaluatorUtils.class); 

	/**
	 * Find out what's the most frequent category stored in the graph.
	 * (method used for assigning the most frequent category in case incoming interaction cannot be categorized).
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
	 * Compare automatic to manual annotation at interaction level and 
	 * return the number of correct decisions.
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
//			System.out.print("Top " + i + " category:\t" + cat + "\t");
			if (docCats.contains(cat)) { //adapted to deal with multiple categories
				countPositive++;
			} 
		}
		return countPositive;
	}
	
	/**
	 * Compute the best category given the set of category decisions, based on the defined method.
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
		if (method.startsWith("bayes")) { //Naive Bayes; TODO: extend to more than just the "best matching" node (as with TFIDF implementation)
			computeBestCatsUsingNaiveBayes(graph, matches, method,
					categoryScoresBigDecimal);
		} else if (method.startsWith("tfidf")) { //TFIDF-based classification (query = new email; documents = categories)
			computeBestCatsUsingTFIDF(graph, matches, bestNodeOnly,
					documentFrequencyQuery, termFrequencyQuery, lengthBoost,
					categoryScoresBigDecimal);
		} else {
			logger.error("Method for query weighting not defined:" + method );
			System.exit(1);
		}
		return getTopNCategories(mostProbableCat, correctCats,
				categoryScoresBigDecimal, topN);

	}

	private static void computeBestCatsUsingTFIDF(
			EntailmentGraphCollapsed graph, Set<NodeMatch> matches,
			boolean bestNodeOnly, char documentFrequencyQuery,
			char termFrequencyQuery, boolean lengthBoost,
			HashMap<String, BigDecimal> categoryScoresBigDecimal) {

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
		
		
		//Collect query cosine values for each category
		double N = graph.getNumberOfCategories(); //overall number of categories
		Set<String> processedMentions = new HashSet<String>();
		int countDecisions = 0;

		BigDecimal overallSum = new BigDecimal("0");
		
		for (NodeMatch match : matches) { //for each matching mention	
			String mentionText = match.getMention().getTextWithoutDoubleSpaces();
			if (!processedMentions.contains(mentionText)) { //make sure to process each mention text only once!
				processedMentions.add(mentionText);
				boolean exit = false;	

				HashMap<String,BigDecimal> sumCategoryScoresBigDecimalForMention = new HashMap<String,BigDecimal>();
				for (PerNodeScore perNodeScore : match.getScores()) { //deal with all nodes, not just the best one
					logger.info("Number of matching nodes for mention: " + match.getScores().size());
					if (match.getScores().size() > 1) {
						for (PerNodeScore score : match.getScores()) 
							logger.debug(score.getNode().getLabel());
						//System.exit(1); //TODO: REMOVE (DEBUGGING ONLY)
					}
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
					
					//compute idf for "query" (incoming request)
					double df = node.getCategoryConfidences().size(); //number of categories associated to the mention
					//if category assigned to the mention is not part of the node yet, add 1:
					double idfForQuery = 1; 
					if (documentFrequencyQuery =='d') idfForQuery = 1/df;
					if (documentFrequencyQuery == 't') idfForQuery = Math.log(N/df);
					
					//compute tf for "query" (incoming request)
					double tfForQuery = tfQueryMap.get(match.getMention().getTextWithoutDoubleSpaces()); 										
					countDecisions += (tfForQuery*df);
					if (termFrequencyQuery == 'l') { //logarithm
						tfForQuery = 1 + Math.log(tfForQuery); // = "wf-idf"
					} else if (termFrequencyQuery == 'b') { //boolean
						if (tfForQuery > 0) tfForQuery = 1;
					}
					
					//compute node score
					double nodeScore = 1.0;
					if (!bestNodeOnly) nodeScore = perNodeScore.getScore();		
						
					//OBS! Slight change of original TF-IDF formular: We integrate the score associated to the node (representing the confidence of the match)
					double scoreForQuery = nodeScore*tfForQuery*idfForQuery;					

					//length boost: boost longer text units based on the number of contained tokens
					double length = match.getMention().getTextWithoutDoubleSpaces().split("\\s+").length;
					if (lengthBoost) scoreForQuery *= length;

					//retrieve tfidf for "document" (category)
					Map<String,Double> tfidfScoresForCategories = node.getCategoryConfidences();				
					
					for (String category : node.getCategoryConfidences().keySet()) { //for each category associated to this node (do once per mention text)
						double D = tfidfScoresForCategories.get(category); //category score in matching node
						BigDecimal scoreForMention = new BigDecimal(scoreForQuery*D); //multiply with scoreForQuery, e.g. simple tf
						if (sumCategoryScoresBigDecimalForMention.containsKey(category)) {
							scoreForMention = sumCategoryScoresBigDecimalForMention.get(category).add(scoreForMention);
						}
						sumCategoryScoresBigDecimalForMention.put(category, scoreForMention);
					}	
					logger.debug("for mention: " + sumCategoryScoresBigDecimalForMention);
				}

				//normalize values based on number of returned nodes (add only a single value per mention to avoid giving too much weight to mentions with entailed nodes)
				for (String category : sumCategoryScoresBigDecimalForMention.keySet()) {
					numberOfAddedUpValues++;
					BigDecimal normalizedScoreForMention = sumCategoryScoresBigDecimalForMention.get(category).divide(new BigDecimal(match.getScores().size()), MathContext.DECIMAL128); 
					overallSum = overallSum.add(normalizedScoreForMention);
					if (categoryScoresBigDecimal.containsKey(category)) {
						normalizedScoreForMention = categoryScoresBigDecimal.get(category).add(normalizedScoreForMention);
					}
					categoryScoresBigDecimal.put(category, normalizedScoreForMention);
				}
				logger.debug("normalized: " + categoryScoresBigDecimal);
			}
		}
		
		logger.debug("overallSum: " + overallSum);
		logger.debug("Number of node matches: " + matches.size());
		logger.debug("Number of added up Values: " + numberOfAddedUpValues);
		logger.debug("Number of processed mentions: " + processedMentions.size());
		logger.debug("Number of category decisions: " + countDecisions);
		logger.debug("Category scores big decimal in tfidf: " + categoryScoresBigDecimal);
	}

	private static void computeBestCatsUsingNaiveBayes(
			EntailmentGraphCollapsed graph, Set<NodeMatch> matches,
			String method, HashMap<String, BigDecimal> categoryScoresBigDecimal) {
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
	}


	/**
	 * Get the to n best-matching categories 
	 * 
	 * @param mostProbableCat - most common category in the graph
	 * @param correctCats - list of correct categories (for logger only!)
	 * @param categoryScoresBigDecimal - map of categories associated to a big decimal confidence score
	 * @param topN - number of best-matching categories to be returned
	 * 
	 * @return n best-matching categories ordered by their confidence score
	 */
	public static String[] getTopNCategories(String mostProbableCat,
			String[] correctCats,
			HashMap<String, BigDecimal> categoryScoresBigDecimal, int topN) {
		// get the N categories with the highest value
		ValueComparatorBigDecimal bvc =  new ValueComparatorBigDecimal(categoryScoresBigDecimal);

		Map<String,BigDecimal> sortedMapBigDecimal = new TreeMap<String,BigDecimal>(bvc);
		sortedMapBigDecimal.putAll(categoryScoresBigDecimal);		
//		System.out.print("best cat score: " + sortedMapBigDecimal + "\t");
		
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
	
	/**
	 * Read two raw graphs from file and then join Set<EntailmentUnit> and Set<EntailmentRelation> 
	 * of these raw graphs into an empty raw graph  
	 * @param rawGraphFile1
	 * @param rawGraphFile2
	 * @param copyEdgesFromRawGraph1
	 * @param copyEdgesFromRawGraph2
	 * @return
	 */
	public static EntailmentGraphRaw joinRawGraphs(File rawGraphFile1, File rawGraphFile2, 
			boolean copyEdgesFromRawGraph1, boolean copyEdgesFromRawGraph2){
		EntailmentGraphRaw resultRawGraph = new EntailmentGraphRaw();
		try {
			EntailmentGraphRaw egr1 = new EntailmentGraphRaw(rawGraphFile1);
			EntailmentGraphRaw egr2 = new EntailmentGraphRaw(rawGraphFile2);
			resultRawGraph = joinRawGraphs(egr1, egr2, copyEdgesFromRawGraph1, copyEdgesFromRawGraph2);
		} catch (EntailmentGraphRawException e) {
			e.printStackTrace();
		}
		return resultRawGraph;
	}
	
	/**
	 * Copy Set<EntailmentUnit> and Set<EntailmentRelation> of two raw graphs into an empty raw graph  
	 * @param rawGraph1
	 * @param rawGraph2
	 * @param copyEdgesFromRawGraph1
	 * @param copyEdgesFromRawGraph2
	 * @return
	 */
	public static EntailmentGraphRaw joinRawGraphs(EntailmentGraphRaw rawGraph1, EntailmentGraphRaw rawGraph2,
			boolean copyEdgesFromRawGraph1, boolean copyEdgesFromRawGraph2){
		EntailmentGraphRaw resultRawGraph = new EntailmentGraphRaw();
		resultRawGraph.copyRawGraphNodesAndAllEdges(rawGraph1);
		resultRawGraph.copyRawGraphNodesAndAllEdges(rawGraph2);
		return resultRawGraph;
	}
	
		
	/**
	 * Build Set<FragmentGraph>
	 * 
	 * @param interactionList
	 * @param relevantTextProvided
	 * @param fragmentAnnotator
	 * @param modifierAnnotator
	 * @param fragGenerator
	 * @return
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 * @throws ModifierAnnotatorException
	 */
	public static Set<FragmentGraph> buildFragmentGraphs(List<Interaction> interactionList, boolean relevantTextProvided, 
			FragmentAnnotator fragmentAnnotator, ModifierAnnotator modifierAnnotator, FragmentGraphGenerator fragGenerator) 
					throws LAPException, FragmentAnnotatorException, FragmentGraphGeneratorException, ModifierAnnotatorException{
		
		Set<FragmentGraph> fragmentGraphs = new HashSet<FragmentGraph>();
		for(Interaction interaction : interactionList){
			fragmentGraphs.addAll(buildFragmentGraphs(interaction, relevantTextProvided, fragmentAnnotator, 
					modifierAnnotator, fragGenerator));
		}
		return fragmentGraphs;
	}
	
	
	/**
	 * Build Set<FragmentGraph>
	 * 
	 * @param interaction
	 * @param relevantTextProvided
	 * @param fragmentAnnotator
	 * @param modifierAnnotator
	 * @param fragGenerator
	 * @return
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws FragmentGraphGeneratorException
	 * @throws ModifierAnnotatorException
	 */
	public static Set<FragmentGraph> buildFragmentGraphs(Interaction interaction, boolean relevantTextProvided, 
			FragmentAnnotator fragmentAnnotator, ModifierAnnotator modifierAnnotator, FragmentGraphGenerator fragGenerator) 
					throws LAPException, FragmentAnnotatorException, FragmentGraphGeneratorException, ModifierAnnotatorException{
		
		List<JCas> docCases = interaction.createAndFillInputCASes(relevantTextProvided);
		Set<FragmentGraph> fragmentGraphs = new HashSet<FragmentGraph>();
		for(JCas cas : docCases) {
			fragmentAnnotator.annotateFragments(cas);
			if(modifierAnnotator != null) {
				if(cas.getAnnotationIndex(DeterminedFragment.type).size() > 0){
					modifierAnnotator.annotateModifiers(cas);
				}
			}
			fragmentGraphs.addAll(fragGenerator.generateFragmentGraphs(cas));
		}
		return fragmentGraphs;
	}
	
	
	
	/***************************************************************************************************
	 *  METHODS TO BUILD LEMMA TOKEN GRAPH AND DEPENDENCY GRAPH WITHOUT EDA AND GRAPH MERGER          **
	 * *************************************************************************************************
	 */
	
	/**
	 * Merge Set<FragmentGraph> into a single token EntailmentGraphRaw
	 * 
	 * @param singleTokenGraph
	 * @param fgs
	 * @throws LexicalResourceException 
	 */
	public static void mergeIntoLemmaTokenGraph(
			EntailmentGraphRaw singleTokenRawGraph, Set<FragmentGraph> fgs) throws LexicalResourceException{
		
		List<FragmentGraph> fgList = new LinkedList<FragmentGraph>(fgs);
		Collections.sort(fgList, new FragmentGraph.CompleteStatementComparator());
		
		for(FragmentGraph fg : fgList) {
			mergeIntoLemmaTokenGraph(singleTokenRawGraph, fg);
		}
	}
	
	
	/**
	 * Merge one FragmentGraph into a single token EntailmentGraphRaw with only lemma related edges
	 *  
	 * @param singleTokenGraph
	 * @param fg
	 * @throws LexicalResourceException 
	 */
	public static void mergeIntoLemmaTokenGraph(
			EntailmentGraphRaw singleTokenRawGraph, FragmentGraph fg) throws LexicalResourceException {
		
		for(EntailmentUnitMention eum : fg.vertexSet()){
			singleTokenRawGraph.addEntailmentUnitMention(eum, fg.getCompleteStatement().getText());
			EntailmentUnit newStatement = singleTokenRawGraph.getVertexWithText(eum.getTextWithoutDoubleSpaces());
			
			String lemmatized = newStatement.getLemmatizedText();
			Set<EntailmentUnit> graphStatementSet = EvaluatorUtils.getLemmatizedVertex(singleTokenRawGraph, lemmatized, true);
			for(EntailmentUnit graphStatement : graphStatementSet)
			{
				if(!newStatement.getTextWithoutDoubleSpaces().equals(graphStatement.getTextWithoutDoubleSpaces()))
				{
					addBidirectionalEdges(singleTokenRawGraph, newStatement, null, null, null, null, false);
				}
			}
		}
	}
	
	/**
	 * Merge Set<FragmentGraph> into a single token EntailmentGraphRaw
	 * 
	 * @param singleTokenRawGraph
	 * @param fg
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param onlyBidirectionalEdges
	 * @throws LexicalResourceException
	 */
	public static void mergeIntoTokenGraph(
			EntailmentGraphRaw singleTokenRawGraph, Set<FragmentGraph> fgs, DerivBaseResource derivBaseResource, GermaNetWrapper germaNetWrapper, 
			List<GermaNetRelation> germaNetRelations, GermanWordSplitter splitter, boolean onlyBidirectionalEdges) throws LexicalResourceException {
		
		List<FragmentGraph> fgList = new LinkedList<FragmentGraph>(fgs);
		Collections.sort(fgList, new FragmentGraph.CompleteStatementComparator());
		
		for(FragmentGraph fg : fgList) {
			mergeIntoTokenGraph(singleTokenRawGraph, fg, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, onlyBidirectionalEdges);
		}
	}
	
	/**
	 * Merge FragmentGraph into a single token EntailmentGraphRaw
	 * 
	 * @param singleTokenRawGraph
	 * @param fg
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param onlyBidirectionalEdges
	 * @throws LexicalResourceException
	 */
	public static void mergeIntoTokenGraph(
			EntailmentGraphRaw singleTokenRawGraph, FragmentGraph fg, DerivBaseResource derivBaseResource, GermaNetWrapper germaNetWrapper, 
			List<GermaNetRelation> germaNetRelations, GermanWordSplitter splitter, boolean onlyBidirectionalEdges) throws LexicalResourceException {
		
		boolean mapNegation = false;
		for(EntailmentUnitMention eum : fg.vertexSet()) {
			singleTokenRawGraph.addEntailmentUnitMention(eum, fg.getCompleteStatement().getTextWithoutDoubleSpaces());
			EntailmentUnit newStatement = singleTokenRawGraph.getVertexWithText(eum.getTextWithoutDoubleSpaces());
			//direction new statement <--> graph statement
			addBidirectionalEdges(singleTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			if(!onlyBidirectionalEdges) {
				//direction new statement --> graph statement 
				addOneDirectionalEntailedEdges(singleTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
				//direction graph statement --> new statement
				addOneDirectionalEntailingEdges(singleTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			}
		}
	}
	
	
	/**
	 * Merge Set<FragmentGraph> into a two token EntailmentGraphRaw
	 * 
	 * @param twoTokenRawGraph
	 * @param fgs
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param mapNegation
	 * @param onlyBidirectionalEdges
	 * @throws LexicalResourceException
	 */
	public static void mergeIntoDependencyGraph(EntailmentGraphRaw twoTokenRawGraph, Set<FragmentGraph> fgs, 
			DerivBaseResource derivBaseResource, GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations, 
			GermanWordSplitter splitter, boolean mapNegation, boolean onlyBidirectionalEdges)  
					throws LexicalResourceException{
		
		List<FragmentGraph> fgList = new LinkedList<FragmentGraph>(fgs);
		Collections.sort(fgList, new FragmentGraph.CompleteStatementComparator());
		for(FragmentGraph fg : fgList) {
			mergeIntoDependencyGraph(twoTokenRawGraph, fg, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, onlyBidirectionalEdges, mapNegation);
		}
	}
	
	
	/**
	 * Merge one FragmentGraph into a two token EntailmentGraphRaw
	 * 
	 * @param twoTokenRawGraph
	 * @param fg
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param mapNegation
	 * @param onlyBidirectionalEdges
	 * @throws LexicalResourceException
	 */
	public static void mergeIntoDependencyGraph(EntailmentGraphRaw twoTokenRawGraph, FragmentGraph fg, 
			DerivBaseResource derivBaseResource, GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations, 
			GermanWordSplitter splitter, boolean mapNegation, boolean onlyBidirectionalEdges) 
					throws LexicalResourceException {
		
		for(EntailmentUnitMention eum : fg.vertexSet()) {
			twoTokenRawGraph.addEntailmentUnitMention(eum, fg.getCompleteStatement().getTextWithoutDoubleSpaces());
			EntailmentUnit newStatement = twoTokenRawGraph.getVertexWithText(eum.getTextWithoutDoubleSpaces());
			//direction new statement <--> graph statement
			addBidirectionalEdges(twoTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			if(!onlyBidirectionalEdges) {
				//direction new statement --> graph statement 
				addOneDirectionalEntailedEdges(twoTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
				//direction graph statement --> new statement
				addOneDirectionalEntailingEdges(twoTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			}
		}
	}
	
	/**
	 * Add bidirectional entailment edges going from and to the input EntailmentUnit
	 * 
	 * @param egr
	 * @param inputEntailmentUnit
	 * @param dbr
	 * @param gnw
	 * @param germanetRelations
	 * @param splitter
	 * @param mapNegation
	 * @throws LexicalResourceException
	 */
	private static void addBidirectionalEdges(EntailmentGraphRaw egr, EntailmentUnit inputEntailmentUnit, DerivBaseResource dbr, 
			GermaNetWrapper gnw, List<GermaNetRelation> germanetRelations, GermanWordSplitter splitter, boolean mapNegation)
					throws LexicalResourceException {
		LinkedList<GermaNetRelation> germanetRelationsModified = new LinkedList<GermaNetRelation> ();
		if(germanetRelations != null){
			germanetRelationsModified = new LinkedList<GermaNetRelation>(germanetRelations);
			if(germanetRelationsModified.size() > 0){
				germanetRelationsModified.remove(GermaNetRelation.has_hyponym);
				germanetRelationsModified.remove(GermaNetRelation.has_hypernym);
				germanetRelationsModified.remove(GermaNetRelation.causes);
				germanetRelationsModified.remove(GermaNetRelation.has_antonym);
				germanetRelationsModified.remove(GermaNetRelation.entails);
			}
		}
		
		addTEEdges(egr, inputEntailmentUnit, dbr, gnw, germanetRelationsModified, null, mapNegation, "both");
	}

	/**
	 * Add entailment edges going from the input EntailmentUnit
	 * 
	 * @param egr
	 * @param inputEntailmentUnit
	 * @param dbr
	 * @param gnw
	 * @param germanetRelations
	 * @param splitter
	 * @param mapNegation
	 * @throws LexicalResourceException
	 */
	private static void addOneDirectionalEntailedEdges(EntailmentGraphRaw egr, EntailmentUnit inputEntailmentUnit, DerivBaseResource dbr, 
			GermaNetWrapper gnw, List<GermaNetRelation> germanetRelations, GermanWordSplitter splitter, boolean mapNegation)
					throws LexicalResourceException {
		LinkedList<GermaNetRelation> germanetRelationsModified = new LinkedList<GermaNetRelation>();
		if(germanetRelations != null){
			germanetRelationsModified = new LinkedList<GermaNetRelation>(germanetRelations);
			if(germanetRelationsModified.size() > 0){
				germanetRelationsModified.remove(GermaNetRelation.has_hyponym);
				germanetRelationsModified.remove(GermaNetRelation.has_antonym);
			}
		}
		addTEEdges(egr, inputEntailmentUnit, dbr, gnw, germanetRelationsModified, splitter, mapNegation, "inputToGraph");
	}
	
	/**
	 * Add entailment edges going to the input EntailmentUnit
	 * 
	 * @param egr
	 * @param inputEntailmentUnit
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germanetRelations
	 * @param splitter
	 * @param mapNegation
	 * @throws LexicalResourceException
	 */
	private static void addOneDirectionalEntailingEdges(EntailmentGraphRaw egr, EntailmentUnit inputEntailmentUnit, DerivBaseResource derivBaseResource, 
			GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germanetRelations, GermanWordSplitter splitter, boolean mapNegation)
					throws LexicalResourceException {
		List<GermaNetRelation> germanetRelationsModified = new LinkedList<GermaNetRelation>();
		germanetRelationsModified.add(GermaNetRelation.has_hyponym);
		germanetRelationsModified.add(GermaNetRelation.has_synonym);
		addTEEdges(egr, inputEntailmentUnit, derivBaseResource, germaNetWrapper, germanetRelationsModified, null, mapNegation, "graphToInput");
	}
	
	/**
	 * Add edges going from or to the input EntailmentUnit depending on the given direction
	 * 
	 * @param egr
	 * @param inputEntailmentUnit
	 * @param dbr
	 * @param gnw
	 * @param germanetRelations
	 * @param splitter
	 * @param mapNegation
	 * @param direction
	 * @throws LexicalResourceException
	 */
	private static void addTEEdges(EntailmentGraphRaw egr, EntailmentUnit inputEntailmentUnit, 
			DerivBaseResource dbr, GermaNetWrapper gnw, List<GermaNetRelation> germanetRelations, GermanWordSplitter splitter, boolean mapNegation, String direction) 
							throws LexicalResourceException {
		
		Set<String> permutatedTextSet = getRelatedText(inputEntailmentUnit.getTextWithoutDoubleSpaces(), inputEntailmentUnit.getLemmatizedText(), 
				dbr, gnw, germanetRelations, splitter, mapNegation);
		
		Set<EntailmentUnit> graphEUSet = EvaluatorUtils.getLemmatizedVertex(egr, permutatedTextSet, true);
		for(EntailmentUnit graphEU : graphEUSet){
			if(!inputEntailmentUnit.getTextWithoutDoubleSpaces().equals(graphEU.getTextWithoutDoubleSpaces()))
			{ 
				if(direction.equalsIgnoreCase("both")){
					if(!egr.containsEdge(inputEntailmentUnit, graphEU)){
						egr.addEdgeByInduction(inputEntailmentUnit, graphEU, DecisionLabel.Entailment, 0.91);
					}
					if(!egr.containsEdge(graphEU, inputEntailmentUnit)){
						egr.addEdgeByInduction(graphEU, inputEntailmentUnit, DecisionLabel.Entailment, 0.91);
					}
				}
				
				else if(direction.equalsIgnoreCase("inputToGraph")){
					if(!egr.containsEdge(inputEntailmentUnit, graphEU)){
						egr.addEdgeByInduction(inputEntailmentUnit, graphEU, DecisionLabel.Entailment, 0.91);
					}
				}
				
				else if(direction.equalsIgnoreCase("graphToInput")){
					if(!egr.containsEdge(graphEU, inputEntailmentUnit)){
						egr.addEdgeByInduction(graphEU, inputEntailmentUnit, DecisionLabel.Entailment, 0.91);
					}
				}
			}
		}
	}
	
	
	/**
	 * Get related text for a single or a two token text given the lexical resource
	 * DerivBaseResource, GermaNet, GermanWordSplitter or negation maper.
	 * 
	 * @param text
	 * @param lemmatizedText
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param mapNegation
	 * @return
	 * @throws LexicalResourceException
	 */
	private static Set<String> getRelatedText(String text, String lemmatizedText, DerivBaseResource derivBaseResource, 
			GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations, GermanWordSplitter splitter, boolean mapNegation) throws LexicalResourceException{
		
		Set<String> permutations = new HashSet<String>();
		List<String> textTokens = Arrays.asList(text.split("\\s+")); //add original text tokens  
		List<String> textLemmas = Arrays.asList(lemmatizedText.split("\\s+"));
		
		//case single token fragments
		if(textTokens.size() == 1 && textLemmas.size() <= 1) {
			String tokenText = textTokens.get(0);
			if(textLemmas.isEmpty()){
				textLemmas.add(tokenText); //to deal with missing decomposition lemma
			}
			String [] lemmas = getLemmas(textLemmas.get(0));
			for(String lemma : lemmas){
				permutations.addAll(EvaluatorUtils.getRelatedLemmas(lemma, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation));
			}
			permutations.add(tokenText);
			permutations.add(tokenText.toLowerCase());
		}
		
		//case two token fragments
		else if(textTokens.size() == 2 && textLemmas.size() == 2) {
			//TODO: deal with missing decomposition lemma
			Set<String> extendedToken_1 = new HashSet<String>();
			Set<String> extendedToken_2 = new HashSet<String>();
			
			for (int i=0; i < textLemmas.size(); i++){
				//extend first and second token of dependency relation by related lemmas
				String [] lemmas = getLemmas(textLemmas.get(i));
				for(String lemma : lemmas){
					if(i==0){
						extendedToken_1.addAll(EvaluatorUtils.getRelatedLemmas(lemma, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation));
						extendedToken_1.add(textTokens.get(0).toLowerCase());
					}
					else if(i==1){
						extendedToken_2.addAll(EvaluatorUtils.getRelatedLemmas(lemma, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation));
						extendedToken_2.add(textTokens.get(1).toLowerCase());
					}
				}
			}
			
			permutations = getPermutations(extendedToken_1, extendedToken_2, true);
		}
		
		return permutations;
	}
	
	
	/**
	 *  Get set of related lemma given the lexical resource
	 * DerivBaseResource, GermaNet or GermanWordSplitter or negation maper.
	 * 
	 * @param lemma
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param mapNegation
	 * @return
	 * @throws LexicalResourceException
	 */
	private static Set<String> getRelatedLemmas(String lemma, DerivBaseResource derivBaseResource, 
			GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations, 
			GermanWordSplitter splitter, boolean mapNegation) throws LexicalResourceException{
		
		Set<String> lemmas = new HashSet<String>();
		lemmas.add(lemma);
		lemmas.addAll(getRelatedLemmas(lemma, splitter));
		
		Set<String> relatedLemmas = new HashSet<String>();	
		for(String tempLemma : lemmas){
			relatedLemmas.add(tempLemma);
			relatedLemmas.addAll(getRelatedLemmas(tempLemma, germaNetWrapper, germaNetRelations));
			relatedLemmas.addAll(getRelatedLemmas(tempLemma, derivBaseResource));
		}

		if(mapNegation && isNegationWordDE(lemma)){
			relatedLemmas.addAll(Arrays.asList(getNegationLemmasDE()));
		}
		
		return relatedLemmas;
	}
	
	/**
	 * Get set of related lemma given the lexical resource DerivBaseResource
	 * 
	 * @param lemma
	 * @param derivBaseResource
	 * @return
	 * @throws LexicalResourceException
	 */
	private static Set<String> getRelatedLemmas(String lemma, DerivBaseResource derivBaseResource) throws LexicalResourceException{
		Set<String> relatedLemmas = new HashSet<String>();
		if(derivBaseResource != null){
			for(LexicalRule<?> rule : derivBaseResource.getRulesForLeft(lemma, null)){
				relatedLemmas.add(rule.getRLemma());
			}
		}
		return relatedLemmas;
	}
	
	/**
	 * Get lemmas of German negation words
	 * @return --  {"keine", "nicht", "nichts", "ohne"}
	 */
	private static String [] getNegationLemmasDE(){
		String [] negationLemmas = {"keine", "keinerlei", "nicht", "nichts", "ohne"};
		return negationLemmas;
	}
	
	/**
	 * Check if the input word is a German negation word
	 * @param word
	 * @return
	 */
	private static boolean isNegationWordDE(String word){
		List<String> negationWords = Arrays.asList(new String [] {"kein", "keins", "keine", "keinem", "keinem", "keiner", "keins", "keinerlei", "nicht", "nichts", "ohne"});
		return negationWords.contains(word);
	}
	
	/**
	 * Get set of related lemma given the resource GermanWordSplitter
	 * 
	 * @param lemma
	 * @param splitter
	 * @return
	 */
	private static Set<String> getRelatedLemmas(String lemma, GermanWordSplitter splitter){
		Set<String> splits = new HashSet<String>();
		splits.add(lemma);
		if(splitter != null){
			for(String hyphenSplit : lemma.split("[-]")){ //to deal with compounds "XML-Daten", where the strict method of GermanWordSplitter fails
				splits.add(hyphenSplit);
				for(String split : splitter.splitWord(hyphenSplit)){
					Character ch = split.charAt(0);
					if(Character.isLetter(ch)){
						Character firstLetterUpperCase = Character.toUpperCase(ch);
						String splitFirstLetterUpperCase = split.replaceFirst(ch.toString(), firstLetterUpperCase.toString());
						splits.add(splitFirstLetterUpperCase);
					}
				}
			}
		}
		return splits;
	}
	
	/**
	 * Get set of related lemma given the lexical resource GermaNet
	 * 
	 * @param lemma
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @return
	 * @throws LexicalResourceException
	 */
	private static Set<String> getRelatedLemmas(String lemma, GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations) 
			throws LexicalResourceException{
		
		Set<String> relatedLemmas = new HashSet<String>();
		if(germaNetWrapper != null) {
			for(GermaNetRelation gnRelation : germaNetRelations){
				if(gnRelation.toGermaNetString().equals("has_hyponym")){
					for(LexicalRule<?> rule : germaNetWrapper.getRulesForRight(lemma, null, gnRelation)){
						relatedLemmas.add(rule.getLLemma());
					}
				}
				else {
					for(LexicalRule<?> rule : germaNetWrapper.getRulesForLeft(lemma, null, gnRelation)){ 
						relatedLemmas.add(rule.getRLemma());
					}
				}
			}
		}
		return relatedLemmas;
	}
	
	/**
	 * Split the lemma string around matches of the regular expression \\|
	 * (to deal with lemma type: lemma|lemma, which is sometimes return by TreeTagger).
	 * 
	 * @param lemma
	 * @return
	 */
	private static String [] getLemmas(String lemma) {
		return lemma.split("\\|");
	}
	
	/**
	 * Get Set<EntailmentUnit> with a given lemmatized text
	 * 
	 * @param egr
	 * @param lemmatizedText
	 * @param ignoreCase
	 * @return
	 */
	private static Set<EntailmentUnit> getLemmatizedVertex(EntailmentGraphRaw egr, String lemmatizedText, boolean ignoreCase) {
		Set<EntailmentUnit> resultSet = new HashSet<EntailmentUnit>();
		if(ignoreCase) {
			lemmatizedText = lemmatizedText.toLowerCase();
		}
		if(egr.hasLemmatizedLabel()){
			for(EntailmentUnit eu : egr.vertexSet()){
				
				String euLemmatizedText = eu.getLemmatizedText();
				if(ignoreCase){
					euLemmatizedText = euLemmatizedText.toLowerCase();
				}
				
				if (euLemmatizedText.equals(lemmatizedText)){
					resultSet.add(eu);
				}
			}
		}
		return  resultSet;
	}
	
	/**
	 * Get Set<EntailmentUnit> with given lemmatized text from a Set<String> lemmatizedTextSet
	 * 
	 * @param egr
	 * @param lemmatizedTextSet
	 * @param ignoreCase
	 * @return
	 */
	private static Set<EntailmentUnit> getLemmatizedVertex(EntailmentGraphRaw egr, Set<String> lemmatizedTextSet, boolean ignoreCase) {
		Set<EntailmentUnit> resultSet = new HashSet<EntailmentUnit>();
		Set<String> textsToFind = new HashSet<String>();
		
		if(ignoreCase){
			for(String lemmatizedText : lemmatizedTextSet){
				textsToFind.add(lemmatizedText.toLowerCase());
			}
		}else {
			textsToFind = lemmatizedTextSet;
		}
		
		if(egr.hasLemmatizedLabel()){
			for(EntailmentUnit eu : egr.vertexSet()){
				String lemmatizedText = eu.getLemmatizedText();
				if(!lemmatizedText.isEmpty()){
					if(ignoreCase){
						lemmatizedText = lemmatizedText.toLowerCase();
					}
					if(textsToFind.contains(lemmatizedText)){
						resultSet.add(eu);
					}
				}
			}
		}
		return  resultSet;
	}
	
	/**
	 * Given two sets A and B, combine every element of A and B to a string "a b" and "b a"
	 * Return a list of all combinations.
	 * 
	 * @param aSet
	 * @param bSet
	 * @param useLowerCase
	 * @return
	 */
	private static Set<String> getPermutations(Set<String> aSet, Set<String> bSet, boolean useLowerCase){
		Set<String> permutations = new HashSet<String>();
		for(String a : aSet){
			for(String b : bSet){
				if(useLowerCase){
					permutations.add(a.toLowerCase() + " " + b.toLowerCase());
					permutations.add(b.toLowerCase() + " " + a.toLowerCase());
				}
				else {
					permutations.add(a + " " + b);
					permutations.add(b + " " + a);
				}
			}
		}
		return permutations;
	}
	
}

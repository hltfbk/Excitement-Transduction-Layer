package eu.excitementproject.tl.composition.confidencecalculator;

import java.util.HashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.excitementproject.tl.composition.exceptions.ConfidenceCalculatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.collapsedgraph.GraphStatistics;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;


/**
* This module computes a confidence score per category for each node in an collapsed graph 
* based on the frequency distribution in the mentions associated to the node. 
*/

public class ConfidenceCalculatorCategoricalFrequencyDistribution extends AbstractConfidenceCalculator {
	
	static Logger logger = Logger.getLogger(ConfidenceCalculatorCategoricalFrequencyDistribution.class); 

	static char termFrequencyDocument = 'n'; //= 'l'; // n (natural), l (logarithm)
	static char documentFrequencyDocument = 'n';  //= 't'; // n (no), t (idf)
	static char normalizationDocument = 'n'; // = 'c'; // n (none), c (cosine) 
	private int categoryBoost; //score used to boost categories associated to fragments appearing in the 
		//description of the category
	String method = "tfidf"; // = "tfidf" or "bayes"

	boolean documentLengthNormalization = false; 

	/**
	 * If no categoryBoost is passed explicitly, it's set to 0.
	 */
	public ConfidenceCalculatorCategoricalFrequencyDistribution() {
		this.categoryBoost = 0;
	}
	
	/**
	 * @param categoryBoost - score used to boost categories associated to fragments appearing in the 
	 * description of the category
	 */
	public ConfidenceCalculatorCategoricalFrequencyDistribution(int categoryBoost) {
		this.categoryBoost = categoryBoost;
	}
	
	/**
	 * @param methodTfIdf - TF-IDF variant; based on SMART notation; the three characters refer to the 
	 * three document-related parameters of the SMART variant
	 */
	public ConfidenceCalculatorCategoricalFrequencyDistribution(char[] methodTfIdf) {
		this.categoryBoost = 0;
		method = "tfidf";
		if (methodTfIdf.length != 3) {
			logger.error("SMART notation not complete!");
			System.exit(1);	
		} else {
			termFrequencyDocument = methodTfIdf[0];
			documentFrequencyDocument = methodTfIdf[1];
			normalizationDocument = methodTfIdf[2];
		}
	}

	/**
	 * @param methodTfIdf - TF-IDF variant; based on SMART notation; the three characters refer to the 
	 * three document-related parameters of the SMART variant
	 * 
	 * @param categoryBoost - score used to boost categories associated to fragments appearing in the 
	 * description of the category
	 */
	public ConfidenceCalculatorCategoricalFrequencyDistribution(char[] methodTfIdf, int categoryBoost) {
		this.categoryBoost = categoryBoost;
		method = "tfidf";
		if (methodTfIdf.length != 3) {
			logger.error("SMART notation not complete!");
			System.exit(1);	
		} else {
			termFrequencyDocument = methodTfIdf[0];
			documentFrequencyDocument = methodTfIdf[1];
			normalizationDocument = methodTfIdf[2];
		}
	}
	
	/**
	 * @param method - method used for computing the combined category scores 
	 */	
	public ConfidenceCalculatorCategoricalFrequencyDistribution(String method) {
		this.method = method;
	}

	/**
	 * Compute category confidence scores and add them to the graph
	 */
	@Override
	public void computeCategoryConfidences(EntailmentGraphCollapsed graph)
			throws ConfidenceCalculatorException {
		
		GraphStatistics graphStatistics = computeGraphStatistics(graph);
		
		Set<EquivalenceClass> nodes = graph.vertexSet();	
		HashMap<String,Double> sumOfSquaredScoresPerCategory = new HashMap<String,Double>();
		
		for (EquivalenceClass node : nodes) { //for each node in the graph
			//map collecting the different categories and how often their occur on this node
			HashMap<String, Integer> categoryFrequencyDistributionOnNode 
				= new HashMap<String, Integer>();
			Set<String> categoriesToBoost = new HashSet<String>(); 
			// For each node, we first collect the frequency distribution on the node by 
			// retrieving the category of each of the m mentions associated to the node 
			// and storing the sum of occurrences of this category. 
			collectFrequencyDistributionPerNode(node,
					categoryFrequencyDistributionOnNode, categoriesToBoost);			
			
			// Now: compute combined category confidences for this node using the
			// specified method
			Map<String,Double> categoryConfidences = new HashMap<String,Double>();
			int N = graph.getNumberOfCategories(); //overall number of categories
			
			if (method.equals("bayes")) { //maximum likelihood estimates based on frequency in data: P(w|c)
				computeConfidenceUsingBayes(graphStatistics,
						categoryFrequencyDistributionOnNode, categoriesToBoost,
						categoryConfidences);	
			} else if (method.equals("bayes_log")) { //maximum likelihood estimates based on frequency in data: P(w|c)
					computeConfidenceUsingBayesLog(graphStatistics,
							categoryFrequencyDistributionOnNode,
							categoriesToBoost, categoryConfidences);
			} else if (method.equals("tfidf")) {
				computeConfidenceUsingTfidf(sumOfSquaredScoresPerCategory,
						categoryFrequencyDistributionOnNode, categoriesToBoost,
						categoryConfidences, N, 
						graphStatistics.getNumberOfMentionsPerCategory());
			}
			//add confidence scores to node
			node.setCategoryConfidences(categoryConfidences);
		}
		
		switch (normalizationDocument) {
		case 'c': //cosine normalization
			doCosineNormalization(nodes, sumOfSquaredScoresPerCategory);
		}
	}

	/**
	 * Perform cosine normalization on the score
	 * 
	 * @param nodes
	 * @param sumOfSquaredScoresPerCategory
	 */
	private void doCosineNormalization(Set<EquivalenceClass> nodes,
			HashMap<String, Double> sumOfSquaredScoresPerCategory) {
		for (EquivalenceClass node : nodes) { //for each node in the graph
			Map<String,Double> categoryConfidences = node.getCategoryConfidences();
			Map<String,Double> categoryConfidencesNormalized = new HashMap<String,Double>();
			for (String category : categoryConfidences.keySet()) {
				double confidence = categoryConfidences.get(category);
				double confidenceNormalized = confidence / Math.sqrt(sumOfSquaredScoresPerCategory.get(category));
				categoryConfidencesNormalized.put(category, confidenceNormalized);
			}
			node.setCategoryConfidences(categoryConfidencesNormalized);
		}
	}

	/** Compute confidence scores using TF-IDF	 
	 *  
 	 * To compute the final confidence for each category, we divide the sum calculated for this category
 	 * by the number of mentions associated to the node. 
	 * 
	 * It also includes a TFIDF-based implementation, where 
	 *   tf = number of occurrences of the category on this node
	 *   idf = Math.log(N/n)
	 *   N = total number of different categories
	 *   n = number of different categories associated to this particular node
	 */
	private void computeConfidenceUsingTfidf(
			HashMap<String, Double> sumOfSquaredScoresPerCategory,
			HashMap<String, Integer> categoryFrequencyDistributionOnNode,
			Set<String> categoriesToBoost,
			Map<String, Double> categoryConfidences, int N, 
			Map<String, Integer> categoryOccurrenceInGraph) {
		double score;
		double termFrequency = 0.0; 
		double documentFrequency = 0.0;
		for (String category : categoryFrequencyDistributionOnNode.keySet()) {	//for all categories in the node				
			double tf = categoryFrequencyDistributionOnNode.get(category);
			double n = categoryFrequencyDistributionOnNode.size(); //number of "documents" containing the "term" --> number of different categories in the node								
			switch (termFrequencyDocument) {
				case 'n': 
					termFrequency = tf;
					break;
				case 'l': 
					if (tf > 0) termFrequency = 1 + Math.log(tf); //sublinear tf scaling
					break;
				default: 
					logger.error("Method for confidence calculation not defined: " + method + " variant " + termFrequencyDocument+documentFrequencyDocument+normalizationDocument);
					System.exit(1);
			}
			
			if (documentLengthNormalization) termFrequency = termFrequency / (double) categoryOccurrenceInGraph.get(category);
			
			switch (documentFrequencyDocument) {
			case 'n': 
				documentFrequency = 1;
				break;
			case 't': 
				documentFrequency = Math.log(N/n);
				break;
			default: 
				logger.error("Method for confidence calculation not defined: " + method + " variant " + termFrequencyDocument+documentFrequencyDocument+normalizationDocument);
				System.exit(1);
			}
			
			score = termFrequency * documentFrequency; 
			if(categoryBoost > 0 && categoriesToBoost.contains(category)){
				score += categoryBoost;
			}
			
//					} else if (method.equals("simple")) {
//						score = tf / (double) sumMentions;
			categoryConfidences.put(category, score);
			
			double scoreSquare = score*score;
			
			if (sumOfSquaredScoresPerCategory.containsKey(category)) {
				scoreSquare += sumOfSquaredScoresPerCategory.get(category);
			}
			sumOfSquaredScoresPerCategory.put(category, scoreSquare);					
		}
	}

	/**
	 * Compute confidence scores using Naive Bayes with logarithm
	 * 
	 * @param graphStatistics
	 * @param categoryFrequencyDistributionOnNode
	 * @param categoriesToBoost
	 * @param categoryConfidences
	 */
	private void computeConfidenceUsingBayesLog(
			GraphStatistics graphStatistics,
			HashMap<String, Integer> categoryFrequencyDistributionOnNode,
			Set<String> categoriesToBoost,
			Map<String, Double> categoryConfidences) {
		for (String category : graphStatistics.getNumberOfMentionsPerCategory().keySet()) { //for all existing categories
			double tf = 0.0;
			if (categoryFrequencyDistributionOnNode.containsKey(category)) {
				tf = categoryFrequencyDistributionOnNode.get(category);
			}
			double smoothingParameter = 1.0; //Laplace smoothing --> apply Laplace estimation by assuming a uniform distribution over all words
			/* double smoothingParameter = 0.1; 					
			P(w|c) = (count(w,c)+1) / count(c) + vocabulary_size
			count(w,c) --> how often does category appear in node
			count(c) --> how often does category appear in graph */
			//estimate log likelihoods
			//logLikelihood = Math.log((count+1.0)/(featureOccurrencesInCategory.get(category)+knowledgeBase.d)); --> Implementation by Vasilis Vryniotis 
			double loglikelihood = Math.log((tf+smoothingParameter) / (graphStatistics.getNumberOfMentionsPerCategory().get(category) + graphStatistics.getTotalNumberOfNodes()));
			if(categoryBoost > 0 && categoriesToBoost.contains(category)){
				loglikelihood += categoryBoost;
			}
			categoryConfidences.put(category, loglikelihood);
		}	
		logger.debug("categoryConfidences: " + categoryConfidences);
	}

	/**
	 * Compute confidence using Naive Bayes
	 * 
	 * @param graphStatistics
	 * @param categoryFrequencyDistributionOnNode
	 * @param categoriesToBoost
	 * @param categoryConfidences
	 */
	private void computeConfidenceUsingBayes(GraphStatistics graphStatistics,
			HashMap<String, Integer> categoryFrequencyDistributionOnNode,
			Set<String> categoriesToBoost,
			Map<String, Double> categoryConfidences) {
		double score;
		for (String category : graphStatistics.getNumberOfMentionsPerCategory().keySet()) { //for all existing categories
			double tf = 0.0;
			if (categoryFrequencyDistributionOnNode.containsKey(category)) {
				tf = categoryFrequencyDistributionOnNode.get(category);
			}
			double smoothingParameter = 1.0; //Laplace smoothing --> apply Laplace estimation by assuming a uniform distribution over all words
			/* double smoothingParameter = 0.1; 					
			P(w|c) = (count(w,c)+1) / count(c) + vocabulary_size
			count(w,c) --> how often does category appear in node
			count(c) --> how often does category appear in graph */
			score = (tf+smoothingParameter) / (graphStatistics.getNumberOfMentionsPerCategory().get(category) + graphStatistics.getTotalNumberOfMentions());
			if(categoryBoost > 0 && categoriesToBoost.contains(category)){
				score += categoryBoost;
			}
			logger.info("Bayes score for category " + category + " : " + score);
			logger.info("tf: " + tf);
			categoryConfidences.put(category, score);
		}
	}

	/**
	 * 
	 * 
	 * @param node
	 * @param categoryFrequencyDistributionOnNode
	 * @param categoriesToBoost
	 */
	private void collectFrequencyDistributionPerNode(EquivalenceClass node,
			HashMap<String, Integer> categoryFrequencyDistributionOnNode,
			Set<String> categoriesToBoost) {
		Set<EntailmentUnit> eus = node.getEntailmentUnits();			
		for (EntailmentUnit eu : eus) {	//for each entailment unit in the node		
			for (EntailmentUnitMention mentionOnNode : eu.getMentions()) { 
				//for each mention associated to the entailment unit
				String categoryMention = mentionOnNode.getCategoryId();
				if(categoryBoost > 0){ //collect the categories to boost 
					//(each category for which the node text appears in the description of the category)
					if(mentionOnNode.getInteractionId().startsWith("c_")){
						categoriesToBoost.add(categoryMention);
					}
				}
				int tf = 0; //how often does the "term" occur in the "document" 
				//--> how often does category occur on the node
				if (categoryFrequencyDistributionOnNode.containsKey(categoryMention)) {
					tf = categoryFrequencyDistributionOnNode.get(categoryMention);
				}
				tf++;
				categoryFrequencyDistributionOnNode.put(categoryMention, tf);
			}
		}
	}

	/**
	 * Compute some graph statistics needed for computing the confidence scores. 
	 * 
	 * @param graph - the graph for which the statistics are computed
	 * @return
	 */
	private GraphStatistics computeGraphStatistics(
			EntailmentGraphCollapsed graph) {
		//compute number of mentions per category (for graph statistics)
		GraphStatistics graphStatistics = new GraphStatistics();
		Set<EquivalenceClass> nodes = graph.vertexSet();	
		int totalNumberOfMentions = 0;
		Map<String,Integer> numberOfMentionsPerCategory = new HashMap<String,Integer>(); //for graph statistics
				
		for (EquivalenceClass node : nodes) { //for each node in the graph
			Set<EntailmentUnit> eus = node.getEntailmentUnits();			
			for (EntailmentUnit eu : eus) {	//for each entailment unit in the node		
				for (EntailmentUnitMention mentionOnNode : eu.getMentions()) { //for each entailment unit mention
					totalNumberOfMentions++; //count total number of mentions
					String categoryMention = mentionOnNode.getCategoryId();
					int categoryCount = 0;
					if (numberOfMentionsPerCategory.containsKey(categoryMention)) {
						categoryCount = numberOfMentionsPerCategory.get(categoryMention);
					}
					categoryCount++;
					numberOfMentionsPerCategory.put(categoryMention, categoryCount); //count number of mentions per category
				}
			}
		}
		graphStatistics.setNumberOfMentionsPerCategory(numberOfMentionsPerCategory);
		graphStatistics.setTotalNumberOfMentions(totalNumberOfMentions);
		graphStatistics.setTotalNumberOfNodes(graph.getNumberOfEquivalenceClasses());
		graph.setGraphStatistics(graphStatistics);

		return graphStatistics;
	}
}

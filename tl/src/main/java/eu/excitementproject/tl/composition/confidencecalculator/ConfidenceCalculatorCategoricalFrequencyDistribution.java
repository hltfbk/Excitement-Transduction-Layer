package eu.excitementproject.tl.composition.confidencecalculator;

import java.util.HashMap;
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
* Computes a confidence score per category for each node in the collapsed graph based on 
* the frequency distribution in the mentions associated to the node. 
*  
* For each node, we first collect the frequency distribution on the node by retrieving the category
* of each of the m mentions associated to the node and storing the sum of occurrences of this category. 
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

public class ConfidenceCalculatorCategoricalFrequencyDistribution extends AbstractConfidenceCalculator {
	
	static Logger logger = Logger.getLogger(ConfidenceCalculatorCategoricalFrequencyDistribution.class); 

	String method = "tfidf_sum"; //or "simple"
	
	public ConfidenceCalculatorCategoricalFrequencyDistribution() {
		this.method = "tfidf_sum";
	}

	public ConfidenceCalculatorCategoricalFrequencyDistribution(String method) {
		this.method = method;
	}
	
	@Override
	public void computeCategoryConfidences(EntailmentGraphCollapsed graph)
			throws ConfidenceCalculatorException {

		GraphStatistics graphStatistics = computeGraphStatistics(graph);
		
		Set<EquivalenceClass> nodes = graph.vertexSet();	
		
		for (EquivalenceClass node : nodes) { //for each node in the graph
			int sumMentions = 0;
			//map collecting the different categories and how often their occur on this node
			HashMap<String, Integer> categoryFrequencyDistributionOnNode 
				= new HashMap<String, Integer>();
			Set<EntailmentUnit> eus = node.getEntailmentUnits();			
			for (EntailmentUnit eu : eus) {	//for each entailment unit in the node		
				for (EntailmentUnitMention mentionOnNode : eu.getMentions()) { 
					//for each mention associated to the entailment unit
					sumMentions++; 
					String categoryMention = mentionOnNode.getCategoryId();
					int tf = 0; //how often does the "term" occur in the "document" --> how often does category occur on the node
					if (categoryFrequencyDistributionOnNode.containsKey(categoryMention)) {
						tf = categoryFrequencyDistributionOnNode.get(categoryMention);
					}
					tf++;
					categoryFrequencyDistributionOnNode.put(categoryMention, tf);
				}
			}
			
			//compute combined category confidences for this node
			Map<String,Double> categoryConfidences = new HashMap<String,Double>();
			int N = graph.getNumberOfCategories(); //overall number of categories
			double score = 0;
			
			if (method.equals("bayes")) { //maximum likelihood estimates based on frequency in data: P(w|c)
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
					logger.info("Bayes score for category " + category + " : " + score);
					logger.info("tf: " + tf);
					categoryConfidences.put(category, score);
				}	
			} else if (method.equals("bayes_log")) { //maximum likelihood estimates based on frequency in data: P(w|c)
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
						categoryConfidences.put(category, loglikelihood);
					}	
					logger.debug("categoryConfidences: " + categoryConfidences);
			} else {
				for (String category : categoryFrequencyDistributionOnNode.keySet()) {	//for all categories in the node				
					double tf = categoryFrequencyDistributionOnNode.get(category);
					if (method.equals("tfidf_sum")) {
						double n = categoryFrequencyDistributionOnNode.size(); //number of "documents" containing the "term" --> number of different categories in the node
						double idf = Math.log(N/n);
						score = tf*idf;
					} else if (method.equals("simple")) {
						score = tf / (double) sumMentions;
					} else {
						logger.error("Method for confidence calculation not defined: " + method);
						System.exit(1);
					}
					categoryConfidences.put(category, score);
				}
			}
			//add confidence scores to node
			node.setCategoryConfidences(categoryConfidences);
		}
	}

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

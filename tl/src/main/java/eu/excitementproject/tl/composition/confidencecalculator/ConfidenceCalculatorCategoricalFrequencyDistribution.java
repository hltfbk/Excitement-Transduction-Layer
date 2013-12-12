package eu.excitementproject.tl.composition.confidencecalculator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.tl.composition.exceptions.ConfidenceCalculatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
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
	
	boolean tfidf = true; 
	
	public ConfidenceCalculatorCategoricalFrequencyDistribution() {
		this.tfidf = true;
	}

	public ConfidenceCalculatorCategoricalFrequencyDistribution(boolean tfidf) {
		this.tfidf = tfidf;
	}
	
	@Override
	public void computeCategoryConfidences(EntailmentGraphCollapsed graph)
			throws ConfidenceCalculatorException {

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
			for (String category : categoryFrequencyDistributionOnNode.keySet()) {	
				double tf = categoryFrequencyDistributionOnNode.get(category);
				if (tfidf) {
					double n = categoryFrequencyDistributionOnNode.size(); //number of "documents" containing the "term" --> number of different categories in the node
					double idf = Math.log(N/n);
					score = tf*idf;
				} else {
				  score = tf / (double) sumMentions;
				}
				categoryConfidences.put(category, score);
			}
			//add confidence scores to node
			node.setCategoryConfidences(categoryConfidences);
		}
	}
}

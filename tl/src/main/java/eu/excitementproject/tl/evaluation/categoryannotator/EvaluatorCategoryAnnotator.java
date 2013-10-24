package eu.excitementproject.tl.evaluation.categoryannotator;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;

import eu.excitement.type.tl.CategoryAnnotation;
import eu.excitement.type.tl.CategoryDecision;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.configuration.CommonConfig;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.common.exception.ConfigurationException;
import eu.excitementproject.eop.core.ImplCommonConfig;
import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.composition.api.ConfidenceCalculator;
import eu.excitementproject.tl.composition.confidencecalculator.ConfidenceCalculatorCategoricalFrequencyDistribution;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.ConfidenceCalculatorException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.decomposition.exceptions.DataReaderException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.InteractionReader;
import eu.excitementproject.tl.laputils.LemmaLevelLapDE;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;
import eu.excitementproject.tl.toplevel.usecasetworunner.UseCaseTwoRunnerPrototype;

/**
 * 
 * @author Kathrin
 *
 * This class evaluates the category annotation on an incoming email (use case 2). 
 * 
 * It first reads in a dataset of emails associated to categories and splits it into a training and test set. 
 * It then builds an entailment graph (collapsed) from the training set, and annotates the emails in the test set
 * based on the generated entailment graph. Finally, it compares the automatically created categories to the 
 * manually annotated categories in the test set.
 * 
 * As the manually assigned categories are per email, whereas the automatically generated ones are assigend per 
 * entailment unit mention, we first calculate a combined score for each automatically assigned category
 * by summing up all confidences per category to get the "best" category (the one with the highest sum). 
 * This best category is then compared to the manually assigned one. 
 */

public class EvaluatorCategoryAnnotator {
	
	public static void main(String[] args) {
		String inputFilename = "./src/test/resources/WP2_public_data_XML/dummy_data_for_evaluator_test.xml"; //dataset to be evaluated
		String outputDirname = "D:/temp"; //output directory (for generated entailment graph)
		String configFilename = "./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_DE.xml"; //config file for EDA
				
		runEvaluationOnTrainTestDataset(inputFilename, outputDirname, configFilename);
	}

	public static double runEvaluationOnTrainTestDataset(String inputFilename, String outputDirname, String configFilename) {
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.categoryannotator"); 
		
		File configFile = new File(configFilename);	
		CommonConfig config = null;
		LAPAccess lap;
		EDABasic<?> eda;
		UseCaseOneRunnerPrototype use1;
		UseCaseTwoRunnerPrototype use2;
		EntailmentGraphCollapsed graph = null;
		ConfidenceCalculator cc = null;
		
		// Read in all emails with their associated categories and split into train/test set
		String[] files =  {inputFilename};
		File f;
		Set<Interaction> docs = new HashSet<Interaction>();

		try {
			for (String name: files) {
				logger.info("Reading " + name);
				f = new File(name);
				docs.addAll(InteractionReader.readInteractionXML(f));
			}
			
			// Split emails into train and test set
			Set<Interaction> docsTrain = new HashSet<Interaction>();
			Set<Interaction> docsTest = new HashSet<Interaction>();
			for (Interaction doc : docs) {
				if (Integer.parseInt(doc.getInteractionId()) % 3 == 0) docsTest.add(doc);
				else docsTrain.add(doc);
			}
			logger.info("Training set contains " + docsTrain.size() + " documents.");
			logger.info("Test set contains " + docsTest.size() + " documents.");
			
			File theDir = new File(outputDirname);
			
			// if the directory does not exist, create it
			if (!theDir.exists()) {
				logger.debug("Creating directory: " + outputDirname);
				boolean result = theDir.mkdir();
				if(result){
					logger.debug("DIR created");
				} else {
					logger.debug("Could not create the output directory. No output files will be created.");
					outputDirname=null;
				}
			}
			
			lap = new LemmaLevelLapDE();
			config = new ImplCommonConfig(configFile);
			eda = new MaxEntClassificationEDA();	
			eda.initialize(config);
			logger.info("Initialized config.");
			use1 = new UseCaseOneRunnerPrototype(lap, eda, outputDirname);
			double threshold = 0.99;
			graph = use1.buildCollapsedGraph(docsTrain, threshold);
			logger.info("Built collapsed graph.");
			cc = new ConfidenceCalculatorCategoricalFrequencyDistribution();
			cc.computeCategoryConfidences(graph);
			String outputFile = outputDirname + "/test.graph.xml";
			graph.toXML(outputFile);			
			graph = new EntailmentGraphCollapsed(new File(outputFile));
			//GraphViewer.drawGraph(graph);

			logger.info("Computed and added category confidences.");

			// Send each email in test data + graph to node use case 2 and have it annotated
			int countPositive = 0;
			for (Interaction doc : docsTest) {
				JCas cas;
				cas = doc.createAndFillInputCAS();
				use2 = new UseCaseTwoRunnerPrototype(lap, eda);
				use2.annotateCategories(cas, graph);
				logger.info("_________________________________________________________");
				Set<CategoryDecision> decisions = CASUtils.getCategoryAnnotationsInCAS(cas);
				logger.info("Found " + decisions.size() + " decisions in CAS for interaction " + doc.getInteractionId());
				CASUtils.dumpAnnotationsInCAS(cas, CategoryAnnotation.type);
				
				// Compare automatic to manual annotation
				String bestCat = "";
			    HashMap<String,Double> categoryScores = new HashMap<String,Double>();
				for (CategoryDecision decision: decisions) {
					String category = decision.getCategoryId();
					double sum = 0;
					if (categoryScores.containsKey(category)) {
						sum = categoryScores.get(category);
					}
					//add up all scores for each category on the CAS and compare the sums of each category
					sum += decision.getConfidence();
					categoryScores.put(category, sum);
				}
				ValueComparator bvc =  new ValueComparator(categoryScores);
		        Map<String,Double> sortedMap = new TreeMap<String,Double>(bvc);
		        sortedMap.putAll(categoryScores);
		        logger.debug("category sums:  " + sortedMap);
				if (sortedMap.size() > 0) {
					bestCat = sortedMap.keySet().iterator().next().toString();
					logger.info("Best category: " + bestCat);
					logger.info("Correct category: " + doc.getCategory());
					if (doc.getCategory().equals(bestCat)) {
						countPositive++;
					} 
				}				
			}
			
			// Compute and print result	
			double result = (double) countPositive / (double) docsTest.size();
			logger.info("Final result: " + result);
			return result;
			
		} catch (ConfigurationException | EDAException | ComponentException 
			| FragmentAnnotatorException | ModifierAnnotatorException 
			| GraphMergerException | IOException 
			| GraphOptimizerException 
			| FragmentGraphGeneratorException 
			| ConfidenceCalculatorException 
			| NodeMatcherException 
			| CategoryAnnotatorException | DataReaderException | EntailmentGraphCollapsedException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public void runEvaluationTenFoldCross() {
		//1. read in all emails with their associated categories (test data)
		
		//2. split the dataset into ten equally sized parts
		
		//for each part P
		//	3. read in entailment graph EG generated on remaining 9 parts
		
		//	for each email E in P
		// 		4. send E + EG to node matcher / category annotator and have it annoatted
		
		//4. compare automatic to manual annotation
		
		//5. compute and print result		
	}

	/**
	 * This evaluation is expected to be more time-consuming!
	 */
	public void runEvaluationOnSingleDataset() {
		//1. read in all emails with their associated categories 
		
		//for each email E
			//2. read in entailment graph G generated on remaining emails (from resources)
		 	//3. send E + G to node matcher / category annotator and have it annotated
		
		//4. compare automatic to manual annotation
		
		//5. compute and print result		
		
	}
	
	/**
	 * This creates, for each email in the dataset, an entailment graph (collapsed)
	 * from the remaining emails in the set
	 */
	public void generateEntailmentGraphsForEvaluation() {
		//1. read in all emails
		
		//for each email
			//2. create entailment graph from remaining emails
			//3. store graph in resources
	}
}

class ValueComparator implements Comparator<String> {

    Map<String, Double> base;
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } 
    }
}


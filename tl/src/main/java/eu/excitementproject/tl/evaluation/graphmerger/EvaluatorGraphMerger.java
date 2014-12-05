package eu.excitementproject.tl.evaluation.graphmerger;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.edautils.TEDecisionWithConfidence;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.utils.EvaluationMeasures;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

/**
 * This class contains methods for evaluating graph merger results.
 * Currently the evaluation accepts ONLY edges with "entailment" decision label
 * 
 * @author Lili Kotlerman
*/
public class EvaluatorGraphMerger {


	/** Evaluates the merge graph procedure. 
	 * The assumption is that the nodes of the gold standard merged graph and the evaluated graph are the same,
	 * i.e. that both were built from exactly the same set of fragment graphs.
	 * Thus, evaluation reflects the quality of the merging process per se.
	 * @param goldStandardEdges - "entailment" edges from the gold standard annotation
	 * @param evaluatedGraphEdges - "entailment" edges from the evaluated graph
	 * @return evaluation measures (recall, precision, f1).
	 */
	public static EvaluationMeasures evaluate(Set<EntailmentRelation> goldStandardEdges, Set<EntailmentRelation> evaluatedGraphEdges, boolean includeFragmentGraphEdges){
		Logger logger = Logger.getLogger("eu.excitementproject.tl.evaluation.graphmerger.EvaluatorGraphMerger");
		logger.info("Evaluating graph edges. Include fragment graph = "+includeFragmentGraphEdges);
		//TODO Transitive closure: GS fragment graphs contain closure edges. Understand whether GS merge phase has closure edges as well. If yes - need to evaluate our graphs with transitive closure. If not - need to add closure edges when generating fragment graphs.
		// e.g. GS edge "Cannot retrieve tickets at Moonport station with card --> Cannot retrieve tickets" is not found in the raw graph
		// I think I fixed it, but need to verify again
		
		// TODO consider adding a verification of whether GS and work graph have the same set of nodes
		EvaluationMeasures eval = new EvaluationMeasures();

		Set<String> gs = new HashSet<String>(); // will hold gs edges
		Set<String> ee = new HashSet<String>(); // will hold evaluated edges
		Set<String> correct = new HashSet<String>(); // will hold common edges (correctly added)
		Set<String> fg = new HashSet<String>(); // will hold fragment graph edges to exclude, if need to exclude
		
		for (EntailmentRelation edge : goldStandardEdges){
			gs.add(getSourceAndTargetString(edge));
			correct.add(getSourceAndTargetString(edge));
		}
		logger.info("GS size: "+gs.size()+" distinct, "+goldStandardEdges.size()+" overall.");
		
		int i = 0;
		for (EntailmentRelation edge : evaluatedGraphEdges){
			i++;
			logger.debug("Evaluated edge #"+i+"\t"+edge.toString());			
			if (!edge.getLabel().is(DecisionLabel.Entailment)) continue; // only evaluate entailment edges, ignore non-entailment edges, if any
			if (!includeFragmentGraphEdges){
				if (edge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) {
					fg.add(getSourceAndTargetString(edge));
					logger.debug("\t This is a fragment graph edge, it will not be included.");			
					continue; //add this edge to fragment graph edges list (fg), don't add to evaluated graph edges (ee)
				}
			}	
			ee.add(getSourceAndTargetString(edge));
			logger.debug("\t Edge added as: <<"+getSourceAndTargetString(edge)+">>");
			logger.debug("\t Evaluated edges set is now of size: "+ee.size());			
		}
		
		logger.info("Evaluated edges: "+evaluatedGraphEdges.size()+" overall (including fragment graph edges and non-entailment edges)");
		if (!includeFragmentGraphEdges) logger.info("Evaluated edges: "+ee.size()+ " distinct, excluding "+fg.size()+" fragment graph edges and non-entailment edges");
		else logger.info("Evaluated edges: "+ee.size()+" distinct edges (excluding non-entailment edges)");
		
		// remove fg edges from the gold standard
		if (!includeFragmentGraphEdges) {
			gs.removeAll(fg);
			correct.removeAll(fg);			
			logger.info("GS size without fragment graph edges: " + gs.size());
		}

		correct.retainAll(ee); // retain only common edges
		
		logger.info(correct.size()+" correctly assigned edges out of "+ee.size() +" assigned edges");
		if (ee.size()==0) eval.setPrecision(1.0);
		else eval.setPrecision((0.0+correct.size())/ee.size());

		logger.info(correct.size()+" correctly found edges out of "+gs.size()+" edges in GS");
		if (gs.size()==0) eval.setRecall(1.0);
		else eval.setRecall((0.0+correct.size())/gs.size()); 		
		
		// memorize for micro-averaging over clusters
		eval.setTruePositives(correct.size());
		eval.setRecallDenominator(gs.size());
		eval.setPrecisionDenominator(ee.size());
		
		for (EntailmentRelation edge : evaluatedGraphEdges){
			if (correct.contains(getSourceAndTargetString(edge))) logger.debug("(+) "+ edge);
			else if(!fg.contains(getSourceAndTargetString(edge)))  {
				logger.debug("(- FP) " + edge);
				eval.addFalsePositiveExample(edge.toString());
			}
		}
		for (EntailmentRelation edge : goldStandardEdges){
			if (!correct.contains(getSourceAndTargetString(edge))){
				if(!fg.contains(getSourceAndTargetString(edge))) {
					logger.debug("(- FN) " + edge);
					eval.addFalseNegativeExample(edge.toString());
				}
			}
		}

		Set<EntailmentUnit> nodes = new HashSet<EntailmentUnit>();
		for(EntailmentRelation e : goldStandardEdges){
			nodes.add(e.getSource());
			nodes.add(e.getTarget());
		}
		
		int allDecisions = 0;
		int correctDecisions = 0;
		// go over all possible pairs of different nodes, 
		// and see whether the assignment of yes/no edge was correct or not
		for (EntailmentUnit nodeA : nodes){
			for (EntailmentUnit nodeB : nodes){
				if(nodeA.equals(nodeB)) continue;
				// get the string representing the edge: "src text -> tgt text" // no matter which decision
				String edge = getSourceAndTargetString(new EntailmentRelation(nodeA, nodeB, new TEDecisionWithConfidence(1.0, DecisionLabel.Unknown)));
				// check if the edge is present in the gs and in the evaluated edges (both include only positive edges)
				boolean isInGs = gs.contains(edge);
				boolean isInEe = ee.contains(edge);
				// if present in both or missing from both - this is a correct assignment
				if (isInEe==isInGs) correctDecisions++;
				allDecisions++;
			}
		}
		eval.setAccuracy(correctDecisions, allDecisions);
		
		eval.setAveragePrecision(averagePrecision(evaluatedGraphEdges, goldStandardEdges, fg, includeFragmentGraphEdges));
		return eval;
	}
	
	public static Double averagePrecision(Set<EntailmentRelation> retrievedSet, Set<EntailmentRelation> relevantSet, Set<String> fgEdges, boolean includeFragmentGraphEdges){
	    
		List<EntailmentRelation> sortedEdges = new LinkedList<EntailmentRelation>(retrievedSet);
		Collections.sort(sortedEdges, new EntailmentRelation.DescendingConfidenceComparator());
		List<String> retrieved = new LinkedList<String>();
		for(EntailmentRelation e : sortedEdges){
			retrieved.add(getSourceAndTargetString(e));
		}
		
		sortedEdges = new LinkedList<EntailmentRelation>(relevantSet);
		Collections.sort(sortedEdges, new EntailmentRelation.DescendingConfidenceComparator());
		List<String> relevant = new LinkedList<String>();
		for(EntailmentRelation e : sortedEdges){
			relevant.add(getSourceAndTargetString(e));
		}

		if (!includeFragmentGraphEdges){
			retrieved.removeAll(fgEdges);
			relevant.removeAll(fgEdges);
		}
		
		int rel_len = relevant.size();
	    double rel_num=0.0;
	    double ret_num=0.0;
	    double ap = 0.0;

	    if (retrieved.size()>0){
	        if (rel_len>0){
	            for (String x : retrieved){ //check all the assigned edges, which are ordered desc by their score
	                ret_num++;
	                if (relevant.contains(x)){
	                    rel_num++;
	                    ap+=(rel_num/ret_num);
	                }
	            }
	            ap=ap/rel_len;
	        }
	    }        
	    return ap;
	}
	
	public static String getSourceAndTargetString(EntailmentRelation edge){
		return edge.getSource().getTextWithoutDoubleSpaces()+" -> "+edge.getTarget().getTextWithoutDoubleSpaces();
	}
	
	public static EvaluationMeasures evaluate(Set<EntailmentRelation> goldStandardEdges, String goldStandardAnnotationsDirectory, boolean includeFragmentGraphEdges) throws GraphEvaluatorException {
		GoldStandardEdgesLoader loader = new GoldStandardEdgesLoader(true);
		loader.loadAllAnnotations(goldStandardAnnotationsDirectory, false);
		return evaluate(goldStandardEdges, loader.getEdges(), includeFragmentGraphEdges);
	}
}

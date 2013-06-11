package eu.excitementproject.tl.toplevel.usecaseonerunner;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.collapsedgraphgenerator.SimpleCollapseGraphGenerator;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.Interaction;

public class UseCaseOneRunnerPrototype implements AbstractUseCaseOneRunner {

	/**
	 * Builds a raw entailment graph from a set of user interactions (as Interaction objects or CASes)
	 * 
	 * @param docs -- a set of user interactions (as Interaction objects or CASes)
	 * @param lap -- the lap for producing input CAS object for the EDA
	 * @param eda -- the EDA for obtaining the entailment decision
	 * 
	 * @return an raw entailment graph (the multigraph with all edges and nodes)
	 */
	@Override
	public <T> EntailmentGraphRaw buildRawGraph(Set<T> docs, LAPAccess lap, EDABasic<?> eda) 
			throws GraphMergerException, FragmentGraphGeneratorException{
		
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>(); 
		FragmentGraphGeneratorFromCAS fgg = new FragmentGraphGeneratorFromCAS();
		AutomateWP2ProcedureGraphMerger agm = new AutomateWP2ProcedureGraphMerger(lap, eda);
		
		for(T obj: docs) {
			fgs.addAll(fgg.generateFragmentGraphs(getCAS(obj)));
		}
		
		EntailmentGraphRaw egr = new EntailmentGraphRaw();
		return agm.mergeGraphs(fgs, egr);
	}
	
	/**
	 * @param f -- a file containing a serialized raw entailment graph
	 * 
	 * @return the collapsed entailment graph corresponding to the raw graph in the input file
	 */
	@Override
	public EntailmentGraphCollapsed buildGraph(File f) 
			throws CollapsedGraphGeneratorException{
		
		SimpleCollapseGraphGenerator scgg = new SimpleCollapseGraphGenerator(); 
		return scgg.generateCollapsedGraph(new EntailmentGraphRaw(f));
	}
	

 	/**
	 * @param f -- a file containing a serialized raw entailment graph
	 * 
	 * @return the collapsed entailment graph corresponding to the raw graph in the input file
	 */
/*	@Override
	public EntailmentGraphCollapsed buildGraph(File f, double confidence) 
			throws CollapsedGraphGeneratorException{
			
		SimpleCollapseGraphGenerator scgg = new SimpleCollapseGraphGenerator(); 
		return scgg.generateCollapsedGraph(new EntailmentGraphRaw(f),confidence);		
	}
*/
	
	/**
	 * Builds a collapsed entailment graph from a set of user interactions (as Interaction objects or CASes), using a given lap and EDA
	 * 
	 * @param docs -- a set of user interactions (as Interaction objects or CASes)
	 * @param lap -- the lap for producing input CAS object for the EDA
	 * @param eda -- the EDA for obtaining the entailment decision
	 * 
	 * @return a collapsed entailment graph
	 */
	@Override
	public <T> EntailmentGraphCollapsed buildGraph(Set<T> docs, LAPAccess lap, EDABasic<?> eda) 
				throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException {
		
		SimpleCollapseGraphGenerator scgg = new SimpleCollapseGraphGenerator();		
		return scgg.generateCollapsedGraph(buildRawGraph(docs, lap, eda));
	}
	
	
	/**
	 * Builds a collapsed entailment graph from a set of user interactions (as Interaction objects or CASes), 
	 * using a given lap and EDA, and filtering the edges using the given confidence score 
	 * 
	 * @param docs -- a set of user interactions (as Interaction objects or CASes)
	 * @param lap -- the lap for producing input CAS object for the EDA
	 * @param eda -- the EDA for obtaining the entailment decision
	 * @param confidence -- a score for filtering edges
	 * 
	 * @return a collapsed entailment graph
	 */
/*	@Override
	public EntailmentGraphCollapsed buildGraph(Set<?> docs,LAPAccess lap, EDABasic<?> eda, double confidence) 
			throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException{
			
		SimpleCollapseGraphGenerator scgg = new SimpleCollapseGraphGenerator();		
		return scgg.generateCollapsedGraph(buildRawGraph(docs),confidence);		
	}
*/	
	
	/**
	 * Returns a CAS object for a given Interaction or CAS object
	 * 
	 * @param obj
	 * @return the corresponding CAS object
	 */
	private <T> JCas getCAS(T obj) {
		String className = obj.getClass().getName();
		if (className.contains("Interaction")) {
			try {
				return ((Interaction) obj).createAndFillInputCAS();
			} catch (LAPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return (JCas) obj;
	}
}

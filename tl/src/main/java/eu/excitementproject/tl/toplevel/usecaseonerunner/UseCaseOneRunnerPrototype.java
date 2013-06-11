package eu.excitementproject.tl.toplevel.usecaseonerunner;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.CollapsedGraphGenerator;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.collapsedgraphgenerator.SimpleCollapseGraphGenerator;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.toplevel.api.UseCaseOneRunner;

public class UseCaseOneRunnerPrototype implements UseCaseOneRunner {


	LAPAccess lap = null;
	EDABasic<?> eda = null;
	
	FragmentAnnotator fragAnot;
	ModifierAnnotator modAnot;
	FragmentGraphGenerator fragGen;
	GraphMerger graphMerger;
	CollapsedGraphGenerator collapseGraph;
	
	
	public UseCaseOneRunnerPrototype(LAPAccess lap, EDABasic<?> eda) throws FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException{
		this.lap = lap;
		this.eda = eda;
		
		initInterfaces();
	}
	
	public UseCaseOneRunnerPrototype() throws FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException {
		initInterfaces();
	}
	
	
	private void initInterfaces() throws FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException {
		fragAnot = new SentenceAsFragmentAnnotator(lap);
		modAnot = new AdvAsModifierAnnotator(lap); 		

		fragGen = new FragmentGraphGeneratorFromCAS();
		graphMerger = new AutomateWP2ProcedureGraphMerger(lap, eda);
		collapseGraph = new SimpleCollapseGraphGenerator();
	}
	
	/**
	 * Builds a raw entailment graph from a set of user interactions
	 * 
	 * @param docs -- a set of user interactions as Interaction objects
	 *  
	 * @return an raw entailment graph (the multigraph with all edges and nodes)
	 * @throws LAPException 
	 * @throws ModifierAnnotatorException 
	 * @throws FragmentAnnotatorException 
	 */
	public EntailmentGraphRaw buildRawGraph(Set<Interaction> docs) 
			throws GraphMergerException, FragmentGraphGeneratorException, LAPException, FragmentAnnotatorException, ModifierAnnotatorException{
		
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>(); 

		for(Interaction i: docs) {
			JCas aJCas = i.createAndFillInputCAS();
			annotateCAS(aJCas);
			fgs.addAll(fragGen.generateFragmentGraphs(i.createAndFillInputCAS()));
		}
		
		return graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());
	}
	
	
	/**
	 * Builds a raw entailment graph from a set of user interactions
	 * 
	 * @param docs -- a set of user interactions as JCas objects
	 *  
	 * @return an raw entailment graph (the multigraph with all edges and nodes)
	 * @throws FragmentAnnotatorException 
	 * @throws ModifierAnnotatorException 
	 */
	public EntailmentGraphRaw buildRawGraph(List<JCas> docs) 
			throws GraphMergerException, FragmentGraphGeneratorException, FragmentAnnotatorException, ModifierAnnotatorException{
		
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>(); 

		
		for(JCas aJCas: docs) {
			annotateCAS(aJCas);
			fgs.addAll(fragGen.generateFragmentGraphs(aJCas));
		}
		
		return graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());
	}
	
	/**
	 * @param f -- a file containing a serialized raw entailment graph
	 * 
	 * @return the collapsed entailment graph corresponding to the raw graph in the input file
	 */
	public EntailmentGraphCollapsed buildGraph(File f) 
			throws CollapsedGraphGeneratorException{
		
		return collapseGraph.generateCollapsedGraph(new EntailmentGraphRaw(f));
	}
	

 	/**
	 * @param f -- a file containing a serialized raw entailment graph
	 * 
	 * @return the collapsed entailment graph corresponding to the raw graph in the input file
	 */
/*	
	public EntailmentGraphCollapsed buildGraph(File f, double confidence) 
			throws CollapsedGraphGeneratorException{
			
		SimpleCollapseGraphGenerator scgg = new SimpleCollapseGraphGenerator(); 
		return scgg.generateCollapsedGraph(new EntailmentGraphRaw(f),confidence);		
	}
*/
	
	/**
	 * Builds a collapsed entailment graph from a set of user interactions
	 * 
	 * @param docs -- a set of user interactions (as Interaction objects)
	 * 
	 * @return a collapsed entailment graph
	 * @throws ModifierAnnotatorException 
	 * @throws FragmentAnnotatorException 
	 * @throws LAPException 
	 */
	public EntailmentGraphCollapsed buildGraph(Set<Interaction> docs) 
				throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException, LAPException, FragmentAnnotatorException, ModifierAnnotatorException {
		
		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs));
	}
	
	
	/**
	 * Builds a collapsed entailment graph from a set of user interactions
	 * 
	 * @param docs -- a set of user interactions (as JCas objects)
	 * 
	 * @return a collapsed entailment graph
	 * @throws ModifierAnnotatorException 
	 * @throws FragmentAnnotatorException 
	 */
	public EntailmentGraphCollapsed buildGraph(List<JCas> docs) 
				throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException, FragmentAnnotatorException, ModifierAnnotatorException {
		
		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs));
	}
	
	/**
	 * Builds a collapsed entailment graph from a set of user interactions 
	 * and filtering the edges using the given confidence score 
	 * 
	 * @param docs -- a set of user interactions (as Interaction objects)
	 * @param confidence -- a score for filtering edges
	 * 
	 * @return a collapsed entailment graph
	 */
/*	
	public EntailmentGraphCollapsed buildGraph(Set<Interaction> docs, double confidence) 
			throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException{

		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs),confidence);		
	}
*/
	
	
	/**
	 * Builds a collapsed entailment graph from a set of user interactions 
	 * and filtering the edges using the given confidence score 
	 * 
	 * @param docs -- a set of user interactions (as JCas objects)
	 * @param confidence -- a score for filtering edges
	 * 
	 * @return a collapsed entailment graph
	 * @throws FragmentAnnotatorException 
	 * @throws ModifierAnnotatorException 
	 */
/*	
	public EntailmentGraphCollapsed buildGraph(List<JCas> docs, double confidence) 
			throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException{
			
		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs),confidence);		
	}
*/


	private void annotateCAS(JCas aJCas) throws FragmentAnnotatorException, ModifierAnnotatorException {
		fragAnot.annotateFragments(aJCas);
		modAnot.annotateModifiers(aJCas);
	}

}

package eu.excitementproject.tl.toplevel.usecaseonerunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.CollapsedGraphGenerator;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.collapsedgraphgenerator.SimpleCollapseGraphGenerator;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
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
	
	String outputPath = null;
	
	private final static Logger logger = Logger.getLogger(UseCaseOneRunnerPrototype.class.getName());
	
	public UseCaseOneRunnerPrototype(LAPAccess lap, EDABasic<?> eda, String outputFolder) throws FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, IOException{
		this.lap = lap;
		this.eda = eda;
		this.outputPath = outputFolder;
		if (outputFolder != null){
			BufferedWriter logfile = new BufferedWriter(new FileWriter(outputFolder+"/log.txt")); //create log file (if exists - rewrite)
			logfile.write(outputFolder+": "+Calendar.getInstance()+"\n");
			logfile.close();			
		}
		
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
	@Override
	public EntailmentGraphRaw buildRawGraph(Set<Interaction> docs) 
			throws GraphMergerException, FragmentGraphGeneratorException, LAPException, FragmentAnnotatorException, ModifierAnnotatorException, IOException{
		
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>(); 

		for(Interaction i: docs) {
			JCas aJCas = i.createAndFillInputCAS();
			annotateCAS(aJCas);
			log("Adding fragment graphs for text: " + aJCas.getDocumentText());
			fgs.addAll(fragGen.generateFragmentGraphs(aJCas));
		}
		
		inspectGraph(fgs);
		
		return graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());
	}
	
	private void log(String message) throws IOException{
		logger.info(message);
		if (outputPath != null){
			BufferedWriter logfile = new BufferedWriter(new FileWriter(outputPath+"/log.txt",true)); //open log file for append
			logfile.write(message+"\n");
			logfile.close();			
		}
	}
	
	
	/**
	 * Builds a raw entailment graph from a set of user interactions
	 * 
	 * @param docs -- a set of user interactions as JCas objects
	 *  
	 * @return an raw entailment graph (the multigraph with all edges and nodes)
	 * @throws FragmentAnnotatorException 
	 * @throws ModifierAnnotatorException 
	 * @throws LAPException 
	 */
	@Override
	public EntailmentGraphRaw buildRawGraph(List<JCas> docs) 
			throws GraphMergerException, FragmentGraphGeneratorException, FragmentAnnotatorException, ModifierAnnotatorException, LAPException, IOException{
		
		Set<FragmentGraph> fgs = new HashSet<FragmentGraph>(); 

		
		for(JCas aJCas: docs) {
			annotateCAS(aJCas);
			fgs.addAll(fragGen.generateFragmentGraphs(aJCas));
		}
		
		inspectGraph(fgs);
		
		return graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());
	}
	
	/**
	 * @param f -- a file containing a serialized raw entailment graph
	 * 
	 * @return the collapsed entailment graph corresponding to the raw graph in the input file
	 */
	@Override
	public EntailmentGraphCollapsed buildCollapsedGraph(File f) 
			throws CollapsedGraphGeneratorException{
		
		try {
			return collapseGraph.generateCollapsedGraph(new EntailmentGraphRaw(f));
		} catch (EntailmentGraphRawException e) {
			// TODO Auto-generated catch block
			throw new CollapsedGraphGeneratorException(e.getMessage());
		}
	}
	

 	/**
	 * @param f -- a file containing a serialized raw entailment graph
	 * 
	 * @return the collapsed entailment graph corresponding to the raw graph in the input file
 	 * @throws EntailmentGraphRawException 
	 */
	
	public EntailmentGraphCollapsed buildGraph(File f, double confidence) 
			throws CollapsedGraphGeneratorException, EntailmentGraphRawException{
			
		return collapseGraph.generateCollapsedGraph(new EntailmentGraphRaw(f),confidence);		
	}

	
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
	@Override
	public EntailmentGraphCollapsed buildCollapsedGraph(Set<Interaction> docs) 
				throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException, LAPException, FragmentAnnotatorException, ModifierAnnotatorException, IOException, ParserConfigurationException, TransformerException {
		
		EntailmentGraphRaw rawGraph = buildRawGraph(docs);
		inspectGraph(rawGraph);
		
//		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs));
		return collapseGraph.generateCollapsedGraph(rawGraph);
	}
	
	
	/**
	 * Builds a collapsed entailment graph from a set of user interactions
	 * 
	 * @param docs -- a set of user interactions (as JCas objects)
	 * 
	 * @return a collapsed entailment graph
	 * @throws ModifierAnnotatorException 
	 * @throws FragmentAnnotatorException 
	 * @throws LAPException 
	 */
	@Override
	public EntailmentGraphCollapsed buildCollapsedGraph(List<JCas> docs) 
				throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException, FragmentAnnotatorException, ModifierAnnotatorException, LAPException, IOException, ParserConfigurationException, TransformerException {
		
		EntailmentGraphRaw rawGraph = buildRawGraph(docs);
		inspectGraph(rawGraph);
		
//		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs));
		return collapseGraph.generateCollapsedGraph(rawGraph);
	}
	
	/**
	 * Builds a collapsed entailment graph from a set of user interactions 
	 * and filtering the edges using the given confidence score 
	 * 
	 * @param docs -- a set of user interactions (as Interaction objects)
	 * @param confidence -- a score for filtering edges
	 * 
	 * @return a collapsed entailment graph
	 * @throws IOException 
	 * @throws ModifierAnnotatorException 
	 * @throws FragmentAnnotatorException 
	 * @throws LAPException 
	 */
	
	public EntailmentGraphCollapsed buildCollapsedGraph(Set<Interaction> docs, double confidence) 
			throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException, LAPException, FragmentAnnotatorException, ModifierAnnotatorException, IOException{

		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs),confidence);		
	}


	
	public void inspectGraph(EntailmentGraphCollapsed graph) throws IOException, TransformerException, ParserConfigurationException{

		if (graph != null) { 
			log("COLLAPSED GRAPH:\n");
			log(graph.toString());
			if (outputPath != null) {
				graph.toDOT(outputPath+"/collapsed_graph.dot.txt");
				graph.toXML(outputPath+"/collapsed_graph.xml");
			}
		} else {
			log("The graph is null");
		}
	}

	
	public void inspectGraph(EntailmentGraphRaw graph) throws IOException, TransformerException, ParserConfigurationException{

		if (graph != null) { 
			log("RAW GRAPH:\n");			
			log(graph.toString());
			if (outputPath != null) {
				graph.toDOT(outputPath+"/raw_graph.dot.txt");
				graph.toXML(outputPath+"/raw_graph.xml");
			}
		} else {
			log("The graph is null");
		}		
	}
	
	
	public void inspectGraph(Set<FragmentGraph> fgs) throws IOException{
		
		log("Inspecting the fragmentGraphs:");
		int i = 1;
		
		if (fgs != null) {
			log("\t" + fgs.size() + " graphs to check");
			
			for(FragmentGraph fg : fgs) {
				if ( fg != null ) {
					log(fg.toString());
					if (outputPath != null) {
						fg.toDOT(outputPath+"/fragment_graph_" + i + ".dot.txt");
						i++;
					}
				} else {
					log("Fragment graph is null");
				}
			}
		} else {
			log("The set of fragment graphs is null");
		}
	}

	/**
	 * Builds a collapsed entailment graph from a set of user interactions 
	 * and filtering the edges using the given confidence score 
	 * 
	 * @param docs -- a set of user interactions (as JCas objects)
	 * @param confidence -- a score for filtering edges
	 * 
	 * @return a collapsed entailment graph
	 * @throws IOException 
	 * @throws LAPException 
	 * @throws FragmentAnnotatorException 
	 * @throws ModifierAnnotatorException 
	 */
	
	public EntailmentGraphCollapsed buildCollapsedGraph(List<JCas> docs, double confidence) 
			throws CollapsedGraphGeneratorException, GraphMergerException, FragmentGraphGeneratorException, LAPException, FragmentAnnotatorException, ModifierAnnotatorException, IOException{
			
		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs),confidence);		
	}


	protected void annotateCAS(JCas aJCas) throws FragmentAnnotatorException, ModifierAnnotatorException {
		fragAnot.annotateFragments(aJCas);
		modAnot.annotateModifiers(aJCas);
	}
}

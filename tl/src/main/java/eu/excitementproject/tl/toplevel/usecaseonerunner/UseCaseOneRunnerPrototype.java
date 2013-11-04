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

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.api.GraphOptimizer;
import eu.excitementproject.tl.composition.api.GraphMerger;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.graphmerger.AutomateWP2ProcedureGraphMerger;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.decomposition.api.FragmentAnnotator;
import eu.excitementproject.tl.decomposition.api.FragmentGraphGenerator;
import eu.excitementproject.tl.decomposition.api.ModifierAnnotator;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.decomposition.fragmentannotator.SentenceAsFragmentAnnotator;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphLiteGeneratorFromCAS;
import eu.excitementproject.tl.decomposition.modifierannotator.AdvAsModifierAnnotator;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphException;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.toplevel.api.UseCaseOneRunner;

@SuppressWarnings("unused")
public class UseCaseOneRunnerPrototype implements UseCaseOneRunner {
	
	LAPAccess lap = null;
	EDABasic<?> eda = null;
	
	FragmentAnnotator fragAnot;
	ModifierAnnotator modAnot;
	FragmentGraphGenerator fragGen;
	GraphMerger graphMerger;
	GraphOptimizer collapseGraph;
		
	private final static Logger logger = Logger.getLogger(UseCaseOneRunnerPrototype.class.getName());
	
	// output path used for outputting graphs to files
	private String outputPath = ".";
	
	public UseCaseOneRunnerPrototype(LAPAccess lap, EDABasic<?> eda) throws FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, IOException{
		this.lap = lap;
		this.eda = eda;

		initInterfaces();
	}
	
	
	public UseCaseOneRunnerPrototype(LAPAccess lap, EDABasic<?> eda, String outputPath) throws FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException, IOException{
		this.lap = lap;
		this.eda = eda;
		this.outputPath = outputPath;

		initInterfaces();
	}
	
	public UseCaseOneRunnerPrototype() throws FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException {
		initInterfaces();
	}
	
	
	private void initInterfaces() throws FragmentAnnotatorException, ModifierAnnotatorException, GraphMergerException {
		fragAnot = new SentenceAsFragmentAnnotator(lap);
		modAnot = new AdvAsModifierAnnotator(lap); 		

		fragGen = new FragmentGraphGeneratorFromCAS();
//		fragGen = new FragmentGraphLiteGeneratorFromCAS();
		graphMerger = new AutomateWP2ProcedureGraphMerger(lap, eda);
		collapseGraph = new SimpleGraphOptimizer();
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
			logger.info("Adding fragment graphs for text: " + aJCas.getDocumentText());
			fgs.addAll(fragGen.generateFragmentGraphs(aJCas));
		}
		
		inspectGraph(fgs);
				
		return graphMerger.mergeGraphs(fgs, new EntailmentGraphRaw());
	}
	
/*	
	private void log(String message) throws IOException{
		logger.info(message);
		if (outputPath != null){
			BufferedWriter logfile = new BufferedWriter(new FileWriter(outputPath+"/log.txt",true)); //open log file for append
			logfile.write(message+"\n");
			logfile.close();			
		}
	}
*/	
	
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
			throws GraphOptimizerException{
		
		try {
			return collapseGraph.optimizeGraph(new EntailmentGraphRaw(f));
		} catch (EntailmentGraphRawException e) {
			// TODO Auto-generated catch block
			throw new GraphOptimizerException(e.getMessage());
		}
	}
	

 	/**
	 * @param f -- a file containing a serialized raw entailment graph
	 * 
	 * @return the collapsed entailment graph corresponding to the raw graph in the input file
 	 * @throws EntailmentGraphRawException 
	 */
	
	public EntailmentGraphCollapsed buildGraph(File f, double confidence) 
			throws GraphOptimizerException, EntailmentGraphRawException{
			
		return collapseGraph.optimizeGraph(new EntailmentGraphRaw(f),confidence);		
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
				throws GraphOptimizerException, GraphMergerException, FragmentGraphGeneratorException, LAPException, FragmentAnnotatorException, ModifierAnnotatorException, IOException, EntailmentGraphRawException {
		
		EntailmentGraphRaw rawGraph = buildRawGraph(docs);
		inspectGraph(rawGraph);
		
//		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs));
		return collapseGraph.optimizeGraph(rawGraph);
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
				throws GraphOptimizerException, GraphMergerException, FragmentGraphGeneratorException, FragmentAnnotatorException, ModifierAnnotatorException, LAPException, IOException, EntailmentGraphRawException {
		
		EntailmentGraphRaw rawGraph = buildRawGraph(docs);
		inspectGraph(rawGraph);
		
//		return collapseGraph.generateCollapsedGraph(buildRawGraph(docs));
		return collapseGraph.optimizeGraph(rawGraph);
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
			throws GraphOptimizerException, GraphMergerException, FragmentGraphGeneratorException, LAPException, FragmentAnnotatorException, ModifierAnnotatorException, IOException{

		return collapseGraph.optimizeGraph(buildRawGraph(docs),confidence);		
	}


	
	public void inspectGraph(EntailmentGraphCollapsed graph) throws IOException, EntailmentGraphCollapsedException{

		if (graph != null) { 
			logger.info("COLLAPSED GRAPH:\n" + graph.toString());
			if (outputPath != null) {
				graph.toDOT(outputPath+"/collapsed_graph.dot.txt");
				graph.toXML(outputPath+"/collapsed_graph.xml");
			}
		} else {
			logger.info("The collapsed graph is null");
		}
	}

	
	public void inspectGraph(EntailmentGraphRaw graph) throws IOException, EntailmentGraphRawException{

		if (graph != null) { 
			logger.info("RAW GRAPH:\n" + graph.toString());			
			if (outputPath != null) {
				graph.toDOT(outputPath+"/raw_graph.dot.txt");
				graph.toXML(outputPath+"/raw_graph.xml");
			}
		} else {
			logger.info("The raw graph is null");
		}		
	}
	
	
	public void inspectGraph(Set<FragmentGraph> fgs) throws IOException{
		
		logger.info("Inspecting the fragmentGraphs:");
		int i = 1;
		
		if (fgs != null) {
			logger.info("\t" + fgs.size() + " graphs to check");
			
			for(FragmentGraph fg : fgs) {
				if ( fg != null ) {
					logger.info(fg.toString());
					if (outputPath != null) {
						fg.toDOT(outputPath+"/fragment_graph_" + i + ".dot.txt");
						try {
							fg.toXML(outputPath+"/fragment_graph_" + i + ".xml");
						} catch (FragmentGraphException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						i++;
					}
				} else {
					logger.info("Fragment graph is null");
				}
			}
		} else {
			logger.info("The set of fragment graphs is null");
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
			throws GraphOptimizerException, GraphMergerException, FragmentGraphGeneratorException, LAPException, FragmentAnnotatorException, ModifierAnnotatorException, IOException{
			
		return collapseGraph.optimizeGraph(buildRawGraph(docs),confidence);		
	}


	protected void annotateCAS(JCas aJCas) throws FragmentAnnotatorException, ModifierAnnotatorException {
		fragAnot.annotateFragments(aJCas);
		modAnot.annotateModifiers(aJCas);
	}
}


package eu.excitementproject.tl.structures.fragmentgraph;


import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.uima.jcas.JCas;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import org.uimafit.util.JCasUtil;

import eu.excitement.type.tl.CategoryAnnotation;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitement.type.tl.FragmentPart;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.toplevel.usecaseonerunner.UseCaseOneRunnerPrototype;

/**
 * 
 * @author vivi@fbk
 *
 *	Graph structure for a text fragment.
 *	We assume a text fragment is composed of a base statement (BS) plus a number of modifiers (M).
 *  A node of this graph will correspond to BS + M_1 ... M_k
 *  We assume a textual entailment (TE) relation between every two statements (S_i, S_j) that differ only
 *  by one modifier: S_i = S_j + M_x => S_i -TE-> S_j
 *  
 *  This class extends the DefaultDirectedWeightedGraph class, because the graph is directed
 *  and we might decide to have the edges weighted. Currently they are not.
 *  
 *  JavaDoc for DefaultDirectedWeightedGraph class for information about inherited methods:
 *  http://jgrapht.org/javadoc/org/jgrapht/graph/DefaultDirectedWeightedGraph.html
 *
 * @param <V> Vertex class
 * @param <E> Edge class
 */
public class FragmentGraph extends DefaultDirectedWeightedGraph<EntailmentUnitMention,FragmentGraphEdge> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4631493969220124299L;
	
	private final static Logger logger = Logger.getLogger(FragmentGraph.class.getName());
	
	/*
	 * apart from the graph's structure, we might benefit from keeping track 
	 * of the base statements (what WP2 calls "base predicates", but that confused us
	 * so I renamed them to what we called them in our meeting)
	 */
	EntailmentUnitMention baseStatement;
	EntailmentUnitMention topStatement = null;

	/**
	 * a CAS object that holds contextual (and structural) information for the text fragment
	 * 
	 * Contextual information covers the document where the fragment comes from, position of
	 * the fragment in the document, etc.
	 * 
	 * Structural information covers tokenization, POS, NEs, parse tree if available, etc.
	 */
	JCas document = null;
	FragmentAnnotation fragment = null;
	
	int depth = -1;
	
	/**
	 * Default constructor
	 * 
	 * @param arg0 -- edge factory for the graph
	 */
	public FragmentGraph(EdgeFactory<EntailmentUnitMention,FragmentGraphEdge> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Default constructor 
	 * 
	 * @param edgeClass -- class of the graph's edges (FragmentGraphEdge)
	 */
	public FragmentGraph(Class<? extends FragmentGraphEdge> edgeClass) {
		super(edgeClass);
	}
	
	public FragmentGraph(String text, Set<String> modifiers) {
		this(FragmentGraphEdge.class);
		baseStatement = new EntailmentUnitMention(text, new HashSet<String>(), modifiers);
		topStatement = new EntailmentUnitMention(text, null, null);
		buildGraph(text, modifiers, modifiers, null);
	}
	

	/**
	 * Build a fragment graph from a (determined) fragment in a CAS object corresponding to a document,
	 * based on the modifier annotations in the fragment.
	 * 
	 * This will build a graph where:
	 * -- each node is the fragment text, minus a subset of modifiers
	 * (NOTE: the modifier combinations kept should be valid! i.e., we cannot have a modifier A
	 *  that depends on another modifier B, but not B)
	 * -- there is an edge between two nodes A and B (direction: A->B), where the set of modifiers in node B M_B = M_A \cup {M_i} 
	 * 
	 * @param aJCas -- CAS object containing annotations for a document
	 * @param f -- the fragment annotation from which to produce a {@link} FragmentGraph 
	 */
	public FragmentGraph(JCas aJCas, FragmentAnnotation frag) {
		this(new ClassBasedEdgeFactory<EntailmentUnitMention, FragmentGraphEdge>(FragmentGraphEdge.class));
		
		document = aJCas;
		fragment = frag;
		baseStatement = new EntailmentUnitMention(aJCas, frag, new HashSet<ModifierAnnotation>());
		topStatement = new EntailmentUnitMention(aJCas, frag, FragmentGraph.getFragmentModifiers(aJCas, frag));
		
		Set<ModifierAnnotation> mods = getFragmentModifiers(aJCas,frag);		
		buildGraph(aJCas, frag, mods, null);
	}
	
	
	/**
	 * 
	 * @param text -- a text fragment
	 * @param modifiers -- set of string modifiers in the text
	 * @param parent -- the parent node for the one that is built in the first step 
	 * 					(has one extra modifier compared to the node that is being built)
	 */
	private void buildGraph(String text, Set<String> modifiers, Set<String> allModifiers, EntailmentUnitMention parent) {
		EntailmentUnitMention eum = new EntailmentUnitMention(text, modifiers, allModifiers);
		
		if (! this.containsVertex(eum)) { // double check that this test does what it should
			addVertex(eum);			
		} else {
			eum = this.getVertex(eum);
		}

		if (parent != null) {
			this.addEdge(parent, eum); // double check the direction of the added edges
		}

		Set<String> sma;
		for(String m: modifiers) {
			sma = new HashSet<String>(modifiers);
			sma.remove(m);
			buildGraph(text, sma, allModifiers, eum);
		}
	}
	

	/**
	 * start with the top node that has all modifiers, remove them one by one 
	 * and recursively build the graph
	 * 
	 * @param aJCas -- document CAS object
	 * @param frag -- (determined) fragment
	 * @param mods -- set of modifiers
	 * @param parent -- parent node (that has one extra modifier compared to the current node)
	 */
	private void buildGraph(JCas aJCas, FragmentAnnotation frag, Set<ModifierAnnotation> modifiers, EntailmentUnitMention parent) {
		
		EntailmentUnitMention eum = new EntailmentUnitMention(aJCas, frag, modifiers);
		
		logger.info("Generated node (EUM) for string: " + eum.getText());
//		eum = addNode(eum);
		if (! this.containsVertex(eum)) { // double check that this test does what it should
			addVertex(eum);
			logger.info("Vertex added: " + eum.text);
		} else {
			eum = getVertex(eum);
			logger.info("Matching vertex retrieved from graph: " + eum.text);
		}
		
		if (parent != null) {
			this.addEdge(parent, eum); // double check the direction of the added edges
		}

		Set<ModifierAnnotation> sma;
		for(ModifierAnnotation m: modifiers) {
			sma = new HashSet<ModifierAnnotation>(modifiers);
			sma.remove(m);
			if (consistentModifiers(sma)) {
				buildGraph(aJCas, frag, sma, eum);
			}
		}
	}
	
	/**
	 * Checks if a set of modifiers is consistent, i.e. -- it doesn't miss a modifier that another depends on
	 * (example: Seats are uncomfortable as too old. 
	 * 				=> Seats are uncomfortable as old (OK)
	 * 				=> Seats are uncomfortable as too (not OK)	
	 * 
	 * @param sma -- a set of modifier annotations from the document CAS
	 * @return -- true if the set of modifiers given is consistent
	 */
	private boolean consistentModifiers(Set<ModifierAnnotation> sma) {
		
		for(ModifierAnnotation m: sma) {
			ModifierAnnotation m_dp = m.getDependsOn();
			if (m_dp != null && ! sma.contains(m_dp)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gather a (determined) fragment's modifiers
	 * 
	 * @param aJCas -- a CAS object
	 * @param f -- a fragment annotation 
	 * @return -- the set of modifiers (as a set of modifier annotations from the CAS) contained in the fragment f
	 */
	public static Set<ModifierAnnotation> getFragmentModifiers(JCas aJCas,
			FragmentAnnotation f) {
		Set<ModifierAnnotation> mas = new HashSet<ModifierAnnotation>();
		FragmentPart fp;
		for(int i = 0; i < f.getFragParts().size(); i++) {
			fp = f.getFragParts(i);
			mas.addAll(JCasUtil.selectCovered(aJCas, ModifierAnnotation.class, fp.getBegin(), fp.getEnd()));
		}
		return mas;
	}

	/**
	 * Gather a (determined) fragment's category annotations
	 * 
	 * @param aJCas -- a CAS object
	 * @param f -- a fragment annotation
	 * @return the set of category annotations that cover fragment f
	 */
	public static Set<CategoryAnnotation> getFragmentCategories(JCas aJCas, FragmentAnnotation f) {
		Set<CategoryAnnotation> cas = new HashSet<CategoryAnnotation>();
		cas.addAll(JCasUtil.selectCovering(aJCas, CategoryAnnotation.class, f.getBegin(), f.getEnd()));
		return cas;
	}
	
	
	/**
	 * Return the FragmentGraph node that corresponds to the given argument, 
	 * or this argument if no such node exists in the graph (to avoid adding duplicate nodes)
	 * 
	 * @param eum -- an entailment unit mention
	 * @return -- the node in this fragment graph that equals the given entailment unit mention,
	 *            or the given entailment unit mention if no equal node exists
	 */
	private EntailmentUnitMention getVertex(EntailmentUnitMention eum) {
		for(EntailmentUnitMention e: this.vertexSet()) {
			if (eum.equals(e)) {
				return e;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private EntailmentUnitMention getVertex(String eumText) {
		for(EntailmentUnitMention e: this.vertexSet()) {
			if (e.getText().matches(eumText)) {
				return e;
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 * @return the base statements of the fragment graph (useful for merging methods) -- for compatibility upwards (with WorkGraph)
	 */
	public EntailmentUnitMention getBaseStatement(){
		return baseStatement;
	}

	/**
	 * 
	 * @return the text fragment for which this fragment graph was built
	 */
	public EntailmentUnitMention getCompleteStatement(){
		if (topStatement == null) {
		   topStatement = (EntailmentUnitMention) getNodes(getMaxLevel()).toArray()[0];
		}
		return topStatement;
	}
	
	/**
	 * 
	 * @return the maximum depth of the graph (equivalent to the number of modifiers in the fragment
	 */
	public int getMaxLevel() {
		
		if (depth < 0) {
			for(EntailmentUnitMention eum: this.vertexSet()) {
				if (eum.getLevel() > depth) {
					depth = eum.getLevel();
				}
			}
		}	
				
		return depth;
	}

	@Override
	public boolean containsVertex(EntailmentUnitMention eum) {
		for(EntailmentUnitMention e: this.vertexSet()) {
			if (eum.equals(e)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 
	 * @param level -- the number of modifiers desired
	 * @return -- the nodes that have "level" number of modifiers (are at distance "level" from the root, aka the base statement)
	 */
	public Set<EntailmentUnitMention> getNodes(int level) {
		Set<EntailmentUnitMention> nodes = new HashSet<EntailmentUnitMention>();
		
		for(EntailmentUnitMention v: this.vertexSet()) {
			if (((EntailmentUnitMention) v).getLevel() == level) {
				nodes.add(v);
			}
		}
		return nodes;
	}

	
	public String getInteractionId(){
		if (document == null) {
			return "N/A";
		}
		
		return CASUtils.getTLMetaData(document).getInteractionId();
	}
	
	
	@Override
	public String toString() {
		String str = "\nFragment graph: \n Interaction id = " + getInteractionId() + "\n";
		for(EntailmentUnitMention v : this.vertexSet()) {
			str += "vertex: " + v.toString() + " ( level = " + v.getLevel() + ")\n";
			for(EntailmentUnitMention x: this.vertexSet()) {
				if (this.containsEdge(v, x))
				str += "\t--entails-->   vertex: " + x.toString() + "\n";
			}
		}
		return str;
	}	

	/**
	 * 
	 * @return a sample set of fragment graphs, for testing purposes
	 */
	public static Set<FragmentGraph> getSampleOutput() {
		Set<FragmentGraph> fgs  = new HashSet<FragmentGraph>();
		
		String text = "Food was really bad";
		Set<String> modifs = new HashSet<String>();
		modifs.add("really");
		fgs.add(new FragmentGraph(text,modifs));
		
		text = "I didn't like the food";
		modifs.clear();
		fgs.add(new FragmentGraph(text,modifs));
		
		text = "a little more leg room would have been perfect";
		modifs.clear();
		modifs.add("a little");
		fgs.add(new FragmentGraph(text,modifs));

		text = "Disappointed with the amount of legroom compared with other trains";
		modifs.clear();
		modifs.add("amount of");
		modifs.add("compared with other trains");
		fgs.add(new FragmentGraph(text,modifs));
		
		return fgs;
	}
	
	
	/**
	 * 
	 * @return one sample fragment graph, for testing purposes
	 */
    public static FragmentGraph getSampleGraph() {

		String text = "The hard old seats were very uncomfortable";
		Set<String> modifiers = new HashSet<String>();
		modifiers.add("hard");
		modifiers.add("old");
		modifiers.add("very");
		FragmentGraph g = new FragmentGraph(text,modifiers);
		
		System.out.println("fragment graph: " + g.toString());
		
		return g;
    }

    
    
	public static void main(String [] argv) {
			String text = "The hard old seats were very uncomfortable";
			Set<String> modifiers = new HashSet<String>();
			modifiers.add("hard");
			modifiers.add("old");
			modifiers.add("very");
			FragmentGraph g = new FragmentGraph(text,modifiers);
					
			System.out.println("Graph: \\" + g.toString());
	}
	
	/* This method was added by Lili on May, 20 
	 * to allow retrieving the source and the target of an edge using its corresponding getters)*/
	@Override
	public FragmentGraphEdge addEdge(EntailmentUnitMention parent, EntailmentUnitMention eum){
//		return super.addEdge(parent, eum );
		FragmentGraphEdge edge = new FragmentGraphEdge(parent, eum);
		
		checkVertex(eum);
		checkVertex(parent);
		
		this.addEdge(parent, eum, edge);
		logger.info("Added edge between: \n\t" + eum.getText() + "\n\t" + parent.getText());
		return edge;
	}

	private void checkVertex(EntailmentUnitMention eum) {
		if (! this.containsVertex(eum)) {
			logger.info("vertex does not exist in the graph: " + eum.text);
//			eum = addNode(eum);
		}
	}
	
	public EntailmentUnitMention addNode(EntailmentUnitMention eum) {
		if (! this.containsVertex(eum)) { // double check that this test does what it should
			addVertex(eum);
			logger.info("Vertex added: " + eum.text);
		} else {
			eum = getVertex(eum);
			logger.info("Matching vertex retrieved from graph: " + eum.text);
		}
		return eum;
	}

}

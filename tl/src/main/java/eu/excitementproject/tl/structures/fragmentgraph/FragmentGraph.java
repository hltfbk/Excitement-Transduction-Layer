package eu.excitementproject.tl.structures.fragmentgraph;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jgrapht.graph.DirectedMultigraph;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.uimafit.util.JCasUtil;

import eu.excitement.type.tl.CategoryAnnotation;
import eu.excitement.type.tl.FragmentAnnotation;
import eu.excitement.type.tl.FragmentPart;
import eu.excitement.type.tl.ModifierAnnotation;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.decomposition.fragmentgraphgenerator.FragmentGraphGeneratorFromCAS;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.laputils.RegionUtils;
import eu.excitementproject.tl.laputils.CASUtils.Region;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

/**
 * 
 * @author vivi@fbk & Lili Kotlerman
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
@SuppressWarnings("unused")
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
	 * start with the top node that has all modifiers, remove them one by one 
	 * and recursively build the graph
	 * 
	 * @param aJCas -- document CAS object
	 * @param frag -- (determined) fragment
	 * @param mods -- set of modifiers
	 * @param parent -- parent node (that has one extra modifier compared to the current node)
	 */
	protected void buildGraph(JCas aJCas, FragmentAnnotation frag, Set<ModifierAnnotation> modifiers, EntailmentUnitMention parent) {
		
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
		
		printAnnotations(sma);
		
		for(ModifierAnnotation m: sma) {
			ModifierAnnotation m_dp = m.getDependsOn();
			if (m_dp != null && ! sma.contains(m_dp)) {
				System.out.println("Modifiers not consistent");
				return false;
			}
		}
		System.out.println("Modifiers OK");
		return true;
	}

	// to check the modifier set
	protected void printAnnotations(Set<ModifierAnnotation> sma) {

		for (Annotation a: sma) {
			System.out.println("\t" + a.getCoveredText() + " / " + a.getClass());
		}
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
		Set<Region> fragmentRegions = new HashSet<Region>();
		FragmentPart fp;
		for(int i = 0; i < f.getFragParts().size(); i++) {
			fp = f.getFragParts(i);
			logger.info("Processing fragment part " + fp.getCoveredText());
			fragmentRegions.add(new Region(fp.getBegin(), fp.getEnd()));
//			mas.addAll(JCasUtil.selectCovered(aJCas, ModifierAnnotation.class, fp.getBegin(), fp.getEnd()));
		}
		
		for(Region r: RegionUtils.compressRegions(fragmentRegions)) {
			
			System.out.println("Fragment region: " + r.getBegin()  + " -- " + r.getEnd());
			mas.addAll(JCasUtil.selectCovered(aJCas, ModifierAnnotation.class, r.getBegin(), r.getEnd()));
		}
		
		return mas;
	}

	
	/**
	 * build maximal (span-wise) contiguous regions for annotated fragments
	 * this is to avoid missing modifiers that have a longer span than the fragment parts 
	 * which now (Feb 2014) consist of tokens
	 * 
	 * 
	 * @param contiguousRegions
	 * @param begin
	 * @param end
	 */
	private static void addRegion(Set<Region> contiguousRegions, int begin,
			int end) {

		if (contiguousRegions.isEmpty()) {
			contiguousRegions.add(new Region(begin, end));
		} else {
		
			Set<Region> newRegions = new HashSet<Region>();
			for(Region r: contiguousRegions) {
				
			}
		}
	}
	
	
	/**
	 * Gather a (determined) fragment's category annotations
	 * 
	 * @param aJCas -- a CAS object
	 * @param f -- a fragment annotation
	 * @return the set of category annotations that cover fragment f
	 */
/*	public static Set<CategoryAnnotation> getFragmentCategories(JCas aJCas, FragmentAnnotation f) {
		Set<CategoryAnnotation> cas = new HashSet<CategoryAnnotation>();
		cas.addAll(JCasUtil.selectCovering(aJCas, CategoryAnnotation.class, f.getBegin(), f.getEnd()));
		return cas;
	}
*/	
	

	/**
	 * Return the FragmentGraph node that corresponds to the given argument, 
	 * or this argument if no such node exists in the graph (to avoid adding duplicate nodes)
	 * 
	 * @param eum -- an entailment unit mention
	 * @return -- the node in this fragment graph that equals the given entailment unit mention,
	 *            or the given entailment unit mention if no equal node exists
	 */
	protected EntailmentUnitMention getVertex(EntailmentUnitMention eum) {
		for(EntailmentUnitMention e: this.vertexSet()) {
			if (eum.equals(e)) {
				return e;
			}
		}
		return null;
	}
	
	
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
	 * @throws LAPException 
	 */
	public static Set<FragmentGraph> getSampleOutput() {
		Set<FragmentGraph> fgs  = new HashSet<FragmentGraph>();

		try {
			JCas aJCas = CASUtils.createNewInputCas(); 
//			File f = new File("./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1/107379.txt.xmi");
			// replaced by Lili, 12.8.2014
			File f = new File("./src/test/resources/WP2_public_data_CAS_XMI/NICE_open/all/15.txt.xmi"); 

			// initiate the FragGraphGenerator... 
			FragmentGraphGeneratorFromCAS fragGen = new FragmentGraphGeneratorFromCAS(); 

			// Read in inputCASes for the examples, and generate the FragmentGraphs 
			CASUtils.deserializeFromXmi(aJCas, f); 
			fgs = fragGen.generateFragmentGraphs(aJCas);
			
		} catch (Exception e) {
			logger.info("Problems generating same fragment graphs from CAS");
			e.printStackTrace();
		}
		
		return fgs;
	}
	
	
	/**
	 * 
	 * @return one sample fragment graph, for testing purposes
	 */
    public static FragmentGraph getSampleGraph() {

    	for(FragmentGraph g : getSampleOutput()){				
  //  		System.out.println("fragment graph: " + g.toString());
    		return g;
    	}
		return null;
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

	
	/** Saves the graph in DOT format to the given file. If such file already exists, it will be overwritten.
	 * @param filename - the name of the file to save the graph
	 * @throws EntailmentGraphRawException if the method did not manage to save the graph (e.g. if the folder specified in the filename does not exist)
	 */
	public void toDOT(String filename) throws IOException{
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write(this.toDOT());
			out.close();
	}	
	
	/** Generates a single string, which contains the graph in DOT format for visualization
	 * @return the generated string
	 */
	public String toDOT(){
		String s = "digraph rawGraph {\n";
		for (FragmentGraphEdge edge : this.edgeSet()){
			s+=edge.toDOT();
		}
		s+="}";	
		return s;
	}
	
	/**
	 * Outputs the fragment graph in XML format
	 */
	public void toXML(String filename) throws FragmentGraphException{
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("rawGraph");
			doc.appendChild(rootElement);

			// add nodes
			for (EntailmentUnitMention eu : this.vertexSet()){
				// EntailmentUnit elements
				Element entailmentUnitNode = doc.createElement("entailmentUnitMentionNode");
				rootElement.appendChild(entailmentUnitNode);

				// set text attribute to eu element
				entailmentUnitNode.setAttribute("text",eu.getText());
				// set level attribute to eu element
				entailmentUnitNode.setAttribute("level",String.valueOf(eu.getLevel()));
				entailmentUnitNode.setAttribute("categoryId",eu.getCategoryId());

				Element completeStatementText = doc.createElement("completeStatement");
				completeStatementText.setAttribute("text",eu.getTextWithoutDoubleSpaces());
				entailmentUnitNode.appendChild(completeStatementText);		

				Element interaction = doc.createElement("interactionId");
				interaction.setAttribute("id",this.getInteractionId());
				entailmentUnitNode.appendChild(interaction);						
			}

			// add edges
			for (FragmentGraphEdge r  : this.edgeSet()){
				// staff elements
				Element entailmentrelationEdge = doc.createElement("entailmentRelationEdge");
				rootElement.appendChild(entailmentrelationEdge);

				// set source attribute to eu element
				entailmentrelationEdge.setAttribute("source",r.getSource().getText());
				// set target attribute to eu element
				entailmentrelationEdge.setAttribute("target",r.getTarget().getText());
				// set confidence attribute to eu element
				entailmentrelationEdge.setAttribute("confidence",String.valueOf(r.getWeight()));
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			File f = new File(filename);
			StreamResult result = new StreamResult(f.toURI().getPath());

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);
		} catch (DOMException | ParserConfigurationException | TransformerException e) {
			throw new FragmentGraphException(e.getMessage());
			// TODO Auto-generated catch block
		}		 
  }
	
	/**
	 * Comparator to sort fragment graphs by their complete statement texts 
	 */
	public static class CompleteStatementComparator implements Comparator<FragmentGraph> {
	    @Override
	    public int compare(FragmentGraph gA, FragmentGraph gB) {
	    	return gA.getCompleteStatement().getText().compareTo(gB.getCompleteStatement().getText());
	    }
	}
	
	/******************************************************************************************
	 * TRANSITIVE CLOSURE
	 * ****************************************************************************************/
	   /**
     * Computes floor(log_2(n)) + 1
     */
    private int computeBinaryLog(int n)
    {
        assert n >= 0;

        int result = 0;
        while (n > 0) {
            n >>= 1;
            ++result;
        }

        return result;
    }
    
    /**
	 *  Adds transitive closure edges to the graph.
	 *  Based on org.jgrapht.alg.TransitiveClosure
	 */
	public void applyTransitiveClosure(){    
		Map<EntailmentUnitMention,Double> newEdgeTargets = new HashMap<EntailmentUnitMention,Double>();

        // At every iteration of the outer loop, we add a path of length 1
        // between nodes that originally had a path of length 2. In the worst
        // case, we need to make floor(log |V|) + 1 iterations. We stop earlier
        // if there is no change to the output graph.

        int bound = computeBinaryLog(this.vertexSet().size());
        boolean done = false;
        for (int i = 0; !done && (i < bound); ++i) {
            done = true;
            for (EntailmentUnitMention v1 : this.vertexSet()) {
                newEdgeTargets.clear();

                for (EntailmentUnitMention v2 : this.getEntailedNodes(v1)) {
                	Double weight = this.getEdge(v1, v2).getWeight();
                    for (EntailmentUnitMention v3 : this.getEntailedNodes(v2)) {

                        // Assign min confidence of the 2 edges as the confidence of the transitive edge
                        if (this.getEdge(v2, v3).getWeight() < weight) weight=this.getEdge(v2, v3).getWeight();

                        if (v1.equals(v3)) {
                            // Don't add self loops.
                            continue;
                        }

                        if (this.getEdge(v1, v3) != null) {
                        	// Already have such edge
                        	continue; 
                        }
                                      
                        newEdgeTargets.put(v3,weight);
                        done = false;
                    }
                }

                for (EntailmentUnitMention v3 : newEdgeTargets.keySet()) {
                	this.addEdge(v1, v3);
                }
            }
        }
	}

	
	/** Returns the set of nodes, entailed by the given node
	 * @param node whose entailed nodes are returned
	 * @return Set<EntailmentUnitMention> with all the entailed nodes of the given node
	 */
	public Set<EntailmentUnitMention> getEntailedNodes(EntailmentUnitMention node){
		if (!this.containsVertex(node)) return null;

		Set<EntailmentUnitMention> entailedNodes = new HashSet<EntailmentUnitMention>();
		for (FragmentGraphEdge edge : this.outgoingEdgesOf(node)){
			entailedNodes.add(edge.getTarget());
		}
		return entailedNodes;
	}
}

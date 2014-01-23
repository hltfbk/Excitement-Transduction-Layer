package eu.excitementproject.tl.structures.fragmentgraph;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.apache.log4j.Logger;
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
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;

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
public class FragmentGraphNoNeg extends FragmentGraph {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4631493969220124299L;
	
	private final static Logger logger = Logger.getLogger(FragmentGraphNoNeg.class.getName());
	
	private Set<ModifierAnnotation> modifiers;
	private int negationPos = -1;

	private String negationPattern = "(no|non|not|n't|kein|nein|nessun|nessuna|nessuno)";
	
	/**
	 * Default constructor
	 * 
	 * @param arg0 -- edge factory for the graph
	 */
	public FragmentGraphNoNeg(EdgeFactory<EntailmentUnitMention,FragmentGraphEdge> arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Default constructor 
	 * 
	 * @param edgeClass -- class of the graph's edges (FragmentGraphEdge)
	 */
	public FragmentGraphNoNeg(Class<? extends FragmentGraphEdge> edgeClass) {
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
	public FragmentGraphNoNeg(JCas aJCas, FragmentAnnotation frag) {

		this(new ClassBasedEdgeFactory<EntailmentUnitMention, FragmentGraphEdge>(FragmentGraphEdge.class));
		
		document = aJCas;
		fragment = frag;

		checkNegation();
		modifiers = getNotNegatedFragmentModifiers(aJCas,frag);		
		
		
		baseStatement = new EntailmentUnitMention(aJCas, frag, new HashSet<ModifierAnnotation>());
		
		logger.info("\tBASE statement added: " + baseStatement.getText());
		
		topStatement = new EntailmentUnitMention(aJCas, frag, modifiers);
		
		logger.info("\tTOP statement added: " + topStatement.getText());
		
		buildGraph(aJCas, frag, modifiers, null);
	}
		
	
	
	/**
	 * Gather a (determined) fragment's modifiers, that are not negations or within a negation's scope
	 * 
	 * @param aJCas -- a CAS object
	 * @param f -- a fragment annotation 
	 * @return -- the set of modifiers (as a set of modifier annotations from the CAS) contained in the fragment f
	 */
	public Set<ModifierAnnotation> getNotNegatedFragmentModifiers(JCas aJCas,
			FragmentAnnotation f) {
		Set<ModifierAnnotation> mas = new HashSet<ModifierAnnotation>();
		FragmentPart fp;
		for(int i = 0; i < f.getFragParts().size(); i++) {
			fp = f.getFragParts(i);
			logger.info("Processing fragment part " + fp.getCoveredText());
//			mas.addAll(JCasUtil.selectCovered(aJCas, ModifierAnnotation.class, fp.getBegin(), fp.getEnd()));
			for(ModifierAnnotation m: JCasUtil.selectCovered(aJCas, ModifierAnnotation.class, fp.getBegin(), fp.getEnd())) {
				if (! isNegation(m) && ! inNegationScope(m, fp, f)) {
					mas.add(m);
				}
			}
		}
		return mas;
	}

	
	/**
	 * Check if the modifier annotation is a negation
	 * 
	 * @param m - a modifier annotation
	 * @return
	 */
	private boolean isNegation(ModifierAnnotation m) {
		return (m.getCoveredText().toLowerCase().matches(negationPattern));
	}

	/**
	 * Checks if a modifier for a given fragment is fine with respect to the negation (if there is one)
	 * i.e. -- neither the negation nor anything in its scope (here we consider everything coming after it as the scope)
	 *         is removed 
	 * 
	 * (example: "not enough seating" -- neither "not", nor "enough" can be removed)	
	 * 
	 * @param m -- a set of modifier annotations from the document CAS
	 * @return -- true if the modifier given is OK with respect to the negation
	 */
	private boolean inNegationScope(ModifierAnnotation m, FragmentPart fp, FragmentAnnotation f) {

		// we could check here only relative to the fragment part, but maybe that's not good enough
		if (m.getBegin() - f.getBegin() >= negationPos) {
			logger.info("Modifier <" + m.getCoveredText() + "> is in negation scope and will not be removed");
			return true;
		}
		
		return false;
	}

	/** 
	 * Checks if the current fragment contains negations, and sets the negationPosition attribute
	 */
	private void checkNegation() {
		
		String text = fragment.getCoveredText();
		logger.info("Checking for negation in:\n\t" + text);
		
		Pattern p = Pattern.compile("\\b" + negationPattern + "\\b",Pattern.CASE_INSENSITIVE);
//		Pattern p = Pattern.compile("\\snot\\s",Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(text);

		if (m.find()) {
			
			negationPos = text.indexOf(m.group(1));
			logger.info("\tNEGATION FOUND at position " + negationPos);
			
//			System.exit(1);
		}
		
	}

	
}

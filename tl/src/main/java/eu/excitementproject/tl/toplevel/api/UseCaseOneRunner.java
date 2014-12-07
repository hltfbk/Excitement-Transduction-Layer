package eu.excitementproject.tl.toplevel.api;

import java.io.File;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.structures.Interaction;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

/**
 * Interface for use case one -- annotate fragments and modifiers, build fragment graphs, combine fragment graphs using an EDA, collapse the final graph
 * 
 * @author Lili Kotlerman and Vivi Nastase
 *
 */
public abstract interface UseCaseOneRunner {
	
	
	/**
	 * Build a raw graph from the given set of interactions (makes annotations first, then builds fragment graphs, then merges them) 
	 * 
	 * @param docs -- a set of Interaction objects
	 * @return the raw entailment graph
	 * 
	 * @throws GraphMergerException
	 * @throws FragmentGraphGeneratorException
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws IOException
	 */
	EntailmentGraphRaw buildRawGraph(Set<Interaction> docs)
			throws GraphMergerException, FragmentGraphGeneratorException,
			LAPException, FragmentAnnotatorException,
			ModifierAnnotatorException, IOException;


	/**
	 * Build a raw graph from the given list of CAS objects (makes annotations first, then builds fragment graphs, then merges them)
	 * 
	 * @param docs -- a list of CAS objects
	 * @return the raw entailment graph 
	 * 
	 * @throws GraphMergerException
	 * @throws FragmentGraphGeneratorException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws LAPException
	 * @throws IOException
	 */
	EntailmentGraphRaw buildRawGraph(List<JCas> docs)
			throws GraphMergerException, FragmentGraphGeneratorException,
			FragmentAnnotatorException, ModifierAnnotatorException, LAPException, IOException;


	/**
	 * Builds a collapsed graph from a raw entailment graph that it reads from a file
	 * 
	 * @param f -- file containing a raw entailment graph
	 * @return the collapsed entailment graph
	 * 
	 * @throws GraphOptimizerException
	 */
	EntailmentGraphCollapsed buildCollapsedGraph(File f)
			throws GraphOptimizerException;


	/**
	 * Builds a collapsed graph from the given set of interaction documents. It will build a raw graph first, and then collapse it.
	 * 
	 * @param docs -- a set of Interactions
	 * @return the collapsed graph built from the input data
	 * 
	 * @throws GraphOptimizerException
	 * @throws GraphMergerException
	 * @throws FragmentGraphGeneratorException
	 * @throws LAPException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws IOException
	 * @throws EntailmentGraphRawException
	 * @throws TransformerException
	 */
	EntailmentGraphCollapsed buildCollapsedGraph(Set<Interaction> docs)
			throws GraphOptimizerException, GraphMergerException,
			FragmentGraphGeneratorException, LAPException,
			FragmentAnnotatorException, ModifierAnnotatorException, IOException,
			EntailmentGraphRawException, TransformerException;


	/**
	 * Builds a collapsed graph from the given list of CAS objects. It will build a raw graph first, and then collapse it.
	 * 
	 * @param docs -- a list of CAS objects
	 * @return the collapsed graph built from the input data
	 * 
	 * @throws GraphOptimizerException
	 * @throws GraphMergerException
	 * @throws FragmentGraphGeneratorException
	 * @throws FragmentAnnotatorException
	 * @throws ModifierAnnotatorException
	 * @throws LAPException
	 * @throws IOException
	 * @throws EntailmentGraphRawException
	 * @throws TransformerException
	 */
	EntailmentGraphCollapsed buildCollapsedGraph(List<JCas> docs)
			throws GraphOptimizerException, GraphMergerException,
			FragmentGraphGeneratorException, FragmentAnnotatorException,
			ModifierAnnotatorException, LAPException, IOException, 
			EntailmentGraphRawException, TransformerException;

}

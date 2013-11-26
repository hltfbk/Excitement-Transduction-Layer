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

public abstract interface UseCaseOneRunner {
	
	
	EntailmentGraphRaw buildRawGraph(Set<Interaction> docs)
			throws GraphMergerException, FragmentGraphGeneratorException,
			LAPException, FragmentAnnotatorException,
			ModifierAnnotatorException, IOException;


	EntailmentGraphRaw buildRawGraph(List<JCas> docs)
			throws GraphMergerException, FragmentGraphGeneratorException,
			FragmentAnnotatorException, ModifierAnnotatorException, LAPException, IOException;


	EntailmentGraphCollapsed buildCollapsedGraph(File f)
			throws GraphOptimizerException;


	EntailmentGraphCollapsed buildCollapsedGraph(Set<Interaction> docs)
			throws GraphOptimizerException, GraphMergerException,
			FragmentGraphGeneratorException, LAPException,
			FragmentAnnotatorException, ModifierAnnotatorException, IOException,
			EntailmentGraphRawException, TransformerException;


	EntailmentGraphCollapsed buildCollapsedGraph(List<JCas> docs)
			throws GraphOptimizerException, GraphMergerException,
			FragmentGraphGeneratorException, FragmentAnnotatorException,
			ModifierAnnotatorException, LAPException, IOException, 
			EntailmentGraphRawException, TransformerException;

}

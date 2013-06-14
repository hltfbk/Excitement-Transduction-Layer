package eu.excitementproject.tl.toplevel.api;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
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
			ModifierAnnotatorException;


	EntailmentGraphRaw buildRawGraph(List<JCas> docs)
			throws GraphMergerException, FragmentGraphGeneratorException,
			FragmentAnnotatorException, ModifierAnnotatorException, LAPException;


	EntailmentGraphCollapsed buildCollapsedGraph(File f)
			throws CollapsedGraphGeneratorException;


	EntailmentGraphCollapsed buildCollapsedGraph(Set<Interaction> docs)
			throws CollapsedGraphGeneratorException, GraphMergerException,
			FragmentGraphGeneratorException, LAPException,
			FragmentAnnotatorException, ModifierAnnotatorException;


	EntailmentGraphCollapsed buildCollapsedGraph(List<JCas> docs)
			throws CollapsedGraphGeneratorException, GraphMergerException,
			FragmentGraphGeneratorException, FragmentAnnotatorException,
			ModifierAnnotatorException, LAPException;

}

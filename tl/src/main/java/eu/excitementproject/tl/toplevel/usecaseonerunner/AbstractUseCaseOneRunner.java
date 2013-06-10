package eu.excitementproject.tl.toplevel.usecaseonerunner;

import java.io.File;
import java.util.Set;

import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.tl.composition.exceptions.CollapsedGraphGeneratorException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.toplevel.api.UseCaseOneRunner;

public abstract interface AbstractUseCaseOneRunner extends UseCaseOneRunner{

	abstract public <T> EntailmentGraphRaw buildRawGraph(Set<T> docs, LAPAccess lap, EDABasic<?> eda) throws FragmentGraphGeneratorException, GraphMergerException, CollapsedGraphGeneratorException ;
	
	abstract public EntailmentGraphCollapsed buildGraph(File f) throws FragmentGraphGeneratorException, GraphMergerException, CollapsedGraphGeneratorException;
//	abstract public EntailmentGraphCollapsed buildGraph(File f, double confidence) throws CollapsedGraphGeneratorException;

	abstract public <T> EntailmentGraphCollapsed buildGraph(Set<T> docs, LAPAccess lap, EDABasic<?> eda) throws FragmentGraphGeneratorException, GraphMergerException, CollapsedGraphGeneratorException;
//	abstract public <T> EntailmentGraphCollapsed buildGraph(Set<T> docs, double confidence, LAPAccess lap, EDABasic<?> eda) throws CollapsedGraphGeneratorException;

	
}

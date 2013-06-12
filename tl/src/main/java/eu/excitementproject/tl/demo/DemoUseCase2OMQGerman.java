package eu.excitementproject.tl.demo;

import java.io.File;

import org.apache.uima.jcas.JCas;

import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.CategoryAnnotatorException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentGraphGeneratorException;
import eu.excitementproject.tl.decomposition.exceptions.ModifierAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.toplevel.usecasetworunner.UseCaseTwoRunnerPrototype;

/**
 * Shows OMQ use case data flow. 
 * 
 * @param args
 * @throws FragmentAnnotatorException
 * @throws ModifierAnnotatorException
 * @throws FragmentGraphGeneratorException
 * @throws NodeMatcherException
 * @throws CategoryAnnotatorException
 * @throws LAPException 
 */
public class DemoUseCase2OMQGerman {
	
	public static void main(String[] args) throws FragmentAnnotatorException, ModifierAnnotatorException, 
		FragmentGraphGeneratorException, NodeMatcherException, CategoryAnnotatorException, LAPException {

		//read in some entailment graph
		EntailmentGraphRaw graph = EntailmentGraphRaw.getSampleOuput(true); //TODO: replace with OMQ example graph
		//System.out.println(entailmentGraph);
		
		//create some sample input
		JCas cas = CASUtils.createNewInputCas();
		//some CAS
		CASUtils.deserializeFromXmi(cas, new File("target/CASInput_example_4.xmi")); //TODO: replace with OMQ example
		
		UseCaseTwoRunnerPrototype p = new UseCaseTwoRunnerPrototype(null, null); //TODO: add initialized lap and eda
		p.annotateCategories(cas, graph);
		
		//print CAS
		CASUtils.dumpCAS(cas);
	}

}

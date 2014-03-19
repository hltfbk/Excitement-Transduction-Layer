package eu.excitementproject.tl.experiments.NICE;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;

public class SemevalStuff {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/NICE_open_trainTest_byClusterSplit/test";

		GoldStandardEdgesLoader gsloader = new GoldStandardEdgesLoader();
		try {
			gsloader.addAllAnnotations(gsAnnotationsDir);
			System.out.print("Number of edges: ");
			System.out.println(gsloader.getCollapsedGraph().edgeSet().size());
			System.out.print("Number of nodes: ");
			EntailmentGraphCollapsed c = gsloader.getCollapsedGraph();
			System.out.println(c.vertexSet().size());
			c.toDOT("D:/Lili/collapsedGold.open.dot.txt");
			c.toXML("D:/Lili/collapsedGold.open.xml");
		} catch (GraphEvaluatorException | GraphOptimizerException | IOException | EntailmentGraphCollapsedException | TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			

	}

}

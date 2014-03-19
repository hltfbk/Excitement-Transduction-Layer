package eu.excitementproject.tl.experiments.NICE;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;

public class SemevalStuff {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/";
		String gsAnnotationsDir = tlDir+"src/test/resources/WP2_gold_standard_annotation/NICE_open";

		File gsDir = new File(gsAnnotationsDir);
		for(String s: gsDir.list()){
			File f = new File(gsAnnotationsDir+"/"+s);
			if (f.isDirectory()){
				System.out.println(f.getName().toUpperCase());
				try {
					GoldStandardEdgesLoader gsloader = new GoldStandardEdgesLoader();
					gsloader.addClusterAnnotations(f.getAbsolutePath());
					EntailmentGraphRaw r = gsloader.getRawGraph();
					System.out.print("Raw graph.\nNumber of edges: ");
					System.out.println(r.edgeSet().size());
					System.out.print("Number of nodes: ");
					System.out.println(r.vertexSet().size());
					r.toDOT("D:/Lili/rawGold"+f.getName().toUpperCase()+".open.dot.txt");
					r.toXML("D:/Lili/rawGold"+f.getName().toUpperCase()+".open.xml");
					EntailmentGraphCollapsed c = gsloader.getCollapsedGraph();
					System.out.print("Collapsed graph.\nNumber of edges: ");
					System.out.println(c.edgeSet().size());
					System.out.print("Number of nodes: ");
					System.out.println(c.vertexSet().size());
					c.toDOT("D:/Lili/collapsedGold"+f.getName().toUpperCase()+".open.dot.txt");
					c.toXML("D:/Lili/collapsedGold"+f.getName().toUpperCase()+".open.xml");
				} catch (GraphEvaluatorException | GraphOptimizerException | IOException | EntailmentGraphCollapsedException | TransformerException | EntailmentGraphRawException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}							
			}
		}
	}

}

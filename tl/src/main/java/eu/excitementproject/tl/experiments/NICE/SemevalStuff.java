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
		String gsAnnotationsDir = "D:/Lili/NICE dadasets/NICEall";
		String resSting = "";
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
					r.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_raw.dot.txt");
					r.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_raw.xml");

/*					r.applyTransitiveClosure(false);
					System.out.print("Raw graph+Closure.\nNumber of edges: ");
					System.out.println(r.edgeSet().size());
					System.out.print("Number of nodes: ");
					System.out.println(r.vertexSet().size());
					r.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_rawClosure.dot.txt");
					r.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_rawClosure.xml");
*/
					EntailmentGraphCollapsed c = gsloader.getCollapsedGraph();
					System.out.print("Collapsed graph.\nNumber of edges: ");
					System.out.println(c.edgeSet().size());
					System.out.print("Number of nodes: ");
					System.out.println(c.vertexSet().size());
					c.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsed.dot.txt", gsloader.getNodeTextById());
					c.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsed.xml");
					resSting +=f.getName().toUpperCase()+"\t"+String.valueOf(r.vertexSet().size())+"\t"+String.valueOf(c.vertexSet().size())+"\t"+String.valueOf(r.edgeSet().size())+"\t"+String.valueOf(c.edgeSet().size())+"\n";
					
					c.removeTransitiveClosure();
					System.out.print("Collapsed graph -MinusClosure.\nNumber of edges: ");
					System.out.println(c.edgeSet().size());
					System.out.print("Number of nodes: ");
					System.out.println(c.vertexSet().size());
					c.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedClosure.dot.txt", gsloader.getNodeTextById());
					c.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedClosure.xml");
					resSting +=f.getName().toUpperCase()+"\t"+String.valueOf(r.vertexSet().size())+"\t"+String.valueOf(c.vertexSet().size())+"\t"+String.valueOf(r.edgeSet().size())+"\t"+String.valueOf(c.edgeSet().size())+"\n";

					
					c.applyTransitiveClosure(true);
					System.out.print("Collapsed graph + closure.\nNumber of edges: ");
					System.out.println(c.edgeSet().size());
					System.out.print("Number of nodes: ");
					System.out.println(c.vertexSet().size());
					c.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedClosure.dot.txt", gsloader.getNodeTextById());
					c.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedClosure.xml");
					resSting +=f.getName().toUpperCase()+"\t"+String.valueOf(r.vertexSet().size())+"\t"+String.valueOf(c.vertexSet().size())+"\t"+String.valueOf(r.edgeSet().size())+"\t"+String.valueOf(c.edgeSet().size())+"\n";
				} catch (GraphEvaluatorException | GraphOptimizerException | IOException | EntailmentGraphCollapsedException | TransformerException | EntailmentGraphRawException e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
									
				}							
			}
		}
		System.out.println(resSting);
	}

}

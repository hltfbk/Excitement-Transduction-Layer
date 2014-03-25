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
		//String gsAnnotationsDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ITA-SPLIT-2014-03-14-FINAL/Test";
		String gsAnnotationsDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-FINAL/Test";
		String resSting ="Cluster"+"\t"+"origRawNodes"+"\t"+"origRawEdges"+"\t"+"noClosureRawEdges"+"\t"+"withClosureRawEdges"+"\t"+"origCollapsedNodes"+"\t"+"origCollapsedEdges"+"\t"+"noClosureRawEdges"+"\t"+"withClosureRawEdges"+"\n";
		File gsDir = new File(gsAnnotationsDir);
		for(String s: gsDir.list()){
			File f = new File(gsAnnotationsDir+"/"+s);
			if (f.isDirectory()){
				System.out.println(f.getName().toUpperCase());
				try {
					GoldStandardEdgesLoader gsloader = new GoldStandardEdgesLoader();
					gsloader.addClusterAnnotations(f.getAbsolutePath(), false);
					EntailmentGraphRaw r = gsloader.getRawGraph();
					System.out.print("Raw graph.\nNumber of edges: ");
					int origRawEdges = r.edgeSet().size();
					System.out.println(origRawEdges);
					System.out.print("Number of nodes: ");
					int origRawNodes = r.vertexSet().size();
					System.out.println(origRawNodes);
					r.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_raw.dot.txt");
					r.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_raw.xml");

					
					r.removeTransitiveClosure();
					System.out.print("Raw graph -MinusClosure.\nNumber of edges: ");
					int noClosureRawEdges = r.edgeSet().size();
					System.out.println(noClosureRawEdges);
					System.out.print("Number of nodes: ");
					int noClosureRawNodes = r.vertexSet().size(); 
					System.out.println(noClosureRawNodes);
					r.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_rawNoClosure.dot.txt");
					r.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_rawNoClosure.xml");

					
					r.applyTransitiveClosure(false);
					System.out.print("Raw graph+Closure.\nNumber of edges: ");
					int withClosureRawEdges = r.edgeSet().size(); 
					System.out.println(withClosureRawEdges);
					System.out.print("Number of nodes: ");
					int withClosureRawNodes =r.vertexSet().size(); 
					System.out.println(withClosureRawNodes);
					r.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_rawClosure.dot.txt");
					r.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_rawClosure.xml");

					EntailmentGraphCollapsed c = gsloader.getCollapsedGraph();
					System.out.print("Collapsed graph.\nNumber of edges: ");
					int origCollapsedEdges = c.edgeSet().size(); 
					System.out.println(origCollapsedEdges);
					System.out.print("Number of nodes: ");
					int origCollapsedNodes = c.vertexSet().size(); 
					System.out.println(origCollapsedNodes);
					c.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsed.dot.txt", gsloader.getNodeTextById());
					c.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsed.xml");
					
					c.removeTransitiveClosure();
					System.out.print("Collapsed graph -MinusClosure.\nNumber of edges: ");
					int noClosureCollapsedEdges = c.edgeSet().size(); 
					System.out.println(noClosureCollapsedEdges);
					System.out.print("Number of nodes: ");
					int noClosureCollapsedNodes = c.vertexSet().size(); 
					System.out.println(noClosureCollapsedNodes);
					c.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedNoClosure.dot.txt", gsloader.getNodeTextById());
					c.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedNoClosure.xml");

					
					c.applyTransitiveClosure(true);
					System.out.print("Collapsed graph + closure.\nNumber of edges: ");
					int withClosureCollapsedEdges = c.edgeSet().size(); 
					System.out.println(withClosureCollapsedEdges);
					System.out.print("Number of nodes: ");
					int withClosureCollapsedNodes = c.vertexSet().size();
					System.out.println(withClosureCollapsedNodes);
					c.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedClosure.dot.txt", gsloader.getNodeTextById());
					c.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedClosure.xml");
					
					resSting +=f.getName().toUpperCase()+"\t"+String.valueOf(origRawNodes)+"\t"+String.valueOf(origRawEdges)+"\t"+String.valueOf(noClosureRawEdges)+"\t"+String.valueOf(withClosureRawEdges)+"\t"+String.valueOf(origCollapsedNodes)+"\t"+String.valueOf(origCollapsedEdges)+"\t"+String.valueOf(noClosureCollapsedEdges)+"\t"+String.valueOf(withClosureCollapsedEdges)+"\n";
	//				resSting +=f.getName().toUpperCase()+"\t"+String.valueOf(origRawNodes)+"\t"+String.valueOf(origRawEdges)+"\t"+"---"+"\t"+"---"+"\t"+String.valueOf(origCollapsedNodes)+"\t"+String.valueOf(origCollapsedEdges)+"\t"+String.valueOf(noClosureCollapsedEdges)+"\t"+String.valueOf(withClosureCollapsedEdges)+"\n";
				} catch (GraphEvaluatorException | GraphOptimizerException | IOException | EntailmentGraphCollapsedException | TransformerException | EntailmentGraphRawException e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
									
				}							
			}
		}
		System.out.println(resSting);
	}

}

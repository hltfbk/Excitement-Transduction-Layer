package eu.excitementproject.tl.evaluation.graphmerger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.transform.TransformerException;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

public class GoldStandardAnalyzer extends GoldStandardEdgesLoader {

	boolean presentFragmentGraphsAsSingleNode;
	
	public GoldStandardAnalyzer(boolean presentFragmentGraphsAsSingleNode, boolean withClosure) {
		super(withClosure);
		this.presentFragmentGraphsAsSingleNode = presentFragmentGraphsAsSingleNode;
	}

	@Override
	public EntailmentGraphRaw getRawGraph(){
		if (!presentFragmentGraphsAsSingleNode) return super.getRawGraph();
		
		EntailmentGraphRaw g = new EntailmentGraphRaw();
		// map every node (by text) to the corresponding complete statement (replace node's interactionId_FGid_statementId with interactionId_FGid_0)
		HashMap<String,String> textToCompleteStatementId = new HashMap<String,String>(); 		
		for (String id : nodeTextById.keySet()){
			// only add complete statement nodes (to have every FG presented solely by it's complete statement) 
			if (id.endsWith("_0")){ //node ids are in the format interactionId_FGid_statementId, where complete statements end with _0
				g.addVertex(getGoldStandardNode(nodeTextById.get(id))); // the EUs should be the same as created when adding edges to the "edges" attribute of the class
			}
			textToCompleteStatementId.put(nodeTextById.get(id), getCompleteStamentId(id)); //for every node, fill in the map of node text -> complete statement id			
		}
		for (EntailmentRelation e : edges.values()){	
			String srcRefId = textToCompleteStatementId.get(e.getSource().getText());
			String tgtRefId = textToCompleteStatementId.get(e.getTarget().getText());
			g.addEdgeByInduction(getGoldStandardNode(nodeTextById.get(srcRefId)), getGoldStandardNode(nodeTextById.get(tgtRefId)), DecisionLabel.Entailment, 1.0);
		}
		return g;
	}
	
	private String getCompleteStamentId(String id){
		if (id.endsWith("_0")) return id;
		String[] s = id.split("_");
		return s[0]+"_"+s[1]+"_0";
	}
	
	public void getCWClustererInput(String outDir){
		EntailmentGraphRaw g = getRawGraph();
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		try {
			BufferedWriter nodeWriter = new BufferedWriter(new FileWriter(new File(outDir+"/nodes.txt")));
			Integer nodeId = 0;
			for (EntailmentUnit node : g.vertexSet()){
				nodeId++;
				map.put(node.getText(),nodeId);
				nodeWriter.write(nodeId.toString()+"\t"+node.getText()+"\n");
			}
			nodeWriter.close();
			BufferedWriter edgeWriter = new BufferedWriter(new FileWriter(new File(outDir+"/edges.txt")));
			
			HashSet<String> closeList = new HashSet<String>(); // CW clustering tool accepts symmetric edges only
			for (EntailmentRelation edge : g.edgeSet()){
				Integer src = map.get(edge.getSource().getText());
				Integer tgt = map.get(edge.getTarget().getText());
				String testIfSeen = src.toString()+"->"+tgt.toString();
				if (!closeList.contains(testIfSeen)){
					Integer score = 1;
					edgeWriter.write(src+"\t"+tgt+"\t"+score.toString()+"\n");
					edgeWriter.write(tgt+"\t"+src+"\t"+score.toString()+"\n");
					closeList.add(src.toString()+"->"+tgt.toString());
					closeList.add(tgt.toString()+"->"+src.toString());
				}
			}			
			edgeWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getStatistics(String gsAnnotationsDir) {
		String resSting ="Cluster"+"\t"+"origRawNodes"+"\t"+"origRawEdges"+"\t"+"noClosureRawEdges"+"\t"+"withClosureRawEdges"+"\t"+"origCollapsedNodes"+"\t"+"origCollapsedEdges"+"\t"+"noClosureRawEdges"+"\t"+"withClosureRawEdges"+"\n";
		File gsDir = new File(gsAnnotationsDir);
		for(String s: gsDir.list()){
			File f = new File(gsAnnotationsDir+"/"+s);
			if (f.isDirectory()){
				System.out.println(f.getName().toUpperCase());
				try {
					GoldStandardEdgesLoader gsloader = new GoldStandardEdgesLoader(false); //load only the original data, no transitive closure added
					gsloader.loadClusterAnnotations(f.getAbsolutePath(), false);
					EntailmentGraphRaw r = gsloader.getRawGraph();
					System.out.print("Raw graph.\nNumber of edges: ");
					int origRawEdges = r.edgeSet().size();
					System.out.println(origRawEdges);
					System.out.print("Number of nodes: ");
					int origRawNodes = r.vertexSet().size();
					System.out.println(origRawNodes);
					r.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_raw.dot.txt");
					r.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_raw.xml");

					
/*					r.removeTransitiveClosure();
					System.out.print("Raw graph -MinusClosure.\nNumber of edges: ");
					int noClosureRawEdges = r.edgeSet().size();
					System.out.println(noClosureRawEdges);
					System.out.print("Number of nodes: ");
					int noClosureRawNodes = r.vertexSet().size(); 
					System.out.println(noClosureRawNodes);
					r.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_rawNoClosure.dot.txt");
					r.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_rawNoClosure.xml");
*/
					
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
					
	/*				c.removeTransitiveClosure();
					System.out.print("Collapsed graph -MinusClosure.\nNumber of edges: ");
					int noClosureCollapsedEdges = c.edgeSet().size(); 
					System.out.println(noClosureCollapsedEdges);
					System.out.print("Number of nodes: ");
					int noClosureCollapsedNodes = c.vertexSet().size(); 
					System.out.println(noClosureCollapsedNodes);
					c.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedNoClosure.dot.txt", gsloader.getNodeTextById());
					c.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedNoClosure.xml");
*/
					
					c.applyTransitiveClosure(true);
					System.out.print("Collapsed graph + closure.\nNumber of edges: ");
					int withClosureCollapsedEdges = c.edgeSet().size(); 
					System.out.println(withClosureCollapsedEdges);
					System.out.print("Number of nodes: ");
					int withClosureCollapsedNodes = c.vertexSet().size();
					System.out.println(withClosureCollapsedNodes);
					c.toDOT("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedClosure.dot.txt", gsloader.getNodeTextById());
					c.toXML("D:/Lili/NICE dadasets/_finalSplitLili/all.collapsed.row/"+f.getName().toUpperCase()+"_collapsedClosure.xml");
					
					resSting +=f.getName().toUpperCase()+"\t"+String.valueOf(origRawNodes)+"\t"+String.valueOf(origRawEdges)+"\t"+"NA"+"\t"+String.valueOf(withClosureRawEdges)+"\t"+String.valueOf(origCollapsedNodes)+"\t"+String.valueOf(origCollapsedEdges)+"\t"+"NA"+"\t"+String.valueOf(withClosureCollapsedEdges)+"\n";
//					resSting +=f.getName().toUpperCase()+"\t"+String.valueOf(origRawNodes)+"\t"+String.valueOf(origRawEdges)+"\t"+String.valueOf(noClosureRawEdges)+"\t"+String.valueOf(withClosureRawEdges)+"\t"+String.valueOf(origCollapsedNodes)+"\t"+String.valueOf(origCollapsedEdges)+"\t"+String.valueOf(noClosureCollapsedEdges)+"\t"+String.valueOf(withClosureCollapsedEdges)+"\n";
	//				resSting +=f.getName().toUpperCase()+"\t"+String.valueOf(origRawNodes)+"\t"+String.valueOf(origRawEdges)+"\t"+"---"+"\t"+"---"+"\t"+String.valueOf(origCollapsedNodes)+"\t"+String.valueOf(origCollapsedEdges)+"\t"+String.valueOf(noClosureCollapsedEdges)+"\t"+String.valueOf(withClosureCollapsedEdges)+"\n";
				} catch (GraphEvaluatorException | GraphOptimizerException | IOException | EntailmentGraphCollapsedException | TransformerException | EntailmentGraphRawException e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
									
				}							
			}
		}
		System.out.println(resSting);
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GoldStandardAnalyzer anal = new GoldStandardAnalyzer(false, true);			
		try {
			anal.loadAllAnnotations("./src/test/resources/WP2_gold_standard_annotation/NICE_open", false);
			EntailmentGraphRaw gr = anal.getRawGraph();
			gr.toDOT("./src/test/resources/WP2_gold_standard_annotation/NICE_open/rawCS.dot");
			
/*			System.out.println("Preparing CW clustrering input");
			anal.getCWClustererInput("./src/test/resources/WP2_gold_standard_annotation/_big");
			System.out.println("Done");
*/			
			EntailmentGraphCollapsed gc = anal.getCollapsedGraph();
			gc.toDOT("./src/test/resources/WP2_gold_standard_annotation/NICE_open/collapsedCS.dot");
		} catch (IOException | GraphOptimizerException | GraphEvaluatorException e) {						
			e.printStackTrace();
		}				
	}
	
	

}

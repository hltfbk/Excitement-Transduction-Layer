package eu.excitementproject.tl.evaluation.graphmerger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

public class GoldStandardAnalyzer extends GoldStandardEdgesLoader {

	boolean presentFragmentGraphsAsSingleNode;
	
	public GoldStandardAnalyzer(boolean presentFragmentGraphsAsSingleNode) {
		super();
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GoldStandardAnalyzer anal = new GoldStandardAnalyzer(false);			
		try {
			anal.addAllAnnotations("./src/test/resources/WP2_gold_standard_annotation/NICE_open");
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

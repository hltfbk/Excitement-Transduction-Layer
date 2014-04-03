package eu.excitementproject.tl.evaluation.graphmerger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.GraphMergerException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

public class GoldStandardToTxtTranslator {
	EntailmentGraphCollapsed cg = null;

	private int getRelation(EquivalenceClass nodeA, EquivalenceClass nodeB){
		// Note: in a collapsed graph there can be 1 edge (in either direction) or no edge
		Set<EntailmentRelationCollapsed> edges = cg.getAllEdges(nodeA, nodeB);
		if (!edges.isEmpty()) return 1; //A->B
		
		edges = cg.getAllEdges(nodeB, nodeA);
		if (!edges.isEmpty()) return -1; //B->A
		
		return 0; //no entailment in any direction
	}

	
	private String getNode(EquivalenceClass node){
		return node.toShortString();
		//return node.toString();
	}
	
	
	private String getEdge(EquivalenceClass nodeA, EquivalenceClass nodeB){
		int relation = getRelation(nodeA, nodeB);
				
		if (relation == 1) {
			String s = nodeA.getLabel()+"\t->\t"+nodeB.getLabel()+"\tYes\n";
			s+=nodeB.getLabel()+"\t->\t"+nodeA.getLabel()+"\tNo\n";
			return s;
		}
		
		if (relation == -1) {
			String s = nodeB.getLabel()+"\t->\t"+nodeA.getLabel()+"\tYes\n";
			s+=nodeA.getLabel()+"\t->\t"+nodeB.getLabel()+"\tNo\n";
			return s;
		}
		
		// relation == 0
		String s = nodeA.getLabel()+"\t->\t"+nodeB.getLabel()+"\tNo\n";
		s+=nodeB.getLabel()+"\t->\t"+nodeA.getLabel()+"\tNo\n";
		return s;
	}
	
	private void loadGraph(File gsDir) throws GraphOptimizerException{
		GoldStandardEdgesLoader gsloader = new GoldStandardEdgesLoader(false); //load the original data only		
		for(String clusterAnnotationDir: gsDir.list()){
			try {
				File gsClusterDir = new File(gsDir.getAbsolutePath()+"/"+clusterAnnotationDir);
				if (gsClusterDir.isDirectory()){
					System.out.println(gsClusterDir.getName().toUpperCase());
					gsloader.loadClusterAnnotations(gsClusterDir.getAbsolutePath(), true); //load FG + merged data
				}
			} catch (GraphEvaluatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
				
		cg = gsloader.getCollapsedGraph();
		cg.applyTransitiveClosure(false);
		
		Map<String,Set<String>> textToIdsMap = new HashMap<String, Set<String>>();
		for (String id : gsloader.getNodeTextById().keySet()){
			String text = gsloader.getNodeTextById().get(id);
			Set<String> idsOfText = new HashSet<String>();
			if (textToIdsMap.containsKey(text)) idsOfText = textToIdsMap.get(text);
			idsOfText.add(id);
			textToIdsMap.put(text, idsOfText);
		}

	}
	
	private void loadGraph(String collapsedXmlFileName) throws EntailmentGraphCollapsedException {
		cg = new EntailmentGraphCollapsed(new File(collapsedXmlFileName));
		cg.applyTransitiveClosure(false);
	}

	
	private void translateFromXml(File txtFile) throws EntailmentGraphCollapsedException, IOException{
			if (cg==null){
				System.out.println("Collapsed graph not loaded. Exiting.");
				return;
			}
			
			int nodeId = 0;
			BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile));
			for (EquivalenceClass node : cg.vertexSet()){
				nodeId++;
				writer.write("collapsed node #"+String.valueOf(nodeId)+"\n"+getNode(node)+"\n");
			}
			
			// now for each possible pairs of nodes, output the relation
			Set<String> closedList = new HashSet<String>();
			int pairId = 0;
			for (EquivalenceClass nodeA : cg.vertexSet()){
				for (EquivalenceClass nodeB : cg.vertexSet()){
					if (nodeA.equals(nodeB)) continue;	
					
					String pair1 = nodeA.toString()+nodeB.toString();
					String pair2 = nodeB.toString()+nodeA.toString();
					if (closedList.contains(pair1)) continue;
					if (closedList.contains(pair2)) continue;
					closedList.add(pair1); closedList.add(pair2);
					
					pairId++;
					writer.write("node pair #"+String.valueOf(pairId)+":\n"+getEdge(nodeA, nodeB)+"\n");
				}
			}
			writer.close();
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File collapsedXmlFile = new File("C:/Users/Lili/Dropbox/_tmp/semeval/EMAIL0210_collapsed.xml");
		File txtFile = new File("C:/Users/Lili/Dropbox/_tmp/semeval/EMAIL0210_collapsed.txt");
		
		GoldStandardToTxtTranslator tr = new GoldStandardToTxtTranslator();
		try {
			tr.loadGraph(collapsedXmlFile.getAbsolutePath());
			tr.translateFromXml(txtFile);
		} catch (EntailmentGraphCollapsedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

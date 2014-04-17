package eu.excitementproject.tl.experiments.Semeval;

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

import arkref.parsestuff.RegexUtil.R;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

public class GoldStandardToTxtTranslator {
	
	private enum Relation{
		BIDIR,
		AB,
		BA,
		NONE
	}
	
	EntailmentGraphRaw rg = null;
	EntailmentGraphCollapsed wp2cg = null;
	EntailmentGraphCollapsed ourCg = null;
	Map<String,Set<String>> textToIdsMap; 
	
	private int getWP2Relation(EquivalenceClass nodeA, EquivalenceClass nodeB){
		// Note: in a collapsed graph there can be 1 edge (in either direction) or no edge
		
		Set<EntailmentRelationCollapsed> edges = wp2cg.getAllEdges(nodeA, nodeB);
		if (!edges.isEmpty()) return 1; //A->B
		
		edges = wp2cg.getAllEdges(nodeB, nodeA);
		if (!edges.isEmpty()) return -1; //B->A
		
		return 0; //no entailment in any direction
	}

	private Relation getRelation(EquivalenceClass nodeA, EquivalenceClass nodeB){
		// Note: in a collapsed graph there can be 1 edge (in either direction) or no edge
		
		boolean ab=false;
		boolean ba=false;
		
		for (EntailmentUnit a : nodeA.getEntailmentUnits()){
			for (EntailmentUnit b : nodeB.getEntailmentUnits()){
				if (rg.isEntailment(a, b)) ab = true;
				if (rg.isEntailment(b, a)) ba = true;
			}
		}
		
		if ((ab)&&(ba)) return Relation.BIDIR;
		if (ab) return Relation.AB;
		if (ba) return Relation.BA;
		return Relation.NONE;
		
	}
	
	private String getNode(EquivalenceClass node){
		return node.toStringWithIds(textToIdsMap);
		//return node.toString();
	}
	
	private String getEdge(EquivalenceClass nodeA, EquivalenceClass nodeB){
		return nodeA.getLabel()+"\t"+nodeA.getEntailmentUnits().size()+"\t->\t"+nodeB.getLabel()+"\t"+nodeB.getEntailmentUnits().size();
	}

	
	private String getAnnotatedEdge(EquivalenceClass nodeA, EquivalenceClass nodeB){
		Relation relation = getRelation(nodeA, nodeB);
				
		if (relation.equals(Relation.AB)) {
			String s = getEdge(nodeA,nodeB)+"\tYes\t"+String.valueOf(countFragmentGraphEdges(nodeA, nodeB))+"\n";
			s+=getEdge(nodeB, nodeA)+"\tNo\n";
			return s;
		}
		
		if (relation.equals(Relation.BA)) {
			String s = getEdge(nodeB, nodeA)+"\tYes\t"+String.valueOf(countFragmentGraphEdges(nodeB, nodeA))+"\n";
			s+=getEdge(nodeA, nodeB)+"\tNo\n";
			return s;
		}
		
		if (relation.equals(Relation.BIDIR)){
			String s = getEdge(nodeA,nodeB)+"\tYes\t"+String.valueOf(countFragmentGraphEdges(nodeA, nodeB))+"\n";
			s+=getEdge(nodeB, nodeA)+"\tYes\t"+String.valueOf(countFragmentGraphEdges(nodeB, nodeA))+"\n";
			return s;
		}
		// relation == 0
		String s = getEdge(nodeA, nodeB)+"\tNo\n";
		s+=getEdge(nodeB, nodeA)+"\tNo\n";
		return s;
	}
	
	private void loadClusterGraph(File gsClusterDir) throws GraphOptimizerException, GraphEvaluatorException{
		System.out.println(gsClusterDir.getAbsolutePath());
		GoldStandardEdgesLoader gsloader = new GoldStandardEdgesLoader(false); //load the original data only		
		if (gsClusterDir.isDirectory()){
			System.out.println(gsClusterDir.getName().toUpperCase());
			gsloader.loadClusterAnnotations(gsClusterDir.getAbsolutePath(), true); //load FG + merged data
		}
				
		System.out.println(gsloader.getEdges().size());

		rg = gsloader.getRawGraph();
		
		wp2cg = gsloader.getCollapsedGraph();
		wp2cg.applyTransitiveClosure(false);
		
		textToIdsMap = new HashMap<String, Set<String>>();
		for (String id : gsloader.getNodeTextById().keySet()){
			String text = gsloader.getNodeTextById().get(id);
			Set<String> idsOfText = new HashSet<String>();
			if (textToIdsMap.containsKey(text)) idsOfText = textToIdsMap.get(text);
			idsOfText.add(id);
			textToIdsMap.put(text, idsOfText);
		}		
	}
	
	private int countFragmentGraphEdges(EquivalenceClass source, EquivalenceClass target){
		int fge = 0;
		for (EntailmentUnit src : source.getEntailmentUnits()){
			for (EntailmentUnit tgt : target.getEntailmentUnits()){				
				for(EntailmentRelation edge : rg.getAllEdges(src, tgt)){
					if (edge.getEdgeType().equals(EdgeType.FRAGMENT_GRAPH)) fge++;
				}				
			}
		}
		return fge;
	}
/*	private void loadGraph(String collapsedXmlFileName) throws EntailmentGraphCollapsedException {
		cg = new EntailmentGraphCollapsed(new File(collapsedXmlFileName));
		cg.applyTransitiveClosure(false);
	}
*/
	
	private void translateFromXml(EntailmentGraphCollapsed cg, File txtFile) throws EntailmentGraphCollapsedException, IOException{
			if (cg==null){
				System.out.println("Collapsed graph not loaded. Exiting.");
				return;
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile));
			writer.write("Node caption	Entailment Unit	EU_id					Node_check	Comments\n\n");
			
			int nodeId = 1000;
			for (EquivalenceClass node : cg.vertexSet()){
				nodeId++;
				writer.write("collapsed node #"+String.valueOf(nodeId)+" : "+ node.getEntailmentUnits().size() +" entailment unit(s) before editing\n"+getNode(node)+"\n");
			}
			
			writer.write("\nSource	#EU in src	->	Target	#EU in tgt	Decision	#FG edges	Decision_new	Comments\n\n");
			
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
					writer.write("node pair #"+String.valueOf(pairId)+":\n"+getAnnotatedEdge(nodeA, nodeB)+"\n");
				}
			}
			writer.close();
	}
	
	private boolean isEmpty(String s){
		s=s.replace("\n", "");
		if (s.isEmpty()) return true;
		for (String ss : s.split("\t")){
			if (!ss.isEmpty()) return false; 
		}
		return true;
	}

	private boolean areNodesConsistent(){
		boolean cons=true;
		Set<String> wp2eus = new HashSet<String>();
		for (EntailmentUnit eu : rg.vertexSet()) wp2eus.add(eu.getTextWithoutDoubleSpaces());
		
		Set<String> closedList = new HashSet<String>();
		for (EquivalenceClass node : ourCg.vertexSet()){
			for(EntailmentUnit eu : node.getEntailmentUnits()){
				if (closedList.contains(eu.getTextWithoutDoubleSpaces())) {
					System.err.println("ERROR: "+eu.getText()+" is listed in more that one collapsed node.");
					cons = false;
				}
				else{
					if (!wp2eus.contains(eu.getText())){
						System.err.println("ERROR: "+eu.getTextWithoutDoubleSpaces()+" is not found in the raw graph.");
						cons = false;						
					}
					else closedList.add(eu.getText());
				}
			}
		}
		return cons;
	}
	
	private void generateCollapsedGraphForFixedNodes(File inputFile) throws EntailmentGraphCollapsedException, IOException{
		if (rg==null){
			System.out.println("raw graph not loaded. Exiting.");
			return;
		}
		rg.applyTransitiveClosure(false);
		ourCg = new EntailmentGraphCollapsed();
		
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		
		String line = reader.readLine(); //caption line before all nodes
		while (line!=null){
			if (line.contains("Source	#EU in src	->	Target	#EU in tgt	Decision	#FG edges	Decision_new	Comments")) break;
			if (isEmpty(line)) { //skip "empty" line
				line=reader.readLine();	
				continue;  
			}
			
			// if reached this point - have just read a node's caption 		
			line = reader.readLine(); // read the first eu (if any)
			Set<EntailmentUnit> nodeEUs = new HashSet<EntailmentUnit>();
			while(!isEmpty(line)) {
				String text = line.split("\t")[1];
				EntailmentUnit eu = GoldStandardEdgesLoader.getGoldStandardNode(text);
				nodeEUs.add(eu);
				line=reader.readLine();
			}
			if (!nodeEUs.isEmpty()){
				EquivalenceClass node = new EquivalenceClass(nodeEUs);
				ourCg.addVertex(node);
			}
		}
		reader.close();
		System.out.println(ourCg.vertexSet().size());

/*		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(inputFile.getAbsolutePath()+".edgesForCleanNodes.txt")));
		for (EquivalenceClass node : wp2cg.vertexSet()){
			nodeId++;
			writer.write("collapsed node #"+String.valueOf(nodeId)+" : "+ node.getEntailmentUnits().size() +" entailment unit(s) before editing\n"+getNode(node)+"\n");
		}
		
		writer.write("\nSource	#EU in src	->	Target	#EU in tgt	Decision	#FG edges\n");
		
		// now for each possible pairs of nodes, output the relation
		Set<String> closedList = new HashSet<String>();
		int pairId = 0;
		for (EquivalenceClass nodeA : wp2cg.vertexSet()){
			for (EquivalenceClass nodeB : wp2cg.vertexSet()){
				if (nodeA.equals(nodeB)) continue;	
				
				String pair1 = nodeA.toString()+nodeB.toString();
				String pair2 = nodeB.toString()+nodeA.toString();
				if (closedList.contains(pair1)) continue;
				if (closedList.contains(pair2)) continue;
				closedList.add(pair1); closedList.add(pair2);
				
				pairId++;
				writer.write("node pair #"+String.valueOf(pairId)+":\n"+getAnnotatedEdge(nodeA, nodeB)+"\n");
			}
		}
		writer.close();
*/}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		String tlDir = "C:/Users/Lili/Git/Excitement-Transduction-Layer";
//		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer";
		
		String clusterName = "EMAIL0210";
		
		File clusterAnnotationsDir = new File(tlDir+"/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-FINAL/test/"+clusterName);
		File txtFile = new File(tlDir+"/tl/src/test/resources/WP2_reannotation/"+clusterName+"_collapsed.txt");
		File txtFileUp = new File(tlDir+"/tl/src/test/resources/WP2_reannotation/"+clusterName+"_collapsed_updatedNoded.txt");
		
		GoldStandardToTxtTranslator tr = new GoldStandardToTxtTranslator();
		try {
			tr.loadClusterGraph(clusterAnnotationsDir);
			//tr.translateFromXml(tr.wp2cg,txtFile);

			File fixedNodesFile = new File(tlDir+"/tl/src/test/resources/WP2_reannotation/"+clusterName+"_collapsed_Reconciled.txt");
			tr.generateCollapsedGraphForFixedNodes(fixedNodesFile);	
			if(tr.areNodesConsistent()){
				tr.translateFromXml(tr.ourCg, txtFileUp);
			}
			else{
				System.err.println("Nodes are not consistent, no updated file was generated.");
			}
		
		} catch (EntailmentGraphCollapsedException | IOException | GraphOptimizerException | GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}

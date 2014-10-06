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

import eu.excitementproject.eop.biutee.utilities.preprocess.NewNormalizerBasedTextPreProcessor;
import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.GraphOptimizerException;
import eu.excitementproject.tl.composition.graphoptimizer.SimpleGraphOptimizer;
import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.evaluation.graphoptimizer.EvaluatorGraphOptimizer;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

@SuppressWarnings("all")
public class GoldStandardReannotation {
		
	private enum Relation{
		BIDIR,
		AB,
		BA,
		NONE
	}


	EntailmentGraphRaw rg = null;
	EntailmentGraphCollapsed wp2cg = null;
	EntailmentGraphRaw rfg = null;	// contains a raw graph with only fragment graphs in it, no additional edges
	EntailmentGraphCollapsed ourCg = null;
	Map<String,Set<String>> textToIdsMap = null;
	Map<String,String> nodeContentById = null;
	Set<String> newNodes = new HashSet<String>();
	int numFGedges = 0;

	private void clearData(){
		rg = null;
		wp2cg = null;
		rfg = null;	
		ourCg = null;
		textToIdsMap = null;	
		nodeContentById = null;
		newNodes = new HashSet<String>();
		numFGedges = 0;
	}
	
/*	private int getWP2Relation(EquivalenceClass nodeA, EquivalenceClass nodeB){
		// Note: in a collapsed graph there can be 1 edge (in either direction) or no edge
		
		Set<EntailmentRelationCollapsed> edges = wp2cg.getAllEdges(nodeA, nodeB);
		if (!edges.isEmpty()) return 1; //A->B
		
		edges = wp2cg.getAllEdges(nodeB, nodeA);
		if (!edges.isEmpty()) return -1; //B->A
		
		return 0; //no entailment in any direction
	}
*/

	private Relation getRelation(EquivalenceClass nodeA, EquivalenceClass nodeB){

		
		// Note: in a collapsed graph there can be 1 edge (in either direction) or no edge
		//		 but after editing collapsed nodes, there can be bidirectional entailments between collapsed nodes
		//		 This happens when EU-members of the nodes were placed under one collapsed node in the original annotaiton  		

		// Note2: after editing FGs, there can be cases where nodes are not found in WP2 annotation (initial collapsed graph)
		
		boolean ab=false;
		boolean ba=false;
		
		for (EntailmentUnit a : nodeA.getEntailmentUnits()){
			for (EntailmentUnit b : nodeB.getEntailmentUnits()){
				boolean isOK = true;
				if (!rg.containsVertex(a)) {
					isOK = false;
					newNodes.add(a.getText());
				}
				if (!rg.containsVertex(b)) {
					isOK = false;
					newNodes.add(b.getText());
				}
				
				if (isOK){
					if (rg.isEntailment(a, b)) ab = true;
					if (rg.isEntailment(b, a)) ba = true;					
				}
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
			s+=getEdge(nodeB, nodeA)+"\tNo\t"+String.valueOf(countFragmentGraphEdges(nodeB, nodeA))+"\n";
			return s;
		}
		
		if (relation.equals(Relation.BA)) {
			String s = getEdge(nodeB, nodeA)+"\tYes\t"+String.valueOf(countFragmentGraphEdges(nodeB, nodeA))+"\n";
			s += getEdge(nodeA,nodeB)+"\tNo\t"+String.valueOf(countFragmentGraphEdges(nodeA, nodeB))+"\n";
			return s;
		}
		
		if (relation.equals(Relation.BIDIR)){
			String s = getEdge(nodeA,nodeB)+"\tYes\t"+String.valueOf(countFragmentGraphEdges(nodeA, nodeB))+"\n";
			s+=getEdge(nodeB, nodeA)+"\tYes\t"+String.valueOf(countFragmentGraphEdges(nodeB, nodeA))+"\n";
			return s;
		}
		// relation == NONE
		String s = getEdge(nodeA,nodeB)+"\tNo\t"+String.valueOf(countFragmentGraphEdges(nodeA, nodeB))+"\n";
		s+=getEdge(nodeB, nodeA)+"\tNo\t"+String.valueOf(countFragmentGraphEdges(nodeB, nodeA))+"\n";
		return s;
	}
	
	private String loadFragmentGraphs(File gsClusterDir) throws GraphEvaluatorException{
		String s="";
		GoldStandardEdgesLoader gsFGloader = new GoldStandardEdgesLoader(false); //load the original data only		
		if (gsClusterDir.isDirectory()){
			
			System.out.println(gsClusterDir.getName().toUpperCase());
			String warnings = gsFGloader.loadFGsRawGraph(gsClusterDir.getAbsolutePath()); //load only FGs\
			if (!warnings.isEmpty()) s+="Problems with cluster "+gsClusterDir.getName()+":\n"+warnings;
		}

		numFGedges = gsFGloader.getNumFGedges();
		rfg = gsFGloader.getRawGraph();
		
		nodeContentById = gsFGloader.getNodeContentById();
		
		textToIdsMap = new HashMap<String, Set<String>>();
		for (String id : gsFGloader.getNodeTextById().keySet()){
			String text = gsFGloader.getNodeTextById().get(id);
			Set<String> idsOfText = new HashSet<String>();
			if (textToIdsMap.containsKey(text)) idsOfText = textToIdsMap.get(text);
			idsOfText.add(id);
			textToIdsMap.put(text, idsOfText);
		}		
		return s;
	}
	
	private String loadClusterGraph(File gsClusterDir) throws GraphOptimizerException, GraphEvaluatorException{		
		clearData();
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
		
	
		String s = loadFragmentGraphs(gsClusterDir);
		return s;
	}

	
	private Integer countFragmentGraphEdges(EquivalenceClass source, EquivalenceClass target){
		int yesFge = 0;
		int noFge = 0;
		for (EntailmentUnit src : source.getEntailmentUnits()){
			for (EntailmentUnit tgt : target.getEntailmentUnits()){
				for(EntailmentRelation edge : rfg.getAllEdges(src, tgt)){
					if (edge.getLabel().is(DecisionLabel.Entailment)) yesFge++;
					else noFge++;
				}				
			}
		}
		if ((yesFge>0)&&(noFge>0)){
			System.err.println("Smth is not OK with the collapsed nodes! There is a collapsed edge that has both positive and negative underlying edges in FGs (will have <<null>> in the corresponding column):");
			System.err.println("\t"+source.getLabel()+"  ->  "+target.getLabel());			
			return null;   
		}
		if (yesFge > 0) return yesFge;
		if (noFge > 0) return -1*noFge;
		return 0;
	}

/*	private void loadGraph(String collapsedXmlFileName) throws EntailmentGraphCollapsedException {
		cg = new EntailmentGraphCollapsed(new File(collapsedXmlFileName));
		cg.applyTransitiveClosure(false);
	}
*/

	private int countEdgePairs(){
		Set<String> closedList = new HashSet<String>();
		int pairId = 0;
		for (EquivalenceClass nodeA : ourCg.vertexSet()){
			for (EquivalenceClass nodeB : ourCg.vertexSet()){
				if (nodeA.equals(nodeB)) continue;	
				
				String pair1 = nodeA.toString()+nodeB.toString();
				String pair2 = nodeB.toString()+nodeA.toString();
				if (closedList.contains(pair1)) continue;
				if (closedList.contains(pair2)) continue;
				closedList.add(pair1); closedList.add(pair2);
				
				pairId++;
			}
		}
		return pairId;
	}
	
	@SuppressWarnings("unused")
	private void createTxtForReannotation(EntailmentGraphCollapsed cg, File txtFile) throws EntailmentGraphCollapsedException, IOException{
			if (cg==null){
				System.out.println("Collapsed graph not loaded. Exiting.");
				return;
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile));
			writer.write("Node caption	Entailment Unit	EU_id					Node_check	Comments\n\n");
			
			int nodeId = 0;
			for (EquivalenceClass node : cg.vertexSet()){
				nodeId++;
				writer.write("collapsed node #"+String.valueOf(nodeId)+" : "+ node.getEntailmentUnits().size() +" entailment unit(s) before editing\n"+getNode(node)+"\n");
			}
			
			writer.write("\nSource	#EU in src	->	Target	#EU in tgt	Decision	#FG edges	Decision_new	Comments\n\n");
			
			// now for each possible pair of nodes, output the relation
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
			System.out.println("Reannotaiton file "+txtFile+" created successfully.");
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
		Set<String> ourEus = new HashSet<String>();
		for (EntailmentUnit eu : rfg.vertexSet()) wp2eus.add(eu.getText()); // create set of all the nodes from the GFs
		
		Set<String> closedList = new HashSet<String>();
		for (EquivalenceClass node : ourCg.vertexSet()){
			for(EntailmentUnit eu : node.getEntailmentUnits()){
				ourEus.add(eu.getText());
				if (closedList.contains(eu.getText())) {
					System.err.println("ERROR: <<"+eu.getText()+">> is listed in more that one collapsed node.");
					cons = false;
				}
				else{
					if (!wp2eus.contains(eu.getText())){
						System.err.println("ERROR: <<"+eu.getText()+">> is not found in the FGs.");
						cons = false;						
					}
					else closedList.add(eu.getText());
				}
			}
		}
		
		// now check if there are EUs present in the FGs, but not listed in the update
		wp2eus.removeAll(ourEus);
		if (wp2eus.size()>0) {
			System.out.println("WARNING:");			
			System.out.println("The following "+wp2eus.size()+" EUs from the fragment graphs are not present in the updated nodes:");
			for (String s : wp2eus){
				System.out.println(" - <<"+s+">> "+ textToIdsMap.get(s));
			}
		}
		return cons;
	}

	private boolean loadFixedNodes(BufferedReader reader, boolean checkFlagsOnNodes) throws IOException{
		String line = reader.readLine(); //caption line before all nodes
		while (line!=null){
			if (line.contains("Source	#EU in src	->	Target	#EU in tgt	Decision	#FG edges	Decision_new	Comments")) break;
			if (isEmpty(line)) { //skip "empty" line
				line=reader.readLine();	
				continue;  
			}
			
			// if reached this point - have just read a node's caption 		
			
			if (checkFlagsOnNodes){
				// verify if the caption has editor's flag
				String[] caption = line.split("\t");
				if (caption.length<8) {
					System.out.println(caption[0]);
					System.out.println("No editor's flag specified for this node in column 8 (H). Loading nodes interrupted.");
					return false;
				}
				System.out.println(caption[0]+"\t...\t"+caption[7]);
			}
			
			// start reading the contents of the node
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
		return true;
	}
	

	private boolean loadReannotatedCollapsedGraph(File txtFileReannotated, boolean checkNodesConsistency) throws Exception{
		ourCg = new EntailmentGraphCollapsed();		
		BufferedReader reader = new BufferedReader(new FileReader(txtFileReannotated));

		String line = reader.readLine(); //caption line before all nodes
		while (line!=null){
			if (line.contains("Source	#EU in src	->	Target	#EU in tgt	Decision	#FG edges	Decision_new	Comments")) break;
			if (isEmpty(line)) { //skip "empty" line
				line=reader.readLine();	
				continue;  
			}
			
			// if reached this point - have just read a node's caption 					
			// start reading the contents of the node
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
		
		if (checkNodesConsistency){ // worth checking that no collapsed nodes or single EUs were deleted by mistake
			// to check again for nodes' consistency - need to load the raw graph 
			if (rfg==null){
				System.out.println("Fragment graphs not loaded, cannot check for the consistency of nodes. Exiting.");
				reader.close();
				return false;
			}				
			if (!areNodesConsistent()) {
				reader.close();
				return false;
			}
		}

		// when reached this point, line contains the caption of edges
		int processedEdgePairs = 0;
		line = reader.readLine(); // read next line after the caption
		while (line!=null){
			if (isEmpty(line)) { //skip "empty" line
				line=reader.readLine();	
				continue;  
			}
			
			// if reached this point - have just read an edge pair's caption 		
			// start reading the contents of the edge pair
			line = reader.readLine(); // read the first annotation out of 2
			EntailmentRelationCollapsed ed1 = getReannotatedEdge(line);
			if (ed1!=null) ourCg.addEdge(ed1.getSource(), ed1.getTarget(), ed1);
			line = reader.readLine(); // read the 2nd annotation out of 2
			EntailmentRelationCollapsed ed2 = getReannotatedEdge(line);
			if (ed2!=null) ourCg.addEdge(ed2.getSource(), ed2.getTarget(), ed2);
			processedEdgePairs++; //successfully processed a pair of edge annotations
			
			line = reader.readLine(); // proceed to the next annotation
		}		
		reader.close();
		
		int expectedPairs = countEdgePairs();
		if (expectedPairs!=processedEdgePairs) {
			System.err.println("Error detected: Expected "+expectedPairs+" edge pairs. Loaded successfully "+ processedEdgePairs+" reannotated pairs.");
			return false;
		}
		
		// if reached this point, the graph is loaded
		
		// now re-collapse the graph since there might be annotation of equivalent collapsed nodes through bidirectional edges
		System.out.println("Loaded reannotation: nodes "+ourCg.vertexSet().size()+", edges "+ourCg.edgeSet().size());
		EntailmentGraphRaw decollapsedGraph = EvaluatorGraphOptimizer.getDecollapsedGraph(ourCg);
		SimpleGraphOptimizer collapser = new SimpleGraphOptimizer();
		// some annotators 'merged' 2 equivalence classes by annotating them as entailing in both directions. To add all clique edges we have to collapse the graph
		ourCg = collapser.optimizeGraph(decollapsedGraph, 0.0);
		System.out.println("Re-collapsed reannotation: nodes "+ourCg.vertexSet().size()+", edges "+ourCg.edgeSet().size());

		System.out.println("Successfully processed "+ processedEdgePairs+" re-annotated edge pairs. Expected number of pairs = "+expectedPairs);
		// consistency check for edges - all transitive closure edges should be explicitly present in the graph
		// if not - smth is wrong
		int addedEdges = ourCg.edgeSet().size();
		System.out.print("Checking edges for consistency:");
		boolean isConsistent = true;
		ourCg.applyTransitiveClosure(false);
		if (ourCg.edgeSet().size() != addedEdges){
			for (EntailmentRelationCollapsed edge : ourCg.edgeSet()){
				if (edge.getEdgeType().equals(EdgeType.TRANSITIVE_CLOSURE)) {
					System.err.println("\nInconsistent edge: "+ edge);
					isConsistent = false;
				}
			}
		}
		if(isConsistent) System.out.println("  No transitivity violations in annotated edges"); 
			
		// now check if added edges into fragment graphs
		Set<EntailmentRelation> decollapsedEdges = EvaluatorGraphOptimizer.getAllEntailmentRelations(ourCg);
		Set<String> decollapsedEgdesStr = new HashSet<String>();
		
		for (EntailmentRelation e : decollapsedEdges){	
			decollapsedEgdesStr.add(e.getSource().getText()+"->"+e.getTarget().getText());
			// debug part
			if (e.getSource().getText().equals("leg room is uncomfortable for someone of 187 cm")){
				if (e.getTarget().getText().equals("leg room is abit uncomfortable")){
					System.err.println("!!!! "+e);
				}
			} // end debug part
			
			EntailmentUnit rs = rfg.getVertexWithText(e.getSource().getText());
			EntailmentUnit rt = rfg.getVertexWithText(e.getTarget().getText());
			for (EntailmentRelation fge : rfg.getAllEdges(rs, rt)){
				if (fge.getLabel().is(DecisionLabel.NonEntailment)){
					isConsistent = false;
					System.err.println("Edge added into Fragment Graph: "+ e);	
					EntailmentRelationCollapsed edge = ourCg.getEdge(ourCg.getVertex(e.getSource().getText()), ourCg.getVertex(e.getTarget().getText()));
					if (edge!=null) System.err.println("from collapsed edge: "+ edge);
					else System.err.println("as part of collapsed node: "+ ourCg.getVertex(e.getSource().getText()));
				}
			}
		}
		
		if (isConsistent) {
			System.out.println("No inconsistent edges added into FGs.");					
//			System.out.println("No inconsistent edges added into FGs.\nThe graph is consistent. Congratulations :)");					
		}
		
		// Now check if edges were REMOVED from fragment graphs
		for (EntailmentRelation fge : rfg.edgeSet()){
			if (fge.getLabel().is(DecisionLabel.NonEntailment)) continue;
			String es = fge.getSource().getText()+"->"+fge.getTarget().getText();
			if (!decollapsedEgdesStr.contains(es)){
				System.out.println("Edge <<"+ es +">> was removed from a FG!");
				isConsistent = false;
			}
		}
		
		if (isConsistent) {
			System.out.println("No edges removed from FGs.\nThe graph is consistent. Congratulations :)");					
		}

		ourCg.toDOT("C:/Users/Lili/My Documents/_graphs/"+txtFileReannotated.getName()+"_graph.dot.txt");
		
		return true;
	}
	
		private EntailmentRelationCollapsed getReannotatedEdge(String line) throws Exception{
			String[] s = line.split("\t");
			try {
				EquivalenceClass src = ourCg.getVertex(s[0]);
				if (src == null) throw new Exception("Cannot find node with text "+s[0]);
				EquivalenceClass tgt = ourCg.getVertex(s[3]);
				if (tgt == null) throw new Exception("Cannot find node with text "+s[3]);
				Integer decision = Integer.valueOf(s[7]);
				if (decision==1){
					EntailmentRelationCollapsed edge = new EntailmentRelationCollapsed(src, tgt, 1.0, EdgeType.MANUAL_ANNOTATION);
					return edge;
				}
				if (decision == 0) return null;
				throw new Exception("Illegal decision annotation (only 0 or 1 allowed): " + decision);
			} catch (Exception e) {
				System.err.println("Cannot read egde annotation from line: "+line);
				throw e;
			}			
		}
	
	@SuppressWarnings("unused")
	private boolean generateCollapsedGraphForFixedNodes(File inputFile) throws EntailmentGraphCollapsedException, IOException{
		if (rg==null){
			System.out.println("Raw graph not loaded. Exiting.");
			return false;
		}
		rg.applyTransitiveClosure(false);
		ourCg = new EntailmentGraphCollapsed();
		
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			
		boolean success = loadFixedNodes(reader, true);
		reader.close();

		if (success) {
			System.out.println("Loaded "+ourCg.vertexSet().size()+" non-empty collapsed nodes");
			return true;
		}
		ourCg = null;
		return false; 
	}
	

	public void step1CreateFileForEditingNodes(String filename, File clusterAnnotationsDir){
		try {
			File txtFile = new File(filename);
			String s = loadClusterGraph(clusterAnnotationsDir);
			System.out.println(s);
			createTxtForReannotation(wp2cg,txtFile);
		} catch (EntailmentGraphCollapsedException | IOException | GraphOptimizerException | GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void step2CreateFileForEdgeAnnotation(String filename, String fixedNodesFilename, File clusterAnnotationsDir){
		try {
			File txtFileUp = new File(filename);
			String s = loadClusterGraph(clusterAnnotationsDir);
			System.out.println(s);
			File fixedNodesFile = new File(fixedNodesFilename);
			if (generateCollapsedGraphForFixedNodes(fixedNodesFile)){
				if(areNodesConsistent()){
					createTxtForReannotation(ourCg, txtFileUp);
					System.out.println("==========");
					for (String nodeText : newNodes){
						System.err.println("Warning: node <<"+nodeText+">> was not found in WP2-annotated graph.");
					}
					System.out.println("Updated file generated.");
				}
				else{
					System.err.println("Nodes are not consistent, no updated file was generated.");
				}				
			}	
		} catch (EntailmentGraphCollapsedException | IOException | GraphOptimizerException | GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public String step3LoadAnnotatedFile(String filename, File clusterAnnotationsDir, String clusterName){
		String res=clusterName+"\t";
		String fgWarnings="";
		try{
			File txtFileReannotated = new File(filename);
			fgWarnings = loadClusterGraph(clusterAnnotationsDir);

			boolean isConsistent = loadReannotatedCollapsedGraph(txtFileReannotated, true);
			if (!isConsistent) {
				res+="NOT CONSISTENT\n";
				return res;
			}
				
			EntailmentGraphRaw decollapsedGraph = EvaluatorGraphOptimizer.getDecollapsedGraph(ourCg);
			GoldStandardToWP2FormatTranslator trToWp2 = new GoldStandardToWP2FormatTranslator();
			trToWp2.createWP2xml(txtFileReannotated.getAbsolutePath().replace(".txt", "PlusClosure.xml"), decollapsedGraph, textToIdsMap, nodeContentById);
		
			System.out.println("Statistics:");
			System.out.println("Cluster \t Distinct orphan nodes \t All nodes in FGs \t All edges in FGs \t All nodes in WP2 graph \t All edges in WP2 graph \t Distinct-text nodes in FGs \t Distinct-text edges in FGs \t Coll nodes \t meta-nodes \t Avg size of meta-node \t All meta-nodes sizes \t Coll Edges \t Distinct-text merged graph nodes \t Distinct-text merged graph edges");			

			int on = 0;
			for (EntailmentUnit node : decollapsedGraph.vertexSet()){
				if (decollapsedGraph.edgesOf(node).isEmpty()) on++;
			}
			
			res+=on+"\t";

			res+=nodeContentById.size()+"\t";
			res+=numFGedges+"\t";
			res+=trToWp2.getNodeNum()+"\t";
			res+=trToWp2.getEdgeNum()+"\t";

			res+=rfg.vertexSet().size()+"\t";
			
			
			
			int fgYesEdges = 0;
			for (EntailmentRelation e: rfg.edgeSet()){
				if (e.getTEdecision().getDecision().is(DecisionLabel.Entailment)) fgYesEdges++;
			}
			res+=fgYesEdges+"\t";
			res+=ourCg.vertexSet().size()+"\t";
			
		//	System.out.println(rfg.toString());
			
			int metaNodes = 0;
			double metaSize = 0;
			String sizes="";
			for (EquivalenceClass eq : ourCg.vertexSet()){
				Integer size = eq.getEntailmentUnits().size();
				if (size>1){
					metaNodes++;
					metaSize+=size;
					sizes+=size.toString()+"  "; 
				}
			}
			if (metaNodes>0) metaSize/= metaNodes;

			res+=metaNodes+"\t";
			res+=metaSize+"\t";
			res+=sizes+"\t";
			
			
			res+=ourCg.edgeSet().size()+"\t";
			res+=decollapsedGraph.vertexSet().size()+"\t";
			res+=decollapsedGraph.edgeSet().size()+"\t";

			Set<String> dedges = new HashSet<String>();
			for (EntailmentRelation e : decollapsedGraph.edgeSet()){
//				dedges.add(e.toString());
				dedges.add(CompareAnnotations.getString(e));
				if (clusterName.contains("0030")) System.out.println("0030 \t "+dedges.size()+" \t "+CompareAnnotations.getString(e));
					
			}
			res+=dedges.size()+"\n";
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		System.out.println(res);
		return res+fgWarnings;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		String tlDir = "C:/Users/Lili/Git/Excitement-Transduction-Layer";
//		String tlDir = "D:/LiliGit/Excitement-Transduction-Layer";
		
		GoldStandardReannotation tr = new GoldStandardReannotation();
		
		
		String[] devsetIt = {"Cluster A1.1 Accrediti e rimborsi",
							"Cluster A1.2 Piano tariffario e contratto",
							"Cluster A1.3 Punti",
							"Cluster A2.1Tariffe e promozioni",
							"Cluster C1.2 Internet Key",
							"Cluster C1.3 Internet, Consolle e Connessione",
							"Cluster C1.4 Telefonate",
							"Cluster D1.1 Call Center e Servizio Clienti - Contatto",
							"Cluster D1.4 Servizio Comm e Postvendita",
							"Cluster D1.5 Website e Forum",
							"Cluster E1.1 Tablet",
							"Cluster E1.2 Telefoni, smartphone e cellulari"};
						//	"Cluster Generic (set1)"};
		
		
		String[] testsetIt = {"Cluster A1.4 Ricarica e consumi",
//							"Cluster A2.1Tariffe e promozioni test",
							"Cluster B1.1 Generici"};
						//	"Cluster C1.1 ADSL e Rete",
						//	"Cluster C1.3 Internet, Consolle e Connessione test"};
						//	"Cluster D1.2 Call Center e Servizio Clienti - Assistenza",
						//	"Cluster D1.3 Call Center e Servizio Clienti - Servizio"};
							//"Cluster D1.6 Webtools"};
		
		String[] devsetEn = {"EMAIL0001",
		                     "EMAIL0002",
		                     "EMAIL0003",
		                     "EMAIL0010",
		                     "EMAIL0020_DEV",
		                     "EMAIL0030",
		                     "EMAIL0110",
		                     "EMAIL0120",
		                     "EMAIL0130",
		                     "EMAIL0140",
		                     "EMAIL0220",
		                     "EMAIL0230",
		                     "EMAIL0240",
		                     "EMAIL0250",
		                     "EMAIL0320",
		                     "EMAIL0340",
		                     "EMAIL0380",
		                     "EMAIL0390",
		                     "EMAIL0410"};
		
	String[] testsetEn = {"EMAIL0020_TEST",
		"EMAIL0040",
		"EMAIL0050",
		"EMAIL0060",
		"EMAIL0210",
		"EMAIL0310",
		"EMAIL0330",
		"EMAIL0350",
		"EMAIL0360",
		"EMAIL0370"};
	
	//	String clusterName = "EMAIL0002";		
	//	String clusterName = "SPEECH0080";		

	String[] single = {"EMAIL0390"};

	String stat="";
	for (String clusterName : devsetEn){
//		String set = "Test";
		 String set = "Dev";
		
//		String suffix = "Reconciled";
//		String suffix = "LB";
		String suffix = "AF";
//		String suffix = "lk";
		
		
		// ITA		
//		File clusterAnnotationsDir = new File(tlDir+"/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ITA-SPLIT-2014-03-14-FINAL/"+set+"/"+clusterName);
		
		// ENG
		File clusterAnnotationsDir = new File(tlDir+"/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-FINAL/"+set+"/"+clusterName);
		
		if (!clusterAnnotationsDir.exists()) {
			System.err.println("Cannot find annotation dir "+clusterAnnotationsDir.getAbsolutePath());
			return;
		}
		
		
		// Step1: create txt file for editing original WP2 collapsed nodes
	//	tr.step1CreateFileForEditingNodes(tlDir+"/tl/src/test/resources/WP2_reannotation/"+clusterName+"_collapsed.txt", clusterAnnotationsDir);

		// Step2 : create txt file for edge annotation, using updated collapsed nodes
	//	tr.step2CreateFileForEdgeAnnotation(tlDir+"/tl/src/test/resources/WP2_reannotation/"+clusterName+"_collapsed_updatedNodes.txt", tlDir+"/tl/src/test/resources/WP2_reannotation/"+clusterName+"_collapsed_"+suffix+".txt", clusterAnnotationsDir);
		

		// Step3 : load final re-annotated graph, check for consistency, calculate statistics and create wp2-format file 		
		stat+=tr.step3LoadAnnotatedFile(tlDir+"/tl/src/test/resources/WP2_reannotation/"+clusterName+".txt", clusterAnnotationsDir, clusterName);
		
		}
	System.out.println(stat);
	}

}

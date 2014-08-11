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

import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;

public class GoldStandardToWP2FormatTranslator {
	protected int nodeNum;
	protected int edgeNum;
	
		public GoldStandardToWP2FormatTranslator() {
		super();
		nodeNum=0;
		edgeNum=0;
	}
		
	public int getNodeNum() {
			return nodeNum;
		}

	public int getEdgeNum() {
			return edgeNum;
		}

	private String getWP2edge(EntailmentRelation edge, Map<String,Set<String>> textToIdsMap){
/*		Output example
		<edge target="413572.txt_1_0" source="112459.txt_1_2" id="413572.txt_1_0-112459.txt_1_2">
		<entailment_mod_insensitive> </entailment_mod_insensitive>
		<entailment type="direct">yes</entailment>
		</edge>
*/		
		String s="";
		String targetText = edge.getTarget().getText();
		String sourceText = edge.getSource().getText();
	//	System.out.print("Edge: "+edge.toString()+"\t");
		for (String tgtId : textToIdsMap.get(targetText)){
			for (String srcId: textToIdsMap.get(sourceText)){
				if (srcId.equals(tgtId)) continue; // do not add edges with same src and tgt ids
				s += "\t<edge target=\""+tgtId+"\" source=\""+srcId+"\" id=\""+tgtId+"-"+srcId+"\">\n";
				s += "\t\t<entailment_mod_insensitive> </entailment_mod_insensitive>\n";
				if (edge.getEdgeType().is(EdgeType.TRANSITIVE_CLOSURE)) s+= "\t\t<entailment type=\"clousure\">yes</entailment>\n";
				else s+= "\t\t<entailment type=\"direct\">yes</entailment>\n";
				s+="\t</edge>\n";
				edgeNum++;
			}
		}			
	//	System.out.println(String.valueOf(i)+"\twp2 edges.");
		return s;
	}
	
	private boolean createWP2xml(File annotationFile, File newFile, GoldStandardEdgesLoader gsloader){
		boolean hasEdges = true;
		try {
			String s = "";
			BufferedReader reader = new BufferedReader(new FileReader(annotationFile));
			String line = reader.readLine();
			while(!line.contains("<edge ")) { // copy node lines, and stop as soon as reach edges
				s+=line+"\n";							
				line = reader.readLine();
				if (line.contains("</F_entailment_graph>")) {
					hasEdges=false;
					break; 
				}
			}
			reader. close();
			
			if (hasEdges){
				// add all edges from the graph
				EntailmentGraphRaw r = gsloader.getRawGraph();
				r.applyTransitiveClosure(true);
				
				// build a mapping from text to all its ids
				Map<String,Set<String>> textToIdsMap = new HashMap<String, Set<String>>();
				for (String id : gsloader.getNodeTextById().keySet()){
					String text = gsloader.getNodeTextById().get(id);
					Set<String> idsOfText = new HashSet<String>();
					if (textToIdsMap.containsKey(text)) idsOfText = textToIdsMap.get(text);
					idsOfText.add(id);
					textToIdsMap.put(text, idsOfText);
				}
				
				// add edges to the file
				for (EntailmentRelation edge : r.edgeSet()){
					s+=getWP2edge(edge, textToIdsMap);
				}			
			}
			
			s+="</F_entailment_graph>\n";
			BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
			writer.write(s);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean createWP2xml(String xmlFilename, EntailmentGraphRaw graph, Map<String,Set<String>> textToIdsMap, Map<String,String> nodeContentById){
		
		try {
			String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<F_entailment_graph problem=\"no\" nodeNum=\""+String.valueOf(graph.vertexSet().size())+"\">\n";
			//create node lines
			for (EntailmentUnit node : graph.vertexSet()){
				for (String id : textToIdsMap.get(node.getText())){
					s+=nodeContentById.get(id);
					nodeNum++;
				}
			}
			
			if (!graph.edgeSet().isEmpty()){ // if graph has edges
				// add all edges from the graph
				
				// graph.applyTransitiveClosure(true); // un-comment if want transitive closure edges marked as "closure"
				
				// add edges to the file
				for (EntailmentRelation edge : graph.edgeSet()){
					s+=getWP2edge(edge, textToIdsMap);
				}			
			}
			
			s+="</F_entailment_graph>\n";
			BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFilename));
			writer.write(s);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void createWP2Data(String gsAnnotationsDir){
		File gsDir = new File(gsAnnotationsDir);
		for(String clusterAnnotationDir: gsDir.list()){
			try {
				File f = new File(gsAnnotationsDir+"/"+clusterAnnotationDir);
				if (f.isDirectory()){
				//	System.out.println(f.getName().toUpperCase());
					GoldStandardEdgesLoader gsloader = new GoldStandardEdgesLoader(false); //load the original data only		
					gsloader.setExcludeSelfLoops(false); // do not exclude self-loops (edges between nodes with the same text are present in the GS, while in our graph these nodes become a single nodes and such edges are self-loops)
					// load merge-graph annotations	
					// clusterAnnotationDir should contain a folder called "FinalMergedGraph" with a single xml file with annotations
					File clusterAnnotationMergedGraphDir = new File (gsAnnotationsDir+"/"+clusterAnnotationDir+"/"+"FinalMergedGraph");
				//	System.out.println(clusterAnnotationMergedGraphDir.getAbsolutePath());
					if (clusterAnnotationMergedGraphDir.isDirectory()){
						for (File annotationFile : clusterAnnotationMergedGraphDir.listFiles()){
							if (gsloader.isValidMergedFile(annotationFile.getName())){
								gsloader.addAnnotationsFromFile(annotationFile.getPath(), false);
								// System.out.println(f.getName().toUpperCase()+" GS LOADED");
								if (createWP2xml(annotationFile, new File(gsAnnotationsDir+"/"+clusterAnnotationDir+"/FinalMergedGraph/"+annotationFile.getName().replace(".xml", "PlusClosure.xml")), gsloader)){
								//	System.out.println(f.getName().toUpperCase()+" CLOSURE XML IS CREATED");									
								}
							//	else System.out.println(f.getName().toUpperCase()+" CLOSURE XML COULD NOT BE CREATED");
							}
						}
					}	
				}
			} catch (GraphEvaluatorException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	//	String gsAnnotationsDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ITA-SPLIT-2014-03-14-FINAL/Test";
	//	String gsAnnotationsDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-TMP/Dev";

	//	String gsAnnotationsDir = "C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-FINAL/Test";
		String gsAnnotationsDir = "C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ITA-SPLIT-2014-03-14-FINAL/Dev";
		
		GoldStandardToWP2FormatTranslator t = new GoldStandardToWP2FormatTranslator();
		t.createWP2Data(gsAnnotationsDir);
	}

}

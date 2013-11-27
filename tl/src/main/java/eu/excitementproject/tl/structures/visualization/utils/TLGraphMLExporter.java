package eu.excitementproject.tl.structures.visualization.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.transform.TransformerConfigurationException;

import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.AbstractGraph;
import org.xml.sax.SAXException;


import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.rawgraph.utils.TEDecisionWithConfidence;

public class TLGraphMLExporter {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void exportGraph(AbstractGraph graph, String filename) {
		try {
						
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			
			GraphNodesNameProvider nodeName = new GraphNodesNameProvider();
			IntegerNameProvider nodeId = new IntegerNameProvider();
			
			GraphEdgeNameProvider edgeName = new GraphEdgeNameProvider();
			IntegerEdgeNameProvider edgeId = new IntegerEdgeNameProvider();
			
			GraphMLExporter gmex = new GraphMLExporter(nodeId, nodeName, edgeId, edgeName);
			
			gmex.export(writer, addRootNode(graph));
			
		} catch (IOException | TransformerConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * For graphs that are disjoint (the merged/raw and the collapsed one, we need to add a root node to be able to display it with the prefuse library 
	 * @param graph -- an abstract graph created in the transduction layer
	 * @return the same type of graph as the argument, but possibly with an added root node, and some new edges
	 */
	@SuppressWarnings("rawtypes")
	private static AbstractGraph addRootNode(AbstractGraph graph) {
		
//		if (graph.getClass() == FragmentGraph.class)
//			return graph;
		
		if (graph.getClass() == EntailmentGraphRaw.class)
			return addRootNode((EntailmentGraphRaw) ((EntailmentGraphRaw) graph).clone(), new EntailmentUnit(new EntailmentUnitMention("ROOT", 0, "none"), "ROOT"));
		
		if (graph.getClass() == EntailmentGraphCollapsed.class)
			return addRootNode((EntailmentGraphCollapsed) ((EntailmentGraphCollapsed) graph).clone(), new EquivalenceClass(new EntailmentUnit(new EntailmentUnitMention("ROOT", 0, "none"), "ROOT")));
		
		return graph;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static AbstractGraph addRootNode(AbstractBaseGraph graph, Object root) {
		
		graph.addVertex(root);
		Boolean edgeAdded = false;
		
		for(Object node: graph.vertexSet()) {
			if ((! node.equals(root)) && graph.inDegreeOf(node) == 0) {
//				graph.addEdge(root, node);
				addGraphEdge(graph, root, node);
				edgeAdded = true;
			}
		}
		
		if (!edgeAdded) {
			System.out.println("ROOT node not necessary -- graph is already connected");
			graph.removeVertex(root);
		}
		
		return graph;
	}
	

	@SuppressWarnings("rawtypes")
	private static void addGraphEdge(AbstractBaseGraph graph, Object source, Object target) {
		
		if (graph.getClass() == EntailmentGraphRaw.class) {
			((EntailmentGraphRaw) graph).addEdge((EntailmentUnit) source, (EntailmentUnit) target, 
						new EntailmentRelation((EntailmentUnit) source, (EntailmentUnit) target, new TEDecisionWithConfidence(1.0,DecisionLabel.Entailment), EdgeType.INDUCED));
			System.out.println("ADDED EDGE: " + ((EntailmentUnit) source).getTextWithoutDoulbeSpaces() + " / " + ((EntailmentUnit) target).getTextWithoutDoulbeSpaces());
		} else if (graph.getClass() == EntailmentGraphCollapsed.class) {
			((EntailmentGraphCollapsed) graph).addEdge((EquivalenceClass) source, (EquivalenceClass) target,
						new EntailmentRelationCollapsed((EquivalenceClass) source, (EquivalenceClass) target, 1.0));
			System.out.println("ADDED EDGE: " + ((EquivalenceClass) source).getLabel() + " / " + ((EquivalenceClass) target).getLabel());
		} else {
			System.out.println("Edge not added because of unknown graph type: " + graph.getClass().getCanonicalName());
		}
		
	}
	
	
	public static void main(String[] argv) {
		
		FragmentGraph fg = FragmentGraph.getSampleGraph();
		String filename = "./src/test/outputs/testMLexporter.txt";
		
		TLGraphMLExporter.exportGraph(fg, filename);		
	}
}

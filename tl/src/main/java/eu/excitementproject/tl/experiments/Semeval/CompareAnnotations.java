package eu.excitementproject.tl.experiments.Semeval;

import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;

public class CompareAnnotations {

	public static void main(String[] args) {
		try {
			
			
			GoldStandardEdgesLoader gslBefore = new GoldStandardEdgesLoader(true);
			gslBefore.setExcludeSelfLoops(false);
			gslBefore.loadAllAnnotations("C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/beforeReannotationNICE", false);
//			gslBefore.loadAllAnnotations("C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/beforeReannotationALMA", true);
			Set<EntailmentRelation> before = gslBefore.getEdges();

			GoldStandardEdgesLoader gslAfter = new GoldStandardEdgesLoader(true);
			gslAfter.setExcludeSelfLoops(false);
			gslAfter.loadAllAnnotations("C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/NICE_EMAIL_reAnnotated/all", false);
//			gslAfter.loadAllAnnotations("C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/ALMA_reAnnotated/all", true);
			Set<EntailmentRelation> after = new HashSet<EntailmentRelation>(gslAfter.getEdges());
			
			
			Set<EntailmentRelation> allEdges = new HashSet<EntailmentRelation>();
			Set<String> allEdgeStrings = new HashSet<String>();
			Set<String> allNodes = new HashSet<String>();
			Set<String> beforeEdgeStrings = new HashSet<String>();
			Set<String> afterEdgeStrings = new HashSet<String>();
			Set<String> beforeNodeStrings = new HashSet<String>();
			Set<String> afterNodeStrings = new HashSet<String>();

			for (EntailmentRelation e : before){
				allEdgeStrings.add(getString(e));
				allEdges.add(e);
				beforeEdgeStrings.add(getString(e));
				allNodes.add(e.getSource().getText());
				allNodes.add(e.getTarget().getText());
				beforeNodeStrings.add(e.getSource().getText());
				beforeNodeStrings.add(e.getTarget().getText());
			}
			for (EntailmentRelation e : after){
				allEdgeStrings.add(getString(e));
				allEdges.add(e);
				afterEdgeStrings.add(getString(e));
				allNodes.add(e.getSource().getText());
				allNodes.add(e.getTarget().getText());
				afterNodeStrings.add(e.getSource().getText());
				afterNodeStrings.add(e.getTarget().getText());
			}
			
			Set<String> commonNodes = new HashSet<String>(beforeNodeStrings);
			commonNodes.retainAll(afterNodeStrings);
			Set<String> commonEdges = new HashSet<String>(beforeEdgeStrings);
			commonEdges.retainAll(afterEdgeStrings);
			
			System.out.println("EDGES: Before: "+ before.size()+" edges, after: "+ after.size()+" edges , together: "+ allEdges.size()+" distinct edges");
			System.out.println("EDGES: Before: "+ beforeEdgeStrings.size()+" edges, after: "+ afterEdgeStrings.size()+" edges , together: "+ allEdgeStrings.size()+" distinct edges");
			
			int agree = commonEdges.size();
			System.out.println("Agree (edges present both before and after): "+agree);
			System.out.println("Edges present before, but not after: "+(beforeEdgeStrings.size()-agree));
			System.out.println("Edges present after, but not before: "+(afterEdgeStrings.size()-agree));
			System.out.println("Disagree: "+(allEdgeStrings.size()-agree));
			
			System.out.println("NODES: Before: "+ beforeNodeStrings.size()+" nodes, after: "+ afterNodeStrings.size()+" nodes , together: "+ allNodes.size()+" distinct nodes, common nodes: "+commonNodes.size());
			
			agree=0;
			for (EntailmentRelation e : allEdges){
				boolean edgeForCommonNodes = true;
				if (!commonNodes.contains(e.getSource().getText())) edgeForCommonNodes = false;
				if (!commonNodes.contains(e.getTarget().getText())) edgeForCommonNodes = false;
				
				if (!edgeForCommonNodes){
					beforeEdgeStrings.remove(getString(e));
					afterEdgeStrings.remove(getString(e));
				}				
			}
			commonEdges = new HashSet<String>(beforeEdgeStrings);
			commonEdges.retainAll(afterEdgeStrings);
			Set<String> allEdgesForCommonNodes = new HashSet<String>(beforeEdgeStrings);
			allEdgesForCommonNodes.addAll(afterEdgeStrings);

			agree = commonEdges.size();
			System.out.println("EDGES BETWEEN COMMON NODES: Before: "+ beforeEdgeStrings.size()+" edges, after: "+ afterEdgeStrings.size()+" edges , together: "+ allEdgesForCommonNodes.size()+" distinct edges");
			System.out.println("Agree (edges present both before and after): "+agree);
			System.out.println("Edges present before, but not after: "+(beforeEdgeStrings.size()-agree));
			System.out.println("Edges present after, but not before: "+(afterEdgeStrings.size()-agree));
			
		} catch (GraphEvaluatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static String  getString(EntailmentRelation e){
		return e.getSource().getText()+"-> "+e.getTarget().getText();//+" "+e.getEdgeType();
		//return e.toString();
	}

}

package eu.excitementproject.tl.experiments.Semeval;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import eu.excitementproject.tl.evaluation.exceptions.GraphEvaluatorException;
import eu.excitementproject.tl.evaluation.graphmerger.GoldStandardEdgesLoader;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;

public class CompareAnnotationsPerCluster {

	public static void main(String[] args) {
		try {
			String res = ("Cluster \t Before edges \t After edges \t Together distinct edges \t "
					+ "Agree edges \t Only before edges \t Only after edges \t Disagree edges \t "
					+ "Before nodes \t After nodes \t Together distinct nodes \t Common nodes \t"
					+ "Edges between common nodes before \t Edges between common nodes after \t Edges between common nodes together (distinct) \t"
					+ "Agree common-node edges \t Only before common-node edges \t Only after common-node edges \n");

//			String dirBeforeReannotation = "C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/beforeReannotationNICE";
			String dirBeforeReannotation = "C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/beforeReannotationALMA";

			for (String cluster : new File(dirBeforeReannotation).list()){
				if (new File(dirBeforeReannotation+"/"+cluster).isDirectory()){
					GoldStandardEdgesLoader gslBefore = new GoldStandardEdgesLoader(true);
					gslBefore.setExcludeSelfLoops(false);

					gslBefore.loadClusterAnnotations(dirBeforeReannotation+"/"+cluster, false);
					Set<EntailmentRelation> before = gslBefore.getEdges();

					GoldStandardEdgesLoader gslAfter = new GoldStandardEdgesLoader(true);
					gslAfter.setExcludeSelfLoops(false);
//					gslAfter.loadClusterAnnotations("C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/NICE_reAnnotated/all/"+cluster, false);
					gslAfter.loadClusterAnnotations("C:/Users/Lili/Git/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/ALMA_reAnnotated/all/"+cluster, false);
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

					
					//		res +=(before.size()+" \t "+ after.size()+" \t "+ allEdges.size()+" \t ");
					res +=(cluster+"\t");
					res +=(beforeEdgeStrings.size()+" \t "+ afterEdgeStrings.size()+" \t "+ allEdgeStrings.size()+" \t");
					
					int agree = commonEdges.size();
					res +=(agree+"\t");
					res +=((beforeEdgeStrings.size()-agree)+"\t");
					res +=((afterEdgeStrings.size()-agree)+"\t");
					res +=((allEdgeStrings.size()-agree)+"\t");
					
					res +=(beforeNodeStrings.size()+" \t "+ afterNodeStrings.size()+" \t "+ allNodes.size()+" \t "+commonNodes.size()+"\t");
					
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
					res +=(beforeEdgeStrings.size()+" \t "+ afterEdgeStrings.size()+" \t "+ allEdgesForCommonNodes.size()+" \t");
					res +=(agree+"\t");
					res +=((beforeEdgeStrings.size()-agree)+"\t");
					res +=((afterEdgeStrings.size()-agree+"\n"));
					
				}
			}
			
			System.out.println(res);
			
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

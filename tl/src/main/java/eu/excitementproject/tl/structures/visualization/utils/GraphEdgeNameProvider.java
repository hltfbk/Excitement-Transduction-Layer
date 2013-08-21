package eu.excitementproject.tl.structures.visualization.utils;

import org.jgrapht.ext.EdgeNameProvider;

import eu.excitementproject.tl.structures.collapsedgraph.EntailmentRelationCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphEdge;
import eu.excitementproject.tl.structures.rawgraph.EntailmentRelation;

@SuppressWarnings("rawtypes")
public class GraphEdgeNameProvider implements EdgeNameProvider {

	@Override
	public String getEdgeName(Object edge) {
		if (edge.getClass() == FragmentGraphEdge.class) {
//			return ((FragmentGraphEdge) edge).getWeight()+" ";
			return "Fragment_entailment";
		}
		
		if (edge.getClass() == EntailmentRelation.class) {
			return ((EntailmentRelation) edge).getLabel().name();
		}
		
		if (edge.getClass() == EntailmentRelationCollapsed.class) {
			return ((EntailmentRelationCollapsed) edge).getConfidence()+"";
		}
		
		return "NO_RELATION";
	}

}

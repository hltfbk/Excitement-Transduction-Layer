package eu.excitementproject.tl.structures.visualization.utils;

import org.jgrapht.ext.VertexNameProvider;

import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

@SuppressWarnings("rawtypes")
public class GraphNodesNameProvider implements VertexNameProvider {

	@Override
	public String getVertexName(Object vertex) {
		if (vertex.getClass() == EntailmentUnitMention.class) {
			return ((EntailmentUnitMention) vertex).getTextWithoutDoubleSpaces();
		}
		
		if (vertex.getClass() == EntailmentUnit.class) {
			return ((EntailmentUnit) vertex).getText() ;
		}
		
		if (vertex.getClass() == EquivalenceClass.class) {
			return ((EquivalenceClass) vertex).getLabel() ;
		}
		
		return "UNKNOWN vertex class";
	}
}

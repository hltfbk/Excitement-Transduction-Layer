package composition;

import org.apache.uima.jcas.JCas;

import decomposition.entities.ClassifiedEntailmentUnit;
import decomposition.entities.EntailmentUnit;
import decomposition.entities.TextualInput;

/**
 * This class implements the annotation of categories on a Textual Input CAS, based on entailment relations extracted from 
 * an existing entailment graph. This is a major part of use case 2.
 * 
 * TO BE IMPLEMENTED BY DFKI
 * 
 * @author Kathrin
 *
 */

public class CategoryAnnotator {
	
	private EntailmentGraphCollapsed graph;
	
	public CategoryAnnotator(EntailmentGraphCollapsed graph) {
		this.graph = graph;
	}
	
	public ClassifiedEntailmentUnit categorize(EntailmentUnit unit) {
		//compute categories and confidence based on graph
		return null;
	}
	
	public JCas addCategoryAnnotation(TextualInput in) {
		//decompose textual input
		//for each entailment unit: categorize it based on graph
		//add category annotations to CAS
		return null;	
	}

	public EntailmentGraphCollapsed getGraph() {
		return graph;
	}

	public void setGraph(EntailmentGraphCollapsed graph) {
		this.graph = graph;
	}

}

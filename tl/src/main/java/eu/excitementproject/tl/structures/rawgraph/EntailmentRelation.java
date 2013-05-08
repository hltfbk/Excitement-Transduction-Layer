package eu.excitementproject.tl.structures.rawgraph;

import org.apache.uima.jcas.JCas;
import org.jgrapht.graph.DefaultEdge;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.exception.ComponentException;


/**
 * 
 * @author vivi@fbk & LiliKotlerman
 *
 * Edge type for the work graph (EntailmentGraphRaw)
 * The edge "value" is a textual entailment decision (TEdecision) obtained from
 * the EDA.
 * 
 *  The class extends DefaultEdge:
 *  http://jgrapht.org/javadoc/org/jgrapht/graph/DefaultEdge.html
 *
 * @param <V>
 */
public class EntailmentRelation extends DefaultEdge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8223382210505322995L;
	
	EntailmentUnit source;
	EntailmentUnit target;
	
	
	/**
	 * The TEdecision object is produced by the EDA, and contains the label, confidence score, ...
	 */
	TEDecision edge;
	
	/*
	 * we might decide to have on each edge information about the configuration 
	 * (EDA, lexical resource, ...) that was used to produce this edge 
	 */
	EDABasic<?> eda;
	
	/*
	 * EOP lap
	 */
//	LAPAccess lap;
	
	public EntailmentRelation(EntailmentUnit source, EntailmentUnit target, EDABasic<?> eda) {
		this.source = source;
		this.target = target;	
		this.eda = eda;
		
		computeTEdecision();
		
	}
	
	/**
	 * Create an entailment relation in cases when TEDecision is known (don't specify the EDA)
	 * This might also be needed to encode the edges coming from fragment graphs and induced by transitivity
	 * ToDo: find out what to put into the eda when copying edges from a fragment graph or inducing  by transitivity
	 * @param source
	 * @param target
	 * @param edge
	 */
	public EntailmentRelation(EntailmentUnit source, EntailmentUnit target, TEDecision edge) {
		this.source = source;
		this.target = target;			
		this.edge = edge;	
	}
	
	/**
	 * Generates a dummy random entailment decision for this EntailmentReation 
	 */

	
	protected void computeTEdecision() {
	
	// Vivi's code below 
 //		JCas pairCAS = lap.generateSingleTHPairCAS(from.getText(), to.getText());
		JCas pairCAS = generateTHPairCAS();
		try {
			edge = eda.process(pairCAS);
		} catch (EDAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return -- a JCas object representing the text and hypothesis pair, 
	 *            obtained by extracting the necessary annotations from "from" and "to"
	 */
	protected JCas generateTHPairCAS(){
		// extract annotations from "from" and "to" to form the JCas object that is used as input to the EDA
		return null;
	}
	
	public TEDecision getTEdecision() {
		return edge;
	}
	
	/**
	 * 
	 * @return -- the confidence part of the TEdecision object
	 */
	public double getConfidence() {
		return edge.getConfidence();
	}
	
	public EntailmentUnit getSource() {
		return source;
	}

	public EntailmentUnit getTarget() {
		return target;
	}

	/**
	 * 
	 * @return -- the decision label from the TEdecision object
	 */
	public DecisionLabel getLabel() {
		return edge.getDecision();
	}
}

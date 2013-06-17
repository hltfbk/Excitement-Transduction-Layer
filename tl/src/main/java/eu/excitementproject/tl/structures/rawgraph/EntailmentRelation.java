package eu.excitementproject.tl.structures.rawgraph;

import java.util.logging.Logger;

import org.apache.uima.jcas.JCas;
import org.jgrapht.graph.DefaultEdge;

import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;


/**
 * 
 * @author vivi@fbk & Lili Kotlerman
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
	
	private static final long serialVersionUID = 8223382210505322995L;

	private static final Logger logger = Logger.getLogger(EntailmentRelation.class.toString());
	
	EntailmentUnit source;
	EntailmentUnit target;
	
	EdgeType edgeType; 	
	
	/*
	 * The TEdecision object is produced by the EDA, and contains the label, confidence score, ...
	 */
	TEDecision edge = null;
	
	/*
	 * we might decide to have on each edge information about the configuration 
	 * (EDA, lexical resource, ...) that was used to produce this edge 
	 */
	EDABasic<?> eda;
	
	/*
	 * EOP lap
	 */
	LAPAccess lap;
	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/
	
	/** Create an entailment relation by computing TEdecision from the input EDA
	 * @param source
	 * @param target
	 * @param eda
	 * @param lap
	 * @throws EntailmentGraphRawException
	 */
	public EntailmentRelation(EntailmentUnit source, EntailmentUnit target, EDABasic<?> eda, LAPAccess lap) throws EntailmentGraphRawException {
		setAttributes(source, target, EdgeType.EDA, eda, lap);
		try {
			computeTEdecision();
		} catch (LAPException e) {
			throw new EntailmentGraphRawException(e.getMessage()); 
		}
		
	}
		
	/**
	 * Create an entailment relation in cases when TEDecision is known (don't specify the EDA).
	 * This constructor is used when copying edges from a fragment graph or inducing from prior knowledge
	 * Assigns null to the eda and lap attributes 
	 * @param source
	 * @param target
	 * @param edge - the TEDecision
	 */
	public EntailmentRelation(EntailmentUnit source, EntailmentUnit target, TEDecision edge, EdgeType edgeType) {
		setAttributes(source, target, edgeType, null, null);
		this.edge = edge;
	}
	
	
	/******************************************************************************************
	 * SETTERS/GERRETS
	 * ****************************************************************************************/

	private void setAttributes(EntailmentUnit source, EntailmentUnit target, EdgeType edgeType, EDABasic<?> eda, LAPAccess lap){
		this.source = source;
		this.target = target;	
		this.edgeType = EdgeType.EDA;
		this.eda = eda;
		this.lap = lap;	
	}
	
	
	
	/******************************************************************************************
	 * OTHER AUXILIARY METHODS
	 * ****************************************************************************************/

	/******************************************************************************************
	 * PRINT
	 * ****************************************************************************************/

	/******************************************************************************************
	 * METHODS FOR INTERNAL TESTING PURPOSES
	 * ****************************************************************************************/

	public static EntailmentRelation GenerateRandomEntailmentRelation(EntailmentUnit source, EntailmentUnit target) {		
		RandomEDA eda = new RandomEDA();
		EntailmentRelation edge = new EntailmentRelation(source, target, computeRandomTEdecision(eda), EdgeType.EDA);
		return edge;
	}
	
	/**
	 * This constructor is only used for internal testing purposes
	 * Create an entailment relation in cases when TEDecision is known (don't specify the EDA) but edge type is not known
	 * @param source
	 * @param target
	 * @param edge
	 */
	public EntailmentRelation(EntailmentUnit source, EntailmentUnit target, TEDecision edge) {
		setAttributes(source, target, EdgeType.UNKNOWN, null, null);
		this.edge = edge;
	}	


	/**
	 * Generates a dummy random entailment decision for this EntailmentReation 
	 * @throws LAPException 
	 */
	
	protected static TEDecision computeRandomTEdecision(RandomEDA eda){		
		TEDecision edge = eda.process(null);
		return edge;
	}
	
	/**
	 * Generates a dummy random entailment decision for this EntailmentReation 
	 * @throws LAPException 
	 */

	
	protected void computeTEdecision() throws LAPException {
	
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
	 * @throws LAPException 
	 */
	protected JCas generateTHPairCAS() throws LAPException{
		// extract annotations from "from" and "to" to form the JCas object that is used as input to the EDA
		logger.info("Generating a cass for the pair: \n \tTEXT: " + source.getText() + "\n \tHYPOTHESIS: " + target.getText());
		return lap.generateSingleTHPairCAS(source.getTextWithoutDoulbeSpaces(), target.getTextWithoutDoulbeSpaces());
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
	
	@Override
	public EntailmentUnit getSource() {
		return source;
	}

	@Override
	public EntailmentUnit getTarget() {
		return target;
	}

	/**
	 * 
	 * @return -- the decision label from the TEdecision object
	 */
	public DecisionLabel getLabel() {
		if (edge != null)
			return edge.getDecision();
		return null;
	}

	
	/**
	 * @return -- the EDA that was used to generate the edge
	 */
	public EDABasic<?> getEda() {
		if (this.edgeType.is(EdgeType.EDA)) return eda;
		return null; // if the edge was not generated by an EDA, then return null;
	}

	@Override 
	public String toString(){
		return this.getSource().getText()+" --> "+this.getTarget().getText() +" ("+this.getLabel().toString()+", "+this.getConfidence()+") ";
	}
	
	public String toDOT(){
		String s = "\""+this.getSource().getTextWithoutDoulbeSpaces()+"\" -> \""+this.getTarget().getTextWithoutDoulbeSpaces()+"\"";
		s+= " [label="+this.getConfidence()+"]";
		String color = "red";
		if (this.getLabel().is(DecisionLabel.Entailment)) color="blue";
		if (this.edgeType.is(EdgeType.FRAGMENT_GRAPH)) color = "green";		
		s+= " [color="+color+"]";
		return s+"\n";
	}
			
}

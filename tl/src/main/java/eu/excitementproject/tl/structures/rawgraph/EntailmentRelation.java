package eu.excitementproject.tl.structures.rawgraph;



import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.jgrapht.graph.DefaultEdge;







import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.EDABasic;
import eu.excitementproject.eop.common.EDAException;
import eu.excitementproject.eop.common.TEDecision;
import eu.excitementproject.eop.common.exception.ComponentException;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphRawException;
import eu.excitementproject.tl.laputils.CachedLAPAccess;
import eu.excitementproject.tl.structures.rawgraph.utils.EdgeType;
import eu.excitementproject.tl.structures.rawgraph.utils.RandomEDA;
import eu.excitementproject.tl.structures.rawgraph.utils.TEDecisionWithConfidence;


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
	CachedLAPAccess lap;
	
	
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
	public EntailmentRelation(EntailmentUnit source, EntailmentUnit target, EDABasic<?> eda, CachedLAPAccess lap) throws EntailmentGraphRawException {
		setAttributes(source, target, EdgeType.EDA, eda, lap);
		computeTEdecision();
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

	private void setAttributes(EntailmentUnit source, EntailmentUnit target, EdgeType edgeType, EDABasic<?> eda, CachedLAPAccess lap){
		this.source = source;
		this.target = target;	
		this.edgeType = edgeType;
		this.eda = eda;
		this.lap = lap;
	}
		
	
	
	/**
	 * @return the lap
	 */
	public CachedLAPAccess getLap() {
		return lap;
	}

	/**
	 * @return the edgeType
	 */
	public EdgeType getEdgeType() {
		return edgeType;
	}


	public void setEdgeType(EdgeType edgeType) {
		this.edgeType = edgeType;
	}

	/**
	 * @return TEdecision
	 */
	public TEDecision getTEdecision() {
		return edge;
	}
	
	/**
	 * 
	 * @return -- the confidence part of the TEdecision object
	 */
	public Double getConfidence() {
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
	
	
	/******************************************************************************************
	 * OTHER AUXILIARY METHODS
	 * ****************************************************************************************/
	/**
	 * Computes TEdecision using this entailment relation's eda.
	 * For this JCAS representing the text and hypothesis pair is generated using the entailment relation's lap.   
	 * @throws EntailmentGraphRawException 
	 */
	protected void computeTEdecision() throws EntailmentGraphRawException {	
		JCas pairCAS;
		try {
			pairCAS = generateTHPairCAS();
			try {
				edge = eda.process(pairCAS);
				if (edge.getConfidence() > 0.5) {
					logger.info("EDA says : " +  edge.getDecision() + "(" + edge.getConfidence() + ")");
				}
			} catch (EDAException | ComponentException e) {
				throw new EntailmentGraphRawException(e.getMessage());
			} catch (RuntimeException rune) {
				logger.error("Cannot obtain EDA decision for edge: " + source.getText() + " -> " + target.getText() +"\n"+rune.getMessage());
				edge = new TEDecisionWithConfidence(0.0, DecisionLabel.Unknown);
			}
		} catch (RuntimeException rune1) {
			logger.error("Cannot generate THPairCAS for edge: " + source.getText() + " -> " + target.getText() +"\n"+rune1.getMessage());
			edge = new TEDecisionWithConfidence(0.0, DecisionLabel.Unknown);
		}
	}


	
	
	
	/**
	 * 
	 * @return -- a JCas object representing the text and hypothesis pair, 
	 *            obtained by extracting the necessary annotations from "from" and "to"
	 * @throws EntailmentGraphRawException 
	 */
	protected JCas generateTHPairCAS() throws EntailmentGraphRawException{
		// extract annotations from "from" and "to" to form the JCas object that is used as input to the EDA
		logger.info("Generating a cass for the pair: \n \tTEXT: " + source.getText() + "\n \tHYPOTHESIS: " + target.getText());
		try {
			lap.annotateSingleTHPairCAS(source.getTextWithoutDoubleSpaces(), target.getTextWithoutDoubleSpaces(), lap.workJCas);

/*			// some printouts trying to understand why BIUTEE LAP fails
			logger.info("generateTHPairCAS:   "+lap.workJCas.getDocumentLanguage());
			Pair pairAnno = JCasUtil.selectSingle(lap.workJCas, Pair.class);
			logger.info("generateTHPairCAS:   "+pairAnno);
			Text textAnno = pairAnno.getText();
			logger.info("generateTHPairCAS:   <<"+textAnno.getCoveredText()+">>");
			Hypothesis hypothesisAnno = pairAnno.getHypothesis();
			logger.info("generateTHPairCAS:   <<"+hypothesisAnno.getCoveredText()+">>");

			try {
				JCas textView = lap.workJCas.getView(LAP_ImplBase.TEXTVIEW);
				Sentence textSentence = JCasUtil.selectSingle(textView, Sentence.class);
				logger.info("generateTHPairCAS:   "+textSentence.getCoveredText());
				JCas hypothesisView = lap.workJCas.getView(LAP_ImplBase.HYPOTHESISVIEW);
				Sentence hypothesisSentence = JCasUtil.selectSingle(hypothesisView, Sentence.class);
				logger.info("generateTHPairCAS:   "+hypothesisSentence.getCoveredText());
			} catch (CASException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/			return lap.workJCas;
		} catch (LAPException e) {
			throw new EntailmentGraphRawException("Cannot generate THPairCAS for edge: " + source.getText() + " -> " + target.getText() +"\n"+e.getMessage());
		}
	}

	/******************************************************************************************
	 * COMPARATORS
	 * ****************************************************************************************/

	/**
	 * Comparator to sort equivalence classes in descending order by their number of interactions
	 */
	public static class DescendingConfidenceComparator implements Comparator<EntailmentRelation> {
	    @Override
	    public int compare(EntailmentRelation edgeA, EntailmentRelation edgeB) {
	        return -1*edgeA.getConfidence().compareTo(edgeB.getConfidence());
	    }
	}
	
	
	/******************************************************************************************
	 * PRINT
	 * ****************************************************************************************/
	@Override 
	public String toString(){
		return this.getSource().getText()+" --> "+this.getTarget().getText() +" ("+this.getLabel().toString()+", "+this.getEdgeType().toString()+", "+this.getConfidence()+") ";
	}
	
	/** Returns a string with the edge in DOT format for outputting the graph
	 * @return the generated string
	 */	public String toDOT(){
		String s = "\""+this.getSource().toDOT()+"\" -> \""+this.getTarget().toDOT()+"\"";
		s+= " [label="+this.getConfidence()+"]";
		String color = "red";
		if (this.getLabel().is(DecisionLabel.Entailment)) color="blue";
		if (this.edgeType.is(EdgeType.FRAGMENT_GRAPH)) color = "green";		
		if (this.getLabel().is(DecisionLabel.NonEntailment)) color = "red";		
		s+= " [color="+color+"]";
		return s+"\n";
	}
	
	 
	/******************************************************************************************
	 * METHODS FOR EVALUATION PURPOSES
	 * ****************************************************************************************/
	 /** 
	 * @param anotherEdge EntailmentRelation to compare with
	 * @return true if the edge's source and target texts are the same as that of anotherEdge
	 * Use getTextWithoutDoubleSpaces() method to get node's text, since gold standard fragment graphs hold node texts without double spaces
	 */
	public boolean isSameSourceAndTarget(EntailmentRelation anotherEdge){
		 if ((this.getSource().getTextWithoutDoubleSpaces().equals(anotherEdge.getSource().getTextWithoutDoubleSpaces())) && (this.getTarget().getTextWithoutDoubleSpaces().equals(anotherEdge.getTarget().getTextWithoutDoubleSpaces()))) return true;		 
		 return false;
	 }
	 
	protected void computeCosineDecision() throws EntailmentGraphRawException {			
		String[] src = source.text.split(" ");
		String[] tgt = target.text.split(" ");
		Map<String,Double> srcVec = new HashMap<String, Double>();
		Map<String,Double> tgtVec = new HashMap<String, Double>();
		for (String s : src){
			srcVec.put(s, 1.0);
		}
		for (String t : tgt){
			tgtVec.put(t, 1.0);
		}
		double cos = cosineSimilarity(srcVec, tgtVec);
		if (cos >= 0.3) 	edge = new TEDecisionWithConfidence(cos, DecisionLabel.Entailment);
		else 	edge = new TEDecisionWithConfidence(1-cos, DecisionLabel.NonEntailment);
	}
	 
	private static double cosineSimilarity(Map<String, Double> vectorA, Map<String, Double> vectorB){
		double similarity = 0.0;
		Set<String> commonKeys = new HashSet<String>(vectorA.keySet());
		commonKeys.retainAll(vectorB.keySet());
		if (commonKeys.isEmpty()) return 0.0; 
		
		for (String key : commonKeys){
			similarity+=(vectorA.get(key)*vectorB.get(key));
		}
						
		double cos = similarity/(magnitude(vectorA)*magnitude(vectorB));
		if (cos>1) return 1.0; // detected results like 1.00000000002, which make LingPipe crazy
		return cos;
	}

	private static double magnitude(Map<String, Double> vector){
		double magnitude = 0.0;
		for (String key : vector.keySet()){
			magnitude+=Math.pow(vector.get(key),2);
		}
		return Math.sqrt(magnitude);
	}

	/******************************************************************************************
	 * METHODS FOR INTERNAL TESTING PURPOSES
	 * ****************************************************************************************/

	/** Creates and returns an entailment relation between the given source and target nodes by producing a random entailment decision
	 * @param source
	 * @param target
	 * @return the generated "random" entailment relation
	 */
	public static EntailmentRelation generateRandomEntailmentRelation(EntailmentUnit source, EntailmentUnit target) {		
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
	
	
}

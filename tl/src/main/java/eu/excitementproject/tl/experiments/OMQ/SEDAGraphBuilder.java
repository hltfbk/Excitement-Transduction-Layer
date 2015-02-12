package eu.excitementproject.tl.experiments.OMQ;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import eu.excitementproject.eop.common.DecisionLabel;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalResourceException;
import eu.excitementproject.eop.core.component.lexicalknowledge.derivbase.DerivBaseResource;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetRelation;
import eu.excitementproject.eop.core.component.lexicalknowledge.germanet.GermaNetWrapper;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentGraphRaw;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;

/**
 *
 * SEDAGraphBuilder merges fragment graphs using Simple Entailment Algorithmus.
 * 
 * Simple Entailment Algorithmus returns ENTAILMENT if every token of the hypothesis can be mapped on any token of the text, 
 * otherwise the EDA returns NONENTAILMENT. This EDA cannot be trained, the decision and the confidence are based 
 * directly on the given input text-hypothesis-pair and the found matchings between their tokens.
 * SEDA uses lemmatizer, GermanWordSplitter, DerivBase and GermaNet to map the tokens.
 * 
 * SEDAGraphBuilder was written for UseCase2 evaluations with single-token and two-token fragments as TH pairs.
 * Simple Entailment Algorithmus is not suitable for sentence fragments as TH pairs.
 * 
 * @author Aleksandra Gabryszak
 *
 */

public class SEDAGraphBuilder {

	private GermanWordSplitter splitter;
	private DerivBaseResource derivBaseResource;
	private GermaNetWrapper germaNetWrapper;
	private List<GermaNetRelation> germaNetRelations;
	private boolean mapNegation;
	private boolean onlyBidirectionalEdges;
	private boolean applyTransitiveClosure;
	
	/**
	 * 
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germaNetRelations
	 * @param splitter
	 * @param mapNegation
	 * @param onlyBidirectionalEdges
	 */
	public SEDAGraphBuilder (DerivBaseResource derivBaseResource, 
			GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germaNetRelations, 
			GermanWordSplitter splitter, boolean mapNegation, boolean onlyBidirectionalEdges,
			boolean applyTransitiveClosure) {
		this.splitter = splitter;
		this.derivBaseResource = derivBaseResource;
		this.germaNetWrapper = germaNetWrapper;
		this.germaNetRelations = germaNetRelations;
		this.mapNegation = mapNegation;
		this.onlyBidirectionalEdges = onlyBidirectionalEdges;
		this.applyTransitiveClosure = applyTransitiveClosure;
	}
	
	/**
	 * Merge Set<FragmentGraph> into a token or dependency EntailmentGraphRaw
	 * 
	 * @param fragmentGraphs
	 * @param rawGraph
	 * @return
	 * @throws LexicalResourceException
	 */
	public EntailmentGraphRaw mergeIntoGraph(Set<FragmentGraph> fragmentGraphs, EntailmentGraphRaw rawGraph) throws LexicalResourceException{
		List<FragmentGraph> fgList = new LinkedList<FragmentGraph>(fragmentGraphs);
		Collections.sort(fgList, new FragmentGraph.CompleteStatementComparator());
		
		for(FragmentGraph fg : fgList) {
			rawGraph = mergeIntoGraph(fg, rawGraph);
		}
		if(applyTransitiveClosure){
			rawGraph.applyTransitiveClosure();
		}
		return rawGraph;
	}
	
	/**
	 * Merge FragmentGraph into a token or dependency EntailmentGraphRaw
	 * 
	 * @param fragmentGraph
	 * @param rawGraph
	 * @return
	 * @throws LexicalResourceException
	 */
	public EntailmentGraphRaw mergeIntoGraph(FragmentGraph fragmentGraph, EntailmentGraphRaw rawGraph) throws LexicalResourceException {
		//TODO: until now we don't use ModifierAnnotator for dependencies, this method doesn't consider FragmentGraphs with more levels
		for(EntailmentUnitMention eum : fragmentGraph.vertexSet()) {
			rawGraph.addEntailmentUnitMention(eum, fragmentGraph.getCompleteStatement().getTextWithoutDoubleSpaces());
			EntailmentUnit newStatement = rawGraph.getVertexWithText(eum.getTextWithoutDoubleSpaces());
			//direction new statement <--> graph statement
			addBidirectionalEdges(rawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			if(!onlyBidirectionalEdges) {
				//direction new statement --> graph statement 
				addOneDirectionalEntailedEdges(rawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
				//direction graph statement --> new statement
				addOneDirectionalEntailingEdges(rawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			}
		}
		return rawGraph;
	}
	
	/**
	 * Merge Set<FragmentGraph> into a single token EntailmentGraphRaw
	 * 
	 * @param fragmentGraphs
	 * @param singleTokenRawGraph
	 * @throws LexicalResourceException
	 */
	//TODO: REMOVE?, replaced by mergeIntoGraph
	/*
	public void mergeIntoLemmaTokenGraph(Set<FragmentGraph> fragmentGraphs, EntailmentGraphRaw singleTokenRawGraph) 
			throws LexicalResourceException{
		
		List<FragmentGraph> fgList = new LinkedList<FragmentGraph>(fragmentGraphs);
		Collections.sort(fgList, new FragmentGraph.CompleteStatementComparator());
		
		for(FragmentGraph fg : fgList) {
			mergeIntoLemmaTokenGraph(fg, singleTokenRawGraph);
		}
	}
	*/
	
	/**
	 * Merge one FragmentGraph into a single token EntailmentGraphRaw with only lemma related edges
	 * 
	 * @param fragmentGraph
	 * @param singleTokenRawGraph
	 * @throws LexicalResourceException
	 */
	//TODO: REMOVE?, replaced by mergeIntoGraph
	/*
	public void mergeIntoLemmaTokenGraph(FragmentGraph fragmentGraph, EntailmentGraphRaw singleTokenRawGraph) 
			throws LexicalResourceException {
		
		for(EntailmentUnitMention eum : fragmentGraph.vertexSet()){
			singleTokenRawGraph.addEntailmentUnitMention(eum, fragmentGraph.getCompleteStatement().getText());
			EntailmentUnit newStatement = singleTokenRawGraph.getVertexWithText(eum.getTextWithoutDoubleSpaces());
			
			String lemmatized = newStatement.getLemmatizedText();
			Set<EntailmentUnit> graphStatementSet = SEDAUtils.getLemmatizedVertex(singleTokenRawGraph, lemmatized, true);
			for(EntailmentUnit graphStatement : graphStatementSet)
			{
				if(!newStatement.getTextWithoutDoubleSpaces().equals(graphStatement.getTextWithoutDoubleSpaces()))
				{
					addBidirectionalEdges(singleTokenRawGraph, newStatement, null, null, null, null, false);
				}
			}
		}
	}
	*/

	/**
	 * Merge Set<FragmentGraph> into a single token EntailmentGraphRaw
	 * 
	 * @param fragmentGraphs
	 * @param singleTokenRawGraph
	 * @return
	 * @throws LexicalResourceException
	 */
	//TODO: REMOVE?, replaced by mergeIntoGraph
	/*
	public EntailmentGraphRaw mergeIntoTokenGraph(Set<FragmentGraph> fragmentGraphs, EntailmentGraphRaw singleTokenRawGraph) 
					throws LexicalResourceException {
		
		List<FragmentGraph> fgList = new LinkedList<FragmentGraph>(fragmentGraphs);
		Collections.sort(fgList, new FragmentGraph.CompleteStatementComparator());
		
		for(FragmentGraph fg : fgList) {
			singleTokenRawGraph = mergeIntoTokenGraph(fg, singleTokenRawGraph);
		}
		return singleTokenRawGraph;
	}
	*/
	
	/**
	 * Merge FragmentGraph into a single token EntailmentGraphRaw
	 * 
	 * @param fragmentGraph
	 * @param singleTokenRawGraph
	 * @return
	 * @throws LexicalResourceException
	 */
	//TODO: REMOVE?, replaced by mergeIntoGraph
	/*
	public EntailmentGraphRaw mergeIntoTokenGraph(FragmentGraph fragmentGraph, EntailmentGraphRaw singleTokenRawGraph) 
			throws LexicalResourceException {
		boolean mapNegation = false;
		for(EntailmentUnitMention eum : fragmentGraph.vertexSet()) {
			singleTokenRawGraph.addEntailmentUnitMention(eum, fragmentGraph.getCompleteStatement().getTextWithoutDoubleSpaces());
			EntailmentUnit newStatement = singleTokenRawGraph.getVertexWithText(eum.getTextWithoutDoubleSpaces());
			//direction new statement <--> graph statement
			addBidirectionalEdges(singleTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			if(!onlyBidirectionalEdges) {
				//direction new statement --> graph statement 
				addOneDirectionalEntailedEdges(singleTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
				//direction graph statement --> new statement
				addOneDirectionalEntailingEdges(singleTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			}
		}
		return singleTokenRawGraph;
	}
	*/
	
	/**
	 * Merge Set<FragmentGraph> into a two token EntailmentGraphRaw
	 * 
	 * @param fragmentGraphs
	 * @param twoTokenRawGraph
	 * @return
	 * @throws LexicalResourceException
	 */
	//TODO: REMOVE?, replaced by mergeIntoGraph
	/*
	public EntailmentGraphRaw mergeIntoDependencyGraph(Set<FragmentGraph> fragmentGraphs, EntailmentGraphRaw twoTokenRawGraph)  
			throws LexicalResourceException{
		List<FragmentGraph> fgList = new LinkedList<FragmentGraph>(fragmentGraphs);
		Collections.sort(fgList, new FragmentGraph.CompleteStatementComparator());
		for(FragmentGraph fg : fgList) {
			twoTokenRawGraph = mergeIntoDependencyGraph(fg, twoTokenRawGraph);
		}
		return twoTokenRawGraph;
	}
	*/
	
	/**
	 * Merge one FragmentGraph into a two token EntailmentGraphRaw
	 * 
	 * @param fragmentGraph
	 * @param twoTokenRawGraph
	 * @return
	 * @throws LexicalResourceException
	 */
	//TODO: REMOVE?, replaced by mergeIntoGraph
	/*
	public EntailmentGraphRaw mergeIntoDependencyGraph(FragmentGraph fragmentGraph, EntailmentGraphRaw twoTokenRawGraph) 
			throws LexicalResourceException {
		for(EntailmentUnitMention eum : fragmentGraph.vertexSet()) {
			twoTokenRawGraph.addEntailmentUnitMention(eum, fragmentGraph.getCompleteStatement().getTextWithoutDoubleSpaces());
			EntailmentUnit newStatement = twoTokenRawGraph.getVertexWithText(eum.getTextWithoutDoubleSpaces());
			//direction new statement <--> graph statement
			addBidirectionalEdges(twoTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			if(!onlyBidirectionalEdges) {
				//direction new statement --> graph statement 
				addOneDirectionalEntailedEdges(twoTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
				//direction graph statement --> new statement
				addOneDirectionalEntailingEdges(twoTokenRawGraph, newStatement, derivBaseResource, germaNetWrapper, germaNetRelations, splitter, mapNegation);
			}
		}
		return twoTokenRawGraph;
	}
	*/
	
	/**
	 * Add bidirectional entailment edges going from and to the input EntailmentUnit
	 * 
	 * @param rawGraph
	 * @param inputEntailmentUnit
	 * @param dbr
	 * @param germaNetWrapper
	 * @param germanetRelations
	 * @param splitter
	 * @param mapNegation
	 * @throws LexicalResourceException
	 */
	private void addBidirectionalEdges(EntailmentGraphRaw rawGraph, EntailmentUnit inputEntailmentUnit, DerivBaseResource dbr, 
			GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germanetRelations, GermanWordSplitter splitter, boolean mapNegation)
					throws LexicalResourceException {
		LinkedList<GermaNetRelation> germanetRelationsModified = new LinkedList<GermaNetRelation> ();
		if(germanetRelations != null){
			germanetRelationsModified = new LinkedList<GermaNetRelation>(germanetRelations);
			if(germanetRelationsModified.size() > 0){
				germanetRelationsModified.remove(GermaNetRelation.has_hyponym);
				germanetRelationsModified.remove(GermaNetRelation.has_hypernym);
				germanetRelationsModified.remove(GermaNetRelation.causes);
				germanetRelationsModified.remove(GermaNetRelation.has_antonym);
				germanetRelationsModified.remove(GermaNetRelation.entails);
			}
		}
		
		addTEEdges(rawGraph, inputEntailmentUnit, dbr, germaNetWrapper, germanetRelationsModified, null, mapNegation, "both");
	}

	/**
	 * Add entailment edges going from the input EntailmentUnit
	 * 
	 * @param rawGraph
	 * @param inputEntailmentUnit
	 * @param dbr
	 * @param germaNetWrapper
	 * @param germanetRelations
	 * @param splitter
	 * @param mapNegation
	 * @throws LexicalResourceException
	 */
	private void addOneDirectionalEntailedEdges(EntailmentGraphRaw rawGraph, EntailmentUnit inputEntailmentUnit, DerivBaseResource dbr, 
			GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germanetRelations, GermanWordSplitter splitter, boolean mapNegation)
					throws LexicalResourceException {
		LinkedList<GermaNetRelation> germanetRelationsModified = new LinkedList<GermaNetRelation>();
		if(germanetRelations != null){
			germanetRelationsModified = new LinkedList<GermaNetRelation>(germanetRelations);
			if(germanetRelationsModified.size() > 0){
				germanetRelationsModified.remove(GermaNetRelation.has_hyponym);
				germanetRelationsModified.remove(GermaNetRelation.has_antonym);
			}
		}
		addTEEdges(rawGraph, inputEntailmentUnit, dbr, germaNetWrapper, germanetRelationsModified, splitter, mapNegation, "inputToGraph");
	}
	
	/**
	 * Add entailment edges going to the input EntailmentUnit
	 * 
	 * @param rawGraph
	 * @param inputEntailmentUnit
	 * @param derivBaseResource
	 * @param germaNetWrapper
	 * @param germanetRelations
	 * @param splitter
	 * @param mapNegation
	 * @throws LexicalResourceException
	 */
	private void addOneDirectionalEntailingEdges(EntailmentGraphRaw rawGraph, EntailmentUnit inputEntailmentUnit, DerivBaseResource derivBaseResource, 
			GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germanetRelations, GermanWordSplitter splitter, boolean mapNegation)
					throws LexicalResourceException {
		List<GermaNetRelation> germanetRelationsModified = new LinkedList<GermaNetRelation>();
		germanetRelationsModified.add(GermaNetRelation.has_hyponym);
		germanetRelationsModified.add(GermaNetRelation.has_synonym);
		addTEEdges(rawGraph, inputEntailmentUnit, derivBaseResource, germaNetWrapper, germanetRelationsModified, null, mapNegation, "graphToInput");
	}
	
	/**
	 * Add edges going from or to the input EntailmentUnit depending on the given direction
	 * 
	 * @param rawGraph
	 * @param inputEntailmentUnit
	 * @param dbr
	 * @param germaNetWrapper
	 * @param germanetRelations
	 * @param splitter
	 * @param mapNegation
	 * @param direction
	 * @throws LexicalResourceException
	 */
	private void addTEEdges(EntailmentGraphRaw rawGraph, EntailmentUnit inputEntailmentUnit, 
			DerivBaseResource dbr, GermaNetWrapper germaNetWrapper, List<GermaNetRelation> germanetRelations, GermanWordSplitter splitter, boolean mapNegation, String direction) 
							throws LexicalResourceException {
		
		Set<String> permutatedTextSet = SEDAUtils.getRelatedText(inputEntailmentUnit.getTextWithoutDoubleSpaces(), inputEntailmentUnit.getLemmatizedText(), 
				dbr, germaNetWrapper, germanetRelations, splitter, mapNegation);
		
		Set<EntailmentUnit> graphEUSet = SEDAUtils.getLemmatizedVertex(rawGraph, permutatedTextSet, true);
		for(EntailmentUnit graphEU : graphEUSet){
			if(!inputEntailmentUnit.getTextWithoutDoubleSpaces().equals(graphEU.getTextWithoutDoubleSpaces()))
			{ 
				if(direction.equalsIgnoreCase("both")){
					if(!rawGraph.containsEdge(inputEntailmentUnit, graphEU)){
						rawGraph.addEdgeByInduction(inputEntailmentUnit, graphEU, DecisionLabel.Entailment, 0.91);
					}
					if(!rawGraph.containsEdge(graphEU, inputEntailmentUnit)){
						rawGraph.addEdgeByInduction(graphEU, inputEntailmentUnit, DecisionLabel.Entailment, 0.91);
					}
				}
				
				else if(direction.equalsIgnoreCase("inputToGraph")){
					if(!rawGraph.containsEdge(inputEntailmentUnit, graphEU)){
						rawGraph.addEdgeByInduction(inputEntailmentUnit, graphEU, DecisionLabel.Entailment, 0.91);
					}
				}
				
				else if(direction.equalsIgnoreCase("graphToInput")){
					if(!rawGraph.containsEdge(graphEU, inputEntailmentUnit)){
						rawGraph.addEdgeByInduction(graphEU, inputEntailmentUnit, DecisionLabel.Entailment, 0.91);
					}
				}
			}
		}
	}

}

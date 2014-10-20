package eu.excitementproject.clustering.data.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import eu.excitementproject.eop.common.datastructures.Pair;
import eu.excitementproject.eop.common.datastructures.PairMap;

/**
 * @author Lili Kotlerman
 *
 */
public abstract class AbstractTextCollection implements TextCollection {

	protected String m_configurationFileName = null;
	protected String m_inputFile=null;
	
	protected  Map<String, List<Integer>> m_docIdsByText = null;
	protected  Map<Integer, String> m_docTextByDocId = null;	
	
	protected  Map<String, Map<Integer,Double>> m_docIdsByOriginalTerm = null; // [term] [docId, freq]
	protected  Set<String> m_originalTermsForExpansion = null; 
	protected  Map<Integer, Map<String,Double>> m_originalTermsByDocId = null; // [docId] [term, freq]
	
	protected  Map<String, Map<Integer,Double>> m_docIdsByExpansionTerm = null; // [term] [docId, confidence]
	protected  Map<Integer, Map<String,Double>> m_expansionTermsByDocId = null; // [docId] [term, confidence]
	
	protected  Map<String, List<Integer>> m_docIdsPerGoldCluster = null;
	protected  Map<Integer, List<String>> m_goldClustersPerDocId = null;
	
	protected PairMap<String, Double> m_termSemanticRelatednessMap = null; // [(term, term)] [relatedness confidence] 
	
/*	*//**
	 * This map shows in how many different documents (original texts) each term occurs. The map should also contain the expansions, which did not occur in any original text, with zero frequency. 
	 *//*
	protected  Map<String, Integer> m_originalTermDocumentFrequencies = null; // values of DF (in how many original documents a term occurred)

	*//**
	 * This map shows in how many different documents (expanded texts) each term occurs. 
	 *//*
	protected  Map<String, Integer> m_expandedTermDocumentFrequencies = null; // values of DF (in how many expanded documents a term occurred)
*/
	
	
	/******************************************************************************************
	 * CONSTRUCTORS
	 * ****************************************************************************************/

	public AbstractTextCollection() {
		
		m_docIdsByText=new HashMap<String, List<Integer>>();
		m_docTextByDocId=new HashMap<Integer, String>();

		m_docIdsPerGoldCluster = new HashMap<String, List<Integer>>();
		m_goldClustersPerDocId= new HashMap<Integer, List<String>>();
		
		m_docIdsByOriginalTerm = new HashMap<String, Map<Integer, Double>>();
		m_docIdsByExpansionTerm = new HashMap<String, Map<Integer, Double>>();
		m_originalTermsByDocId = new HashMap<Integer, Map<String, Double>>();
		m_expansionTermsByDocId = new HashMap<Integer, Map<String, Double>>();
		m_originalTermsForExpansion = new HashSet<String>();
		
		m_termSemanticRelatednessMap = new PairMap<String,Double>();	
		
	}
	
	/******************************************************************************************
	 * AUXILIARY METHODS FOR DATA ORGANIZATION (LOAD COLLECTION & EXPAND COLLECTION)
	 * ****************************************************************************************/

	protected void setInputFile(String inputFile) {
		m_inputFile = inputFile;
	}
	
	public String getInputFilename(){
		return m_inputFile;
	}
	
	public String getDatasetName(){
		return m_inputFile;
	}	

	protected void setConfigurationFileName(String configurationFileName){
		m_configurationFileName=configurationFileName;
	}
	
	protected void addDocumentToCollection(Integer docId, String docText, String goldStandardClusterName){
    	// add the document to the document indexes if not yet there
    	if (!m_docTextByDocId.containsKey(docId)){
    		// add to text by id index
    		m_docTextByDocId.put(docId, docText);
	    	// add to ids by text index
    		List<Integer> ids = new LinkedList<Integer>();
	    	ids.add(docId);
	    	// check if document(s) with such text was seen before
	    	if(m_docIdsByText.containsKey(docText)){
	    		// if yes - add the previous ids to the current id
	    		ids.addAll(m_docIdsByText.get(docText));
	    	}	
	    	m_docIdsByText.put(docText,ids);			    		
    	}
	    	
    	// update cluster indexes
    	// update docs per cluster index
    	List<Integer> ids= new LinkedList<Integer>(); 
    	ids.add(docId);
    	if (m_docIdsPerGoldCluster.containsKey(goldStandardClusterName)){
    		ids.addAll(m_docIdsPerGoldCluster.get(goldStandardClusterName));
    	}
        m_docIdsPerGoldCluster.put(goldStandardClusterName,ids);

        // update clusters per doc index
        List<String> clusters = new LinkedList<String>();
        clusters.add(goldStandardClusterName);
        if(m_goldClustersPerDocId.containsKey(docId)){
        	clusters.addAll(m_goldClustersPerDocId.get(docId));
        }
        m_goldClustersPerDocId.put(docId, clusters);		
	}
	
	
	protected void addOriginalTerm(String term, String termFormForExpansion, Integer docId, Double count){
		// count - by what number to increment the number of times the term is found in the document
		// term - the way the term should be used in the documents representation (e.g. only lemma, or stem etc)
		// termFormForExpansion - the term to use for expansion (e.g. surface form, or lemma+pos etc) 
		
		// add term for expansion
		m_originalTermsForExpansion.add(termFormForExpansion);
		
		//add to docs by term index
		Map<Integer,Double> docTermCounts;	
		if (m_docIdsByOriginalTerm.containsKey(term)) docTermCounts=m_docIdsByOriginalTerm.get(term);
		else docTermCounts = new HashMap<Integer,Double>();			
		
		if(!docTermCounts.containsKey(docId)) {
			docTermCounts.put(docId, count);
		}
		else{	
			docTermCounts.put(docId,docTermCounts.get(docId)+count);
		}
		m_docIdsByOriginalTerm.put(term, docTermCounts);
		
		// add to terms by doc index
		Map<String,Double> termDocCounts;
		if(m_originalTermsByDocId.containsKey(docId)) termDocCounts = m_originalTermsByDocId.get(docId);
		else termDocCounts = new HashMap<String,Double>();
		
		if (!termDocCounts.containsKey(term)){
			termDocCounts.put(term, count);
		}
		else{
			termDocCounts.put(term, termDocCounts.get(term)+count);
		}
		m_originalTermsByDocId.put(docId, termDocCounts);
		
/*		int termFreq=1; //termFreq shows in how many different docs this term occurs
		if (m_originalTermDocumentFrequencies.containsKey(term)){
			termFreq+=m_originalTermDocumentFrequencies.get(term);
		}
		m_originalTermDocumentFrequencies.put(term, termFreq);
*/	
	}
	
	
	protected void addExpansionTerm(String expansionTerm, Double confidence, String expandedTerm){
		if (expandedTerm.equals(expansionTerm)) return;
		
		// add the term to all relevant documents (all documents containing the expanded term)
		for (Integer docId : m_docIdsByOriginalTerm.get(expandedTerm).keySet()){
			addExpansionTermToOneDocument(expansionTerm, docId, confidence);						
		}
		// add the term to semantic relatedness map
		Pair<String> termPair = new Pair<String>(expandedTerm, expansionTerm);
		if (m_termSemanticRelatednessMap.containsPair(termPair)) confidence = getConfidence(confidence, m_termSemanticRelatednessMap.getValueOf(termPair));
		m_termSemanticRelatednessMap.put(termPair, confidence);
	}

	protected void addExpansionTermToOneDocument(String expansionTerm, Integer docId, Double confidence){
		//add to docs by term index
		Map<Integer,Double> docTermValues;	
		if (m_docIdsByExpansionTerm.containsKey(expansionTerm)) docTermValues=m_docIdsByExpansionTerm.get(expansionTerm);
		else docTermValues = new HashMap<Integer,Double>();			
		
		if(!docTermValues.containsKey(docId)) {
			docTermValues.put(docId, confidence);
		}
		else{	
			docTermValues.put(docId, getConfidence(docTermValues.get(docId),confidence));
		}
		m_docIdsByExpansionTerm.put(expansionTerm, docTermValues);
		
		// add to terms by doc index
		Map<String,Double> termDocValues;
		if(m_expansionTermsByDocId.containsKey(docId)) termDocValues = m_expansionTermsByDocId.get(docId);
		else termDocValues = new HashMap<String,Double>();
		
		if (!termDocValues.containsKey(expansionTerm)){
			termDocValues.put(expansionTerm, confidence);
		}
		else{
			termDocValues.put(expansionTerm, getConfidence(termDocValues.get(expansionTerm),confidence));
		}
		m_expansionTermsByDocId.put(docId, termDocValues);

/*		int termFreq=0; //termFreq shows in how many different docs this term occurs, should be initialized as 0 here since expansions are not seen (a priori) in any documents
		if (m_originalTermDocumentFrequencies.containsKey(expansionTerm)){
			termFreq+=m_originalTermDocumentFrequencies.get(expansionTerm);
		}
		m_originalTermDocumentFrequencies.put(expansionTerm, termFreq); // We add expansions to this map to have the whole dictionary. Terms not seen in any of the original texts will have zero frequency
*/
	}

	/**
	 * Adds transitive relations between original and expansion terms  
	 * e.g. given two relations (mastercard <-> visa) and (visa <-> creditcard), add the relation (mastercard <-> creditcard) to semantic relatedness map, if it's not yet there
	 */
	protected void addTransitiveClosure() {
		SimpleWeightedGraph<String, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		// add term-nodes
		for (String term : this.getDocIdsByOriginalTerm().keySet()){
			g.addVertex(term);
		}
		for (String term : this.getDocIdsByExpansionTerm().keySet()){
			g.addVertex(term);
		}
		
		// add relatedness edges
		for (String a : g.vertexSet()){
			for (String b : g.vertexSet()){
				if (a.equals(b)) continue; // no loops
				Pair<String> ab = new Pair<String>(a, b);
				if (m_termSemanticRelatednessMap.containsPair(ab)){
					double weight = m_termSemanticRelatednessMap.getValueOf(ab);
					g.addEdge(a, b);
					g.setEdgeWeight(g.getEdge(a, b), weight);
				}
			}
		}
		
		// add closure edges 
		Map<String,Double> newEdgeTargets = new HashMap<String,Double>();
        // At every iteration of the outer loop, we add a path of length 1
        // between nodes that originally had a path of length 2. In the worst
        // case, we need to make floor(log |V|) + 1 iterations. We stop earlier
        // if there is no change to the output graph.

        int bound = computeBinaryLog(g.vertexSet().size());
        boolean done = false;
        for (int i = 0; !done && (i < bound); ++i) {
            done = true;
            for (String v1 : g.vertexSet()) {
                newEdgeTargets.clear();

                for (String v2 : getTgts(g, v1)) {
                	Double weight = g.getEdgeWeight(g.getEdge(v1, v2));
                    for (String v3 : getTgts(g, v2)) {

                        // Assign min confidence of the 2 edges as the confidence of the transitive edge
                        if (g.getEdgeWeight(g.getEdge(v2, v3)) < weight) weight=g.getEdgeWeight(g.getEdge(v2, v3));

                        if (v1.equals(v3)) {
                            // Don't add self loops.
                            continue;
                        }

                        if (g.containsEdge(v1, v3)) {
                        	// Already have such edge
                        	continue; 
                        }
                                      
                        newEdgeTargets.put(v3,weight);
                        done = false;
                    }
                }

                for (String v3 : newEdgeTargets.keySet()) {
                	g.addEdge(v1, v3);
					g.setEdgeWeight(g.getEdge(v1, v3), newEdgeTargets.get(v3)); 
					System.out.println("Added transitive relatedness: "+v1+" <->" + v3 + " : "+ String.valueOf(newEdgeTargets.get(v3)));
					
                }
            }
        }

        // translate the graph with closures back to the relatedness map
        for (DefaultWeightedEdge e : g.edgeSet()){
        	Pair<String> ab = new Pair<String>(g.getEdgeSource(e), g.getEdgeTarget(e));
        	m_termSemanticRelatednessMap.put(ab, g.getEdgeWeight(e));
        }
		
	}

	
	private	Set<String> getTgts(SimpleWeightedGraph<String, DefaultWeightedEdge> g, String vertex){
		Set<String> res = new HashSet<String>();
		for (DefaultWeightedEdge e : g.edgeSet()){
			if (g.getEdgeSource(e).equals(vertex)) res.add(g.getEdgeTarget(e));
		}
		return res;
	}
		   /**
	  * Computes floor(log_2(n)) + 1
	  */
	 private int computeBinaryLog(int n)
	 {
	     assert n >= 0;
	
	     int result = 0;
	     while (n > 0) {
	         n >>= 1;
	         ++result;
	     }
	
	     return result;
	 }	

	/** The method is used to set up the confidence of an expansion, suggested by more than one resource.
	 * @param confA - confidence from previous resources
	 * @param confB - confidence from the new resource
	 * @return Currently max of the two confidences is returned. It's common to trust an expansion more if it's suggested by multiple sources - taking max of the confidences follows this logic 
	 */
	private double getConfidence(double confA, double confB){
		 return Math.max(confA, confB);
		// return confA+confB; //another possibility - not used since an expansion is likely to receive higher score than original terms
	}
	
	/******************************************************************************************
	 * IMPLEMENTATION OF THE TextCollection INTERFACE METHODS
	 * ****************************************************************************************/

	@Override
	public Double getOverallTermFrequencyBeforeExpansion(String term) {
		double freq = 0;
		if (m_docIdsByOriginalTerm.containsKey(term)){
			for (int docId : m_docIdsByOriginalTerm.get(term).keySet()){
				freq += m_docIdsByOriginalTerm.get(term).get(docId);
			}					
		}
		return freq;
	}

	@Override
	public Double getOverallTermFrequencyAfterExpansion(String term) {
		double freq = getOverallTermFrequencyBeforeExpansion(term);
		if (m_docIdsByExpansionTerm.containsKey(term)){
			for (int docId : m_docIdsByExpansionTerm.get(term).keySet()){
				freq += m_docIdsByExpansionTerm.get(term).get(docId);
			}					
		}		
		return freq;
	}
	
	@Override
	public Double getTermFrequencyInDocumentBeforeExpansion(String term, Integer docId) {
		double freq = 0;
		if (m_docIdsByOriginalTerm.containsKey(term)){
			if (m_docIdsByOriginalTerm.get(term).containsKey(docId)){
				freq += m_docIdsByOriginalTerm.get(term).get(docId);
			}					
		}
		return freq;
	}

	@Override
	public Double getTermFrequencyInDocumentAfterExpansion(String term, Integer docId) {
		double freq = getOverallTermFrequencyBeforeExpansion(term);
		if (m_docIdsByExpansionTerm.containsKey(term)){
			if (m_docIdsByExpansionTerm.get(term).containsKey(docId)){
				freq += m_docIdsByExpansionTerm.get(term).get(docId);
			}					
		}		
		return freq;
	}	
	
	@Override
	public Double getInvertedDocumentFrequencyBeforeExpansion(String term){
		if (!m_docIdsByOriginalTerm.containsKey(term)) return null;
		double df = new HashSet<Integer>(m_docIdsByOriginalTerm.get(term).keySet()).size();
		double idf = Math.log(m_docTextByDocId.size()/df);
		return idf;
	}
	
	@Override
	public Double getInvertedDocumentFrequencyAfterExpansion(String term){
		Set<Integer> allDocIds = new HashSet<Integer>();
		if (m_docIdsByOriginalTerm.containsKey(term)){
			allDocIds.addAll(m_docIdsByOriginalTerm.get(term).keySet());
		}
		if (m_docIdsByExpansionTerm.containsKey(term)){
			allDocIds.addAll(m_docIdsByExpansionTerm.get(term).keySet());			
		}
		if (allDocIds.isEmpty()) return null;
		double df = allDocIds.size();
		
		double idf = Math.log(m_docTextByDocId.size()/df);
		
		return idf;
	}
		

	@Override
	public Map<String, ? extends List<Integer>> getDocIdsByText() {
		// TODO Auto-generated method stub
		return m_docIdsByText;
	}



	@Override
	public Map<Integer, String> getDocTextsByDocId() {
		// TODO Auto-generated method stub
		return m_docTextByDocId;
	}



	@Override
	public Map<Integer, ? extends List<String>> getGoldStandardClustersPerDocId() {
		// TODO Auto-generated method stub
		return m_goldClustersPerDocId;
	}



	@Override
	public Map<String, ? extends List<Integer>> getDocIdsPerGoldStandardCluster() {
		// TODO Auto-generated method stub
		return m_docIdsPerGoldCluster;
	}



	@Override
	public Map<String, Map<Integer, Double>> getDocIdsByOriginalTerm() {
		// TODO Auto-generated method stub
		return m_docIdsByOriginalTerm;
	}



	@Override
	public Map<String, Map<Integer, Double>> getDocIdsByExpansionTerm() {
		// TODO Auto-generated method stub
		return m_docIdsByExpansionTerm;
	}

	@Override
	public Map<Integer, Map<String, Double>> getOriginalTermsByDocId() {
		return m_originalTermsByDocId;
	}

	@Override
	public Map<Integer, Map<String, Double>> getExpansionTermsByDocId() {
		return m_expansionTermsByDocId;
	}

	@Override
	public PairMap<String, Double> getTermSemanticRelatednessMap() {
		return m_termSemanticRelatednessMap;
	}	

	public Map<String, Double> getAllDocumentTermsAfterExpansion(Integer docId){
		Set<String> terms = new HashSet<String>(getOriginalTermsByDocId().get(docId).keySet());
		if(getExpansionTermsByDocId().containsKey(docId)){
			terms.addAll(getExpansionTermsByDocId().get(docId).keySet());
		}
		
		Map<String, Double> allTerms = new HashMap<String, Double>();
		for (String term : terms){
			allTerms.put(term, getTermFrequencyInDocumentAfterExpansion(term, docId));	
		}
		return allTerms;	
	}

}

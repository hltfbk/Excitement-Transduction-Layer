/**
 * 
 */
package eu.excitementproject.tl.composition.nodematcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import eu.excitementproject.tl.composition.api.ConfidenceCalculator;
import eu.excitementproject.tl.composition.confidencecalculator.ConfidenceCalculatorCategoricalFrequencyDistribution;
import eu.excitementproject.tl.composition.exceptions.ConfidenceCalculatorException;
import eu.excitementproject.tl.composition.exceptions.EntailmentGraphCollapsedException;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.collapsedgraph.EquivalenceClass;
import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.rawgraph.EntailmentUnit;
import eu.excitementproject.tl.structures.search.NodeMatch;
import eu.excitementproject.tl.structures.search.PerNodeScore;

/**
 * This NodeMatcher compares an input fragment graph to an input entailment graph. It transforms
 * the entailment graph into a Lucene index and the fragment graph into a Lucene query, which is
 * then matched against this index. For efficiency reasons, it compares the base statement only. 
 * If a matching node is found, it returns this node together with the confidence score of the match. 
 * 
 * @author Kathrin Eichler
 *
 */
public class NodeMatcherLucene extends AbstractNodeMatcher {
	
	private EntailmentGraphCollapsed entailmentGraph;
	private String indexPath;
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private QueryParser parser; 
	
	private final static Logger logger = Logger.getLogger(NodeMatcherLucene.class.getName());
	
	
	/**
	 * Constructor with just one input argument: the entailment graph.
	 * 
	 * @param myEntailmentGraph
	 */
	public NodeMatcherLucene(EntailmentGraphCollapsed myEntailmentGraph) {
		entailmentGraph = myEntailmentGraph;
		indexPath = "src/test/resources/Lucene_index/";
		analyzer = new StandardAnalyzer(Version.LUCENE_44);
	}
	
	/**
	 * Constructor with just three input arguments:
	 * 
	 * @param myEntailmentGraph: the input entailment graph
	 * @param myIndexPath: the path to which the index will be written
	 * @param myAnalyzer: the analyzer to be used for indexing / searching
	 */
	public NodeMatcherLucene(EntailmentGraphCollapsed myEntailmentGraph, String myIndexPath, Analyzer myAnalyzer) {
		entailmentGraph = myEntailmentGraph;
		indexPath = myIndexPath;
		analyzer = myAnalyzer;
	}
	
	/**
	 * Initializing the search process: reading the index and initializing searcher and query parser. 
	 * 
	 * @throws IOException
	 */
	public void initializeSearch() throws IOException {
		reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		searcher = new IndexSearcher(reader);
		parser = new QueryParser(Version.LUCENE_44, "euText", analyzer); //field to be searched: euText (test of entailment unit)
		parser.setDefaultOperator(QueryParser.AND_OPERATOR);
	}
		
	
	/**
	 * Find entailment units in the entailment graph nodes matching the input fragment graph (longest match).
	 * 
	 * @param fragmentGraph: the input fragment graph
	 */
	@Override
	public Set<NodeMatch> findMatchingNodesInGraph(FragmentGraph fragmentGraph) throws NodeMatcherException {
		
		//create empty node match set
		Set<NodeMatch> nodeMatches = new HashSet<NodeMatch>();
		
		/** read fragment graph, starting with the longest (complete) statement, going down to the 
		base statement, stopping as soon as a match is found */
		for (int i = fragmentGraph.getMaxLevel(); i>=0; i--) {		
			Set<EntailmentUnitMention> mentions = fragmentGraph.getNodes(i); 
			for (EntailmentUnitMention mention : mentions) {
				NodeMatch match;
				try {
					match = findMatchingNodesForMention(mention);
					if (null != match) nodeMatches.add(match);
				} catch (ParseException | IOException e) {
					e.printStackTrace();
				}
			}
			if (nodeMatches.size() > 0) break;
		}
		return nodeMatches;
	}

	/**
	 * Finds nodes in an entailment graph that match a mention.
	 * 
	 * Search for a query text (the mentionToBeFound) in the index. For a node to be returned, 
	 * all tokens in the query have to EXACTLY match the tokens of at least one entailment unit 
	 * associated to the node. (There can be difference in word order, however!)
	 * The score of the match is set to 1.0.
	 * 
	 * @param mentionToBeFound: mention from the fragment graph
	 * @return NodeMatch holding the input mention associated to PerNodeScore-s (matching nodes and the confidence of the match)
	 * @throws ParseException
	 * @throws IOException
	 */
	public NodeMatch findMatchingNodesForMention(EntailmentUnitMention mentionToBeFound) throws ParseException, IOException {
		String queryText = mentionToBeFound.getTextWithoutDoubleSpaces();
		String queryTextEscaped = QueryParser.escape(queryText); //make sure special chars like '?' are escaped
		Query query = parser.parse(queryTextEscaped);
		
		Date start = new Date();
		searcher.search(query, null, 100);
		Date end = new Date();
		logger.info("Search took "+(end.getTime()-start.getTime())+"ms");
		
		TopDocs results = searcher.search(query, 5);
		ScoreDoc[] hits = results.scoreDocs;
		//int numTotalHits = results.totalHits;
		//logger.info(numTotalHits + " potentially matching documents:");
		Map<Document,Float> matchScores = new HashMap<Document,Float>();
		for (int i=0; i<hits.length; i++) {
			ScoreDoc hit = hits[i];
		    Document d = searcher.doc(hit.doc);
		    //matchScores.put(d, hit.score); //score returned by Lucene
		    if (d.getField("euText").stringValue().split("\\s+").length == queryText.split("\\s+").length) {
		    	//all query terms match and returned document has the same number of terms --> perfect match!
		    	matchScores.put(d, new Float(1.0)); 
		    }
		}	
		logger.info(matchScores.size() + " matching documents");
		List<PerNodeScore> scores = new ArrayList<PerNodeScore>();
		for (Document document : matchScores.keySet()) {
		    float score = matchScores.get(document);
			if (score > 0) { //add non-zero scores to list
				PerNodeScore perNodeScore = new PerNodeScore();
				EquivalenceClass ec = entailmentGraph.getVertex(document.get("label"));				
				perNodeScore.setNode(ec);
				perNodeScore.setScore(score);
				scores.add(perNodeScore);
			}
		}
		
		if (scores.size() > 0) { //at least one match found
			NodeMatch nodeMatch = new NodeMatch();
			nodeMatch.setMention(mentionToBeFound);
			nodeMatch.setScores(scores);
			return nodeMatch;
		}
		return null;
	}
	
	/**
	 * Index all entailment graph nodes. 
	 * 
	 * @throws IOException
	 */
	public void indexGraphNodes() throws IOException {
		boolean create = true;
		
		logger.info("Indexing graph nodes to directory '" + indexPath + "'..."); 
		Directory dir = FSDirectory.open(new File(indexPath));
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		 
		 if (create) {
			 // Create a new index in the directory, removing any
		     // previously indexed documents:
		     iwc.setOpenMode(OpenMode.CREATE);
		 } else {
			 // Add new documents to an existing index:
		     iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
		 }
		 
		 // Optional: for better indexing performance, if you
		 // are indexing many documents, increase the RAM
		 // buffer.  But if you do this, increase the max heap
		 // size to the JVM (eg add -Xmx512m or -Xmx1g):
		 //
		 // iwc.setRAMBufferSizeMB(256.0);
		 
		 IndexWriter writer = new IndexWriter(dir, iwc);
		 
		 Document doc;
		 int written = 0;
		 int updated = 0;
		 for (EquivalenceClass ec : entailmentGraph.vertexSet()) {
			 doc = new Document(); // make a new, empty document	
			 String label = ec.getLabel();
			 for (EntailmentUnit eu : ec.getEntailmentUnits()) { //index entailment units
				 String euText = eu.getTextWithoutDoulbeSpaces();
				 doc.add(new TextField("euText", euText, Store.YES));
				 doc.add(new TextField("label", label, Store.YES));
			 }
			 if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
					// New index, so we just add the document (no old document can be there):
					writer.addDocument(doc);
					written++;
				 } else {
					// Existing index (an old copy of this document may have been indexed) so 
					// we use updateDocument instead to replace the old one matching the exact 
					// path, if present:
					writer.updateDocument(new Term("id", "1"), doc);
					updated++;
				 }
		 }		
		 writer.close();
		 logger.info("Added " + written + " documents");
		 logger.info("Updated " + updated + " documents");
    }
		
	public static void main(String[] args) throws IOException, ParseException, EntailmentGraphCollapsedException, ConfidenceCalculatorException {
		EntailmentGraphCollapsed graph = new EntailmentGraphCollapsed(new File("src/test/resources/sample_graphs/german_dummy_data_for_evaluator_test_graph.xml"));
		ConfidenceCalculator cc = new ConfidenceCalculatorCategoricalFrequencyDistribution();
		cc.computeCategoryConfidences(graph);
		NodeMatcherLucene nm = new NodeMatcherLucene(graph, "src/test/resources/Lucene_index", new StandardAnalyzer(Version.LUCENE_44));
		nm.indexGraphNodes();
		nm.initializeSearch();
		NodeMatch nodeMatch = nm.findMatchingNodesForMention(new EntailmentUnitMention("Die Punkte lösen mein Problem nicht", 0));
		if (null != nodeMatch) {
			List<PerNodeScore> perNodeScores = nodeMatch.getScores();
			for (PerNodeScore perNodeScore : perNodeScores) {
				System.out.println("Matching node in graph: " + perNodeScore.getNode().getLabel());
				System.out.println("Score of the match: " + perNodeScore.getScore());
				System.out.println("Category confidences: ");
				for (String category : perNodeScore.getNode().getCategoryConfidences().keySet()) {
					System.out.println("category "+ category + ": " + perNodeScore.getNode().getCategoryConfidences().get(category));					
				}
				
			}
		}		
	}

}

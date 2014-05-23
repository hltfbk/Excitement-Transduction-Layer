package eu.excitementproject.tl.composition.nodematcher;

import java.io.File;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import eu.excitementproject.tl.composition.api.NodeMatcher;
import eu.excitementproject.tl.composition.api.NodeMatcherWithIndex;
import eu.excitementproject.tl.composition.exceptions.NodeMatcherException;
import eu.excitementproject.tl.structures.collapsedgraph.EntailmentGraphCollapsed;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.search.NodeMatch;

/**
An index-based implementation of the {@link NodeMatcher} interface

* @author Kathrin Eichler
* 
*/
public abstract class AbstractNodeMatcherLucene implements NodeMatcherWithIndex {
	
	protected static EntailmentGraphCollapsed entailmentGraph;
	protected static String indexPath;
	protected static final String DEFAULT_INDEX_PATH = "src/test/resources/Lucene_index/";
	protected static IndexReader reader;
	protected static IndexSearcher searcher;
	protected static Analyzer analyzer;
	protected static final Analyzer DEFAULT_ANALYZER = new StandardAnalyzer(Version.LUCENE_44);

	
	/**
	 * Constructor with just one input argument: the entailment graph.
	 * 
	 * @param myEntailmentGraph
	 */
	public AbstractNodeMatcherLucene(EntailmentGraphCollapsed myEntailmentGraph) {
		entailmentGraph = myEntailmentGraph;
		indexPath = DEFAULT_INDEX_PATH;
		analyzer = DEFAULT_ANALYZER;
	}
		
	/**
	 * Constructor with three input arguments:
	 * 
	 * @param myEntailmentGraph: the input entailment graph
	 * @param myIndexPath: the path to which the index will be written
	 * @param myAnalyzer: the analyzer to be used for indexing / searching
	 */
	public AbstractNodeMatcherLucene(EntailmentGraphCollapsed myEntailmentGraph, String myIndexPath, Analyzer myAnalyzer) {
		entailmentGraph = myEntailmentGraph;
		indexPath = myIndexPath;
		analyzer = myAnalyzer;
	}
	
	
	/**
	 * Initializing the search process. 
	 * 
	 * @throws IOException 
	 */
	public void initializeSearch() {
		if (indexPath == null) indexPath = DEFAULT_INDEX_PATH;
		try {
			reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		searcher = new IndexSearcher(reader);
		if (analyzer == null) analyzer = DEFAULT_ANALYZER;
	}
	
	/**
	 * Index entailment graph nodes, writing index to indexPath.
	 * 
	 * @throws IOException
	 */
	public void indexGraphNodes() {
	}
	
	@Override
	public Set<NodeMatch> findMatchingNodesInGraph(FragmentGraph fragmentGraph) throws NodeMatcherException {
		return null;
	}

}

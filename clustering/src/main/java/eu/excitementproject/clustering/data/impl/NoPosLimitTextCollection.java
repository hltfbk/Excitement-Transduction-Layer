package eu.excitementproject.clustering.data.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import eu.excitementproject.clustering.data.api.AbstractTextCollection;
import eu.excitementproject.clustering.data.api.LexicalExpander;
import eu.excitementproject.eop.common.representation.partofspeech.BySimplerCanonicalPartOfSpeech;
import eu.excitementproject.eop.common.representation.partofspeech.PartOfSpeech;
import eu.excitementproject.eop.common.representation.partofspeech.SimplerCanonicalPosTag;
import eu.excitementproject.eop.common.representation.partofspeech.UnsupportedPosTagStringException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFileDuplicateKeyException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.eop.lap.biu.en.lemmatizer.gate.GateLemmatizer;
import eu.excitementproject.eop.lap.biu.en.postagger.stanford.MaxentPosTagger;
import eu.excitementproject.eop.lap.biu.lemmatizer.Lemmatizer;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;
import eu.excitementproject.eop.lap.biu.postagger.PosTaggedToken;
import eu.excitementproject.eop.lap.biu.postagger.PosTagger;
import eu.excitementproject.eop.lap.biu.postagger.PosTaggerException;

/**
 * @author Lili Kotlerman
 *
 */
public class NoPosLimitTextCollection extends AbstractTextCollection  {

	protected PosTagger m_posTagger=null;

	//	protected static final File gateRulesFile = new File("D:/Jars/GATE-3.1/plugins/Tools/resources/morph/default.rul");
	protected Lemmatizer m_lemmatizer;
	protected String m_gateRulesFilename;

	protected boolean m_doFilterStopwords=false;
	protected String m_stopwordsFilename=null;
	protected Set<String> m_domainVocabulary = null;
	
/*	protected double m_stopwordMoreThanPercentThreshold=0;
	protected double m_stopwordLessThanPercentThreshold=0;	
*/
	protected List<String> m_stopWords=null;
	
	private final String m_posSeparator= "::";
	
	
	/******************************************************************************************
	 * CONSTRUCTORS & INITIALIZATION
	 * @throws ConfigurationException 
	 * @throws ConfigurationFileDuplicateKeyException 
	 * ****************************************************************************************/
 
	public NoPosLimitTextCollection(String configurationFileName) throws LemmatizerException, MalformedURLException, ConfigurationFileDuplicateKeyException, ConfigurationException{
		super();
		setConfigurationFileName(configurationFileName);
		init(new ConfigurationFile(m_configurationFileName));
	}
	
	
	public NoPosLimitTextCollection(String configurationFileName, String dataFilename) throws LemmatizerException, MalformedURLException, ConfigurationFileDuplicateKeyException, ConfigurationException{
		super();
		setConfigurationFileName(configurationFileName);
		init(new ConfigurationFile(m_configurationFileName), dataFilename);
	}
	
	private void init(ConfigurationFile conf) throws LemmatizerException, MalformedURLException, ConfigurationException{
		

		if(conf.isModuleExist("Preprocessing")){
			ConfigurationParams params = conf.getModuleConfiguration("Preprocessing");
			if(params.containsKey("filter_stopwords")) m_doFilterStopwords = params.getBoolean("filter_stopwords");
			if(params.containsKey("stopwords_file")) m_stopwordsFilename = params.getString("stopwords_file");
			if(params.containsKey("pos-tagger-model")) {
				try {
					m_posTagger = new MaxentPosTagger(params.get("pos-tagger-model"));					
//					m_posTagger = new MaxentPosTagger("D:/JARS/stanford-postagger-full-2008-09-28/models/left3words-wsj-0-18.tagger");
//					m_posTagger = new MaxentPosTagger("D:/JARS_fat/stanford-postagger-full-2008-06-06/models/left3words-wsj-0-18.tagger");
					
					m_posTagger.init();
					System.out.println("Init pos tagger");
				} catch (PosTaggerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}							
			}
		//	if(params.containsKey("stopword_lessThan_Percent_threshold")) m_stopwordLessThanPercentThreshold = params.getDouble("stopword_lessThan_Percent_threshold");
		//	if(params.containsKey("stopword_moreThan_Percent_threshold")) m_stopwordMoreThanPercentThreshold = params.getDouble("stopword_moreThan_Percent_threshold");
		}
		loadStopwords();
		
		if(conf.isModuleExist("Data")){
			ConfigurationParams params = conf.getModuleConfiguration("Data");
			if(params.containsKey("data_file")) setInputFile(params.getString("data_file"));
		}
		
		if(conf.isModuleExist("Gate-lemmatizer")){
			System.out.println("1");
			ConfigurationParams params = conf.getModuleConfiguration("Gate-lemmatizer");
			System.out.println("***"+params.getClass().getName());
			if(params.containsKey("lemmatizer-rule-file"))
			{
				System.out.println("###");
				m_gateRulesFilename = params.getString("lemmatizer-rule-file");
			}
			System.out.println(params.keySet());
			System.out.println(m_gateRulesFilename);
			m_lemmatizer = new GateLemmatizer(new File(m_gateRulesFilename).toURI().toURL());
			m_lemmatizer.init();
		}	
		
	}
	
	private void init(ConfigurationFile conf, String dataFilename) throws LemmatizerException, MalformedURLException, ConfigurationException{
		init(conf);
		setInputFile(dataFilename);
		
	}
	
	
	/******************************************************************************************
	 * METHODS FOR TERM FILTERING (STOP WORDS, FUNCTION WORDS, TOO FREQUENT/INFREQUENT TERMS)
	 * ****************************************************************************************/

	protected void loadStopwords(){
		m_stopWords = new LinkedList<String>();
		if (m_doFilterStopwords){
			try {
				BufferedReader reader = new BufferedReader(new FileReader(m_stopwordsFilename));
				String line = reader.readLine();	
				while(line != null) {
					m_stopWords.add(line.replace("\n", ""));
					line=reader.readLine();
				}
				reader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	
	@Override
	public void setDomainVocabulary(String filename){
			
		File vocFile = new File(filename);
		if (!vocFile.exists()) return; // if vocabulary file does not exist - don't set up the domain vocabulary
		
		m_domainVocabulary = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(vocFile));
			String line = reader.readLine();	
			while(line != null) {
				String term = line.replace("\n", "");
				if (!isStopwordTerm(term)) m_domainVocabulary.add(term);
				line=reader.readLine();
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}

	protected boolean isStopwordTerm(String term){
		if (m_stopWords==null) return false; //if stopwords are not defined (null), consider the term valid		
		if (m_stopWords.contains(term)) return true;
		if (term.toCharArray().length<2) return true;
		return false;
	}
	
	protected boolean isValidPOS(char pos){
		//if ((pos=='j')||(pos=='r')||(pos=='n')||(pos=='v')) return true;	
		//else return false;
		return true;
	}
	
	protected boolean isValidTerm(String lemma, char pos){
		if (isStopwordTerm(lemma)) return false;
		if (!isValidPOS(pos)) return false;
		if (lemma.contains("http")) return false;
		if (lemma.contains("@")) return false;
		return true;
	}
	
	private boolean isStopwordNgram(String ngram){
		if (isStopwordTerm(ngram)) return true; // if the whole ngram appears in stop words list - it's a stop word
/*		for (String term : ngram.split(" ")){
			if (isStopwordTerm(term)) 	return true; // if at least one word is a stop word - return true
		}
*/		return false; // otherwise this is not a stopword
	}
	
	@Override
	public boolean isStopword(String word) {
		if (word.contains(" ")) return isStopwordNgram(word);
		return isStopwordTerm(word);
	}

	/*private boolean isAllStopwordNgram(String ngram){
		if (isStopword(ngram)) return true; // if the whole ngram appears in stop words list - it's a stop word
		for (String term : ngram.split(" ")){
			if (!isStopword(term)) 	return false; // if at least one word is not a stop word - then this is not a stop-word ngram
		}
		return true;
	}	*/
	
/*	// updated on 13/10/13 
  	private boolean isNoiseNgram(String ngram){	
		// if ngram is found in too little or too many documents - consider it noisy
		int ngramFrequency=m_termDocumentFrequencies.get(ngram);
		int docsNumber = m_docTextByDocId.size();
		if(ngramFrequency>=docsNumber*m_stopwordMoreThanPercentThreshold) {
			if(ngramFrequency<=docsNumber*m_stopwordLessThanPercentThreshold){
				return true;
			}
		}
		return false;
	}
*/	
	
/*	protected void filterStowords(){	
		for (String ngram : m_termDocumentFrequencies.keySet()){
			if (isStopwordNgram(ngram)){
				m_docIdsByOriginalTerm.remove(ngram);
				
				m_docIdsByExpansionTerm.remove(ngram);
			}
		}
		System.out.println(m_docIdsByOriginalTerm.size()+" different ngrams left after removing stopwords");
	}
*/	

	protected boolean isValidBigram(String bigram, char posA, char posB){
		// bigram is valid if 
		// 1) both its words are not stopwords
		// and 2) at least one of bigram's terms is an adverb (r), adjective (j), noun (n) or verb (v)		 
		if (isStopwordNgram(bigram)) return false;		
	//	if ((posA=='j')||(posA=='r')||(posA=='n')||(posA=='v')) return true;
	//	if ((posB=='j')||(posB=='r')||(posB=='n')||(posB=='v')) return true;		
	//	return false;
		return true;
	}	
	
	
	/******************************************************************************************
	 * METHODS FOR TEXT PROCESSING
	 * ****************************************************************************************/

	public String getLemma (String token, PartOfSpeech tokenPos) throws LemmatizerException, UnsupportedPosTagStringException{
		m_lemmatizer.set(token,tokenPos);
		m_lemmatizer.process();
		String lemma=m_lemmatizer.getLemma(); 
		if ((lemma.endsWith("us"))&&(token.endsWith("i"))) return token; //fix strange cases when wifi->wifus, citi->citus, abu dhabi -> abu dhabus etc
		return lemma;	

	}	
	
	public String getLemma (String token) throws LemmatizerException {
		String lemma = "";
		for (String lemmaterm : token.split(" ")){
			m_lemmatizer.set(lemmaterm);
			m_lemmatizer.process();
			String addLemma = m_lemmatizer.getLemma();
			if ((addLemma.endsWith("us"))&&(lemmaterm.endsWith("i"))) addLemma=lemmaterm; //fix strange cases when wifi->wifus, citi->citus, abu dhabi -> abu dhabus etc
			lemma+=addLemma+" ";			
		}
		//m_lemmatizer.set(keyTokenLemma);
		//m_lemmatizer.process();
		return lemma.trim();			
	}	
	
	/** Currently process the text as if it is a single sentence (no splitting performed)
	 * 	Extract unigrams and bigrams
	 * @param text
	 * @return
	 */
	public List<String> extractDocumentTerms(String text) throws LemmatizerException, UnsupportedPosTagStringException, PosTaggerException{
		List<String> terms = new LinkedList<String>();
		System.out.println(m_posTagger);
		m_posTagger.setTokenizedSentence(normalizeText(text)); 
		m_posTagger.process();		
		
		String prevLemma = "";
		char prevPOS = ' ';		
		for (PosTaggedToken token: m_posTagger.getPosTaggedTokens()){
			PartOfSpeech pos = token.getPartOfSpeech(); 
			char shortPOS = pos.toString().toLowerCase().charAt(0);
			String tokenLemma= getLemma(token.getToken(), pos);
			tokenLemma = tokenLemma.trim();
			if (isValidTerm(tokenLemma, shortPOS)){
				terms.add(tokenLemma+m_posSeparator+String.valueOf(shortPOS));
			}
			if(isValidBigram(prevLemma+" "+tokenLemma, prevPOS, shortPOS)){				

				 terms.add(prevLemma+" "+tokenLemma+m_posSeparator+String.valueOf(prevPOS)+m_posSeparator+String.valueOf(shortPOS));  

			}
			prevLemma=tokenLemma;
			prevPOS=shortPOS;
		}	
		if (terms.isEmpty()) {
			terms.add("non-topic-words-only");
			System.out.println("The document: <<"+text+">> has no content words.");
		}
		return terms;
	}
	
	/******************************************************************************************
	 * IMPLEMENTATION OF THE TextCollection INTERFACE METHODS
	 * ****************************************************************************************/
	
	public void loadNewCollection() {		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(m_inputFile));
			// file format: clusterName \t docId \t docText
			String line = reader.readLine();	
			while(line != null) {				
				String[] s = line.split("\t");
				if (s.length<3) {
					 line = reader.readLine();
					 continue;
				}
				    
				// parse cluster-document info
				String clusterName = s[0];
				Integer docId = Integer.valueOf(s[1]);
				String docText = s[2].toLowerCase().replace("#"," ").replace("@", " ").replace("\""," ").replace("\n","");

				addDocumentToCollection(docId, docText, clusterName);				     				
			    // proceed to the next cluster-document pair    
				line=reader.readLine();
			}
			reader.close();
			System.out.println("Loaded "+m_docTextByDocId.size()+" docs (texts). (Distinct texts: "+ m_docIdsByText.size()+")");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			// process the documents - split to terms
			for (Integer docId : m_docTextByDocId.keySet()){
				for (String term : extractDocumentTerms(m_docTextByDocId.get(docId))) {				
					addOriginalTerm(term.split(m_posSeparator)[0], term, docId, getOriginalTermOccurrenceWeight()); // add one occurrence of current lemma (unigram ot bigram, as returned by the term extractor)
				}
			}
		} catch (PosTaggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LemmatizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedPosTagStringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected Double getOriginalTermOccurrenceWeight(){
		return 1.0; // one occurrence of original term counts as 1
	}

	private boolean isInDomainVocabulary(String expansionTerm){
		if (m_domainVocabulary == null) return true; // if domain vocabulary is not defined (null), consider all expansions to be in-domain
		if (m_domainVocabulary.isEmpty()) return true; // if domain vocabulary is not defined (empty), consider all expansions to be in-domain
		if (m_domainVocabulary.contains(expansionTerm)) return true;
		return false;
	}
	
	@Override
	public void expandCollection(LexicalExpander LE) {
		try {
			for(String termToExpand : m_originalTermsForExpansion){
				
				if (doNotExpandThisTerm(termToExpand)) continue;

				HashMap<String,Double> expansions;
				
				PartOfSpeech n = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.NOUN);
				PartOfSpeech v = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.VERB);
				PartOfSpeech j = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.ADJECTIVE);
				PartOfSpeech r = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.ADVERB);
				
				String[] s = termToExpand.split(m_posSeparator);
				LinkedList<String> pos = new LinkedList<String>();  
				if (s.length>1) {
					pos.add(s[1]);
					if (s.length>2) pos.add(s[2]);
				}
				if (pos.isEmpty()){ 
					expansions = LE.getExpansions(termToExpand);
					for (String expansionTerm : expansions.keySet()){ // for each expansion
						if (isInDomainVocabulary(expansionTerm)){ // don't add expansions which are not in the domain vocabulary (if given)
							Double confidence = expansions.get(expansionTerm);
							addExpansionTerm(expansionTerm, confidence, termToExpand);
						}
						else{
							System.out.println("!!!!!!!!! Discarded "+expansionTerm+" as an out-of-domain term.");
						}
					}
				}	
				else{
					String term = s[0];
					for (String posString : pos){
						
						if(posString.equals("n")) 	expansions = LE.getExpansions(term, n);
						else if(posString.equals("v")) 	expansions = LE.getExpansions(term, v);
						else if(posString.equals("j")) 	expansions = LE.getExpansions(term, j);
						else if(posString.equals("r")) expansions = LE.getExpansions(term, r);
						else expansions = LE.getExpansions(term);

						for (String expansionTerm : expansions.keySet()){ // for each expansion
							if (isInDomainVocabulary(expansionTerm)){ // don't add expansions which are not in the domain vocabulary (if given)
								Double confidence = expansions.get(expansionTerm);
								addExpansionTerm(expansionTerm, confidence, term);
							}
							else{
								System.out.println("!!!!!!!!! Discarded "+expansionTerm+" as an out-of-domain term.");
							}
						}						
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		m_domainVocabulary=null; //not needed anymore
		
	//	addTransitiveClosure();
	}

	/** Currently WordNet returns a lot of junk for bigrams (trigrams etc) that are not really found in WN
	 * This is a temporary patch for that behavior
	 * TODO remove this method when not needed
	 * @param termToExpand
	 * @return
	 */
	private boolean doNotExpandThisTerm(String termToExpand){
		//if (termToExpand.contains(" ") && getOverallTermFrequencyBeforeExpansion(termToExpand)<10) return true;
		return false;
	}
	
	@Override
	public String getDatasetName(){
		for(String s : super.getDatasetName().replace('/', '#').replace('\\', '#').replace(':', '#').split("#")){
			if (s.contains(".txt")) return s.replace(".txt","");
		}		
		return getDatasetName();
	}	
	
	public String normalizeText(String text){
		return text.replace(","," , ").replace("."," . ").replace("*"," ").
				replace('"',' ').replace('/',' ').replace("' "," ").replace(" '"," ").
				replace("!"," ").replace("?"," ").replace("("," ").replace(")"," ").
				replace("&amp;"," ").replace("&amp"," ").replace("&quot;"," ").
				replace("&quot"," ").replace("amp;"," ").replace("quot;"," ").
				replace("amp"," ").replace("quot"," ").replace(";"," ").
				replace("#"," ");		
	}




}

package tmp;

import java.io.File;
import java.net.MalformedURLException;

import java.util.List;

import eu.excitementproject.eop.common.representation.partofspeech.PartOfSpeech;
import eu.excitementproject.eop.common.representation.partofspeech.UnsupportedPosTagStringException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFileDuplicateKeyException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.eop.lap.biu.en.lemmatizer.gate.GateLemmatizer;
import eu.excitementproject.eop.lap.biu.en.postagger.stanford.MaxentPosTagger;
import eu.excitementproject.eop.lap.biu.lemmatizer.Lemmatizer;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;
import eu.excitementproject.eop.lap.biu.postagger.PosTagger;
import eu.excitementproject.eop.lap.biu.postagger.PosTaggerException;

/**
 * @author Lili Kotlerman
 *
 */
public class SimplePreprocessor {

	protected PosTagger m_posTagger=null;

	//	protected static final File gateRulesFile = new File("D:/Jars/GATE-3.1/plugins/Tools/resources/morph/default.rul");
	protected Lemmatizer m_lemmatizer;
	protected String m_gateRulesFilename;

	protected boolean m_doFilterStopwords=false;
	protected String m_stopwordsFilename=null;
	protected String m_inputFile=null;	
/*	protected double m_stopwordMoreThanPercentThreshold=0;
	protected double m_stopwordLessThanPercentThreshold=0;	
*/
	protected List<String> m_stopWords=null;
	
	
	/******************************************************************************************
	 * CONSTRUCTORS & INITIALIZATION
	 * @throws ConfigurationException 
	 * @throws ConfigurationFileDuplicateKeyException 
	 * ****************************************************************************************/
 
	public SimplePreprocessor(String configurationFileName) throws LemmatizerException, MalformedURLException, ConfigurationFileDuplicateKeyException, ConfigurationException{
		init(new ConfigurationFile(configurationFileName));
	}
	
	private void init(ConfigurationFile conf) throws LemmatizerException, MalformedURLException, ConfigurationException{
		
		try {
			m_posTagger = new MaxentPosTagger("D:/JARS/stanford-postagger-full-2008-09-28/models/left3words-wsj-0-18.tagger");
//			m_posTagger = new MaxentPosTagger("D:/JARS_fat/stanford-postagger-full-2008-06-06/models/left3words-wsj-0-18.tagger");
			
			m_posTagger.init();
			System.out.println("Init pos tagger");
		} catch (PosTaggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			

		if(conf.isModuleExist("Gate-lemmatizer")){
			ConfigurationParams params = conf.getModuleConfiguration("Gate-lemmatizer");
			if(params.containsKey("lemmatizer-rule-file")) m_gateRulesFilename = params.getString("lemmatizer-rule-file");						
			m_lemmatizer = new GateLemmatizer(new File(m_gateRulesFilename).toURI().toURL());
			m_lemmatizer.init();
		}	
		
	}
	
	/******************************************************************************************
	 * METHODS FOR TEXT PROCESSING
	 * ****************************************************************************************/

	public String getLemma (String keyTokenLemma, PartOfSpeech tokenPos) throws LemmatizerException, UnsupportedPosTagStringException{
		m_lemmatizer.set(keyTokenLemma,tokenPos);
		m_lemmatizer.process();
		return m_lemmatizer.getLemma();	

	}	
	
	public String getLemma (String keyTokenLemma) throws LemmatizerException {
		String lemma = "";
		for (String lemmaterm : keyTokenLemma.split(" ")){
			m_lemmatizer.set(lemmaterm);
			m_lemmatizer.process();
			lemma+=m_lemmatizer.getLemma()+" ";						
		}
		return lemma.trim();			
	}	
	
}

package eu.excitementproject.clustering.data.impl.lexicalexpander;


import java.util.HashMap;

import eu.excitementproject.clustering.data.api.AbstractLexicalExpander;
import eu.excitementproject.clustering.data.api.TextCollection;
import eu.excitementproject.eop.common.component.lexicalknowledge.LexicalRule;
import eu.excitementproject.eop.common.component.lexicalknowledge.RuleInfo;
import eu.excitementproject.eop.common.representation.partofspeech.PartOfSpeech;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFile;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationParams;
import eu.excitementproject.eop.distsim.resource.SimilarityStorageBasedLexicalResource;

/**
 * @author Lili Kotlerman
 *
 */
public class BapLexicalExpander extends AbstractLexicalExpander {

	private SimilarityStorageBasedLexicalResource bap;

	private final boolean m_REPLACE_DIGITS = true;
	private final String m_replaceDigitsWith = "@"; 
	
	final static double CONFIDENCE = 0.03;
	
	private final double m_conf_threshold = 0.01;
	
	public BapLexicalExpander(String configurationFileName, TextCollection collection) throws ConfigurationException {
		super(collection);
		init(new ConfigurationFile(configurationFileName));
	}


	private void init(ConfigurationFile conf) throws ConfigurationException {				
		
		try {
			if(conf.isModuleExist("knowledge-resource")){
				System.out.println("Creating bap expander");
				ConfigurationParams params = conf.getModuleConfiguration("knowledge-resource");			
				bap = new SimilarityStorageBasedLexicalResource(params);
				System.out.println(bap);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public String replaceDigits(String s){
		if (m_REPLACE_DIGITS){
			String s1=s.replace("0", m_replaceDigitsWith).replace("1", m_replaceDigitsWith).replace("2", m_replaceDigitsWith).replace("3", m_replaceDigitsWith).replace("4", m_replaceDigitsWith).replace("5", m_replaceDigitsWith).replace("6", m_replaceDigitsWith).replace("7", m_replaceDigitsWith).replace("8", m_replaceDigitsWith).replace("9", m_replaceDigitsWith);
			return s1;
		}
		else return s;		
	}

	@Override
	public HashMap<String,Double> getUnfilteredExpansions(String lemma, PartOfSpeech pos) throws Exception{
		HashMap<String,Double> expansions = new HashMap<String, Double>();
	//	System.out.println("bap:"+bap);
		
		for (LexicalRule<? extends RuleInfo> r: bap.getRulesForLeft(lemma, pos)){
			if (r.getConfidence()<m_conf_threshold) continue;
			expansions.put(replaceDigits(r.getRLemma().toLowerCase()),r.getConfidence());
		}
		for (LexicalRule<? extends RuleInfo> r: bap.getRulesForRight(lemma, pos)){
			if (r.getConfidence()<m_conf_threshold) continue;
			expansions.put(replaceDigits(r.getRLemma().toLowerCase()),r.getConfidence());
		}
		
		if (lemma.contains(" ")){
			// add two variations - with "-" and as signle term, to work with ukwac expansions, since there are no multiword terms there
			// e.g. "credit card" = "creditcard" and "credit-card"			
			String[] addLemmas = {lemma.replace(" ", "-"), lemma.replace(" ", "")};
			for (String additionalLemma : addLemmas){
				HashMap<String,Double> additionalExpansions = getUnfilteredExpansions(additionalLemma, pos);
				if (!additionalExpansions.isEmpty()){
					additionalExpansions.put(additionalLemma, CONFIDENCE); //if additional lemma had expansions, add this lemma itself as an expansion of the original lemma
					for (String expansion : additionalExpansions.keySet()){
						double score = additionalExpansions.get(expansion);
						if (expansions.containsKey(expansion)) score += expansions.get(expansion);
						expansions.put(expansion, score);
					}
				}				
			}
		}

		System.out.println("[bap]: "+lemma+" : "+expansions.size()+" : "+expansions.keySet());
		return expansions;
	}
		

	@Override
	public HashMap<String, Double> getUnfilteredExpansions(String lemma) throws Exception {
		HashMap<String, Double> expansions = new HashMap<String, Double>();

	/*	PartOfSpeech n = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.NOUN);
		PartOfSpeech v = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.VERB);
		PartOfSpeech a = new BySimplerCanonicalPartOfSpeech(SimplerCanonicalPosTag.ADJECTIVE);
*/
			expansions.putAll(getUnfilteredExpansions(lemma,null));
		/*	expansions.putAll(getExpansions(lemma,n));
			expansions.putAll(getExpansions(lemma,v));
			expansions.putAll(getExpansions(lemma,a)); */
		
	//	if (lemma.contains(" ")) System.out.println(lemma+" >> "+expansions);
		return expansions;
	}
}

package eu.excitementproject.tl.decomposition.fragmentannotator;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.abelssoft.wordtools.jwordsplitter.impl.GermanWordSplitter;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.excitement.type.tl.DeterminedFragment;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.tl.decomposition.exceptions.FragmentAnnotatorException;
import eu.excitementproject.tl.laputils.CASUtils;

/**
 * This class implements a simple "fragment annotator": token + compound part 
 * Namely, each token except the punctuation is considered as a possible (continuous) fragment. 
 * If a filter for parts of speech is passed to the constructor, then only tokens are annotated, which match the filter.
 * If a token is a compound word, then the compound parts can be also annotated.
 * 
 * @author Aleksandra (November 2014)
 *
 */
public class TokenAsFragmentAnnotatorForGerman extends AbstractFragmentAnnotator {

	private final List<String> tokenPOSFilter;
	private GermanWordSplitter splitter;
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param decompoundWords -- set to true if words are to decompound
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess l, boolean decompoundWords) throws FragmentAnnotatorException
	{
		super(l);
		tokenPOSFilter = null;
		if(decompoundWords){
			try {
				splitter = new GermanWordSplitter();
				splitter.setStrictMode(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param lap - The implementation may need to call LAP. The needed LAP should be passed via Constructor.
	 * @param tokenPOSFilter - types of parts of speeches of tokens, which which should be annotated 
	 * @throws FragmentAnnotatorException
	 */
	public TokenAsFragmentAnnotatorForGerman(LAPAccess l, List<String> tokenPOSFilter, boolean decompoundWords) throws FragmentAnnotatorException
	{
		super(l); 
		this.tokenPOSFilter = tokenPOSFilter;
		if(decompoundWords){
			try {
				splitter = new GermanWordSplitter();
				splitter.setStrictMode(true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void annotateFragments(JCas aJCas) throws FragmentAnnotatorException {
		
		Logger fragLogger = Logger.getLogger(this.getClass().getName()); 

		// first of all, check determined fragmentation is there or not. 
		// If it is there, we don't run and pass 
		// check the annotated data
		AnnotationIndex<Annotation> frgIndex = aJCas.getAnnotationIndex(DeterminedFragment.type);
		
		if (frgIndex.size() > 0)
		{
			fragLogger.info("The CAS already has " + frgIndex.size() + " determined fragment annotation. Won't process this CAS."); 
			return; 
		}
		
		// check if token annotation is there or not 
		AnnotationIndex<Annotation> tokenIndex = aJCas.getAnnotationIndex(Token.type);
		Iterator<Annotation> tokenItr = tokenIndex.iterator(); 		
		
		if (!tokenItr.hasNext())
		{
			// It seems that there are no tokens in the CAS. 
			// Run LAP on it. 
			fragLogger.info("No token annotation found: calling the given LAP"); 
			try 
			{
				this.getLap().addAnnotationOn(aJCas); 
			}
			catch (LAPException e)
			{
				throw new FragmentAnnotatorException("Unable to run LAP on the inputCAS: LAP raised an exception", e); 
			}
			// all right. LAP annotated. Try once again 
			tokenIndex = aJCas.getAnnotationIndex(Token.type);
			tokenItr = tokenIndex.iterator(); 		
			
			// throw exception, if still no tokens found
			if (!tokenItr.hasNext())
			{
				throw new FragmentAnnotatorException("Calling on LAPAccess " + this.getLap().getClass().getName() + " didn't added token annotation. Cannot proceed."); 
			}

		}

		fragLogger.info("Annotating determined fragments on CAS. CAS Text has: \"" + aJCas.getDocumentText() + "\"."); 
		
		int num_frag = 0; 
		
		while(tokenItr.hasNext())
		{
			//annotate each token except punctuation as one fragment, if it matches the filter 
			Token tk = (Token) tokenItr.next(); 
			int begin = tk.getBegin(); 
			int	end = tk.getEnd(); 
			CASUtils.Region[] r = new CASUtils.Region[1];
			r[0] = new CASUtils.Region(begin,  end); 
			
			try {
				if(tk.getPos()!= null){
					if(!tk.getPos().getType().getShortName().equals("PUNC")){ // no punctuation
						if (tokenPOSFilter == null || tokenPOSFilter.isEmpty() || tokenPOSFilter.contains(tk.getPos().getPosValue())){
							fragLogger.info("Annotating the following as a fragment: " + tk.getCoveredText());
							CASUtils.annotateOneDeterminedFragment(aJCas, r);
							num_frag++;
							
							if(splitter != null){
								String tokenText = tk.getCoveredText();
								Collection<String> tokenTextParts = splitter.splitWord(tokenText);
								if(tokenTextParts.size() > 1){
									for(String tokenTextPart : tokenTextParts){
										int index = tokenText.indexOf(tokenTextPart);
										begin = tk.getBegin() + index;
										end = begin + tokenTextPart.length();
										r[0] = new CASUtils.Region(begin,  end);
										fragLogger.info("Annotating the following as a fragment: " + aJCas.getDocumentText().substring(begin, end));
										CASUtils.annotateOneDeterminedFragment(aJCas, r);
										num_frag++;
										index = end + 1;
									}
								}
							}
						}
					}
				}
			}
			
			catch (LAPException e)
			{
				throw new FragmentAnnotatorException("CASUtils reported exception while annotating Fragment, on token (" + begin + ","+ end, e );
			}
			 
		}
		fragLogger.info("Annotated " + num_frag + " determined fragments"); 
	}

}


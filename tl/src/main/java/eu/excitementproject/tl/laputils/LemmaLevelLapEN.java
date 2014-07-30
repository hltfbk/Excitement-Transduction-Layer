package eu.excitementproject.tl.laputils;

import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;
import eu.excitementproject.eop.lap.implbase.LAP_ImplBaseAE;

/**
 * 
 * LAP based on TreeTagger and a tokenizer. 
 * Roughly Same as EOP's TreeTaggerEN pipeline --- but EOP 1.1.3 TreeTagger's 
 * tokenizer (open nlp tokenizer) makes several problems and we use our own TL 
 * version to counter that... 
 *  
 * @author Gil
 *
 */
public class LemmaLevelLapEN extends LAP_ImplBaseAE implements LAPAccess 
{
	
	public LemmaLevelLapEN() throws LAPException
	{
		// 1) prepare AEs
		AnalysisEngineDescription[] descArr = new AnalysisEngineDescription[2];
		try
		{
			descArr[0] = createPrimitiveDescription(BreakIteratorSegmenter.class);
			descArr[1] = createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class);
		}
		catch (ResourceInitializationException e)
		{
			throw new LAPException("Unable to create AE descriptions", e);
		}

		// 2) initialize Views
		initializeViews(descArr);

		// 3) set lang ID
		languageIdentifier = "EN"; // set languageIdentifer, this ID is needed for generateTHPair from String 
	}	
}

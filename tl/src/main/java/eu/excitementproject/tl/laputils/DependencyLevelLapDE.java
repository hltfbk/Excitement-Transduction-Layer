package eu.excitementproject.tl.laputils;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.maltparser.MaltParser;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;
import eu.excitementproject.eop.lap.LAPAccess;
import eu.excitementproject.eop.lap.LAPException;
import eu.excitementproject.eop.lap.implbase.LAP_ImplBaseAE;

public class DependencyLevelLapDE extends LAP_ImplBaseAE implements LAPAccess {

	public DependencyLevelLapDE() throws LAPException
	{
		// 1) prepare AEs
		AnalysisEngineDescription[] descArr = new AnalysisEngineDescription[3];
		try
		{
			descArr[0] = createPrimitiveDescription(BreakIteratorSegmenter.class);
			descArr[1] = createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class);
			descArr[2] = createPrimitiveDescription(MaltParser.class,
					MaltParser.PARAM_VARIANT, null,
					MaltParser.PARAM_PRINT_TAGSET, true);
		}
		catch (ResourceInitializationException e)
		{
			throw new LAPException("Unable to create AE descriptions", e);
		}

		// 2) initialize Views
		initializeViews(descArr);

		// 3) set lang ID
		languageIdentifier = "IT"; // set languageIdentifer, this ID is needed for generateTHPair from String 
	}	

}

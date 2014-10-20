package eu.excitementproject.clustering.data.util;

import eu.excitementproject.eop.common.representation.partofspeech.CanonicalPosTag;
import eu.excitementproject.eop.common.representation.partofspeech.PartOfSpeech;
import eu.excitementproject.eop.common.representation.partofspeech.UnsupportedPosTagStringException;

public class FakePartOfSpeech extends PartOfSpeech
// copy-pasted from ac.biu.nlp.nlp.instruments.lemmatizer.DemoGateLemmatizer.FakePartOfSpeech;
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5721876040837720311L;

	public FakePartOfSpeech(String posTagString)
			throws UnsupportedPosTagStringException
	{
		super(posTagString);
	}

	@Override
	protected void setCanonicalPosTag()
	{			
		if (this.posTagString.equals("v"))
			canonicalPosTag = CanonicalPosTag.V;
		else if (this.posTagString.equals("n"))
			canonicalPosTag = CanonicalPosTag.N;
		else
			canonicalPosTag = CanonicalPosTag.OTHER;
	}

	@Override
	protected void validatePosTagString(String posTagString)
			throws UnsupportedPosTagStringException
	{
		// do nothing
	}

	@Override
	public PartOfSpeech createNewPartOfSpeech(String posTagString) throws UnsupportedPosTagStringException
	{
		return new FakePartOfSpeech(posTagString);
	}
}
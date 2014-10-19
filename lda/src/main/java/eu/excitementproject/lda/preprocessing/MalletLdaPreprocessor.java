package eu.excitementproject.lda.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface MalletLdaPreprocessor {

	public Map<String,Set<String>> preprocessData(File extractionFile, File outFile) throws IOException;
}

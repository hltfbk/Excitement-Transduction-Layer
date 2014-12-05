package eu.excitementproject.tl.experiments.Semeval;


/**
 * 
 * @author ??
 *
 */
public class SemevalStuff {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String gsAnnotationsDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ITA-SPLIT-2014-03-14-FINAL/Test";
	//	String gsAnnotationsDir = "D:/LiliGit/Excitement-Transduction-Layer/tl/src/test/resources/WP2_gold_standard_annotation/GRAPH-ENG-SPLIT-2014-03-24-FINAL/Test";
	
	//	GoldStandardAnalyzer.getStatistics(gsAnnotationsDir);
		GoldStandardToWP2FormatTranslator t = new GoldStandardToWP2FormatTranslator();
		t.createWP2Data(gsAnnotationsDir);
		
	}
	
	
		
}

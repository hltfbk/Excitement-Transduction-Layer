package eu.excitementproject.tl.demo;

import eu.excitementproject.eop.core.MaxEntClassificationEDA;

import eu.excitementproject.tl.laputils.LemmaLevelLapIT;

public class DemoUseCase1ALMAItalian extends UseCaseOneDemo {

	
	
	public DemoUseCase1ALMAItalian(String configFileName, String dataDir, int fileNumberLimit,
			String outputFolder, Class<?> lapClass, Class<?> edaClass) {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass, edaClass);
		// TODO Auto-generated constructor stub
	}
	

	public DemoUseCase1ALMAItalian(){
				
		super("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_IT.xml",
				"./src/test/resources/WP2_public_data_CAS_XMI/ALMA_social_media", 4,
				"./src/test/outputs/WP2_public_data_CAS_XMI/ALMA_social_media", 
				LemmaLevelLapIT.class, MaxEntClassificationEDA.class);
	}
	
	public static void main(String[] argv) {
		DemoUseCase1ALMAItalian demoIT = new DemoUseCase1ALMAItalian();
		demoIT.inspectResults();
	}
}

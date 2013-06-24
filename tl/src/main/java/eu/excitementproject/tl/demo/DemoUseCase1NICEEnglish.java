package eu.excitementproject.tl.demo;

import eu.excitementproject.eop.core.MaxEntClassificationEDA;
import eu.excitementproject.eop.lap.dkpro.OpenNLPTaggerEN;
import eu.excitementproject.tl.usecaseone.UseCaseOneDemo;

public class DemoUseCase1NICEEnglish extends UseCaseOneDemo{

	public DemoUseCase1NICEEnglish(String configFileName, String dataDir, String outputFolder, Class<?> lapClass, Class<?> edaClass) {
		super(configFileName, dataDir, outputFolder, lapClass, edaClass);
	}
	
	
	public DemoUseCase1NICEEnglish() {
		
		super("./src/test/resources/EOP_configurations/MaxEntClassificationEDA_Base_EN.xml",
				"./src/test/resources/WP2_public_data_CAS_XMI/nice_email_1",
				"./src/test/outputs/WP2_public_data_CAS_XMI/nice_email_1",
				OpenNLPTaggerEN.class,
				MaxEntClassificationEDA.class
				);
	}
	
	public static void main(String[] argv) {
		DemoUseCase1NICEEnglish demoEN = new DemoUseCase1NICEEnglish();
		demoEN.inspectResults();
	}
	
}

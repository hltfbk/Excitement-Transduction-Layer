package eu.excitementproject.tl.demo;

import eu.excitementproject.eop.core.MaxEntClassificationEDA;

import eu.excitementproject.eop.lap.dkpro.TreeTaggerEN;

public class DemoUseCase1NICEEnglish extends UseCaseOneDemo{

	public DemoUseCase1NICEEnglish(String configFileName, String dataDir, int fileNumberLimit, String outputFolder, Class<?> lapClass, Class<?> edaClass) {
		super(configFileName, dataDir, fileNumberLimit, outputFolder, lapClass, edaClass);
	}
	
	
	public DemoUseCase1NICEEnglish() {
				
		// to run for a different cluster change the cluster name
		super("./src/test/resources/NICE_experiments/MaxEntClassificationEDA_Base+WN+VO_EN.xml",
				"./src/test/resources/WP2_public_data_CAS_XMI/NICE_EMAIL_TEST2_perFrag/EMAIL0001", Integer.MAX_VALUE,
				"./src/test/outputs/WP2_public_data_CAS_XMI/NICE_EMAIL_TEST2_perFragg/EMAIL0001",
				TreeTaggerEN.class,
				MaxEntClassificationEDA.class
				);

	}
	
	public static void main(String[] argv) {

		// run the flow, create raw and collapsed entailment graphs and save them to files
		DemoUseCase1NICEEnglish demoEN = new DemoUseCase1NICEEnglish();		
		demoEN.inspectResults();
		// the results can be found under "./src/test/outputs/WP2_public_data_CAS_XMI/nice_email_1"
		
		// now load some existing collapsed graph (we load one with 10 interactions)
		
/*		File xmlFile = new File("./src/test/outputs/WP2_public_data_CAS_XMI/nice_email_1/first_10/collapsed_graph.xml");
		try {
			EntailmentGraphCollapsed graph = new EntailmentGraphCollapsed(xmlFile);
			logger.info("\nLoaded the graph from ./src/test/outputs/WP2_public_data_CAS_XMI/nice_email_1/first_10/collapsed_graph.xml");

			logger.info(graph.toString());
			
			logger.info("\nThe number of nodes is: "+graph.getNumberOfEquivalenceClasses());
			logger.info("The overall number of entailment units is: "+graph.getNumberOfEntailmentUnits());
			logger.info("The number of (complete) textual inputs is: "+graph.getNumberOfTextualInputs());
			

			// get some example entailment unit
			// we use its canonical text (note that the unit itself can also be passed as the parameter to the methods below)
			String euText = ""; 
			if (graph.vertexSet().iterator().hasNext()) {
				euText = graph.vertexSet().iterator().next().getLabel();
			}
			
			logger.info("\nShow equivalent statements for \""+euText+"\":");
			int i=1;
			for (EntailmentUnit eu : graph.getEquivalentEntailmentUnits(euText)){
				logger.info("\t"+i+") "+eu.getText());
				i++;
			}
			
			logger.info("\nShow entailing statements for \""+euText+"\":");
			i=1;
			for (EquivalenceClass node: graph.getEntailingEquivalenceClasses(euText)){
				for (EntailmentUnit eu : node.getEntailmentUnits()){
					logger.info("\t"+i+") "+eu.getText());
					i++;
				}
			}
			
			logger.info("\nShow entailed statements for \""+euText+"\":");
			i=1;
			for (EquivalenceClass node: graph.getEntailedEquivalenceClasses(euText)){
				for (EntailmentUnit eu : node.getEntailmentUnits()){
					logger.info("\t"+i+") "+eu.getText());
					i++;
				}
			}
			
			logger.info("\nShow relevant interaction ids for \""+euText+"\":");
			i=1;
			for (String id: graph.getRelevantInteractionIDs(euText)){
				System.out.print(id+"  ");
				if (i%5==0) logger.info();
				i++;				
			}
			logger.info();
			
			logger.info("\nShow top-3 sorted nodes:");
			i=1;
			for (EquivalenceClass node: graph.sortNodesByNumberOfInteractions(3)){
				System.out.print("  node#"+i+": "+node.toString());
				i++;				
			}
			
		} catch (EntailmentGraphCollapsedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/		
	}
	
}

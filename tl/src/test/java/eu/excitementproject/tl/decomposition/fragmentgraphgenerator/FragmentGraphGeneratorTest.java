package eu.excitementproject.tl.decomposition.fragmentgraphgenerator;

import static org.junit.Assert.*;


import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import javax.swing.filechooser.FileFilter;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;

import eu.excitementproject.tl.laputils.CASUtils;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;

/**
 * 
 * @author ??
 *
 */
@SuppressWarnings("unused")
public class FragmentGraphGeneratorTest {
	
	private final Logger logger = Logger.getLogger(this.getClass());

	/**
	 *  The test class tests FragmentGraphGenerator 
	 */
	@Test
	public void test() {
		
		try {
			logger.info("Testing the FragmentGraphGenerator");
			
			// first, call CASUtils to generate InputCAS examples. 		
			CASUtils.generateExamples(); // the examples are stored in /target. 
			
			// generate fragment graphs from each of the inputCAS examples.  
			JCas aJCas = CASUtils.createNewInputCas(); 
			
			// initiate the FragGraphGenerator... 
			FragmentGraphGeneratorFromCAS fragGen = new FragmentGraphGeneratorFromCAS(); 
//			FragmentGraphLiteGeneratorFromCAS fragGen = new FragmentGraphLiteGeneratorFromCAS(); 

			File file = new File("./src/test/resources/WP2_public_data_CAS_XMI/NICE_reAnnotated/perFrag/test/EMAIL0001/228464.txt_3.xml.graphf3output.xml.xmi");
			
			// Read in inputCASes for the examples, and generate the FragmentGraphs 
			CASUtils.deserializeFromXmi(aJCas, file); 
			Set<FragmentGraph> fgs_example = fragGen.generateFragmentGraphs(aJCas); 
			
			Assert.assertNotNull(fgs_example); 
			Assert.assertTrue(fgs_example.size() > 0);
			logger.info("\n________________\nFragment graphs for the given example: ");
			for(FragmentGraph f: fgs_example) {
				logger.info(f.toString());
			}
			
			
// generate fragment graphs from files in a given directory			
			
/*			File dir = new File("./src/test/resources/WP2_public_data_CAS_XMI/nice_speech");
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File directory, String fileName) {
					return fileName.endsWith(".xmi");
				}
			};
			
			logger.info("Processing directory " + dir.getName());
			
			for(File f: dir.listFiles(filter)) {
				// Read in inputCASes for the examples, and generate the FragmentGraphs 
				
				logger.info("Processing fragments from file " + f.getName());
				
				CASUtils.deserializeFromXmi(aJCas, f); 
				Set<FragmentGraph> fgs_example = fragGen.generateFragmentGraphs(aJCas);
				
//				Assert.assertNotNull(fgs_example); 
//				Assert.assertTrue(fgs_example.size() > 0);
				if (fgs_example == null || fgs_example.size() <= 0) {
					logger.info("\n________________\nNO fragment graphs for file " + f.getName());
				} else {
					logger.info("\n________________\nFragment graphs for file " + f.getName() + " (" + fgs_example.size() + ")");
					for(FragmentGraph fg: fgs_example) {
						logger.info(fg.toString());
					}
				}
			}
*/			
				
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage()); 
		}
	}

}

package eu.excitementproject.tl.evaluation.graphmerger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author NICE Systems
 *
 */
public class ClusterStatistics {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length != 1)
		{
			usage();
			System.exit(-1);
		}
		
		final File clusterDir = new File(args[0]);
		if (!clusterDir.exists() || !clusterDir.isDirectory())
		{
			System.err.println("Wrong parameter");
			System.exit(-1);
		}
		
		processAll(clusterDir);
	}
	
	private static void usage()
	{	
		System.err.println("java Extractor <cluster_dir>");
	}

	private static void processAll(File dir)
	{
		File[] clusters = dir.listFiles();
		for (File cluster : clusters){
			if (cluster.isDirectory()){
				String clusterName = cluster.getName().toLowerCase();
				File clusterGraph = new File(cluster.getAbsolutePath(),clusterName + ".xml");
				try {
					processCluster(clusterGraph);
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println(clusterGraph.getName() + "\tError");
				}
			}
		}
	}
	
	public static void processCluster(File cluster) 
			throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(cluster);
	 
		//optional, but recommended
		//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("edge");
		int noEdge = nList.getLength();
		int newEdge = 0;
		int yesEdge = 0;
		int biEdge = 0;
		HashSet<String> nodes = new HashSet<String>();
		HashSet<String> yesEdges = new HashSet<String>();
		for (int i=0;i<noEdge;i++){
			Element fragment = (Element)nList.item(i);
			String source = fragment.getAttribute("source");
			String target = fragment.getAttribute("target");
			if (!(source.substring(0,source.lastIndexOf("_")).
					equals(target.substring(0,target.lastIndexOf("_"))))){
				newEdge++;
				if (!(nodes.contains(source))) nodes.add(source);
				if (!(nodes.contains(target))) nodes.add(target);
				String id = source + "+" + target;
				String revId = target + "+" + source;
				if (yesEdges.contains(revId))
				{
					biEdge++;
					yesEdge--;
					yesEdges.remove(revId);
				}
				else {
					yesEdge++;
					yesEdges.add(id);
				}
			}
		}
		System.err.println(cluster.getName() + "\t" + noEdge + "\t" + newEdge + "\t" + yesEdge + 
				"\t" + yesEdges.size() + "\t" + biEdge + "\t" + nodes.size());
	}
}

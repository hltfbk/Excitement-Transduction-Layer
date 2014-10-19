package tmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.util.Distance;

import eu.excitementproject.clustering.clustering.impl.completeLink.util.SemanticDistance;
import eu.excitementproject.eop.common.datastructures.Pair;
import eu.excitementproject.eop.common.datastructures.PairMap;

public class ClusterGraph {

	public ClusterGraph() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String,String> nodes = new HashMap<String,String>();
		PairMap<String, Double> map;
		try {
			BufferedReader r = new BufferedReader(new FileReader(new File("D:/LiliEclipseWorkspace/clustering/src/main/java/tmp/nodes.txt")));
			String line = r.readLine();
			while(line != null){
				String[] s = line.split("\t");
				nodes.put(s[0], s[1]);
				line = r.readLine();
			}
			r.close();
			System.out.println(nodes);
			
			r = new BufferedReader(new FileReader(new File("D:/LiliEclipseWorkspace/clustering/src/main/java/tmp/edges.txt")));
			map = new PairMap<String, Double>();
			line = r.readLine();
			while(line != null){
				String[] s = line.split("\t");
				String n1 = s[0];
				String n2 = s[1];
				map.put(new Pair<String>(n1, n2), 1.0);
				line = r.readLine();
			}
			r.close();
			System.out.println(map);

		
			// cluster all the terms in the collection (original + expansions) using semantic relatedness to measure distance
			Distance<String> distance = new SemanticDistance(map);
			Set<String> textNodes = new HashSet<String>(nodes.keySet());
			Map<String,List<String>> res = termClusterer(distance, textNodes, 15);
			for (String cluster : res.keySet()){
				System.out.println("Cluster " +cluster+": "+res.get(cluster).size()+ " fragments");
				for (String id : res.get(cluster)){
					System.out.println("\t"+id+": "+nodes.get(id));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	}

	private static Map<String,List<String>> termClusterer(Distance<String> distance, Set<String> terms, int topKClusters){
		CompleteLinkClusterer<String> clusterer = new CompleteLinkClusterer<String>(1, distance);
	
		Map<String,List<String>> clusteringResults = new HashMap<String, List<String>>();
		int clusterId = 1;
		for(Set<String> cluster : clusterer.hierarchicalCluster(terms).partitionK(topKClusters)){
			List<String> termsInCuster = new LinkedList<String>(cluster);
			clusteringResults.put(String.valueOf(clusterId), termsInCuster);
			clusterId++;
		}
		return clusteringResults;
	}

}

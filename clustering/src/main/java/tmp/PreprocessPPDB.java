package tmp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import eu.excitementproject.eop.common.utilities.configuration.ConfigurationException;
import eu.excitementproject.eop.common.utilities.configuration.ConfigurationFileDuplicateKeyException;
import eu.excitementproject.eop.lap.biu.lemmatizer.LemmatizerException;

public class PreprocessPPDB {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SimplePreprocessor p = new SimplePreprocessor("D://LiliEclipseWorkspace//clustering//configurations//Experiment1-conf.xml");
			File dir = new File("D://Lili//lexRes//processed");
			File[] resources = dir.listFiles();
			for (File res : resources){
				if (res.getName().endsWith(".txt")){
					System.out.println("\n*****************************\n"+res.getName());
					BufferedReader r = new BufferedReader(new FileReader(res)); 
					BufferedWriter w = new BufferedWriter(new FileWriter(new File(res.getAbsolutePath().replace(".txt", "_lemmatized.txt"))));
					BufferedWriter l = new BufferedWriter(new FileWriter(new File(res.getAbsolutePath().replace(".txt", "_lost_in_lemmatization.txt"))));
					int i = 1;
					int lost=0;
					String line = r.readLine();
					while(line != null){
						if (i%50000==0) System.out.println(i+": "+line);
						String[] s = line.split("\t");
						if (s.length<6){
							line = r.readLine();
							continue;
						}
//						double giga = Double.valueOf(s[4]);
//						double google = Double.valueOf(s[5]);
//						if (giga*google > 0){
							String lhs = p.getLemma(s[0]);
							String rhs = p.getLemma(s[1]);
							if (!lhs.equals(rhs)){
								w.write(lhs+"\t"+rhs+"\t"+s[2]+"\t"+s[3]+"\t"+s[4]+"\t"+s[5]+"\n");
							}
							else {
								l.write("same "+i+": "+line+"\n");
								lost++;
							}
//						}
//						else {
//							l.write("zero "+i+": "+line+"\n");
//							lost++;
//						}
						i++;
						line = r.readLine();
					}
					r.close();
					w.close();
					l.close();
					System.out.println("\n\n"+lost);
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationFileDuplicateKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LemmatizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

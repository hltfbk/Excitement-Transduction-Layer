/**
 * 
 */
package eu.excitementproject.lda.rep.records;

/**
 * @author Jonathan Berant & Lili Kotlerman
 *
 */
public class TopicProbGivenDocAndTermRecord extends LineRecord {
	
	public String docName;
	public String term; // term 
	public short topic;
	public double probability;
	
	static public String getHeaderLines() {
		return TYPE + "\n" + LINE_LABELS + "\n";
	}
	
	static private final String TYPE = TopicProbGivenDocAndTermRecord.class.getName();
	public static final String LINE_LABELS = "DOCUMENT\tTERM\tTOPIC\tPROBABILITY";

	
	public TopicProbGivenDocAndTermRecord(String docName, String term, short topic, double probability) {
		this.docName = docName;
		this.term = term;
		this.topic = topic;
		this.probability = probability;
		// TODO Auto-generated constructor stub
	}
	
	public TopicProbGivenDocAndTermRecord(String line) throws RecordException {
		fromLine(line);
	}

	@Override
	public void fromLine(String line) throws RecordException {
		
		String[] tokens = line.split("\t");
		docName = tokens[0];
		term = tokens[1];
		topic = Short.parseShort(tokens[2]);
		probability = Double.parseDouble(tokens[3]);
	}

	@Override
	public String toLine() {
		
		return (docName + "\t" + term + "\t" + topic + "\t" + probability);
		
	}

}

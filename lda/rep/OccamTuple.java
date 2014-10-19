package ac.biu.nlp.graph.untyped.preprocessing.rep;


/**
 * This class creates tuples from occam files
 * @author User
 *
 */
public class OccamTuple implements Tuple {

	public OccamTuple(String occamLine) {
		
		String[] tokens = occamLine.split("\t");
		m_count = Integer.parseInt(tokens[3]);
		m_arg1 = tokens[0];
		m_predicate = tokens[1];
		m_arg2 = tokens[2];
	}
	
	@Override
	public String toString() {
		return m_arg1+"\t"+m_predicate+"\t"+m_arg2+"\t"+m_count;
	}
	
	public String arg1() {
		return m_arg1;
	}

	public String arg2() {
		return m_arg2;
	}

	public String predicate() {
		return m_predicate;
	}

	public int count() {
		return m_count;
	}
	
	@Override
	public void setArg1(String arg1) {
		m_arg1 = arg1;
	}

	@Override
	public void setArg2(String arg2) {
		m_arg2 = arg2;
	}

	@Override
	public void setPred(String pred) {
		m_predicate = pred;
	}

	private String m_arg1;
	private String m_arg2;
	private String m_predicate;
	private int m_count;
	
}

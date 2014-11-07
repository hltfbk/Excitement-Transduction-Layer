package ac.biu.nlp.graph.untyped.preprocessing.rep;

/**
 * This interface is the standard representation for tuples
 * @author Jonathan
 * @date 13/7/11
 *
 */
public interface Tuple {
	public String arg1();
	public String arg2();
	public String predicate();
	public int count();
	public void setArg1(String arg1);
	public void setArg2(String arg2);
	public void setPred(String pred);
}

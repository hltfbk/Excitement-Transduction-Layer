/**
 * 
 */
package eu.excitementproject.lda.rep.records;

/**
 * @author Jonathan Berant
 *
 */
public abstract class LineRecord {
	
	abstract public void fromLine(String line) throws RecordException;
	abstract public String toLine();

	protected String[] parseLine(String line) {
		return (line.split("\t"));		
	}
	
}

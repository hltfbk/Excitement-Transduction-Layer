/**
 * 
 */
package eu.excitementproject.lda.rep;


/**
 * @author Oren Melamud
 *
 */
public class DocTermTuple {
	
		public int doc;
		public int term;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + term;
			result = prime * result + doc;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DocTermTuple other = (DocTermTuple) obj;
			if (term != other.term)
				return false;
			if (doc != other.doc)
				return false;
			return true;
		}
		
			
	
	

}

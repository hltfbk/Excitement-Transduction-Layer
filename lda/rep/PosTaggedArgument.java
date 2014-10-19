package ac.biu.nlp.graph.untyped.preprocessing.rep;

import ac.biu.nlp.normalization.BiuNormalizer;

public class PosTaggedArgument implements Comparable<PosTaggedArgument>{

	private String m_arg; //original predicate extracted by reverb
	private String m_normArg; 
	private String m_posArg;

	public PosTaggedArgument(String pred, String normPred, String posPred) {
		this.m_arg = pred;
		this.m_normArg = normPred;
		this.m_posArg = posPred;
	}

	public void setPosTaggedPredicate(PosTaggedArgument other) {
		this.m_arg = other.m_arg;
		this.m_normArg = other.m_normArg;
		this.m_posArg = other.m_posArg;
	}

	private void clearPredicate() {
		m_arg = "";
		m_normArg = "";
		m_posArg = "";
	}

	public String getArg() {
		return m_arg;
	}

	public void setArg(String arg) {
		this.m_arg = arg;
	}

	public String getNormArg() {
		return m_normArg;
	}

	public void setNormArg(String normArg) {
		this.m_normArg = normArg;
	}

	public String getPosPred() {
		return m_posArg;
	}

	public void setPosArg(String posArg) {
		this.m_posArg = posArg;
	}

	@Override
	public String toString() {
		return  m_arg + "\t" + m_normArg + "\t" + m_posArg;
	}

	@Override
	/**
	 * hascode is determined by the original predicate alone
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((m_arg == null) ? 0 : m_arg.hashCode());
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
		PosTaggedArgument other = (PosTaggedArgument) obj;
		if (m_arg == null) {
			if (other.m_arg!= null)
				return false;
		} else if (!m_arg.equals(other.m_arg))
			return false;
		return true;
	}

	@Override
	public int compareTo(PosTaggedArgument o) {
		return m_arg.compareTo(o.m_arg);
	}
	
	public void correctNormalization(BiuNormalizer normalizer) throws Exception {
		
		if(startsWithBadCharacter(m_normArg))
			clearPredicate();
		else {
	
			m_normArg = m_normArg.toLowerCase();
			String[] normArgTokens = m_normArg.split("\\s+");
			String[] posArgTokens = m_posArg.split("\\s+");
			StringBuilder newNormArg = new StringBuilder();
			
			for(int i = 0; i < normArgTokens.length; i++) {
				
				if(normArgTokens[i].equals("an")) {
					newNormArg.append("a ");
				}
				else if(posArgTokens[i].equals("JJ")) {
					
					StringBuilder tempBuilder = new StringBuilder();
					tempBuilder.append(normArgTokens[i]+" ");
					
					int j = i+1;
					boolean omit = false;

					for(; j < posArgTokens.length;j++) {
						
						tempBuilder.append(normArgTokens[j]+" ");
						if(posArgTokens[j].equals("NN") || posArgTokens[j].equals("NNP") || 
								posArgTokens[j].equals("NNS") || posArgTokens[j].equals("NNPS")) {
							omit = true;
							break;
						}
						else if(!posArgTokens[j].equals("JJ") && !posArgTokens[j].equals("CC") &&
								!posArgTokens[j].equals(",")) 
							break;
						
					}
					
					if(omit) {
						newNormArg.append(normArgTokens[j]+" ");
						i=j;
					}
					else {
						newNormArg.append(tempBuilder.toString());
						i=j;
					}
				}
				else {
					newNormArg.append(normArgTokens[i]+" ");
				}
			}
			
			String numberNormalized = normalizer.normalize(newNormArg.toString().trim());
			m_normArg = clusterNumber(numberNormalized);	
			if(startsWithBadCharacter(m_normArg))
				clearPredicate();
		}
	}

	private boolean startsWithBadCharacter(String str) {

		
		return (str.startsWith("#") ||
				str.startsWith("$ $ ") ||
				str.startsWith("&") ||
				str.startsWith("\'\'") ||
				str.startsWith("*") ||
				str.startsWith("-") ||
				str.startsWith("~") ||
				str.startsWith("!") ||
				str.startsWith(".") ||
				str.startsWith(",") ||
				str.startsWith("+")) ;
	}
	
	private static String clusterNumber(String str) {
		
		String[] words = str.split(" ");
		for(int i = 0; i < words.length; ++i) {
			
			try {
				Integer number = Integer.parseInt(words[i]);
				if(number<3); //do nothing
				else if(number >=3 && number <=99) 
					words[i] = "3-99";
				else if(number >=100 && number <=9999) 
					words[i] = "100-9999";
				else 
					words[i] = "10000+";
				
			}
			catch(NumberFormatException e) {
				try {
					Double real = Double.parseDouble(words[i]);
					if(real >=0 && real <=100) 
						words[i] = "0.0-100.0";
					else if(real >=99 && real <10000) 
						words[i] = "100.0-10000.0";
					else 
						words[i] = "10000.0+";
				}
				catch(NumberFormatException err) {
					
				}		
			}
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < words.length;++i)
			sb.append(words[i]+" ");
		return sb.toString().trim();
		
	}

}

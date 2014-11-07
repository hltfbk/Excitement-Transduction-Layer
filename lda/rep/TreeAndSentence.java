package ac.biu.nlp.graph.untyped.preprocessing.rep;

import java.io.Serializable;

import ac.biu.nlp.nlp.instruments.parse.tree.dependency.english.EnglishNode;

public class TreeAndSentence implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private EnglishNode m_tree;
	private String m_sentence;
	
	public TreeAndSentence(EnglishNode tree, String sentence) {
		m_tree = tree;
		m_sentence = sentence;
	}

	public EnglishNode getTree() {
		return m_tree;
	}
	
	public String getSentence() {
		return m_sentence;
	}

}

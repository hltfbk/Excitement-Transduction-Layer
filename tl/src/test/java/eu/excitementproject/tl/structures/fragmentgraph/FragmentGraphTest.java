package eu.excitementproject.tl.structures.fragmentgraph;

import java.util.HashSet;
import java.util.Set;

//import org.junit.Ignore;
import org.junit.Test;

public class FragmentGraphTest {

	@Test
	public void test() {
		String text = "The hard old seats were very uncomfortable";
		Set<String> modifiers = new HashSet<String>();
		modifiers.add("hard");
		modifiers.add("old");
		modifiers.add("very");
		FragmentGraph g = new FragmentGraph(text,modifiers);
				
		System.out.println("Graph: \n" + g.toString());
		
		System.out.println("Base statement: \n" + g.getBaseStatement().getText());
		System.out.println("Top statement: \n" + g.getCompleteStatement().getText());
		
		for(EntailmentUnitMention eum: g.vertexSet()) {
			System.out.println("text: " + eum.getText() + " (level " + eum.getLevel() + ")" );
			for(SimpleModifier sm: eum.getModifiers()) {
				System.out.println("\t" + sm.getText() + " (" + sm.getStart() + " -- " + sm.getEnd() + ")");
			}
		}
	}
}

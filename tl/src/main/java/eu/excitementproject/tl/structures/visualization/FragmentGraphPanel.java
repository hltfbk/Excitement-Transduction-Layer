package eu.excitementproject.tl.structures.visualization;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.ext.JGraphModelAdapter;

import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphEdge;

public class FragmentGraphPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3909032936246720776L;
	JPanel graphPane;
	JGraph frag;
	
	FragmentGraph fg;

    private JGraphModelAdapter<EntailmentUnitMention,FragmentGraphEdge> gma;

    private static final Dimension DEFAULT_SIZE = new Dimension( 1000, 700 );

		
    public FragmentGraphPanel() {
    	init();
    	addGraphToPanel();
    	this.add(graphPane);
	}
	
    public void init() {
        // create a JGraphT graph
    	fg = makeSampleGraph();
    	
        // create a visualization using JGraph, via an adapter
        gma = new JGraphModelAdapter<EntailmentUnitMention,FragmentGraphEdge>( fg );

        frag = new JGraph( gma );
    }

//    private ListenableDirectedGraph<EntailmentUnitMention,FragmentGraphEdge> makeSampleGraph() {
    private FragmentGraph makeSampleGraph() {

		String text = "The hard old seats were very uncomfortable";
		Set<String> modifiers = new HashSet<String>();
		modifiers.add("hard");
		modifiers.add("old");
		modifiers.add("very");
		FragmentGraph g = new FragmentGraph(text,modifiers);
		
		System.out.println("fragment graph: " + g.toString());
		
		return g;
    }

    private void addGraphToPanel() {
      	graphPane = new JPanel();
    	graphPane.add( frag );
    	graphPane.setSize(DEFAULT_SIZE);
    	graphPane.setBounds(0, 0, DEFAULT_SIZE.width, DEFAULT_SIZE.height);
	
    	int skip = 350;
    	for (int i = fg.getMaxLevel(); i >= 0; i--) {
    		int x = 10;
    		for(EntailmentUnitMention eum: fg.getNodes(i)) {
    			if (eum == null) {
    				System.out.println("error reading nodes");
    				System.exit(0);
    			}
    			positionVertexAt(eum, x, i * 250 - 45 * ((int) x/skip) );
    			x += skip;
    		}
    	}
    }
    

    private void positionVertexAt( Object vertex, int x, int y ) {
 
       	String text = ((EntailmentUnitMention) vertex).getText();
        
    	System.out.println("Positioning node: <" + text + "> at ("+ x + "," + y + ")");
    	
        DefaultGraphCell cell = gma.getVertexCell( vertex );
        if (cell != null) {
        	Map<?, ?>              attr = cell.getAttributes(  );
//        	Rectangle        b    = (Rectangle) GraphConstants.getBounds( attr );
//        	GraphConstants.setBounds( attr, new Rectangle( x, y, b.width, b.height ) );

        	GraphConstants.setBounds(attr, new Rectangle(x,y,text.length() * 8,20));
        	
        	Map<DefaultGraphCell, Map<?, ?>> cellAttr = new HashMap<DefaultGraphCell, Map<?, ?>>(  );
        	cellAttr.put( cell, attr );
        	gma.edit( cellAttr, null, null, null);
        } else {
        	System.err.println("Null cell!");
        }
    }
    
    
    
	
}

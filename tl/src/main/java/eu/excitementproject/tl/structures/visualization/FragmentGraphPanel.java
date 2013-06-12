package eu.excitementproject.tl.structures.visualization;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgrapht.graph.AbstractGraph;

import eu.excitementproject.tl.structures.fragmentgraph.EntailmentUnitMention;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraph;
import eu.excitementproject.tl.structures.fragmentgraph.FragmentGraphEdge;

public class FragmentGraphPanel extends TLGraphPanel<EntailmentUnitMention,FragmentGraphEdge> {


	/**
	 * 
	 */
	private static final long serialVersionUID = -3909032936246720776L;

	public FragmentGraphPanel(
			AbstractGraph<EntailmentUnitMention, FragmentGraphEdge> g) {
		super(g);
		// TODO Auto-generated constructor stub
	}
	
	public FragmentGraphPanel() {
		super();
		addGraph(FragmentGraph.getSampleGraph());
	}
	
//    private ListenableDirectedGraph<EntailmentUnitMention,FragmentGraphEdge> makeSampleGraph() {


    protected void addGraphToPanel() {
      	graphPane = new JPanel();
    	graphPane.add( jgraph );
    	graphPane.setSize(DEFAULT_SIZE);
    	graphPane.setBounds(0, 0, DEFAULT_SIZE.width, DEFAULT_SIZE.height);
	
    	removeEdgeLabels(g.edgeSet());
    	
    	int skip = 350;
    	for (int i = ((FragmentGraph) g).getMaxLevel(); i >= 0; i--) {
    		int x = 10;
    		for(EntailmentUnitMention eum: ((FragmentGraph) g).getNodes(i)) {
    			if (eum == null) {
    				System.out.println("error reading nodes");
    				System.exit(0);
    			}
    			positionVertexAt(eum, x, i * 250 - 45 * ((int) x/skip) );
    			x += skip;
    		}
    	}
    }
    

    protected void positionVertexAt( Object vertex, int x, int y ) {
 
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
